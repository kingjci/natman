package jc.server.core;

import jc.Random;
import jc.server.core.ControlConnection.ControlConnectionRegistry;
import jc.server.core.ControlTunnel.ControlTunnel;
import jc.server.core.PublicTunnel.PublicTunnelRegistry;

import static jc.Utils.Go;


public class Main {

    public static Random random = new Random();
    public static PublicTunnelRegistry publicTunnelRegistry = new PublicTunnelRegistry();
    public static ControlConnectionRegistry controlConnectionRegistry = new ControlConnectionRegistry();


    public static void main(String[] args) {

        ControlTunnel controlTunnel = new ControlTunnel(12345, publicTunnelRegistry,controlConnectionRegistry, random);
        Go(controlTunnel);

    }

}
