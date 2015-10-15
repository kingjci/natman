package jc.client.core;

import jc.Random;
import jc.TCPConnection;
import jc.message.ProxyResponse;
import jc.message.ProxyStart;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

import static jc.Utils.Dial;
import static jc.Utils.Join;

public class Proxy implements Runnable {

    private final String clientId;
    private final String serverAddress;
    private final Random random;
    private final Option option;
    private final Logger runtimeLogger;
    private final Logger accessLogger;
    private final Map<String, PrivateTunnel> privateTunnels;


    public Proxy(Controller controller){
        clientId = controller.getClientId();
        serverAddress = controller.getServerAddress();
        random = controller.getRandom();
        option = controller.getOption();
        runtimeLogger = controller.getRuntimeLogger();
        accessLogger = controller.getAccessLogger();
        privateTunnels = controller.getPrivateTunnels();
    }


    @Override
    public void run() {

        TCPConnection proxyTCPConnection =
                Dial(
                        serverAddress,
                        option.getControlPort(),
                        "proxy",
                        random.getRandomString(8),
                        runtimeLogger,
                        accessLogger
                );

        try{
            proxyTCPConnection.writeMessage(new ProxyResponse(clientId));
        }catch (IOException e){
            runtimeLogger.error(
                    String.format("Send ProxyResponse to %s failure", proxyTCPConnection.getRemoteAddress())
            );
            runtimeLogger.error(e.getMessage(), e);
            return;
        }

        ProxyStart proxyStart = null;
        try{
            proxyStart =(ProxyStart) proxyTCPConnection.readMessage();
            runtimeLogger.info(
                    String.format(
                            "New proxy connection[%s] from %s",
                            proxyTCPConnection.getConnectionId(),
                            proxyTCPConnection.getRemoteAddress()
                    )
            );
      }catch (IOException e){
            proxyTCPConnection.close();
            runtimeLogger.error(
                    String.format(
                            "Proxy connection[%s] is shutdown by server",
                            proxyTCPConnection.getConnectionId()
                    )
            );
            runtimeLogger.error(e.getMessage(),e);
            return;
        }

        PrivateTunnel privateTunnel = privateTunnels.get(proxyStart.getUrl());

        if (privateTunnel == null){
            runtimeLogger.error(
                    String.format(
                            "Couldn't find tunnel for proxy: %s",
                            proxyStart.getUrl()
                    )
            );

            return;
        }

        TCPConnection localTCPConnection =
                Dial(
                        privateTunnel.getLocalAddress(),
                        privateTunnel.getLocalPort(),
                        "private", random.getRandomString(8),
                        runtimeLogger,
                        accessLogger
                );
        if (localTCPConnection == null){
            runtimeLogger.error(
                    String.format(
                            "Fail to open private connection %s: %s",
                            privateTunnel.getLocalAddress() ,
                            privateTunnel.getLocalPort()
                    )
            );
            return;
        }

        Join(localTCPConnection, proxyTCPConnection);

    }
}
