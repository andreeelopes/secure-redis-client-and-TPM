package main.java;

import java.util.Iterator;
import java.util.List;

import TPM.client.TPMClient;
import redis.client.RedisClient;
import redis.client.SafeRedis;
import utils.Pair;

public class Main {

	public static void main(String[] args) {
		RedisClient redis=new RedisClient();
		redis.setClient(1, "ola", "ola", "ola", "ola", "ola");
		redis.setClient(1, "ola", "adeus", "ola", "ola", "ola");
		redis.removeClient(1);

		redis.getClientDate("ola");

		
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
		//System.out.println(TPMClient.atest("localhost", 4443, "localhost", 4443));
		
		//	}
	//	}	
	}

}
