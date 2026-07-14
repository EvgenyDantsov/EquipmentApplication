package com.example.equipmentapplication.window;

import com.example.equipmentapplication.dao.EquipmentDAO;
import com.example.equipmentapplication.dao.EquipmentRepairDAO;
import com.example.equipmentapplication.dao.OfficeDAO;
import com.example.equipmentapplication.dto.Equipment;
import com.example.equipmentapplication.dto.EquipmentRepair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.example.equipmentapplication.FieldValidator.clearFieldStyles;
import static com.example.equipmentapplication.util.AlertUtils.showErrorAlert;
import static com.example.equipmentapplication.util.WindowUtils.centerStageOnParent;

public class EquipmentRepairWindow {
    private TableView<EquipmentRepair> table;
    private ObservableList<EquipmentRepair> repairList;

    private TextField equipmentInfoField;

    private DatePicker repairDatePicker;

    private TextArea malfunctionArea;
    private TextArea workDoneArea;

    private TextField costField;

    private Stage repairStage;

    private final int equipmentId;

    private static final String ERROR_TITLE = "Ошибка";

    public EquipmentRepairWindow(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public void start(Stage repairStage, Stage parentStage) {

        this.repairStage = repairStage;

        repairStage.setTitle("История ремонтов");

        table = new TableView<>();
        repairList = FXCollections.observableArrayList();

        TableColumn<EquipmentRepair, Integer> idColumn =
                new TableColumn<>("ID");
        idColumn.setCellValueFactory(
                new PropertyValueFactory<>("id")
        );
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        TableColumn<EquipmentRepair, LocalDate> dateColumn =
                new TableColumn<>("Дата ремонта");
        dateColumn.setCellValueFactory(
                new PropertyValueFactory<>("repairDate")
        );
        dateColumn.setCellFactory(column -> new TableCell<EquipmentRepair, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });

        TableColumn<EquipmentRepair, String> malfunctionColumn =
                new TableColumn<>("Неисправность");
        malfunctionColumn.setCellValueFactory(
                new PropertyValueFactory<>("malfunction")
        );

        TableColumn<EquipmentRepair, String> workDoneColumn =
                new TableColumn<>("Выполненные работы");
        workDoneColumn.setCellValueFactory(
                new PropertyValueFactory<>("workDone")
        );

        TableColumn<EquipmentRepair, BigDecimal> costColumn =
                new TableColumn<>("Стоимость");
        costColumn.setCellValueFactory(
                new PropertyValueFactory<>("cost")
        );

        table.getColumns().addAll(
                idColumn,
                dateColumn,
                malfunctionColumn,
                workDoneColumn,
                costColumn
        );

        idColumn.setPrefWidth(60);
        dateColumn.setPrefWidth(120);
        malfunctionColumn.setPrefWidth(250);
        workDoneColumn.setPrefWidth(250);
        costColumn.setPrefWidth(100);

        loadRepairs();

        equipmentInfoField = new TextField();
        equipmentInfoField.setEditable(false);

        Equipment equipment =
                EquipmentDAO.getEquipmentById(equipmentId);

        if (equipment != null) {

            String officeNumber =
                    OfficeDAO.getOfficeNumberById(
                            equipment.getOfficeId()
                    );

            equipmentInfoField.setText(
                    equipment.getName()
                            + " "
                            + equipment.getModel()
                            + " | Кабинет "
                            + officeNumber
            );
        }

        repairDatePicker = new DatePicker();
        repairDatePicker.setValue(LocalDate.now());

        malfunctionArea = new TextArea();
        malfunctionArea.setPrefRowCount(3);

        workDoneArea = new TextArea();
        workDoneArea.setPrefRowCount(3);

        costField = new TextField();

        table.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {

                    if (newValue != null) {
                        fillFields(newValue);
                    }
                });

        Button addButton =
                new Button("Добавить");

        addButton.setOnAction(
                e -> addRepair()
        );

        Button updateButton =
                new Button("Изменить");

        updateButton.setOnAction(
                e -> updateRepair()
        );

        Button deleteButton =
                new Button("Удалить");

        deleteButton.setOnAction(
                e -> deleteRepair()
        );

        Button backButton =
                new Button("Назад");

        backButton.setOnAction(
                e -> repairStage.close()
        );

        HBox buttons = new HBox(
                10,
                addButton,
                updateButton,
                deleteButton,
                backButton
        );

        GridPane form = new GridPane();

        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        form.addRow(
                0,
                new Label("Оборудование:"),
                equipmentInfoField
        );

        form.addRow(
                1,
                new Label("Дата ремонта:"),
                repairDatePicker
        );

        form.addRow(
                2,
                new Label("Неисправность:"),
                malfunctionArea
        );

        form.addRow(
                3,
                new Label("Выполненные работы:"),
                workDoneArea
        );

        form.addRow(
                4,
                new Label("Стоимость:"),
                costField
        );

        VBox layout =
                new VBox(
                        10,
                        table,
                        form,
                        buttons
                );

        Scene scene =
                new Scene(
                        layout,
                        900,
                        650
                );

        scene.getStylesheets().add(
                getClass()
                        .getResource("/styles.css")
                        .toExternalForm()
        );

        repairStage.setScene(scene);

        repairStage.setOnShown(
                event -> centerStageOnParent(
                        repairStage,
                        parentStage
                )
        );

        repairStage.show();
    }

    private void loadRepairs() {

        repairList.setAll(
                EquipmentRepairDAO
                        .getRepairsByEquipmentId(
                                equipmentId
                        )
        );

        table.setItems(repairList);
    }

    private void addRepair() {

        if (!validateFields()) {
            return;
        }

        BigDecimal cost = parseCost();

        if (EquipmentRepairDAO.addRepair(
                equipmentId,
                repairDatePicker.getValue(),
                malfunctionArea.getText().trim(),
                workDoneArea.getText().trim(),
                cost
        )) {

            loadRepairs();
            clearFields();

        } else {

            showErrorAlert(
                    repairStage,
                    ERROR_TITLE,
                    "Не удалось добавить ремонт"
            );
        }
    }

    private void updateRepair() {

        EquipmentRepair selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showErrorAlert(
                    repairStage,
                    ERROR_TITLE,
                    "Выберите запись"
            );

            return;
        }

        if (!validateFields()) {
            return;
        }

        BigDecimal cost = parseCost();

        if (EquipmentRepairDAO.updateRepair(
                selected.getId(),
                repairDatePicker.getValue(),
                malfunctionArea.getText().trim(),
                workDoneArea.getText().trim(),
                cost
        )) {

            loadRepairs();
            clearFields();

        } else {

            showErrorAlert(
                    repairStage,
                    ERROR_TITLE,
                    "Не удалось обновить ремонт"
            );
        }
    }

    private void deleteRepair() {

        EquipmentRepair selected =
                table.getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            showErrorAlert(
                    repairStage,
                    ERROR_TITLE,
                    "Выберите запись"
            );

            return;
        }

        if (EquipmentRepairDAO.deleteRepair(
                selected.getId()
        )) {

            loadRepairs();
            clearFields();

        } else {

            showErrorAlert(
                    repairStage,
                    ERROR_TITLE,
                    "Не удалось удалить запись"
            );
        }
    }

    private void fillFields(
            EquipmentRepair repair
    ) {

        repairDatePicker.setValue(
                repair.getRepairDate()
        );

        malfunctionArea.setText(
                repair.getMalfunction()
        );

        workDoneArea.setText(
                repair.getWorkDone()
        );

        costField.setText(
                repair.getCost() == null
                        ? ""
                        : repair.getCost().toString()
        );
    }

    private void clearFields() {

        repairDatePicker.setValue(
                LocalDate.now()
        );

        malfunctionArea.clear();
        workDoneArea.clear();
        costField.clear();

        table.getSelectionModel()
                .clearSelection();
    }

    private boolean validateFields() {

        clearFieldStyles(costField);

        boolean valid = true;

        if (repairDatePicker.getValue() == null
                || malfunctionArea.getText().trim().isEmpty()
                || workDoneArea.getText().trim().isEmpty()) {

            showErrorAlert(
                    repairStage,
                    ERROR_TITLE,
                    "Заполните обязательные поля"
            );

            valid = false;
        }

        return valid;
    }

    private BigDecimal parseCost() {

        String value =
                costField.getText().trim();

        if (value.isEmpty()) {
            return null;
        }

        try {

            return new BigDecimal(
                    value.replace(",", ".")
            );

        } catch (Exception e) {

            showErrorAlert(
                    repairStage,
                    ERROR_TITLE,
                    "Некорректная стоимость"
            );

            return null;
        }
    }
}
