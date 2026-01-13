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

    private String homeCurrency = "USD";
    private String targetCurrency = "USD";

    public void initData(Trip trip) {
        this.currentTrip = trip;

        try {
            com.isep.smarttripplanner.model.AppConfig config = configRepo.getConfig();
            homeCurrency = config.getDefaultCurrency();
            targetCurrency = config.getTargetCurrency();
        } catch (Exception e) {
        }

        final String fHome = homeCurrency;
        final String fTarget = targetCurrency;
        final String fTripCurrency = (trip.getCurrency() != null) ? trip.getCurrency() : "USD";

        spentLabel.setText("Loading...");
        balanceLabel.setText("converting...");
        if (fTripCurrency.equals(fHome)) {
            exchangeService.getExchangeRate(fHome, fTarget).thenAccept(r -> {
                javafx.application.Platform.runLater(() -> {
                    updateMultiCurrencyUI(trip, r, fHome, fTarget, 1.0);
                });
            }).exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> updateMultiCurrencyUI(trip, 1.0, fHome, fTarget, 1.0));
                return null;
            });
        } else {
            exchangeService.getExchangeRate(fHome, fTripCurrency).thenAccept(homeToTripRate -> {
                double tripToHomeRate = 1.0 / homeToTripRate;

                if (fHome.equals(fTarget)) {
                    javafx.application.Platform.runLater(() -> {
                        updateMultiCurrencyUI(trip, 1.0, fHome, fTarget, tripToHomeRate);
                    });
                } else {
                    exchangeService.getExchangeRate(fHome, fTarget).thenAccept(homeToTargetRate -> {
                        javafx.application.Platform.runLater(() -> {
                            updateMultiCurrencyUI(trip, homeToTargetRate, fHome, fTarget, tripToHomeRate);
                        });
                    }).exceptionally(ex -> {
                        javafx.application.Platform
                                .runLater(() -> updateMultiCurrencyUI(trip, 1.0, fHome, fTarget, tripToHomeRate));
                        return null;
                    });
                }
            }).exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> updateMultiCurrencyUI(trip, 1.0, fHome, fTarget, 1.0));
                return null;
            });
        }
    }

    private void updateMultiCurrencyUI(Trip trip, double homeToTargetRate, String homeCode, String targetCode,
            double tripToHomeRate) {
        this.cachedRate = homeToTargetRate;

        if (budgetCurrencyCombo.getItems().isEmpty()) {
            budgetCurrencyCombo.getItems().clear();
            budgetCurrencyCombo.getItems().addAll("USD", "EUR", "TRY", "GEL", "INR");

            if (!budgetCurrencyCombo.getItems().contains(homeCode))
                budgetCurrencyCombo.getItems().add(homeCode);
            if (!budgetCurrencyCombo.getItems().contains(targetCode))
                budgetCurrencyCombo.getItems().add(targetCode);

            budgetCurrencyCombo.setValue(homeCode);
        }

        double convertedBudgetLimit = trip.getBudget() * tripToHomeRate;
        this.currentBudget = new Budget(convertedBudgetLimit, homeCode);

        String symbolTarget = getCurrencySymbol(targetCode);
        String symbolHome = getCurrencySymbol(homeCode);

        List<Expense> expenses = expenseRepository.findExpensesByTripId(trip.getId());

        java.util.List<Expense> displayExpenses = new java.util.ArrayList<>();

        for (Expense e : expenses) {

            Expense convertedE = new Expense(e.getId(), e.getTripId(), e.getDescription(),
                    e.getAmount() * tripToHomeRate, e.getCategory(), e.getDate());
            currentBudget.addExpense(convertedE);
            displayExpenses.add(convertedE);
        }

        if (expenseListView != null) {
            expenseListView.getItems().clear();
            expenseListView.getItems().addAll(displayExpenses);

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

                        double convertedAmount = item.getAmount() * homeToTargetRate;

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

        double spentTarget = spentHome * homeToTargetRate;
        double balanceTarget = balanceHome * homeToTargetRate;

        spentLabel.setText(String.format("Spent: %s%.2f (%s%.2f)", symbolTarget, spentTarget, symbolHome, spentHome));
        balanceLabel.setText(
                String.format("Balance: %s%.2f (%s%.2f)", symbolTarget, balanceTarget, symbolHome, balanceHome));

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
            double inputLimit = Double.parseDouble(limitField.getText());
            String selectedCurrency = budgetCurrencyCombo.getValue();
            String tripCurrency = currentTrip.getCurrency();

            processConversion(inputLimit, selectedCurrency, tripCurrency, convertedLimit -> {
                javafx.application.Platform
                        .runLater(() -> updateBudgetInRepo(convertedLimit, inputLimit, selectedCurrency));
            });

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number (use '.' for decimals).");
        }
    }

    private void updateBudgetInRepo(double limit, double originalInput, String originalCurrency) {
        try {
            currentTrip.setBudget(limit);
            com.isep.smarttripplanner.repository.TripRepository tripRepo = new com.isep.smarttripplanner.repository.TripRepository();
            tripRepo.updateTrip(currentTrip);
            initData(currentTrip);

            String msg = "Budget updated to " + String.format("%.2f %s", originalInput, originalCurrency);
            showAlert("Success", msg);
        } catch (Exception e) {
            showAlert("Error", "Could not save budget: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddExpense() {
        try {
            String desc = expenseDescField.getText();
            double amountInput = Double.parseDouble(expenseAmountField.getText());
            String category = categoryComboBox.getValue();
            String selectedCurrency = budgetCurrencyCombo.getValue();
            String tripCurrency = currentTrip.getCurrency();

            if (desc == null || desc.isEmpty() || category == null) {
                showAlert("Missing Info", "Please fill in all expense details.");
                return;
            }

            processConversion(amountInput, selectedCurrency, tripCurrency, convertedAmount -> {
                javafx.application.Platform.runLater(() -> saveExpense(desc, convertedAmount, category));
            });

        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for the expense amount.");
        }
    }

    private void processConversion(double amount, String selectedCurrency, String tripCurrency,
            java.util.function.Consumer<Double> callback) {
        if (selectedCurrency == null || selectedCurrency.equals(tripCurrency)) {
            callback.accept(amount);
            return;
        }

        if (selectedCurrency.equals(homeCurrency)) {
            exchangeService.getExchangeRate(homeCurrency, tripCurrency).thenAccept(rate -> {
                callback.accept(amount * rate);
            }).exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> showAlert("Error", "Could not fetch exchange rate."));
                return null;
            });
            return;
        }

        if (selectedCurrency.equals(targetCurrency)) {
            exchangeService.getExchangeRate(homeCurrency, tripCurrency).thenAccept(rToTrip -> {
                exchangeService.getExchangeRate(homeCurrency, targetCurrency).thenAccept(rToTarget -> {
                    // Prevent division by zero
                    if (rToTarget == 0)
                        rToTarget = 1.0;
                    callback.accept(amount * rToTrip / rToTarget);
                }).exceptionally(ex -> {
                    javafx.application.Platform.runLater(() -> showAlert("Error", "Could not fetch exchange rate."));
                    return null;
                });
            }).exceptionally(ex -> {
                javafx.application.Platform.runLater(() -> showAlert("Error", "Could not fetch exchange rate."));
                return null;
            });
            return;
        }

        exchangeService.getExchangeRate(selectedCurrency, tripCurrency).thenAccept(rate -> {
            callback.accept(amount * rate);
        }).exceptionally(ex -> {
            javafx.application.Platform.runLater(() -> showAlert("Error", "Could not fetch exchange rate."));
            return null;
        });
    }

    private void saveExpense(String desc, double amount, String category) {
        try {
            Expense expense = new Expense(0, currentTrip.getId(), desc, amount, category, LocalDate.now());
            expenseRepository.insertExpense(expense);
            expenseDescField.clear();
            expenseAmountField.clear();
            initData(currentTrip);
        } catch (Exception e) {
            showAlert("Error", "Could not save expense: " + e.getMessage());
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
