module com.example.equipmentapplication {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.base;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.example.equipmentapplication to javafx.fxml;
    exports com.example.equipmentapplication;
    exports com.example.equipmentapplication.dto;
    opens com.example.equipmentapplication.dto to javafx.fxml;
    exports com.example.equipmentapplication.window;
    opens com.example.equipmentapplication.window to javafx.fxml;
}