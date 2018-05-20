package client;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import redis.clients.jedis.Jedis;

public class Client {
	private Jedis jedis ;
	private CipherConfig config;
	private static KeyManager keyMan;
	private Key cipherKey;
	private Key macKey;
	private static final String keyCipher="redis_cipherkey";
	private static final String keymac="redis_mackey";
	public Client() {
		jedis = new Jedis("172.17.0.2", 6379);
        jedis.connect();
        jedis.flushAll();
        String cipherA[]=XMLParser.getClientconfig();
        KeyManager.setCredencials("srsc", "srsc");
        config= new CipherConfig(cipherA[0],cipherA[1],cipherA[2],cipherA[3]);
        String cipherAlg = config.getCipherSuite().split("/")[0];
        cipherKey= getKey(keyCipher,cipherAlg,cipherA[2], Integer.parseInt(cipherA[4]));
        macKey= getKey(keymac,cipherA[1],cipherA[3],Integer.parseInt(cipherA[5]));
        
	}
	
	
	private static Key getKey(String entryName, String algorithm, String provider, int keySize) {

		Key k = null;
		try {
			if((k = KeyManager.getKey(entryName)) == null) {
				k = KeyManager.generateKey(algorithm, provider, keySize);
				KeyManager.storeKey((SecretKey) k, entryName);
			}
		}
		catch (Exception e) {
		}
		return k;
	}
	
	public void set(String key,String field,String value) {
		try {

			Cipher cipher = Cipher.getInstance(config.getCipherSuite(),config.getCipherProvider());
			cipher.init(cipher.ENCRYPT_MODE, cipherKey);
			byte[] cipherID=cipher.doFinal(Utils.toByteArrayFromString(value));
		
		
		
		
		
		} catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
