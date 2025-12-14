import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class NetworkDiscovery {

    private static final int DISCOVERY_PORT = 9999;
    private static final String DISCOVERY_REQUEST = "STREAM_SERVER_REQUEST";
    private static final String DISCOVERY_RESPONSE = "STREAM_SERVER_RESPONSE";

    public static String findServerIp() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);

            byte[] requestData = DISCOVERY_REQUEST.getBytes();

            // Trimite broadcast pe toate interfetele de retea
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    try {
                        DatagramPacket sendPacket = new DatagramPacket(requestData, requestData.length, broadcast, DISCOVERY_PORT);
                        socket.send(sendPacket);
                    } catch (Exception e) {
                        // Ignora interfetele care nu suporta broadcast
                    }
                }
            }

            // Asteapta raspuns
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.setSoTimeout(2000); // Asteapta maxim 2 secunde
            socket.receive(receivePacket);

            String message = new String(receivePacket.getData()).trim();
            if (message.equals(DISCOVERY_RESPONSE)) {
                return receivePacket.getAddress().getHostAddress();
            }
        } catch (Exception e) {
            // Nu a gasit serverul, incearca pe localhost ca fallback
            return "localhost";
        }
        return "localhost";
    }

    public static void startServerListener() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT, InetAddress.getByName("0.0.0.0"))) {
                socket.setBroadcast(true);
                System.out.println("Discovery service pornit, astept request-uri pe portul " + DISCOVERY_PORT);

                while (true) {
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);

                    String message = new String(packet.getData()).trim();
                    if (message.equals(DISCOVERY_REQUEST)) {
                        byte[] sendData = DISCOVERY_RESPONSE.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        socket.send(sendPacket);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
