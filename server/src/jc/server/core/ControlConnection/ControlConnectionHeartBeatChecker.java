package jc.server.core.ControlConnection;

import jc.TCPConnection;
import jc.Time;

import java.util.TimerTask;

import static jc.Utils.timeStamp;

/**
 * Created by 金成 on 2015/10/10.
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
            tcpConnection.close();//强制关闭这个control connection里面的socket，这样
            //control connection 里面的主循环会被强制退出，退出线程
            //准备关闭控制连接
        }
    }
}