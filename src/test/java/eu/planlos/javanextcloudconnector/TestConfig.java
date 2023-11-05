package eu.planlos.javanextcloudconnector;

import eu.planlos.javanextcloudconnector.config.NextcloudApiConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {
    @Bean
    public NextcloudApiConfig nextcloudApiConfig() {
        return new NextcloudApiConfig(false, "testAddress", "testUser", "testPassword", "testGroup");
    }
}
