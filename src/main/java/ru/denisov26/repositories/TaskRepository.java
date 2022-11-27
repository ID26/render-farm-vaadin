package ru.denisov26.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.denisov26.domain.Task;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    @Query(value = "select * from task t where t.user_id = ?", nativeQuery = true)
    List<Task> findAllByUserid(UUID id);

    Optional<Task> findByTaskName(String value);
}
