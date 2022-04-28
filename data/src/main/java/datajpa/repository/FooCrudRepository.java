package datajpa.repository;

import datajpa.model.EmailAddress;
import datajpa.model.Foo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;
import org.springframework.data.util.Streamable;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public interface FooCrudRepository extends Repository<Foo, Long> {

    Foo save(Foo entity);
    Optional<Foo> findById(Long primaryKey);
    Iterable<Foo> findAll();
    long count();
    void delete(Foo entity);
    boolean existsById(Long primaryKey);
    Iterable<Foo> findAll(Sort sort);
    Page<Foo> findAll(Pageable pageable);
    long deleteByName(String name);
    List<Foo> removeByName(String name);
    Slice<Foo> findByLastname(String lastname, Pageable pageable);
    Streamable<Foo> findByFirstnameContaining(String firstname);
    Streamable<Foo> findByLastnameContaining(String lastname);
    @Nullable
    Foo findByEmailAddress(@Nullable EmailAddress emailAdress);
    Stream<Foo> readAllByFirstnameNotNull();
    @Async
    Future<Foo> findByFirstname(String firstname);

    @Async
    CompletableFuture<Foo> findOneByFirstname(String firstname);

    @Async
    ListenableFuture<Foo> findOneByLastname(String lastname);
}
