package org.example.service;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

@Service
public class TotpEngineService {

    private static final int TIME_WINDOW_SECONDS = 30;
    private static final int CODE_LENGTH = 6;
    private static final String CRYPTO_ALGORITHM = "HmacSHA1";

    public String generateSharedSecret() {
        byte[] buffer = new byte[20];
        new SecureRandom().nextBytes(buffer);
        return new Base32().encodeToString(buffer);
    }

    public String generateQrCodeUri(String secret, String username) {
        return String.format("otpauth://totp/SecureVaultBank:%s?secret=%s&issuer=SecureVaultBank", username, secret);
    }

    public boolean verifyToken(String secret, int inputCode) {
        long currentWindow = System.currentTimeMillis() / 1000 / TIME_WINDOW_SECONDS;
        // Check current, previous, and next windows to guard against network clock drift
        for (int i = -1; i <= 1; i++) {
            if (calculateCodeForWindow(secret, currentWindow + i) == inputCode) {
                return true;
            }
        }
        return false;
    }

    private int calculateCodeForWindow(String secret, long window) {
        try {
            byte[] decodedKey = new Base32().decode(secret);
            byte[] timeBytes = ByteBuffer.allocate(8).putLong(window).array();

            SecretKeySpec signKey = new SecretKeySpec(decodedKey, CRYPTO_ALGORITHM);
            Mac mac = Mac.getInstance(CRYPTO_ALGORITHM);
            mac.init(signKey);

            byte[] hash = mac.doFinal(timeBytes);
            int offset = hash[hash.length - 1] & 0xF;

            long truncatedHash = 0;
            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }

            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= Math.pow(10, CODE_LENGTH);

            return (int) truncatedHash;
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error computing HMAC-SHA1 signature", e);
        }
    }
}