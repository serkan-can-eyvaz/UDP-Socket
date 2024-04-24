package UDPClient;

import javax.swing.*;
import java.awt.event.*;
import java.net.*;

public class UDPClient extends JFrame {

    private DatagramSocket socket;
    private InetAddress host;
    private JTextArea chatArea;
    private JTextField messageField;
    private JTextField portField;
    private String username;
    private int retryCount = 0;

    public UDPClient() {
        setTitle("UDP Client");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Kullanıcı adını almak için diyalog penceresi
        username = JOptionPane.showInputDialog(this, "Enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.trim().isEmpty()) {
            username = "Anonymous"; // Kullanıcı adı boş ise 'Anonymous' kullanılır
        }

        chatArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBounds(10, 10, 360, 200);
        add(scrollPane);

        messageField = new JTextField();
        messageField.setBounds(10, 220, 250, 30);
        add(messageField);

        portField = new JTextField();
        portField.setBounds(270, 220, 100, 30);
        add(portField);

        JButton sendButton = new JButton("Send");
        sendButton.setBounds(10, 260, 100, 30);
        add(sendButton);

        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(270, 260, 100, 30);
        add(connectButton);

        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                retryCount = 0;
                connectToServer();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
    }

    private void connectToServer() {
        try {
            int port = Integer.parseInt(portField.getText());
            host = InetAddress.getLocalHost();
            socket = new DatagramSocket();
            chatArea.append("Connected to server on port " + port + "\n");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        new Thread(() -> { // Yeni bir arka plan thread'i oluştur
            try {
                String message = messageField.getText();
                byte[] buffer = (username + ": " + message).getBytes();
                int port = Integer.parseInt(portField.getText());
                DatagramPacket outPacket = new DatagramPacket(buffer, buffer.length, host, port);

                socket.setSoTimeout(3000); // Set timeout to 3 seconds before each send/receive
                socket.send(outPacket);
                chatArea.append(username + ": " + message + "\n");

                try {
                    Thread.sleep(3000); // Sunucudan cevap almadan önce 3 saniye beklet
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                // Sunucunun cevabını al
                byte[] responseBuffer = new byte[256];
                DatagramPacket inPacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                socket.receive(inPacket);
                String response = new String(inPacket.getData(), 0, inPacket.getLength());
                chatArea.append("Server: " + response + "\n");
            } catch (SocketTimeoutException ex) {
                handleTimeout();
            } catch (Exception ex) {
                chatArea.append("Error: " + ex.getMessage() + "\n");
                ex.printStackTrace();
            }
        }).start(); // Thread'i başlat
    }

    private void handleTimeout() {
        if (++retryCount <= 3) {
            JOptionPane.showMessageDialog(this, "Timeout occurred. " + (3 - retryCount + 1) + " attempts left. Please press Send again.", "Timeout Error", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to receive response after 3 attempts, closing application.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            dispose(); // Uygulamayı kapat
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UDPClient().setVisible(true);
            }
        });
    }
}
