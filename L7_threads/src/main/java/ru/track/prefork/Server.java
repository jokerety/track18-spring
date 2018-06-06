package ru.track.prefork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


import com.google.gson.Gson;
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

public class Server {

    static Logger log = LoggerFactory.getLogger(Server.class);
    private List<Potok> list = Collections.synchronizedList(new ArrayList<>()) ;
    private ConversationService messages = new Database();
    private static Gson gson = new Gson();
    private int port;

    Thread reader = new Thread(() -> read());
    Thread broadcast = new Thread(() -> broadcast());

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
                if (list.isEmpty()) {
                    System.out.println("Empty");
                }
            }

            if ("drop".equals(firstArg)) {
                try {
                    int id = Integer.parseInt(secondArg);
                    Iterator<Potok> it = list.iterator();
                    while (it.hasNext()) {
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

    public void broadcast() {
        try {
            long lastUpdate = System.currentTimeMillis();

            while (!Thread.interrupted())
            {
                long nowUpdate = System.currentTimeMillis();
                List<Message> broadcastMessages = messages.getHistory(lastUpdate, nowUpdate, 100);
                synchronized (list) {
                    for (Potok connection : list) {
                        for (Message message : broadcastMessages)
                        {
                            OutputStream outputStream = connection.socket.getOutputStream();
                            if (!message.getSenderName().equals(connection.getNameSender())) {
                                outputStream.write(gson.toJson(message).getBytes());
                            }
                        }
                    }
                }

                lastUpdate = nowUpdate;
                Thread.sleep(200);
            }
        } catch (IOException | InterruptedException  e) {
            System.out.println("cannot broadcast");
        }
    }

    public void serve() {
        ServerSocket serverSocket;
        Socket socket = null;

        reader.start();
        broadcast.start();

        try {
            serverSocket = new ServerSocket(port, 10, InetAddress.getByName("localhost"));
            log.info("broadcasting");
            while (true) {
                socket = serverSocket.accept();

                Potok potok = new Potok(socket, messages, list);
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
