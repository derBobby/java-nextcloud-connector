package eu.planlos.javanextcloudconnector.service;

import eu.planlos.javanextcloudconnector.config.NextcloudApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public abstract class NextcloudApiService {

    protected final NextcloudApiConfig config;
    protected final WebClient webClient;

    public NextcloudApiService(NextcloudApiConfig config, WebClient webClient) {
        this.config = config;
        this.webClient = webClient;
    }

    public boolean isAPIDisabled() {
        if(config.inactive()) {
            log.info("Nextcloud API is not enabled. Returning empty list or null");
            return true;
        }
        return false;
    }
}