import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StartUpPage.fxml"));
        Parent root = loader.load();
        String css = this.getClass().getResource("/view/StartUpPage.css").toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(css);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}