package com.example.printapplication.window;

import com.example.printapplication.dao.DepartmentDAO;
import com.example.printapplication.dao.SeniorDepartmentDAO;
import com.example.printapplication.dto.Department;
import com.example.printapplication.dto.SeniorDepartment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Collections;

import static com.example.printapplication.FieldValidator.*;
import static com.example.printapplication.util.AlertUtils.showErrorAlert;
import static com.example.printapplication.util.WindowUtils.centerStageOnParent;

public class SeniorDepartmentWindow {
    private TableView<SeniorDepartment> table;
    private ObservableList<SeniorDepartment> seniorDepartmentList;
    private ObservableList<Department> departmentList; // Список отделов
    private TextField fioField;
    private ComboBox<Department> departmentComboBox; // Выпадающий список для выбора отдела
    private Stage seniorDepartmentStage; // Сохраняем родительское окно
    private static final String ERROR_TITLE = "Ошибка";
    private static final String FILL_REQUIRED_FIELDS = "Заполните все обязательные поля.";
    private static final String SELECT_FIO = "Выберите ФИО для изменения.";
    private static final String FIO_UNIQUE = "ФИО должен быть уникальным";
    private static final String ADD_SENIOR_DEPARTMENT = "Не удалось добавить старшую отделения";
    private static final String UPDATE_SENIOR_DEPARTMENT = "Не удалось обновить старшую отделения.";
    private static final String SELECT_DEPARTMENT_NAME = "Выберите название отделения.";
    private static final String SELECT_FIO_TO_DELETE = "Выберите ФИО для удаления.";
    public static final int NO_EXCLUDE = -1;

    public void start(Stage seniorDepartmentStage, Stage parentStage) {
        this.seniorDepartmentStage = seniorDepartmentStage;
        seniorDepartmentStage.setTitle("Старшие отделений");
        // Инициализация таблицы
        table = new TableView<>();
        seniorDepartmentList = FXCollections.observableArrayList();
        departmentList = FXCollections.observableArrayList(); // Инициализация списка отделов

        // Колонки таблицы
        TableColumn<SeniorDepartment, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<SeniorDepartment, String> fioColumn = new TableColumn<>("ФИО");
        fioColumn.setCellValueFactory(new PropertyValueFactory<>("fio"));
        TableColumn<SeniorDepartment, Integer> departmentIdColumn = new TableColumn<>("ID Отдела");
        departmentIdColumn.setCellValueFactory(new PropertyValueFactory<>("departmentId"));

        Collections.addAll(table.getColumns(), idColumn, fioColumn, departmentIdColumn);
        // Заполнение таблицы данными
        loadSeniorDepartment();
        loadDepartment(); // Загружаем список отделов

        // Поля для ввода данных
        fioField = new TextField();
        fioField.setPromptText("ФИО");
        fioField.setPrefWidth(250); // Устанавливаем предпочтительную ширину
        fioField.setMaxWidth(250); // Ограничиваем максимальную ширину
        fioField.setMinWidth(250); // Ограничиваем минимальную ширину

        // Выпадающий список для выбора отдела
        departmentComboBox = new ComboBox<>(departmentList);
        departmentComboBox.setPromptText("Название отделения");
        departmentComboBox.setPrefWidth(200); // Устанавливаем ширину
        departmentComboBox.setMaxWidth(200);
        departmentComboBox.setMinWidth(200);
        setupValidationListeners();
        departmentComboBox.setConverter(new StringConverter<Department>() {
            @Override
            public String toString(Department department) {
                return department.getName(); // Отображаем название отдела
            }

            @Override
            public Department fromString(String string) {
                return departmentComboBox.getItems().stream()
                        .filter(department -> department.getName().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
        // Слушатель для заполнения полей при выборе старшего отделения
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithSelectedPrint(newSelection); // Заполняем поля данными выбранного старшего отделения
            }
        });
        // Кнопки для добавления, изменения и удаления
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addSeniorDepartment());
        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updateSeniorDepartment());
        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteSeniorDepartment());
        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> seniorDepartmentStage.close());

        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, backButton);

        // Форма для ввода данных
        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.addRow(0, new Label("ФИО:"), fioField);
        inputForm.addRow(1, new Label("Название отделения:"), departmentComboBox);

        VBox layout = new VBox(10, table, inputForm, buttonsBox);
        Scene scene = new Scene(layout, 600, 400);
        seniorDepartmentStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        // Позиционирование окна по центру относительно родительского окна
        seniorDepartmentStage.setOnShown(event -> {
            centerStageOnParent(seniorDepartmentStage, parentStage);
        });
        seniorDepartmentStage.show();
    }

    // Загрузка данных из базы данных в таблицу
    private void loadSeniorDepartment() {
        seniorDepartmentList.setAll(SeniorDepartmentDAO.getAllSeniorDepartment());
        table.setItems(seniorDepartmentList);
    }

    // Загрузка данных из базы данных в таблицу
    private void loadDepartment() {
        departmentList.setAll(DepartmentDAO.getAllDepartments());
    }

    // Добавление нового старшего отделения
    private void addSeniorDepartment() {
        // Проверяем обязательные поля
        if (!validateFields()) return;
        // Проверяем, выбрано ли отделение
        if (departmentComboBox.getValue() == null) {
            highlightInvalidComboBox(departmentComboBox);
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, SELECT_DEPARTMENT_NAME);
            return;
        }
        SeniorDepartmentFormData seniorDepartmentFormData = getSeniorDepartmentFormData();
        // Проверка уникальности fio
        if (SeniorDepartmentDAO.fioExists(seniorDepartmentFormData.fio, NO_EXCLUDE)) {
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, FIO_UNIQUE);
            return;
        }
        if (SeniorDepartmentDAO.addSeniorDepartment(seniorDepartmentFormData.fio, seniorDepartmentFormData.department.getId())) {
            loadSeniorDepartment();
            clearFields();
        } else {
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, ADD_SENIOR_DEPARTMENT);
        }
    }

    // Обновление выбранного старшего отделения
    private void updateSeniorDepartment() {
        // Очищаем стили перед проверкой
        if (!validateFields()) return;
        SeniorDepartment selectedSeniorDepartment = table.getSelectionModel().getSelectedItem();
        if (selectedSeniorDepartment == null) {
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, SELECT_FIO);
            return;
        }
        SeniorDepartmentFormData seniorDepartmentFormData = getSeniorDepartmentFormData();
        // Проверка уникальности fio (исключая текущую запись)
        if (SeniorDepartmentDAO.fioExists(seniorDepartmentFormData.fio, selectedSeniorDepartment.getId())) {
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, FIO_UNIQUE);
            return;
        }
        if (SeniorDepartmentDAO.updateSeniorDepartment(selectedSeniorDepartment.getId(), seniorDepartmentFormData.fio, seniorDepartmentFormData.department.getId())) {
            loadSeniorDepartment();
            clearFields();
        } else {
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, UPDATE_SENIOR_DEPARTMENT);
        }
    }

    // Удаление выбранного старшего отделения
    private void deleteSeniorDepartment() {
        SeniorDepartment selectedSeniorDepartment = table.getSelectionModel().getSelectedItem();
        if (selectedSeniorDepartment == null) {
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, SELECT_FIO_TO_DELETE);
            return;
        }
        if (SeniorDepartmentDAO.deleteSeniorDepartment(selectedSeniorDepartment.getId())) {
            loadSeniorDepartment();
            clearFields();
        } else {
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
        }
    }

    private boolean validateFields() {
        clearFieldStyles(fioField);
        clearComboBoxStyle(departmentComboBox);

        boolean isValid = true;

        if (fioField.getText().isEmpty() ||
                departmentComboBox.getValue() == null) {
            highlightInvalidFields(fioField);
            highlightInvalidComboBox(departmentComboBox);
            showErrorAlert(seniorDepartmentStage, ERROR_TITLE, FILL_REQUIRED_FIELDS);
            isValid = false;
        }
        return isValid;
    }

    private void setupValidationListeners() {
        fioField.textProperty().addListener((observable, oldValue, newValue) -> clearFieldStyles(fioField));
        departmentComboBox.valueProperty().addListener((observable, oldValue, newValue) -> clearComboBoxStyle(departmentComboBox));
    }

    // Метод для получения данных из формы и проверки обязательных полей
    private SeniorDepartmentFormData getSeniorDepartmentFormData() {
        String fio = fioField.getText();
        Department selectedDepartment = departmentComboBox.getValue();
        return new SeniorDepartmentFormData(fio, selectedDepartment);
    }

    // Вспомогательный класс для хранения данных формы
    private static class SeniorDepartmentFormData {
        String fio;
        Department department;

        SeniorDepartmentFormData(String fio, Department department) {
            this.fio = fio;
            this.department = department;
        }
    }

    private void fillFieldsWithSelectedPrint(SeniorDepartment seniorDepartment) {
        fioField.setText(seniorDepartment.getFio());
        // Находим соответствующий Department в списке departmentList
        Department selectedDepartment = departmentList.stream()
                .filter(department -> department.getId() == seniorDepartment.getDepartmentId())
                .findFirst()
                .orElse(null);
        departmentComboBox.getSelectionModel().select(selectedDepartment);
    }

    // Очистка полей ввода
    private void clearFields() {
        fioField.clear();
        departmentComboBox.getSelectionModel().clearSelection();
    }
}