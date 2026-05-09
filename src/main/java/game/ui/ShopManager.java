package game.ui;

import game.logic.MachineType;
import game.logic.PlayerBank;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Manages the player's economy and inventory state.
 * Acts as the bridge between the logical PlayerBank and the visual Shop/Inventory UI,
 * handling purchases, inventory consumption, and UI synchronization.
 */
public class ShopManager {

    // ==========================================
    // Core state
    // ==========================================
    private final PlayerBank bank;
    private final Map<MachineType, Integer> inventory = new EnumMap<>(MachineType.class);

    /** Types bought at least once — controls which tiles appear in the inventory bar. */
    private final Set<MachineType> everBought = EnumSet.noneOf(MachineType.class);

    private MachineType activeSelection = MachineType.NONE;

    // ==========================================
    // UI references
    // ==========================================
    private final Label moneyLabel;
    private final Function<MachineType, Image> imageLookup;
    private final Runnable onStateChange;

    private InventoryUIBuilder inventoryUI;

    // ==========================================
    // Constructor
    // ==========================================

    /**
     * Initializes the shop manager with references to the player's bank and UI callbacks.
     *
     * @param bank The logical bank handling the player's currency.
     * @param moneyLabel The UI label displaying the current balance.
     * @param imageLookup Function to retrieve images for inventory tiles.
     * @param onStateChange Callback triggered whenever inventory or balance updates occur.
     */
    public ShopManager(PlayerBank bank,
                       Label moneyLabel,
                       Function<MachineType, Image> imageLookup,
                       Runnable onStateChange) {
        this.bank          = bank;
        this.moneyLabel    = moneyLabel;
        this.imageLookup   = imageLookup;
        this.onStateChange = onStateChange;

        for (MachineType type : MachineType.values()) {
            if (type != MachineType.NONE) inventory.put(type, 0);
        }
    }

    // ==========================================
    // Inventory bar wiring
    // ==========================================

    /**
     * Links the visual inventory bar to this manager's internal state.
     *
     * @param inventoryTabPane The TabPane container for the inventory UI.
     */
    public void initInventoryUI(TabPane inventoryTabPane) {
        inventoryUI = new InventoryUIBuilder(
                inventoryTabPane,
                imageLookup,
                this::handleTileClick
        );
    }

    // ==========================================
    // Shop actions
    // ==========================================

    /**
     * Attempts to purchase one unit of the specified machine.
     * Deducts currency and adds to inventory if funds are sufficient.
     *
     * @param type The machine type to purchase.
     */
    public void attemptBuy(MachineType type) {
        if (type == MachineType.NONE) return;

        if (bank.trySpend(type.getCost())) {
            inventory.put(type, inventory.get(type) + 1);

            if (!everBought.contains(type)) {
                everBought.add(type);
                if (inventoryUI != null) inventoryUI.ensureVisible(type);
            }

            refreshUI();
        }
    }

    /**
     * Deducts one unit of the specified machine type from the player's inventory.
     * Called when a machine is successfully placed on the grid.
     *
     * @param type The machine type to consume.
     */
    public void consumeFromInventory(MachineType type) {
        int current = inventory.getOrDefault(type, 0);
        if (current > 0) {
            inventory.put(type, current - 1);
            refreshUI();
        }
    }

    /**
     * Returns one unit of {@code type} to the player's inventory.
     * Called when a placed machine is removed from the grid.
     * If this is somehow the first time the type is seen, its tile is also revealed.
     */
    public void returnToInventory(MachineType type) {
        if (type == MachineType.NONE) return;
        inventory.merge(type, 1, Integer::sum);

        if (!everBought.contains(type)) {
            everBought.add(type);
            if (inventoryUI != null) inventoryUI.ensureVisible(type);
        }

        refreshUI();
    }

    // ==========================================
    // Selection
    // ==========================================

    /**
     * @return The machine type currently selected by the player for placement.
     */
    public MachineType getActiveSelection() { return activeSelection; }

    /**
     * Checks how many units of a specific machine type the player currently owns.
     *
     * @param type The machine type to query.
     * @return The quantity available in the inventory.
     */
    public int getInventoryCount(MachineType type) {
        return inventory.getOrDefault(type, 0);
    }

    /**
     * Toggles the active placement selection when an inventory tile is clicked.
     *
     * @param type The machine type corresponding to the clicked tile.
     */
    private void handleTileClick(MachineType type) {
        activeSelection = (activeSelection == type) ? MachineType.NONE : type;
        refreshUI();
    }

    // ==========================================
    // UI refresh
    // ==========================================

    /**
     * Forces an immediate visual update of the bank balance label and inventory slot quantities.
     * Triggers the external state change callback to update dependent UI elements.
     */
    public void refreshUI() {
        if (moneyLabel != null) {
            moneyLabel.setText(String.format("Balance: $%.0f", bank.getBalance()));
        }

        if (inventoryUI != null) {
            inventoryUI.refreshSlots(inventory, activeSelection);
        }

        if (onStateChange != null) {
            onStateChange.run();
        }
    }
}