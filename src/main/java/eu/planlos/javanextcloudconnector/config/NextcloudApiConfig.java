package eu.planlos.javanextcloudconnector.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nextcloud.api")
public record NextcloudApiConfig(boolean active, String address, String user, String password, String defaultGroup, int retryCount, int retryInterval, String accountNamePrefix, String accountNameSuffix) {
    public boolean inactive() {
        return !active;
    }
}