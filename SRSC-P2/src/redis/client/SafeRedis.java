package redis.client;


import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
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

	private static final String keyCipherName = "redis_cipherkey";
	private static final String keyMacName = "redis_mackey";
	private Mac mac;
	private IvParameterSpec ivParameterSpec;
	private Cipher cipher;
	public SafeRedis() {

		jedis = new Jedis("172.17.0.2", 6379, 10000, false);
		//jedis.flushAll();
		jedis.connect();
		config = XMLParser.getClientconfig();
		cipherKey = KeyManager.getOrCreateKey(keyCipherName, config.getCipherAlg(), config.getCipherProvider(), 
				config.getCipherKeySize(), "srsc", "mykeystore.jceks", "srsc");
		Key macKey = KeyManager.getOrCreateKey(keyMacName, config.getMacAlgorithm(), config.getCipherProvider(), 
				config.getMacKeySize(), "srsc", "mykeystore.jceks", "srsc");
		String encoded=config.getIv();
		byte[] iv = Base64.getDecoder().decode(encoded);

		ivParameterSpec = new IvParameterSpec(iv);
		 try {

				mac = Mac.getInstance(config.getMacAlgorithm());
				mac.init(macKey);
			cipher = Cipher.getInstance(config.getCipherSuite(), config.getCipherProvider());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mac=null;
		}
		 
	}	
	
	public List<Map<String, String>> get(String field, String value) {
		
		try {
			byte[] valueByteArray = value.getBytes("ISO-8859-1");

			byte[] hmacValue = mac.doFinal(valueByteArray);
			Set<String> keys = jedis.smembers(field + ":" + new String(hmacValue, "ISO-8859-1"));
			List<Map<String,String>> result = new ArrayList<Map<String,String>>(keys.size());
			Iterator<String> it = keys.iterator();
			for (int i = 0; it.hasNext(); i++) {
				Map<String,String> list = new HashMap<String,String>();
				String mainKey=it.next();
				Map<String, String> fields = jedis.hgetAll(mainKey);
				for (String key : fields.keySet()) {
					byte[] encoded = fields.get(key).getBytes("ISO-8859-1");
					cipher.init(cipher.DECRYPT_MODE, cipherKey, ivParameterSpec);
					byte[] pArray = cipher.doFinal(encoded);
					list.put(key, new String(pArray, "ISO-8859-1"));
				}
				if(!this.checkIfisValid(mainKey,list))
					this.remove(list.get("Key"));
				else {
					result.add(list);
				}
			}

			return result;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean checkIfisValid(String mainKey, Map<String, String> map) {
		try {
		String[] hashValues=mainKey.split(" ");

		byte[] tempbuffer;
		String checkString;

		for(int i=0;i<hashValues.length;i++) {
			String[] hashValue=hashValues[i].split(":");
			Pair p=new Pair(hashValue[0],hashValue[1]);

			tempbuffer=map.get(p.getKey()).getBytes("ISO-8859-1");
			tempbuffer=mac.doFinal(tempbuffer);
			checkString=new String(tempbuffer,"ISO-8859-1");
			if(!checkString.equals(p.getValue())) return false;
		}
		return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public String makeHash(String field,String value)
	{
		try {
		byte[] valueByteArray = value.getBytes("ISO-8859-1");

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
			
			
			cipher.init(cipher.ENCRYPT_MODE, cipherKey, ivParameterSpec);
			byte[] cipherValue = cipher.doFinal(valueByteArray);

			String cipherStringValue = new String(cipherValue, "ISO-8859-1");
			
			//	byte[] encoded = cipherStringValue.getBytes("ISO-8859-1");

			jedis.hset(key, field, cipherStringValue);

			//criar hash para a indexação chave ex: marca:adidas, key
			//se pesquisarmos por marca:adidas vai resultar de uma lista com todas
			//as keys que tenham a marca adidas

			byte[] hmacValue = mac.doFinal(valueByteArray);
			
			jedis.sadd(field + ":" + new String(hmacValue, "ISO-8859-1"), key);


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean remove(String key) {
		//remove se tiver uma lista onde a key é Key:(valor)
		//Quando inserir definir a key no field Key

		try {
			byte[] valueByteArray = key.getBytes("ISO-8859-1");

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

		byte[] valueByteArray;
		try {
			valueByteArray = key.getBytes("ISO-8859-1");

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
