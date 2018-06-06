package ru.track.prefork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;

import static java.lang.System.currentTimeMillis;


public class Client {
    static Logger log = LoggerFactory.getLogger(Client.class);
    private Socket socket = null;
    private int port;
    private String Name;
    private String host;
    private static Gson gson = new Gson();

    Thread writer = new Thread(() -> write());
    Thread reader = new Thread(() -> read());

    public Client(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void write()
    {
        try{
            final InputStream in = socket.getInputStream();
            byte[] buf = new byte[1024];
            while (true)
            {
                int nRead = in.read(buf);
                if (nRead == -1)
                {
                    break;
                }
                Message message = gson.fromJson(new String (buf,0,nRead), Message.class);
                System.out.println(message.getSenderName() + ":" + message.getData());
            }
        }
        catch (IOException e)
        {
            System.out.println("Server is not available");
        }
        finally {
            IOUtils.closeQuietly(socket);
        }

    }
    public void read()
    {
        try{
            final OutputStream out = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);

            while (true)
            {
                String line = scanner.nextLine();
                if ("exit".equals(line)) {
                    break;
                }

                Message msg = new Message(line, System.currentTimeMillis(), getName());
                String message = gson.toJson(msg);
                out.write(message.getBytes());
                out.flush();
            }
        }
        catch (IOException e)
        {
            System.out.println("Server is not available");
        }
        finally {
            IOUtils.closeQuietly(socket);
        }
    }

    public void loop() {

        try{
            socket = new Socket(host, port);
            final OutputStream out = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);

            System.out.println("Введите имя пользователя");
            String name = scanner.nextLine();
            setName(name);
            out.write(name.getBytes());
            out.flush();
            reader.start();
            writer.start();
        }
        catch (IOException e)
        {
            throw  new RuntimeException(e);
        }


    }
    public void setName (String name) {Name = name;}
    public String getName () { return Name;}
    public static void main(String[] args) throws Exception {
        Client client = new Client(9000, "localhost");
        client.loop();
    }
}
