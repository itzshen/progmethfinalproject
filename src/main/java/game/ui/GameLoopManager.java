package game.ui;

import javafx.animation.AnimationTimer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

/**
 * Owns the two game loops:
 * 1. Render loop  — AnimationTimer, fires every frame, passes delta-time (seconds) to onRenderFrame
 * 2. Logic loop   — ScheduledExecutorService, fires on a fixed interval, calls onLogicTick
 *
 * GameLoopManager knows nothing about game state; it only drives timing
 * and delegates all real work through callbacks.
 */
public class GameLoopManager {

    // ==========================================
    // Constants
    // ==========================================

    /** The speed at which the camera pans across the game world, measured in pixels per second. */
    public static final double PAN_SPEED_PX_PER_SEC = 280.0;

    /** The fixed time interval (in seconds) between each execution of the game logic loop. */
    private static final double LOGIC_INTERVAL_SEC  = 0.5;

    // ==========================================
    // Callbacks
    // ==========================================
    private final DoubleConsumer onRenderFrame; // Receives dtSec — caller handles camera + render
    private final Runnable       onLogicTick;   // Fixed-rate game logic (grid tick, UI refresh)

    // ==========================================
    // Loop State
    // ==========================================
    private AnimationTimer           renderLoop;
    private ScheduledExecutorService logicThread;
    private long                     lastFrameNanos;

    // ==========================================
    // Constructor
    // ==========================================

    /**
     * Constructs a new GameLoopManager to coordinate timing for visuals and mechanics.
     *
     * @param onRenderFrame Callback executed every UI frame, receiving the elapsed time (delta-time) in seconds.
     * @param onLogicTick Callback executed at a fixed interval to process game state updates.
     */
    public GameLoopManager(DoubleConsumer onRenderFrame, Runnable onLogicTick) {
        this.onRenderFrame = onRenderFrame;
        this.onLogicTick   = onLogicTick;
    }

    // ==========================================
    // Lifecycle
    // ==========================================

    /**
     * Starts the game engine by launching both the rendering timer and the background logic thread.
     */
    public void start() {
        startRenderLoop();
        startLogicLoop();
    }

    /**
     * Safely halts both loops. Shuts down the logic executor thread and stops the animation timer.
     */
    public void stop() {
        if (renderLoop != null) {
            renderLoop.stop();
            renderLoop = null;
        }
        if (logicThread != null && !logicThread.isShutdown()) {
            logicThread.shutdownNow();
            logicThread = null;
        }
    }

    /**
     * Checks the current running state of the game loop.
     *
     * @return true if the engine is actively running, false otherwise.
     */
    public boolean isRunning() {
        return renderLoop != null;
    }

    // ==========================================
    // Private — Loop Setup
    // ==========================================

    /**
     * Initializes and starts the JavaFX AnimationTimer for frame rendering.
     */
    private void startRenderLoop() {
        lastFrameNanos = 0;
        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double dtSec = (lastFrameNanos == 0) ? 0 : (now - lastFrameNanos) / 1_000_000_000.0;
                lastFrameNanos = now;
                onRenderFrame.accept(dtSec);
            }
        };
        renderLoop.start();
    }

    /**
     * Initializes and starts the scheduled executor service for processing background game logic at a fixed rate.
     */
    private void startLogicLoop() {
        logicThread = Executors.newSingleThreadScheduledExecutor();
        long intervalMs = (long) (LOGIC_INTERVAL_SEC * 1000);
        logicThread.scheduleAtFixedRate(() -> {
            try {
                onLogicTick.run();
            } catch (Exception e) {
                System.err.println("Error in Logic Thread:");
                e.printStackTrace();
            }
        }, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
    }
}