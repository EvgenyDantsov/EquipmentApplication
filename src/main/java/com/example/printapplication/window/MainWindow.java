package com.example.printapplication.window;

import com.example.printapplication.DatabaseHelper;
import com.example.printapplication.dao.OfficeDAO;
import com.example.printapplication.dao.PrinterDAO;
import com.example.printapplication.dto.MainRecord;
import com.example.printapplication.dto.Office;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.printapplication.util.AlertUtils.showErrorAlert;
import static com.example.printapplication.util.AlertUtils.showInformationAlert;
import static com.example.printapplication.util.WindowUtils.*;

public class MainWindow {
    private TableView<MainRecord> table;
    private ObservableList<MainRecord> allDataList = FXCollections.observableArrayList();
    private Stage mainStage;
    private TextField officeNameFilter;
    private TextField numberOfficeFilter;
    private TextField departmentFilter;
    private TextField printerFilter;
    private TextField modelFilter;
    private TextField snFilter;
    private TextField noteFilter;
    private TextField statusFilter;
    private TextField fioFilter;
    private FilteredList<MainRecord> filteredData;
    private Label totalPrintersLabel;
    private Label totalDepartmentsLabel;
    private Label totalResponsiblesLabel;
    private Label totalFilteredLabel;
    private Label lastUpdateLabel;
    private Label totalOfficesLabel;
    private ToolBar statusBar;

    public void start(Stage mainStage, Stage primaryStage) {
        this.mainStage = mainStage;
        mainStage.setTitle("Главное окно");
        // Инициализация UI компонентов
        initializeUI();

        // Уже загруженные данные просто отображаем
        //table.setItems(allDataList);
        setupFiltering();
        setupContextMenu();

        mainStage.setOnShown(event -> centerStageOnParent(mainStage, primaryStage));
    }

    private void initializeUI() {
        // Создание кнопок для работы с таблицами
        Button departmentButton = new Button("Отделение");
        departmentButton.setOnAction(e -> openDepartmentWindow());
        Button printerButton = new Button("Принтер");
        printerButton.setOnAction(e -> openPrintWindow());
        Button headButton = new Button("Старшая отделения");
        headButton.setOnAction(e -> openSeniorDepartmentWindow());
        Button floorButton = new Button("Номер кабинета");
        floorButton.setOnAction(e -> openOfficeWindow());
        Button reportButton = new Button("Сформировать отчет");
        reportButton.setOnAction(e -> generateReport());
        Button closeButton = new Button("Выход");
        closeButton.getStyleClass().add("exit-button");
        closeButton.setOnAction(e -> mainStage.close());
        table = new TableView<>();
        // Создание колонок таблицы
        TableColumn<MainRecord, String> officeNameColumn = new TableColumn<>("Название кабинета");
        officeNameColumn.setCellValueFactory(new PropertyValueFactory<>("nameOffice"));
        TableColumn<MainRecord, String> numberOfficeColumn = new TableColumn<>("Кабинет");
        numberOfficeColumn.setCellValueFactory(new PropertyValueFactory<>("numberOffice"));
        TableColumn<MainRecord, String> departmentColumn = new TableColumn<>("Отделение");
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("nameDepartment"));
        TableColumn<MainRecord, String> printerColumn = new TableColumn<>("Принтер");
        printerColumn.setStyle("-fx-alignment: CENTER;");
        printerColumn.setCellValueFactory(new PropertyValueFactory<>("namePrinter"));
        TableColumn<MainRecord, String> modelColumn = new TableColumn<>("Модель");
        modelColumn.setStyle("-fx-alignment: CENTER;");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        TableColumn<MainRecord, String> snColumn = new TableColumn<>("Серийный номер");
        snColumn.setStyle("-fx-alignment: CENTER;");
        snColumn.setCellValueFactory(new PropertyValueFactory<>("snNumber"));
        TableColumn<MainRecord, String> noteColumn = new TableColumn<>("Заметки");
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        TableColumn<MainRecord, String> statusColumn = new TableColumn<>("Cтатус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(createStatusTableCellFactory());
        TableColumn<MainRecord, String> fioColumn = new TableColumn<>("Старшая отделения");
        fioColumn.setCellValueFactory(new PropertyValueFactory<>("fio"));
        Collections.addAll(table.getColumns(), numberOfficeColumn, officeNameColumn, printerColumn, modelColumn, snColumn, departmentColumn, noteColumn, statusColumn, fioColumn);
        loadAllData();
        filteredData = new FilteredList<>(allDataList, p -> true);
        table.setItems(filteredData);
        // Размещение кнопок в верхней части окна (горизонтально)
        HBox topButtons = new HBox(20, departmentButton, printerButton, headButton, floorButton, reportButton);
        topButtons.setAlignment(Pos.CENTER);
        topButtons.setPadding(new Insets(10));
        topButtons.getStyleClass().add("top-buttons");

        // Размещение кнопки выхода внизу окна
        HBox bottomExitButton = new HBox();
        bottomExitButton.getChildren().add(closeButton);
        bottomExitButton.setAlignment(Pos.BOTTOM_RIGHT); // Выравнивание по правому нижнему углу
        bottomExitButton.setPadding(new Insets(10));
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setPadding(new Insets(5));
        filterBox.getStyleClass().add("filter-box");

        officeNameFilter = createFilterField("Название кабинета...");
        numberOfficeFilter = createFilterField("Кабинет...");
        departmentFilter = createFilterField("Отделение...");
        printerFilter = createFilterField("Принтер...");
        modelFilter = createFilterField("Модель...");
        snFilter = createFilterField("Серийный номер...");
        noteFilter = createFilterField("Заметки...");
        statusFilter = createFilterField("Статус...");
        fioFilter = createFilterField("Старшая отделения...");

        // Добавляем все поля в HBox
        filterBox.getChildren().addAll(numberOfficeFilter, officeNameFilter, printerFilter, modelFilter, snFilter, departmentFilter, noteFilter, statusFilter, fioFilter);
        statusBar = createStatusBar();
        VBox mainLayout = new VBox(10, topButtons, table, filterBox, bottomExitButton, statusBar);
        VBox.setVgrow(table, Priority.ALWAYS); // Таблица будет растягиваться
        Scene mainScene = new Scene(mainLayout, 1200, 600);
        URL stylesheetUrl = getClass().getResource("/styles.css");
        mainScene.getStylesheets().add(stylesheetUrl != null ? stylesheetUrl.toExternalForm() : "");
        //mainStage.setOnShown(event -> centerStageOnParent(mainStage, primaryStage));
        setupFiltering();
        setupContextMenu();
        mainStage.setScene(mainScene);
    }

    private TextField createFilterField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.setMaxWidth(130); // Устанавливаем ширину
        textField.getStyleClass().add("filter-field");
        return textField;
    }

    private ToolBar createStatusBar() {
        statusBar = new ToolBar();
        statusBar.getStyleClass().add("status-bar");
        // Создаем иконки или decorative элементы
        Label printerIcon = createIconLabel("\uD83D\uDCE0", "Принтеры: 0");
        Label departmentIcon = createIconLabel("\uD83C\uDFDB", "Отделения: 0");
        Label responsibleIcon = createIconLabel("\uD83D\uDC68\u200D\uD83D\uDCBC", "Ответственные: 0");
        Label officeIcon = createIconLabel("\uD83D\uDEAA", "Кабинеты: 0");
        totalFilteredLabel = new Label("📋 0/0");
        lastUpdateLabel = new Label("🕐 --:--");
        totalPrintersLabel = printerIcon;
        totalDepartmentsLabel = departmentIcon;
        totalResponsiblesLabel = responsibleIcon;
        totalOfficesLabel = officeIcon;

        // Стилизуем метки
        totalPrintersLabel.getStyleClass().add("status-item");
        totalDepartmentsLabel.getStyleClass().add("status-item");
        totalResponsiblesLabel.getStyleClass().add("status-item");
        totalOfficesLabel.getStyleClass().add("status-item");
        totalFilteredLabel.getStyleClass().add("status-item");
        lastUpdateLabel.getStyleClass().add("status-item-right");

        // Добавляем разделители с кастомными стилями
        Separator[] separators = new Separator[5];
        for (int i = 0; i < separators.length; i++) {
            separators[i] = createStyledSeparator();
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusBar.getItems().addAll(
                totalFilteredLabel, separators[0], // Самый важный - первым!
                totalPrintersLabel, separators[1],
                totalDepartmentsLabel, separators[2],
                totalResponsiblesLabel, separators[3],
                totalOfficesLabel, separators[4],
                spacer,
                lastUpdateLabel
        );

        return statusBar;
    }

    private Label createIconLabel(String icon, String text) {
        Label label = new Label(icon + " " + text);
        label.setContentDisplay(ContentDisplay.LEFT);
        label.setGraphicTextGap(5);
        return label;
    }

    private Separator createStyledSeparator() {
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.getStyleClass().add("status-separator");
        return separator;
    }

    private void updateStatusBar() {
        if (filteredData == null) {
            return;
        }

        // Обновляем статистику
        int printerCount = filteredData.size();
        long departmentCount = filteredData.stream()
                .map(MainRecord::getNameDepartment)
                .distinct()
                .count();
        long responsibleCount = filteredData.stream()
                .map(MainRecord::getFio)
                .filter(fio -> fio != null && !fio.trim().isEmpty())
                .distinct()
                .count();
        long officeCount = filteredData.stream()
                .map(MainRecord::getNumberOffice)
                .distinct()
                .count();
        int totalCount = allDataList.size();
        int filteredCount = filteredData.size();
        // Обновляем текст
        totalPrintersLabel.setText("\uD83D\uDCE0 Принтеры: " + printerCount);
        totalDepartmentsLabel.setText("\uD83C\uDFDB Отделения: " + departmentCount);
        totalResponsiblesLabel.setText("\uD83D\uDC68\u200D\uD83D\uDCBC Ответственные: " + responsibleCount);
        totalOfficesLabel.setText("\uD83D\uDEAA Кабинеты: " + officeCount);
        totalFilteredLabel.setText("📋 " + filteredCount + "/" + totalCount);
        lastUpdateLabel.setText("🕐 " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem movePrinterItem = new MenuItem("Переместить принтер");
        movePrinterItem.setOnAction(e -> movePrinter());

        contextMenu.getItems().add(movePrinterItem);

        table.setRowFactory(tv -> {
            TableRow<MainRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY && !row.isEmpty()) {
                    contextMenu.show(table, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }

    private void movePrinter() {
        MainRecord selectedRecord = table.getSelectionModel().getSelectedItem();
        if (selectedRecord == null) {
            showErrorAlert(mainStage, "Ошибка", "Выберите принтер для перемещения");
            return;
        }

        // Получаем список всех кабинетов и проверяем на null
        ObservableList<Office> offices = FXCollections.observableArrayList();
        List<Office> allOffices = OfficeDAO.getAllOffice();
        if (allOffices != null) {
            offices.addAll(allOffices);
        }

        // Создаем диалоговое окно для выбора нового кабинета
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Перемещение принтера");
        dialog.setHeaderText("Перемещение принтера: " + selectedRecord.getNamePrinter() +
                "\nТекущий кабинет: " + selectedRecord.getNumberOffice());

        ButtonType moveButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(moveButtonType, ButtonType.CANCEL);

        ComboBox<Office> officeCombo = new ComboBox<>(offices);
        officeCombo.setConverter(new StringConverter<Office>() {
            @Override
            public String toString(Office office) {
                // Добавляем проверку на null
                return office == null ? "" : office.getNumberOffice() + " (" + office.getNameOffice() + ")";
            }

            @Override
            public Office fromString(String string) {
                return null;
            }
        });

        // Устанавливаем обработчик для предотвращения выбора null значения
        officeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                officeCombo.setValue(oldVal);
            }
        });
        TextField noteField = new TextField();
        noteField.setPromptText("Введите примечание");
        // Устанавливаем текущее примечание из выбранного принтера
        noteField.setText(selectedRecord.getNote() != null ? selectedRecord.getNote() : "");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Новый кабинет:"), 0, 0);
        grid.add(officeCombo, 1, 0);
        grid.add(new Label("Примечание:"), 0, 1);
        grid.add(noteField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(officeCombo::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == moveButtonType) {
                // Формируем обновленное примечание
                String updatedNote = noteField.getText();
                if (!updatedNote.isEmpty()) {
                    updatedNote += "\n";
                }
                // Возвращаем результат операции
                return PrinterDAO.movePrinter(selectedRecord.getPrinterId(),
                        officeCombo.getValue().getId(),
                        updatedNote);
            }
            return false;
        });
        Optional<Boolean> result = dialog.showAndWait();

        result.ifPresent(success -> {
            if (success) {
                showInformationAlert(mainStage, "Успех", "Принтер успешно перемещен");
                loadAllData(); // Явно обновляем данные
                resetFilters();
                setupFiltering();
                updateStatusBar();
            } else {
                showErrorAlert(mainStage, "Ошибка", "Не удалось переместить принтер");
            }
        });

    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(allDataList, p -> true);

        List<TextField> filters = Arrays.asList(
                numberOfficeFilter, officeNameFilter, printerFilter,
                modelFilter, snFilter, departmentFilter, noteFilter, statusFilter, fioFilter
        );

        ChangeListener<String> filterListener = (observable, oldValue, newValue) -> {
            filteredData.setPredicate(record ->
                    (numberOfficeFilter.getText().isEmpty() || record.getNumberOffice().toLowerCase().contains(numberOfficeFilter.getText().toLowerCase())) &&
                            (officeNameFilter.getText().isEmpty() || record.getNameOffice().toLowerCase().contains(officeNameFilter.getText().toLowerCase())) &&
                            (printerFilter.getText().isEmpty() || record.getNamePrinter().toLowerCase().contains(printerFilter.getText().toLowerCase())) &&
                            (modelFilter.getText().isEmpty() || record.getModel().toLowerCase().contains(modelFilter.getText().toLowerCase())) &&
                            (snFilter.getText().isEmpty() || record.getSnNumber().toLowerCase().contains(snFilter.getText().toLowerCase())) &&
                            (departmentFilter.getText().isEmpty() || record.getNameDepartment().toLowerCase().contains(departmentFilter.getText().toLowerCase())) &&
                            (noteFilter.getText().isEmpty() || record.getNote().toLowerCase().contains(noteFilter.getText().toLowerCase())) &&
                            (statusFilter.getText().isEmpty() || getStatusDisplayName(record.getStatus()).toLowerCase().contains(statusFilter.getText().toLowerCase())) &&
                            (fioFilter.getText().isEmpty() || record.getFio().toLowerCase().contains(fioFilter.getText().toLowerCase()))
            );
            updateStatusBar();
        };

        for (TextField filter : filters) {
            filter.textProperty().addListener(filterListener);
        }

        table.setItems(filteredData);
        updateStatusBar();
    }

    private void resetFilters() {
        numberOfficeFilter.setText("");
        officeNameFilter.setText("");
        printerFilter.setText("");
        modelFilter.setText("");
        snFilter.setText("");
        departmentFilter.setText("");
        noteFilter.setText("");
        statusFilter.setText("");
        fioFilter.setText("");
    }

    private void openDepartmentWindow() {
        // Получаем текущее окно (родительское окно)
        Stage parentStage = (Stage) table.getScene().getWindow(); // Используем table как узел текущего окна
        // Создаем новое окно
        Stage departmentStage = new Stage();
        // Открываем новое окно, передавая родительское окно
        new DepartmentWindow().start(departmentStage, parentStage);
        // Обновляем таблицу после закрытия окна
        departmentStage.setOnHidden(event -> {
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void openPrintWindow() {
        // Получаем текущее окно (родительское окно)
        Stage parentStage = (Stage) table.getScene().getWindow(); // Используем table как узел текущего окна
        // Создаем новое окно
        Stage printStage = new Stage();
        // Открываем новое окно, передавая родительское окно
        new PrinterWindow().start(printStage, parentStage);
        // Обновляем таблицу после закрытия окна
        printStage.setOnHidden(event -> {
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void openOfficeWindow() {
        // Получаем текущее окно (родительское окно)
        Stage parentStage = (Stage) table.getScene().getWindow(); // Используем table как узел текущего окна
        // Создаем новое окно
        Stage officeStage = new Stage();
        // Открываем новое окно, передавая родительское окно
        new OfficeWindow().start(officeStage, parentStage);
        // Обновляем таблицу после закрытия окна
        officeStage.setOnHidden(event -> {
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void openSeniorDepartmentWindow() {
        // Получаем текущее окно (родительское окно)
        Stage parentStage = (Stage) table.getScene().getWindow(); // Используем table как узел текущего окна
        // Создаем новое окно
        Stage seniorDepartmentStage = new Stage();
        // Открываем новое окно, передавая родительское окно
        new SeniorDepartmentWindow().start(seniorDepartmentStage, parentStage);
        // Обновляем таблицу после закрытия окна
        seniorDepartmentStage.setOnHidden(event -> {
            loadAllData();
            setupFiltering();
            updateStatusBar();
        });
    }

    private void loadAllData() {
        allDataList.setAll(DatabaseHelper.getAllView());
        table.setItems(allDataList);
        updateStatusBar();
    }

    private void generateReport() {
        Stage parentStage = (Stage) table.getScene().getWindow();
        // Создаем новый workbook и лист
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Отчет");
        // Создаем заголовки столбцов
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < table.getColumns().size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(table.getColumns().get(i).getText());
        }
        // Заполняем данными из таблицы
        for (int i = 0; i < filteredData.size(); i++) {
            Row row = sheet.createRow(i + 1);
            MainRecord record = filteredData.get(i);
            row.createCell(0).setCellValue(record.getNumberOffice());
            row.createCell(1).setCellValue(record.getNameOffice());
            row.createCell(2).setCellValue(record.getNamePrinter());
            row.createCell(3).setCellValue(record.getModel());
            row.createCell(4).setCellValue(record.getSnNumber());
            row.createCell(5).setCellValue(record.getNameDepartment());
            row.createCell(6).setCellValue(record.getNote());
            String statusInRussian=getStatusDisplayName(record.getStatus());
            row.createCell(7).setCellValue(statusInRussian);
            row.createCell(8).setCellValue(record.getFio());
        }
        // Авторазмер для всех столбцов
        for (int i = 0; i < table.getColumns().size(); i++) {
            sheet.autoSizeColumn(i);
            // Увеличиваем ширину столбца на 2 символа для корректного отображения
            int currentWidth = sheet.getColumnWidth(i);
            int newWidth = currentWidth + 2 * 256; // 1 символ = 256 единиц 
            sheet.setColumnWidth(i, newWidth);
        }
        // Создаем таблицу в Excel
        CellRangeAddress range = new CellRangeAddress(0, allDataList.size(), 0, table.getColumns().size() - 1);
        sheet.setAutoFilter(range);
        // Формируем имя файла с текущей датой
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String fileName = "Отчет_" + LocalDate.now().format(formatter) + ".xlsx";
        // Сохраняем файл
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить отчет");
        fileChooser.setInitialFileName(fileName); // Устанавливаем имя файла
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(table.getScene().getWindow());

        if (file != null) {
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                workbook.close();
                showInformationAlert(parentStage, "Отчет сформирован", "Отчет успешно сохранен!");
            } catch (IOException e) {
                e.printStackTrace();
                showErrorAlert(parentStage, "Ошибка", "Не удалось сохранить отчет.");
            }
        }
    }
}