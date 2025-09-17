package com.example.printapplication.window;

import com.example.printapplication.dao.UltrasoundSensorDAO;
import com.example.printapplication.dto.UltrasoundSensor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import java.util.Collections;
import static com.example.printapplication.FieldValidator.*;
import static com.example.printapplication.util.AlertUtils.showErrorAlert;
import static com.example.printapplication.util.WindowUtils.centerStageOnParent;

public class UltrasoundSensorWindow {
    private TableView<UltrasoundSensor> table;
    private ObservableList<UltrasoundSensor> sensorList;

    private TextField nameField;
    private TextField typeField;
    private TextField snNumberField;
    private TextArea noteArea;

    private Stage sensorStage;
    private int equipmentId; // Привязка к конкретному УЗИ аппарату
    private static final String ERROR_TITLE = "Ошибка";
    private static final String SN_NUMBER_UNIQUE = "Серийный номер должен быть уникальным.";
    private static final String ADD_ULTRASOUNDSENSOR_FAILED = "Не удалось добавить датчик.";
    private static final String FILL_REQUIRED_FIELDS = "Заполните все обязательные поля.";
    private static final String SELECT_SENSOR = "Выберите датчик для изменения или удаления.";
    public UltrasoundSensorWindow(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public void start(Stage sensorStage, Stage parentStage) {
        this.sensorStage = sensorStage;
        sensorStage.setTitle("Ультразвуковые датчики");

        // Таблица
        table = new TableView<>();
        sensorList = FXCollections.observableArrayList();

        TableColumn<UltrasoundSensor, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<UltrasoundSensor, String> nameColumn = new TableColumn<>("Название датчика");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("sensorName"));

        TableColumn<UltrasoundSensor, String> typeColumn = new TableColumn<>("Тип датчика");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("sensorType"));

        TableColumn<UltrasoundSensor, String> snColumn = new TableColumn<>("Серийный номер");
        snColumn.setCellValueFactory(new PropertyValueFactory<>("snNumber"));

        TableColumn<UltrasoundSensor, String> noteColumn = new TableColumn<>("Примечание");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));

        Collections.addAll(table.getColumns(), idColumn, nameColumn, typeColumn, snColumn, noteColumn);
        idColumn.setPrefWidth(50);
        nameColumn.setPrefWidth(150);
        typeColumn.setPrefWidth(120);
        snColumn.setPrefWidth(120);
        noteColumn.setPrefWidth(200);

        // Загружаем датчики для конкретного УЗИ аппарата
        loadSensors();

        // Поля ввода
        nameField = new TextField();
        nameField.setPromptText("Название датчика");

        typeField = new TextField();
        typeField.setPromptText("Тип датчика");

        snNumberField = new TextField();
        snNumberField.setPromptText("Серийный номер");

        noteArea = new TextArea();
        noteArea.setPromptText("Примечание");
        noteArea.setPrefRowCount(2);

        // Слушатель выбора строки
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                fillFields(newSel);
            }
        });

        // Кнопки
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addSensor());

        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updateSensor());

        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteSensor());

        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> sensorStage.close());

        HBox buttons = new HBox(10, addButton, updateButton, deleteButton, backButton);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Название:"), nameField);
        form.addRow(1, new Label("Тип:"), typeField);
        form.addRow(2, new Label("Серийный номер:"), snNumberField);
        form.addRow(3, new Label("Примечание:"), noteArea);

        VBox layout = new VBox(10, table, form, buttons);
        Scene scene = new Scene(layout, 700, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        sensorStage.setScene(scene);

        sensorStage.setOnShown(event -> centerStageOnParent(sensorStage, parentStage));
        sensorStage.show();
    }

    private void loadSensors() {
        sensorList.setAll(UltrasoundSensorDAO.getSensorsByEquipmentId(equipmentId));
        table.setItems(sensorList);
    }
    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }
    private void addSensor() {
        if (!validateFields()) return;
        // Проверяем уникальность серийного номера
        if (!UltrasoundSensorDAO.isSnNumberUnique(snNumberField.getText(), -1)) {
            highlightInvalidFields(snNumberField);
            showErrorAlert(sensorStage, ERROR_TITLE, SN_NUMBER_UNIQUE);
            return;
        }
        if (UltrasoundSensorDAO.addSensor(
                nameField.getText(),
                typeField.getText(),
                snNumberField.getText(),
                noteArea.getText(),
                equipmentId
        )) {
            loadSensors();
            clearFields();
        } else {
            showErrorAlert(sensorStage, ERROR_TITLE, ADD_ULTRASOUNDSENSOR_FAILED);
        }
    }

    private void updateSensor() {
        UltrasoundSensor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert(sensorStage, ERROR_TITLE, SELECT_SENSOR);
            return;
        }
        if (!validateFields()) return;
        // Проверяем уникальность (исключаем текущий id)
        if (!UltrasoundSensorDAO.isSnNumberUnique(snNumberField.getText(), selected.getId())) {
            highlightInvalidFields(snNumberField);
            showErrorAlert(sensorStage, ERROR_TITLE, SN_NUMBER_UNIQUE);
            return;
        }
        if (UltrasoundSensorDAO.updateSensor(
                selected.getId(),
                nameField.getText(),
                typeField.getText(),
                snNumberField.getText(),
                noteArea.getText(),
                equipmentId
        )) {
            loadSensors();
            clearFields();
        } else {
            showErrorAlert(sensorStage, ERROR_TITLE, ADD_ULTRASOUNDSENSOR_FAILED);
        }
    }

    private void deleteSensor() {
        UltrasoundSensor selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert(sensorStage, ERROR_TITLE, SELECT_SENSOR);
            return;
        }

        if (UltrasoundSensorDAO.deleteSensor(selected.getId())) {
            loadSensors();
            clearFields();
        } else {
            showErrorAlert(sensorStage, ERROR_TITLE, "Не удалось удалить датчик");
        }
    }

    private boolean validateFields() {
        clearFieldStyles(nameField, typeField, snNumberField);
        boolean isValid = true;
        if (nameField.getText().trim().isEmpty()
                || typeField.getText().trim().isEmpty()
                || snNumberField.getText().trim().isEmpty()) {

            highlightInvalidFields(nameField, typeField, snNumberField);
            showErrorAlert(sensorStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        nameField.clear();
        typeField.clear();
        snNumberField.clear();
        noteArea.clear();
    }

    private void fillFields(UltrasoundSensor sensor) {
        nameField.setText(sensor.getSensorName());
        typeField.setText(sensor.getSensorType());
        snNumberField.setText(sensor.getSnNumber());
        noteArea.setText(sensor.getNote());
    }
}
