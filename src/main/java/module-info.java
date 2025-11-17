module com.isep.smarttripplanner {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.isep.smarttripplanner to javafx.fxml;
    exports com.isep.smarttripplanner;
}