package jc.server.core;

import jc.Random;
import jc.server.core.ControlConnection.ControlConnectionRegistry;
import jc.server.core.Controller.Controller;
import jc.server.core.PublicTunnel.PublicTunnelRegistry;

import static jc.Utils.Go;


public class Main {

    public static Random random = new Random();
    public static PublicTunnelRegistry publicTunnelRegistry = new PublicTunnelRegistry();
    public static ControlConnectionRegistry controlConnectionRegistry = new ControlConnectionRegistry();


    public static void main(String[] args) {

        Controller controller = new Controller(12345, publicTunnelRegistry,controlConnectionRegistry, random);
        Go(controller);

    }

}
