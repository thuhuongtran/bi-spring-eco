package datajpa.repository;

import datajpa.model.Foo;
import datajpa.model.Foo_;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class TransactionRepository extends SimpleJpaRepository<Foo, Long> {
    private final EntityManager entityManager;

    TransactionRepository(JpaEntityInformation entityInformation,
                     EntityManager entityManager) {
        super(entityInformation, entityManager);
        // Keep the EntityManager around to used from the newly introduced methods.
        this.entityManager = entityManager;
    }

    @Transactional(timeout = 10)
    public Foo save(Foo entity) {
        // implementation goes here
        return null;
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
}
