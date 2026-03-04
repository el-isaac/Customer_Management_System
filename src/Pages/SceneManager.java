package Pages;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    public static void init(Stage primaryStage) {
        stage = primaryStage;

        // Track size only when not maximized
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isMaximized()) {
                WindowState.width = newVal.doubleValue();
            }
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (!stage.isMaximized()) {
                WindowState.height = newVal.doubleValue();
            }
        });

        stage.maximizedProperty().addListener((obs, oldVal, newVal) -> {
            WindowState.isMaximized = newVal;
        });

        stage.showingProperty().addListener((obs, wasShowing, isShowing) ->{
            if(isShowing && !WindowState.isMaximized){
                stage.setWidth(WindowState.width);
                stage.setHeight(WindowState.height);
            }
        });
    }

    public static void showScene(Scene scene) {
        stage.setScene(scene);
        // 🔴 Restore state BEFORE setting scene
        if (WindowState.isMaximized) {
            stage.setMaximized(true);
        } else {
            stage.setMaximized(false);
            stage.setWidth(WindowState.width);
            stage.setHeight(WindowState.height);
        }
    }

    public static Stage getStage() {
        return stage;
    }
}
