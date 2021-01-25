package com.tiberiugaspar.tpjadcontactsapp.utils;

import android.content.Context;
import android.security.KeyPairGeneratorSpec;
import android.util.Base64;
import android.util.Log;

import com.tiberiugaspar.tpjadcontactsapp.models.Contact;
import com.tiberiugaspar.tpjadcontactsapp.models.PhoneNumber;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

public class EncryptionV2 {
    private static final String LOG_TAG = "encrypt_decrypt";
    private static final String KEY_ALIAS = "rsa_key";
    private static PrivateKey privKey;
    private static PublicKey pubKey;

    public static void generateKeys(Context context) {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 25);
        Date end = cal.getTime();

        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            kpg.initialize(new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setStartDate(now)
                    .setEndDate(end)
                    .setSerialNumber(BigInteger.valueOf(1))
                    .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                    .build());
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        KeyPair kp = kpg.generateKeyPair();

        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration<String> aliases = ks.aliases();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }

        KeyStore.Entry entry = null;
        try {
            entry = ks.getEntry(KEY_ALIAS, null);
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableEntryException e) {
            e.printStackTrace();
        }
        if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
            Log.e(LOG_TAG, "Not an instance of PrivateKeyEntry.");
        } else {
            privKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
            pubKey = ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();
        }

    }

    public static String encryptString(String value) {
        byte[] encodedBytes = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround");
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encodedBytes = cipher.doFinal(value.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    public static String decryptString(String value) {
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround");
            c.init(Cipher.DECRYPT_MODE, privKey);
            decodedBytes = c.doFinal(Base64.decode(value, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Error", "Error = " + e);
            return "SECURE_FAILURE";
        }

        return new String(decodedBytes);
    }

    public static Contact encryptContact(Contact contact) {

        contact.setEmail(encryptString(contact.getEmail()));
        contact.setUriToImage(encryptString(contact.getUriToImage()));
        contact.setFirstName(encryptString(contact.getFirstName()));
        contact.setLastName(encryptString(contact.getLastName()));

        List<PhoneNumber> phoneNumbers = new ArrayList<>();

        for (PhoneNumber p : contact.getPhoneNumberList()) {
            p.setPhoneNumber(encryptString(p.getPhoneNumber()));
            phoneNumbers.add(p);
        }
        contact.setPhoneNumberList(phoneNumbers);

        return contact;
    }

    public static Contact decryptContact(Contact contact) {
        contact.setFirstName(decryptString(contact.getFirstName()));
        contact.setLastName(decryptString(contact.getLastName()));
        contact.setEmail(decryptString(contact.getEmail()));
        contact.setUriToImage(decryptString(contact.getUriToImage()));

        List<PhoneNumber> phoneNumberList = new ArrayList<>();

        for (PhoneNumber p : contact.getPhoneNumberList()) {
            p.setPhoneNumber(decryptString(p.getPhoneNumber()));
            phoneNumberList.add(p);
        }
        contact.setPhoneNumberList(phoneNumberList);
        return contact;
    }

}
