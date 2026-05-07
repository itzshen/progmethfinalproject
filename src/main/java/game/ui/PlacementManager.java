package game.ui;

import game.logic.*;
import javafx.scene.input.MouseEvent;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Owns all machine-placement state and logic:
 *   - Placement facing direction and cycling
 *   - Mouse world position tracking
 *   - Canvas click → place machine on grid
 *   - Shop hint text (depends on facing, so it lives here)
 * Talks to ShopManager and GridSystem exclusively through
 * constructor-injected callbacks to stay loosely coupled.
 */
public class PlacementManager {

    // ==========================================
    // Constants
    // ==========================================
    private static final double TILE_SIZE = GameConstants.TILE_SIZE;

    // ==========================================
    // Placement State
    // ==========================================
    private Direction placementFacing = Direction.RIGHT;
    private double mouseWorldX;
    private double mouseWorldY;

    // ==========================================
    // Hard Dependencies
    // ==========================================
    private final GridSystem logicGrid;
    private final PlayerBank bank;

    // ==========================================
    // Callbacks into ShopManager / UI
    // ==========================================
    private final BooleanSupplier          isShopVisible;
    private final Supplier<MachineType>    getActiveSelection;
    private final Function<MachineType, Integer> getInventoryCount;
    private final Consumer<MachineType>    consumeFromInventory;
    private final Runnable                 refreshUI;
    private final Consumer<String>         onHintText; // writes to shopHintLabel

    // ==========================================
    // Constructor
    // ==========================================
    public PlacementManager(GridSystem logicGrid,
                            PlayerBank bank,
                            BooleanSupplier isShopVisible,
                            Supplier<MachineType> getActiveSelection,
                            Function<MachineType, Integer> getInventoryCount,
                            Consumer<MachineType> consumeFromInventory,
                            Runnable refreshUI,
                            Consumer<String> onHintText) {
        this.logicGrid           = logicGrid;
        this.bank                = bank;
        this.isShopVisible       = isShopVisible;
        this.getActiveSelection  = getActiveSelection;
        this.getInventoryCount   = getInventoryCount;
        this.consumeFromInventory = consumeFromInventory;
        this.refreshUI           = refreshUI;
        this.onHintText          = onHintText;
    }

    // ==========================================
    // Accessors
    // ==========================================
    public Direction getPlacementFacing() { return placementFacing; }
    public double    getMouseWorldX()     { return mouseWorldX; }
    public double    getMouseWorldY()     { return mouseWorldY; }

    // ==========================================
    // Facing
    // ==========================================
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

    /** Recomputes and pushes the hint string to the UI label. */
    public void updateHint() {
        if (onHintText == null) return;
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
    public void handleCanvasClick(MouseEvent event) {
        if (isShopVisible.getAsBoolean()) return;

        MachineType selection = getActiveSelection.get();
        if (selection == MachineType.NONE) return;

        if (getInventoryCount.apply(selection) <= 0) {
            refreshUI.run();
            return;
        }

        int gx = (int) Math.floor(event.getX() / TILE_SIZE);
        int gy = (int) Math.floor(event.getY() / TILE_SIZE);
        if (!logicGrid.isInside(gx, gy)) return;

        Machine toPlace = createMachine(selection);
        if (logicGrid.placeMachine(gx, gy, toPlace)) {
            consumeFromInventory.accept(selection);
        }
    }

    public void handleCanvasMouseMove(MouseEvent event) {
        if (isShopVisible.getAsBoolean()) return;
        mouseWorldX = event.getX();
        mouseWorldY = event.getY();
    }

    // ==========================================
    // Private
    // ==========================================
    private Machine createMachine(MachineType type) {
        return type.create(placementFacing, bank);
    }
}