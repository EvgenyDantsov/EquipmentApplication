package com.example.printapplication.window;

import com.example.printapplication.dao.EquipmentTypeDAO;
import com.example.printapplication.dto.EquipmentType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import static com.example.printapplication.util.WindowUtils.centerStageOnParent;

public class EquipmentTypeWindow {
    private TableView<EquipmentType> table;
    private ObservableList<EquipmentType> equipmentTypeList;
    private TextField nameField;
    private Stage equipmentTypeStage;

    private static final String ERROR_TITLE = "Ошибка";
    private static final String FILL_REQUIRED_FIELDS = "Заполните обязательное поле.";
    private static final String SELECT_TYPE = "Выберите тип оборудования.";
    private static final String ADD_TYPE_FAILED = "Не удалось добавить тип оборудования.";
    private static final String UPDATE_TYPE_FAILED = "Не удалось изменить тип оборудования.";
    private static final String DELETE_TYPE_FAILED = "Не удалось удалить тип оборудования.";
    public void start(Stage equipmnetTypeStage, Stage parentStage) {
        this.equipmentTypeStage = equipmnetTypeStage;
        equipmnetTypeStage.setTitle("Типы оборудования");
        // Инициализация таблицы
        table = new TableView<>();
        equipmentTypeList = FXCollections.observableArrayList();

        // Колонки таблицы
        TableColumn<EquipmentType, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<EquipmentType, String> nameColumn = new TableColumn<>("Название типа");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        table.getColumns().addAll(idColumn, nameColumn);
        idColumn.setPrefWidth(50);
        nameColumn.setPrefWidth(300);

        // Загружаем данные
        loadEquipmentTypes();

        // Поле для ввода
        nameField = new TextField();
        nameField.setPromptText("Название типа");

        // Заполняем поля при выборе строки
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                nameField.setText(newSel.getName());
            }
        });

        // Кнопки
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> addEquipmentType());

        Button updateButton = new Button("Изменить");
        updateButton.setOnAction(e -> updateEquipmentType());

        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteEquipmentType());

        Button backButton = new Button("Назад");
        backButton.setOnAction(e -> equipmentTypeStage.close());

        HBox buttonsBox = new HBox(10, addButton, updateButton, deleteButton, backButton);

        // Форма
        GridPane inputForm = new GridPane();
        inputForm.setHgap(10);
        inputForm.setVgap(10);
        inputForm.addRow(0, new Label("Название типа:"), nameField);

        VBox layout = new VBox(10, table, inputForm, buttonsBox);

        Scene scene = new Scene(layout, 450, 350);
        equipmentTypeStage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        equipmentTypeStage.setOnShown(event -> centerStageOnParent(equipmnetTypeStage, parentStage));
        equipmentTypeStage.show();
    }

    private void loadEquipmentTypes() {
        equipmentTypeList.setAll(EquipmentTypeDAO.getAllEquipmentTypes());
        table.setItems(equipmentTypeList);
    }

    private void addEquipmentType() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError(ERROR_TITLE, FILL_REQUIRED_FIELDS);
            return;
        }
        if (EquipmentTypeDAO.addEquipmentType(name)) {
            loadEquipmentTypes();
            clearFields();
        } else {
            showError(ERROR_TITLE, ADD_TYPE_FAILED);
        }
    }

    private void updateEquipmentType() {
        EquipmentType selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(ERROR_TITLE, SELECT_TYPE);
            return;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError(ERROR_TITLE, FILL_REQUIRED_FIELDS);
            return;
        }
        // нужно будет реализовать update в DAO
        if (EquipmentTypeDAO.updateEquipmentType(selected.getId(), name)) {
            loadEquipmentTypes();
            clearFields();
        } else {
            showError(ERROR_TITLE, UPDATE_TYPE_FAILED);
        }
    }

    private void deleteEquipmentType() {
        EquipmentType selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError(ERROR_TITLE, SELECT_TYPE);
            return;
        }
        // нужно будет реализовать delete в DAO
        if (EquipmentTypeDAO.deleteEquipmentType(selected.getId())) {
            loadEquipmentTypes();
            clearFields();
        } else {
            showError(ERROR_TITLE, DELETE_TYPE_FAILED);
        }
    }

    private void clearFields() {
        nameField.clear();
    }

    private void showError(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }
}
