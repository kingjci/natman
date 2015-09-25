package jc;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class Random {

    private java.util.Random random;
    private char[] chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public Random(long seed){
        random = new  java.util.Random(seed);
    }

    public Random(){
        this(System.currentTimeMillis());
    }

    public String getRandomString(int length){
        StringBuffer stringBuffer = new StringBuffer();
        for(int i = 0 ; i < length; ++i){
            stringBuffer.append(chars[random.nextInt(62)]);
        }
        return stringBuffer.toString();
    }

    public long getRandomLong(){
        return random.nextLong();
    }

    public int getRandomInt(){
        return  random.nextInt();
    }



}
