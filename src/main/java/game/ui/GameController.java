package game.ui;

import game.logic.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;

import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings("CallToPrintStackTrace")
public class GameController implements Initializable {

    // ==========================================
    // Constants
    // ==========================================
    private static final double TILE_SIZE = GameConstants.TILE_SIZE;

    // ==========================================
    // FXML UI Components
    // ==========================================
    @FXML private Canvas  gameCanvas;
    @FXML private VBox    shopPopup;
    @FXML private HBox    inventoryBox;
    @FXML private Label   moneyLabel;
    @FXML private Label   shopHintLabel;
    @FXML private TabPane shopTabPane;

    // ==========================================
    // Core Systems
    // ==========================================
    private final GridSystem    logicGrid = new GridSystem(20, 20);
    private final PlayerBank    bank      = new PlayerBank(500.0);
    private final GameRenderer  renderer  = new GameRenderer();
    private final CameraManager camera    = new CameraManager();

    // ==========================================
    // Managers
    // ==========================================
    private ShopManager      shopManager;
    private InputHandler     inputHandler;
    private GameLoopManager  gameLoopManager;
    private PlacementManager placementManager;

    // ==========================================
    // Initialization
    // ==========================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initShop();
        initPlacement();
        initInput();
        initGameLoop();
        bindCanvas();
        bindSceneLifecycle();
    }

    // ==========================================
    // Wiring — each method sets up one manager
    // ==========================================

    private void initShop() {
        shopManager = new ShopManager(
                bank,
                inventoryBox,
                moneyLabel,
                renderer::imageForMachineType,
                () -> { if (placementManager != null) placementManager.updateHint(); }
        );
        ShopUIBuilder.build(shopTabPane, renderer::imageForMachineType, shopManager::attemptBuy);
        shopManager.refreshUI();
    }

    private void initPlacement() {
        placementManager = new PlacementManager(
                logicGrid,
                bank,
                () -> shopPopup != null && shopPopup.isVisible(),
                shopManager::getActiveSelection,
                shopManager::getInventoryCount,
                shopManager::consumeFromInventory,
                shopManager::refreshUI,
                text -> { if (shopHintLabel != null) shopHintLabel.setText(text); }
        );
    }

    private void initInput() {
        inputHandler = new InputHandler(
                this::toggleShop,
                placementManager::cyclePlacementFacing,
                camera::applyZoom,
                () -> shopPopup != null && shopPopup.isVisible()
        );
    }

    private void initGameLoop() {
        gameLoopManager = new GameLoopManager(
                this::onRenderFrame,
                this::onLogicTick
        );
    }

    private void bindCanvas() {
        gameCanvas.setOnMouseClicked(placementManager::handleCanvasClick);
        gameCanvas.setOnMouseMoved(placementManager::handleCanvasMouseMove);
    }

    private void bindSceneLifecycle() {
        gameCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                inputHandler.detach(oldScene);
                gameLoopManager.stop();
            }
            if (newScene != null) {
                inputHandler.attach(newScene);
                gameLoopManager.start();
            }
        });
    }

    // ==========================================
    // Game Loop Callbacks
    // ==========================================

    private void onRenderFrame(double dtSec) {
        if (dtSec > 0) {
            double pan = GameLoopManager.PAN_SPEED_PX_PER_SEC * dtSec;
            double dx = 0, dy = 0;
            if (inputHandler.getActiveKeys().contains(KeyCode.W)) dy += pan;
            if (inputHandler.getActiveKeys().contains(KeyCode.S)) dy -= pan;
            if (inputHandler.getActiveKeys().contains(KeyCode.A)) dx += pan;
            if (inputHandler.getActiveKeys().contains(KeyCode.D)) dx -= pan;
            camera.pan(dx, dy);
        }

        double worldW = logicGrid.getWidth()  * TILE_SIZE;
        double worldH = logicGrid.getHeight() * TILE_SIZE;
        camera.applyTransformsAndClamp(gameCanvas, worldW, worldH);

        renderer.render(
                gameCanvas, logicGrid, shopManager,
                placementManager.getMouseWorldX(),
                placementManager.getMouseWorldY(),
                placementManager.getPlacementFacing(),
                shopPopup.isVisible()
        );
    }

    private void onLogicTick() {
        logicGrid.tick();
        Platform.runLater(shopManager::refreshUI);
    }

    // ==========================================
    // Shop Toggle
    // ==========================================

    @FXML
    void toggleShop() {
        boolean opening = !shopPopup.isVisible();
        shopPopup.setVisible(opening);
        if (opening) inputHandler.clearKeys();
    }
}