package jc.server.core;

import jc.server.core.connection.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ½ð³É on 2015/9/23.
 */
public class Pipe implements  Runnable{

        private Connection to;
        private Connection from;
        private CountDownLatch waitGroup;

        Pipe(Connection to, Connection from, CountDownLatch waitGroup){
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


            from.close();
            to.close();
            waitGroup.countDown();

        }catch (IOException e){
            e.printStackTrace();
        }


        }
}
