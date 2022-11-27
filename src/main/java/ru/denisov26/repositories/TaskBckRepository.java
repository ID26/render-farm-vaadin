package ru.denisov26.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.denisov26.domain.TaskBck;

import java.util.List;
import java.util.UUID;

public interface TaskBckRepository extends JpaRepository<TaskBck, UUID> {
    void deleteAllByTaskId(UUID id);
    List<TaskBck> findAllByTaskId(UUID id);
}
