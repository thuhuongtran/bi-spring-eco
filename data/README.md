### Spring Data JPA
```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```
Connect with Postgres
```java
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```
Spring Data JPA automatically injects Hibernate dependencies.

To generate metadata type. Add hibernate jpadatamodel dependency. Then run ```mvn install```
It will translate ```User``` bean to ```User_``` class.
```java
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-jpamodelgen</artifactId>
</dependency>
```
##### Config
```java
@Configuration
@EnableJpaRepositories
public class ConfigJpa {
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        // ...
    }
}
```
##### Audit
```java
@CreatedBy
private User user;

@CreatedDate
private Instant createdDate;
```
##### Embedded
```java
@Embedded
private EmailAddress emailAddress;
```
##### CRUD Repository
```java
public interface FooCrudRepository extends Repository<Foo, Long> {

    Foo save(Foo entity);
    Optional<Foo> findById(Long primaryKey);
    Iterable<Foo> findAll();
    long count();
    void delete(Foo entity);
    boolean existsById(Long primaryKey);
```
##### Sort
```java
Iterable<Foo> findAll(Sort sort);
```
```java
Sort sort = Sort.by("firstname").ascending()
  .and(Sort.by("lastname").descending());
```
```java
TypedSort<Person> person = Sort.sort(Person.class);

Sort sort = person.by(Person::getFirstname).ascending()
  .and(person.by(Person::getLastname).descending());
```
##### Page, Slice
A Page knows about the total number of elements and pages available. It does so by the infrastructure triggering a count query to calculate the overall number. As this might be expensive (depending on the store used), you can instead return a Slice. A Slice knows only about whether a next Slice is available, which might be sufficient when walking through a larger result set.
```java
Page<Foo> findAll(Pageable pageable);
Slice<Foo> findByLastname(String lastname, Pageable pageable);
```
```java
Page<Foo> users = fooCrudRepository.findAll(PageRequest.of(page, size));
```
##### Streamable
```java
Streamable<Foo> findByFirstnameContaining(String firstname);
Streamable<Foo> findByLastnameContaining(String lastname);
```
```java
Streamable<Foo> result = fooCrudRepository.findByFirstnameContaining("av")
                .and(fooCrudRepository.findByLastnameContaining("ea"));
```
##### Stream
```java
Stream<Foo> readAllByFirstnameNotNull();
```
```java
try (Stream<Foo> stream = fooCrudRepository.readAllByFirstnameNotNull()) {
            stream.forEach(foo -> {});
}
```
##### Nullable
They provide a tooling-friendly approach and opt-in null checks during runtime, as follows:
- ```@NonNullApi```: Used on the package level to declare that the default behavior for parameters and return values is, respectively, neither to accept nor to produce null values.

- ```@NonNull```: Used on a parameter or return value that must not be null (not needed on a parameter and return value where @NonNullApi applies).

- ```@Nullable```: Used on a parameter or return value that can be null.

```java
@Nullable
User findByEmailAddress(@Nullable EmailAddress emailAdress); 
```
By default, it throws an EmptyResultDataAccessException when the query does not produce a result.
Throws an IllegalArgumentException when the emailAddress handed to the method is null.

Returns ```null``` when the query does not produce a result.
Also accepts null as the value for emailAddress.
##### Async
```java
@Async
Future<Foo> findByFirstname(String firstname);

@Async
CompletableFuture<Foo> findOneByFirstname(String firstname);

@Async
ListenableFuture<Foo> findOneByLastname(String lastname);
```
##### Transactional
```java
@Transactional(timeout = 10)
public Foo save(Foo entity) {
    // ..    
}
```
##### Criteria
```java
public class TransactionRepository extends SimpleJpaRepository<Foo, Long> {
    private final EntityManager entityManager;

    TransactionRepository(JpaEntityInformation entityInformation,
                     EntityManager entityManager) {
        super(entityInformation, entityManager);
        // Keep the EntityManager around to used from the newly introduced methods.
        this.entityManager = entityManager;
    }

    public List<Foo> findByIdPredicate() {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Foo> criteriaQuery = criteriaBuilder.createQuery(Foo.class);
        Root<Foo> root = criteriaQuery.from(Foo.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get(Foo_.id), 2015));
        TypedQuery<Foo> typedQuery = entityManager.createQuery(criteriaQuery);
        List<Foo> results = typedQuery.getResultList();
        return results;
    }
```
##### Query DSL
```java
Optional<Foo> findById(Predicate predicate);
Iterable<Foo> findAll(Predicate predicate);
```
```java
interface UserRepository extends CrudRepository<User, Long>, QuerydslPredicateExecutor<User> {
}
```
```java
Predicate predicate = user.firstname.equalsIgnoreCase("dave")
	.and(user.lastname.startsWithIgnoreCase("mathews"));
userRepository.findAll(predicate);
```
##### Connect with DB
Example connecting with Postgre
```java
server.port = 4567
spring.jpa.database=POSTGRESQL
spring.datasource.platform= postgres
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.show-sql=true
spring.jpa.generate-ddl=true
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation= true
```