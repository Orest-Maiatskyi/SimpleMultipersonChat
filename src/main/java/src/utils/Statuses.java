package src.utils;

public class Statuses {

    private String ip;
    private String login;

    public Statuses(String ip, String login) {
        this.ip = ip;
        this.login = login;
    }

    public Status getIncorrectQuerySyntaxStatus(Status.Actions action) {
        return new Status(ip, login, action, Status.Statuses.ABORT,
                "Incorrect " + action.toString() + " syntax.",
                "Incorrect " + action.toString() + " syntax.");
    }

    public Status getConnectionIsNotSecureStatus(Status.Actions action) {
        return new Status(ip, login, action, Status.Statuses.ABORT,
                "Attempt to " + action.toString() + " through unsecure connection.",
                "Connection is not secure.");
    }

    public Status getUserNotLoggedInStatus(Status.Actions action) {
        return new Status(ip, login, action, Status.Statuses.ABORT,
                "Attempt to " + action.toString() + " without authorisation.",
                "Client need to be logged in first, try to use LOGIN command.");
    }

    public Status getUnableToDecryptClientMessageStatus(Status.Actions action) {
        return new Status(ip, login, action, Status.Statuses.ABORT,
                "Unable to decrypt client message.",
                "Incorrect secure message.");
    }

    public Status getUnableToEncryptClientMessageStatus(Status.Actions action) {
        return new Status(ip, login, action, Status.Statuses.ABORT,
                "Unable to encrypt client message.",
                "Unable to send secure message.");
    }

    public Status getUnableToSendMessageStatus(Status.Actions action) {
        return new Status(ip, login, action, Status.Statuses.ABORT,
                "Unable to send message.",
                null);
    }

    public Status getUnableToSendSecureMessageStatus(Status.Actions action) {
        return new Status(ip, login, action, Status.Statuses.ABORT,
                "Unable to send secure message.",
                "Unable to send secure message.");
    }

    public Status getUnableToConnectStatus(String descriptionForServer) {
        return new Status(ip, login, Status.Actions.CONNECT, Status.Statuses.ABORT,
                descriptionForServer,
                "Unable to set secure connection.");
    }

    public Status getUnableToRegisterStatus(String description) {
        return new Status(ip, login, Status.Actions.REGISTER, Status.Statuses.ABORT,
                "Invalid register data: " + description,
                description);
    }

    public Status getUnableToLoginStatus(String descriptionForServer) {
        return new Status(ip, login, Status.Actions.LOGIN, Status.Statuses.ABORT,
                descriptionForServer,
                "Incorrect login or password.");
    }

    public Status getUserAlreadyLoggedInStatus() {
        return new Status(ip, login, Status.Actions.LOGIN, Status.Statuses.ABORT,
                "Attempt to login account with online state.",
                "This account has online state.");
    }

    public Status getSuccessfulConnectStatus() {
        return new Status(ip, login, Status.Actions.CONNECT, Status.Statuses.OK,
                "Successful connect.",
                "Successful connect.");
    }

    public Status getSuccessfulRegisterStatus() {
        return new Status(ip, login, Status.Actions.REGISTER, Status.Statuses.OK,
                "Successful register.",
                "Successful register.");
    }

    public Status getSuccessfulLoginStatus() {
        return new Status(ip, login, Status.Actions.LOGIN, Status.Statuses.OK,
                "Successful login.",
                "Successful login.");
    }

    public String getIp() {
        return ip;
    }

    public String getLogin() {
        return login;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
