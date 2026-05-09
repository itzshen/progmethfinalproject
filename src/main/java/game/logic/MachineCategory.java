package game.logic;

/**
 * Shop and UI grouping categories for machine types.
 */
public enum MachineCategory {
    PRODUCTION("Production"),
    TRANSPORT("Transport"),
    UPGRADES("Upgrades"),
    PROCESSING("Processing");

    private final String displayName;

    /**
     * Creates a category with a player-facing name.
     *
     * @param displayName the name shown in the UI
     */
    MachineCategory(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return the player-facing category name
     */
    public String getDisplayName() {
        return displayName;
    }
}