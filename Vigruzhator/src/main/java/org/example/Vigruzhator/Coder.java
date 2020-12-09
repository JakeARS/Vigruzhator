package org.example.Vigruzhator;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Base64;

public class Coder {
    private static String transformation = "AES/ECB/PKCS5Padding";

    private static Key getKey() {
        String encodedKey = "YrnAjDNr9EJlVKcsUZbqsA==";
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        Key originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        return originalKey;
    }

    public static String decodePassword(String str) throws Exception {
        String decodedString;
        Key originalKey = getKey();

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, originalKey);
        byte[] encrypted = DatatypeConverter.parseHexBinary(str);

        decodedString = new String(cipher.doFinal(encrypted));

        return decodedString;
    }

    public static String encodePassword(String str) throws Exception {
        String encodedString;
        Key originalKey = getKey();

        //задаем параметры для кодирования
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, originalKey);
        //кодируем нашу строку в массив байт
        byte[] encrypted = cipher.doFinal(str.getBytes());
        encodedString = DatatypeConverter.printHexBinary(encrypted);

        return encodedString;
    }
}
