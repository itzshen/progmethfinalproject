package game.logic;

/**
 * Item with a type and mutable monetary value.
 */
public class Item {
    private double value;
    private final ItemType type;

    /**
     * Creates an item using the default value of its type.
     *
     * @param type the item type
     */
    public Item(ItemType type) {
        this.type = type;
        this.value = type.getValue();
    }

    /**
     * @return the item type
     */
    public ItemType getType() {
        return type;
    }

    /**
     * @return the item's current value
     */
    public double getValue() {
        return value;
    }

    /**
     * Multiplies the item's current value.
     *
     * @param factor the value multiplier
     */
    public void multiplyValue(double factor) {
        this.value *= factor;
    }
}
