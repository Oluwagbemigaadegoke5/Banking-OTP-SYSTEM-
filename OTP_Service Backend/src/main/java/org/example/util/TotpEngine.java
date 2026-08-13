package org.example.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Base32;

public class TotpEngine {
    private static final int TIME_STEP = 30;
    private static final int DIGITS = 6;

    public static String generateTOTP(String base32Secret, long timeIndex) {
        Base32 base32 = new Base32();
        byte[] key = base32.decode(base32Secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeIndex).array();

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            int truncatedHash = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);

            int pinValue = truncatedHash % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", pinValue);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Cryptographic calculation failure", e);
        }
    }

    public static boolean verifyCode(String base32Secret, String code) {
        long currentWindow = (System.currentTimeMillis() / 1000) / TIME_STEP;
        // Allows a tolerance of 1 time-step backward or forward to neutralize clock drift
        for (int i = -1; i <= 1; i++) {
            if (generateTOTP(base32Secret, currentWindow + i).equals(code)) {
                return true;
            }
        }
        return false;
    }
}