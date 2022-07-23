package src.utils;

import java.time.LocalDateTime;

public class Status {

    public enum Actions {
        CONNECT,
        REGISTER,
        LOGIN,
        SEND,
        UNDEFINED
    }

    public enum Statuses {
        OK,
        ERROR,
        ABORT
    }

    private final Actions action;
    private final Statuses status;
    private String clientIp;
    private String clientLogin;
    private String descriptionForServer;
    private String descriptionForClient;
    private final LocalDateTime localDateTime;

    { localDateTime = LocalDateTime.now(); }

    public Status(String clientIp, String clientLogin, Actions action, Statuses status) {
        this.clientIp = clientIp;
        this.clientLogin = clientLogin;
        this.action = action;
        this.status = status;
    }

    public Status(String clientIp, String clientLogin, Actions action, Statuses status, String descriptionForServer) {
        this(clientIp,clientLogin, action, status);
        this.descriptionForServer = descriptionForServer;
    }

    public Status(String clientIp, String clientLogin, Actions action, Statuses status, String descriptionForServer, String descriptionForClient) {
        this(clientIp,clientLogin, action, status, descriptionForServer);
        this.descriptionForClient = descriptionForClient;
    }

    public String getLogForServer() {
        return "TIME: " + localDateTime + " IP: " + clientIp + " LOGIN: " + clientLogin +
                " ACTION: " + action + " STATUS: " + status + " DESCRIPTION: " + descriptionForServer;
    }

    public String getLogForClient() {
        return action + " " + status + " " + descriptionForClient;
    }
}
