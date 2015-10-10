package jc;


import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 金成 on 2015/9/8.
 */
public class Utils {

    public static void Go(Runnable runnable){

        Thread thread = new Thread(runnable);
        thread.start();

    }

    public static void Join(TCPConnection c1, TCPConnection c2){

        //这个程序会等到两个Connection的双向传输都传送结束才结束返回
        CountDownLatch waitGroup = new CountDownLatch(2);
        Go(new Pipe(c1, c2, waitGroup));
        Go(new Pipe(c2, c1, waitGroup));

        System.out.printf("[%s][Utils]Join %s with %s\n", timeStamp(),c1.Id(), c2.Id());

        try{
            waitGroup.await();
            c1.close();
            c2.close();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        System.out.printf("[%s][Utils]Separate %s with %s\n", timeStamp(), c1.Id(), c2.Id());

    }

    public static String timeStamp(long timeMillis){

        SimpleDateFormat timeFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return timeFormat.format(timeMillis);

    }

    public static String timeStamp(){
        return timeStamp(System.currentTimeMillis());
    }


}
