module com.isep.smarttripplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires jdk.compiler;


    opens com.isep.smarttripplanner to javafx.fxml;
    exports com.isep.smarttripplanner;
}