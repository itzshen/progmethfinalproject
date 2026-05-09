package game.logic;

/**
 * Conveyor that multiplies an item's value when it accepts the item.
 */
public class Upgrader extends Conveyor {
    private final double upgradeFactor;

    /**
     * Creates an upgrader with the given value multiplier.
     *
     * @param type the machine type
     * @param cost the purchase cost
     * @param facing the direction items are moved
     * @param upgradeFactor the multiplier applied to accepted items
     */
    public Upgrader(MachineType type, double cost, Direction facing, double upgradeFactor) {
        super(type, cost, facing);
        this.upgradeFactor = upgradeFactor;
    }

    /**
     * @return the value multiplier applied by this upgrader
     */
    public double getUpgradeFactor() {
        return upgradeFactor;
    }

    /**
     * Multiplies the accepted item's value.
     *
     * @param item the accepted item
     */
    @Override
    public void processItem(Item item) {
        item.multiplyValue(upgradeFactor);
    }
}
