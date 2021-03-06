package benchmarkSafeRedis;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;

import TPM.client.TPMClient;
import redis.client.RedisClient;


public class SafeGetSetBenchmark {
	private static final int TOTAL_OPERATIONS = 100;

	public static void main(String[] args) throws UnknownHostException, IOException {
		String redisIP;
		int redisPort;
		String ipGOSTPM;
		int portGOSTPM; 
		String ipVMSTPM;
		int portVMSTPM;
		
		if(args.length < 6) {
			System.out.println("Usage: <redisIP> <redisPort> <ipGOSTPM> <portGOSTPM> <ipVMSTPM> <portVMSTPM>");
			return ;
		}
		else {
			redisIP = args[0];//172.17.0.2
			redisPort= Integer.parseInt(args[1]);
			ipGOSTPM = args[2];//4446
			portGOSTPM = Integer.parseInt(args[3]);
			ipVMSTPM = args[4];//4443
			portVMSTPM = Integer.parseInt(args[5]);

		}
		long begin = Calendar.getInstance().getTimeInMillis();

		//if attestion ok do benchmark
		if(	new TPMClient("TPMClientConfig.json").attest(ipGOSTPM, portGOSTPM, ipVMSTPM, portVMSTPM)) {

			//Jedis jedis = new Jedis("rediss://localhost", 6379);
			RedisClient jedis = new RedisClient(redisIP,redisPort);

			//jedis.connect();
		


			for (int n = 0; n <= TOTAL_OPERATIONS; n++) {

				jedis.setClient(n,"test", "test", "test", "test", "test");
				// System.out.println(jedis.get(key));
			}

			for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
				
				jedis.getClientID(Integer.toString(n));
				//System.out.println(jedis.get(key));
			}
			for (int n = 0; n <= TOTAL_OPERATIONS; n++) {

				jedis.removeClient(n);
				// System.out.println(jedis.get(key));
			}
			
			
			
			long elapsed = Calendar.getInstance().getTimeInMillis() - begin;

		

			System.out.println(((1000 * 3 * TOTAL_OPERATIONS) / elapsed) + " ops/s");	
		}


	}
}







