package jc.server.core;

import jc.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ��� on 2015/9/23.
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

            while ((len = inputStream.read(buffer)) != -1) {

                outputStream.write(buffer, 0, len);
                outputStream.flush();

            }

            //from.getSocket().shutdownInput();
            to.getSocket().shutdownOutput();


        }catch (IOException e){
            e.printStackTrace();
        }finally {

            waitGroup.countDown();
        }


    }
}
