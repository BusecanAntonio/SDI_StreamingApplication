import java.io.*;
import java.net.*;
import java.util.*;

public class ControlServer {

    private static final Set<String> activeStreamers =
            Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(6000);
        System.out.println("ControlServer pornit pe portul 6000.");

        while (true) {
            Socket client = server.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private static void handleClient(Socket client) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
             PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

            String line = in.readLine();
            if (line == null) return;

            if (line.startsWith("STREAMER ")) {
                String name = line.substring(9).trim();
                activeStreamers.add(name);
                System.out.println("Streamer activ: " + name);
                out.println("OK");
            }
            else if (line.equals("LIST")) {
                // trimite lista
                String response = String.join(",", activeStreamers);
                out.println(response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
