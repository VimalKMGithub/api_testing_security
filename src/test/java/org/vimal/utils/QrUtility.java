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
        int queryStart = totpUrl.indexOf('?');
        for (String param : totpUrl.substring(queryStart + 1)
                .split("&")) {
            if (param.startsWith("secret=")) {
                return param.substring(7);
            }
        }
        throw new RuntimeException("No secret parameter found in Totp Url");
    }

    private static String decodeByteArrayOfQrCode(byte[] byteArrayOfQrCode) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(byteArrayOfQrCode));
        MultiFormatReader reader = MULTI_FORMAT_READER.get();
        try {
            return reader.decode(
                            new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(bufferedImage))),
                            HINTS
                    )
                    .getText();
        } finally {
            reader.reset();
        }
    }
}
