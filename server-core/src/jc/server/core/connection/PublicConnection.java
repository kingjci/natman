package jc.server.core.connection;

import org.omg.CORBA_2_3.portable.*;

import javax.jws.soap.SOAPBinding;
import java.io.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ½ð³É on 2015/9/4.
 */
public class PublicConnection implements Runnable{

    private ServerSocket serverSocket;
    PipedOutputStream public2proxyPipe;
    PipedInputStream proxy2publicPipe;
    private final int BUFSIZE = 1024;

    public PublicConnection(int publicPort,
                            PipedOutputStream public2proxy,
                            PipedInputStream proxy2public){

        try{
            this.serverSocket = new ServerSocket(publicPort);
            this.public2proxyPipe = public2proxy;
            this.proxy2publicPipe = proxy2public;
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    @Override
    public void run() {

        while (true){
            try {
                Socket publicSocket = serverSocket.accept();
                InputStream publicSocketInputStream = publicSocket.getInputStream();
                OutputStream publicSocketOutputStream = publicSocket.getOutputStream();
                byte[] public2proxyBytes = new byte[BUFSIZE];
                byte[] proxy2publicBytes = new byte[BUFSIZE];
                int publicSocketInputStreamCount = 1, publicSocketOutputStreamCount = 1;
                while (true){
                    publicSocketInputStreamCount = publicSocketInputStream.read(public2proxyBytes);
                    publicSocketOutputStreamCount = proxy2publicPipe.read(proxy2publicBytes);
                    if (publicSocketInputStreamCount > 0){
                        public2proxyPipe.write(public2proxyBytes);
                        public2proxyPipe.flush();
                    }
                    if (publicSocketOutputStreamCount > 0){
                        publicSocketOutputStream.write(proxy2publicBytes);
                        publicSocketOutputStream.flush();
                    }

                    if (publicSocketInputStreamCount == -1 && publicSocketOutputStreamCount == -1){
                        break;
                    }

                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }


    }
}
