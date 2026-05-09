package game.logic;

/**
 * Machine that converts held items into money by depositing their value into a
 * {@link PlayerBank}.
 */
public class Furnace extends Machine {
    private final PlayerBank bank;

    /**
     * Creates a furnace linked to the given player bank.
     *
     * @param type the machine type
     * @param cost the purchase cost
     * @param facing the machine's facing direction
     * @param bank the bank that receives smelted item value
     */
    public Furnace(MachineType type, double cost, Direction facing, PlayerBank bank) {
        super(type, cost, facing);
        this.bank = bank;
    }

    /**
     * Smelts any held item during the tick.
     *
     * @param hadItemAtStart whether this furnace held an item at tick start
     */
    @Override
    public void onTick(boolean hadItemAtStart) {
        smeltHeldItem();
    }

    /**
     * Accepts an item without immediately smelting it.
     *
     * @param item the accepted item
     */
    @Override
    public void processItem(Item item) {
        // Smelting happens on tick when an item is held; see GridSystem.tick().
    }

    /**
     * Deposits the held item's value and removes it from the furnace.
     */
    void smeltHeldItem() {
        Item item = getCurrentItem();
        if (item == null) {
            return;
        }
        bank.deposit(item.getValue());
        clearCurrentItem();
    }
}