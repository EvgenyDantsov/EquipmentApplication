package com.example.equipmentapplication.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRCodeGenerator {
    public static Image generateQRCodeImage(String text) throws Exception {

        QRCodeWriter writer = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix matrix = writer.encode(
                text,
                BarcodeFormat.QR_CODE,
                250,
                250,
                hints
        );

        int width = matrix.getWidth();
        int height = matrix.getHeight();

        WritableImage image = new WritableImage(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.getPixelWriter().setColor(
                        x,
                        y,
                        matrix.get(x, y)
                                ? javafx.scene.paint.Color.BLACK
                                : javafx.scene.paint.Color.WHITE
                );
            }
        }
        return image;
    }
    public static byte[] generateQRCodeBytes(String text) throws Exception {

        QRCodeWriter writer = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        BitMatrix matrix = writer.encode(
                text,
                BarcodeFormat.QR_CODE,
                250,
                250,
                hints
        );

        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        return baos.toByteArray();
    }
}