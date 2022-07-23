package src.clientDesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("clientDesktop/fxml/client-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 550);
        String css = this.getClass().getClassLoader().getResource("clientDesktop/css/client-styles.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("Client");
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
