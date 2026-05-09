package game.ui.sound;

/**
 * Sound effects used by UI and gameplay actions.
 */
public enum SoundEffect {
    PLACE   ("place.wav"),
    REMOVE  ("remove.wav"),
    DEPOSIT ("deposit.wav");

    private final String fileName;
    SoundEffect(String fileName) { this.fileName = fileName; }

    /**
     * @return the audio file name for this effect
     */
    public String getFileName()  { return fileName; }
}