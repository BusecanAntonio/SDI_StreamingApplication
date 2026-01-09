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

    private String serverIp;
    private final int controlPort = 6000;

    public StreamLobby() {
        super("Stream Lobby");

        findAndSetServerIp();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 450);
        setLocationRelativeTo(null);

        streamerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(streamerList);

        JButton viewButton = new JButton("Vizualizează stream-ul selectat");
        viewButton.addActionListener(e -> openSelectedStream());

        JButton changeServerButton = new JButton("Schimbă Serverul");
        changeServerButton.addActionListener(e -> changeServer());

        streamerList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !streamerList.isSelectionEmpty()) {
                    openSelectedStream();
                }
            }
        });

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.add(viewButton);
        bottomPanel.add(changeServerButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JLabel("Streameri activi pe serverul " + serverIp + ":"), BorderLayout.NORTH);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(bottomPanel, BorderLayout.SOUTH);

        Timer timer = new Timer(2000, e -> refreshStreamers());
        timer.start();
        refreshStreamers(); // Refresh initial
    }

    private void findAndSetServerIp() {
        this.serverIp = NetworkDiscovery.findServerIp();
        if (this.serverIp == null) {
            this.serverIp = JOptionPane.showInputDialog(this, "Nu am gasit serverul automat. Introdu IP-ul serverului:");
            if (this.serverIp == null || this.serverIp.trim().isEmpty()) {
                // Fallback la localhost daca se anuleaza
                this.serverIp = "127.0.0.1";
            }
        }
        updateTitle();
    }

    private void changeServer() {
        // Cautam servere
        List<String> servers = NetworkDiscovery.findAllServers();
        
        // Adaugam si optiunea de a introduce manual
        servers.add("Manual...");

        String[] options = servers.toArray(new String[0]);

        String selected = (String) JOptionPane.showInputDialog(this, 
                "Selecteaza un server din lista sau introdu manual:",
                "Schimbare Server",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (selected != null) {
            if (selected.equals("Manual...")) {
                String manualIp = JOptionPane.showInputDialog(this, "Introdu IP-ul serverului:");
                if (manualIp != null && !manualIp.trim().isEmpty()) {
                    this.serverIp = manualIp.trim();
                }
            } else {
                this.serverIp = selected;
            }
            updateTitle();
            refreshStreamers();
        }
    }

    private void updateTitle() {
        setTitle("Stream Lobby - Conectat la " + serverIp);
        // Actualizam si label-ul de sus daca exista (putem face asta mai elegant, dar momentan e ok)
        Component[] comps = getContentPane().getComponents();
        for (Component c : comps) {
            if (c instanceof JLabel) {
                ((JLabel) c).setText("Streameri activi pe serverul " + serverIp + ":");
                break;
            }
        }
    }

    private void openSelectedStream() {
        String selected = streamerList.getSelectedValue();
        if (selected == null || selected.startsWith("Serverul")) {
            JOptionPane.showMessageDialog(this,
                    "Selecteaza mai intai un streamer.",
                    "Nimic selectat",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }


        new Thread(() -> {
            try {
                Viewer.startForStreamer(selected, serverIp);
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
            if (streamers == null) {
                SwingUtilities.invokeLater(() -> {
                    if (streamerListModel.isEmpty() || !streamerListModel.get(0).startsWith("Serverul")) {
                         streamerListModel.clear();
                         streamerListModel.addElement("Serverul nu raspunde la " + serverIp);
                    }
                });
                return;
            }

            List<String> finalStreamers = streamers;
            SwingUtilities.invokeLater(() -> {
                // Salvam selectia curenta
                String selected = streamerList.getSelectedValue();
                
                streamerListModel.clear();
                for (String s : finalStreamers) {
                    streamerListModel.addElement(s);
                }
                
                // Restauram selectia daca inca exista
                if (selected != null && finalStreamers.contains(selected)) {
                    streamerList.setSelectedValue(selected, true);
                }
            });
        }).start();
    }

    private List<String> fetchStreamersFromServer() {
        try (Socket socket = new Socket(serverIp, controlPort);
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
            // System.err.println("Eroare conexiune Lobby -> Server: " + e.getMessage());
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
