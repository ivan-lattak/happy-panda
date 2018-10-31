package happypanda;

import happypanda.controllers.HappyPandaController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("happy-panda.fxml"));
        Parent root = loader.load();
        HappyPandaController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("Happy Panda");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
