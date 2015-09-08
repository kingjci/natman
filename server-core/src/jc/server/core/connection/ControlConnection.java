package jc.server.core.connection;

import jc.message.AuthRequest;
import jc.message.Message;
import jc.server.core.Main;
import jc.server.core.Tunnel;

import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by ��� on 2015/9/8.
 * �������ӱ�����Ҳ�Ǹ�Connection�����Ǳ�һ���Connection���ܶ�ö࣬����ʵ��
 * һ��socket��װ��Connection���ڰ�װ��ControlConnection
 */
public class ControlConnection {

    private AuthRequest authRequest;
    private Connection connection;
    private BlockingQueue<Message> out = new LinkedBlockingQueue<Message>();
    private BlockingQueue<Message> in = new LinkedBlockingDeque<Message>();
    private Date lastPing;
    private List<Tunnel> tunnels = new LinkedList<Tunnel>();
    private BlockingQueue<Connection> proxies = new LinkedBlockingQueue<Connection>();
    private String id;

    public ControlConnection(Connection connection, AuthRequest authRequest){

        this.authRequest = authRequest;
        this.connection = connection;
        this.out = new LinkedBlockingQueue<Message>();
        this.in = new LinkedBlockingQueue<Message>();
        this.proxies = new LinkedBlockingQueue<Connection>();
        this.lastPing = new Date(System.currentTimeMillis());

        this.id = authRequest.getClientId();
        if (this.id == null || "".equalsIgnoreCase(this.id)){
            this.id = Main.random.getRandomString(16);
        }

        this.connection.SetType("control");

    }

}
