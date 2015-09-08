package jc.server.core;

import jc.server.core.connection.TunnelRegistry;

public class Main {

    public static int connReadTimeout = 10*1000;
    public static Options options;
    public static Random random = new Random();
    public static TunnelRegistry tunnelRegistry = new TunnelRegistry();


    public static void main(String[] args) {
        System.out.println("hello world");
    }

}
