package redis.client;


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
import utils.CipherConfig;
import utils.KeyManager;
import utils.Pair;
import utils.Utils;
import utils.XMLParser;

public class SafeRedis {
	private Jedis jedis ;
	private CipherConfig config;
	private Key cipherKey;
	private Key macKey;
	private static final String keyCipherName = "redis_cipherkey";
	private static final String keyMacName = "redis_mackey";

	private IvParameterSpec ivParameterSpec;
	
	public SafeRedis() {

		jedis = new Jedis("172.17.0.2", 6379, 10000, false);
		jedis.flushAll();
		jedis.connect();
		config = XMLParser.getClientconfig();
		cipherKey = KeyManager.getOrCreateKey(keyCipherName, config.getCipherAlg(), config.getCipherProvider(), 
				config.getCipherKeySize(), "srsc", "mykeystore.jceks", "srsc");
		macKey = KeyManager.getOrCreateKey(keyMacName, config.getMacAlgorithm(), config.getCipherProvider(), 
				config.getMacKeySize(), "srsc", "mykeystore.jceks", "srsc");

		byte[] iv = new byte[16]; //TODO :tirar isto daqui
		new SecureRandom().nextBytes(iv);
		ivParameterSpec = new IvParameterSpec(iv);
	}	
	
	public List<List<Pair>> get(String field, String value) {
		Mac mac;
		try {
			byte[] valueByteArray = value.getBytes("ISO-8859-1");
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
					list.add(new Pair(key, new String(pArray, "ISO-8859-1")));
				}
				result.add(list);
			}

			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public String makeHash(String field,String value)
	{
		try {
		byte[] valueByteArray = value.getBytes("ISO-8859-1");
		Mac mac = Mac.getInstance(config.getMacAlgorithm());
		mac.init(macKey);
		byte[] hmacValue = mac.doFinal(valueByteArray);
	
			return (field + ":" + new String(hmacValue, "ISO-8859-1"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;

	}
	public void set(String key, String field, String value) {
		try {
			
			
			byte[] valueByteArray = value.getBytes("ISO-8859-1");
			
			Cipher cipher = Cipher.getInstance(config.getCipherSuite(), config.getCipherProvider()); //cipher value
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
			
			jedis.sadd(field + ":" + new String(hmacValue, "ISO-8859-1"), key);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean remove(String key) {
		//remove se tiver uma lista onde a key é Key:(valor)
		//Quando inserir definir a key no field Key
		Mac mac;
		try {
			byte[] valueByteArray = key.getBytes("ISO-8859-1");
			mac = Mac.getInstance(config.getMacAlgorithm());
			mac.init(macKey);
			byte[] hmacValue = mac.doFinal(valueByteArray);
			Set<String> keys = jedis.smembers("Key" + ":" + new String(hmacValue, "ISO-8859-1"));
			Iterator<String> it = keys.iterator();
			if (!it.hasNext())
				return false;
			else {
				String keyHash=it.next();
				String[] keysArray =keyHash.split(" ");
				for(int i=0;i<keysArray.length;i++) {
					jedis.srem(keysArray[i], keyHash);
				}
				jedis.del(keyHash);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public void ifExistDel(String key) {
		Mac mac;
		byte[] valueByteArray;
		try {
			valueByteArray = key.getBytes("ISO-8859-1");
		mac = Mac.getInstance(config.getMacAlgorithm());
		mac.init(macKey);
		byte[] hmacValue = mac.doFinal(valueByteArray);
		String skey="Key" + ":" + new String(hmacValue, "ISO-8859-1");
		Set<String> keys = jedis.smembers(skey);
		if(keys.size()>0) {
			this.remove(key);
			jedis.spop(skey);
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
