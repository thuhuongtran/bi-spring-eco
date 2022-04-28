package datajpa.model;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;

public abstract class AbstractAudit {
    @CreatedBy
    private User user;

    @CreatedDate
    private Instant createdDate;
}
