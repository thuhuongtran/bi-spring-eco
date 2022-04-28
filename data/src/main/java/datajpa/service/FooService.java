package datajpa.service;

import datajpa.model.Foo;
import datajpa.repository.FooCrudRepository;
import datajpa.repository.QueryDslRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@Service
public class FooService {
    @Autowired
    private FooCrudRepository fooCrudRepository;
    @Autowired
    private QueryDslRepository queryDslRepository;

    public Page<Foo> findAllFoo(int page, int size) {
        Page<Foo> users = fooCrudRepository.findAll(PageRequest.of(page, size));
        return users;
    }

    public void findAndTypeSortAllFoo(int page, int size) {
        Sort.TypedSort<Foo> person = Sort.sort(Foo.class);
        Sort sort = person.by(Foo::getId).ascending()
                .and(person.by(Foo::getName).descending());
    }

    public void findByFirstNameAndLastName() {
        Streamable<Foo> result = fooCrudRepository.findByFirstnameContaining("av")
                .and(fooCrudRepository.findByLastnameContaining("ea"));
    }

    public void readByFirstName() {
        try (Stream<Foo> stream = fooCrudRepository.readAllByFirstnameNotNull()) {
            stream.forEach(foo -> {});
        }
    }
}
