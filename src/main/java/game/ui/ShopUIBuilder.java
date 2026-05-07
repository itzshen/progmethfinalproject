package game.ui;

import game.logic.MachineCategory;
import game.logic.MachineType;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Stateless builder that populates a TabPane with shop buttons.
 *
 * Separated from GameController because building shop UI has nothing
 * to do with game loop, input, or placement — it runs exactly once
 * at startup and then steps aside.
 *
 * All data flows in through parameters; this class holds no state
 * and has no references to GameController or ShopManager.
 */
public class ShopUIBuilder {

    // Private constructor — static utility class, not meant to be instantiated
    private ShopUIBuilder() {}

    /**
     * Clears and rebuilds all tabs and buy-buttons inside the given TabPane.
     *
     * @param shopTabPane  The FXML TabPane to populate
     * @param imageLookup  Maps MachineType → its icon Image (from GameRenderer)
     * @param onBuy        Called with the chosen MachineType when a buy button is clicked
     */
    public static void build(TabPane shopTabPane,
                             Function<MachineType, Image> imageLookup,
                             Consumer<MachineType> onBuy) {
        if (shopTabPane == null) return;
        shopTabPane.getTabs().clear();

        // 1. One Tab + TilePane per category
        var categoryPanes = new java.util.EnumMap<MachineCategory, TilePane>(MachineCategory.class);

        for (MachineCategory cat : MachineCategory.values()) {
            TilePane tilePane = createTilePane();

            ScrollPane scrollPane = new ScrollPane(tilePane);
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            Tab tab = new Tab(cat.getDisplayName());
            tab.setContent(scrollPane);
            tab.setClosable(false);

            shopTabPane.getTabs().add(tab);
            categoryPanes.put(cat, tilePane);
        }

        // 2. One buy-button per MachineType, dropped into its category's TilePane
        for (MachineType type : MachineType.values()) {
            if (type == MachineType.NONE || type.getCategory() == null) continue;

            Button buyBtn = buildBuyButton(type, imageLookup, onBuy);
            categoryPanes.get(type.getCategory()).getChildren().add(buyBtn);
        }
    }

    // ==========================================
    // Private Helpers
    // ==========================================

    private static TilePane createTilePane() {
        TilePane pane = new TilePane();
        pane.setPrefColumns(2);
        pane.setHgap(10.0);
        pane.setVgap(10.0);
        pane.setStyle("-fx-padding: 10;");
        return pane;
    }

    private static Button buildBuyButton(MachineType type,
                                         Function<MachineType, Image> imageLookup,
                                         Consumer<MachineType> onBuy) {
        Button btn = new Button("Buy " + type.name() + " ($" + type.getCost() + ")");
        btn.getStyleClass().add("shop-button");

        ImageView icon = new ImageView(imageLookup.apply(type));
        icon.setFitWidth(32);
        icon.setFitHeight(32);
        btn.setGraphic(icon);

        btn.setOnAction(e -> onBuy.accept(type));
        return btn;
    }
}