package feign.basic.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import feign.basic.config.BookMocks;
import feign.basic.config.WireMockConfig;
import feign.client.JSONPlaceHolderClient;
import feign.config.MyClientConfiguration;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { WireMockConfig.class })
public class BooksClientIntegrationTest {
    @Autowired
    private WireMockServer mockBooksService;

    @Autowired
    private JSONPlaceHolderClient client;

    @BeforeEach
    void setUp() throws IOException {
        BookMocks.setupMockBooksResponse(mockBooksService);
    }

    @Test
    public void whenGetBooks_thenBooksShouldBeReturned() {
        assertFalse(client.getPosts().isEmpty());
    }
}
