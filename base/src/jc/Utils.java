package jc;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ��� on 2015/9/8.
 */
public class Utils {

    public static void Go(Runnable runnable){

        Thread thread = new Thread(runnable);
        thread.start();

    }

    public static void Join(TCPConnection c1, TCPConnection c2){

        //��������ȵ�����Connection��˫���䶼���ͽ����Ž�������
        CountDownLatch waitGroup = new CountDownLatch(2);
        Go(new Pipe(c1, c2, waitGroup));
        Go(new Pipe(c2, c1, waitGroup));

        //System.out.printf("[%s][Utils]Join %s with %s\n", timeStamp(),c1.Id(), c2.Id());

        try{
            waitGroup.await();
            c1.close();
            c2.close();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        //System.out.printf("[%s][Utils]Separate %s with %s\n", timeStamp(), c1.Id(), c2.Id());

    }

    public static TCPConnection Dial(String host, int port, String type, String connectionId){

        Socket socket = null;
        TCPConnection TCPConnection = null;
        try{
            socket = new Socket();
            SocketAddress address = new InetSocketAddress(host, port);
            //���Ӷ˿ڣ�3���ӳ�ʱ�������ӱ��ض˿ڵ�ʱ������˿�û�п��Ż�����쳣connect refused
            socket.connect(address, 3*1000);
            TCPConnection = new TCPConnection(socket, type, connectionId);
            //System.out.printf("[%s][Utils]New %s connection[%s] to: %s\n",
                    //timeStamp(), TCPConnection.getType(), TCPConnection.getConnectionId(), TCPConnection.getRemoteAddr());

        }catch (IOException e){
            System.out.printf("[%s][Utils]can not connect to %s:%d, perhaps this port is not open\n", timeStamp(), host, port);
            e.printStackTrace();
        }
        return TCPConnection;

    }

    public static String timeStamp(long timeMillis){

        SimpleDateFormat timeFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return timeFormat.format(timeMillis);

    }

    public static String timeStamp(){
        return timeStamp(System.currentTimeMillis());
    }


}
