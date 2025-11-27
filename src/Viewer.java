import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Viewer {

    public static void startForStreamer(String streamerName) throws Exception {
        Socket socket = new Socket("localhost", 5000);

        OutputStream out = socket.getOutputStream();
        out.write(("VIEWER " + streamerName + "\n").getBytes());

        InputStream in = socket.getInputStream();

        JFrame f = new JFrame("Viewer - " + streamerName);
        JLabel label = new JLabel();
        f.add(label);
        f.setSize(800, 600);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        while (true) {
            byte[] sizeArr = in.readNBytes(4);
            if (sizeArr.length < 4) break;

            int size = ((sizeArr[0] & 0xff) << 24) |
                    ((sizeArr[1] & 0xff) << 16) |
                    ((sizeArr[2] & 0xff) << 8) |
                    (sizeArr[3] & 0xff);

            byte[] frameBytes = in.readNBytes(size);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(frameBytes));

            label.setIcon(new ImageIcon(img));
            f.repaint();
        }
    }
}
