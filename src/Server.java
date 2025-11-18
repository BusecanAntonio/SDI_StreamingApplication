import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("Server pornit pe portul 5000.");

        List<Socket> clients = new ArrayList<>();

        while (true) {
            Socket client = server.accept();
            clients.add(client);
            System.out.println("Client conectat!");

            new Thread(() -> {
                try {
                    InputStream in = client.getInputStream();

                    while (true) {
                        // citim dimensiunea frame-ului
                        byte[] sizeArr = in.readNBytes(4);
                        if (sizeArr.length < 4) break;

                        int size = ((sizeArr[0] & 0xff) << 24) |
                                ((sizeArr[1] & 0xff) << 16) |
                                ((sizeArr[2] & 0xff) << 8) |
                                (sizeArr[3] & 0xff);

                        byte[] frame = in.readNBytes(size);

                        // relay catre toti clientii
                        for (Socket c : clients) {
                            if (c != client) {
                                OutputStream out = c.getOutputStream();
                                out.write(sizeArr);
                                out.write(frame);
                                out.flush();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
