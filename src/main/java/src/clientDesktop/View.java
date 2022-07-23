package src.clientDesktop;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class View {

    @FXML
    private Label alertLabel;

    @FXML
    private AnchorPane formsContainer;

    @FXML
    private AnchorPane connectForm;
    @FXML
    private TextField connectIpField;
    @FXML
    private TextField connectPortField;
    @FXML
    private PasswordField connectPasswordField;
    @FXML
    private Button connectButton;

    @FXML
    private AnchorPane loginForm;
    @FXML
    private TextField loginLoginField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Button loginButton;
    @FXML
    private Label loginLabel;

    @FXML
    private AnchorPane registerForm;
    @FXML
    private TextField registerLoginField;
    @FXML
    private TextField registerNicknameField;
    @FXML
    private PasswordField registerPasswordField;
    @FXML
    private Button registerButton;

    @FXML
    private VBox messagesVBox;


    public void setVisibleAlert(boolean visible) { alertLabel.setVisible(visible); }

    public void setVisibleFormsContainer(boolean visible) { formsContainer.setVisible(visible); }

    public void setVisibleConnectForm(boolean visible) { connectForm.setVisible(visible); }

    public void setVisibleLoginForm(boolean visible) { loginForm.setVisible(visible); }

    public void setVisibleRegisterForm(boolean visible) { registerForm.setVisible(visible); }


    public String[] getConnectFormInput() {
        return new String[] { connectIpField.getText(), connectPortField.getText(), connectPasswordField.getText() };
    }

    public String[] getLoginFormInput() {
        return new String[] { loginLoginField.getText(), loginPasswordField.getText() };
    }

    public String[] getRegisterFormInput() {
        return new String[] { registerLoginField.getText(), registerNicknameField.getText(), registerPasswordField.getText() };
    }

    public void addServerMessage(String nickname, String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPadding(new Insets(5, 5, 5, 5));

        Text nicknameText = new Text(nickname + "\n");
        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(nicknameText, text);

        textFlow.setStyle(
                "-fx-background-color: rgb(192, 187, 178);" +
                "-fx-background-radius: 10px;");

        textFlow.setPadding(new Insets(5, 5, 5, 5));

        hBox.getChildren().add(textFlow);
        messagesVBox.getChildren().add(hBox);
    }

    public void addClientMessage(String nickname, String message) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 5, 50));

        Text nicknameText = new Text(nickname + "\n");
        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(nicknameText, text);

        textFlow.setStyle(
                "-fx-background-color: rgb(145, 178, 199);" +
                "-fx-background-radius: 10px;");

        textFlow.setPadding(new Insets(5, 5, 5, 5));

        hBox.getChildren().add(textFlow);
        messagesVBox.getChildren().add(hBox);
    }
}
