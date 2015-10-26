package jc.server.core.PublicTunnel.HTTP;

import jc.TCPConnection;
import jc.server.core.ControlConnection.ControlConnection;
import jc.server.core.PublicTunnel.TCP.PublicTCPTunnelHandler;

/**
 * Created by 金成 on 2015/10/25.
 */
public class PublicHTTPTunnelHandler extends PublicTCPTunnelHandler {

    public PublicHTTPTunnelHandler(
            ControlConnection controlConnection,
            TCPConnection publicTCPConnection,
            String publicUrl
    ){
        super(controlConnection, publicTCPConnection, publicUrl);
    }
}
