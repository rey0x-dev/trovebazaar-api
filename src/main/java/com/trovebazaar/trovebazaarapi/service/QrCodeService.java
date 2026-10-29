package com.trovebazaar.trovebazaarapi.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;

@Service
public class QrCodeService {

    public BufferedImage generateQrCodeImage(String qrCode) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix bitMatrix = qrCodeWriter.encode(
                qrCode,
                BarcodeFormat.QR_CODE,
                300,
                300
        );

        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
