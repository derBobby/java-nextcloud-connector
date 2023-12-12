package eu.planlos.javanextcloudconnector;

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ConfigurationPropertiesScan
@SpringBootApplication
class ContextTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    void contextLoads() {
        ServletContext servletContext = webApplicationContext.getServletContext();
        assertNotNull(servletContext);
    }
}