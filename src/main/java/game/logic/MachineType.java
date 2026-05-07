package game.logic;

public enum MachineType {
    NONE(0),
    DROPPER(50),
    CONVEYOR(10),
    UPGRADER(100),
    FURNACE(200);

    private final double cost;
    MachineType(double cost) { this.cost = cost; }
    public double getCost() { return cost; }
}