package ru.denisov26.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.denisov26.domain.Task;
import ru.denisov26.domain.TaskBck;
import ru.denisov26.domain.User;
import ru.denisov26.exceptions.TaskNotFoundException;
import ru.denisov26.listener.TaskEventPublisher;
import ru.denisov26.repositories.TaskBckRepository;
import ru.denisov26.repositories.TaskRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository repository;
    private final TaskBckRepository bckRepository;

    private final TaskEventPublisher taskEventPublisher;

    @Autowired
    public TaskService(TaskRepository repository, TaskBckRepository bckRepository, TaskEventPublisher taskEventPublisher) {
        this.repository = repository;
        this.bckRepository = bckRepository;
        this.taskEventPublisher = taskEventPublisher;
    }

    public List<Task> list() {
        return repository.findAll();
    }

    public Task findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new TaskNotFoundException("Зачача не найдена"));
    }

    public Task create(Task task) {
        Task res = repository.saveAndFlush(task);
        bckRepository.save(new TaskBck(res));
        return res;
    }

    @TransactionalEventListener
    public Task update(Task task) {
        if (task == null) {
            throw new RuntimeException("Объект пустой");
        }
        Task save = repository.save(task);
        bckRepository.save(new TaskBck(save));
        taskEventPublisher.publishCustomEvent(task);
        return save;
    }

    public void deleteById(UUID id) {
        bckRepository.deleteAllByTaskId(id);
        repository.deleteById(id);
    }

    public void delete(Task task) {
        bckRepository.deleteAllByTaskId(task.getId());
        repository.delete(task);
    }

    public List<Task> findTaskByUser(User user) {
        return repository.findAllByUserid(user.getId());
    }

    public Optional<Task> findTaskByName(String value) {
        return repository.findByTaskName(value);
    }
}
