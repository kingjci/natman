package jc.server.core.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ½ð³É on 2015/9/8.
 */
public class Pipe implements Runnable {

    private Connection from;
    private Connection to;
    private CountDownLatch waitGroup;

    public Pipe(Connection to, Connection from, CountDownLatch waitGroup){
        this.to = to;
        this.from = from;
        this.waitGroup = waitGroup;

    }


    @Override
    public void run() {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try{
            inputStream = this.from.getSocket().getInputStream();
            outputStream = this.to.getSocket().getOutputStream();

            int len = 0;
            byte[] buffer = new byte[1024];
            while (len != -1) {

                    len = inputStream.read(buffer);
                    outputStream.write(buffer, 0, len);
                    outputStream.flush();
            }
            inputStream.close();
            outputStream.close();
        }catch (IOException e){
            e.printStackTrace();
        }

        from.Close();
        to.Close();
        waitGroup.countDown();
    }
}
