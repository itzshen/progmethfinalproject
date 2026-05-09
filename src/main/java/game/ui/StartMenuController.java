package game.ui;

import game.Main;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Controls the main start menu UI.
 * Handles loading the background, attaching interactive hover animations to buttons,
 * and transitioning the scene to the main game loop or exiting the application.
 */
public class StartMenuController implements Initializable {

    @FXML private Pane      backgroundPane;
    @FXML private Button    playButton;
    @FXML private Button    quitButton;

    /**
     * Called automatically by JavaFX after the FXML file is loaded.
     * Initializes the background graphics and attaches hover animations to the menu buttons.
     *
     * @param location The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadBackground();
        setupHoverEffects();
    }

    // ==========================================
    // Background
    // ==========================================

    /**
     * Attempts to load the main menu background image from resources and applies it
     * to the root pane, configuring it to cover the entire window dynamically.
     */
    private void loadBackground() {
        try (InputStream stream = StartMenuController.class
                .getResourceAsStream("/images/environment/StartMenuBackground.JPG")) {
            if (stream != null) {
                Image img = new Image(stream);
                if (!img.isError()) {
                    // This configuration perfectly mimics CSS "background-size: cover"
                    BackgroundImage bgImg = new BackgroundImage(
                            img,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true)
                    );
                    backgroundPane.setBackground(new Background(bgImg));
                }
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not load start menu background: " + e.getMessage());
        }
    }

    // ==========================================
    // Hover Effects
    // ==========================================

    /**
     * Initializes the CSS styling and scaling animations for all interactive menu buttons.
     */
    private void setupHoverEffects() {
        setupPlayButtonHover();
        setupQuitButtonHover();
    }

    /**
     * Binds mouse enter and exit events to the Play button to swap its CSS styles
     * and trigger a slight scaling animation.
     */
    private void setupPlayButtonHover() {
        String base = "-fx-background-color: #e8a020;" +
                "-fx-text-fill: #0a0c0f;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 2;" +
                "-fx-cursor: hand;";

        String hovered = "-fx-background-color: #ffb830;" +
                "-fx-text-fill: #0a0c0f;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 2;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,184,48,0.8), 24, 0.4, 0, 0);";

        playButton.setOnMouseEntered(e -> {
            playButton.setStyle(hovered);
            scaleButton(playButton, 1.0, 1.04);
        });
        playButton.setOnMouseExited(e -> {
            playButton.setStyle(base +
                    "-fx-effect: dropshadow(gaussian, rgba(232,160,32,0.55), 18, 0.3, 0, 0);");
            scaleButton(playButton, 1.04, 1.0);
        });
    }

    /**
     * Binds mouse enter and exit events to the Quit button to swap its CSS styles
     * and trigger a slight scaling animation.
     */
    private void setupQuitButtonHover() {
        String base = "-fx-background-color: transparent;" +
                "-fx-text-fill: #a0a8b0;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 2;" +
                "-fx-border-color: #3a4450;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 2;" +
                "-fx-cursor: hand;";

        String hovered = "-fx-background-color: rgba(180,50,40,0.15);" +
                "-fx-text-fill: #e05545;" +
                "-fx-font-family: 'Courier New';" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 2;" +
                "-fx-border-color: #e05545;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 2;" +
                "-fx-cursor: hand;";

        quitButton.setOnMouseEntered(e -> {
            quitButton.setStyle(hovered);
            scaleButton(quitButton, 1.0, 1.04);
        });
        quitButton.setOnMouseExited(e -> {
            quitButton.setStyle(base);
            scaleButton(quitButton, 1.04, 1.0);
        });
    }

    /**
     * Executes a smooth scaling animation on a target button.
     *
     * @param button The button to animate.
     * @param from The starting scale multiplier.
     * @param to The ending scale multiplier.
     */
    private void scaleButton(Button button, double from, double to) {
        ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
        st.setFromX(from);
        st.setFromY(from);
        st.setToX(to);
        st.setToY(to);
        st.play();
    }

    // ==========================================
    // Actions
    // ==========================================

    /**
     * Triggered when the Play button is clicked.
     * Loads the main game FXML layout and swaps the current scene to start the simulation.
     *
     * @param event The ActionEvent generated by the button click.
     * @throws IOException If the layout.fxml file cannot be found or loaded.
     */
    @FXML
    void startGame(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(
                getClass().getResource("/layout.fxml")));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        double width  = stage.getWidth()  > 0 ? stage.getWidth()  : Main.SCENE_WIDTH;
        double height = stage.getHeight() > 0 ? stage.getHeight() : Main.SCENE_HEIGHT;

        stage.setScene(new Scene(root, width, height));
    }

    /**
     * Triggered when the Quit button is clicked.
     * Closes the active window and safely shuts down the JavaFX application thread.
     *
     * @param event The ActionEvent generated by the button click.
     */
    @FXML
    void quitGame(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
        javafx.application.Platform.exit();
    }
}