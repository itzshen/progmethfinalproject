package game.ui;

import game.logic.MachineCategory;
import game.logic.MachineType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builds and manages the tabbed bottom inventory bar.
 * One Tab per MachineCategory; tiles are added on the first purchase only.
 */
public class InventoryUIBuilder {

    private static final double TILE_W   = 80;
    private static final double TILE_H   = 90;
    private static final double IMG_SIZE = 46;

    private final Function<MachineType, Image> imageFor;
    private final Consumer<MachineType>         onSelect;

    // Per-type UI references
    private final Map<MachineType, StackPane> tiles         = new EnumMap<>(MachineType.class);
    private final Map<MachineType, Label>     quantityLabels = new EnumMap<>(MachineType.class);

    // One TilePane per category
    private final Map<MachineCategory, TilePane> categoryPanes = new EnumMap<>(MachineCategory.class);

    // ==========================================
    // Constructor
    // ==========================================

    /**
     * Initializes the inventory UI builder and constructs the category tabs.
     *
     * @param tabPane The base TabPane to populate with inventory categories.
     * @param imageFor Function to retrieve the graphical asset for a specific machine type.
     * @param onSelect Callback executed when the player clicks a machine tile to select it.
     */
    public InventoryUIBuilder(TabPane tabPane,
                              Function<MachineType, Image> imageFor,
                              Consumer<MachineType> onSelect) {
        this.imageFor = imageFor;
        this.onSelect = onSelect;
        buildTabs(tabPane);
    }

    // ==========================================
    // Public API
    // ==========================================

    /**
     * Adds a tile for this type if it has never been shown before.
     * Call this after the first successful purchase of a type.
     */
    public void ensureVisible(MachineType type) {
        if (type == MachineType.NONE || tiles.containsKey(type)) return;

        MachineCategory cat  = type.getCategory();
        TilePane        pane = categoryPanes.get(cat);
        if (pane == null) return;

        StackPane tile = buildTile(type);
        tiles.put(type, tile);
        pane.getChildren().add(tile);
    }

    /**
     * Refreshes quantity badges and active/empty styling on all visible tiles.
     */
    public void refreshSlots(Map<MachineType, Integer> inventory, MachineType activeSelection) {
        for (Map.Entry<MachineType, Label> entry : quantityLabels.entrySet()) {
            MachineType type  = entry.getKey();
            Label       label = entry.getValue();
            int         qty   = inventory.getOrDefault(type, 0);

            label.setText("x" + qty);

            StackPane tile = tiles.get(type);
            if (tile != null) {
                tile.getStyleClass().removeAll("inv-tile-active", "inv-tile-empty");
                if (type == activeSelection) {
                    tile.getStyleClass().add("inv-tile-active");
                } else if (qty == 0) {
                    tile.getStyleClass().add("inv-tile-empty");
                }
            }
        }
    }

    // ==========================================
    // Private builders
    // ==========================================

    /**
     * Generates the scrollable UI tabs for each available machine category.
     *
     * @param tabPane The parent TabPane to attach the newly created tabs to.
     */
    private void buildTabs(TabPane tabPane) {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        for (MachineCategory cat : MachineCategory.values()) {
            TilePane tilePane = new TilePane();
            tilePane.setHgap(6);
            tilePane.setVgap(6);
            tilePane.setPadding(new Insets(6));
            tilePane.setPrefTileWidth(TILE_W);
            tilePane.setPrefTileHeight(TILE_H);

            ScrollPane scroll = new ScrollPane(tilePane);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setFitToHeight(true);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            Tab tab = new Tab(cat.getDisplayName(), scroll);
            tabPane.getTabs().add(tab);
            categoryPanes.put(cat, tilePane);
        }
    }

    /**
     * Constructs a clickable visual tile for a machine, including its image, name, and quantity badge.
     *
     * @param type The specific machine type to represent on this tile.
     * @return A fully styled StackPane representing the interactive inventory slot.
     */
    private StackPane buildTile(MachineType type) {
        // Image
        ImageView iv = new ImageView();
        Image img = imageFor.apply(type);
        if (img != null) iv.setImage(img);
        iv.setFitWidth(IMG_SIZE);
        iv.setFitHeight(IMG_SIZE);
        iv.setPreserveRatio(true);

        // Name label
        Label nameLabel = new Label(type.getFallBackText());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        nameLabel.setTextFill(Color.web("#ecf0f1"));
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(TILE_W - 8);
        nameLabel.setAlignment(Pos.CENTER);

        // Quantity badge (top-left corner)
        Label qtyLabel = new Label("x0");
        qtyLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        qtyLabel.setTextFill(Color.WHITE);
        qtyLabel.setStyle(
                "-fx-background-color: rgba(0,0,0,0.6);" +
                        "-fx-background-radius: 3;" +
                        "-fx-padding: 1 4 1 4;"
        );
        quantityLabels.put(type, qtyLabel);

        // Inner layout: image on top, name below
        VBox inner = new VBox(3, iv, nameLabel);
        inner.setAlignment(Pos.CENTER);

        // Outer tile
        StackPane tile = new StackPane();
        tile.setPrefSize(TILE_W, TILE_H);
        tile.setMinSize(TILE_W, TILE_H);
        tile.setMaxSize(TILE_W, TILE_H);
        tile.getStyleClass().add("inv-tile");
        tile.getChildren().addAll(inner, qtyLabel);
        StackPane.setAlignment(qtyLabel, Pos.TOP_LEFT);
        StackPane.setMargin(qtyLabel, new Insets(4, 0, 0, 4));

        tile.setOnMouseClicked(e -> onSelect.accept(type));

        return tile;
    }
}