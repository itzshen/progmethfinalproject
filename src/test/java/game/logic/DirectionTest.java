package game.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DirectionTest {

    @Test
    void rightDeltaX() { assertEquals(1,  Direction.RIGHT.deltaX()); }

    @Test
    void rightDeltaY() { assertEquals(0,  Direction.RIGHT.deltaY()); }

    @Test
    void leftDeltaX()  { assertEquals(-1, Direction.LEFT.deltaX());  }

    @Test
    void leftDeltaY()  { assertEquals(0,  Direction.LEFT.deltaY());  }

    @Test
    void downDeltaX()  { assertEquals(0,  Direction.DOWN.deltaX());  }

    @Test
    void downDeltaY()  { assertEquals(1,  Direction.DOWN.deltaY());  }

    @Test
    void upDeltaX()    { assertEquals(0,  Direction.UP.deltaX());    }

    @Test
    void upDeltaY()    { assertEquals(-1, Direction.UP.deltaY());    }
}
