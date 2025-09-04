package org.vimal.utils;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;

import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.time.Instant;

public final class TotpUtility {
    private TotpUtility() {
    }

    private static final TimeBasedOneTimePasswordGenerator TOTP_GENERATOR = new TimeBasedOneTimePasswordGenerator();
    private static final Base32 BASE_32 = new Base32();

    public static String generateTotp(String base32Secret) throws InvalidKeyException {
        if (base32Secret == null || base32Secret.isEmpty()) {
            throw new IllegalArgumentException("Secret cannot be null or empty");
        }
        byte[] keyBytes = BASE_32.decode(base32Secret);
        return String.format("%06d", Integer.parseInt(TOTP_GENERATOR.generateOneTimePasswordString(new SecretKeySpec(keyBytes, TOTP_GENERATOR.getAlgorithm()), Instant.now())));
    }
}
