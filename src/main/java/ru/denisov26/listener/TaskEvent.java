package ru.denisov26.listener;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import ru.denisov26.domain.Task;


@Getter
public class TaskEvent extends ApplicationEvent {
    private final Task event;
    public TaskEvent(Task event) {
        super(event);
        this.event = event;
    }
}
