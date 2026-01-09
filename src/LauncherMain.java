import javax.swing.*;

public class LauncherMain {

    public static void main(String[] args) {

        // Daca avem argumente in linia de comanda, le respectam (pentru automatizare)
        if (args.length > 0) {
            String mode = args[0].toLowerCase();
            switch (mode) {
                case "streamer":
                    startStreamer();
                    break;
                case "lobby":
                    startLobby();
                    break;
                case "server":
                    startServers();
                    break;
                case "host":
                    startServers();
                    startLobby();
                    startStreamer();
                    break;
            }
            return;
        }

        // Daca nu avem argumente, afisam meniul grafic
        String[] options = {
                "HOST (Server + Lobby + Streamer)", // 0 - Pentru PC
                "CLIENT (Lobby + Streamer)",        // 1 - Pentru Laptop
                "Doar Streamer",                    // 2
                "Doar Lobby"                        // 3
        };

        int choice = JOptionPane.showOptionDialog(null,
                "Selecteaza modul de pornire:",
                "SDI Streaming Launcher",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]); // Default selectat: Client

        switch (choice) {
            case 0: // HOST
                startServers();
                startLobby();
                startStreamer();
                break;
            case 1: // CLIENT
                // Nu pornim serverele!
                startLobby();
                startStreamer();
                break;
            case 2: // DOAR LOBBY
                startLobby();
                break;
            default:
                System.exit(0);
        }
    }

    // --- Metode ajutatoare ---

    private static void startServers() {
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
    }

    private static void startLobby() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            StreamLobby lobby = new StreamLobby();
            lobby.setVisible(true);
        });
    }

    private static void startStreamer() {
        new Thread(() -> {
            try {
                // Asteptam putin sa fim siguri ca serverul (sau discovery) e gata
                Thread.sleep(1000);
                Streamer.main(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "Streamer-Thread").start();
    }
}
