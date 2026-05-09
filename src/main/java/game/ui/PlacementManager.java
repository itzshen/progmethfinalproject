package game.ui;

import game.logic.*;
import javafx.scene.input.MouseEvent;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Manages the interactive placement and removal of machines on the game grid.
 * Translates mouse input into grid coordinates, handles inventory consumption,
 * and delegates UI updates and sound effects via callbacks.
 */
public class PlacementManager {

    // ==========================================
    // Constants
    // ==========================================
    private static final double TILE_SIZE = GameConstants.TILE_SIZE;

    // ==========================================
    // Placement State
    // ==========================================
    private Direction     placementFacing = Direction.RIGHT;
    private PlacementMode placementMode   = PlacementMode.BUILD;
    private double mouseWorldX;
    private double mouseWorldY;

    // ==========================================
    // Hard Dependencies
    // ==========================================
    private final GridSystem logicGrid;
    private final PlayerBank bank;

    // ==========================================
    // Callbacks
    // ==========================================
    private final BooleanSupplier                isShopVisible;
    private final Supplier<MachineType>          getActiveSelection;
    private final Function<MachineType, Integer> getInventoryCount;
    private final Consumer<MachineType>          consumeFromInventory;
    private final Consumer<MachineType>          returnToInventory;
    private final Runnable                       refreshUI;
    private final Consumer<String>               onHintText;
    private final Runnable                       onPlaceSound;
    private final Runnable                       onRemoveSound;

    // ==========================================
    // Constructor
    // ==========================================

    /**
     * Initializes the placement manager with necessary game state dependencies and interaction callbacks.
     *
     * @param logicGrid The core logic grid where machines are placed and removed.
     * @param bank The player's bank, required for initializing certain machine types.
     * @param isShopVisible Supplier to check if the shop is currently obscuring the grid.
     * @param getActiveSelection Supplier to retrieve the currently selected machine type from the inventory UI.
     * @param getInventoryCount Function to check the available quantity of a specific machine type.
     * @param consumeFromInventory Callback to deduct a machine from the player's inventory upon placement.
     * @param returnToInventory Callback to refund a machine to the player's inventory upon removal.
     * @param refreshUI Callback to trigger a visual update of the inventory UI.
     * @param onHintText Callback to push dynamic instructional text to the player interface.
     * @param onPlaceSound Callback to trigger a sound effect when a machine is successfully built.
     * @param onRemoveSound Callback to trigger a sound effect when a machine is successfully removed.
     */
    public PlacementManager(GridSystem logicGrid,
                            PlayerBank bank,
                            BooleanSupplier isShopVisible,
                            Supplier<MachineType> getActiveSelection,
                            Function<MachineType, Integer> getInventoryCount,
                            Consumer<MachineType> consumeFromInventory,
                            Consumer<MachineType> returnToInventory,
                            Runnable refreshUI,
                            Consumer<String> onHintText,
                            Runnable onPlaceSound,
                            Runnable onRemoveSound) {
        this.logicGrid            = logicGrid;
        this.bank                 = bank;
        this.isShopVisible        = isShopVisible;
        this.getActiveSelection   = getActiveSelection;
        this.getInventoryCount    = getInventoryCount;
        this.consumeFromInventory = consumeFromInventory;
        this.returnToInventory    = returnToInventory;
        this.refreshUI            = refreshUI;
        this.onHintText           = onHintText;
        this.onPlaceSound         = onPlaceSound;
        this.onRemoveSound        = onRemoveSound;
    }

    // ==========================================
    // Accessors
    // ==========================================

    /** @return The current rotational direction newly placed machines will face. */
    public Direction     getPlacementFacing() { return placementFacing; }

    /** @return The current interaction mode (e.g., BUILD or REMOVE). */
    public PlacementMode getPlacementMode()   { return placementMode;   }

    /** @return The last recorded X coordinate of the mouse in world space. */
    public double        getMouseWorldX()     { return mouseWorldX;     }

    /** @return The last recorded Y coordinate of the mouse in world space. */
    public double        getMouseWorldY()     { return mouseWorldY;     }

    // ==========================================
    // Mode Switching
    // ==========================================

    /**
     * Updates the current placement mode and immediately refreshes the UI hint text to match.
     *
     * @param mode The new placement mode (BUILD or REMOVE).
     */
    public void setMode(PlacementMode mode) {
        this.placementMode = mode;
        updateHint();
    }

    // ==========================================
    // Facing
    // ==========================================

    /**
     * Rotates the placement facing direction 90 degrees clockwise and updates the UI hint text.
     */
    public void cyclePlacementFacing() {
        placementFacing = switch (placementFacing) {
            case RIGHT -> Direction.DOWN;
            case DOWN  -> Direction.LEFT;
            case LEFT  -> Direction.UP;
            case UP    -> Direction.RIGHT;
        };
        updateHint();
    }

    // ==========================================
    // Hint Text
    // ==========================================

    /**
     * Generates contextual instruction text based on the current mode, selection, and facing,
     * then pushes it to the UI via the configured callback.
     */
    public void updateHint() {
        if (onHintText == null) return;

        if (placementMode == PlacementMode.REMOVE) {
            onHintText.accept("REMOVE mode — click a machine to remove it.");
            return;
        }

        MachineType selection = getActiveSelection.get();
        int qty = getInventoryCount.apply(selection);
        onHintText.accept(
                selection == MachineType.NONE
                        ? "Pick a slot from inventory. Facing: " + placementFacing + " (R rotates)."
                        : "Selected: " + selection + " (x" + qty + ") | Facing: " + placementFacing + " (R rotates)"
        );
    }

    // ==========================================
    // Canvas Event Handlers
    // ==========================================

    /**
     * Processes a mouse click on the game canvas, translating the screen coordinates to grid coordinates,
     * and delegates to the appropriate build or remove handler.
     *
     * @param event The mouse event containing the click coordinates.
     */
    public void handleCanvasClick(MouseEvent event) {
        if (isShopVisible.getAsBoolean()) return;

        int gx = (int) Math.floor(event.getX() / TILE_SIZE);
        int gy = (int) Math.floor(event.getY() / TILE_SIZE);
        if (!logicGrid.isInside(gx, gy)) return;

        if (placementMode == PlacementMode.REMOVE) {
            handleRemove(gx, gy);
        } else {
            handleBuild(gx, gy);
        }
    }

    /**
     * Tracks the mouse movement across the canvas to update world coordinates,
     * primarily used by the rendering engine to draw the placement hologram.
     *
     * @param event The mouse event containing the current hover coordinates.
     */
    public void handleCanvasMouseMove(MouseEvent event) {
        if (isShopVisible.getAsBoolean()) return;
        mouseWorldX = event.getX();
        mouseWorldY = event.getY();
    }

    // ==========================================
    // Private helpers
    // ==========================================

    /**
     * Attempts to place the currently selected machine at the specified grid coordinates.
     * Validates inventory counts and triggers consumption and audio callbacks upon success.
     *
     * @param gx The target X coordinate on the grid.
     * @param gy The target Y coordinate on the grid.
     */
    private void handleBuild(int gx, int gy) {
        MachineType selection = getActiveSelection.get();
        if (selection == MachineType.NONE) return;
        if (getInventoryCount.apply(selection) <= 0) {
            refreshUI.run();
            return;
        }

        Machine toPlace = createMachine(selection);
        if (logicGrid.placeMachine(gx, gy, toPlace)) {
            consumeFromInventory.accept(selection);
            if (onPlaceSound != null) onPlaceSound.run();
        }
    }

    /**
     * Attempts to remove an existing machine from the specified grid coordinates.
     * Validates machine presence and triggers inventory refunds and audio callbacks upon success.
     *
     * @param gx The target X coordinate on the grid.
     * @param gy The target Y coordinate on the grid.
     */
    private void handleRemove(int gx, int gy) {
        Machine existing = logicGrid.getMachine(gx, gy);
        if (existing == null) return;

        MachineType type = existing.getType();
        if (logicGrid.removeMachine(gx, gy)) {
            if (type != MachineType.NONE) returnToInventory.accept(type);
            if (onRemoveSound != null) onRemoveSound.run();
            refreshUI.run();
        }
    }

    /**
     * Instantiates a new logic model for a machine based on its type and current configuration.
     *
     * @param type The enum type of the machine to instantiate.
     * @return A newly constructed Machine instance ready for placement.
     */
    private Machine createMachine(MachineType type) {
        return type.create(placementFacing, bank);
    }
}