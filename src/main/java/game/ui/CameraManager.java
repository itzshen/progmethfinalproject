package game.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

/**
 * Tracks camera position and zoom, then applies the view transform to the game canvas.
 */
public class CameraManager {
    private static final double ZOOM_MIN = 0.8;
    private static final double ZOOM_MAX = 2.0;

    private double x = 0.0;
    private double y = 0.0;
    private double zoom = 1.0;

    /**
     * Moves the camera by the given screen-space offset.
     *
     * @param dx horizontal movement
     * @param dy vertical movement
     */
    public void pan(double dx, double dy) {
        x += dx;
        y += dy;
    }

    /**
     * Changes zoom by a multiplier while keeping it within allowed limits.
     *
     * @param factor zoom multiplier
     */
    public void applyZoom(double factor) {
        zoom *= factor;
        zoom = Math.min(ZOOM_MAX, Math.max(ZOOM_MIN, zoom));
    }

    /**
     * Clamps the camera and applies scale/translation transforms to the canvas.
     *
     * @param canvas the canvas to transform
     * @param worldWidthPx world width in pixels
     * @param worldHeightPx world height in pixels
     */
    public void applyTransformsAndClamp(Canvas canvas, double worldWidthPx, double worldHeightPx) {
        double visualW = worldWidthPx * zoom;
        double visualH = worldHeightPx * zoom;

        // Clamp camera to screen bounds
        x = Math.max(-visualW + 200, Math.min(800, x));
        y = Math.max(-visualH + 200, Math.min(600, y));

        canvas.getTransforms().clear();
        canvas.getTransforms().addAll(
                new Scale(zoom, zoom, 0, 0),
                new Translate(x, y)
        );
    }
}