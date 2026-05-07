package game.logic;

public enum MachineType {
    /* Dynamically add into machine list
    To add new machine just copy and change constructor and use correct image path
     */

    NONE(0, "conveyor.png") {
        @Override
        public Machine create(Direction face, PlayerBank bank) {
            throw new IllegalStateException("Cannot create a NONE machine.");
        }
    },

    CONVEYOR(10, "conveyor.png") {
        @Override
        public Machine create(Direction face, PlayerBank bank) {
            return new Conveyor(getCost(), face);
        }
    },

    // Dropper
    DROPPER(50, "dropper.png") {
        @Override
        public Machine create(Direction face, PlayerBank bank) {
            return new Dropper(getCost(), face, 10.0); // 10.0 drop rate
        }
    },

    // Upgrader
    UPGRADER(100, "upgrader.png") {
        @Override
        public Machine create(Direction face, PlayerBank bank) {
            return new Upgrader(getCost(), face, 2.0); // 2.0 upgrade multiplier
        }
    },
    TEST(19, "upgrader.png") {
        @Override
        public Machine create(Direction face, PlayerBank bank) {
            return new Upgrader(getCost(), face, 1231);
        }
    },

    // Furnace
    FURNACE(200, "furnace.png") {
        @Override
        public Machine create(Direction face, PlayerBank bank) {
            return new Furnace(getCost(), face, bank); // Furnace needs the bank
        }
    };

    private final double cost;
    private final String imageName;

    MachineType(double cost, String imageName) {
        this.cost = cost;
        this.imageName = imageName;
    }

    public double getCost() {
        return cost;
    }

    public String getImageName() {
        return imageName;
    }

    public abstract Machine create(Direction face, PlayerBank bank);
}