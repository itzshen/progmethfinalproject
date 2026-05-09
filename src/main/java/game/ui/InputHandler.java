package game.ui;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;

/**
 * Processes keyboard and scroll events, triggering UI toggles, build modes,
 * and tracking active keys for continuous camera movement.
 */
public class InputHandler {

    // ==========================================
    // Input State
    // ==========================================
    private final Set<KeyCode> activeKeys = ConcurrentHashMap.newKeySet();

    // ==========================================
    // Callbacks
    // ==========================================
    private final Runnable       onToggleShop;
    private final Runnable       onToggleInventory;
    private final Runnable       onBuildMode;
    private final Runnable       onRemoveMode;
    private final Runnable       onCycleFacing;
    private final DoubleConsumer onZoom;
    private final BooleanSupplier isShopVisible;
    private final BooleanSupplier isInventoryVisible;

    // ==========================================
    // Constructor
    // ==========================================

    /**
     * Initializes the input handler with callbacks for all user-triggered game actions.
     *
     * @param onToggleShop Callback to open/close the shop UI.
     * @param onToggleInventory Callback to open/close the inventory UI.
     * @param onBuildMode Callback to activate the machine placement mode.
     * @param onRemoveMode Callback to activate the machine removal mode.
     * @param onCycleFacing Callback to rotate the currently selected machine.
     * @param onZoom Callback to adjust the camera zoom level.
     * @param isShopVisible Supplier to check if the shop is currently obscuring the screen.
     * @param isInventoryVisible Supplier to check if the inventory bar is currently active.
     */
    public InputHandler(Runnable onToggleShop,
                        Runnable onToggleInventory,
                        Runnable onBuildMode,
                        Runnable onRemoveMode,
                        Runnable onCycleFacing,
                        DoubleConsumer onZoom,
                        BooleanSupplier isShopVisible,
                        BooleanSupplier isInventoryVisible) {
        this.onToggleShop       = onToggleShop;
        this.onToggleInventory  = onToggleInventory;
        this.onBuildMode        = onBuildMode;
        this.onRemoveMode       = onRemoveMode;
        this.onCycleFacing      = onCycleFacing;
        this.onZoom             = onZoom;
        this.isShopVisible      = isShopVisible;
        this.isInventoryVisible = isInventoryVisible;
    }

    // ==========================================
    // Scene Binding
    // ==========================================

    /**
     * Binds key and scroll event listeners to the active game scene.
     *
     * @param scene The active JavaFX scene to attach inputs to.
     */
    public void attach(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED,  this::handleKeyPressed);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
        scene.addEventHandler(ScrollEvent.SCROLL,    this::handleScroll);
    }

    /**
     * Unbinds event listeners to prevent memory leaks or ghost inputs when switching scenes.
     *
     * @param scene The JavaFX scene to detach inputs from.
     */
    public void detach(Scene scene) {
        scene.removeEventHandler(KeyEvent.KEY_PRESSED,  this::handleKeyPressed);
        scene.removeEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
        scene.removeEventHandler(ScrollEvent.SCROLL,    this::handleScroll);
    }

    // ==========================================
    // Accessors
    // ==========================================

    /**
     * Retrieves the set of currently held keys, typically used by the render loop for continuous camera panning.
     *
     * @return A thread-safe set of pressed KeyCodes.
     */
    public Set<KeyCode> getActiveKeys() { return activeKeys; }

    /**
     * Forcibly clears all tracked key presses. Useful when losing window focus to prevent infinite panning.
     */
    public void clearKeys() { activeKeys.clear(); }

    // ==========================================
    // Handlers
    // ==========================================
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code        = event.getCode();
        boolean shopOpen    = isShopVisible.getAsBoolean();
        boolean inventoryOpen = isInventoryVisible.getAsBoolean();

        if (code == KeyCode.E)      { onToggleShop.run();      return; }
        if (code == KeyCode.B)      { onToggleInventory.run(); return; }
        if (code == KeyCode.DIGIT1) { onBuildMode.run();       return; }
        if (code == KeyCode.DIGIT2) { onRemoveMode.run();      return; }

        // R rotates — allowed only when inventory is open
        if (code == KeyCode.R) {
            if (inventoryOpen && !shopOpen) onCycleFacing.run();
            return;
        }

        // Movement keys — only when no panel is open at all
        if (!shopOpen && !inventoryOpen) activeKeys.add(code);
    }

    private void handleKeyReleased(KeyEvent event) {
        activeKeys.remove(event.getCode());
    }

    private void handleScroll(ScrollEvent event) {
        if (isShopVisible.getAsBoolean() || isInventoryVisible.getAsBoolean()) return;
        double factor = event.getDeltaY() > 0 ? 1.08 : 1.0 / 1.08;
        onZoom.accept(factor);
        event.consume();
    }
}