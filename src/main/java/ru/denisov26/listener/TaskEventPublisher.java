package ru.denisov26.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.denisov26.domain.Task;

@Component
public class TaskEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public TaskEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishCustomEvent(Task task) {
        TaskEvent taskEvent = new TaskEvent(task);
        applicationEventPublisher.publishEvent(taskEvent);
    }
}
