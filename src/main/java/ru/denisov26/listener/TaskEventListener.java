package ru.denisov26.listener;

import lombok.Getter;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Getter
public class TaskEventListener implements ApplicationListener<TaskEvent> {

    private Queue<TaskEvent> taskEvents = new ConcurrentLinkedQueue<>();
    @Override
    public void onApplicationEvent(TaskEvent event) {
        taskEvents.add(event);
    }
}