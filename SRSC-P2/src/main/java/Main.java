package main.java;

import java.util.Iterator;
import java.util.List;

import redis.client.SafeRedis;
import utils.Pair;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SafeRedis client = new SafeRedis();
		client.set("ola", "ola", "what");
		client.set("ola2", "ola", "what");
		List<List<Pair>> result = client.get("ola", "what");
		for (int i = 0; i < result.size(); i++) {
			Iterator<Pair> it = result.get(i).iterator();
			String fields = "";
			while (it.hasNext()) {
				Pair p = it.next();
				System.out.println(p.getKey() + ": " + p.getValue());
			}
		}	
	}

}
