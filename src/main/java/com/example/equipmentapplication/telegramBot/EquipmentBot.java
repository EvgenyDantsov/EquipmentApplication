package com.example.equipmentapplication.telegramBot;

import com.example.equipmentapplication.DatabaseHelper;
import com.example.equipmentapplication.config.Config;
import com.example.equipmentapplication.dao.EquipmentDAO;
import com.example.equipmentapplication.dao.EquipmentHistoryDAO;
import com.example.equipmentapplication.dao.EquipmentRepairDAO;
import com.example.equipmentapplication.dao.OfficeDAO;
import com.example.equipmentapplication.dto.EquipmentRepair;
import com.example.equipmentapplication.dto.MainRecord;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EquipmentBot extends TelegramLongPollingBot {
    private final Map<Long, String> userQr = new HashMap<>();
    private final Map<Long, String> userOffice = new HashMap<>();
    private final Map<Long, String> userStatus = new HashMap<>();
    private final Map<Long, String> repairQr = new HashMap<>();
    private final Map<Long, String> repairFault = new HashMap<>();
    private final Map<Long, String> repairWork = new HashMap<>();
    private final Map<Long, String> repairCost = new HashMap<>();
    private final DefaultBotOptions options;

    public EquipmentBot(DefaultBotOptions options) {
        super(options);
        this.options = options;
    }

    @Override
    public String getBotUsername() {
        return Config.get("BOT_USERNAME"); // Укажите имя бота
    }

    @Override
    public String getBotToken() {
        return Config.get("BOT_TOKEN"); // Укажите токен вашего бота
    }

    @Override
    public void onUpdateReceived(Update update) {
        long allowedChatId = Long.parseLong(Config.get("telegram.allowed.chatId"));
        long userChatId;
        if (update.hasMessage()) {

            userChatId = update.getMessage().getChatId();

        } else if (update.hasCallbackQuery()) {

            userChatId =
                    update.getCallbackQuery()
                            .getMessage()
                            .getChatId();

        } else {
            return;
        }
        // метод, чтобы узнать айди пользователя
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText().trim();

            if ("/id".equalsIgnoreCase(text)) {
                sendMessage(userChatId, "Ваш Chat ID: " + userChatId);
                return;
            }
        }
        if (userChatId != allowedChatId) {
            sendMessage(userChatId, "⛔ У вас нет доступа");
            String username =
                    update.getMessage()
                            .getFrom()
                            .getUserName();
            String firstName = update.getMessage()
                    .getFrom()
                    .getFirstName();
            String dateTime = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            String action = "Неизвестно";

            if (update.hasMessage() && update.getMessage().hasText()) {
                action = update.getMessage().getText();
            } else if (update.hasCallbackQuery()) {
                action = "Кнопка: " +
                        update.getCallbackQuery().getData();
            }
            sendMessage(
                    allowedChatId,
                    "⚠ Попытка доступа\n" +
                            "Дата: " + dateTime + "\n" +
                            "ID: " + userChatId + "\n" +
                            "Имя: " + firstName + "\n" +
                            "Username: @" + username + "\n" +
                            "Действие: " + action
            );
            return;
        }
        try {

            // ================= CALLBACK BUTTONS =================
            if (update.hasCallbackQuery()) {

                CallbackQuery callback = update.getCallbackQuery();

                String data = callback.getData();
                Long chatId = callback.getMessage().getChatId();

                handleCallback(data, chatId);

                return;
            }

            // ================= TEXT MESSAGE =================
            if (update.hasMessage() && update.getMessage().hasText()) {

                String text = update.getMessage().getText();
                Long chatId = update.getMessage().getChatId();

                String qr;

                // если ждем кабинет
                if (userQr.containsKey(chatId) && !userOffice.containsKey(chatId)) {

                    userOffice.put(chatId, text.trim());

                    sendStatusKeyboard(chatId);
                    return;
                }
                // неисправность
                if (repairQr.containsKey(chatId)
                        && !repairFault.containsKey(chatId)) {

                    repairFault.put(chatId, text.trim());

                    sendMessage(chatId,
                            "🧾 Введите выполненные работы:");

                    return;
                }

                // работы
                if (repairFault.containsKey(chatId)
                        && !repairWork.containsKey(chatId)) {

                    repairWork.put(chatId, text.trim());

                    InlineKeyboardButton skipBtn =
                            new InlineKeyboardButton();

                    skipBtn.setText("⏭ Пропустить");
                    skipBtn.setCallbackData("SKIP_REPAIR_COST");

                    InlineKeyboardMarkup markup =
                            new InlineKeyboardMarkup();

                    markup.setKeyboard(
                            List.of(
                                    List.of(skipBtn)
                            )
                    );

                    SendMessage msg = new SendMessage();
                    msg.setChatId(chatId.toString());
                    msg.setText("💰 Введите стоимость ремонта или нажмите кнопку «Пропустить»");
                    msg.setReplyMarkup(markup);

                    executeSafe(msg);

                    return;
                }

                // стоимость
                if (repairWork.containsKey(chatId)
                        && !repairCost.containsKey(chatId)) {
                    repairCost.put(chatId, text.trim());
                    saveRepair(chatId);

                    return;
                }
                // /sn <серийник>
                if (text.startsWith("/sn")) {
                    String[] parts = text.split(" ", 2);

                    if (parts.length < 2 || parts[1].isBlank()) {
                        sendMessage(chatId, "Введите серийный номер после команды /sn");
                        return;
                    }

                    String sn = parts[1].trim();
                    String response = findEquipmentBySn(sn);

                    SendMessage message = new SendMessage();
                    message.setChatId(chatId.toString());
                    message.setText(response);
                    execute(message);
                    return;
                }
                // Если открыли через QR ссылку
                if (text.startsWith("/start")) {

                    String[] parts = text.split(" ", 2);

                    if (parts.length < 2) {

                        sendMessage(chatId,
                                "Отсканируйте QR-код оборудования");

                        return;
                    }

                    qr = parts[1];

                } else {

                    // если пользователь просто отправил qr текстом
                    qr = text.trim();
                }

                String response = findEquipmentByQr(qr);

                SendMessage message = new SendMessage();
                message.setChatId(chatId.toString());
                message.setText(response);

                // 🔥 ДОБАВЛЯЕМ КНОПКИ
                message.setReplyMarkup(createKeyboard(qr));

                execute(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleCallback(String data, Long chatId) {

        try {

            // ================= START MOVE =================
            if (data.startsWith("MOVE:")) {

                String qr = data.substring(5);
                clearUserState(chatId); // 🔥 ВАЖНО: чистим старый процесс

                userQr.put(chatId, qr);

                sendMessage(chatId, "🏢 Введите номер кабинета:");

                return;
            }

            // ================= STATUS SELECT =================
            if (data.startsWith("STATUS:")) {

                String status = data.substring(7);

                userStatus.put(chatId, status);

                applyMove(chatId);

                return;
            }

            // ================= HISTORY =================
            if (data.startsWith("HISTORY:")) {

                String qr = data.substring(8);

                sendMessage(chatId, getHistory(qr));
                return;
            }
            // ================= REPAIR HISTORY =================
            if (data.startsWith("REPAIR_HISTORY:")) {

                String qr = data.substring(15);
                sendMessage(chatId, getRepairHistory(qr));
                return;
            }

            // ================= ADD REPAIR =================
            if (data.startsWith("ADD_REPAIR:")) {

                String qr = data.substring(11);

                clearRepairState(chatId);

                repairQr.put(chatId, qr);

                sendMessage(chatId, "❗ Введите неисправность:");
                return;
            }
            if ("SKIP_REPAIR_COST".equals(data)) {

                repairCost.put(chatId, "");

                saveRepair(chatId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "⚠ Ошибка обработки");
        }
    }

    private void sendStatusKeyboard(Long chatId) {

        List<List<InlineKeyboardButton>> rows = List.of(
                List.of(button("✔ Установлен", "STATUS:active")),
                List.of(button("📦 На хранении", "STATUS:in_storage")),
                List.of(button("❌ Списан", "STATUS:written_off")),
                List.of(button("⏭ Оставить без изменений", "STATUS:KEEP"))
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText("📊 Выберите статус:");
        msg.setReplyMarkup(markup);

        executeSafe(msg);
    }

    private void applyMove(Long chatId) {

        String qr = userQr.get(chatId);
        String officeText = userOffice.get(chatId);
        String status = userStatus.get(chatId);

        if (qr == null || officeText == null || status == null) {
            sendMessage(chatId, "⚠ Недостаточно данных");
            return;
        }
//        // ================= KEEP ОБРАБОТКА =================
//        if ("KEEP".equals(status)) {
//            status = null; // не меняем статус
//        }
        try {

            int officeId = OfficeDAO.getOfficeIdByNumber(officeText);
            boolean result = EquipmentDAO.moveEquipmentByQr(
                    qr,
                    officeId,
                    "",          // note (если не используешь — пусто)
                    status
            );

            if (result) {
                sendMessage(chatId, "✅ Оборудование перемещено");
            } else {
                sendMessage(chatId, "❌ Ошибка перемещения");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "⚠ Ошибка перемещения");
        }

        clearUserState(chatId); // 🔥 очищаем после завершения
    }

    private void clearUserState(Long chatId) {
        userQr.remove(chatId);
        userOffice.remove(chatId);
        userStatus.remove(chatId);
    }

    private String findEquipmentByQr(String qr) {
        try {
            MainRecord record = DatabaseHelper.getViewByQr(qr);
            if (record == null) {
                return "❌ Оборудование не найдено по QR: " + qr;
            }

            String statusRu = mapStatus(record.getStatus());

            return "📦 Оборудование:\n\n"
                    + "🏢 Кабинет: " + record.getNameOffice() + "\n"
                    + "🏷 Номер кабинета: " + record.getNumberOffice() + "\n"
                    + "💻 Название: " + record.getNameEquipment() + "\n"
                    + "📦 Модель: " + record.getModel() + "\n"
                    + "🔢 Серийный номер: " + record.getSnNumber() + "\n"
                    + "📊 Статус: " + statusRu + "\n"
                    + "👤 МОЛ: " + record.getFio();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Ошибка при поиске оборудования";
        }
    }
    // =========================================================
    // ИСТОРИЯ
    // =========================================================

    private String getHistory(String qr) {

        try {
            Integer equipmentId =
                    DatabaseHelper.getEquipmentIdByQr(qr);
            if (equipmentId == null) {
                return "❌ Оборудование не найдено";
            }

            var historyList = EquipmentHistoryDAO.getHistoryByEquipmentId(equipmentId);

            if (historyList.isEmpty()) {
                return "📭 История отсутствует";
            }

            StringBuilder sb = new StringBuilder("📜 История изменений:\n\n");

            for (var h : historyList) {

                sb.append("📌 ")
                        .append(h.getAction())
                        .append(" | ")
                        .append(h.getChangeDate())
                        .append("\n");

                // 🔥 ВАЖНО: показываем изменения
                if (h.getDetails() != null && !h.getDetails().isEmpty()) {
                    sb.append("🧾 Изменения:\n")
                            .append(h.getDetails())
                            .append("\n");
                }

                sb.append("🏢 Кабинет: ")
                        .append(h.getOfficeName())
                        .append(" (")
                        .append(h.getOfficeNumber())
                        .append(")\n")
                        .append("👤 МОЛ: ")
                        .append(h.getResponsibleFio())
                        .append("\n\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Ошибка получения истории";
        }
    }

    // =========================================================
    // ОТПРАВКА СООБЩЕНИЙ
    // =========================================================

    private void sendMessage(Long chatId, String text) {

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String mapStatus(String status) {
        return switch (status) {
            case "active" -> "Установлен";
            case "in_storage" -> "На хранении";
            case "written_off" -> "Списан";
            default -> status;
        };
    }

    private InlineKeyboardButton button(String text, String data) {

        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(data);

        return b;
    }

    private InlineKeyboardMarkup createKeyboard(String qr) {

        InlineKeyboardButton moveBtn = new InlineKeyboardButton();
        moveBtn.setText("🔄 Переместить");
        moveBtn.setCallbackData("MOVE:" + qr);

        InlineKeyboardButton historyBtn = new InlineKeyboardButton();
        historyBtn.setText("📜 История");
        historyBtn.setCallbackData("HISTORY:" + qr);

        InlineKeyboardButton repairHistoryBtn = new InlineKeyboardButton();
        repairHistoryBtn.setText("🛠 История ремонтов");
        repairHistoryBtn.setCallbackData("REPAIR_HISTORY:" + qr);

        InlineKeyboardButton addRepairBtn = new InlineKeyboardButton();
        addRepairBtn.setText("➕ Добавить ремонт");
        addRepairBtn.setCallbackData("ADD_REPAIR:" + qr);

        List<InlineKeyboardButton> row1 =
                Arrays.asList(moveBtn, historyBtn);
        List<InlineKeyboardButton> row2 = Arrays.asList(repairHistoryBtn, addRepairBtn);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(List.of(row1, row2));

        return markup;
    }

    private void executeSafe(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String findEquipmentBySn(String sn) {
        try {
            MainRecord record = DatabaseHelper.getViewBySn(sn);

            if (record == null) {
                return "❌ Оборудование не найдено по серийному номеру: " + sn;
            }

            String statusRu = mapStatus(record.getStatus());

            return "📦 Оборудование:\n\n"
                    + "🏢 Кабинет: " + record.getNameOffice() + "\n"
                    + "🏷 Номер кабинета: " + record.getNumberOffice() + "\n"
                    + "💻 Название: " + record.getNameEquipment() + "\n"
                    + "📦 Модель: " + record.getModel() + "\n"
                    + "🔢 Серийный номер: " + record.getSnNumber() + "\n"
                    + "📊 Статус: " + statusRu + "\n"
                    + "👤 МОЛ: " + record.getFio();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Ошибка при поиске оборудования";
        }
    }

    private void saveRepair(Long chatId) {

        String qr = repairQr.get(chatId);
        String fault = repairFault.get(chatId);
        String work = repairWork.get(chatId);
        String costText = repairCost.get(chatId);

        if (qr == null
                || fault == null
                || work == null) {

            sendMessage(chatId,
                    "⚠ Недостаточно данных");

            return;
        }

        try {
            Integer equipmentId = DatabaseHelper.getEquipmentIdByQr(qr);

            if (equipmentId == null) {
                sendMessage(chatId, "❌ Оборудование не найдено");
                return;
            }
            BigDecimal cost = null;

            if (costText != null && !costText.isBlank()) {
                cost = new BigDecimal(costText.replace(",", "."));
            }

            boolean result =
                    EquipmentRepairDAO.addRepair(
                            equipmentId,
                            LocalDate.now(),
                            fault,
                            work,
                            cost
                    );

            if (result) {
                sendMessage(chatId, "✅ Ремонт добавлен");
            } else {
                sendMessage(chatId, "❌ Ошибка добавления ремонта");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(chatId, "⚠ Ошибка сохранения");
        }
        clearRepairState(chatId);
    }

    private String getRepairHistory(String qr) {

        try {
            Integer equipmentId =
                    DatabaseHelper.getEquipmentIdByQr(qr);

            if (equipmentId == null) {
                return "❌ Оборудование не найдено";
            }

            var list = EquipmentRepairDAO.getRepairsByEquipmentId(equipmentId);

            if (list.isEmpty()) {
                return "📭 Ремонтов нет";
            }

            StringBuilder sb = new StringBuilder("🛠 История ремонтов:\n\n");

            for (var r : list) {

                sb.append("📅 Дата: ")
                        .append(r.getRepairDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                        .append("\n")
                        .append("❗ Неисправность: ")
                        .append(r.getMalfunction())
                        .append("\n")
                        .append("🧾 Выполненные работы: ")
                        .append(r.getWorkDone())
                        .append("\n");

                if (r.getCost() != null) {
                    sb.append("💰 Стоимость: ")
                            .append(r.getCost())
                            .append("\n");
                }

                sb.append("----------------\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Ошибка получения истории ремонтов";
        }
    }

    private void clearRepairState(Long chatId) {
        repairQr.remove(chatId);
        repairFault.remove(chatId);
        repairWork.remove(chatId);
        repairCost.remove(chatId);
    }
}