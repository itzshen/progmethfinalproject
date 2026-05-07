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
 * Owns all keyboard and mouse-scroll input for the game scene.
 * Fires callbacks instead of acting on game state directly,
 * keeping it decoupled from every other system.
 */
public class InputHandler {

    // ==========================================
    // Input State
    // ==========================================
    private final Set<KeyCode> activeKeys = ConcurrentHashMap.newKeySet();

    // ==========================================
    // Callbacks
    // ==========================================
    private final Runnable      onToggleShop;   // B key
    private final Runnable      onCycleFacing;  // R key
    private final DoubleConsumer onZoom;         // Scroll — receives zoom factor
    private final BooleanSupplier isShopVisible; // Lets InputHandler gate its own logic

    // ==========================================
    // Constructor
    // ==========================================
    public InputHandler(Runnable onToggleShop,
                        Runnable onCycleFacing,
                        DoubleConsumer onZoom,
                        BooleanSupplier isShopVisible) {
        this.onToggleShop  = onToggleShop;
        this.onCycleFacing = onCycleFacing;
        this.onZoom        = onZoom;
        this.isShopVisible = isShopVisible;
    }

    // ==========================================
    // Scene Binding
    // ==========================================
    public void attach(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED,  this::handleKeyPressed);
        scene.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
        scene.addEventHandler(ScrollEvent.SCROLL,    this::handleScroll);
    }

    public void detach(Scene scene) {
        scene.removeEventHandler(KeyEvent.KEY_PRESSED,  this::handleKeyPressed);
        scene.removeEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
        scene.removeEventHandler(ScrollEvent.SCROLL,    this::handleScroll);
    }

    // ==========================================
    // Accessors
    // ==========================================
    public Set<KeyCode> getActiveKeys() { return activeKeys; }

    /** Called by GameController when the shop opens so held keys don't linger. */
    public void clearKeys() { activeKeys.clear(); }

    // ==========================================
    // Handlers (private — scene events only)
    // ==========================================
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();

        // When the shop is open only hotkeys are allowed — no movement input
        if (isShopVisible.getAsBoolean()) {
            if (code == KeyCode.R) onCycleFacing.run();
            if (code == KeyCode.B) onToggleShop.run();
            return;
        }

        if (code == KeyCode.B) { onToggleShop.run();  return; }
        if (code == KeyCode.R) { onCycleFacing.run(); return; }

        activeKeys.add(code);
    }

    private void handleKeyReleased(KeyEvent event) {
        if (isShopVisible.getAsBoolean()) return;
        activeKeys.remove(event.getCode());
    }

    private void handleScroll(ScrollEvent event) {
        if (isShopVisible.getAsBoolean()) return;
        double factor = event.getDeltaY() > 0 ? 1.08 : 1.0 / 1.08;
        onZoom.accept(factor);
        event.consume();
    }
}