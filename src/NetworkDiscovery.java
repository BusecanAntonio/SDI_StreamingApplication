import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetworkDiscovery {

    private static final int DISCOVERY_PORT = 9999;
    private static final String DISCOVERY_REQUEST = "STREAM_SERVER_REQUEST";
    private static final String DISCOVERY_RESPONSE = "STREAM_SERVER_RESPONSE";

    /**
     * Incearca sa gaseasca IP-ul serverului prin UDP Broadcast.
     * @return IP-ul serverului sub forma de String, sau null daca nu este gasit.
     */
    public static String findServerIp() {
        List<String> servers = findAllServers();
        if (!servers.isEmpty()) {
            return servers.get(0);
        }
        return null;
    }

    /**
     * Cauta toate serverele disponibile in retea.
     * @return O lista cu IP-urile serverelor gasite.
     */
    public static List<String> findAllServers() {
        List<String> foundServers = new ArrayList<>();
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

            // Asteapta raspunsuri timp de 2 secunde
            socket.setSoTimeout(2000);
            long endTime = System.currentTimeMillis() + 2000;

            while (System.currentTimeMillis() < endTime) {
                try {
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(receivePacket);

                    String message = new String(receivePacket.getData()).trim();
                    if (message.equals(DISCOVERY_RESPONSE)) {
                        String ip = receivePacket.getAddress().getHostAddress();
                        if (!foundServers.contains(ip)) {
                            foundServers.add(ip);
                        }
                    }
                } catch (Exception e) {
                    // Timeout la receive, continuam sau iesim
                    break;
                }
            }

        } catch (Exception e) {
            // Eroare generala
        }
        return foundServers;
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
