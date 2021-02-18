package Tiles;

public class Tile {
	protected int id;
    protected boolean solid;
    private int levelColour;
    private String name;

	public static final Tile[] tiles = new Tile[8192];
    
    public static final SolidTile BARRIER = 			new SolidTile(8190, 0xFF000001, "Barrier", 1.0);
    public static final Tile VOID = 					new Tile(8191, 0xFFFFFFFF, "Void");
    public static final Tile OVERLAY = 					new Tile(1, 0xFF000000, "Overlay");
    public static final Tile SKY = 						new Tile(2, 0xFF71C9DD, "Sky");
    public static final SolidTile WOOD = 				new DestructibleTile(3, 0XFF5B2F00, "Wooden Planks", 0.7, true, 2.0, 50.0);
    public static final Tile NATURAL_WOOD =     		new BackgroundDestructibleTile(4, 0xFFAF833C, "Log", true, 2.0, 50.0);
    public static final SolidTile DIRT = 				new DestructibleTile(5, 0xFF6D553F, "Dirt", 0.6, true, 1.2, 50.0);
    public static final SolidTile GRASS = 				new DestructibleTile(6, 0xFF5A8B2F, "Grass", 0.6, true, 1.2, 50.0);
    public static final SolidTile SAND = 				new DestructibleTile(7, 0xFFFFEB87, "Sand", 0.4, true, 1.0, 40.0);
    public static final SolidTile STONE =   			new DestructibleTile(8, 0xFFC0C0C0, "Stone", 0.8, true, 5.0, 250.0);
    public static final Tile LEAVES = 		    		new BackgroundDestructibleTile(9, 0xFF3E911B, "Leaves", true, 0.2, 5.0);
    public static final SolidTile GLASS = 				new DestructibleTile(10, 0xFFF7F7F7, "Glass", 0.1, true, 2.0, 10.0);
    public static final SolidTile IRON_ORE =   			new DestructibleTile(11, 0xFF8E514B, "Iron Ore", 0.8, true, 15.0, 1500.0);
    public static final SolidTile IRON_TILE =   		new DestructibleTile(12, 0xFFF2F2F2, "Iron Tile", 0.2, true, 30.0, 3000.0);
    public static final Platform WOOD_PLATFORM = 		new Platform(13, 0xFFD86C00, "Wooden Platform", 0.7, true, 2.0, 50.0);
    public static final SolidTile SHIP_TILE =			new DestructibleTile(14, 0xFF405396, "Ship Tile", 0.2, false, 500.0, 12000000.0);
    public static final Tile SHIP_BACKGROUND = 	 		new BackgroundDestructibleTile(15, 0xFF697393, "Ship Background", false, 500.0, 12000000.0);
    public static final Tile WOOD_WALL =				new BackgroundDestructibleTile(16, 0xFF9E7842, "Wood Wall", false, 2.0, 75.0);
    public static final SolidTile COBBLESTONE =			new DestructibleTile(17, 0xFF6B6B6B, "Cobblestone", 0.92, true, 4.0, 200.0);
    public static final Tile STONE_WALL =				new BackgroundDestructibleTile(18, 0xFF494949, "Stone Wall", false, 5.0, 300.0);
    public static final SolidTile TNT = 				new DestructibleTile(19, 0xFF0000, "TNT", 0.6, true, 1.0, 40.0);
    
    public Tile(int id, int levelColour, String name) {
        this.id = id;
        this.name = name;
        if (tiles[id] != null)
            throw new RuntimeException("Duplicate tile id on " + id);
        this.solid = false;
        this.levelColour = levelColour;
        tiles[id] = this;
    }
	
	public int getId() {
        return id;
    }

    public boolean isSolid() {
        return solid;
    }

    public int getLevelColour() {
        return levelColour;
    }
    
    public String getName() {
 		return name;
 	}
}
