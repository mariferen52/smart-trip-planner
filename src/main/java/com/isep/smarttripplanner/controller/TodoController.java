package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.TodoItem;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.repository.TodoRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.List;

public class TodoController {
    @FXML
    private VBox todoListContainer;
    @FXML
    private TextField newTaskField;
    @FXML
    private DatePicker dueDatePicker;

    private Trip currentTrip;
    private final TodoRepository todoRepository = new TodoRepository();

    public void initData(Trip trip) {
        this.currentTrip = trip;
        refreshTodoList();
    }

    @FXML
    private void handleAddTask() {
        String description = newTaskField.getText();
        LocalDate dueDate = dueDatePicker.getValue();

        if (description.isEmpty()) {
            return;
        }

        TodoItem item = new TodoItem(0, currentTrip.getId(), description, false, dueDate);
        todoRepository.insertTodo(item);
        newTaskField.clear();
        dueDatePicker.setValue(null);
        refreshTodoList();
    }

    private void refreshTodoList() {
        todoListContainer.getChildren().clear();
        List<TodoItem> items = todoRepository.findTodosByTripId(currentTrip.getId());
        for (TodoItem item : items) {
            CheckBox checkBox = new CheckBox(
                    item.getDescription() + (item.getDueDate() != null ? " (Due: " + item.getDueDate() + ")" : ""));
            checkBox.setSelected(item.isCompleted());
            checkBox.setOnAction(event -> {
                item.setCompleted(checkBox.isSelected());
                todoRepository.updateTodo(item);
            });
            todoListContainer.getChildren().add(checkBox);
        }
    }
}
