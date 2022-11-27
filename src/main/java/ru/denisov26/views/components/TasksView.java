package ru.denisov26.views.components;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.denisov26.domain.Status;
import ru.denisov26.domain.Task;
import ru.denisov26.repositories.TaskBckRepository;
import ru.denisov26.security.AuthenticatedUser;
import ru.denisov26.services.ExecuteTaskService;
import ru.denisov26.services.TaskService;
import ru.denisov26.views.MainLayout;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

@PageTitle("Task list")
@AnonymousAllowed
@Route(value = "tasks", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class TasksView extends VerticalLayout {
    private final Queue<FutureTask<Task>> futureTaskQueue;
    private Integer gridSize;
    private final TaskService taskService;
    private final TaskBckRepository bckRepo;
    private final ExecuteTaskService service;
    private Grid<Task> grid;

    private static Div hint;

    public TasksView(TaskService taskService, AuthenticatedUser authenticatedUser, TaskBckRepository bckRepo, ExecuteTaskService service) {
        this.taskService = taskService;
        this.futureTaskQueue = new ConcurrentLinkedQueue<>();
        this.bckRepo = bckRepo;
        this.service = service;

        addClassName("task-view");
//        grid на весь экран
//        setSizeFull();
        createGrid(authenticatedUser);
        if(authenticatedUser.get().isPresent()) {
            add(grid, createFieldAndButton(authenticatedUser));
        }
        refreshGrid();
    }

    private void refreshGrid() {
        if (gridSize > 0) {
            grid.setVisible(true);
            hint.setVisible(false);
            grid.getDataProvider().refreshAll();
        } else {
            grid.setVisible(false);
            hint.setVisible(true);
        }
    }

    private void createGrid(AuthenticatedUser user) {

        grid = new Grid<>(Task.class, false);
        grid.addColumn(Task::getId).setHeader("Номер").setAutoWidth(true);
        grid.addColumn(Task::getTaskName).setHeader("Название задачи").setAutoWidth(true);
        grid.addColumn(createStatusComponentRenderer()).setHeader("Статус").setAutoWidth(true);
        grid.addColumn(createStartButton(user)).setHeader("Запуск задачи").setWidth("5em").setVisible(user.get().isPresent());
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addItemDoubleClickListener(event -> new TaskConfirmDialog(event.getItem(), bckRepo));

        hint = new Div();
        hint.setText("Список задач пуст");
        hint.getStyle().set("padding", "var(--lumo-size-l)")
                .set("text-align", "center").set("font-style", "italic")
                .set("color", "var(--lumo-contrast-70pct)");

        gridFill(user);
        add(hint, grid);
    }

    private ComponentRenderer<Button, Task> createStartButton(AuthenticatedUser user) {
        return new ComponentRenderer<>(Button::new, (button, task) -> {
            button.setIcon(new Icon(VaadinIcon.PLAY));
            button.addClickListener(click -> {
                button.setIcon(new Icon(VaadinIcon.SPINNER_THIRD));
                FutureTask<Task> future = service.executeTask(task);
                futureTaskQueue.add(future);
                checkQueue(user);
                refreshGrid();
            });
        });
    }

    private static ComponentRenderer<Span, Task> createStatusComponentRenderer() {
        return new ComponentRenderer<>(Span::new, statusComponentUpdater);
    }

    private static final SerializableBiConsumer<Span, Task> statusComponentUpdater = (
            span, task) -> {
        boolean isComplete = Status.COMPLETE ==(task.getStatus());
        boolean isRendering = Status.RENDERING ==(task.getStatus());
        String theme = String.format("badge %s",
                isComplete ? "success" : isRendering ? "error" : "primary");
        span.getElement().setAttribute("theme", theme);
        span.setText(task.getStatus().name());
    };

    private void gridFill(AuthenticatedUser user) {
        List<Task> tasks;
        if (user.get().isPresent()) {
            tasks = taskService.findTaskByUser(user.get().get());
        } else {
            tasks = taskService.list();
        }

        gridSize = tasks.size();
        grid.setItems(tasks);
    }

    private HorizontalLayout createFieldAndButton(AuthenticatedUser user) {
        HorizontalLayout layout = new HorizontalLayout();
        Button save = new Button("Сохранить");
        TextField taskName = new TextField();
        taskName.setPlaceholder("Название задачи");
        taskName.setWidth("50em");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        save.addClickListener(click -> {
            Optional<Task> taskFromDb = taskService.findTaskByName(taskName.getValue());
            if (taskFromDb.isPresent()) {
                        Notification.show(taskFromDb.get().getTaskName() + " задача с таким назанием уже существует")
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            } else {
                Task newTask = Task.builder()
                        .taskName(taskName.getValue())
                        .user(user.get().orElseThrow(() -> new RuntimeException("Не авторизованный пользователь пытается добавить задачу")))
                        .build();

                taskService.create(newTask);
            }
            taskName.clear();
            gridFill(user);
            refreshGrid();
            grid.scrollToEnd();
        });
        save.addClickShortcut(Key.ENTER);
        layout.add(taskName, save);
        return layout;
    }
    private void checkQueue(AuthenticatedUser user) {
        Runnable runnable = () -> {
            while (futureTaskQueue.size() > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (futureTaskQueue.peek() != null && futureTaskQueue.peek().isDone()) {
                    futureTaskQueue.poll();
                    lockSession(user);
                }
            }
        };
        Thread demon = new Thread(runnable);
        demon.setDaemon(true);
        demon.start();
    }

    private void lockSession(AuthenticatedUser user) {
        if (getUI().isPresent()) {
            UI ui = getUI().get();
            ui.getSession().lock();
            gridFill(user);
            refreshGrid();
            ui.getSession().unlock();
        }
    }

}
