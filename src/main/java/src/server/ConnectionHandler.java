package src.server;

import src.config.Config;
import src.db.MessagesFileStorage;
import src.db.UsersDB;
import src.secure.Defender;
import src.secure.RSA;
import src.utils.Logger;
import src.utils.Status;
import src.utils.Statuses;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

public class ConnectionHandler implements Runnable {

    public static ArrayList<ConnectionHandler> connectionHandlers = new ArrayList<>();
    private Socket socket;
    private Scanner scanner;
    private BufferedWriter bw;
    private Defender defender;
    private final Statuses statuses;
    private String clientIp;
    private String clientLogin = null;
    private String clientNickname = null;

    public ConnectionHandler(Socket socket) {
        this.socket = socket;
        this.clientIp = socket.getRemoteSocketAddress().toString();
        statuses = new Statuses(clientIp, null);
        try {
            this.scanner = new Scanner(new InputStreamReader(socket.getInputStream()));
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }catch (IOException e) {
            e.printStackTrace();
            Status status = new Status(clientIp, null, Status.Actions.UNDEFINED, Status.Statuses.ABORT,
                    "Unable to create Scanner or BufferReader.",
                    null);
            Logger.writeLog(status);
            closeConnection();
        }

        try { defender = new Defender(Integer.parseInt(Config.getValue("secure.RSAKeyLength"))); }
        catch (NullPointerException e) {
            e.printStackTrace();
            Status status = new Status(
                    clientIp,
                    clientLogin,
                    Status.Actions.UNDEFINED,
                    Status.Statuses.ABORT,
                    "Unable to create RSA KeyPair.",
                    "Unable to establish connection.");
            Logger.writeLog(status);
            abort(status.getLogForClient()); }
    }

    private String getMessage() {
        if (!socket.isClosed()) {
            String clientMessage = null;
            if (scanner.hasNextLine()) clientMessage = scanner.nextLine();
            return clientMessage;
        }
        return null;
    }

    private String getSecureMessage() {
        if (!socket.isClosed()) {
            String clientMessage = null;
            if (scanner.hasNextLine()) {
                clientMessage = scanner.nextLine();
                clientMessage = defender.decryptMessage(clientMessage);

                if (clientMessage == null) {
                    Status status = statuses.getUnableToDecryptClientMessageStatus(Status.Actions.UNDEFINED);
                    Logger.writeLog(status.getLogForServer());
                    abort(status.getLogForClient());
                }
            }
            return clientMessage;
        }
        return null;
    }

    private void sendMessage(String messageToSend) {
        if (!socket.isClosed()) {
            try {
                bw.write(messageToSend);
                bw.newLine();
                bw.flush();

            } catch (IOException e) {
                e.printStackTrace();
                Status status = statuses.getUnableToSendMessageStatus(Status.Actions.UNDEFINED);
                Logger.writeLog(status.getLogForServer());
                closeConnection();
            }
        }
    }

    private void sendSecureMessage(String messageToSend) {
        if (!socket.isClosed()) {
            try {
                messageToSend = defender.encryptMessage(messageToSend);

                if (messageToSend == null) {
                    Status status = statuses.getUnableToEncryptClientMessageStatus(Status.Actions.UNDEFINED);
                    Logger.writeLog(status);
                    abort(status.getLogForClient());
                } else {
                    bw.write(messageToSend);
                    bw.newLine();
                    bw.flush();
                }

            } catch (IOException e) {
                e.printStackTrace();
                Status status = statuses.getUnableToSendSecureMessageStatus(Status.Actions.UNDEFINED);
                Logger.writeLog(status);
                abort(status.getLogForClient());
            }
        }
    }

    private void connect(String publicKeyString) {
        PublicKey publicKey2 = RSA.getPublicKeyFromString(publicKeyString);
        if (publicKey2 == null) {
            Status status = statuses.getUnableToConnectStatus("Invalid client public key string: " + publicKeyString);
            Logger.writeLog(status);
            abort(status.getLogForClient());

        } else {
            defender.setPublicKey2(publicKey2);
            sendMessage(RSA.convertPublicKeyToString(defender.getPublicKey()));

            if (defender.decryptMessage(getMessage()).equals(Config.getValue("server.password"))) {
                defender.setSecureState(true);

                Status status = statuses.getSuccessfulConnectStatus();
                Logger.writeLog(status);
                sendSecureMessage(status.getLogForClient());
            } else {
                Status status = statuses.getUnableToConnectStatus("Invalid server password.");
                Logger.writeLog(status);
                abort(status.getLogForClient());
            }
        }
    }

    public void register(String login, String nickName, String password) {
        String errors = "";
        if (!UsersDB.isUserLoginUnique(login)) errors += "This login is already in use. ";
        if (!UsersDB.isUserNicknameUnique(nickName)) errors += "This nickname is already in use. ";
        if (password.length() < 10) errors += "This password is to weak. ";

        if (errors.length() > 0) {
            Status status = statuses.getUnableToRegisterStatus(errors);
            Logger.writeLog(status);
            sendSecureMessage(status.getLogForClient());
        } else {
            byte[] salt = defender.generateSalt();
            byte[] hash = defender.generateHash(password, salt, 1024, 512);
            UsersDB.addUser(RSA.encodeBase64(salt), RSA.encodeBase64(hash), login, nickName);
            clientLogin = login;
            clientNickname = UsersDB.getNicknameByLogin(login);
            if (!connectionHandlers.contains(this)) connectionHandlers.add(this);
            statuses.setLogin(login);
            defender.setLoggedInState(true);

            Status status = statuses.getSuccessfulRegisterStatus();
            Logger.writeLog(status);
            sendSecureMessage(status.getLogForClient());
        }
    }

    private void login(String login, String password) {
        if (defender.isLoggedIn()) {
            Status status = statuses.getUnableToLoginStatus("Already logged in.");
            Logger.writeLog(status);
            abort(status.getLogForClient());
        } else {
            String[] saltHash = UsersDB.getSaltHashByLogin(login);
            if (saltHash == null) {
                Status status = statuses.getUnableToLoginStatus(
                        "Attempt to login with incorrect login.");
                Logger.writeLog(status);
                abort(status.getLogForClient());
            } else {
                String salt = saltHash[0];
                String hash = saltHash[1];

                String newHash = RSA.encodeBase64(
                        defender.generateHash(password, RSA.decodeBase64(salt), 1024, 512));

                if (hash.equals(newHash)) {
                    for (ConnectionHandler connectionHandler : connectionHandlers) {
                        if (connectionHandler.clientLogin.equals(login)) {
                            Status status = statuses.getUserAlreadyLoggedInStatus();
                            Logger.writeLog(status);
                            abort(status.getLogForClient());
                            break;
                        }
                    }

                    defender.setLoggedInState(true);
                    clientLogin = login;
                    clientNickname = UsersDB.getNicknameByLogin(login);
                    if (!connectionHandlers.contains(this)) connectionHandlers.add(this);
                    Status status = statuses.getSuccessfulLoginStatus();
                    Logger.writeLog(status);
                    sendSecureMessage(status.getLogForClient());
                    sendSecureMessage(clientNickname);
                } else {
                    Status status = statuses.getUnableToLoginStatus(
                            "Attempt to login with incorrect password.");
                    Logger.writeLog(status);
                    abort(status.getLogForClient());
                }
            }
        }
    }

    private void send(String messageType, String nickname) {
        if (messageType.equals("TEXT")) {
            String clientText = "";
            String tempText;
            while ((tempText = getSecureMessage()) != null) {
                if (tempText.equals("END")) break;
                clientText += tempText;
            }

            MessagesFileStorage.addMessage(LocalDateTime.now(), clientNickname, clientText);
            for (ConnectionHandler connectionHandler : connectionHandlers) {
                if (!connectionHandler.equals(this)) {
                    try {
                        connectionHandler.sendSecureMessage("TEXT " + nickname);
                        connectionHandler.sendSecureMessage(clientText);
                        connectionHandler.sendSecureMessage("END");
                    } catch (Exception e) { connectionHandler.closeConnection(); }
                }
            }
        }
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected() && !socket.isClosed()) {

                String clientMessage;
                if (defender.isSecure()) clientMessage = getSecureMessage();
                else clientMessage = getMessage();

                if (clientMessage != null) {
                    String[] splitMessage = clientMessage.split(" ");
                    switch (splitMessage[0]) {
                        case "CONNECT":
                            if (splitMessage.length == 2) {
                                if (!defender.isSecure()) connect(splitMessage[1]);
                                else {
                                    Status status = statuses.getUnableToConnectStatus(
                                            "Attempt to create a secure connection twice.");
                                    Logger.writeLog(status);
                                    abort(status.getLogForClient());
                                }
                            } else {
                                Status status = statuses.getIncorrectQuerySyntaxStatus(Status.Actions.CONNECT);
                                Logger.writeLog(status);
                                abort(status.getLogForClient());
                            }
                            break;

                        case "REGISTER":
                            if (splitMessage.length == 4) {
                                if (defender.isSecure()) register(splitMessage[1], splitMessage[2], splitMessage[3]);
                                else {
                                    Status status = statuses.getConnectionIsNotSecureStatus(Status.Actions.REGISTER);
                                    Logger.writeLog(status);
                                    abort(status.getLogForClient());
                                }
                            } else {
                                Status status = statuses.getIncorrectQuerySyntaxStatus(Status.Actions.CONNECT);
                                Logger.writeLog(status);
                                abort(status.getLogForClient());
                            }
                            break;

                        case "LOGIN":
                            if (splitMessage.length == 3) {
                                if (defender.isSecure()) login(splitMessage[1], splitMessage[2]);
                                else {
                                    Status status = statuses.getConnectionIsNotSecureStatus(Status.Actions.REGISTER);
                                    Logger.writeLog(status);
                                    abort(status.getLogForClient());
                                }
                            } else {
                                Status status = statuses.getIncorrectQuerySyntaxStatus(Status.Actions.CONNECT);
                                Logger.writeLog(status);
                                abort(status.getLogForClient());
                            }
                            break;

                        case "SEND":
                            if (splitMessage.length == 3) {
                                if (defender.isSecure()) {
                                    if (defender.isLoggedIn()) send(splitMessage[1], splitMessage[2]);
                                    else {
                                        Status status = statuses.getUserNotLoggedInStatus(Status.Actions.SEND);
                                        Logger.writeLog(status);
                                        abort(status.getLogForClient());
                                    }
                                } else {
                                    Status status = statuses.getConnectionIsNotSecureStatus(Status.Actions.SEND);
                                    Logger.writeLog(status);
                                    abort(status.getLogForClient());
                                }
                            } else {
                                Status status = statuses.getIncorrectQuerySyntaxStatus(Status.Actions.SEND);
                                Logger.writeLog(status);
                                abort(status.getLogForClient());
                            }
                            break;

                        default:
                            Status status = statuses.getIncorrectQuerySyntaxStatus(Status.Actions.UNDEFINED);
                            Logger.writeLog(status);
                            abort(status.getLogForClient());
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            closeConnection();
            System.out.println(clientIp + " " + clientLogin + " - CONNECTION ABORTED.");
        }
    }

    private void closeConnection() {
        try {
            bw.close();
            scanner.close();
            connectionHandlers.remove(this);
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void abort(String abortMessage) {
        sendMessage("NOT-SECURE " + abortMessage);
        closeConnection();
        System.out.println(clientIp + " " + clientLogin + " - CONNECTION ABORTED.");
    }
}
