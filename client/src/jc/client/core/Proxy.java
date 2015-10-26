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

public class Proxy extends Thread {

    private final String clientId;
    private final Config config;
    private final Random random;
    private final Option option;
    private final Logger runtimeLogger;
    private final Logger accessLogger;
    private final Map<String, PrivateTunnel> privateTunnels;


    public Proxy(Controller controller){
        clientId = controller.getClientId();
        config = controller.getConfig();
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
                        config.getServerAddress(),
                        config.getControlPort(),
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

        ProxyStart proxyStart;
        try{
            proxyStart =(ProxyStart) proxyTCPConnection.readMessage();
            runtimeLogger.debug(
                    String.format(
                            "New proxy connection[%s] from %s",
                            proxyTCPConnection.getConnectionId(),
                            proxyTCPConnection.getRemoteAddress()
                    )
            );
      }catch (IOException e){
            runtimeLogger.error(
                    String.format(
                            "Proxy connection[%s] is shutdown by server",
                            proxyTCPConnection.getConnectionId()
                    )
            );
            runtimeLogger.error(e.getMessage(),e);

            try{
                proxyTCPConnection.close();
            }catch (IOException ee){
                runtimeLogger.error(
                        String.format(
                                "Fail to close proxy connection[%s] from %s",
                                proxyTCPConnection.getConnectionId(),
                                proxyTCPConnection.getRemoteAddress()
                        )
                );
                runtimeLogger.error(ee.getMessage(),e);
            }

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

        Join(localTCPConnection, proxyTCPConnection, runtimeLogger);

    }
}
