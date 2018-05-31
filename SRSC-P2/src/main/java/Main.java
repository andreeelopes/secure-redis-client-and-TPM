package main.java;

import java.util.Iterator;
import java.util.List;

import TPM.client.TPMClient;
import TPM.client.TPMClientConfig;
import redis.client.RedisClient;
import redis.client.SafeRedis;
import utils.Pair;

public class Main {

	public static void main(String[] args) {

	
		//		RedisClient redis=new RedisClient();
		//		redis.setClient(1, "ola", "ola", "ola", "ola", "ola");
		//		//redis.setClient(1, "ola", "adeus", "ola", "ola", "ola");
		//		//redis.removeClient(1);
		//
		//		redis.getClientDate("ola");


		//		RedisClient redis=new RedisClient();
		//		redis.setClient(1, "ola", "ola", "ola", "ola", "ola");
		//		//redis.setClient(1, "ola", "adeus", "ola", "ola", "ola");
		//		
		//
		//		redis.getClientDate("ola");


		//client.remove("ola");
		//		// TODO Auto-generated method stub
		//		SafeRedis client = new SafeRedis();
		//		client.set("ola", "ola", "what");
		//		client.set("ola2", "ola", "what");
		/*
		List<List<Pair>> result = client.get("Key", "ola");
		for (int i = 0; i < result.size(); i++) {
			Iterator<Pair> it = result.get(i).iterator();
			String fields = "";
			while (it.hasNext()) {
				Pair p = it.next();
				System.out.println(p.getKey() + ": " + p.getValue());
			}
		}*/
		
		
		//String json = new TPMClientConfig("TPMClientTrustStore", "srscsrsc", "goscert", "vmscert", "DH", "BC", "SHA256withRSA", "BC",
		//		"SHA256", "BC").toJSON();
		//System.out.println("JSON = \n" + json);
		
		
		System.out.println("\n\n\n ATTESTATION RESULT = " + new TPMClient("TPMClientConfig.json").attest("localhost", 4446, "localhost", 4443) + "\n\n\n");
	}

}
