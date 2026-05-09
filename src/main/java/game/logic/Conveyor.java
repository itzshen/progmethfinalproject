package game.logic;

import java.util.Optional;

/**
 * A transport machine that moves its currently held {@link Item} one grid cell
 * in the direction it is facing.
 * <p>
 * A conveyor does not modify items when they enter it. During a grid tick, it
 * prepares an outgoing transfer only when it is holding an item, the target
 * position is inside the grid, the target cell contains a machine, the target
 * machine did not already contain an item at the start of the tick, and the
 * target machine can accept the item.
 * </p>
 */
public class Conveyor extends Machine {

    /**
     * Creates a conveyor with the given machine type, purchase cost, and facing
     * direction.
     *
     * @param type the machine type represented by this conveyor
     * @param cost the purchase cost of the conveyor
     * @param facing the direction this conveyor pushes items toward
     */
    public Conveyor(MachineType type, double cost, Direction facing) {
        super(type, cost, facing);
    }

    /**
     * Handles an item entering this conveyor.
     * <p>
     * Conveyors do not transform, upgrade, consume, or otherwise modify items on
     * intake.
     * </p>
     *
     * @param item the item accepted by this conveyor
     */
    @Override
    public void processItem(Item item) {
        // No transformation on intake.
    }

    /**
     * Determines whether this conveyor can move its held item to the next cell
     * during the current tick and, if so, creates the corresponding transfer.
     * <p>
     * The transfer is only prepared when the destination is valid, occupied by a
     * machine, was empty at the start of the tick, and can accept the outgoing
     * item.
     * </p>
     *
     * @param grid the grid containing this conveyor and potential destination
     *             machines
     * @param hadItemAtStart a snapshot indicating which grid cells contained an
     *                       item at the beginning of the tick
     * @return an {@link Optional} containing the prepared outgoing transfer, or
     *         {@link Optional#empty()} if no transfer can be made
     */
    @Override
    Optional<OutgoingTransfer> prepareOutgoingTransfer(GridSystem grid, boolean[][] hadItemAtStart) {
        if (getCurrentItem() == null) {
            return Optional.empty();
        }
        int nx = getGridX() + getFacing().deltaX();
        int ny = getGridY() + getFacing().deltaY();
        if (!grid.isInside(nx, ny)) {
            return Optional.empty();
        }
        Machine target = grid.getMachine(nx, ny);
        if (target == null) {
            return Optional.empty();
        }
        if (hadItemAtStart[nx][ny]) {
            return Optional.empty();
        }
        Item outgoing = getCurrentItem();
        if (!target.wouldAccept(outgoing)) {
            return Optional.empty();
        }
        return Optional.of(new OutgoingTransfer(getGridX(), getGridY(), nx, ny, outgoing));
    }
}
