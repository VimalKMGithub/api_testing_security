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
    private static final ThreadLocal<Base32> BASE_32 = ThreadLocal.withInitial(Base32::new);

    public static String generateTotp(String base32Secret) throws InvalidKeyException {
        return TOTP_GENERATOR.generateOneTimePasswordString(
                new SecretKeySpec(
                        BASE_32.get()
                                .decode(base32Secret),
                        TOTP_GENERATOR.getAlgorithm()
                ),
                Instant.now()
        );
    }
}
