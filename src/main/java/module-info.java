module com.isep.smarttripplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.compiler;

    opens com.isep.smarttripplanner to javafx.fxml;
    opens com.isep.smarttripplanner.controller to javafx.fxml;

    exports com.isep.smarttripplanner;
    exports com.isep.smarttripplanner.controller;
}