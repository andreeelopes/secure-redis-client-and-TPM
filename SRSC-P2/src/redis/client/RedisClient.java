package redis.client;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import utils.Pair;

public class RedisClient {
/*por número de
cliente (inteiro), cartão de cidadão do cliente (string), data de emissão (string)
, morada (string), telefone
(string), número de contribuinte fiscal do cliente (string),*/
	private SafeRedis redis;
	public RedisClient(String ip,int port) {
		redis = new SafeRedis(ip,port);
	}
	public void setClient(int clientnr,String CC,String emissionDate,
			String address,String phone, String NIF) {
		String clientnrString=String.valueOf(clientnr);
		redis.ifExistDel(clientnrString);
		int totalSize=0;
		String key=redis.makeHash("Key", clientnrString);
		totalSize+= key.length();
		String ccH=" "+redis.makeHash("cc", CC);
		totalSize+=ccH.length();
		String dateH=" "+redis.makeHash("emissionDate", emissionDate);
		totalSize+=dateH.length();
		String addressH=" "+redis.makeHash("address", address);
		totalSize+=addressH.length();
		String phoneH=" "+redis.makeHash("phone", phone);
		totalSize+=phoneH.length();
		String NIFH=" "+redis.makeHash("nif", NIF);
		totalSize+=NIFH.length();
		StringBuilder sb = new StringBuilder(totalSize);
		sb.append(key);
		sb.append(ccH);
		sb.append(dateH);
		sb.append(addressH);
		sb.append(phoneH);
		sb.append(NIFH);
		//System.out.println(key);
		String mainKey=sb.toString();
		redis.set(mainKey, "Key", clientnrString);
		redis.set(mainKey, "cc", CC);
		redis.set(mainKey, "emissionDate", emissionDate);
		redis.set(mainKey, "address", address);
		redis.set(mainKey, "phone", phone);
		redis.set(mainKey, "nif", NIF);
		redis.makeSign(mainKey);
	}
	public List<Map<String,String>>  getClientID(String value) {

		List<Map<String,String>> result = redis.get("Key", value);
		
		//so para debug
//		for (int i = 0; i < result.size(); i++) {
//			Iterator<Map<String, String>> it = result.iterator();
//			while (it.hasNext()) {
//				Map<String, String> p = it.next();
//				for(String key: p.keySet())
//					System.out.print(key+": "+p.get(key)+" ");
//			}
//			System.out.println();
//		
//	}
		return result;
	}
	public List<Map<String,String>>  getClientCC(String value) {

		List<Map<String,String>> result = redis.get("cc", value);
		
		//so para debug
//		for (int i = 0; i < result.size(); i++) {
//			Iterator<Map<String, String>> it = result.iterator();
//			while (it.hasNext()) {
//				Map<String, String> p = it.next();
//				for(String key: p.keySet())
//					System.out.print(key+": "+p.get(key)+" ");
//			}
//			System.out.println();
		
//	}
		return result;
	}
	public List<Map<String,String>>  getClientNIF(String value) {

		List<Map<String,String>> result = redis.get("nif", value);
		
		//so para debug
//		for (int i = 0; i < result.size(); i++) {
//			Iterator<Map<String, String>> it = result.iterator();
//			while (it.hasNext()) {
//				Map<String, String> p = it.next();
//				for(String key: p.keySet())
//					System.out.print(key+": "+p.get(key)+" ");
//			}
//			System.out.println();
//		
//	}
		return result;
	}
	public List<Map<String,String>>  getClientAdd(String value) {

			List<Map<String,String>> result = redis.get("address", value);
			
			//so para debug
//			for (int i = 0; i < result.size(); i++) {
//				Iterator<Map<String, String>> it = result.iterator();
//				while (it.hasNext()) {
//					Map<String, String> p = it.next();
//					for(String key: p.keySet())
//						System.out.print(key+": "+p.get(key)+" ");
//				}
//				System.out.println();
//			
//		}
		return result;
	}
	public List<Map<String,String>>  getClientDate(String value) {

			List<Map<String,String>> result = redis.get("emissionDate", value);
			
			//so para debug
//			for (int i = 0; i < result.size(); i++) {
//				Iterator<Map<String, String>> it = result.iterator();
//				while (it.hasNext()) {
//					Map<String, String> p = it.next();
//					for(String key: p.keySet())
//						System.out.print(key+": "+p.get(key)+" ");
//				}
//				System.out.println();
//			
//		}
		return result;
	}
	
	public List<Map<String,String>>  getClientPhone(String value) {

			List<Map<String,String>> result = redis.get("phone", value);
			
			//so para debug
//			for (int i = 0; i < result.size(); i++) {
//				Iterator<Map<String, String>> it = result.iterator();
//				while (it.hasNext()) {
//					Map<String, String> p = it.next();
//					for(String key: p.keySet())
//						System.out.print(key+": "+p.get(key)+" ");
//				}
//				System.out.println();
//			
//		}
		return result;
	}
	
	public void removeClient(int clientID) {
		String clientnrString=String.valueOf(clientID);
		redis.remove(clientnrString);
	}
}
