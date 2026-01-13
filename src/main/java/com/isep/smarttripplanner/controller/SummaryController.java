package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Expense;
import com.isep.smarttripplanner.model.TodoItem;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.repository.ExpenseRepository;
import com.isep.smarttripplanner.repository.TodoRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SummaryController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label dailySpentLabel;
    @FXML
    private Label totalSpentLabel;
    @FXML
    private Label dailyTasksLabel;
    @FXML
    private Label totalTasksLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label progressText;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void initData(Trip trip, boolean isFinalDay) {
        LocalDate today = LocalDate.now();
        dateLabel.setText(today.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

        if (isFinalDay) {
            titleLabel.setText("Trip Summary 🎉");
        } else {
            titleLabel.setText("Daily Summary 🌙");
        }

        ExpenseRepository expenseRepo = new ExpenseRepository();
        TodoRepository todoRepo = new TodoRepository();

        List<Expense> expenses = expenseRepo.findExpensesByTripId(trip.getId());
        List<TodoItem> todos = todoRepo.findTodosByTripId(trip.getId());

        // 1. Calculate Spent
        double dailySpentVal = expenses.stream()
                .filter(e -> e.getDate().equals(today))
                .mapToDouble(Expense::getAmount).sum();

        double totalSpentVal = expenses.stream()
                .mapToDouble(Expense::getAmount).sum();

        // 2. Calculate Tasks
        long dueToday = todos.stream().filter(t -> t.getDueDate() != null && t.getDueDate().equals(today)).count();
        long completedToday = todos.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().equals(today) && t.isCompleted()).count();

        long totalTodos = todos.size();
        long totalCompleted = todos.stream().filter(TodoItem::isCompleted).count();

        // 3. Format Currency
        String currency = trip.getCurrency() != null ? trip.getCurrency() : "USD";
        String symbol = currency;
        try {
            symbol = java.util.Currency.getInstance(currency).getSymbol();
        } catch (Exception e) {
        }

        // 4. Set Text
        dailySpentLabel.setText(String.format("%s%.2f", symbol, dailySpentVal));
        totalSpentLabel.setText(String.format("%s%.2f", symbol, totalSpentVal));

        if (dueToday > 0) {
            dailyTasksLabel.setText(completedToday + " / " + dueToday + " Done");
        } else {
            dailyTasksLabel.setText("No tasks today");
        }

        totalTasksLabel.setText(totalCompleted + " / " + totalTodos + " Done");

        long totalDays = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getTripEndDate()) + 1;
        long daysPassed = ChronoUnit.DAYS.between(trip.getStartDate(), today) + 1;

        if (daysPassed < 0)
            daysPassed = 0;
        if (daysPassed > totalDays)
            daysPassed = totalDays;

        double progress = (double) daysPassed / totalDays;
        progressBar.setProgress(progress);
        progressText.setText(String.format("%.0f%% Complete", progress * 100));
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }
}
