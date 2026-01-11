package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Budget;
import com.isep.smarttripplanner.model.Expense;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.repository.ExpenseRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.util.List;

public class BudgetController {
    @FXML
    private ProgressBar budgetProgressBar;
    @FXML
    private Label spentLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private TextField limitField;
    @FXML
    private TextField expenseDescField;
    @FXML
    private TextField expenseAmountField;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private ComboBox<String> budgetCurrencyCombo;
    @FXML
    private ListView<Expense> expenseListView;

    private double cachedRate = 1.0;

    private Budget currentBudget;
    private Trip currentTrip;
    private final ExpenseRepository expenseRepository = new ExpenseRepository();

    private final com.isep.smarttripplanner.repository.AppConfigRepository configRepo = new com.isep.smarttripplanner.repository.AppConfigRepository();
    private final com.isep.smarttripplanner.service.ExchangeRateService exchangeService = new com.isep.smarttripplanner.service.ExchangeRateService();

    public void initData(Trip trip) {
        this.currentTrip = trip;

        String homeCurrency = "USD";
        String targetCurrency = "USD";

        try {
            com.isep.smarttripplanner.model.AppConfig config = configRepo.getConfig();

            if (trip.getCurrency() != null) {
                homeCurrency = config.getDefaultCurrency();
            }

            targetCurrency = config.getTargetCurrency();
        } catch (Exception e) {
        }

        final String fHome = homeCurrency;
        final String fTarget = targetCurrency;

        spentLabel.setText("Loading...");
        balanceLabel.setText("converting...");

        exchangeService.getExchangeRate(fHome, fTarget).thenAccept(r -> {
            javafx.application.Platform.runLater(() -> {
                double conversionRate = r;
                updateMultiCurrencyUI(trip, conversionRate, fHome, fTarget);
            });
        }).exceptionally(ex -> {
            ex.printStackTrace();
            javafx.application.Platform.runLater(() -> updateMultiCurrencyUI(trip, 1.0, fHome, fHome));
            return null;
        });
    }

    private void updateMultiCurrencyUI(Trip trip, double rate, String homeCode, String targetCode) {
        this.cachedRate = rate;

        if (budgetCurrencyCombo.getItems().isEmpty()) {
            budgetCurrencyCombo.getItems().clear();
            budgetCurrencyCombo.getItems().addAll("USD", "EUR", "TRY", "GEL", "INR");

            if (!budgetCurrencyCombo.getItems().contains(homeCode))
                budgetCurrencyCombo.getItems().add(homeCode);
            if (!budgetCurrencyCombo.getItems().contains(targetCode))
                budgetCurrencyCombo.getItems().add(targetCode);

            budgetCurrencyCombo.setValue(homeCode);
        }

        this.currentBudget = new Budget(trip.getBudget(), homeCode);

        String symbolTarget = getCurrencySymbol(targetCode);
        String symbolHome = getCurrencySymbol(homeCode);

        List<Expense> expenses = expenseRepository.findExpensesByTripId(trip.getId());

        for (Expense e : expenses) {
            currentBudget.addExpense(e);
        }

        if (expenseListView != null) {
            expenseListView.getItems().clear();
            expenseListView.getItems().addAll(expenses);

            expenseListView.setCellFactory(param -> new ListCell<Expense>() {
                @Override
                protected void updateItem(Expense item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(10);
                        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                        double convertedAmount = item.getAmount() * rate;
                        String text = String.format("%s: %s%.2f (%s)", item.getDescription(), symbolTarget,
                                convertedAmount, item.getCategory());
                        Label label = new Label(text);

                        Button deleteBtn = new Button("X");
                        deleteBtn.setStyle(
                                "-fx-text-fill: red; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
                        deleteBtn.setOnAction(event -> handleDeleteExpense(item));

                        hbox.getChildren().addAll(label, spacer, deleteBtn);
                        setGraphic(hbox);
                    }
                }
            });
        }

        double spentHome = currentBudget.getCurrentSpent();
        double limitHome = currentBudget.getTotalLimit();
        double balanceHome = currentBudget.getRemainingBalance();

        double spentTarget = spentHome * rate;

        double balanceTarget = balanceHome * rate;

        spentLabel.setText(String.format("Spent: %s%.2f (%s%.2f)", symbolTarget, spentTarget, symbolHome, spentHome));
        balanceLabel.setText(String.format("Balance: %s%.2f", symbolTarget, balanceTarget));

        double progress = limitHome > 0 ? spentHome / limitHome : 0;
        budgetProgressBar.setProgress(Math.min(progress, 1.0));

        if (progress > 0.9) {
            budgetProgressBar.setStyle("-fx-accent: red;");
        } else {
            budgetProgressBar.setStyle("-fx-accent: #4caf50;");
        }
    }

    private void handleDeleteExpense(Expense expense) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Expense");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete: " + expense.getDescription() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    expenseRepository.deleteExpense(expense.getId());
                    initData(currentTrip);
                } catch (Exception e) {
                    showAlert("Error", "Could not delete expense.");
                }
            }
        });
    }

    private String getCurrencySymbol(String code) {
        try {
            return java.util.Currency.getInstance(code).getSymbol();
        } catch (Exception e) {
            return code;
        }
    }

    @FXML
    private void handleUpdateLimit() {
        try {
            double limit = Double.parseDouble(limitField.getText());
            String selected = budgetCurrencyCombo.getValue();

            if (selected != null && !selected.startsWith(currentTrip.getCurrency())) {
                if (cachedRate > 0) {
                    limit = limit / cachedRate;
                }
            }

            currentTrip.setBudget(limit);

            com.isep.smarttripplanner.repository.TripRepository tripRepo = new com.isep.smarttripplanner.repository.TripRepository();
            tripRepo.updateTrip(currentTrip);

            initData(currentTrip);

            showAlert("Success", "Budget updated to " + String.format("%.2f %s", limit, currentTrip.getCurrency()));

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number.");
        } catch (Exception e) {
            showAlert("Error", "Could not save budget: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddExpense() {
        try {
            String desc = expenseDescField.getText();
            double amountTarget = Double.parseDouble(expenseAmountField.getText());
            String category = categoryComboBox.getValue();

            if (desc == null || desc.isEmpty() || category == null) {
                showAlert("Missing Info", "Please fill in all expense details.");
                return;
            }

            double amountHome = amountTarget;
            if (cachedRate > 0) {
                amountHome = amountTarget / cachedRate;
            }

            Expense expense = new Expense(0, currentTrip.getId(), desc, amountHome, category, LocalDate.now());
            expenseRepository.insertExpense(expense);

            expenseDescField.clear();
            expenseAmountField.clear();

            initData(currentTrip);

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the expense amount.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
