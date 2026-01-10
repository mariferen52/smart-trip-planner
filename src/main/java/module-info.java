module com.isep.smarttripplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;
    requires com.google.gson;
    requires java.net.http;
    requires java.desktop;
    requires jdk.compiler;
    requires java.sql;

    opens com.isep.smarttripplanner to javafx.fxml;
    opens com.isep.smarttripplanner.controller to javafx.fxml;
    opens com.isep.smarttripplanner.model to javafx.base;

    exports com.isep.smarttripplanner;
    exports com.isep.smarttripplanner.controller;
    exports com.isep.smarttripplanner.model;
    exports com.isep.smarttripplanner.repository;
}