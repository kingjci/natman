package jc.server.core.connection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by ½ð³É on 2015/9/4.
 */
public class ProxyConnection implements Runnable{

    private ServerSocket serverSocket;
    PipedOutputStream proxy2publicPipe;
    PipedInputStream public2proxyPipe;
    private final int BUFSIZE = 1024;


    public ProxyConnection(int proxyPort,
                           PipedOutputStream proxy2publicPipe,
                           PipedInputStream public2proxyPipe){
        try{
            this.serverSocket = new ServerSocket(proxyPort);
            this.proxy2publicPipe = proxy2publicPipe;
            this.public2proxyPipe = public2proxyPipe;
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while (true){
            try {
                Socket proxySocket = serverSocket.accept();
                InputStream proxySocketInputStream = proxySocket.getInputStream();
                OutputStream proxySocketOutputStream = proxySocket.getOutputStream();
                byte[] public2proxyBytes = new byte[BUFSIZE];
                byte[] proxy2publicBytes = new byte[BUFSIZE];
                int proxySocketInputStreamCount = 1, proxySocketOutputStreamCount = 1;
                while (true){
                    proxySocketInputStreamCount = proxySocketInputStream.read(proxy2publicBytes);
                    proxySocketOutputStreamCount = public2proxyPipe.read(proxy2publicBytes);
                    if (proxySocketInputStreamCount > 0){
                        proxy2publicPipe.write(proxy2publicBytes);
                        proxy2publicPipe.flush();
                    }
                    if (proxySocketOutputStreamCount > 0){
                        proxySocketOutputStream.write(proxy2publicBytes);
                        proxySocketOutputStream.flush();
                    }

                    if (proxySocketInputStreamCount == -1 && proxySocketOutputStreamCount == -1){
                        break;
                    }

                }

            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
