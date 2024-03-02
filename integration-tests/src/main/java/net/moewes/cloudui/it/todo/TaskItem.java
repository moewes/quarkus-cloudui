package net.moewes.cloudui.it.todo;

import lombok.Data;

import java.util.UUID;

@Data
public class TaskItem {

    public TaskItem() {
        super();
        task = "Task";
    }

    private String task;
    private UUID id;
}
