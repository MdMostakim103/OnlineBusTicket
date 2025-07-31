package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Request;
import model.Response;
import util.BackToPrevScene;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class BusOwnerLogIn {
    @FXML private TextField usernameTextfield;
    @FXML private TextField busuniqueidTextfield;
    @FXML private PasswordField passwordTextfield;
    @FXML private Label invalidInfoText;
    @FXML private Label forgotCredentials;
    @FXML private Label signUp;

    @FXML
    private void busLogIn() throws IOException, ClassNotFoundException {
        String enterpriseName = usernameTextfield.getText();
        String busId = busuniqueidTextfield.getText();
        String password = passwordTextfield.getText();

        // Validate inputs
        if (enterpriseName.isEmpty() || busId.isEmpty() || password.isEmpty()) {
            invalidInfoText.setText("Please fill all fields");
            return;
        }
        Socket socket = new Socket("localhost", 5063);
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

        String temp = busId + "," + password + "," + enterpriseName;
        Request request = new Request("BusOwner", "LogIn", temp);
        oos.writeObject(request);

        Response response = (Response) ois.readObject();
        if (response.isSuccess()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerHomePage.fxml"));
            Parent root = loader.load();
            BusOwnerHomePage controller = loader.getController();

            controller.setBusOwner(busId, enterpriseName);

            Stage stage = (Stage) usernameTextfield.getScene().getWindow();
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/view/BusOwnerHomePage.css").toExternalForm());
        } else {
            invalidInfoText.setText("Invalid email or password");
        }
    }

    @FXML
    private void forgotCredentials(MouseEvent event) {
        BackToPrevScene.navigateTo(usernameTextfield, "/view/BusOwnerForgotPass.fxml","/view/BusOwnerForgotPass.css");
    }

    @FXML
    private void signUp(MouseEvent event) {
        BackToPrevScene.navigateTo(usernameTextfield, "/view/BusOwnerSignUp.fxml","/view/BusOwnerSignUp.css");
    }

    @FXML
    private void backToPrevScene() {
        // Return to startup page
        BackToPrevScene.navigateTo(usernameTextfield, "/view/StartUpPage.fxml","/view/StartUpPage.css");
    }
}