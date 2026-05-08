package game.logic;

public enum ItemType {
    COAL("Coal", 2.0, "Coal.png"),
    COPPER("Copper Ore",5.0,"Copper.png"),
    IRON("Iron Ore", 20.0, "Iron.png"),
    SILVER("Silver Ore",75.0,"Silver.png"),
    GOLD("Gold Ore", 250.0, "Gold.png"),
    PLATINUM("Platinum Ore",500.0,"Platinum.png"),
    RUBY("Ruby",1600.0,"Ruby.png"),
    SAPPHIRE("Sapphire",6000.0,"Sapphire.png"),
    EMERALD("Emerald",18500.0,"Emerald.png"),
    DIAMOND("Diamond",50000.0,"Diamond.png");


    private final String displayName;
    private final double value;
    private final String imageName;

    ItemType(String displayName, double value, String imageName) {
        this.displayName = displayName;
        this.value = value;
        this.imageName = imageName;
    }

    public String getDisplayName() { return displayName; }
    public double getValue() { return value; }
    public String getImageName() { return imageName; }
}