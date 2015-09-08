package jc.server.core.connection;

import jc.message.*;
import jc.server.core.Main;
import jc.server.core.connection.Tunnel;
import jc.server.core.Utils;
import jc.server.core.Version;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

import static jc.server.core.Utils.*;

/**
 * Created by ��� on 2015/9/8.
 * �������ӱ�����Ҳ�Ǹ�Connection�����Ǳ�һ���Connection���ܶ�ö࣬����ʵ��
 * һ��socket��װ��Connection���ڰ�װ��ControlConnection
 */
public class ControlConnection {

    public static final long pingTimeoutInterval = 30*1000; // 30s

    private AuthRequest authRequest;
    private Connection connection;
    private BlockingQueue<Message> out = new LinkedBlockingQueue<Message>();
    private BlockingQueue<Message> in = new LinkedBlockingDeque<Message>();
    protected long lastPing;
    private List<Tunnel> tunnels = new LinkedList<Tunnel>();
    private BlockingQueue<Connection> proxies = new LinkedBlockingQueue<Connection>();
    private String id;

    public ControlConnection(Connection connection, AuthRequest authRequest){

        this.authRequest = authRequest;
        this.connection = connection;
        this.out = new LinkedBlockingQueue<Message>();
        this.in = new LinkedBlockingQueue<Message>();
        this.proxies = new LinkedBlockingQueue<Connection>();
        this.lastPing = System.currentTimeMillis();

        this.id = authRequest.getClientId();
        if (this.id == null || "".equalsIgnoreCase(this.id)){
            this.id = Main.random.getRandomString(16);
        }

        this.connection.SetType("control");
        if (authRequest.getVersion() != Version.Current){
            System.out.printf("Incompatible versions. Server %s, client %s.\n", Version.Major, authRequest.getVersion());
            return;
        }

        go(new Writer());

        try{
            Message message = new AuthResponse(Version.Current, Version.Minor, connection.Id());//�ͻ���clientId�ǿͻ��˵���������������socket��id
            out.put(message);
            message = new ProxyRequest();
            out.put(message);
        }catch (InterruptedException e){
            e.printStackTrace();
        }


    }

    public void registerTunnel(TunnelRequest tunnelRequest){

        try{

            System.out.println("Registering new tunnel");
            Tunnel tunnel = new Tunnel(tunnelRequest, this);
            if (tunnel == null){
                System.out.println("registerTunnel fail...");
                out.put((new TunnelResponse("registerTunnel fail...")));
            }

            tunnels.add(tunnel);
            TunnelResponse tunnelResponse = new TunnelResponse(tunnel.getUrl(), tunnelRequest.getProtocol(), tunnelRequest.getRequestId());
            out.put(tunnelResponse);

        }catch (InterruptedException e){
            e.printStackTrace();
        }



    }

    public Connection GetProxy(){


        Connection proxyConnection = null;
        try{

            for (int i = 0 ; i < 3 ; i++){

                proxyConnection = proxies.poll(0, TimeUnit.SECONDS );
                if (proxyConnection == null){
                    out.put(new ProxyRequest());
                    System.out.println("No proxy in pool, requesting proxy from control . . .");
                    Thread.sleep(10);
                }else {
                    break;
                }
            }

        }catch (InterruptedException e){
            e.printStackTrace();
        }

        if (proxyConnection == null){
            System.out.println("No proxy connections available, control is closing");
        }
        return proxyConnection;


    }



    private class Writer implements Runnable{
        @Override
        public void run() {

            try{

                OutputStream outputStream = connection.getSocket().getOutputStream();
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

                while (true){
                    try{
                        Message message = out.take();
                        objectOutputStream.writeObject(message);
                        objectOutputStream.flush();


                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }

            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    private class Manager implements Runnable{
        @Override
        public void run() {
            while (true){

                try {
                    Message message = in.take();
                    switch (message.getMessageType()){

                        case "RequestTunnel":

                            break;

                        case "PingRequest":

                            break;

                        default:

                            System.out.printf("receive unknown message from %s\n", connection.Id());




                    }

                }catch (InterruptedException e){
                    e.printStackTrace();
                }


            }

        }



    }

    private class Ticker implements Runnable{
        @Override
        public void run() {
            long interval = System.currentTimeMillis() - lastPing;
            if (interval > pingTimeoutInterval ){
                System.out.printf("Lost heartbeat:%s", connection.Id());
                //�رտ������ӵĴ���
            }
        }
    }

}
