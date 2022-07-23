package src.server;

import src.config.Config;
import src.db.MessagesFileStorage;
import src.db.UsersDB;
import src.utils.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class Server {
    private final ServerSocket serverSocket;

    public Server(String configPath) {
        Config.open(configPath);
        Logger.writeLog("New session started on " + LocalDateTime.now() + "\n"
                        + "Server started on port: " + Config.getValue("server.port")
                        + " ip: " + Config.getValue("server.ip") + "\n");
        UsersDB.open(Config.getValue("userDB.fullPath"), true);
        MessagesFileStorage.open();
        try {
            serverSocket = new ServerSocket(
                    Integer.parseInt(Config.getValue("server.port")),
                    Integer.parseInt(Config.getValue("server.backlog")),
                    InetAddress.getByName(Config.getValue("server.ip")));
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public void start() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(new ConnectionHandler(socket)).start();
                System.out.println("NEW USER CONNECTED");
            } catch (IOException e) { throw new RuntimeException(e); }
        }
    }

    public static void main(String[] args) {
        String configPath = "./config.properties";
        if (args.length == 2) if (args[0].equals("-config-path")) configPath = args[1];
        Server server = new Server(configPath);
        server.start();
    }

}
