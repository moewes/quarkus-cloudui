package net.moewes.cloudui.it.todo;

import jakarta.inject.Inject;
import net.moewes.cloudui.UiComponent;
import net.moewes.cloudui.annotations.CloudUiView;
import net.moewes.cloudui.html.Div;
import net.moewes.cloudui.html.H1;
import net.moewes.cloudui.quarkus.runtime.CloudUi;

import java.util.logging.Logger;

@CloudUiView("/todos")
public class TaskListView extends Div {

    @Inject
    public TaskListView(TaskRepository repository, CloudUi ui) {

        getElement().setAttribute("style", "padding: 1em");

        add(new H1("Todos"));

        repository.getTasks().forEach(item -> {
            Div taskItem = new Div();
            taskItem.setId(item.getId().toString());
            taskItem.setInnerHtml(item.getTask());

            UiComponent delbutton = new UiComponent("button");
            delbutton.setInnerHtml("Delete Task");
            taskItem.add(delbutton);

            delbutton.addEventListener("click", event -> {
                Logger.getLogger("Delete").info("Task: " + item.getTask());
                repository.deleteTask(item);
                this.remove(taskItem);
            });

            add(taskItem);
        });

        UiComponent button = new UiComponent("button");
        button.setInnerHtml("Add Task");
        add(button);

        button.addEventListener("click", event -> {
            repository.setCurrentItem(new TaskItem());
            ui.navigate(AddTaskView.class);
        });

    }
}
