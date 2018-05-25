package utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;



public class KeyManager {


	public static KeyStore getOrCreateKeyStore(String fileName, String pw) {
		File file = new File(fileName);
		KeyStore keyStore = null;

		try {
			keyStore = KeyStore.getInstance("JCEKS");

			if (file.exists()) {// .keystore file already exists => load it
				try {
					keyStore.load(new FileInputStream(file), pw.toCharArray());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {// .keystore file not created yet => create it
				keyStore.load(null, null);
				try {
					keyStore.store(new FileOutputStream(fileName), pw.toCharArray());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return keyStore;
	}


	public static SecretKey generateKey(String algorithm, String provider, int keySize) {
		SecretKey secretKey = null;
		KeyGenerator keyGen = null;
		try {
			keyGen = KeyGenerator.getInstance(algorithm, provider);
			keyGen.init(keySize);
			secretKey = keyGen.generateKey();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return secretKey;
	}

	public static KeyPair getKeyPair(String entryName, String keyMasterPwd, String keyStoreFile, String keyStorePwd) {

		PublicKey publicKey = null;
		Key key = null;
		try {

			KeyStore keystore = getOrCreateKeyStore(keyStoreFile, keyStorePwd);
			key = keystore.getKey(entryName, keyMasterPwd.toCharArray());
			if (key instanceof PrivateKey) {
				Certificate cert = keystore.getCertificate(entryName);
				// Get now public key
				publicKey = cert.getPublicKey();
			}
		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return new KeyPair(publicKey, (PrivateKey) key);
	}


	public static void storeKey(SecretKey key, String entryName, String keyMasterPwd, String keyStoreFile, String keyStorePwd) {

		KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(key);
		PasswordProtection keyPassword = new PasswordProtection(keyMasterPwd.toCharArray());
		try {
			KeyStore keyStore = getOrCreateKeyStore(keyStoreFile, keyStorePwd);
			keyStore.setEntry(entryName, keyStoreEntry, keyPassword);
			keyStore.store(new FileOutputStream(keyStoreFile), keyStorePwd.toCharArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static Key getKey(String keyName, String keyMasterPwd, String keyStoreFile, String keyStorePwd) {

		PasswordProtection pwdProtection = new PasswordProtection(keyMasterPwd.toCharArray());
		KeyStore.Entry entry = null;
		Key key = null;
		try {
			entry = getOrCreateKeyStore(keyStoreFile, keyStorePwd).getEntry(keyName, pwdProtection);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (entry != null)
			key = ((KeyStore.SecretKeyEntry) entry).getSecretKey();

		return key;
	}


	public static Key getOrCreateKey(String keyName, String algorithm, String provider, 
			int keySize, String keyMasterPwd, String keyStoreFile, String keyStorePwd) {

		Key k = null;
		try {
			if ((k = KeyManager.getKey(keyName, keyMasterPwd, keyStoreFile, keyStorePwd)) == null) {
				k = KeyManager.generateKey(algorithm, provider, keySize);
				KeyManager.storeKey((SecretKey) k, keyName, keyMasterPwd, keyStoreFile, keyStorePwd);
			}
		} catch (Exception e) {
		}
		return k;
	}

}
