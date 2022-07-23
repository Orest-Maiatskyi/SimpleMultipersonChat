package src.client;

import src.secure.Defender;
import src.secure.RSA;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private final Socket socket;
    private final Scanner scanner;
    private final BufferedWriter bw;
    private final Defender defender;
    private String clientState = "listening";
    private String login;
    public String nickname;


    public Client(int port, String ip) throws IOException {
        this.socket = new Socket(InetAddress.getByName(ip), port);
        this.scanner = new Scanner(new InputStreamReader(socket.getInputStream()));
        this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.defender = new Defender(2048);
    }

    private String getMessage() {
        String serverMessage = null;
        if (scanner.hasNextLine()) serverMessage = scanner.nextLine();
        return serverMessage;
    }

    private String getSecureMessage() {
        String clientMessage = getMessage();
        if (clientMessage != null) {
            if (!clientMessage.split(" ")[0].equals("NOT-SECURE")) {
                clientMessage = defender.decryptMessage(clientMessage);
            } else closeConnection();
        }
        return clientMessage;
    }

    private void sendMessage(String messageToSend) {
        try {
            bw.write(messageToSend);
            bw.newLine();
            bw.flush();
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private void sendSecureMessage(String message) {
        if (defender.isSecure()) {
            sendMessage(defender.encryptMessage(message));
        }
    }

    public String connect(String serverPassword) {
        clientState = "connect";
        sendMessage("CONNECT " + RSA.convertPublicKeyToString(defender.getPublicKey()));
        defender.setPublicKey2(RSA.getPublicKeyFromString(getMessage()));
        sendMessage(defender.encryptMessage(serverPassword));
        String connectStatus = getSecureMessage();
        clientState = "listen";
        if (connectStatus != null) {
            if (connectStatus.equals("CONNECT OK Successful connect.")) {
                defender.setSecureState(true);
                return "OK";
            } else return connectStatus;
        } else return "Connection aborted.";
    }

    public String register(String login, String nickname, String password) {
        if (defender.isSecure()) {
            clientState = "register";
            sendSecureMessage("REGISTER " + login + " " + nickname + " " + password);
            String registerStatus = getSecureMessage();
            clientState = "listen";
            if (registerStatus != null) {
                if (registerStatus.equals("REGISTER OK Successful register.")) {
                    this.login = login;
                    this.nickname = nickname;
                    defender.setLoggedInState(true);
                    return "OK";
                } else return registerStatus;
            } else return "Connection aborted.";
        } return "Connection is not secure.";
    }

    public String login(String login, String password) {
        if (defender.isSecure()) {
            clientState = "login";
            sendSecureMessage("LOGIN " + login + " " + password);
            String loginStatus = getSecureMessage();
            if (loginStatus != null) {
                if (loginStatus.equals("LOGIN OK Successful login.")) {
                    defender.setLoggedInState(true);
                    this.login = login;
                    this.nickname = getSecureMessage();
                    clientState = "listen";
                    return "OK";
                } return loginStatus;
            } return "Connection aborted.";
        } return "Connection is not secure.";
    }

    public void sendText(String text) {
        if (defender.isSecure() && defender.isLoggedIn()) {
            sendSecureMessage("SEND TEXT " + nickname);
            sendSecureMessage(text);
            sendSecureMessage("END");
        }
    }

    public String[] listenForMessages() {
        if (clientState.equals("listen")) {
            String text = "";
            String[] splitMessage = getSecureMessage().split(" ");
            if (splitMessage[0].equals("TEXT")) {
                while (true) {
                    String tempText = getSecureMessage();
                    if (tempText.equals("END")) break;
                    text += tempText;
                }
                return new String[] {splitMessage[1], text};
            }
        }
        return null;
    }

    public void closeConnection() {
        try {
            bw.close();
            scanner.close();
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean isConnected() { return socket.isConnected(); }

}
