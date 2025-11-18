import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Streamer {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("localhost", 5000);
        OutputStream out = socket.getOutputStream();

        int frameNumber = 0;

        while (true) {

            // 1. Cream un frame (imagine) direct din cod
            BufferedImage img = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();

            // Fundal simplu colorat
            g.setColor(Color.BLUE);
            g.fillRect(0, 0, 640, 480);

            // Text care se schimbă
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Frame: " + frameNumber, 50, 200);

            g.dispose();
            frameNumber++;

            // 2. Convertim imaginea în JPG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            byte[] data = baos.toByteArray();

            // 3. Trimitem lungimea frame-ului
            out.write(new byte[]{
                    (byte)(data.length >> 24),
                    (byte)(data.length >> 16),
                    (byte)(data.length >> 8),
                    (byte)(data.length)
            });

            // 4. Trimitem frame-ul
            out.write(data);
            out.flush();

            // 5. Frame rate (~30 FPS)
            Thread.sleep(33);
        }
    }
}
