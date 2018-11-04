package happypanda;

import happypanda.controllers.HappyPandaController;
import happypanda.services.DefaultExceptionHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static final int ROOT_H_PADDING = 20;
    private static final int ROOT_V_PADDING = 40;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("happy-panda.fxml"));
        Parent root = loader.load();
        HappyPandaController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("Happy Panda");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(root.minWidth(-1) + ROOT_H_PADDING);
        primaryStage.setMinHeight(root.minHeight(-1) + ROOT_V_PADDING);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler.getInstance());
        launch(args);
    }

}
