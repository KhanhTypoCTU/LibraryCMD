package khanhtypo.librarycmd.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class PswrdObfsctr {
    private static final Base64.Encoder ENCODER = Base64.getEncoder().withoutPadding();

    private PswrdObfsctr() {
    }

    public static String encode(String plaintext) {
        return new StringBuilder(internal_encode(new StringBuilder(internal_encode(plaintext)).reverse().toString())).reverse().toString();
    }

    public static String decode(String encoded) {
        return makeString(Base64.getDecoder().decode(new StringBuilder().append(makeString(Base64.getDecoder().decode(new StringBuilder(encoded).reverse().toString()))).reverse().toString()));
    }

    private static String makeString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String internal_encode(String plaintext) {
        return makeString(ENCODER.encode(plaintext.getBytes(StandardCharsets.UTF_8)));
    }
}
