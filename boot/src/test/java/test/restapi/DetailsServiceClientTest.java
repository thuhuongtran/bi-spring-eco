package test.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Book;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@RestClientTest(DetailsServiceClient.class)
public class DetailsServiceClientTest {

    @Autowired
    private DetailsServiceClient client;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    public DetailsServiceClientTest() {
    }

    @Before
    public void setUp() throws Exception {
        String detailsString =
                objectMapper.writeValueAsString(new Book(1, "John Smith", "john"));

        this.server.expect(requestTo("/john/details"))
                .andRespond(withSuccess(detailsString, MediaType.APPLICATION_JSON));
    }

    @Test
    public void whenCallingGetUserDetails_thenClientMakesCorrectCall() {
        Book details = this.client.getUserDetails("john");
        assertThat(details.getAuthor()).isEqualTo("john");
        assertThat(details.getTitle()).isEqualTo("John Smith");
    }
}
