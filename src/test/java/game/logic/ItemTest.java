package game.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void itemInitialisesWithCorrectType() {
        Item item = new Item(ItemType.COAL);
        assertEquals(ItemType.COAL, item.getType());
    }

    @Test
    void itemInitialisesWithTypeValue() {
        Item item = new Item(ItemType.COAL);
        assertEquals(ItemType.COAL.getValue(), item.getValue(), 1e-9);
    }

    @Test
    void multiplyValueScalesCorrectly() {
        Item item = new Item(ItemType.COAL);
        double original = item.getValue();
        item.multiplyValue(3.0);
        assertEquals(original * 3.0, item.getValue(), 1e-9);
    }

    @Test
    void multiplyValueByOneIsNoOp() {
        Item item = new Item(ItemType.IRON);
        double original = item.getValue();
        item.multiplyValue(1.0);
        assertEquals(original, item.getValue(), 1e-9);
    }

    @Test
    void multiplyValueByZeroCollapses() {
        Item item = new Item(ItemType.GOLD);
        item.multiplyValue(0.0);
        assertEquals(0.0, item.getValue(), 1e-9);
    }

    @Test
    void multiplyValueCanBeAppliedRepeatedly() {
        Item item = new Item(ItemType.COAL);
        double original = item.getValue();
        item.multiplyValue(2.0);
        item.multiplyValue(2.0);
        assertEquals(original * 4.0, item.getValue(), 1e-9);
    }
}