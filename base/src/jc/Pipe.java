package jc;

import jc.TCPConnection;
import org.apache.log4j.Logger;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ��� on 2015/9/23.
 */
public class Pipe implements  Runnable{

        private TCPConnection to;
        private TCPConnection from;
        private CountDownLatch waitGroup;
        private Logger runtimeLogger;

        public Pipe(TCPConnection to, TCPConnection from, CountDownLatch waitGroup, Logger runtimeLogger){
            this.to = to;
            this.from = from;
            this.waitGroup = waitGroup;
            this.runtimeLogger = runtimeLogger;
        }


    @Override
    public void run() {

        InputStream inputStream = null;
        OutputStream outputStream = null;



        try{

            inputStream = from.getInputStream();
            outputStream = to.getOutPutStream();

            int len;
            byte[] buffer = new byte[1024];

            while (  (len = inputStream.read(buffer)) != -1 ) {

                outputStream.write(buffer, 0, len);
                outputStream.flush();

            }

        }catch (SocketException e){
            //当pipe的一端被close而没有shutdown的时候会发生这个异常，运行到这里。这不算
            //是一个异常

        }catch (IOException e){
            //运行到这里真的发生了异常
            runtimeLogger.error(e.getMessage(),e);

        }finally {

            try{
                from.shutdownInput();
                to.shutdownOutput();
            }catch (IOException e){
                runtimeLogger.error(e.getMessage(),e);
            }

            waitGroup.countDown();
        }


    }
}
