package feign.client;

import feign.model.Post;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient("books-service")
public interface BookClient {
    @RequestMapping(method = RequestMethod.GET, value = "/posts")
    List<Post> getPosts();
}
