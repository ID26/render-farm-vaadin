package ru.denisov26.domain;

import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EnableJpaAuditing
@EntityListeners(AuditingEntityListener.class)
public class Task extends AbstractEntity implements Serializable {

    private String taskName;

    private Status status;

    @LastModifiedDate
    private LocalDateTime lastEdit;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Long version;

    @PrePersist
    public void prePersistSetData() {
        this.setLastEdit(LocalDateTime.now());
        this.setStatus(Status.CREATED);
        this.setVersion(1L);
    }

    @PostPersist
    public void taskExecute() {
    }

    @PreUpdate
    public void taskComplete() {
        this.setVersion(getVersion() + 1);
    }
}
