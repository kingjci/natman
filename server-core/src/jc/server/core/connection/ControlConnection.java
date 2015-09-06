package jc.server.core.connection;

import javax.naming.ldap.Control;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.Pipe;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by 金成 on 2015/9/4.
 */
public class ControlConnection implements Runnable{

    private ServerSocket serverSocket;
    private Map<String, String> users;
    private Set<String> authorisedUsers;
    private Map<Integer, Integer> publicPortMap;

    public ControlConnection(Map<String, String> users, Set<String> authorisedUsers, int port, Map<Integer, Integer> publicPortMap){
        try{
            this.users = users;
            this.serverSocket = new ServerSocket(port);
            this.authorisedUsers = authorisedUsers;
            this.publicPortMap = publicPortMap;
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public ControlConnection(Map<String, String> users, Set<String> authorisedUsers, Map<Integer, Integer> publicPortMap){
        this(users, authorisedUsers, 4000, publicPortMap); //默认使用4000端口监听控制
    }

    @Override
    public void run() {
        while (true){

            try{
                Socket socket = serverSocket.accept();
                System.out.println(String.format("success:%s connect to control connection", socket.getInetAddress()));
                Thread controlThread = new Thread(new ControlConnectionHandler(socket));
                controlThread.start();
            }catch (SocketException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }


        }
    }

    private class ControlConnectionHandler implements Runnable{

        private Socket socket;
        private InputStreamReader inputStreamReader;
        private OutputStreamWriter outputStreamWriter;
        private BufferedReader inputBufferedReader;
        private BufferedWriter outputBufferWriter;

        public ControlConnectionHandler(Socket socket){
            try{

                this.socket = socket;
                this.inputStreamReader = new InputStreamReader(socket.getInputStream());
                this.outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
                this.inputBufferedReader = new BufferedReader(inputStreamReader);
                this.outputBufferWriter = new BufferedWriter(outputStreamWriter);

            }catch (IOException e){
                e.printStackTrace();
            }

        }

        @Override
        public void run(){
            try{
                String receive;
                while ((receive = inputBufferedReader.readLine()) != null){

                    String[] parameters = receive.split("\\s+");
                    String command = parameters[0];
                    switch (command){
                        //auth aaa bbb
                        case "auth":
                            boolean authorised = users.containsKey(parameters[1]) &&
                                    parameters[2].equalsIgnoreCase(users.get(parameters[1]));
                            if (authorised){
                                authorisedUsers.add(parameters[1]);
                                outputBufferWriter.write("auth success\n");
                                outputBufferWriter.flush();
                                System.out.println(String.format("success:auth %s", parameters[1]));
                            }else {
                                outputBufferWriter.write(String.format("failure:auth %s\n", parameters[1]));
                                outputBufferWriter.flush();
                                System.out.println(String.format("failure:auth %s", parameters[1]));
                            }
                            break;
                        //start aaa 8080(服务器上端口)
                        case "start":
                            int publicPortNumber = Integer.valueOf(parameters[2]);
                            int proxyPortNumber = 9000 + publicPortMap.size();
                            if (authorisedUsers.contains(parameters[1])){
                                if (!publicPortMap.containsKey(Integer.valueOf(parameters[2]))){
                                    publicPortMap.put(publicPortNumber, proxyPortNumber);
                                    //System.out.println(String.format("Server port mapping:%d -> %d", publicPortNumber, proxyPortNumber));
                                    PipedOutputStream public2proxyPipeOutputStream = new PipedOutputStream();
                                    PipedInputStream public2ProxyPipeInputStream = new PipedInputStream(public2proxyPipeOutputStream);

                                    PipedOutputStream proxy2publicPipedOutputStream = new PipedOutputStream();
                                    PipedInputStream proxy2publicPipeInputStream = new PipedInputStream(proxy2publicPipedOutputStream);

                                    //公共端口，由用户指定
                                    Thread publicConnection = new Thread(new PublicConnection(publicPortNumber, public2proxyPipeOutputStream, proxy2publicPipeInputStream));
                                    publicConnection.start();
                                    System.out.println(String.format("success:public connection on port %d", publicPortNumber));
                                    //服务器上的代理端口，从9000开始依次增加，
                                    Thread proxyConnection = new Thread(new ProxyConnection(proxyPortNumber, proxy2publicPipedOutputStream, public2ProxyPipeInputStream));
                                    proxyConnection.start();
                                    System.out.println(String.format("success:proxy connection on port %d", proxyPortNumber));
                                    outputBufferWriter.write(String.format("success:start %d->%d\n", publicPortNumber, proxyPortNumber));
                                    outputBufferWriter.flush();
                                    System.out.println(String.format("success:start %d->%d", publicPortNumber, proxyPortNumber));
                                }else {
                                    outputBufferWriter.write(String.format("failure:start %d->%d, publicPort %d is already bind\n", publicPortNumber, proxyPortNumber,publicPortNumber));
                                    System.out.println(String.format("failure:start %d->%d, publicPort %d is already bind", publicPortNumber, proxyPortNumber, publicPortNumber));
                                }
                            }
                            break;

                        default:
                            System.out.println("");
                }
            }

        }catch (SocketException e){
            System.out.println(String.format("Connection reset by %s", socket.getInetAddress()));
           // e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }


        }
    }

}
