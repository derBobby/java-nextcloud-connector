package eu.planlos.javanextcloudconnector.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NextcloudApiConfig.class)
public class NextcloudApiConfigBeanConfiguration {
}