package com.twiiiins.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class Tokens {
    private Tokens() {}
    public static String random() {
        byte[] value = new byte[32];
        new SecureRandom().nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
    public static String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    public static String sign(String context, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(context.getBytes(StandardCharsets.UTF_8));
            return encoded + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(encoded.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    public static String verify(String token, String key) {
        try {
            String encoded = token.substring(0, token.indexOf('.'));
            String context = new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
            if (!MessageDigest.isEqual(sign(context, key).getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) throw new IllegalArgumentException();
            return context;
        } catch (Exception e) { throw new IllegalArgumentException("Invalid or expired link."); }
    }
}
