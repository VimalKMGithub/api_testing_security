package org.vimal.utils;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;

public final class QrUtility {
    private QrUtility() {
    }

    private static final ThreadLocal<MultiFormatReader> MULTI_FORMAT_READER = ThreadLocal.withInitial(MultiFormatReader::new);
    private static final Map<DecodeHintType, Object> HINTS = buildHints();

    private static Map<DecodeHintType, Object> buildHints() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        return hints;
    }

    public static String extractSecretFromByteArrayOfQrCode(byte[] byteArrayOfQrCode) throws IOException, NotFoundException {
        String totpUrl = decodeByteArrayOfQrCode(byteArrayOfQrCode);
        if (totpUrl == null || !totpUrl.startsWith("otpauth://totp/") || !totpUrl.contains("secret=")) {
            throw new IllegalArgumentException("Invalid Totp URL format");
        }
        int queryStart = totpUrl.indexOf('?');
        if (queryStart == -1 || queryStart == totpUrl.length() - 1) {
            throw new IllegalArgumentException("No query parameters found in Totp URL");
        }
        String[] params = totpUrl.substring(queryStart + 1).split("&");
        for (String param : params) {
            if (param.startsWith("secret=")) {
                return URLDecoder.decode(param.substring(7), StandardCharsets.UTF_8);
            }
        }
        throw new IllegalArgumentException("No secret parameter found in Totp Url");
    }

    private static String decodeByteArrayOfQrCode(byte[] byteArrayOfQrCode) throws IOException, NotFoundException {
        if (byteArrayOfQrCode == null || byteArrayOfQrCode.length == 0) {
            throw new IllegalArgumentException("Byte array cannot be null or empty");
        }
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(byteArrayOfQrCode));
        if (bufferedImage == null) {
            throw new IllegalArgumentException("Invalid Qr code image data");
        }
        MultiFormatReader reader = MULTI_FORMAT_READER.get();
        try {
            return reader.decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage))), HINTS).getText();
        } finally {
            reader.reset();
        }
    }
}
