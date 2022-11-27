package ru.denisov26.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TaskBck extends AbstractEntity implements Serializable {
    private UUID taskId;
    private String taskName;
    private Status status;
    private LocalDateTime lastEdit;
    private UUID userId;
    private Long version;

    public TaskBck(Task toSave) {
        this.taskId = toSave.getId();
        this.taskName = toSave.getTaskName();
        this.status = toSave.getStatus();
        this.lastEdit = toSave.getLastEdit();
        this.userId = toSave.getUser().getId();
        this.version = toSave.getVersion();
    }
}
