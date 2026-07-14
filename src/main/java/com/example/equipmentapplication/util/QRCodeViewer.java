package com.example.equipmentapplication.util;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class QRCodeViewer {
    public static void show(String qrText) {
        if (qrText == null || qrText.isEmpty()) return;

        try {
            Image qrImage = QRCodeGenerator.generateQRCodeImage("https://t.me/testNewRSbot?start="+ qrText);

            ImageView imageView = new ImageView(qrImage);
            imageView.setFitWidth(250);
            imageView.setFitHeight(250);
            imageView.setPreserveRatio(true);

            Stage stage = new Stage();
            stage.setTitle("QR-код");
            stage.setScene(new Scene(new StackPane(imageView), 300, 300));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
