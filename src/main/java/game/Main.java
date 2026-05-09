package game;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * The primary entry point for the JavaFX application.
 * Responsible for initializing the main window (Stage), configuring its properties,
 * and loading the initial start menu scene.
 */
public class Main extends Application {

    /** The default width of the application window in pixels. */
    public static final int SCENE_WIDTH = 1666;

    /** The default height of the application window in pixels. */
    public static final int SCENE_HEIGHT = 930;

    /**
     * The main entry point for all JavaFX applications.
     * Sets the window title, minimum dimensions, and loads the start menu FXML layout.
     *
     * @param primaryStage The primary window provided by the JavaFX platform.
     * @throws IOException If the start menu FXML file cannot be found or loaded.
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setTitle("Mine Tycoon");

        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/start_menu.fxml")));
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root, SCENE_WIDTH, SCENE_HEIGHT));
        primaryStage.show();
    }

    /**
     * A JavaFX lifecycle hook called when the application is closing.
     * Forcibly terminates the JVM to ensure all background threads (like the logic loop) are destroyed.
     *
     * @throws Exception If an error occurs during shutdown.
     */
    @Override
    public void stop() throws Exception {
        System.exit(0);
    }

    /**
     * The standard Java entry point, used primarily as a fallback to launch the JavaFX application lifecycle.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}