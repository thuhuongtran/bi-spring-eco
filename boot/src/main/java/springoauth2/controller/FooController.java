package springoauth2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springoauth2.model.Foo;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RestController
@RequestMapping(value = "/foos")
public class FooController {

    @GetMapping(value = "/{id}")
    public Foo findOne(@PathVariable Long id) {
        return new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4));
    }

    @GetMapping
    public List findAll() {
        List fooList = new ArrayList();
        fooList.add(new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4)));
        fooList.add(new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4)));
        fooList.add(new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4)));
        return fooList;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public void create(@RequestBody Foo newFoo) {
    }
}
