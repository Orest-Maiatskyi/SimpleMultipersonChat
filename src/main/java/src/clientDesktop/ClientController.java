package src.clientDesktop;

import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import src.client.Client;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ClientController extends View implements Initializable {

    @FXML
    private Label alertLabel;

    @FXML
    private AnchorPane formsContainer;

    @FXML
    private AnchorPane connectForm;
    @FXML
    private Button connectButton;

    @FXML
    private AnchorPane loginForm;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginLabel;

    @FXML
    private AnchorPane registerForm;
    @FXML
    private Button registerButton;

    @FXML
    private Button sendMessageButton;
    @FXML
    private TextField messageTextField;

    private Client client;
    private String serverPassword;
    private String ip;
    private int port;
    private String login;
    private String nickname;
    private String password;

    public void alert(int status, String alertMessage) {
        if (status == 0) alertLabel.setStyle("-fx-background-color: #92D293;");
        else if (status == 1) alertLabel.setStyle("-fx-background-color: #F3E496;");
        else if (status == 2) alertLabel.setStyle("-fx-background-color: #D2686E;");
        alertLabel.setText(alertMessage);
        setVisibleAlert(true);

        TranslateTransition openTransition = new TranslateTransition(Duration.seconds(1), alertLabel);
        openTransition.setFromY(-40.0);
        openTransition.setToY(0.0);
        openTransition.play();

        TranslateTransition closeTransition = new TranslateTransition(Duration.seconds(1), alertLabel);
        closeTransition.setFromY(0.0);
        closeTransition.setToY(-40.0);

        SequentialTransition seqTransition = new SequentialTransition (
                new PauseTransition(Duration.millis(2000)), closeTransition );

        openTransition.setOnFinished(e -> seqTransition.play());
    }

    public boolean reconnect () {
        try {
            client.closeConnection();
            client = new Client(port, ip);
            return client.connect(serverPassword).equals("OK");
        } catch (IOException e) { return false; }
    }

    public void messageListenerThread() {
        Thread thread = new Thread(() -> {
            while (client.isConnected()) {
                String[] message = client.listenForMessages();
                if (message != null) Platform.runLater(() -> addServerMessage(message[0], message[1]));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setVisibleAlert(false);
        loginForm.setVisible(false);
        registerForm.setVisible(false);

        connectButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            String[] connectFormInput = getConnectFormInput();
            String ip = connectFormInput[0];
            String port = connectFormInput[1];
            String password = connectFormInput[2];

            try {
                client = new Client(Integer.parseInt(port), ip);
                String status = client.connect(password);
                if (status.equals("OK")) {
                    this.ip = ip;
                    this.port = Integer.parseInt(port);
                    this.serverPassword = password;
                    setVisibleConnectForm(false);
                    setVisibleLoginForm(true);
                } else if (status.split(" ")[0].equals("NOT-SECURE")) alert(2, status);
                else alert(2, status);
            } catch (IOException | NumberFormatException e) { alert(1, "Invalid ip or port."); }
        });

        loginButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            String[] loginFormInput = getLoginFormInput();
            String login = loginFormInput[0];
            String password = loginFormInput[1];

            String status = client.login(login, password);
            if (status.equals("OK")) {
                this.login = login;
                this.password = password;
                setVisibleLoginForm(false);
                setVisibleFormsContainer(false);
                messageListenerThread();
            } else {
                if (status.split(" ")[0].equals("NOT-SECURE")) alert(2, status);
                else alert(1, status);

                if (!reconnect()) {
                    setVisibleLoginForm(false);
                    setVisibleConnectForm(true);
                }
            }
        });

        loginLabel.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            setVisibleLoginForm(false);
            setVisibleRegisterForm(true);
        });

        registerButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            String[] registerFormInput = getRegisterFormInput();
            String login = registerFormInput[0];
            String nickname = registerFormInput[1];
            String password = registerFormInput[2];

            String status = client.register(login, nickname, password);
            if (status.equals("OK")) {
                this.login = login;
                this.password = password;
                setVisibleRegisterForm(false);
                setVisibleFormsContainer(false);
                messageListenerThread();
            } else {
                if (status.split(" ")[0].equals("NOT-SECURE")) alert(2, status);
                else alert(1, status);

                if (!reconnect()) {
                    setVisibleLoginForm(false);
                    setVisibleConnectForm(true);
                }
            }
        });

        sendMessageButton.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            String messageText = messageTextField.getText();
            if (!messageText.replaceAll(" ", "").equals("")) {
                addClientMessage(client.nickname, messageText);
                client.sendText(messageText);
                messageTextField.setText("");
            }
        });
    }
}
