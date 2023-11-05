package eu.planlos.javanextcloudconnector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.planlos.javanextcloudconnector.model.NextcloudApiResponseDeserializer;
import eu.planlos.javanextcloudconnector.model.NextcloudApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
@Slf4j
public class NextcloudWebClientConfig {

    private final List<ExchangeFilterFunction> customFilters;

    @Autowired
    public NextcloudWebClientConfig(@Qualifier("nextcloudRequestFilter") List<ExchangeFilterFunction> customFilters) {
        this.customFilters = customFilters;
    }

    @Bean
    @Qualifier("NextcloudWebClient")
    public WebClient configureNextcloudWebClient(NextcloudApiConfig apiConfig) {

        String address = apiConfig.address();
        String user = apiConfig.user();
        String defaultGroup = apiConfig.defaultGroup();
        String password = apiConfig.password();

        if (apiConfig.inactive()) {
            address = "mocked-address";
            user = "mocked-user";
            defaultGroup = "mocked-default-group";
            password = "mocked-password";
        }

        log.info("Creating WebClient using:");
        log.info("- Nextcloud address: {}", address);
        log.info("- Nextcloud username: {}", user);
        log.info("- Nextcloud default group: {}", defaultGroup);

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    ObjectMapper objectMapper = new ObjectMapper();
                    SimpleModule module = new SimpleModule();
                    module.addDeserializer(NextcloudApiResponse.class, new NextcloudApiResponseDeserializer<>());
                    module.addDeserializer(NextcloudApiResponse.class, new NextcloudApiResponseDeserializer<>());
                    objectMapper.registerModule(module);
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                })
                .build();

        WebClient.Builder builder = WebClient.builder()
                .baseUrl(address)
                .exchangeStrategies(exchangeStrategies)
                .filter(ExchangeFilterFunctions.basicAuthentication(user, password))
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.set("OCS-APIRequest", "true");
                    httpHeaders.set("Accept", "application/json");
                });

        // Adding injected filters from parent application
        customFilters.forEach(builder::filter);

        return builder.build();
    }
}