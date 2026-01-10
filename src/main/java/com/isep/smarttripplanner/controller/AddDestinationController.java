package com.isep.smarttripplanner.controller;

import com.isep.smarttripplanner.model.Destination;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class AddDestinationController {

    @FXML
    private TextField destinationName;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    public Destination getDestination() {
        String name = destinationName.getText();
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        Destination dest = new Destination(name, 0.0, 0.0);
        dest.setDestinationStartDate(startDate.getValue());
        dest.setDestinationEndDate(endDate.getValue());
        return dest;
    }
}
