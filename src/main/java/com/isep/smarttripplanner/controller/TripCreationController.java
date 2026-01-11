package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.MainApplication;
import com.isep.smarttripplanner.model.Destination;
import com.isep.smarttripplanner.model.Trip;
import com.isep.smarttripplanner.repository.TripRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class TripCreationController {

    @FXML
    private VBox tripTitle;

    @FXML
    private VBox startDate;
    @FXML
    private VBox tripCreationForm;
    @FXML
    private AnchorPane tripCreationView;
    @FXML
    private TableView<Destination> destinationsTable;
    @FXML
    private TableColumn<Destination, String> colDestination;
    @FXML
    private TableColumn<Destination, String> colStartDate;
    @FXML
    private TableColumn<Destination, String> colEndDate;
    @FXML
    private Label lblTripStartDate;
    @FXML
    private Label lblTripEndDate;
    @FXML
    private TextField tripTitleInput;

    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private final ObservableList<Destination> destinationList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        if (tripCreationView == null) {
            return;
        }

        destinationsTable.setItems(destinationList);
        if (colDestination != null) {
            colDestination.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        }
        if (colStartDate != null) {
            colStartDate.setCellValueFactory(cellData -> {
                LocalDate date = cellData.getValue().getDestinationStartDate();
                return new SimpleStringProperty(date != null ? date.toString() : "");
            });
        }
        if (colEndDate != null) {
            colEndDate.setCellValueFactory(cellData -> {
                LocalDate date = cellData.getValue().getDestinationEndDate();
                return new SimpleStringProperty(date != null ? date.toString() : "");
            });
        }

        destinationList.addListener((javafx.collections.ListChangeListener<Destination>) c -> recalculateTripDates());

        tripCreationView.heightProperty().addListener(((observable, oldValue, newValue) -> {
            updateFontSize(newValue.doubleValue());
        }));
    }

    @FXML
    public void onAddDestination() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/isep/smarttripplanner/views/add-destination-view.fxml"));

            javafx.scene.Node content = loader.load();
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(content);

            AddDestinationController controller = loader.getController();

            java.util.List<javafx.util.Pair<LocalDate, LocalDate>> occupiedRanges = new java.util.ArrayList<>();
            for (Destination d : destinationList) {
                if (d.getDestinationStartDate() != null && d.getDestinationEndDate() != null) {
                    occupiedRanges.add(new javafx.util.Pair<>(d.getDestinationStartDate(), d.getDestinationEndDate()));
                }
            }
            controller.setBlockedRanges(occupiedRanges);

            Dialog<Destination> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New Destination");
            dialog.setResizable(false);

            Window owner = tripCreationView.getScene().getWindow();
            if (owner != null) {
                dialog.initOwner(owner);
                dialog.setOnShown(event -> {
                    Window dWindow = dialog.getDialogPane().getScene().getWindow();
                    dWindow.setX(owner.getX() + (owner.getWidth() - dWindow.getWidth()) / 2);
                    dWindow.setY(owner.getY() + (owner.getHeight() - dWindow.getHeight()) / 2);
                });
            }

            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            final Button btAdd = (Button) dialog.getDialogPane().lookupButton(addButtonType);
            btAdd.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                if (controller.getDestination() == null) {
                    event.consume();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Invalid Destination Details");
                    alert.setContentText(
                            "Please ensure a city is selected and dates are valid.\nEnd date cannot be before start date.");
                    alert.showAndWait();
                }
            });

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    return controller.getDestination();
                }
                return null;
            });

            Stage ownerStage = (Stage) tripCreationView.getScene().getWindow();
            boolean wasFullScreen = ownerStage != null && ownerStage.isFullScreen();

            Optional<Destination> result = dialog.showAndWait();
            result.ifPresent(destinationList::add);

            if (wasFullScreen && ownerStage != null) {
                ownerStage.setFullScreen(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not open add destination dialog: " + e.getMessage());
        }
    }

    private Trip editingTrip;

    public void setTrip(Trip trip) {
        this.editingTrip = trip;
        tripTitleInput.setText(trip.getTitle());
        destinationList.setAll(trip.getDestinations());
        saveButton.setText("Apply Changes");

        if (tripCreationView.getParent() != null) {
        }
    }

    @FXML
    public void onSaveTrip() {
        String title = tripTitleInput.getText();

        if (title == null || title.trim().isEmpty()) {
            showAlert("Validation Error", "Please enter a trip title.");
            return;
        }

        if (destinationList.isEmpty()) {
            showAlert("Validation Error", "Please add at least one destination.");
            return;
        }

        LocalDate minDate = null;
        LocalDate maxDate = null;
        for (Destination dest : destinationList) {
            LocalDate start = dest.getDestinationStartDate();
            LocalDate end = dest.getDestinationEndDate();
            if (start != null) {
                if (minDate == null || start.isBefore(minDate))
                    minDate = start;
            }
            if (end != null) {
                if (maxDate == null || end.isAfter(maxDate))
                    maxDate = end;
            }
        }

        if (editingTrip != null) {
            editingTrip.setTitle(title);
            editingTrip.setStartDate(minDate);
            editingTrip.setTripEndDate(maxDate);
            editingTrip.setDestinations(new java.util.ArrayList<>(destinationList));

            try {
                TripRepository repo = new TripRepository();
                repo.updateTrip(editingTrip);

                returnToHome();
            } catch (Exception e) {
                showAlert("Error", "Could not update trip: " + e.getMessage());
            }
        } else {
            double initialBudget = 0.0;
            Trip newTrip = new Trip(title, minDate, maxDate, initialBudget, new java.util.ArrayList<>(destinationList));

            try {
                com.isep.smarttripplanner.repository.AppConfigRepository configRepo = new com.isep.smarttripplanner.repository.AppConfigRepository();
                newTrip.setCurrency(configRepo.getConfig().getDefaultCurrency());
            } catch (Exception e) {
                newTrip.setCurrency("USD");
            }
            try {
                TripRepository repo = new TripRepository();
                repo.insertTrip(newTrip);

                returnToHome();
            } catch (Exception e) {
                showAlert("Error", "Could not save trip: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onCancel() {
        returnToHome();
    }

    private void returnToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    MainApplication.class.getResource("/com/isep/smarttripplanner/views/home-view.fxml"));
            Node homeView = loader.load();
            AnchorPane parent = (AnchorPane) tripCreationView.getParent();

            if (parent != null) {
                parent.getChildren().setAll(homeView);
                AnchorPane.setTopAnchor(homeView, 0.0);
                AnchorPane.setBottomAnchor(homeView, 0.0);
                AnchorPane.setLeftAnchor(homeView, 0.0);
                AnchorPane.setRightAnchor(homeView, 0.0);
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not return to home screen.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.setResizable(false);
        alert.showAndWait();
    }

    private void updateFontSize(double newValue) {
        double fontSize = newValue / 10;

        if (tripCreationForm == null)
            return;

        for (Node node : tripCreationForm.getChildren()) {
            if (node instanceof VBox area) {
                VBox.setMargin(area, new Insets(fontSize / 2, fontSize, 0, fontSize));
                for (Node child : area.getChildren()) {
                    if (child instanceof Label label) {
                        label.setStyle("-fx-font-size: " + (fontSize * 0.4)
                                + "px; -fx-font-weight: bold; ");
                    } else if (child instanceof TextField text) {
                        text.setStyle("-fx-min-width: " + fontSize * 9 + "px; " +
                                "-fx-max-width: " + fontSize * 9 + "px;" +
                                "-fx-font-size: " + (fontSize * 0.3)
                                + "px; ");

                    } else if (child instanceof DatePicker date) {
                        date.setStyle("-fx-min-width: " + fontSize * 9 + "px; " +
                                "-fx-max-width: " + fontSize * 9 + "px;" +
                                "-fx-font-size: " + (fontSize * 0.3)
                                + "px; ");
                    } else if (child instanceof TableView<?> table) {
                        for (TableColumn<?, ?> col : table.getColumns()) {
                            col.setStyle("-fx-font-size: " + (fontSize * 0.3)
                                    + "px; -fx-font-weight: bold; ");
                        }
                        table.setStyle("-fx-min-height: " + fontSize * 7.5 + "px; " +
                                "-fx-max-height: " + fontSize * 7.5 + "px; ");
                    } else if (child instanceof Button button) {
                        VBox.setMargin(button, new Insets(fontSize / 8, 0, 0, 0));
                        button.setStyle("-fx-font-size: " + fontSize / 4 + "px; -fx-font-weight: bold; ");
                    }
                }
            }
            if (node instanceof HBox bar) {
                VBox.setMargin(bar, new Insets(fontSize / 2, fontSize, fontSize / 2, fontSize));
                bar.setSpacing(fontSize / 2);
                for (Node child : bar.getChildren()) {
                    HBox.setMargin(bar, new Insets(0, fontSize / 2, 0, fontSize / 2));
                    if (child instanceof Button button) {
                        button.setStyle("-fx-font-size: " + fontSize / 4 + "px; -fx-font-weight: bold; ");
                    }
                }
            }
        }
    }

    private void recalculateTripDates() {
        if (destinationList.isEmpty()) {
            if (lblTripStartDate != null)
                lblTripStartDate.setText("Start Date: --/--/----");
            if (lblTripEndDate != null)
                lblTripEndDate.setText("End Date: --/--/----");
            return;
        }

        LocalDate minDate = null;
        LocalDate maxDate = null;

        for (Destination dest : destinationList) {
            LocalDate start = dest.getDestinationStartDate();
            LocalDate end = dest.getDestinationEndDate();

            if (start != null) {
                if (minDate == null || start.isBefore(minDate)) {
                    minDate = start;
                }
            }
            if (end != null) {
                if (maxDate == null || end.isAfter(maxDate)) {
                    maxDate = end;
                }
            }
        }

        if (lblTripStartDate != null) {
            lblTripStartDate.setText("Start Date: " + (minDate != null ? minDate.toString() : "--/--/----"));
        }
        if (lblTripEndDate != null) {
            lblTripEndDate.setText("End Date: " + (maxDate != null ? maxDate.toString() : "--/--/----"));
        }
    }
}