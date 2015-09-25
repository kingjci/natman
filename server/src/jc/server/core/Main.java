package jc.server.core;

import jc.server.core.control.ControlTunnel;
import jc.server.core.registry.ControlConnectionRegistry;
import jc.server.core.registry.TunnelRegistry;
import static jc.server.core.Utils.Go;
public class Main {

    public static int connReadTimeout = 10*1000;
    public static Options options;
    public static Random random = new Random();
    public static TunnelRegistry tunnelRegistry = new TunnelRegistry();
    public static ControlConnectionRegistry controlConnectionRegistry = new ControlConnectionRegistry();

    public static void main(String[] args) {


        Go(new ControlTunnel(12345));



        System.out.println("hello world");
    }

}
