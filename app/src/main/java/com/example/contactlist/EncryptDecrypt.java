package com.example.contactlist;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class EncryptDecrypt {
    private static final String UNICODE_FORMAT = "UTF-8";

    public static SecretKey generateKey(String encryptionType){
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionType);
            SecretKey myKey = keyGenerator.generateKey();
            return myKey;
        }
        catch (Exception exception)
        {
            return null;
        }
    }

    public static byte[] encryptString(String dataToEncrypt, SecretKey myKey, Cipher cipher){

        try {
            byte[] text = dataToEncrypt.getBytes(UNICODE_FORMAT);
            cipher.init(Cipher.ENCRYPT_MODE, myKey);
            byte[] textEncrypted = cipher.doFinal(text);
            return textEncrypted;

        }
        catch (Exception exception)
        {
            return null;

        }
    }

    public static String decryptString(byte[] dataToDecrypt, SecretKey myKey, Cipher cipher){
        try {
            cipher.init(Cipher.DECRYPT_MODE, myKey);
            byte[] textDecrypted = cipher.doFinal(dataToDecrypt);
            String result = new String(textDecrypted);
            return result;
        }
        catch (Exception exception)
        {
            System.out.println(exception);
            return null;
        }
    }
}
