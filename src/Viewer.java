import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;

public class Viewer {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 5000);
        InputStream in = socket.getInputStream();

        JFrame frame = new JFrame("Viewer");
        JLabel label = new JLabel();
        frame.add(label);
        frame.setSize(640, 480);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
            frame.repaint();
        }
    }
}
