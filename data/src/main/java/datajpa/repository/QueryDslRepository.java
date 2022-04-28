package datajpa.repository;

import datajpa.model.Foo;
import org.springframework.cglib.core.Predicate;

import java.util.Optional;

public interface QueryDslRepository {
    Optional<Foo> findById(Predicate predicate);

    Iterable<Foo> findAll(Predicate predicate);

    long count(Predicate predicate);

    boolean exists(Predicate predicate);
}
