package game.logic;

import java.util.Optional;

/**
 * Base class for grid machines that face a direction and can hold one item.
 */
public abstract class Machine implements Tickable {
    private int gridX;
    private int gridY;
    private final MachineType type;
    private double cost;
    private Direction facing;
    protected Item currentItem;

    /**
     * Creates a machine with the given type, cost, and facing direction.
     *
     * @param type the machine type
     * @param cost the purchase cost
     * @param facing the direction this machine faces
     */
    protected Machine(MachineType type, double cost, Direction facing) {
        setCost(cost);
        setFacing(facing);
        this.type = type;
    }

    /**
     * Runs this machine's per-tick behavior.
     *
     * @param hadItemAtStart whether this machine held an item at tick start
     */
    @Override
    public void onTick(boolean hadItemAtStart) {
        // Simulation steps are orchestrated by GridSystem.tick().
    }

    /**
     * @return this machine's grid x-coordinate
     */
    public int getGridX() {
        return gridX;
    }

    /**
     * @return this machine's grid y-coordinate
     */
    public int getGridY() {
        return gridY;
    }

    /**
     * Sets this machine's grid position.
     *
     * @param gridX the x-coordinate
     * @param gridY the y-coordinate
     */
    public void setGridPosition(int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }

    /**
     * @return this machine's purchase cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Sets this machine's purchase cost.
     *
     * @param cost the new cost
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * @return the direction this machine faces
     */
    public Direction getFacing() {
        return facing;
    }

    /**
     * Sets this machine's facing direction.
     *
     * @param facing the new facing direction
     */
    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    /**
     * @return the item currently held by this machine, or null if empty
     */
    public Item getCurrentItem() {
        return currentItem;
    }

    /**
     * Sets the item currently held by this machine.
     *
     * @param currentItem the new held item, or null to make the machine empty
     */
    void setCurrentItem(Item currentItem) {
        this.currentItem = currentItem;
    }

    /**
     * @return this machine's type
     */
    public MachineType getType() {
        return type;
    }

    /**
     * Processes an item immediately after it is accepted.
     *
     * @param item the accepted item
     */
    public abstract void processItem(Item item);

    /**
     * Checks whether this machine can accept an item.
     *
     * @param item the item to check
     * @return true if the item is non-null and this machine is empty
     */
    public boolean wouldAccept(Item item) {
        if (item == null) {
            return false;
        }
        return currentItem == null;
    }

    /**
     * Accepts an item if possible and processes it.
     *
     * @param item the item to accept
     * @return true if the item was accepted
     */
    public boolean acceptItem(Item item) {
        if (!wouldAccept(item)) {
            return false;
        }
        this.currentItem = item;
        processItem(item);
        return true;
    }

    /**
     * Removes the currently held item.
     */
    void clearCurrentItem() {
        this.currentItem = null;
    }

    /**
     * Prepares this machine's outgoing transfer for the current tick, if any.
     *
     * @param grid the grid containing this machine
     * @param hadItemAtStart the item-occupancy snapshot from tick start
     * @return the outgoing transfer, or empty if none
     */
    Optional<OutgoingTransfer> prepareOutgoingTransfer(GridSystem grid, boolean[][] hadItemAtStart) {
        return Optional.empty();
    }
}
