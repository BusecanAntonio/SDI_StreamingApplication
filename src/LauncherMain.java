public class LauncherMain {

    public static void main(String[] args) {

        if (args.length > 0 && "streamer".equalsIgnoreCase(args[0])) {
            // Porneste doar un nou streamer
            try {
                Streamer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Comportamentul original: porneste serverele, lobby-ul si un streamer
            
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
}

//    javac -d out/production/SDI_StreamingApplication src/*.java
//        java -cp out/production/SDI_StreamingApplication LauncherMain streamer
//
