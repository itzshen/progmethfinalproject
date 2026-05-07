package game.ui;

import javafx.animation.AnimationTimer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleConsumer;

/**
 * Owns the two game loops:
 *   1. Render loop  — AnimationTimer, fires every frame, passes delta-time (seconds) to onRenderFrame
 *   2. Logic loop   — ScheduledExecutorService, fires on a fixed interval, calls onLogicTick
 *
 * GameLoopManager knows nothing about game state; it only drives timing
 * and delegates all real work through callbacks.
 */
public class GameLoopManager {

    // ==========================================
    // Constants
    // ==========================================
    public static final double PAN_SPEED_PX_PER_SEC = 280.0;
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
    public GameLoopManager(DoubleConsumer onRenderFrame, Runnable onLogicTick) {
        this.onRenderFrame = onRenderFrame;
        this.onLogicTick   = onLogicTick;
    }

    // ==========================================
    // Lifecycle
    // ==========================================

    public void start() {
        startRenderLoop();
        startLogicLoop();
    }

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

    public boolean isRunning() {
        return renderLoop != null;
    }

    // ==========================================
    // Private — Loop Setup
    // ==========================================

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
