package com.example.equipmentapplication.window;

import com.example.equipmentapplication.dao.UltrasoundSensorDictionaryDAO;
import com.example.equipmentapplication.dto.UltrasoundSensorDictionary;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static com.example.equipmentapplication.FieldValidator.*;
import static com.example.equipmentapplication.util.AlertUtils.showErrorAlert;
import static com.example.equipmentapplication.util.WindowUtils.centerStageOnParent;

public class UltrasoundSensorDictionaryWindow {
    private TableView<UltrasoundSensorDictionary> table;
    private ObservableList<UltrasoundSensorDictionary> ultrasoundSensorList;
    private TextField nameField;
    private TextField typeField;
    private Stage ultrasoundSensorDictionaryStage;
    private static final String ERROR_TITLE = "Ошибка";
    private static final String FILL_REQUIRED_FIELDS = "Заполните все обязательные поля.";
    private static final String MODEL_UNIQUE = "Модель должна быть уникальна для данного имени и типа.";
    private static final String SELECT_ENTRY = "Выберите запись для изменения.";
    private static final String SELECT_ENTRY_TO_DELETE = "Выберите запись для удаления.";
    private static final String ADD_SENSOR_FAILED = "Не удалось добавить датчик.";
    private static final String UPDATE_SENSOR_FAILED = "Не удалось обновить датчик.";

    public void start(Stage stage, Stage parentStage) {
        this.ultrasoundSensorDictionaryStage = stage;
        ultrasoundSensorDictionaryStage.setTitle("Справочник датчиков");

        ultrasoundSensorList = FXCollections.observableArrayList();
        table = new TableView<>(ultrasoundSensorList);

        // Таблица
        TableColumn<UltrasoundSensorDictionary, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<UltrasoundSensorDictionary, String> nameColumn = new TableColumn<>("Название датчика");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<UltrasoundSensorDictionary, String> modelColumn = new TableColumn<>("Тип датчика");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        table.getColumns().addAll(idColumn, nameColumn, modelColumn);

        // Поля ввода
        nameField = new TextField();
        nameField.setPromptText("Название");

        typeField = new TextField();
        typeField.setPromptText("Тип");
        setupValidationListeners();

        // Слушатель выбора записи
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) fillFieldsWithSelectedEntry(newSel);
        });

        // Кнопки
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addSensor());

        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updateSensor());

        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteSensor());

        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> ultrasoundSensorDictionaryStage.close());

        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, backButton);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.addRow(0, new Label("Название:"), nameField);
        form.addRow(1, new Label("Тип:"), typeField);

        VBox layout = new VBox(10, table, form, buttonsBox);
        Scene scene = new Scene(layout, 600, 500);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        ultrasoundSensorDictionaryStage.setScene(scene);

        ultrasoundSensorDictionaryStage.setOnShown(event -> centerStageOnParent(ultrasoundSensorDictionaryStage, parentStage));

        loadSensors();
        ultrasoundSensorDictionaryStage.show();
    }

    private void loadSensors() {
        ultrasoundSensorList.setAll(UltrasoundSensorDictionaryDAO.getAllSensors());
    }

    private void addSensor() {
        if (!validateFields()) return;

        if (!UltrasoundSensorDictionaryDAO.isModelUnique(nameField.getText(), typeField.getText(), -1)) {
            showErrorAlert(ultrasoundSensorDictionaryStage, ERROR_TITLE, MODEL_UNIQUE);
            return;
        }

        if (UltrasoundSensorDictionaryDAO.addSensor(nameField.getText(), typeField.getText())) {
            loadSensors();
            clearFields();
        } else {
            showErrorAlert(ultrasoundSensorDictionaryStage, ERROR_TITLE, ADD_SENSOR_FAILED);
        }
    }

    private void updateSensor() {
        UltrasoundSensorDictionary selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert(ultrasoundSensorDictionaryStage, ERROR_TITLE, SELECT_ENTRY);
            return;
        }

        if (!validateFields()) return;

        if (!UltrasoundSensorDictionaryDAO.isModelUnique(selected.getName(), selected.getType(), selected.getId())) {
            showErrorAlert(ultrasoundSensorDictionaryStage, ERROR_TITLE, MODEL_UNIQUE);
            return;
        }

        if (UltrasoundSensorDictionaryDAO.updateSensor(selected)) {
            loadSensors();
            clearFields();
        } else {
            showErrorAlert(ultrasoundSensorDictionaryStage, ERROR_TITLE, UPDATE_SENSOR_FAILED);
        }
    }

    private void deleteSensor() {
        UltrasoundSensorDictionary selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert(ultrasoundSensorDictionaryStage, ERROR_TITLE, SELECT_ENTRY_TO_DELETE);
            return;
        }

        if (UltrasoundSensorDictionaryDAO.deleteSensor(selected.getId())) {
            loadSensors();
            clearFields();
        } else {
            showErrorAlert(ultrasoundSensorDictionaryStage, ERROR_TITLE, "Не удалось удалить запись.");
        }
    }

    private void fillFieldsWithSelectedEntry(UltrasoundSensorDictionary sensor) {
        nameField.setText(sensor.getName());
        typeField.setText(sensor.getType());
    }

    private boolean validateFields() {

        boolean isValid = true;

        if (nameField.getText().isEmpty() ||
                typeField.getText().isEmpty()) {
            highlightInvalidFields(nameField, typeField);
            showErrorAlert(ultrasoundSensorDictionaryStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        nameField.clear();
        typeField.clear();
    }

    private void setupValidationListeners() {
        nameField.textProperty().addListener((obs, oldVal, newVal) -> clearFieldStyles(nameField));
        typeField.textProperty().addListener((obs, oldVal, newVal) -> clearFieldStyles(typeField));
    }
}