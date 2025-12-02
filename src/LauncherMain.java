public class LauncherMain {

    public static void main(String[] args) {

        // 1. Pornire ControlServer
        new Thread(() -> {
            try {
                ControlServer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "ControlServer-Thread").start();

        // 2. Pornire VideoServer
        new Thread(() -> {
            try {
                VideoServer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "VideoServer-Thread").start();

        // 3. Pornire StreamLobby (GUI)
        javax.swing.SwingUtilities.invokeLater(() -> {
            StreamLobby lobby = new StreamLobby();
            lobby.setVisible(true);
        });

        // 4. Pornire Streamer

        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Streamer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Streamer-Thread").start();
    }
}
