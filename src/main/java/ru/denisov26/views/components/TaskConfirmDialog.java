package ru.denisov26.views.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import ru.denisov26.domain.Task;
import ru.denisov26.domain.TaskBck;
import ru.denisov26.repositories.TaskBckRepository;

public class TaskConfirmDialog extends Div {

    private final TaskBckRepository bckRepo;
    private final Task task;
    private Span status;

    public TaskConfirmDialog(Task task, TaskBckRepository bckRepo) {
        this.bckRepo = bckRepo;
        this.task = task;

        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        status = new Span();
        status.setVisible(false);

        Dialog dialog = new Dialog();
        Grid<TaskBck> dialogContent = createDialogContent();
        Button cancelButton = new Button("Cancel", (e) -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.add(dialogContent, cancelButton);


        Button button = new Button("Open confirm dialog");
        button.addClickListener(event -> {
            dialog.open();
            status.setVisible(false);
        });

        dialog.open();

        layout.add(button, status);
        add(layout);

        // Center the button within the example
        getStyle().set("position", "fixed").set("top", "0").set("right", "0")
                .set("bottom", "0").set("left", "0").set("display", "flex")
                .set("align-items", "center").set("justify-content", "center");
    }

    private Grid<TaskBck> createDialogContent() {
        Grid<TaskBck> grid = new Grid<>(TaskBck.class, false);
        grid.setItems(bckRepo.findAllByTaskId(task.getId()));
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn(TaskBck::getStatus).setHeader("Статус");
        grid.addColumn(TaskBck::getLastEdit).setHeader("Время изменения");

        grid.getStyle().set("width", "500px").set("max-width", "100%");

        return grid;
    }
}