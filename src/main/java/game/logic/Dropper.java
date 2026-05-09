package game.logic;

/**
 * Machine that creates a specific {@link ItemType} when empty, then moves items
 * forward like a {@link Conveyor}.
 */
public class Dropper extends Conveyor { ;
    private final ItemType itemToDrop;

    /**
     * Creates a dropper for the given item type.
     *
     * @param type the machine type
     * @param cost the purchase cost
     * @param facing the direction items are moved
     * @param itemToDrop the type of item this dropper creates
     */
    public Dropper(MachineType type, double cost, Direction facing, ItemType itemToDrop) {
        super(type, cost, facing);
        this.itemToDrop = itemToDrop;
    }

    /**
     * Spawns an item if this dropper was empty at the start of the tick.
     *
     * @param hadItemAtStart whether this dropper held an item at tick start
     */
    @Override
    public void onTick(boolean hadItemAtStart) {
        spawnIfStillEmpty(!hadItemAtStart);
    }

    /**
     * Accepts an item without modifying it.
     *
     * @param item the accepted item
     */
    @Override
    public void processItem(Item item) {
        // No transformation on intake (normally not fed by belts).
    }

    /**
     * Creates this dropper's item if it was empty at tick start and is still empty.
     *
     * @param wasEmptyAtTickStart whether this dropper was empty at tick start
     */
    void spawnIfStillEmpty(boolean wasEmptyAtTickStart) {
        if (!wasEmptyAtTickStart) {
            return;
        }
        if (getCurrentItem() != null) {
            return;
        }
        acceptItem(new Item(itemToDrop));
    }
}
