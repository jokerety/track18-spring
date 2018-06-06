package ru.track.prefork;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.List;

public class Potok extends Thread {
    private static int order;
    private int position;
    private String Name;
    public Socket socket;
    private ConversationService  messages;
    private List<Potok> connections;
    private static Gson gson = new Gson();

    public Potok(Socket socket, ConversationService messages, List<Potok> connections) {
        this.socket = socket;
        this.messages = messages;
        this.connections = connections;
        setPosition(getOrder());
        inc();

        setName("Client" + "@[" + getPosition() + "]" + socket.getInetAddress() + ":" + socket.getPort());
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] buf = new byte[1024];
            int nameRead = inputStream.read(buf);
            setNameSender(new String (buf, 0 , nameRead));

            while (!isInterrupted()) {

                while (inputStream.available() == 0) {
                    sleep(200);
                }

                int nRead = inputStream.read(buf);
                if (nRead == -1) {
                    System.out.println("cannot read");
                    break;
                }

                Message message = gson.fromJson(new String(buf, 0, nRead), Message.class);
                messages.store(message);
            }
        } catch (InterruptedException e) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            connections.remove(this);
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

    public synchronized void setPosition(int number) {
        position = number;
    }
    public void setNameSender(String  name){Name = name;}
    public String getNameSender () {return Name;}
}

