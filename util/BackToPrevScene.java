package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class BackToPrevScene {

    // Private constructor to prevent instantiation
    private BackToPrevScene() {}

    /**
     * Navigates to a new scene from any control's context
     * @param currentControl Any control in the current scene (buttons, textfields etc)
     * @param fxmlPath Path to the FXML file (e.g., "/view/StartUpPage.fxml")
     * @param cssPath Optional CSS path (pass null if not needed)
     */
    public static void navigateTo(Node currentControl, String fxmlPath, String cssPath) {
        try {
            Stage stage = (Stage) currentControl.getScene().getWindow();
            Parent root = FXMLLoader.load(BackToPrevScene.class.getResource(fxmlPath));

            Scene scene = new Scene(root);
            if (cssPath != null) {
                scene.getStylesheets().add(BackToPrevScene.class.getResource(cssPath).toExternalForm());
            }

            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load scene: " + fxmlPath, e);
        }
    }

    // Overloaded method without CSS
    public static void navigateTo(Node currentControl, String fxmlPath) {
        navigateTo(currentControl, fxmlPath, null);
    }
}