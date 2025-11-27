import java.io.*;
import java.net.*;
import java.util.*;

public class VideoServer {

    static class ViewerConnection {
        String streamerName;
        Socket socket;

        ViewerConnection(String s, Socket sock) {
            streamerName = s;
            socket = sock;
        }
    }

    static class StreamerConnection {
        String name;
        Socket socket;
        List<ViewerConnection> viewers = new ArrayList<>();
    }

    private static final Map<String, StreamerConnection> streamers =
            Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket(5000);
        System.out.println("VideoServer pornit pe portul 5000.");

        while (true) {
            Socket client = server.accept();
            new Thread(() -> handleClient(client)).start();
        }
    }

    private static void handleClient(Socket client) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            String firstLine = in.readLine(); // prima linie este identificația

            if (firstLine.startsWith("STREAMER ")) {
                handleStreamer(client, firstLine.substring(9).trim());
            }
            else if (firstLine.startsWith("VIEWER ")) {
                handleViewer(client, firstLine.substring(7).trim());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void handleStreamer(Socket socket, String name) throws Exception {
        System.out.println("Streamer conectat: " + name);

        StreamerConnection sc = new StreamerConnection();
        sc.name = name;
        sc.socket = socket;
        streamers.put(name, sc);

        InputStream in = socket.getInputStream();

        while (true) {
            byte[] sizeArr = in.readNBytes(4);
            if (sizeArr.length < 4) break;

            int size = ((sizeArr[0] & 0xff) << 24) |
                    ((sizeArr[1] & 0xff) << 16) |
                    ((sizeArr[2] & 0xff) << 8) |
                    (sizeArr[3] & 0xff);

            byte[] frame = in.readNBytes(size);

            synchronized (sc.viewers) {
                for (ViewerConnection vc : sc.viewers) {
                    try {
                        OutputStream out = vc.socket.getOutputStream();
                        out.write(sizeArr);
                        out.write(frame);
                        out.flush();
                    } catch (Exception ex) {
                        // viewer deconectat
                        sc.viewers.remove(vc);
                    }
                }
            }
        }

        streamers.remove(name);
        socket.close();
        System.out.println("Streamer deconectat: " + name);
    }

    private static void handleViewer(Socket socket, String streamerName) throws Exception {
        System.out.println("Viewer vrea streamerul: " + streamerName);

        StreamerConnection sc = streamers.get(streamerName);
        if (sc == null) {
            System.out.println("Streamer nu exista!");
            socket.close();
            return;
        }

        synchronized (sc.viewers) {
            sc.viewers.add(new ViewerConnection(streamerName, socket));
        }

        // viewerul nu trimite nimic, doar sta
        while (!socket.isClosed()) {
            Thread.sleep(1000);
        }
    }
}
