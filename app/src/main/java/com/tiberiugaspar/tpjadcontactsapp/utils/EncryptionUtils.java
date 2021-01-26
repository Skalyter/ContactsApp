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
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;
import javax.security.auth.x500.X500Principal;

/**
 * An utility class for managing encryption/decryption processes
 */
public class EncryptionUtils {

    private static final String KEY_ALIAS = "rsa_key";
    private static PrivateKey privKey;
    private static PublicKey pubKey;

    /**
     * This method should be called only once, when the user is opening the app for the first time
     * <p>The main functionality of this method is to generate pair of
     * {@link PublicKey} and {@link PrivateKey} objects, used for encryption/decryption. These keys
     * are meant to be unique for every device, therefore a user can't have access to another users'
     * contact lists</p>
     *
     * @param context the application context
     */
    public static void generateKeys(Context context) {

        //instantiate a calendar object to store the key pair validity (in this case, will be valid
        //for 25 years after the user first opens the app)
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 25);
        Date end = cal.getTime();

        //instantiate the KeyPairGenerator object which will generate the keys, for the RSA algorithm
        // using the AndroidKeyStore - default key store provider
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        try {
            //build the keyPairGenerator
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

        //generate the key pair
        KeyPair kp = kpg.generateKeyPair();

        //initialize the keyStore
        initializeKeyStore();
    }

    /**
     * This method uses a KeyStore object to instantiate the {@link PrivateKey} and {@link PublicKey}
     * objects, as often as the user needs to get his/her keys safely from the AndroidKeyStore
     */
    private static void initializeKeyStore() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            Enumeration<String> aliases = ks.aliases();
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        }

        try {
            privKey = (PrivateKey) ks.getKey(KEY_ALIAS, null);
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        try {
            pubKey = ks.getCertificate(KEY_ALIAS).getPublicKey();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method encrypt a given string using the {@link PublicKey} instance with the RSA algorithm.
     *
     * @param value the String original plaintext to be encrypted
     * @return the encrypted plaintext
     */
    private static String encryptString(String value) {
        //a quick checkup to see if any of the keys are null. If so, we instantiate them by calling
        // initializeKeyStore() method
        if (privKey == null || pubKey == null) {
            initializeKeyStore();
        }
        //creating a byte array which will hold the encoded plaintext
        byte[] encodedBytes = null;

        try {
            //instantiating the Cipher object, setting the RSA algorithm and the AndroidKeyStoreProvider
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding",
                    "AndroidKeyStoreBCWorkaround");

            //initialize the Cypher with the ENCRYPT_MODE and the public key
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);

            //set the byte array with the returned value of cipher's method doFinal()
            // which returns the encrypted byte plaintext
            encodedBytes = cipher.doFinal(value.getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }

        //finally, we return the encoded byte array as a string, using the Base64 default encoding system
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    /**
     * This method decrypt a given encrypted plaintext using the RSA algorithm
     *
     * @param value the encrypted plaintext
     * @return the decrypted plaintext for the given value
     */
    private static String decryptString(String value) {

        //quick checkup to see whether the keys are null or not. If true, we initialize them once again
        if (privKey == null || pubKey == null) {
            initializeKeyStore();
        }

        //creating a byte array to hold the decrypted byte message
        byte[] decodedBytes = null;
        try {
            //instantiate the cipher with the RSA algorithm and AndroidKeyStore provider
            Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidKeyStoreBCWorkaround");

            //initialize the cipher in DECRYPT_MODE using the private key
            c.init(Cipher.DECRYPT_MODE, privKey);

            //set the byte array with the result of cipher's doFinal() method which decrypt the
            //encrypted plaintext using Base64's default decoding system
            decodedBytes = c.doFinal(Base64.decode(value, Base64.DEFAULT));

        } catch (Exception e) {

            //if any problem occurs, we print a log containing the exception message and return
            //"SECURE_FAILURE" value
            e.printStackTrace();
            Log.e("Error", "Error = " + e);
            return "SECURE_FAILURE";
        }

        //if the algorithm successfully decrypted the encrypted plaintext, we return a new String
        // object, created from the byte array
        return new String(decodedBytes);
    }

    /**
     * Encrypt a given {@link Contact} object with all its fields, excepting
     * the user's id and contact's id - because they're alphanumeric ids and are not sensitive,
     * and phoneNumber's category - because it is simply an integer code corresponding to the category
     * of the phone number
     *
     * @param contact the {@link Contact} object to be encrypted
     * @return the same {@link Contact} object with its fields successfully encrypted
     */
    public static Contact encryptContact(Contact contact) {

        //call the encryptString for each field of the contact object
        contact.setFirstName(encryptString(contact.getFirstName()));

        //encrypt the lastName only if it is not null (because it is an optional field)
        if (contact.getLastName() != null) {
            contact.setLastName(encryptString(contact.getLastName()));
        }

        //the same logic as above
        if (contact.getEmail() != null) {
            contact.setEmail(encryptString(contact.getEmail()));
        }

        //the same logic as above
        if (contact.getUriToImage() != null) {
            contact.setUriToImage(encryptString(contact.getUriToImage()));
        }

        List<PhoneNumber> phoneNumbers = new ArrayList<>();

        //encrypt each phoneNumber from the contact's phoneNumberList
        for (PhoneNumber p : contact.getPhoneNumberList()) {
            p.setPhoneNumber(encryptString(p.getPhoneNumber()));
            phoneNumbers.add(p);
        }
        //set the phoneNumberList with the encrypted contact list
        contact.setPhoneNumberList(phoneNumbers);

        //return the encrypted contact
        return contact;
    }

    /**
     * Decrypt a given {@link Contact} object with all its fields, excepting
     * the user's id, contact's id and phoneNumbers' categories -
     * because they weren't encrypted in the first place
     *
     * @param contact the {@link Contact} object to be decrypted
     * @return the same {@link Contact} object with its fields successfully decrypted
     */
    public static Contact decryptContact(Contact contact) {

        //decrypt all fields using decryptString() method
        contact.setFirstName(decryptString(contact.getFirstName()));

        //decrypt last name only if it is not null (because it is an optional field)
        if (contact.getLastName() != null && !contact.getLastName().equals("null")) {
            contact.setLastName(decryptString(contact.getLastName()));
        }

        //the same logic as above
        if (contact.getEmail() != null && !contact.getEmail().equals("null")) {
            contact.setEmail(decryptString(contact.getEmail()));
        }

        //the same logic as above
        if (contact.getUriToImage() != null && !contact.getUriToImage().equals("null")) {
            contact.setUriToImage(decryptString(contact.getUriToImage()));
        }

        List<PhoneNumber> phoneNumberList = new ArrayList<>();

        //decrypt all the contact's phone numbers
        for (PhoneNumber p : contact.getPhoneNumberList()) {
            p.setPhoneNumber(decryptString(p.getPhoneNumber()));
            phoneNumberList.add(p);
        }
        //set the phoneNumberList with the decrypted phone numbers list
        contact.setPhoneNumberList(phoneNumberList);

        //return the successfully decrypted contact object
        return contact;
    }
}
