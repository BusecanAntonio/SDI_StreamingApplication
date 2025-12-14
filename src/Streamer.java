import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

public class Streamer {

    public static void main(String[] args) throws Exception {
        String serverIp = NetworkDiscovery.findServerIp();
        System.out.println("Server gasit la: " + serverIp);

        String name = JOptionPane.showInputDialog("Numele streamerului:");

        // trimitem numele streamerului catre ControlServer
        try (Socket ctrl = new Socket(serverIp, 6000);
             PrintWriter out = new PrintWriter(ctrl.getOutputStream(), true)) {
            out.println("STREAMER " + name);
        }

        // conectare la VideoServer
        Socket socket = new Socket(serverIp, 5000);
        OutputStream out = socket.getOutputStream();

        // anuntam serverul video
        out.write(("STREAMER " + name + "\n").getBytes());

        Robot robot = new Robot();
        Rectangle screen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

        while (true) {
            BufferedImage img = robot.createScreenCapture(screen);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] data = baos.toByteArray();

            out.write(new byte[]{
                    (byte)(data.length >> 24),
                    (byte)(data.length >> 16),
                    (byte)(data.length >> 8),
                    (byte)(data.length)
            });

            out.write(data);
            out.flush();

            Thread.sleep(13); // ~30 FPS
        }
    }
}
