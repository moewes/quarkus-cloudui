package net.moewes.cloudui.it.todo;

import jakarta.inject.Inject;
import net.moewes.cloudui.UiBinder;
import net.moewes.cloudui.UiComponent;
import net.moewes.cloudui.annotations.CloudUiView;
import net.moewes.cloudui.html.Div;
import net.moewes.cloudui.html.H1;
import net.moewes.cloudui.quarkus.runtime.CloudUi;

import java.util.UUID;

@CloudUiView("/todos/add")
public class AddTaskView extends Div {

    @Inject
    public AddTaskView(TaskRepository repository, CloudUi ui) {

        getElement().setAttribute("style", "padding: 1em");

        add(new H1("Add Todo"));

        UiComponent taskField = new UiComponent("input");
        taskField.getElement().setHasInput(true);
        add(taskField);
        UiBinder binder = new UiBinder();
        repository.getCurrentItem().ifPresent(taskItem -> {
            binder.bind(taskField, taskItem::getTask, taskItem::setTask);
        });

        UiComponent saveButton = new UiComponent("button");
        saveButton.setInnerHtml("Save");
        saveButton.addEventListener("click", event -> {
            repository.getCurrentItem().ifPresent(taskItem -> {
                taskItem.setId(UUID.randomUUID());
                repository.addTask(taskItem);
            });
            ui.navigate(TaskListView.class);
        });

        add(saveButton);
    }
}
