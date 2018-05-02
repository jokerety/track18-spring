package ru.track.prefork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * - multithreaded +
 * - atomic counter +
 * - setName() +
 * - thread -> Worker +
 * - save threads
 * - broadcast (fail-safe)
 */
class Message {
    private String data;
    private long time;
    private String senderName;

    public Message(String data, long time, String senderName)
    {
        this.data = data;
        this.senderName = senderName;
        this.time = time;
    }
    public String getData()
    {
        return data;
    }
    public long getTime()
    {
        return time;
    }
    public String getSenderName()
    {
        return senderName;
    }
}

class Potok extends Thread {
    private static int order;
    public Socket socket;
    public List<Message> messages;
    public List<Potok> connections;
    public Potok(Socket socket, List<Message> messages, List<Potok> connections) {
        this.socket = socket;
        this.messages = messages;
        this.connections = connections;
    }
    public void broadcast(Message message) {
       //for (Message message : messages)


                try{

                    for (Potok connection : connections)
                    {
                        if (!message.getSenderName().equals(connection.getName()))
                        {
                        OutputStream outputStream = connection.socket.getOutputStream();
                        outputStream.write(message.getData().getBytes());
                        }
                    }
                    }

                catch(IOException e)
                {
                    System.out.println("cannot broadcast");
                }



    }
    @Override
    public void run() {

        try {
            while (!isInterrupted()) {
                inc();
                InputStream inputStream = socket.getInputStream();

                byte[] buf = new byte[1024];
                if (inputStream.available() != 0) {
                    sleep(200);
                }
                int nRead = inputStream.read(buf);
                if (nRead == -1) {
                    System.out.println("cannot read");
                    break;
                }
                System.out.println(new String(buf, 0, nRead));
                Message message = new Message(new String(buf, 0, nRead), new Date().getTime(), getName());
               //messages.add(message);
                broadcast(message);
            }
        } catch (InterruptedException e) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            interrupt();
        }

    }

    public int getOrder() {
        return order;
    }

    public synchronized void inc() {
        order++;
    }
}

public class Server {
    static Logger log = LoggerFactory.getLogger(Server.class);

    List<Potok> list = new ArrayList<Potok>();
    List<Message> messages = new ArrayList<Message>();
    private int port;

    public Server(int port) {
        this.port = port;
    }



    public void serve() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(port, 10, InetAddress.getByName("localhost"));
            log.info("on select...");
            while (true) {
                socket = serverSocket.accept();
                Potok potok = new Potok(socket, messages, list);
                potok.setName("Client" + "[" + potok.getOrder() + "]@" + socket.getInetAddress() + ":" + socket.getPort());
                System.out.println(potok.getName());
                list.add(potok);
                potok.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(socket);
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(9000);
        server.serve();
    }
}
