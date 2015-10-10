package jc.server.core.ControlConnection;

import jc.TCPConnection;
import jc.Time;

import java.util.TimerTask;

import static jc.Utils.timeStamp;

/**
 * Created by ��� on 2015/10/10.
 */
public class ControlConnectionHeartBeatChecker extends TimerTask {

    private Time lastPing;
    private TCPConnection tcpConnection;

    public ControlConnectionHeartBeatChecker(TCPConnection tcpConnection, Time lastPing){
        this.tcpConnection = tcpConnection;
        this.lastPing = lastPing;
    }

    @Override
    public void run() {
        long currentTimeMillis = System.currentTimeMillis();
        long diff = currentTimeMillis - lastPing.getTime();
        if (diff > 30*1000){
            System.out.printf("[%s][ControlConnection]Lost heartbeat\n", timeStamp());
            tcpConnection.close();//ǿ�ƹر����control connection�����socket������
            //control connection �������ѭ���ᱻǿ���˳����˳��߳�
            //׼���رտ�������
        }
    }
}