package jc.server.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class RandomTest {

    Random random = new Random();
    @Test
    public void testGetRandomString() throws Exception {
        String s = random.getRandomString(10);
        System.out.println(s);
    }

    @Test
    public void testGetRandomLong() throws Exception{
        long a = random.getRandomLong();
        System.out.println(a);
    }

    @Test
    public void testGetRandomInt() throws Exception{
        int a = random.getRandomInt();
        System.out.println(a);
    }
}