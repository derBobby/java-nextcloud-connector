package eu.planlos.javanextcloudconnector.service;

import eu.planlos.javanextcloudconnector.config.NextcloudApiConfig;
import eu.planlos.javanextcloudconnector.model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;

import static eu.planlos.javanextcloudconnector.NextcloudTestDataUtility.okMeta;
import static eu.planlos.javanextcloudconnector.NextcloudTestDataUtility.takenUser;
import static eu.planlos.javanextcloudconnector.service.NextcloudApiUserService.NC_API_USERLIST_JSON_URL;
import static eu.planlos.javanextcloudconnector.service.NextcloudApiUserService.NC_API_USER_JSON_URL;
import static org.mockito.Mockito.mock;
import static org.springframework.web.reactive.function.client.WebClient.*;

@ExtendWith(MockitoExtension.class)
public class NextcloudApiUserServiceTest {

    private static final NextcloudApiConfig apiConfig = new NextcloudApiConfig(true, "https://localhost/{userId}", "username", "password", "group", 0, 0, "prefix-", "-suffix");

    private static RequestBodyUriSpec requestBodyUriMock;
    @SuppressWarnings("rawtypes")
    private static RequestHeadersSpec requestHeadersMock;
    @SuppressWarnings("rawtypes")
    private static RequestHeadersUriSpec requestHeadersUriSpec;
    private static RequestBodySpec requestBodyMock;
    private static ResponseSpec responseMock;
    private static WebClient webClientMock;

    @BeforeAll
    static void mockWebClient() {
        requestBodyUriMock = mock(RequestBodyUriSpec.class);
        requestHeadersMock = mock(RequestHeadersSpec.class);
        requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        requestBodyMock = mock(RequestBodySpec.class);
        responseMock = mock(ResponseSpec.class);
        webClientMock = mock(WebClient.class);
        Mockito.when(webClientMock.get()).thenReturn(requestHeadersUriSpec);
        Mockito.when(webClientMock.post()).thenReturn(requestBodyUriMock);
    }

    @Test
    public void mailAndUserIdAvailable_accountCreationSuccessful() {
        // Prepare
        //      objects
        NextcloudApiUserService nextcloudApiUserService = new NextcloudApiUserService(apiConfig, webClientMock);
        NextcloudUserList takenUserList = new NextcloudUserList();
        //      webclient
        NextcloudApiResponse<NextcloudUserList> apiResponseUserList = new NextcloudApiResponse<>(new NextcloudMeta(), takenUserList);
        NextcloudResponse response = new NextcloudResponse();
        NextcloudApiResponse<NextcloudResponse> apiResponse = new NextcloudApiResponse<>(okMeta(), response);
        //      methods
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudResponse>>() {})).thenReturn(Mono.just(apiResponse));
        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestHeadersMock);

        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>() {})).thenReturn(Mono.just(apiResponseUserList));
        Mockito.when(requestBodyUriMock.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.bodyValue(ArgumentMatchers.anyMap())).thenReturn(requestHeadersMock);
        Mockito.when(requestHeadersMock.retrieve()).thenReturn(responseMock);

        // Act
        String userId = nextcloudApiUserService.createUser("newuser@example.com", "New", "User");

        // Check
        Assertions.assertEquals(userId, String.format("%snuser%s", apiConfig.accountNamePrefix(), apiConfig.accountNameSuffix()));
    }

    @Test
    public void mailAlreadyInUse_accountCreationFails() {
        // Prepare
        //      objects
        NextcloudApiUserService nextcloudApiUserService = new NextcloudApiUserService(apiConfig, webClientMock);
        NextcloudUser takenUser = takenUser(apiConfig.accountNamePrefix(), apiConfig.accountNameSuffix());
        NextcloudUserList takenUserList = new NextcloudUserList(List.of(takenUser.id()));
        //      webclient
        NextcloudApiResponse<NextcloudUserList> apiResponseUserList = new NextcloudApiResponse<>(new NextcloudMeta(), takenUserList);
        NextcloudApiResponse<NextcloudUser> apiResponseUser = new NextcloudApiResponse<>(new NextcloudMeta(), takenUser);
        String uriUser = String.format(NC_API_USER_JSON_URL, takenUser.id());
        //      methods
        Mockito.when(requestBodyMock.retrieve()).thenReturn(responseMock);

        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestBodyMock);
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>(){})).thenReturn(Mono.just(apiResponseUserList));

        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(uriUser))).thenReturn(requestBodyMock);
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUser>>(){})).thenReturn(Mono.just(apiResponseUser));

        // Act
        // Check
        Assertions.assertThrows(NextcloudException.class, () -> nextcloudApiUserService.createUser(takenUser.email(), "New", "User"));
    }

    //TODO is there a test library that starts a webserver ans answers with what I want?
    // Integration test instead of this crap code...

    @Test
    public void nextcloudReturns4xx_noRetry() {
        // Prepare
        //      objects
        NextcloudApiUserService nextcloudApiUserService = new NextcloudApiUserService(apiConfig, webClientMock);
        NextcloudUser takenUser = takenUser(apiConfig.accountNamePrefix(), apiConfig.accountNameSuffix());
        NextcloudUserList takenUserList = new NextcloudUserList(List.of(takenUser.id()));
        //      webclient
        NextcloudApiResponse<NextcloudUserList> apiResponseUserList = new NextcloudApiResponse<>(new NextcloudMeta(), takenUserList);
        NextcloudApiResponse<NextcloudUser> apiResponseUser = new NextcloudApiResponse<>(new NextcloudMeta(), takenUser);
        String uriUser = String.format(NC_API_USER_JSON_URL, takenUser.id());
        //      methods
        Mockito.when(requestBodyMock.retrieve()).thenReturn(responseMock);

        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestBodyMock);
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>(){}))
                .thenReturn(Mono.just(apiResponseUserList));

        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(uriUser))).thenReturn(requestBodyMock);
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUser>>(){}))
                .thenReturn(Mono.just(apiResponseUser));

        // Handle 4xx status code (e.g., 400 Bad Request)
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>(){}))
                .thenReturn(Mono.error(
                        WebClientResponseException.create(400, "Bad Request",
                                null, null, null)
                ));

        // Act
        // Check
        //TODO Wrap WebClientResponseException.class in NextcloudApiException.class
        Assertions.assertThrows(WebClientResponseException.class, () -> nextcloudApiUserService.createUser(takenUser.email(), "New", "User"));
    }

    @Test
    public void nextcloudReturns5xx_noRetry() {
        // Prepare
        //      objects
        NextcloudApiUserService nextcloudApiUserService = new NextcloudApiUserService(apiConfig, webClientMock);
        NextcloudUser takenUser = takenUser(apiConfig.accountNamePrefix(), apiConfig.accountNameSuffix());
        NextcloudUserList takenUserList = new NextcloudUserList(List.of(takenUser.id()));
        //      webclient
        NextcloudApiResponse<NextcloudUserList> apiResponseUserList = new NextcloudApiResponse<>(new NextcloudMeta(), takenUserList);
        NextcloudApiResponse<NextcloudUser> apiResponseUser = new NextcloudApiResponse<>(new NextcloudMeta(), takenUser);
        String uriUser = String.format(NC_API_USER_JSON_URL, takenUser.id());
        //      methods
        Mockito.when(requestBodyMock.retrieve()).thenReturn(responseMock);

        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestBodyMock);
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>(){}))
                .thenReturn(Mono.just(apiResponseUserList));

        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(uriUser))).thenReturn(requestBodyMock);
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUser>>(){}))
                .thenReturn(Mono.just(apiResponseUser));

        // Handle 5xx status code (e.g., 500 Internal Server Error)
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>(){}))
                .thenReturn(Mono.error(
                        WebClientResponseException.create(500, "Internal Server Error",
                                null, null, null)
                ));

        // Act
        // Check
        //TODO Wrap WebClientResponseException.class in NextcloudApiException.class
        Assertions.assertThrows(WebClientResponseException.class, () -> nextcloudApiUserService.createUser(takenUser.email(), "New", "User"));
    }

    @Test
    public void mailAvailableUserIdTaken_createsAlternateId() {
        // Prepare
        //      objects
        NextcloudApiUserService nextcloudApiUserService = new NextcloudApiUserService(apiConfig, webClientMock);
        NextcloudUser takenUser = takenUser(apiConfig.accountNamePrefix(), apiConfig.accountNameSuffix());
        NextcloudUserList takenUserList = new NextcloudUserList(List.of(takenUser.id()));
        //      webclient
        NextcloudApiResponse<NextcloudUserList> apiResponseUserList = new NextcloudApiResponse<>(new NextcloudMeta(), takenUserList);
        NextcloudApiResponse<NextcloudUser> apiResponseUser = new NextcloudApiResponse<>(new NextcloudMeta(), takenUser);

        NextcloudResponse response = new NextcloudResponse();
        NextcloudApiResponse<NextcloudResponse> apiResponse = new NextcloudApiResponse<>(okMeta(), response);

        String uriUser = String.format(NC_API_USER_JSON_URL, takenUser.id());
        //      methods
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudResponse>>() {})).thenReturn(Mono.just(apiResponse));
        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestHeadersMock);

        Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(uriUser))).thenReturn(requestBodyMock);
        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUser>>(){})).thenReturn(Mono.just(apiResponseUser));
        Mockito.when(requestBodyMock.retrieve()).thenReturn(responseMock);

        Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>() {})).thenReturn(Mono.just(apiResponseUserList));
        Mockito.when(requestBodyUriMock.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodyMock);
        Mockito.when(requestBodyMock.bodyValue(ArgumentMatchers.anyMap())).thenReturn(requestHeadersMock);
        Mockito.when(requestHeadersMock.retrieve()).thenReturn(responseMock);

        // Act
        String userId = nextcloudApiUserService.createUser("newuser@example.com", "Display", "Name");

        // Check
        Assertions.assertEquals(userId, String.format("%sdiname%s", apiConfig.accountNamePrefix(), apiConfig.accountNameSuffix()));
    }

    @Test
    public void firstnameNotLongEnoughToAvoidExisting_accountCreationFails() {
            // Prepare
            //      objects
            NextcloudApiUserService nextcloudApiUserService = new NextcloudApiUserService(apiConfig, webClientMock);
            NextcloudUser takenUser = takenUser(apiConfig.accountNamePrefix(), apiConfig.accountNameSuffix());
            NextcloudUserList takenUserList = new NextcloudUserList(List.of(takenUser.id()));
            //      webclient
            NextcloudApiResponse<NextcloudUserList> apiResponseUserList = new NextcloudApiResponse<>(new NextcloudMeta(), takenUserList);
            NextcloudApiResponse<NextcloudUser> apiResponseUser = new NextcloudApiResponse<>(new NextcloudMeta(), takenUser);

            NextcloudResponse response = new NextcloudResponse();
            NextcloudApiResponse<NextcloudResponse> apiResponse = new NextcloudApiResponse<>(okMeta(), response);

            String uriUser = String.format(NC_API_USER_JSON_URL, takenUser.id());
            //      methods
            Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudResponse>>() {
            })).thenReturn(Mono.just(apiResponse));
            Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestHeadersMock);

            Mockito.when(requestHeadersUriSpec.uri(ArgumentMatchers.eq(uriUser))).thenReturn(requestBodyMock);
            Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUser>>() {
            })).thenReturn(Mono.just(apiResponseUser));
            Mockito.when(requestBodyMock.retrieve()).thenReturn(responseMock);

            Mockito.when(responseMock.bodyToMono(new ParameterizedTypeReference<NextcloudApiResponse<NextcloudUserList>>() {
            })).thenReturn(Mono.just(apiResponseUserList));
            Mockito.when(requestBodyUriMock.uri(ArgumentMatchers.eq(NC_API_USERLIST_JSON_URL))).thenReturn(requestBodyMock);
            Mockito.when(requestBodyMock.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodyMock);
            Mockito.when(requestBodyMock.bodyValue(ArgumentMatchers.anyMap())).thenReturn(requestHeadersMock);
            Mockito.when(requestHeadersMock.retrieve()).thenReturn(responseMock);

            // Act
            // Check
            Assertions.assertThrows(NextcloudException.class, () -> nextcloudApiUserService.createUser("newuser@example.com", "D", "Name"));
        }
}