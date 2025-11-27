import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class StreamLobby extends JFrame {

    private final DefaultListModel<String> streamerListModel = new DefaultListModel<>();
    private final JList<String> streamerList = new JList<>(streamerListModel);

    private final String controlHost = "localhost";
    private final int controlPort = 6000;

    public StreamLobby() {
        super("Stream Lobby");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 400);
        setLocationRelativeTo(null);

        streamerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(streamerList);

        JButton viewButton = new JButton("Vizualizează stream-ul selectat");
        viewButton.addActionListener(e -> openSelectedStream());

        streamerList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !streamerList.isSelectionEmpty()) {
                    openSelectedStream();
                }
            }
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(viewButton, BorderLayout.CENTER);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel("Streameri activi:"), BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        Timer timer = new Timer(2000, e -> refreshStreamers());
        timer.start();
    }

    private void openSelectedStream() {
        String selected = streamerList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Selecteaza mai intai un streamer.",
                    "Nimic selectat",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }


        new Thread(() -> {
            try {
                Viewer.startForStreamer(selected);
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this,
                                "Nu s-a putut porni viewer-ul pentru " + selected + "\n" + ex.getMessage(),
                                "Eroare",
                                JOptionPane.ERROR_MESSAGE));
            }
        }).start();
    }

    private void refreshStreamers() {
        new Thread(() -> {
            List<String> streamers = fetchStreamersFromServer();
            if (streamers == null) return;

            SwingUtilities.invokeLater(() -> {
                streamerListModel.clear();
                for (String s : streamers) {
                    streamerListModel.addElement(s);
                }
            });
        }).start();
    }

    private List<String> fetchStreamersFromServer() {
        try (Socket socket = new Socket(controlHost, controlPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("LIST");
            String line = in.readLine();
            if (line == null || line.isEmpty()) {
                return new ArrayList<>();
            }

            String[] parts = line.split(",");
            List<String> result = new ArrayList<>();
            for (String p : parts) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    result.add(trimmed);
                }
            }
            return result;

        } catch (IOException e) {
            System.err.println("Nu s-a putut obtine lista de streameri: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            StreamLobby lobby = new StreamLobby();
            lobby.setVisible(true);
        });
    }
}
