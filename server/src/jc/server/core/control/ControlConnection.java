package jc.server.core.control;


import jc.message.*;
import jc.server.core.PublicTunnel;
import jc.Version;
import jc.Connection;

import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static jc.server.core.Main.random;
import static jc.server.core.Main.controlConnectionRegistry;

public class ControlConnection{

    private AuthRequest authRequest;

    private Connection connection;

    protected long lastPing;

    private List<PublicTunnel> publicTunnels = new LinkedList<PublicTunnel>();

    private BlockingQueue<Connection> proxies = new LinkedBlockingQueue<Connection>();

    private String id;

    ControlConnection(Socket socket, AuthRequest authRequest){

        this.connection = new Connection(socket, "control");

        lastPing = System.currentTimeMillis();

        this.id = authRequest.getClientId();
        if ("".equalsIgnoreCase(this.id)){
            this.id = random.getRandomString(16);
        }

        connection.setType("control");

        if (authRequest.getVersion() < 0){
            System.out.println("Incompatible versions. Server %s, client %s. Download a new version");
            AuthResponse authResponse = new AuthResponse("Incompatible versions");
            connection.writeMessage(authResponse);
            connection.close();
        }

        if (controlConnectionRegistry.has(authRequest.getClientId())){
            ControlConnection controlConnection = controlConnectionRegistry.get(authRequest.getClientId());
            controlConnection.close();
        }

        controlConnectionRegistry.register(this.id, this);


        AuthResponse authResponse = new AuthResponse(Version.Current, this.id);
        connection.writeMessage(authResponse);






    }

    public void bindTunnel(TunnelRequest tunnelRequest){

        //controlConnection.bindTunnel->new Tunnel->tunnelRegistry.register
        //最终通过在controlConnection中调用bindTunnel实现新建一个tunnel并注册的功能

        PublicTunnel publicTunnel = new PublicTunnel(tunnelRequest, this);
        TunnelResponse tunnelResponse = new TunnelResponse(publicTunnel.getUrl(), tunnelRequest.getProtocol(), tunnelRequest.getRequestId());
        connection.writeMessage(tunnelResponse);
        publicTunnels.add(publicTunnel);
    }

    public Connection getProxy(){

        Connection proxyConnection = null;

        for (int i = 0 ; i < 5 ; i++){

            if (proxies.size() == 0){
                System.out.println("No proxy in pool, requesting proxy from control . . .");
                ProxyRequest proxyRequest = new ProxyRequest();
                connection.writeMessage(proxyRequest);
            }

            try{
                proxyConnection = proxies.poll(3l, TimeUnit.MILLISECONDS);
            }catch (InterruptedException e){
                e.printStackTrace();
            }

            if (proxyConnection != null){
                break;
            }

        }

        if (proxyConnection == null){
            System.out.println("No proxy connections available, control is closing");
        }

        return proxyConnection;

    }


    public void close(){

        connection.close();
    }







}