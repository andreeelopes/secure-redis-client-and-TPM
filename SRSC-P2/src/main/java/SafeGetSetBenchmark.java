package main.java;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;

import TPM.client.TPMClient;
import redis.client.RedisClient;


public class SafeGetSetBenchmark {
	private static final int TOTAL_OPERATIONS = 100;

	public static void main(String[] args) throws UnknownHostException, IOException {

		long begin = Calendar.getInstance().getTimeInMillis();

		//if attestion ok do benchmark
		if(	new TPMClient("TPMClientConfig.json").attest("localhost", 4446, "localhost", 4443)) {

			//Jedis jedis = new Jedis("rediss://localhost", 6379);
			RedisClient jedis = new RedisClient();

			//jedis.connect();
		


			for (int n = 0; n <= TOTAL_OPERATIONS; n++) {

				jedis.setClient(n,"test", "test", "test", "test", "test");
				// System.out.println(jedis.get(key));
			}

			for (int n = 0; n <= TOTAL_OPERATIONS; n++) {

				jedis.removeClient(n);
				// System.out.println(jedis.get(key));
			}
			
			for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
				
				jedis.getClientID(Integer.toString(n));
				//System.out.println(jedis.get(key));
			}
			
			
			long elapsed = Calendar.getInstance().getTimeInMillis() - begin;

		

			System.out.println(((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " ops/s");	
		}


	}
}







