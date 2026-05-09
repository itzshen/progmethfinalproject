package game.ui.sound;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

/**
 * Centralised audio manager.
 * - AudioClip  → short SFX (preloaded, low-latency, supports overlap)
 * - MediaPlayer → background music (streamed, looping)
 *
 * All methods are safe to call even if a sound failed to load — they
 * simply no-op so a missing file never crashes the game.
 */
public class SoundManager {

    // ==========================================
    // Paths (relative to resources root)
    // ==========================================
    private static final String SFX_PATH   = "/sounds/sfx/";
    private static final String MUSIC_PATH = "/sounds/music/";
    private static final String MUSIC_FILE = "theme.mp3";

    // ==========================================
    // State
    // ==========================================
    private final Map<SoundEffect, AudioClip> clips = new EnumMap<>(SoundEffect.class);
    private MediaPlayer musicPlayer;

    private double sfxVolume   = 0.35;
    private double musicVolume = 0.03;
    private boolean sfxEnabled   = true;
    private boolean musicEnabled = true;

    // ==========================================
    // Constructor
    // ==========================================
    /**
     * Loads available sound effects and background music.
     */
    public SoundManager() {
        preloadSfx();
        loadMusic();
    }

    // ==========================================
    // Preloading
    // ==========================================

    /**
     * Loads all sound effects into memory.
     */
    private void preloadSfx() {
        for (SoundEffect effect : SoundEffect.values()) {
            String path = SFX_PATH + effect.getFileName();
            URL url = SoundManager.class.getResource(path);
            if (url != null) {
                try {
                    AudioClip clip = new AudioClip(url.toExternalForm());
                    clip.setVolume(sfxVolume);
                    clips.put(effect, clip);
                } catch (Exception e) {
                    System.err.println("Warning: Could not load SFX: " + path);
                }
            } else {
                System.err.println("Warning: SFX not found: " + path);
            }
        }
    }

    /**
     * Loads the background music file into memory.
     */
    private void loadMusic() {
        String path = MUSIC_PATH + MUSIC_FILE;
        URL url = SoundManager.class.getResource(path);
        if (url != null) {
            try {
                Media media = new Media(url.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                musicPlayer.setVolume(musicVolume);
            } catch (Exception e) {
                System.err.println("Warning: Could not load music: " + path);
            }
        } else {
            System.err.println("Warning: Music file not found: " + path);
        }
    }

    // ==========================================
    // Playback — SFX
    // ==========================================
    /**
     * Plays a short sound effect if sound effects are enabled and loaded.
     *
     * @param effect the sound effect to play
     */
    public void playSfx(SoundEffect effect) {
        if (!sfxEnabled) return;
        AudioClip clip = clips.get(effect);
        if (clip != null) clip.play();
    }

    // ==========================================
    // Playback — Music
    // ==========================================
    /**
     * Starts or resumes looping background music if available.
     */
    public void playMusic() {
        if (!musicEnabled || musicPlayer == null) return;
        musicPlayer.play();
    }

    /**
     * Pauses background music if it is loaded.
     */
    public void pauseMusic() {
        if (musicPlayer != null) musicPlayer.pause();
    }

    /**
     * Stops background music if it is loaded.
     */
    public void stopMusic() {
        if (musicPlayer != null) musicPlayer.stop();
    }

    // ==========================================
    // Volume Controls
    // ==========================================
//
//    public void setSfxVolume(double volume) {
//        this.sfxVolume = clamp(volume);
//        clips.values().forEach(c -> c.setVolume(this.sfxVolume));
//    }
//
//    public void setMusicVolume(double volume) {
//        this.musicVolume = clamp(volume);
//        if (musicPlayer != null) musicPlayer.setVolume(this.musicVolume);
//    }
//
//    public void setSfxEnabled(boolean enabled)   { this.sfxEnabled   = enabled; }
//    public void setMusicEnabled(boolean enabled)  { this.musicEnabled = enabled; }
//
//    public double getSfxVolume()    { return sfxVolume;   }
//    public double getMusicVolume()  { return musicVolume; }
//    public boolean isSfxEnabled()   { return sfxEnabled;  }
//    public boolean isMusicEnabled() { return musicEnabled; }
//
//    // ==========================================
//    // Helpers
//    // ==========================================
//
//    private double clamp(double v) {
//        return Math.max(0.0, Math.min(1.0, v));
//    }
}