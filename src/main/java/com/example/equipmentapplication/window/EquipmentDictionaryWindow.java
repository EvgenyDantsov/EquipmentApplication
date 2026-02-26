package com.example.equipmentapplication.window;

import com.example.equipmentapplication.dao.EquipmentDictionaryDAO;
import com.example.equipmentapplication.dao.EquipmentTypeDAO;
import com.example.equipmentapplication.dto.EquipmentDictionary;
import com.example.equipmentapplication.dto.EquipmentType;
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

import static com.example.equipmentapplication.FieldValidator.*;
import static com.example.equipmentapplication.util.AlertUtils.showErrorAlert;
import static com.example.equipmentapplication.util.WindowUtils.centerStageOnParent;

public class EquipmentDictionaryWindow {
    private TableView<EquipmentDictionary> table;
    private ObservableList<EquipmentDictionary> dictionaryList;
    private ObservableList<EquipmentType> equipmentTypeList;

    private ComboBox<EquipmentType> equipmentTypeComboBox;
    private TextField nameField;
    private TextField modelField;

    private Stage equipmentDictionaryStage;
    private static final String ERROR_TITLE = "Ошибка";
    private static final String FILL_REQUIRED_FIELDS = "Заполните все обязательные поля.";
    private static final String MODEL_UNIQUE = "Модель должена быть уникальна.";
    private static final String SELECT_ENTRY = "Выберите запись для изменения.";
    private static final String SELECT_ENTRY_TO_DELETE = "Выберите запись для удаления.";
    private static final String ADD_EQUIPMENT_FAILED = "Не удалось добавить оборудование.";
    private static final String UPDATE_EQUIPMENT_FAILED = "Не удалось обновить оборудование.";
    private static final String SELECT_EQUIPMENT_TYPE = "Выберите тип оборудования.";

    public void start(Stage equipmentDictionaryStage, Stage parentStage) {
        this.equipmentDictionaryStage = equipmentDictionaryStage;
        equipmentDictionaryStage.setTitle("Справочник оборудования");

        dictionaryList = FXCollections.observableArrayList();
        equipmentTypeList = FXCollections.observableArrayList();
        loadEquipmentTypes();
        loadDictionary();

        table = new TableView<>(dictionaryList);
        TableColumn<EquipmentDictionary, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<EquipmentDictionary, Integer> typeColumn = new TableColumn<>("ID Типа");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentTypeId"));

        TableColumn<EquipmentDictionary, String> nameColumn = new TableColumn<>("Производитель");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<EquipmentDictionary, String> modelColumn = new TableColumn<>("Модель");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));

        table.getColumns().addAll(idColumn, typeColumn, nameColumn, modelColumn);

        // Поля ввода
        equipmentTypeComboBox = new ComboBox<>(equipmentTypeList);
        equipmentTypeComboBox.setPromptText("Тип оборудования");

        nameField = new TextField();
        nameField.setPromptText("Производитель");

        modelField = new TextField();
        modelField.setPromptText("Модель");
        setupValidationListeners();
        equipmentTypeComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(EquipmentType type) {
                return type != null ? type.getName() : "";
            }

            @Override
            public EquipmentType fromString(String string) {
                return equipmentTypeList.stream()
                        .filter(t -> t.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // Слушатель для заполнения полей при выборе записи
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) fillFieldsWithSelectedEntry(newSelection);
        });
        // Кнопки
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addDictionaryEntry());

        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updateDictionaryEntry());

        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteDictionaryEntry());

        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> equipmentDictionaryStage.close());

        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, backButton);

        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.addRow(0, new Label("Тип оборудования:"), equipmentTypeComboBox);
        inputForm.addRow(1, new Label("Производитель:"), nameField);
        inputForm.addRow(2, new Label("Модель:"), modelField);

        VBox layout = new VBox(10, table, inputForm, buttonsBox);
        Scene scene = new Scene(layout, 600, 500);
        equipmentDictionaryStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        equipmentDictionaryStage.setOnShown(event -> centerStageOnParent(equipmentDictionaryStage, parentStage));
        equipmentDictionaryStage.show();

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) fillFieldsWithSelectedEntry(newSelection);
        });
    }

    private void loadEquipmentTypes() {
        equipmentTypeList.setAll(EquipmentTypeDAO.getAllEquipmentTypes());
    }

    private void loadDictionary() {
        dictionaryList.setAll(EquipmentDictionaryDAO.getAllEntries());
    }

    private void addDictionaryEntry() {
        if (!validateFields()) return;
        // Проверяем обязательные поля
        if (equipmentTypeComboBox.getValue() == null || nameField.getText().isEmpty() || modelField.getText().isEmpty()) {
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            return;
        }

        // Проверка уникальности модели
        if (!EquipmentDictionaryDAO.isModelUnique(modelField.getText(), -1)) { // -1 для новой записи
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, MODEL_UNIQUE);
            return;
        }

        // Добавляем запись
        boolean success = EquipmentDictionaryDAO.addDictionaryEntry(
                equipmentTypeComboBox.getValue().getId(),
                nameField.getText(),
                modelField.getText()
        );

        if (success) {
            loadDictionary();
            clearFields();
        } else {
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, ADD_EQUIPMENT_FAILED);
        }
    }

    private void updateDictionaryEntry() {
        if (!validateFields()) return;
        EquipmentDictionary selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, SELECT_ENTRY);
            return;
        }
        if (equipmentTypeComboBox.getValue() == null || nameField.getText().isEmpty() || modelField.getText().isEmpty()) {
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            return;
        }

        // Проверка уникальности модели (исключая текущую запись)
        if (!EquipmentDictionaryDAO.isModelUnique(modelField.getText(), selected.getId())) {
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, MODEL_UNIQUE);
            return;
        }

        // Обновляем запись
        boolean success = EquipmentDictionaryDAO.updateDictionaryEntry(
                selected.getId(),
                nameField.getText(),
                modelField.getText(),
                equipmentTypeComboBox.getValue().getId()
        );

        if (success) {
            loadDictionary();
            clearFields();
        } else {
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, UPDATE_EQUIPMENT_FAILED);
        }
    }

    private void deleteDictionaryEntry() {
        EquipmentDictionary selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, SELECT_ENTRY_TO_DELETE);
            return;
        }
        boolean success = EquipmentDictionaryDAO.deleteDictionaryEntry(selected.getId());
        if (success) {
            loadDictionary();
            clearFields();
        } else {
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
        }
    }

    private void fillFieldsWithSelectedEntry(EquipmentDictionary entry) {
        nameField.setText(entry.getName());
        modelField.setText(entry.getModel());
        EquipmentType type = equipmentTypeList.stream()
                .filter(t -> t.getId() == entry.getEquipmentTypeId())
                .findFirst().orElse(null);
        equipmentTypeComboBox.setValue(type);
    }

    private boolean validateFields() {
        clearFieldStyles(nameField, modelField);
        clearComboBoxStyle(equipmentTypeComboBox);

        boolean isValid = true;

        if (nameField.getText().isEmpty() ||
                modelField.getText().isEmpty() ||
                equipmentTypeComboBox.getValue() == null) {

            highlightInvalidFields(nameField, modelField);
            highlightInvalidComboBox(equipmentTypeComboBox);
            showErrorAlert(equipmentDictionaryStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        nameField.clear();
        modelField.clear();
        equipmentTypeComboBox.getSelectionModel().clearSelection();
    }

    private void setupValidationListeners() {
        nameField.textProperty().addListener((obs, oldVal, newVal) -> clearFieldStyles(nameField));
        modelField.textProperty().addListener((obs, oldVal, newVal) -> clearFieldStyles(modelField));
        equipmentTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> clearComboBoxStyle(equipmentTypeComboBox));
    }
}