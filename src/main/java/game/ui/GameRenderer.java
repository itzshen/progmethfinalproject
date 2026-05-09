package game.ui;

import game.logic.*;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the visual rendering of the game state onto the JavaFX Canvas.
 * Responsible for caching images, drawing the grid environment, rendering machines and items,
 * and displaying the placement hologram.
 */
public class GameRenderer {

    private static final double TILE_SIZE = GameConstants.TILE_SIZE;
    private final Map<String, Image> imageCache = new HashMap<>();

    private Image gridTexture;

    /**
     * Initializes the rendering engine and preloads all required graphical assets into memory.
     */
    public GameRenderer() {
        preloadImages();
    }

    /**
     * Iterates through all MachineType and ItemType enums to load and cache their respective images.
     * Generates fallback placeholder images if non-essential graphical files are missing.
     */
    private void preloadImages() {
        for (MachineType type : MachineType.values()) {
            if (type == MachineType.NONE || type.getCategory() == null) continue;

            if (type.getFallBackText() == null) {
                imageCache.put(type.getImageName(), loadRequiredAsset(type.getImageName(), "machine"));
            } else {
                imageCache.put(type.getImageName(), getImage(type.getImageName(), type.getFallBackText(), "machine"));
            }
        }

        for (ItemType item : ItemType.values()) {
            String fallback = item.name().substring(0, Math.min(2, item.name().length()));
            fallback = fallback.substring(0, 1).toUpperCase() + fallback.substring(1).toLowerCase();
            imageCache.put(item.getImageName(), getImage(item.getImageName(), fallback, "item"));
        }

        gridTexture = getImage("Grid.png", "Grid", "environment");
    }

    /**
     * Loads an essential graphical asset from the resources directory.
     * * @param fileName The exact file name of the image.
     * @param subfolder The resource subfolder containing the image.
     * @return The loaded JavaFX Image.
     * @throws RuntimeException if the asset is missing, forcing an application exit.
     */
    private Image loadRequiredAsset(String fileName, String subfolder) {
        String path = "/images/" + subfolder + "/" + fileName;
        InputStream stream = GameRenderer.class.getResourceAsStream(path);
        if (stream == null) {
            System.err.println("CRITICAL FAILURE: Missing required asset: " + path);
            System.exit(1);
        }
        return new Image(stream);
    }

    /**
     * Attempts to load an image from the resources directory, falling back to a procedural placeholder if absent.
     *
     * @param filename The exact file name of the image.
     * @param fallbackText The text to display on the generated placeholder if loading fails.
     * @param subfolder The resource subfolder containing the image.
     * @return The loaded JavaFX Image, or a generated placeholder.
     */
    private Image getImage(String filename, String fallbackText, String subfolder) {
        String path = "/images/" + subfolder + "/" + filename;
        try (InputStream stream = GameRenderer.class.getResourceAsStream(path)) {
            if (stream != null) {
                Image loaded = new Image(stream);
                if (!loaded.isError()) return loaded;
            }
        } catch (Exception ignored) {}
        return createPlaceholderImage(fallbackText);
    }

    /**
     * Generates a basic procedural graphical placeholder for missing assets.
     *
     * @param text The short text identifier to render on the placeholder.
     * @return A WritableImage containing the generated placeholder graphic.
     */
    private Image createPlaceholderImage(String text) {
        int w = 50, h = 50;
        Canvas c = new Canvas(w, h);
        GraphicsContext g = c.getGraphicsContext2D();
        g.setFill(Color.WHITE); g.fillRect(0, 0, w, h);
        g.setStroke(Color.BLACK); g.strokeRect(0.5, 0.5, w - 1, h - 1);
        g.setFill(Color.BLACK); g.setFont(Font.font(11));
        g.fillText(text == null ? "?" : text, 6, 28);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage snap = c.snapshot(params, null);
        return snap == null ? new WritableImage(w, h) : snap;
    }

    /**
     * Retrieves the cached image associated with a specific machine type.
     *
     * @param s The MachineType to look up.
     * @return The cached JavaFX Image for the given machine.
     */
    public Image imageForMachineType(MachineType s) {
        return imageCache.get(s.getImageName());
    }

    /**
     * Converts a Direction enum into its corresponding rotation angle in degrees.
     *
     * @param d The facing direction.
     * @return The angle in degrees (0, 90, 180, or 270).
     */
    private double facingAngle(Direction d) {
        return switch (d) {
            case RIGHT -> 0; case DOWN -> 90; case LEFT -> 180; case UP -> 270;
        };
    }

    /**
     * Draws an image onto the graphics context at a specified position with a designated rotation.
     *
     * @param gc The GraphicsContext to draw onto.
     * @param img The image to draw.
     * @param x The top-left X coordinate.
     * @param y The top-left Y coordinate.
     * @param w The width to draw the image.
     * @param h The height to draw the image.
     * @param degrees The rotation angle in degrees.
     */
    private void drawImageRotated(GraphicsContext gc, Image img, double x, double y, double w, double h, double degrees) {
        if (img == null) return;
        gc.save();
        gc.translate(x + w / 2.0, y + h / 2.0);
        gc.rotate(degrees);
        gc.drawImage(img, -w / 2.0, -h / 2.0, w, h);
        gc.restore();
    }

    /**
     * Renders the complete game state, including the grid floor, grid lines, placed machines,
     * items currently on conveyors, and the interactive placement hologram.
     *
     * @param shopVisible      true when the shop popup is open
     * @param inventoryVisible true when the inventory bar is open
     * @param placementMode    current placement mode (BUILD / REMOVE)
     */
    public void render(Canvas gameCanvas, GridSystem logicGrid, ShopManager shopManager,
                       double mouseWorldX, double mouseWorldY, Direction placementFacing,
                       boolean shopVisible, boolean inventoryVisible, PlacementMode placementMode) {

        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        double worldW = logicGrid.getWidth() * TILE_SIZE;
        double worldH = logicGrid.getHeight() * TILE_SIZE;

        // Draw Floor
        for (int x = 0; x < logicGrid.getWidth(); x++) {
            for (int y = 0; y < logicGrid.getHeight(); y++) {
                gc.drawImage(gridTexture, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw Grid Lines
        gc.setStroke(Color.color(0.25, 0.28, 0.32));
        gc.setLineWidth(1);
        for (int gx = 0; gx <= logicGrid.getWidth(); gx++) gc.strokeLine(gx * TILE_SIZE, 0, gx * TILE_SIZE, worldH);
        for (int gy = 0; gy <= logicGrid.getHeight(); gy++) gc.strokeLine(0, gy * TILE_SIZE, worldW, gy * TILE_SIZE);

        // Draw Machines and Items
        for (int x = 0; x < logicGrid.getWidth(); x++) {
            for (int y = 0; y < logicGrid.getHeight(); y++) {
                Machine m = logicGrid.getMachine(x, y);
                if (m == null) continue;

                double px = x * TILE_SIZE, py = y * TILE_SIZE;
                drawImageRotated(gc, imageForMachineType(m.getType()), px, py, TILE_SIZE, TILE_SIZE, facingAngle(m.getFacing()));

                if (m.getCurrentItem() != null) {
                    double inset = TILE_SIZE * 0.2;
                    String exactImageName = m.getCurrentItem().getType().getImageName();
                    Image exactItemImg = imageCache.get(exactImageName);
                    drawImageRotated(gc, exactItemImg, px + inset * 0.5, py + inset * 0.5, TILE_SIZE - inset, TILE_SIZE - inset, 0);
                }
            }
        }

        // Draw Hologram — only when inventory bar is open and in BUILD mode
        renderHologram(gc, logicGrid, shopManager, mouseWorldX, mouseWorldY,
                placementFacing, shopVisible, inventoryVisible, placementMode);
    }

    /**
     * Renders a semi-transparent preview of the currently selected machine at the mouse cursor's grid location.
     * Highlights green (or blue) for valid placements and red for invalid placements.
     *
     * @param gc The GraphicsContext to draw onto.
     * @param logicGrid The game grid used to validate placement.
     * @param shopManager The manager determining current inventory counts and active selection.
     * @param mouseWorldX The current X coordinate of the mouse in world space.
     * @param mouseWorldY The current Y coordinate of the mouse in world space.
     * @param placementFacing The current rotation direction for the new machine.
     * @param shopVisible True if the shop menu is obscuring the screen.
     * @param inventoryVisible True if the player is actively using the build inventory.
     * @param placementMode The current interaction mode (must be BUILD to render).
     */
    private void renderHologram(GraphicsContext gc, GridSystem logicGrid, ShopManager shopManager,
                                double mouseWorldX, double mouseWorldY, Direction placementFacing,
                                boolean shopVisible, boolean inventoryVisible, PlacementMode placementMode) {

        // Hologram only shows when: inventory bar is open, not in shop, in BUILD mode, something selected
        if (shopVisible) return;
        if (!inventoryVisible) return;
        if (placementMode != PlacementMode.BUILD) return;
        if (shopManager.getActiveSelection() == MachineType.NONE) return;

        int gx = (int) Math.floor(mouseWorldX / TILE_SIZE);
        int gy = (int) Math.floor(mouseWorldY / TILE_SIZE);
        if (!logicGrid.isInside(gx, gy)) return;

        boolean valid = logicGrid.getMachine(gx, gy) == null
                && shopManager.getInventoryCount(shopManager.getActiveSelection()) > 0;
        double px = gx * TILE_SIZE, py = gy * TILE_SIZE;

        gc.save();
        gc.setGlobalAlpha(0.4);
        drawImageRotated(gc, imageForMachineType(shopManager.getActiveSelection()),
                px, py, TILE_SIZE, TILE_SIZE, facingAngle(placementFacing));
        gc.setFill(valid ? Color.web("#3498db", 0.4) : Color.web("#e74c3c", 0.5));
        gc.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        gc.restore();
    }
}