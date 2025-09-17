package com.example.printapplication.window;

import com.example.printapplication.dao.EquipmentTypeDAO;
import com.example.printapplication.dao.OfficeDAO;
import com.example.printapplication.dao.EquipmentDAO;
import com.example.printapplication.dao.EquipmentDictionaryDAO;

import com.example.printapplication.dto.EquipmentType;
import com.example.printapplication.dto.Office;
import com.example.printapplication.dto.Equipment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
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
import static com.example.printapplication.util.AlertUtils.*;
import static com.example.printapplication.util.WindowUtils.*;

public class EquipmentWindow {
    private TableView<Equipment> table;
    private ObservableList<Equipment> equipmentList;
    private ObservableList<Office> officeList; // Список кабинетов
    private ObservableList<EquipmentType> equipmentTypeList;
    private TextField equipmentTypeField;
    private int initialTypeId = 0;
    private String initialTypeName = null;
    private ComboBox<String> nameComboBox;
    private ComboBox<String> modelComboBox;
    private TextField snNumberField;
    private TextField noteField;
    private TextField filterOfficeField; // Поле для ввода номера кабинета
    private Button clearFilterButton; // Кнопка для сброса фильтра
    private ComboBox<Office> officeComboBox; // Выпадающий список для выбора номера кабинета
    private ComboBox<String> statusComboBox;
    private Stage equipmentStage; // Сохраняем родительское окно
    private static final String ERROR_TITLE = "Ошибка";
    private static final String FILL_REQUIRED_FIELDS = "Заполните все обязательные поля.";
    private static final String SELECT_EQUIPMENT = "Выберите оборудование для изменения.";
    private static final String SN_NUMBER_UNIQUE = "Серийный номер должен быть уникальным.";
    private static final String ADD_EQUIPMENT_FAILED = "Не удалось добавить оборудование.";
    private static final String UPDATE_EQUIPMENT_FAILED = "Не удалось обновить оборудование.";
    private static final String SELECT_OFFICE = "Выберите номер кабинета.";
    private static final String SELECT_EQUIPMENT_TO_DELETE = "Выберите оборудование для удаления.";

    public void start(Stage equipmentStage, Stage parentStage) {
        this.equipmentStage = equipmentStage;
        equipmentStage.setTitle("Оборудование");
        officeList = FXCollections.observableArrayList();
        loadOffice();
        equipmentList=FXCollections.observableArrayList();
        // Инициализация таблицы
        table = new TableView<>();
        table.setItems(equipmentList);
        equipmentTypeList = FXCollections.observableArrayList();
        loadEquipmentTypes();

        // Колонки таблицы
        TableColumn<Equipment, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Equipment, String> nameColumn = new TableColumn<>("Название оборудования");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Equipment, String> modelColumn = new TableColumn<>("Модель ");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        TableColumn<Equipment, String> snColumn = new TableColumn<>("Серийный номер");
        snColumn.setCellValueFactory(new PropertyValueFactory<>("snNumber"));
        TableColumn<Equipment, String> noteColumn = new TableColumn<>("Примечание");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        TableColumn<Equipment, Integer> officeIdColumn = new TableColumn<>("ID Кабинета");
        officeIdColumn.setCellValueFactory(new PropertyValueFactory<>("officeId"));
        // Новая колонка для статуса
        TableColumn<Equipment, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(createStatusTableCellFactory());
        TableColumn<Equipment, Integer> equipmentTypeColumn = new TableColumn<>("Тип оборудования"); // NEW
        equipmentTypeColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentTypeId")); // NEW
        Collections.addAll(table.getColumns(), idColumn, nameColumn, modelColumn, snColumn, noteColumn, statusColumn, officeIdColumn, equipmentTypeColumn);
        loadEquipment();
        // Поля для ввода данных
        nameComboBox = new ComboBox<>();
        nameComboBox.setPromptText("Название оборудования...");
        modelComboBox = new ComboBox();
        // Подгружаем названия для фиксированного типа сразу
        if (initialTypeId != 0) {
            nameComboBox.setItems(
                    FXCollections.observableArrayList(
                            EquipmentDictionaryDAO.getManufacturersByType(initialTypeId)
                    )
            );
        }
        modelComboBox.setPromptText("Модель...");
        modelComboBox.setItems(FXCollections.observableArrayList());
        Tooltip modelTooltip = new Tooltip("Сначала выберите название оборудования");
        modelComboBox.setTooltip(modelTooltip);
        equipmentTypeField = new TextField();
        equipmentTypeField.setText(initialTypeName != null ? initialTypeName : "");
        equipmentTypeField.setEditable(false); // нельзя редактировать
        equipmentTypeField.setStyle("-fx-opacity: 1;");
        // Блокировка выбора, если название не выбрано
        modelComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
                // Ставим disable для каждой ячейки, если nameComboBox пуст
                setDisable(nameComboBox.getValue() == null);
            }
        });
        // При выборе производителя – загружаем модели
        nameComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && initialTypeId != 0) {
                int typeId = initialTypeId;
                modelComboBox.setItems(
                        FXCollections.observableArrayList(
                                EquipmentDictionaryDAO.getModelsByTypeAndManufacturer(typeId, newVal)
                        )
                );
                modelComboBox.setDisable(false);
            } else {
                modelComboBox.getItems().clear();
                modelComboBox.setDisable(true);
            }
        });
        // Подсказка при наведении на модель, если название не выбрано
        modelComboBox.setOnMouseEntered(event -> {
            if (nameComboBox.getValue() == null) {
                modelTooltip.setText("Сначала выберите название оборудования");
                modelTooltip.show(modelComboBox,
                        event.getScreenX(), event.getScreenY() + 20);
            } else {
                modelTooltip.hide();
            }
        });
        // Скрываем подсказку при уходе мыши
        modelComboBox.setOnMouseExited(event -> modelTooltip.hide());
        snNumberField = new TextField();
        snNumberField.setPromptText("Серийный номер...");
        noteField = new TextField();
        noteField.setPromptText("Примечание...");
        // Добавляем элементы для фильтрации
        filterOfficeField = new TextField();
        filterOfficeField.setPromptText("Введите номер кабинета...");

        clearFilterButton = new Button("Сбросить фильтр");
        clearFilterButton.setOnAction(e -> {
            filterOfficeField.clear();
            table.setItems(equipmentList); // Показываем все записи
        });
        // Слушатель для фильтрации при вводе текста
        filterOfficeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                table.setItems(equipmentList);
            } else {
                filterTableByOfficeNumber(newVal);
            }
        });
        // Выпадающий список для выбора кабинета
        officeComboBox = new ComboBox<>(officeList);
        officeComboBox.setPromptText("Номер кабинета");
        // Новый комбобокс для статуса
        statusComboBox = new ComboBox<>(FXCollections.observableArrayList(
                "Установлен", "На хранении", "Списан"
        ));
        statusComboBox.setPromptText("Статус");
        statusComboBox.getSelectionModel().selectFirst(); // По умолчанию "Активный"
        setupValidationListeners();
        officeComboBox.setConverter(new StringConverter<Office>() {
            @Override
            public String toString(Office office) {
                return office.getNumberOffice(); // Отображаем название кабинета
            }

            @Override
            public Office fromString(String string) {
                return officeComboBox.getItems().stream()
                        .filter(office -> office.getNumberOffice().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        // Слушатель для заполнения полей при выборе принтера
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithSelectedEquipment(newSelection); // Заполняем поля данными выбранного принтера
            }
        });
        // Кнопки для добавления, изменения и удаления
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addEquipment());
        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updateEquipment());
        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteEquipment());
        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> equipmentStage.close());
        Button sensorsButton = new Button("Датчики");
        if (initialTypeName != null && initialTypeName.equalsIgnoreCase("Ультразвуковой аппарат")) {
            sensorsButton.setOnAction(e -> {
                Equipment selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    // Открываем окно датчиков, передавая ID конкретного аппарата
                    UltrasoundSensorWindow sensorWindow = new UltrasoundSensorWindow(selected.getId());
                    Stage sensorStage = new Stage();
                    sensorWindow.start(sensorStage, equipmentStage);
                } else {
                    showErrorAlert(equipmentStage, "Ошибка", "Выберите УЗИ аппарат, чтобы просмотреть датчики");
                }
            });
        } else {
            sensorsButton.setVisible(false); // для других типов кнопка скрыта
        }
        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, backButton, clearFilterButton);
        if (sensorsButton.isVisible()) {
            buttonsBox.getChildren().add(sensorsButton);
        }
        // Форма для ввода данных
        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.addRow(0, new Label("Название оборудования:"), nameComboBox);
        inputForm.addRow(0, new Label("Модель:"), modelComboBox);
        inputForm.addRow(1, new Label("Серийный номер:"), snNumberField);
        inputForm.addRow(1, new Label("Фильтр по номеру кабинета:"), filterOfficeField);
        inputForm.addRow(2, new Label("Примечание:"), noteField);
        inputForm.addRow(2, new Label("Статус:"), statusComboBox);
        inputForm.addRow(3, new Label("Номер кабинета:"), officeComboBox);
        inputForm.addRow(4, new Label("Тип оборудования:"), equipmentTypeField); // NEW
        inputForm.add(clearFilterButton, 3, 3); // Колонка 3, строка 3
        GridPane.setHalignment(clearFilterButton, HPos.LEFT); // Выравнивание по левому краю

        VBox layout = new VBox(10, table, inputForm, buttonsBox);
        Scene scene = new Scene(layout, 900, 550);
        equipmentStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        // Позиционирование окна по центру относительно родительского окна
        equipmentStage.setOnShown(event -> centerStageOnParent(equipmentStage, parentStage));
        equipmentStage.show();
    }
    private int findOfficeIdByNumber(String number) {
        return officeList.stream()
                .filter(o -> o.getNumberOffice().equals(number))
                .map(Office::getId)
                .findFirst()
                .orElse(0); // 0 если не найдено
    }
    // Загрузка данных из базы данных в таблицу
    private void loadEquipment() {
        if (initialTypeId != 0) {
            // Загружаем только по выбранному типу
            equipmentList.setAll(EquipmentDAO.getEquipmentByType(initialTypeId));
        } else {
            // Загружаем всё оборудование
            equipmentList.setAll(EquipmentDAO.getAllEquipment());
        }
        table.setItems(equipmentList);
    }
    public void setInitialTypeId(int id) {
        this.initialTypeId = id;
    }
    public void setInitialTypeName(String name) {
        this.initialTypeName = name;
    }
    private void loadEquipmentTypes() {
        equipmentTypeList.setAll(EquipmentTypeDAO.getAllEquipmentTypes());
    }
    // Метод для фильтрации таблицы по номеру кабинета
    private void filterTableByOfficeNumber(String officeNumber) {
        ObservableList<Equipment> filteredList = equipmentList.filtered(p -> {
            // Находим кабинет по ID принтера
            Office office = officeList.stream()
                    .filter(o -> o.getId() == p.getOfficeId())
                    .findFirst()
                    .orElse(null);

            // Проверяем, содержит ли номер кабинета введенный текст (без учета регистра)
            return office != null && office.getNumberOffice().toLowerCase()
                    .contains(officeNumber.toLowerCase());
        });
        table.setItems(filteredList);
    }

    // Загрузка данных из базы данных в таблицу
    private void loadOffice() {
        officeList.setAll(OfficeDAO.getAllOffice());
    }

    // Добавление нового принтера
    private void addEquipment() {
        // Проверяем обязательные поля
        if (!validateFields()) return;
        // Проверяем, выбран ли кабинет
        if (officeComboBox.getValue() == null) {
            highlightInvalidComboBox(officeComboBox);
            showErrorAlert(equipmentStage, ERROR_TITLE, SELECT_OFFICE);
            return;
        }
        EquipmentFormData equipmentFormData = getEquipmentFormData();
        String status = getStatusFromComboBox();
        // Проверка уникальности snNumber
        if (EquipmentDAO.isSnNumberUnique(equipmentFormData.snNumber, -1)) { // -1 означает, что это новая запись
            showErrorAlert(equipmentStage, ERROR_TITLE, SN_NUMBER_UNIQUE);
            return;
        }
        if (EquipmentDAO.addEquipment(equipmentFormData.name, equipmentFormData.model, equipmentFormData.snNumber, equipmentFormData.note, status, equipmentFormData.office.getId(), equipmentFormData.equipmentType.getId())) {
            loadEquipment();
            clearFields();
        } else {
            showErrorAlert(equipmentStage, ERROR_TITLE, ADD_EQUIPMENT_FAILED);
        }
    }

    // Обновление выбранного принтера
    private void updateEquipment() {
        // Очищаем стили перед проверкой
        if (!validateFields()) return;
        Equipment selectedEquipment = table.getSelectionModel().getSelectedItem();
        if (selectedEquipment == null) {
            showErrorAlert(equipmentStage, ERROR_TITLE, SELECT_EQUIPMENT);
            return;
        }
        EquipmentFormData equipmentFormData = getEquipmentFormData();
        String status = getStatusFromComboBox();
        // Проверка уникальности snNumber (исключая текущую запись)
        if (EquipmentDAO.isSnNumberUnique(equipmentFormData.snNumber, selectedEquipment.getId())) {
            showErrorAlert(equipmentStage, ERROR_TITLE, SN_NUMBER_UNIQUE);
            return;
        }
        if (EquipmentDAO.updateEquipment(selectedEquipment.getId(), equipmentFormData.name, equipmentFormData.model, equipmentFormData.snNumber, equipmentFormData.note, status, equipmentFormData.office.getId(),equipmentFormData.equipmentType.getId())) {
            loadEquipment();
            clearFields();
        } else {
            showErrorAlert(equipmentStage, ERROR_TITLE, UPDATE_EQUIPMENT_FAILED);
        }
    }

    // Удаление выбранного принтера
    private void deleteEquipment() {
        Equipment selectedEquipment = table.getSelectionModel().getSelectedItem();
        if (selectedEquipment == null) {
            showErrorAlert(equipmentStage, ERROR_TITLE, SELECT_EQUIPMENT_TO_DELETE);
            return;
        }
        if (EquipmentDAO.deleteEquipment(selectedEquipment.getId())) {
            loadEquipment();
            clearFields();
        } else {
            showErrorAlert(equipmentStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
        }
    }

    private void setupValidationListeners() {
        nameComboBox.valueProperty().addListener((observable, oldValue, newValue) -> clearComboBoxStyle(nameComboBox));
        modelComboBox.valueProperty().addListener((observable, oldValue, newValue) -> clearComboBoxStyle(modelComboBox));
        snNumberField.textProperty().addListener((observable, oldValue, newValue) -> clearFieldStyles(snNumberField));
        officeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> clearComboBoxStyle(officeComboBox));
    }

    private boolean validateFields() {
        clearFieldStyles(snNumberField);
        clearComboBoxStyle(nameComboBox, modelComboBox,officeComboBox);

        boolean isValid = true;

        if (nameComboBox.getValue()==null ||
                modelComboBox.getValue()==null ||
                snNumberField.getText().isEmpty() ||
                officeComboBox.getValue() == null) {

            highlightInvalidFields(snNumberField);
            highlightInvalidComboBox(nameComboBox,modelComboBox, officeComboBox);
            showErrorAlert(equipmentStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            isValid = false;
        }

        return isValid;
    }

    // Метод для получения данных из формы и проверки обязательных полей
    private EquipmentFormData getEquipmentFormData() {
        String name = nameComboBox.getValue();
        String model = modelComboBox.getValue();
        String snNumber = snNumberField.getText();
        String note = noteField.getText();
        Office selectedOffice = officeComboBox.getValue();
        EquipmentType selectedType = new EquipmentType(initialTypeId, initialTypeName);

        return new EquipmentFormData(name, model, snNumber, note, selectedOffice, selectedType);
    }

    // Вспомогательный класс для хранения данных формы
    private static class EquipmentFormData {
        String name, model, snNumber, note;
        Office office;
        EquipmentType equipmentType;

        EquipmentFormData(String name, String model, String snNumber, String note, Office office, EquipmentType equipmentType) {
            this.name = name;
            this.model = model;
            this.snNumber = snNumber;
            this.note = note;
            this.office = office;
            this.equipmentType = equipmentType;
        }
    }

    private void fillFieldsWithSelectedEquipment(Equipment equipment) {
        nameComboBox.setValue(equipment.getName());
        modelComboBox.setValue(equipment.getModel());
        snNumberField.setText(equipment.getSnNumber());
        noteField.setText(equipment.getNote());
        // Устанавливаем статус
        String statusDisplayName = getStatusDisplayName(equipment.getStatus());
        statusComboBox.getSelectionModel().select(statusDisplayName);
        // Находим соответствующий Office в списке officeList
        Office selectedOffice = officeList.stream()
                .filter(office -> office.getId() == equipment.getOfficeId())
                .findFirst()
                .orElse(null);
        officeComboBox.getSelectionModel().select(selectedOffice);
        equipmentTypeField.setText(
                equipmentTypeList.stream()
                        .filter(et -> et.getId() == equipment.getEquipmentTypeId())
                        .map(EquipmentType::getName)
                        .findFirst()
                        .orElse("")
        );
    }

    // Очистка полей ввода
    private void clearFields() {
        nameComboBox.getSelectionModel().clearSelection();
        modelComboBox.getSelectionModel().clearSelection();
        snNumberField.clear();
        noteField.clear();
        officeComboBox.getSelectionModel().clearSelection();
        equipmentTypeField.setText(initialTypeName != null ? initialTypeName : "");
    }
    private String getStatusFromComboBox() {
        String selectedStatus = statusComboBox.getValue();
        return switch (selectedStatus) {
            case "Установлен" -> "active";
            case "На хранении" -> "in_storage";
            case "Списан" -> "written_off";
            default -> "active";
        };
    }
}