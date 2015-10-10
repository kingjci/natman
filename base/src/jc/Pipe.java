package jc;

import jc.TCPConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by 金成 on 2015/9/23.
 */
public class Pipe implements  Runnable{

        private TCPConnection to;
        private TCPConnection from;
        private CountDownLatch waitGroup;

        public Pipe(TCPConnection to, TCPConnection from, CountDownLatch waitGroup){
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

            int len;
            byte[] buffer = new byte[1024];

            while (  (len = inputStream.read(buffer)) != -1 ) {

                outputStream.write(buffer, 0, len);
                outputStream.flush();

            }

        }catch (SocketException e){
            //当浏览器的socket被浏览器关闭的时候，可能会进入到这里

        }catch (IOException e){

            e.printStackTrace();

        }finally {

            try{
                from.getSocket().shutdownInput();
                to.getSocket().shutdownOutput();
            }catch (IOException e){
                e.printStackTrace();
            }

            waitGroup.countDown();
        }


    }
}
