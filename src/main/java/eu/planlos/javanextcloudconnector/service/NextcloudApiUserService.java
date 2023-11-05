package eu.planlos.javanextcloudconnector.service;

import com.fasterxml.jackson.databind.JsonNode;
import eu.planlos.javanextcloudconnector.config.NextcloudApiConfig;
import eu.planlos.javanextcloudconnector.model.*;
import eu.planlos.javautilities.GermanStringsUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public NextcloudApiUserService(NextcloudApiConfig nextcloudApiConfig, @Qualifier("NextcloudWebClient") WebClient webClient) {
        super(nextcloudApiConfig, webClient);
    }

    public List<String> getAllUserIdsFromNextcloud() {

        if(isAPIDisabled()) {
            return Collections.emptyList();
        }

        if (log.isDebugEnabled()) {
            JsonNode jsonNode = webClient
                    .get()
                    .uri(NC_API_USERLIST_JSON_URL)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
            logJsonResponse(jsonNode);
        }

        NextcloudApiResponse<NextcloudUserList> apiResponse = webClient
                .get()
                .uri(NC_API_USERLIST_JSON_URL)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>() {})
                .retryWhen(Retry.fixedDelay(0, Duration.ofSeconds(1)))
                .doOnError(error -> log.error("{}: {}", FAIL_MESSAGE_GET_USERS, error.getMessage()))
                .block();

        if (apiResponse == null) {
            throw new NextcloudApiException(NextcloudApiException.IS_NULL);
        }

        NextcloudUserList nextcloudUseridList = apiResponse.getData();
        return nextcloudUseridList.getUsers();
    }

    private NextcloudUser getUser(String username) {

        if (log.isDebugEnabled()) {
            JsonNode jsonNode = webClient
                    .get()
                    .uri(buildUriGetUser(username))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .retryWhen(Retry.fixedDelay(0, Duration.ofSeconds(1)))
                    .doOnError(error -> log.error("{}: {}", FAIL_MESSAGE_GET_USERS, error.getMessage()))
                    .block();
            logJsonResponse(jsonNode);
        }

        NextcloudApiResponse<NextcloudUser> apiResponse = webClient
                .get()
                .uri(buildUriGetUser(username))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUser>>() {})
                .retryWhen(Retry.fixedDelay(0, Duration.ofSeconds(1)))
                .doOnError(error -> log.error("{}: {}", FAIL_MESSAGE_GET_USERS, error.getMessage()))
                .block();

        if (apiResponse == null) {
            throw new NextcloudApiException(NextcloudApiException.IS_NULL);
        }

        return apiResponse.getData();
    }

    private static void logJsonResponse(JsonNode jsonNode) {
        if(jsonNode == null) {
            log.error("JSON is null");
        } else {
            log.debug(jsonNode.toPrettyString());
        }
    }

    public Map<String, String> getAllUsersAsUseridEmailMap() {

        if (nextcloudApiConfig.inactive()) {
            return Collections.emptyMap();
        }

        List<String> useridList = getAllUserIdsFromNextcloud();
        return useridList.stream().collect(Collectors.toMap(
                userid -> userid,
                userid -> getUser(userid).email()
        ));
    }

    public String createUser(String email, String firstName, String lastName) {

        if (nextcloudApiConfig.inactive()) {
            log.info(SUCCESS_API_INACTIVE);
            return "<none, API inactive>";
        }

        Map<String, String> allUsersMap = getAllUsersAsUseridEmailMap();
        failIfMailAddressAlreadyInUse(email, allUsersMap);

        // Create user
        String userid = generateUserId(allUsersMap, firstName, lastName);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("userid", userid);
        formData.add("email", email);
        formData.add("displayName", String.format("%s %s", firstName, lastName));
        formData.add("groups[]", nextcloudApiConfig.defaultGroup());

        NextcloudApiResponse<NextcloudResponse> apiResponse = webClient
                .post()
                .uri(NC_API_USERLIST_JSON_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudResponse>>() {
                })
                .retryWhen(Retry.fixedDelay(0, Duration.ofSeconds(1)))
                .doOnError(error -> log.error(FAIL_MESSAGE_CREATE_USER, email, error.getMessage()))
                .block();

        if (apiResponse == null) {
            throw new NextcloudApiException(NextcloudApiException.IS_NULL);
        }
        if (apiResponse.getMeta().getStatus().equals("failure")) {
            throw new NextcloudApiException(String.format("Status is 'failure': %s", apiResponse));
        }
        log.info(SUCCESS_MESSAGE_CREATE_USER, apiResponse.getMeta());

        return userid;
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
            throw new AccountCreationException("No free userid can be generated");
        }

        String userid = String.format(
                "kv-kraichgau-%s%s",
                firstName.substring(0, charCount).toLowerCase(),
                lastName.toLowerCase());

        if (allUsersMap.containsKey(userid)) {
            log.info("Minimal userid is already in use: {}", userid);
            return generateUserId(allUsersMap, firstName, lastName, charCount + 1);
        }

        log.info("Created userid is {}", userid);
        return userid;
    }

    private void failIfMailAddressAlreadyInUse(String email, Map<String, String> userMap) {
        boolean emailAlreadyInUse = userMap.containsValue(email);
        if (emailAlreadyInUse) {
            throw new AccountCreationException(String.format("Email=%s is already in use", email));
        }
        log.info("Email={} is still free, proceeding", email);
    }

    /*
     * Uri generators
     */
    private String buildUriGetUser(String username) {
        return String.format(NC_API_USER_JSON_URL, username);
    }
}