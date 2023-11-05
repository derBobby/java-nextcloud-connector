package eu.planlos.javanextcloudconnector;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ConfigurationPropertiesScan
@SpringBootApplication
class ContextTest {

    @Test
    void contextLoads() {
        //Test if Spring context starts
    }
}