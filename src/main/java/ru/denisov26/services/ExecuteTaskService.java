package ru.denisov26.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.denisov26.domain.Status;
import ru.denisov26.domain.Task;
import ru.denisov26.repositories.TaskBckRepository;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.*;

@Service
public class ExecuteTaskService {
    private final TaskService repository;

    @Autowired
    public ExecuteTaskService(TaskService repository) {
        this.repository = repository;
    }

    public FutureTask<Task> executeTask(Task source) {

        Callable<Task> task = () -> {
            source.setStatus(Status.RENDERING);
            source.setLastEdit(LocalDateTime.now());
            Task byId = repository.update(source);
            try {
//                оставил, чтоб гонять быстрее
                TimeUnit.SECONDS.sleep(new Random().nextInt(10) + 5);
//                TimeUnit.MINUTES.sleep(new Random().nextInt(5) + 1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            byId.setStatus(Status.COMPLETE);
            byId.setLastEdit(LocalDateTime.now());
            return repository.update(byId);
        };
        FutureTask<Task> future = new FutureTask<>(task);

        new Thread(future).start();
        return future;
    }
}
