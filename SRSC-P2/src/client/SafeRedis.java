package client;


import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import redis.clients.jedis.Jedis;

public class SafeRedis {
	private Jedis jedis ;
	private CipherConfig config;
	private static KeyManager keyMan;
	private Key cipherKey;
	private Key macKey;
	private static final String keyCipher = "redis_cipherkey";
	private static final String keymac = "redis_mackey";

	private IvParameterSpec ivParameterSpec;
	public SafeRedis() {

		jedis = new Jedis("172.17.0.2", 6379, 10000, false);
		jedis.flushAll();
		jedis.connect();
		config = XMLParser.getClientconfig();
		KeyManager.setCredencials("srsc", "srsc");
		cipherKey = getKey(keyCipher, config.getCipherAlg(), config.getCipherProvider(), 
				config.getCipherKeySize());
		macKey = getKey(keymac, config.getMacAlgorithm(), config.getCipherProvider(), 
				config.getMacKeySize());
		
		byte[] iv = new byte[16]; //TODO :tirar isto daqui
		new SecureRandom().nextBytes(iv);
		ivParameterSpec = new IvParameterSpec(iv);
	}


	private static Key getKey(String entryName, String algorithm, String provider, int keySize) {

		Key k = null;
		try {
			if ((k = KeyManager.getKey(entryName)) == null) {
				k = KeyManager.generateKey(algorithm, provider, keySize);
				KeyManager.storeKey((SecretKey) k, entryName);
			}
		} catch (Exception e) {
		}
		return k;
	}
	public List<List<Pair>> get(String field, String value) {
		byte[] valueByteArray = Utils.toByteArrayFromString(value);
		Mac mac;
		try {
			mac = Mac.getInstance(config.getMacAlgorithm());
			mac.init(macKey);
			byte[] hmacValue = mac.doFinal(valueByteArray);
			Set<String> keys = jedis.smembers(field + ":" + new String(hmacValue, "ISO-8859-1"));
			List<List<Pair>> result = new ArrayList<List<Pair>>(keys.size());
			Iterator<String> it = keys.iterator();
			for (int i = 0; it.hasNext(); i++) {
				List<Pair> list = new LinkedList<Pair>();
				Map<String, String> fields = jedis.hgetAll(it.next());
				for (String key : fields.keySet()) {

					byte[] encoded = fields.get(key).getBytes("ISO-8859-1");
					Cipher cipher = Cipher.getInstance(config.getCipherSuite(), config.getCipherProvider());
					cipher.init(cipher.DECRYPT_MODE, cipherKey, ivParameterSpec);
					byte[] pArray = cipher.doFinal(encoded);
					list.add(new Pair(key, Utils.toStringFromByteArray(pArray)));
				}
				result.add(list);
			}

			return result;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public void set(String key, String field, String value) {
		try {
			//TODO: chave= h(valores de todos os fields)
			byte[] valueByteArray = Utils.toByteArrayFromString(value);
			//cifrar o value


			Cipher cipher = Cipher.getInstance(config.getCipherSuite(), config.getCipherProvider());
			cipher.init(cipher.ENCRYPT_MODE, cipherKey, ivParameterSpec);
			byte[] cipherValue = cipher.doFinal(valueByteArray);

			String cipherStringValue = new String(cipherValue, "ISO-8859-1");
			//	byte[] encoded = cipherStringValue.getBytes("ISO-8859-1");

			jedis.hset(key, field, cipherStringValue);



			//criar hash para a indexação chave ex: marca:adidas, key
			//se pesquisarmos por marca:adidas vai resultar de uma lista com todas
			//as keys que tenham a marca adidas
			Mac mac = Mac.getInstance(config.getMacAlgorithm());
			mac.init(macKey);
			byte[] hmacValue = mac.doFinal(valueByteArray);
			System.out.println(new String(hmacValue, "ISO-8859-1"));
			jedis.sadd(field + ":" + new String(hmacValue, "ISO-8859-1"), key);


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
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean remove(String key) {
		//remove se tiver uma lista onde a key é Key:(valor)
		//Quando inserir definir a key no field Key
		byte[] valueByteArray = Utils.toByteArrayFromString(key);
		Mac mac;
		try {
			mac = Mac.getInstance(config.getMacAlgorithm());
			mac.init(macKey);
			byte[] hmacValue = mac.doFinal(valueByteArray);
			Set<String> keys = jedis.smembers("Key" + ":" + new String(hmacValue, "ISO-8859-1"));
			Iterator<String> it = keys.iterator();
			if (!it.hasNext())
				return false;
			else {
				jedis.del(it.next());
				return true;
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
		return false;
	}
}
