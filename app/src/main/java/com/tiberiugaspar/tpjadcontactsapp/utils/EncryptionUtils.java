package com.tiberiugaspar.tpjadcontactsapp.utils;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;

import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

public class EncryptionUtils {

    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String KEY_ALIAS = "rsa_key";
    private static final String RSA_MODE = "RSA/ECB/PKCS1Padding";

    private static KeyStore keyStore;

    public static void generateKeys(Context context) {

        try {
            keyStore = KeyStore.getInstance(AndroidKeyStore);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        try {
            keyStore.load(null);
        } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

// Generate the RSA key pairs
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                // Generate a key pair for encryption
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 30);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(KEY_ALIAS)
                        .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                        .setSerialNumber(BigInteger.TEN)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

                KeyPairGenerator kpg
                        = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore);
                kpg.initialize(spec);
                kpg.generateKeyPair();
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

    }

    private static byte[] rsaEncrypt(byte[] secret) {
        PublicKey publicKey
                = null;
        try {
            publicKey = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        // Encrypt the text
        Cipher inputCipher = null;
        try {
            inputCipher = Cipher.getInstance(RSA_MODE, AndroidKeyStore);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            inputCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        try {
            cipherOutputStream.write(secret);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            cipherOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    private static byte[] rsaDecrypt(byte[] encrypted) {
        PrivateKey privateKey
                = null;
        try {
            privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }

        Cipher output = null;
        try {
            output = Cipher.getInstance(RSA_MODE, AndroidKeyStore);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        try {
            output.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(encrypted), output);

        ArrayList<Byte> values = new ArrayList<>();

        int nextByte;
        while (true) {
            try {
                if ((nextByte = cipherInputStream.read()) == -1) break;
                values.add((byte) nextByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] bytes = new byte[values.size()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i).byteValue();
        }
        return bytes;
    }

    private static String string2Bytes(byte[] byteArray) {
        return new String(byteArray, StandardCharsets.UTF_8);
    }

    private static byte[] int2ByteArray(int integer) {
        return ByteBuffer.allocate(4).putInt(integer).array();
    }

    private static int byteArray2Int(byte[] byteArray) {
        return ByteBuffer.wrap(byteArray).getInt();
    }

    public static Contact encryptContact(Contact contact) {
        Contact c = new Contact();
//        c.setUserId(string2Bytes(rsaEncrypt(contact.getUserId().getBytes(StandardCharsets.UTF_8))));
//        c.setContactId(string2Bytes(rsaEncrypt(contact.getContactId().getBytes(StandardCharsets.UTF_8))));
        c.setEmail(string2Bytes(rsaEncrypt(contact.getEmail().getBytes(StandardCharsets.UTF_8))));
        c.setUriToImage(string2Bytes(rsaEncrypt(contact.getUriToImage().getBytes(StandardCharsets.UTF_8))));
        c.setFirstName(string2Bytes(rsaEncrypt(contact.getFirstName().getBytes(StandardCharsets.UTF_8))));
        c.setLastName(string2Bytes(rsaEncrypt(contact.getLastName().getBytes(StandardCharsets.UTF_8))));
        List<PhoneNumber> phoneNumberList = new ArrayList<>();
        for (PhoneNumber p : contact.getPhoneNumberList()) {
            PhoneNumber p1 = new PhoneNumber();
            p1.setPhoneNumber(string2Bytes(rsaEncrypt(p.getPhoneNumber().getBytes(StandardCharsets.UTF_8))));
            p1.setCategory(byteArray2Int(rsaEncrypt(int2ByteArray(p.getCategory()))));
            phoneNumberList.add(p1);
        }
        c.setPhoneNumberList(phoneNumberList);
        return c;
    }

    public static Contact decryptContact(Contact contact) {
        Contact c = new Contact();
//        c.setUserId(string2Bytes(rsaDecrypt(contact.getUserId().getBytes(StandardCharsets.UTF_8))));
//        c.setContactId(string2Bytes(rsaDecrypt(contact.getContactId().getBytes(StandardCharsets.UTF_8))));
        c.setEmail(string2Bytes(rsaDecrypt(contact.getEmail().getBytes(StandardCharsets.UTF_8))));
        c.setUriToImage(string2Bytes(rsaDecrypt(contact.getUriToImage().getBytes(StandardCharsets.UTF_8))));
        c.setFirstName(string2Bytes(rsaDecrypt(contact.getFirstName().getBytes(StandardCharsets.UTF_8))));
        c.setLastName(string2Bytes(rsaDecrypt(contact.getLastName().getBytes(StandardCharsets.UTF_8))));
        List<PhoneNumber> phoneNumberList = new ArrayList<>();
        for (PhoneNumber p : contact.getPhoneNumberList()) {
            PhoneNumber p1 = new PhoneNumber();
            p1.setPhoneNumber(string2Bytes(rsaDecrypt(p.getPhoneNumber().getBytes(StandardCharsets.UTF_8))));
            p1.setCategory(byteArray2Int(rsaDecrypt(int2ByteArray(p.getCategory()))));
            phoneNumberList.add(p1);
        }
        c.setPhoneNumberList(phoneNumberList);
        return c;
    }


}
