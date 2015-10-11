# NatMan - VPS tunnels to localhost
==================================

### "I want to expose some ports(e.g. web 80, mysql 3306, MS Remote desktop 3389) to the Internet securely behind a router with private address or behind firewall "
![](http://jincheng.link/natman.png)

##What is NatMan?
  NatMan is a reverse proxy that creates a secure tunnel from a Public Server(VPS) to
local computer behind router with private address or behind firewall. NatMan consists of two
part, the server and the client. It sounds like ngrok, of course it is, When I want to expose
other port to the Internet besides 80(web), ngrok has many limits . Well, the most important
reason is that the official auth website of ngrok is block in China.

##What can I do with NatMan?
- Expose any tcp(udp and http multiplexing is under development) port behind a NAT or firewall
  to the internet
- You can deploy the server of NatMan to your own server(VPS) easily
- NatMan is written in Java and it is a cross platform software. You can deploy the server on
  different operation system, Linux(Ubuntu, RedHat ...) Windows

##What is NatMan useful for?
- Temporarily sharing a website, mysql, that is only running on your development machine. Access
  to your computer behind Nat or firewall with Remote desktop or vnc.
- Running networked services on machines that are firewalled off from the internet

##Download and installing NatMan
- Just download server.jar and client.jar
- Run the server on vps 
```sh
    >>java -jar server.jar
```
- Run the client on your local computer
```sh
>>java -jar client.jar -localport 8080 -remoteport 80 -hostname abc.xyz
```
- More features is under development

##Contacts
Email:kingjci@sina.com
