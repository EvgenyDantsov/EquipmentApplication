package com.example.equipmentapplication.window;

import com.example.equipmentapplication.dao.EquipmentHistoryDAO;
import com.example.equipmentapplication.dto.EquipmentHistory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Collections;

import static com.example.equipmentapplication.util.AlertUtils.showErrorAlert;
import static com.example.equipmentapplication.util.WindowUtils.*;

public class EquipmentHistoryWindow {
    private TableView<EquipmentHistory> table;
    private ObservableList<EquipmentHistory> historyList;
    private Stage historyStage;
    private int equipmentId;
    private static final String WINDOW_TITLE = "История оборудования";
    private static final String ERROR_TITLE = "Ошибка";
    private static final String SELECT_HISTORY_TO_DELETE = "Выберите запись для удаления";
    private static final String ERROR_HISTORY_TO_DELETE = "Ошибка удаления истории";

    public EquipmentHistoryWindow(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public void start(Stage historyStage, Stage parentStage) {
        this.historyStage = historyStage;
        historyStage.setTitle(WINDOW_TITLE);

        // Таблица
        table = new TableView<>();
        historyList = FXCollections.observableArrayList();
        TableColumn<EquipmentHistory, Integer> officeIdColumn = new TableColumn<>("Кабинета");
        officeIdColumn.setCellValueFactory(new PropertyValueFactory<>("officeNumber"));
        officeIdColumn.setPrefWidth(120);
        TableColumn<EquipmentHistory, String> officeNameColumn = new TableColumn<>("Название кабинета");
        officeNameColumn.setCellValueFactory(new PropertyValueFactory<>("officeName"));
        officeNameColumn.setPrefWidth(220);
        TableColumn<EquipmentHistory, String> nameColumn = new TableColumn<>("Оборудование");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentName"));
        nameColumn.setStyle("-fx-alignment: CENTER;");
        TableColumn<EquipmentHistory, String> modelColumn = new TableColumn<>("Модель");
        modelColumn.setStyle("-fx-alignment: CENTER;");
        modelColumn.setPrefWidth(75);
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("equipmentModel"));
        TableColumn<EquipmentHistory, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(createStatusTableCellFactory());
        TableColumn<EquipmentHistory, String> dateColumn = new TableColumn<>("Дата");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedChangeDate"));
        TableColumn<EquipmentHistory, String> fioColumn = new TableColumn<>("Старшая отделения");
        fioColumn.setCellValueFactory(new PropertyValueFactory<>("responsibleFio"));
        TableColumn<EquipmentHistory, String> departmentColumn = new TableColumn<>("Название отделения");
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        departmentColumn.setPrefWidth(200);
        TableColumn<EquipmentHistory, String> actionColumn = new TableColumn<>("Действие");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        TableColumn<EquipmentHistory, String> detailsColumn = new TableColumn<>("Подробности");
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsColumn.setPrefWidth(350);
        detailsColumn.setCellFactory(tc -> new TableCell<>() {
            private final Text text = new Text();

            {
                text.wrappingWidthProperty().bind(tc.widthProperty().subtract(10));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    text.setText(item);
                    setGraphic(text);
                    // Автоматически увеличиваем высоту строки
                    this.setPrefHeight(Control.USE_COMPUTED_SIZE);
                }
            }
        });
        Collections.addAll(table.getColumns(), officeIdColumn, officeNameColumn, nameColumn, modelColumn,
                statusColumn, dateColumn, fioColumn, departmentColumn, actionColumn, detailsColumn);
        //table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Загружаем историю
        loadHistory();
        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> deleteHistoryRecord());
        HBox buttonDelete = new HBox(10, deleteButton);
        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> historyStage.close());
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox layout = new VBox(10, table, buttonDelete, buttonBox);
        layout.setPadding(new Insets(10));
        Scene scene = new Scene(layout, 1200, 400);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        historyStage.setScene(scene);

        historyStage.setOnShown(event -> centerStageOnParent(historyStage, parentStage));
        historyStage.show();
    }

    private void loadHistory() {
        historyList.setAll(EquipmentHistoryDAO.getHistoryByEquipmentId(equipmentId));
        table.setItems(historyList);
    }

    private void deleteHistoryRecord() {

        EquipmentHistory selected = table.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showErrorAlert(historyStage, ERROR_TITLE, SELECT_HISTORY_TO_DELETE);
            return;
        }

        boolean success = EquipmentHistoryDAO.deleteHistoryById(selected.getId());

        if (success) {
            loadHistory(); // обновляем таблицу
        } else {
            showErrorAlert(historyStage, ERROR_TITLE, ERROR_HISTORY_TO_DELETE);
        }
    }
}
