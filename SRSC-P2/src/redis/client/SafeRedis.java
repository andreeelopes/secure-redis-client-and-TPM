package redis.client;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import redis.clients.jedis.Jedis;
import utils.CipherConfig;
import utils.KeyManager;
import utils.Pair;
import utils.XMLParser;

public class SafeRedis {
	
	
	private static final String PATH_TO_CONFIG_FILE = "redisClientConfig.xml";
	private static final String keyCipherName = "redis_cipherkey";
	private static final String keyMacName = "redis_mackey";
	
	private Jedis jedis ;
	private CipherConfig config;
	private Key cipherKey;
	KeyPair kp;
	private Mac mac;
	private IvParameterSpec ivParameterSpec;
	private Cipher cipher;
	Signature signature;
	public SafeRedis(String ip,int port) {
		jedis = new Jedis(ip, port, 10000, false);
		jedis.flushAll();
		jedis.connect();
		config = XMLParser.getClientconfig(PATH_TO_CONFIG_FILE);
		cipherKey = KeyManager.getOrCreateKey(keyCipherName, config.getCipherAlg(), config.getCipherProvider(), 
				config.getCipherKeySize(), "srsc", "mykeystore.jceks", "srsc");
		Key macKey = KeyManager.getOrCreateKey(keyMacName, config.getMacAlgorithm(), config.getCipherProvider(), 
				config.getMacKeySize(), "srsc", "mykeystore.jceks", "srsc");
		String encoded=config.getIv();
		byte[] iv = Base64.getDecoder().decode(encoded);
		kp=KeyManager.getKeyPair("DBcert", "srsc1718", "DBcert.jks", "srsc1718");
		ivParameterSpec = new IvParameterSpec(iv);
		try {
			signature = Signature.getInstance("SHA512withRSA", "BC");
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
			boolean valid;
			byte[] hmacValue = mac.doFinal(valueByteArray);
			Set<String> keys = jedis.smembers(field + ":" + new String(hmacValue, "ISO-8859-1"));
			List<Map<String,String>> result = new ArrayList<Map<String,String>>(keys.size());
			Iterator<String> it = keys.iterator();
			for (int i = 0; it.hasNext(); i++) {
				Map<String,String> list = new HashMap<String,String>();
				String mainKey=it.next();
				valid=true;
				Map<String, String> fields = jedis.hgetAll(mainKey);
				for (String key : fields.keySet()) {
					if(key.equals("Sign")) {
						if(!this.checkSign(mainKey, fields.get(key))) {
							valid=false;
							break;
						}
					}
					else {
						byte[] encoded = fields.get(key).getBytes("ISO-8859-1");
						cipher.init(cipher.DECRYPT_MODE, cipherKey, ivParameterSpec);
						byte[] pArray = cipher.doFinal(encoded);
						list.put(key, new String(pArray, "ISO-8859-1"));
					}
				}
				if(!this.checkIfisValid(mainKey,list)|| !valid)
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
				if(hashValue.length!=2)continue;
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
				jedis.srem("Key" + ":" + new String(hmacValue, "ISO-8859-1"), keyHash);
				this.makeThrash(keyHash);
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
	public void makeSign(String key) {
		try {
		signature.initSign(kp.getPrivate());
		byte[] keybyte= key.getBytes("ISO-8859-1");
		signature.update(keybyte);
		byte[]  sigBytes = signature.sign();
		jedis.hset(key, "Sign",  new String(sigBytes, "ISO-8859-1"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean checkSign(String key,String sign) throws Exception {

		byte[] keybyte= key.getBytes("ISO-8859-1");

		signature.initVerify(kp.getPublic());
		signature.update(keybyte);


		if (signature.verify(sign.getBytes("ISO-8859-1")))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public void makeThrash(String mainKey) throws Exception {
		Map<String,String> values=jedis.hgetAll(mainKey);
		Set<String>fields=values.keySet();
		Iterator<String> it=fields.iterator();
		while(it.hasNext()) {
			String field=it.next();
			byte[] valueByteArray = new byte[values.get(field).length()];
			new Random().nextBytes(valueByteArray);
			
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256); // for example
			SecretKey secretKey = keyGen.generateKey();
			
			Cipher tempCipher=Cipher.getInstance("AES/CBC/PKCS7Padding");
			tempCipher.init(cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
			byte[] cipherValue = tempCipher.doFinal(valueByteArray);

			String cipherStringValue = new String(cipherValue, "ISO-8859-1");

			jedis.hset(mainKey, field, cipherStringValue);
		}
	}
}
