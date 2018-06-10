package main.java;


import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Calendar;

import redis.clients.jedis.Jedis;

public class GetSetBenchmark {
    private static final int TOTAL_OPERATIONS = 1000;

    public static void main(String[] args) throws UnknownHostException, IOException {
        Jedis jedis = new Jedis("localhost", 6379);

        jedis.connect();
        jedis.flushAll();

        long begin = Calendar.getInstance().getTimeInMillis();

        for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
            String key = "11111111111111111111111111111111:11111111111111111111111111111111:11111111111111111111111111111111" + n;
            jedis.set(key, "22222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222" + n);
            // System.out.println(jedis.get(key));
        }
        for (int n = 0; n <= TOTAL_OPERATIONS; n++) {
            String key = "11111111111111111111111111111111:11111111111111111111111111111111:11111111111111111111111111111111" + n;
            jedis.get(key);
            //System.out.println(jedis.get(key));
        }

        long elapsed = Calendar.getInstance().getTimeInMillis() - begin;

        jedis.disconnect();

        System.out.println(((1000 * 2 * TOTAL_OPERATIONS) / elapsed) + " ops/s");
    }
}