package com.example.equipmentapplication.telegramBot;

import com.example.equipmentapplication.config.Config;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.bots.DefaultBotOptions;

public class BotStarter {

    private static BotSession botSession;
    public static void start() {

        try {

            // 1. Proxy настройки ЯВНО
            DefaultBotOptions options = new DefaultBotOptions();

            options.setProxyHost(Config.get("proxy.host"));
            options.setProxyPort(Integer.parseInt(Config.get("proxy.port")));

            // важно: тип proxy
            options.setProxyType(DefaultBotOptions.ProxyType.HTTP);

            // 2. Создаём бота с options
            EquipmentBot bot = new EquipmentBot(options);

            // 3. Запускаем через API
            TelegramBotsApi botsApi =
                    new TelegramBotsApi(DefaultBotSession.class);

            botSession = botsApi.registerBot(bot);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void stop() {

        try {

            if (botSession != null) {

                Thread stopThread = new Thread(() -> {

                    try {
                        botSession.stop();
                        System.out.println("Telegram bot stopped");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

                stopThread.setDaemon(true);
                stopThread.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    private static void testInternetConnection() {
//        try {
//            URL url = new URL("https://api.telegram.org/bot"+Config.get("BOT_TOKEN")+"/getMe");
//
//            HttpURLConnection connection =
//                    (HttpURLConnection) url.openConnection();
//
//            connection.setRequestMethod("GET");
//
//            connection.setConnectTimeout(5000);
//            connection.setReadTimeout(5000);
//
//            int responseCode = connection.getResponseCode();
//
//            System.out.println("Ответ от сервера: " + responseCode);
//            System.out.println("Сообщение: " + connection.getResponseMessage());
//
//        } catch (Exception e) {
//            System.out.println("Ошибка подключения:");
//            e.printStackTrace();
//        }
//    }
}