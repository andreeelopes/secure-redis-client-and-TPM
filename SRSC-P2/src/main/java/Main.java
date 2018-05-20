package main.java;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import client.Client;
import client.Pairs;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Client client=new Client();
		client.set("ola", "ola","what");
		client.set("ola2", "ola","what");
		List<List<Pairs>> result=client.get("ola", "what");
		for(int i=0;i<result.size();i++) {
		Iterator<Pairs> it=result.get(i).iterator();
		String fields= "";
		while(it.hasNext()) {
			Pairs p=it.next();
			System.out.println(p.getL()+": "+p.getR());
		}
		}
	}

}
