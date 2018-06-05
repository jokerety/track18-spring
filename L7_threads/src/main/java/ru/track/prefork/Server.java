package ru.track.prefork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


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

    public Message(String data, long time, String senderName) {
        this.data = data;
        this.senderName = senderName;
        this.time = time;
    }

    public String getData() {
        return data;
    }

    public long getTime() {
        return time;
    }

    public String getSenderName() {
        return senderName;
    }
}

class Potok extends Thread {
    private static int order;
    private int position;
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


        try {

            for (Potok connection : connections) {
                if (!message.getSenderName().equals(connection.getName())) {
                    OutputStream outputStream = connection.socket.getOutputStream();
                    outputStream.write(message.getData().getBytes());
                }
            }
        } catch (IOException e) {
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
                while (inputStream.available() == 0) {
                    sleep(200);
                }
                int nRead = inputStream.read(buf);
                if (nRead == -1) {
                    System.out.println("cannot read");
                    break;
                }

                Message message = new Message(new String(buf, 0, nRead), new Date().getTime(), getName());
                //messages.add(message);
                broadcast(message);
            }
        } catch (InterruptedException e) {

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(socket);

        }

    }

    public int getOrder() {
        return order;
    }

    public synchronized void inc() {
        order++;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int number) {
        position = number;
    }
}

public class Server {
    static Logger log = LoggerFactory.getLogger(Server.class);

    List<Potok> list = new ArrayList<Potok>();
    List<Message> messages = new ArrayList<Message>();
    private int port;
    Thread reader = new Thread(() -> read());

    public Server(int port) {
        this.port = port;
    }

    public void read() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine();
            StringTokenizer tokenizer = new StringTokenizer(line);
            String firstArg = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
            String secondArg = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";
            if ("list".equals(firstArg)) {
                for (Potok potok : list) {
                    System.out.println(potok.getName());
                }
                if (list.isEmpty())
                {
                    System.out.println("Empty");
                }
             }

            if ("drop".equals(firstArg)) {
                try {
                    int id = Integer.parseInt(secondArg);
                    Iterator<Potok> it = list.iterator();
                    while (it.hasNext())
                    {
                        Potok potok = it.next();
                        if (potok.getPosition() == id) {
                            System.out.println("removed:" + potok.getName());
                            potok.interrupt();
                            it.remove();
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Not a number");
                }
            }
        }
    }

    public void serve() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        reader.start();
        try {
            serverSocket = new ServerSocket(port, 10, InetAddress.getByName("localhost"));
            log.info("broadcasting");
            while (true) {
                socket = serverSocket.accept();
                Potok potok = new Potok(socket, messages, list);
                potok.setName("Client" + "@[" + potok.getOrder() + "]" + socket.getInetAddress() + ":" + socket.getPort());
                potok.setPosition(potok.getOrder());
                System.out.println("Connected new " + potok.getName() + ">Hello!");
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
