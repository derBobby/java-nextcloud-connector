package eu.planlos.javanextcloudconnector.service;

import eu.planlos.javanextcloudconnector.config.NextcloudApiConfig;
import eu.planlos.javanextcloudconnector.model.*;
import eu.planlos.javautilities.GermanStringsUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static eu.planlos.javanextcloudconnector.model.NextcloudException.USERID_NOT_POSSIBLE;

@Slf4j
@Service
public class NextcloudApiUserService extends NextcloudApiService {

    private static final String SUCCESS_MESSAGE_CREATE_USER = "Created user: {}";
    private static final String SUCCESS_API_INACTIVE = "No user created: API is inactive";
    private static final String FAIL_MESSAGE_CREATE_USER = "Could not create  user: {}, Error: {}";
    private static final String FAIL_MESSAGE_GET_USERS = "Could not load users from nextcloud";
    private static final String NC_API_JSON_SUFFIX = "?format=json";
    private static final String NC_API_USERS_URL = "/ocs/v1.php/cloud/users";
    public static final String NC_API_USERLIST_JSON_URL = NC_API_USERS_URL + NC_API_JSON_SUFFIX;
    public static final String NC_API_USER_JSON_URL = NC_API_USERS_URL + "/%s" + NC_API_JSON_SUFFIX;

    public NextcloudApiUserService(NextcloudApiConfig config, @Qualifier("NextcloudWebClient") WebClient webClient) {
        super(config, webClient);
    }

    public List<String> getAllUserIdsFromNextcloud() {

        if(isAPIDisabled()) {
            return Collections.emptyList();
        }

        NextcloudApiResponse<NextcloudUserList> apiResponse = webClient
                .get()
                .uri(NC_API_USERLIST_JSON_URL)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>() {
                })
                .retryWhen(Retry
                        .fixedDelay(config.retryCount(), Duration.ofSeconds(config.retryInterval()))
                        .filter(this::shouldRetry)
                )
                .doOnError(error -> log.error("{}: {}", FAIL_MESSAGE_GET_USERS, error.getMessage()))
                .block();

        if (apiResponse == null) {
            throw new NextcloudException(NextcloudException.IS_NULL);
        }

        NextcloudUserList nextcloudUseridList = apiResponse.getData();
        return nextcloudUseridList.getUsers();
    }

    //TODO need this in p2nc-integrator
    private boolean shouldRetry(Throwable throwable) {
        if (throwable instanceof WebClientResponseException responseException) {
            HttpStatusCode statusCode = responseException.getStatusCode();
            return !statusCode.is5xxServerError() && !statusCode.is4xxClientError();
        }
        return true;
    }

    private NextcloudUser getUser(String username) {

        if(isAPIDisabled()) {
            return null;
        }

        NextcloudApiResponse<NextcloudUser> apiResponse = webClient
                .get()
                .uri(buildUriGetUser(username))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUser>>() {})
                .retryWhen(Retry.fixedDelay(config.retryCount(), Duration.ofSeconds(config.retryInterval())))
                .doOnError(error -> log.error("{}: {}", FAIL_MESSAGE_GET_USERS, error.getMessage()))
                .block();

        if (apiResponse == null) {
            throw new NextcloudException(NextcloudException.IS_NULL);
        }

        return apiResponse.getData();
    }

    public Map<String, String> getAllUsersAsUseridEmailMap() {
        List<String> useridList = getAllUserIdsFromNextcloud();
        return useridList.stream().collect(Collectors.toMap(
                userid -> userid,
                userid -> {
                    NextcloudUser user = getUser(userid);
                    return user == null ? "" : user.email();
                }
        ));
    }

    /**
     * Checks, if email is already in use, generates username from given parameters. tries to create account in Nextcloud
     * @param email to use for account
     * @param firstName to use for login name generation
     * @param lastName to use for login name generation
     * @return Optional<String> that is empty, if email is already in use and userId if no exception is thrown.
     */
    public Optional<String> createUser(String email, String firstName, String lastName) {

        if (isAPIDisabled()) {
            log.info(SUCCESS_API_INACTIVE);
            return Optional.of("<none, API inactive>");
        }

        Map<String, String> allUsersMap = getAllUsersAsUseridEmailMap();
        if(isMailAddressAlreadyInUse(email, allUsersMap)) {
            return Optional.empty();
        }

        // Create user
        String userid = generateUserId(allUsersMap, firstName, lastName);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("userid", userid);
        formData.add("email", email);
        formData.add("displayName", String.format("%s %s", firstName, lastName));
        formData.add("groups[]", config.defaultGroup());

        NextcloudApiResponse<NextcloudResponse> apiResponse = webClient
                .post()
                .uri(NC_API_USERLIST_JSON_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudResponse>>() {
                })
                .retryWhen(Retry.fixedDelay(config.retryCount(), Duration.ofSeconds(config.retryInterval())))
                .doOnError(error -> log.error(FAIL_MESSAGE_CREATE_USER, email, error.getMessage()))
                .block();

        if (apiResponse == null) {
            throw new NextcloudException(NextcloudException.IS_NULL);
        }
        if (apiResponse.getMeta().getStatus().equals("failure")) {
            throw new NextcloudException(String.format("Status is 'failure': %s", apiResponse));
        }
        log.info(SUCCESS_MESSAGE_CREATE_USER, apiResponse.getMeta());

        return Optional.of(userid);
    }

    /*
     * Username generators
     */
    private String generateUserId(Map<String, String> allUsersMap, String firstNameParam, String lastNameParam) {
        String firstName = GermanStringsUtility.normalizeGermanCharacters(firstNameParam);
        String lastName = GermanStringsUtility.normalizeGermanCharacters(lastNameParam);
        return generateUserId(allUsersMap, firstName, lastName, 1);
    }

    private String generateUserId(Map<String, String> allUsersMap, String firstName, String lastName, int charCount) {

        // Assert because <= 0 can only happen for coding errors
        assert charCount > 0;

        if (charCount > firstName.length()) {
            throw new NextcloudException(USERID_NOT_POSSIBLE);
        }

        String userid = String.format(
                "%s%s%s%s",
                config.accountNamePrefix(),
                firstName.substring(0, charCount).toLowerCase(),
                lastName.toLowerCase(),
                config.accountNameSuffix());

        if (allUsersMap.containsKey(userid)) {
            log.info("Minimal userid is already in use: {}", userid);
            return generateUserId(allUsersMap, firstName, lastName, charCount + 1);
        }

        log.info("Created userid is {}", userid);
        return userid;
    }

    private boolean isMailAddressAlreadyInUse(String email, Map<String, String> userMap) {
        return userMap.containsValue(email);
    }

    /*
     * Uri generators
     */
    private String buildUriGetUser(String username) {
        return String.format(NC_API_USER_JSON_URL, username);
    }
}