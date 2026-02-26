package com.example.equipmentapplication.window;

import com.example.equipmentapplication.dao.EquipmentDAO;
import com.example.equipmentapplication.dao.UltrasoundSensorDAO;
import com.example.equipmentapplication.dao.UltrasoundSensorDictionaryDAO;
import com.example.equipmentapplication.dto.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.equipmentapplication.FieldValidator.*;
import static com.example.equipmentapplication.util.AlertUtils.showErrorAlert;
import static com.example.equipmentapplication.util.WindowUtils.centerStageOnParent;

public class UltrasoundSensorWindow {
    private TableView<UltrasoundSensor> table;
    private ObservableList<UltrasoundSensor> sensorList;
    private ComboBox<UltrasoundSensorDictionary> sensorComboBox;
    private TextField equipmentDisplayField;
    private TextField typeField;
    private TextField snNumberField;
    private TextArea noteArea;
    private Stage sensorStage;
    private int equipmentId; // Привязка к конкретному УЗИ аппарату
    private static final String ERROR_TITLE = "Ошибка";
    private static final String SN_NUMBER_UNIQUE = "Серийный номер должен быть уникальным.";
    private static final String ADD_ULTRASOUNDSENSOR_FAILED = "Не удалось добавить датчик.";
    private static final String UPDATE_ULTRASOUNDSENSOR_FAILED = "Не удалось обновить датчик.";
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
        equipmentDisplayField = new TextField();
        equipmentDisplayField.setEditable(false); // Только для отображения
        equipmentDisplayField.setPromptText("Название и модель УЗИ аппарата");
        // Поля ввода
        sensorComboBox = new ComboBox<>();
        sensorComboBox.setPromptText("Выберите датчик");
        loadSensorComboBox();

        typeField = new TextField();
        typeField.setPromptText("Тип датчика");
        typeField.setEditable(false); // нельзя менять вручную

        snNumberField = new TextField();
        snNumberField.setPromptText("Серийный номер");

        noteArea = new TextArea();
        noteArea.setPromptText("Примечание");
        noteArea.setPrefRowCount(2);
        // Обновляем typeField при выборе датчика в ComboBox

        sensorComboBox.setOnAction(e -> {
            UltrasoundSensorDictionary selected = sensorComboBox.getValue();
            if (selected != null) {
                typeField.setText(selected.getType());
            } else {
                typeField.clear();
            }
        });
        Equipment equipment = EquipmentDAO.getEquipmentById(equipmentId);
        if (equipment != null) {
            equipmentDisplayField.setText(equipment.getName() + " " + equipment.getModel());
        }
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
        form.addRow(0, new Label("УЗИ аппарат:"), equipmentDisplayField);
        form.addRow(1, new Label("Название:"), sensorComboBox);
        form.addRow(2, new Label("Тип:"), typeField);
        form.addRow(3, new Label("Серийный номер:"), snNumberField);
        form.addRow(4, new Label("Примечание:"), noteArea);

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
    private void loadSensorComboBox() {

        // 1️⃣ Получаем текущий аппарат
        Equipment current = EquipmentDAO.getEquipmentById(equipmentId);

        // 2️⃣ Получаем все аппараты той же модели
        List<Equipment> sameModelEquipments =
                EquipmentDAO.getByModel(current.getModel());

        Set<Integer> sameModelEquipmentIds = sameModelEquipments.stream()
                .map(Equipment::getId)
                .collect(Collectors.toSet());

        // 3️⃣ Все датчики в базе
        List<UltrasoundSensor> allSensors =
                UltrasoundSensorDAO.getAllSensors();

        // 4️⃣ Все датчики из справочника
        List<UltrasoundSensorDictionary> allDictionary =
                UltrasoundSensorDictionaryDAO.getAllSensors();

        // 5️⃣ Датчики, используемые в этой модели
        Set<Integer> modelSensorIds = allSensors.stream()
                .filter(s -> sameModelEquipmentIds.contains(s.getEquipmentId()))
                .map(UltrasoundSensor::getSensorDictionaryId)
                .collect(Collectors.toSet());

        // 6️⃣ Датчики, используемые где-либо вообще
        Set<Integer> busyIds = allSensors.stream()
                .map(UltrasoundSensor::getSensorDictionaryId)
                .collect(Collectors.toSet());

        ObservableList<UltrasoundSensorDictionary> comboItems =
                FXCollections.observableArrayList();

        for (UltrasoundSensorDictionary dict : allDictionary) {

            boolean usedInThisModel = modelSensorIds.contains(dict.getId());
            boolean completelyFree = !busyIds.contains(dict.getId());

            if (usedInThisModel || completelyFree) {
                comboItems.add(dict);
            }
        }

        sensorComboBox.setItems(comboItems);

        sensorComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(UltrasoundSensorDictionary dict, boolean empty) {
                super.updateItem(dict, empty);
                if (empty || dict == null) {
                    setText(null);
                } else {
                    boolean completelyFree = !busyIds.contains(dict.getId());
                    setText(dict.getName() + (completelyFree ? " (new)" : ""));
                }
            }
        });

        sensorComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(UltrasoundSensorDictionary dict, boolean empty) {
                super.updateItem(dict, empty);
                if (empty || dict == null) {
                    setText(null);
                } else {
                    setText(dict.getName());
                }
            }
        });
    }

    private void addSensor() {
        if (!validateFields()) return;
        UltrasoundSensorFormData formData = getUltrasoundSensorFormData();
        // Проверяем уникальность серийного номера
        if (!UltrasoundSensorDAO.isSnNumberUnique(formData.snNumber, -1)) {
            highlightInvalidFields(snNumberField);
            showErrorAlert(sensorStage, ERROR_TITLE, SN_NUMBER_UNIQUE);
            return;
        }
        if (UltrasoundSensorDAO.addSensor(
                formData.dictionaryId,
                formData.name,
                formData.type,
                formData.snNumber,
                formData.note,
                equipmentId
        )) {
            loadSensors();
            loadSensorComboBox();
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
        UltrasoundSensorFormData formData = getUltrasoundSensorFormData();
        // Проверяем уникальность (исключаем текущий id)
        if (!UltrasoundSensorDAO.isSnNumberUnique(formData.snNumber, selected.getId())) {
            highlightInvalidFields(snNumberField);
            showErrorAlert(sensorStage, ERROR_TITLE, SN_NUMBER_UNIQUE);
            return;
        }
        if (UltrasoundSensorDAO.updateSensor(
                selected.getId(),
                formData.dictionaryId,
                formData.name,
                formData.type,
                formData.snNumber,
                formData.note,
                equipmentId
        )) {
            loadSensors();
            loadSensorComboBox();
            clearFields();
        } else {
            showErrorAlert(sensorStage, ERROR_TITLE, UPDATE_ULTRASOUNDSENSOR_FAILED);
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
            loadSensorComboBox();
            clearFields();
        } else {
            showErrorAlert(sensorStage, ERROR_TITLE, "Не удалось удалить датчик");
        }
    }

    public static class UltrasoundSensorFormData {
        private final int dictionaryId;
        private final String name;
        private final String type;
        private final String snNumber;
        private final String note;

        public UltrasoundSensorFormData(int dictionaryId, String name, String type, String snNumber, String note) {
            this.dictionaryId=dictionaryId;
            this.name = name;
            this.type = type;
            this.snNumber = snNumber;
            this.note = note;
        }
    }

    private UltrasoundSensorFormData getUltrasoundSensorFormData() {
        UltrasoundSensorDictionary selected = sensorComboBox.getValue();

        // Убираем "(new)" из имени для добавления в базу
        String name = selected.getName().replace(" (new)", "");
        String type = selected.getType();
        int dictionaryId = selected.getId();
        String snNumber = snNumberField.getText();
        String note = noteArea.getText();

        return new UltrasoundSensorFormData(dictionaryId, name, type, snNumber, note);
    }

    private boolean validateFields() {
        clearFieldStyles(typeField, snNumberField);
        clearComboBoxStyle(sensorComboBox);
        boolean isValid = true;
        if (sensorComboBox.getValue() == null
                || snNumberField.getText().trim().isEmpty()) {

            highlightInvalidFields(snNumberField);
            highlightInvalidComboBox(sensorComboBox);
            showErrorAlert(sensorStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        sensorComboBox.getSelectionModel().clearSelection();
        typeField.clear();
        snNumberField.clear();
        noteArea.clear();
    }

    private void fillFields(UltrasoundSensor sensor) {
        // Найти соответствующий датчик в справочнике
        for (UltrasoundSensorDictionary dict : sensorComboBox.getItems()) {
            if (dict.getName().equals(sensor.getSensorName()) &&
                    dict.getType().equals(sensor.getSensorType())) {
                sensorComboBox.setValue(dict);
                typeField.setText(dict.getType());
                break;
            }
        }
        snNumberField.setText(sensor.getSnNumber());
        noteArea.setText(sensor.getNote());
    }
}
