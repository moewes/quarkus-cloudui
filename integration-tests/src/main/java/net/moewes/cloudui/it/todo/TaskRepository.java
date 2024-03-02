package net.moewes.cloudui.it.todo;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@ApplicationScoped
public class TaskRepository {

    private List<TaskItem> taskItems = new ArrayList<>();
    private TaskItem currentItem;

    public void addTask(TaskItem task) {
        taskItems.add(task);
    }

    public void deleteTask(TaskItem task) {
        taskItems.remove(task);
    }

    public List<TaskItem> getTasks() {
        return taskItems;
    }

    public Optional<TaskItem> getCurrentItem() {
        return Optional.ofNullable(currentItem);
    }

    public void setCurrentItem(TaskItem item) {
        currentItem = item;
    }
}
