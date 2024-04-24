package udpserver;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;

public class UDPServer extends JFrame {
    private DatagramSocket socket;
    private JTextArea logArea;
    private JTextField portField;
    private JButton startButton;
    private boolean isRunning = false;

    public UDPServer() {
        super("UDP Server");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout());

        portField = new JTextField(5);  // Port numarası buraya girilecek
        bottomPanel.add(new JLabel("Port:"));
        bottomPanel.add(portField);

        startButton = new JButton("Start Server");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isRunning && !portField.getText().trim().isEmpty()) {
                    try {
                        int port = Integer.parseInt(portField.getText().trim());
                        startServer(port);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid port number.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    stopServer();
                }
            }
        });
        bottomPanel.add(startButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void startServer(int port) {
        try {
            socket = new DatagramSocket(port);
            logArea.append("Server started on port: " + port + "\n");
            isRunning = true;
            startButton.setText("Stop Server");
            new Thread(() -> {
                try {
                    while (isRunning) {
                        byte[] buffer = new byte[256];
                        DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
                        socket.receive(inPacket);
                        String receivedMessage = new String(inPacket.getData(), 0, inPacket.getLength());
                        InetAddress clientAddress = inPacket.getAddress();
                        int clientPort = inPacket.getPort();
                        logArea.append("Received from " + clientAddress + ":" + clientPort + "> " + receivedMessage + "\n");
                        String responseMessage = receivedMessage.toUpperCase();
                        DatagramPacket outPacket = new DatagramPacket(responseMessage.getBytes(), responseMessage.length(), clientAddress, clientPort);
                        socket.send(outPacket);
                    }
                } catch (IOException ex) {
                    logArea.append("Error: " + ex.getMessage() + "\n");
                }
            }).start();
        } catch (SocketException ex) {
            logArea.append("Failed to start server: " + ex.getMessage() + "\n");
        }
    }

    private void stopServer() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
            isRunning = false;
            startButton.setText("Start Server");
            logArea.append("Server stopped.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new UDPServer().setVisible(true);
        });
    }
}
