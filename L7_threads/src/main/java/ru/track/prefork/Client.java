package ru.track.prefork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.IOUtils;



public class Client {
    static Logger log = LoggerFactory.getLogger(Client.class);
    private Socket socket = null;
    private int port;
    private String host;

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
                System.out.println(new String (buf, 0 ,nRead));
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
                out.write(line.getBytes());
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
            reader.start();
            writer.start();
        }
        catch (IOException e)
        {
            throw  new RuntimeException(e);
        }


    }

    public static void main(String[] args) throws Exception {
        Client client = new Client(9000, "localhost");
        client.loop();
    }
}
