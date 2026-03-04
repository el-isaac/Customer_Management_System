package Pages;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialize database connection
        DatabaseConnection.setupDatabase();
        SceneManager.init(primaryStage);

        SceneManager.getStage().setTitle("TI Electric");

        // Launch HomePage with the primary stage
        HomePage home = new HomePage();
        SceneManager.showScene(home.getScene());


        SceneManager.getStage().show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
