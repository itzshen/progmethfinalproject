package game.ui;

import game.logic.MachineType;
import game.logic.PlayerBank;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public class ShopManager {

    private final PlayerBank bank;
    private final Map<MachineType, Integer> inventory = new EnumMap<>(MachineType.class);
    private MachineType activeSelection = MachineType.NONE;

    private final HBox inventoryBox;
    private final Label moneyLabel;
    private final Function<MachineType, Image> imageLookup;
    private final Runnable onStateChange;

    private final Map<MachineType, VBox> slotContainers = new EnumMap<>(MachineType.class);
    private final Map<MachineType, Label> qtyLabels = new EnumMap<>(MachineType.class);

    public ShopManager(PlayerBank bank, HBox inventoryBox, Label moneyLabel,
                       Function<MachineType, Image> imageLookup, Runnable onStateChange) {
        this.bank = bank;
        this.inventoryBox = inventoryBox;
        this.moneyLabel = moneyLabel;
        this.imageLookup = imageLookup;
        this.onStateChange = onStateChange;

        if (this.inventoryBox != null) {
            this.inventoryBox.getChildren().clear(); // Clear any FXML placeholder data once
        }

        for (MachineType type : MachineType.values()) {
            if (type != MachineType.NONE) {
                inventory.put(type, 0);
                if (this.inventoryBox != null) {
                    buildInventorySlot(type);
                }
            }
        }

        refreshUI();
    }

    public MachineType getActiveSelection() { return activeSelection; }
    public int getInventoryCount(MachineType type) { return inventory.getOrDefault(type, 0); }

    public void attemptBuy(MachineType type) {
        if (type == MachineType.NONE) return;

        if (bank.trySpend(type.getCost())) {
            inventory.put(type, inventory.get(type) + 1);
            refreshUI();
        }
    }

    public void consumeFromInventory(MachineType type) {
        int current = inventory.getOrDefault(type, 0);
        if (current > 0) {
            inventory.put(type, current - 1);
            refreshUI();
        }
    }

    public void refreshUI() {
        if (moneyLabel != null) {
            moneyLabel.setText(String.format("Balance: $%.0f", bank.getBalance()));
        }

        for (MachineType type : MachineType.values()) {
            if (type == MachineType.NONE) continue;

            int qty = inventory.getOrDefault(type, 0);
            VBox slot = slotContainers.get(type);
            Label label = qtyLabels.get(type);

            if (slot != null && label != null) {
                label.setText("x" + qty);
                slot.getStyleClass().removeAll("inventory-slot-empty", "inventory-slot-active");

                if (qty == 0) {
                    slot.getStyleClass().add("inventory-slot-empty");
                }
                if (activeSelection == type) {
                    slot.getStyleClass().add("inventory-slot-active");
                }
            }
        }

        if (onStateChange != null) {
            onStateChange.run();
        }
    }

    private void buildInventorySlot(MachineType type) {
        VBox slot = new VBox(4);
        slot.getStyleClass().add("inventory-slot");

        ImageView icon = new ImageView(imageLookup.apply(type));
        icon.setFitWidth(40);
        icon.setFitHeight(40);
        icon.setPreserveRatio(true);

        Label qtyLabel = new Label("x0");
        qtyLabel.setTextFill(Color.web("#ecf0f1"));
        qtyLabel.setFont(Font.font(12));

        slot.getChildren().addAll(icon, qtyLabel);

        slot.setOnMouseClicked(e -> {
            activeSelection = (activeSelection == type) ? MachineType.NONE : type;
            refreshUI();
        });

        VBox.setVgrow(icon, Priority.NEVER);

        slotContainers.put(type, slot);
        qtyLabels.put(type, qtyLabel);

        inventoryBox.getChildren().add(slot);
    }
}