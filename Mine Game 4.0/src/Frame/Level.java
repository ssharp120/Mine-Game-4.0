package Frame;

import static Utilities.FileUtilities.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import Entities.Crop;
import Entities.Entity;
import Entities.OxygenGenerator;
import Entities.Plant;
import Entities.Player;
import Tiles.BackgroundDestructibleTile;
import Tiles.DestructibleTile;
import Tiles.Platform;
import Tiles.RandomizedTile;
import Tiles.Tile;
import UI.InventoryItem;
import UI.InventoryTile;
import Utilities.FileUtilities;

public class Level {
	
	protected GameLoop game;
	protected String filePath;
	public String name;
	private int[][] tiles;
	private int[][] tileData;
	private boolean[][] exploredTiles;
	private double percentExplored;
	private int horizon;
	private double[][] durabilities;
	public int width, height;
	public int xOffset, yOffset;
	public int spawnX, spawnY;
	public int index;
	protected BufferedImage image;
	public Sky sky;
	
	private List<Entity> entities = new ArrayList<Entity>();
	
	public Level(BufferedImage image, String name, int i, GameLoop mg, int x, int y) {
		index = i;
		spawnX = x;
		spawnY = y;
		sky = new Sky("sky1", game);
		this.name = name;
		this.loadLevelFromImage(image);
		game = mg;
		loadHorizon();
	}
	
	public Level(String path, String name, int i, GameLoop mg, int x, int y) {
		index = i;
		spawnX = x;
		spawnY = y;
		sky = new Sky("sky1", game);
		this.name = name;
		game = mg;
		if (path != null) {
            this.filePath = path;
            this.loadLevelFromFile(filePath);
            game = mg;
        }
	}
	
	private void loadHorizon() {
		horizon = 480;
		exploredTiles = new boolean[width][height];
		
		FileUtilities.log("Removing fog...\n");
		// Remove fog up to horizon
		/*for (int j = 0; j < width; j++) {
			for (int k = 0; k <= horizon; k++) {
				exploredTiles[j][k] = true;
			}
		}*/
		
		for (int i = 0; i < 128; i++) {
			for (int j = horizon; j < horizon + 128; j++) {
				if (i < 64 || i - 64 + j - horizon < 72) exploredTiles[i][j] = true;
			}
		}
		
		// Add teaser fog
		int p = (int) (Math.round(Math.random() * (double) (width - 16)) + 8);
		for (int n = p; n < 8; n++) {
			if (height > 16 && horizon > 16)
			tiles[n][14] = Tile.IRON_TILE.getId();
			tiles[p][15] = Tile.IRON_TILE.getId();
			tiles[p + 7][15] = Tile.IRON_TILE.getId();
			tiles[n][16] = Tile.IRON_TILE.getId();
			if (n > p && n < p + 7) {
				exploredTiles[n][16] = false;
			}
		}
		
		String levelHorizonPath = "level_" + FileUtilities.TIMESTAMP_AT_RUNTIME + "_horizon";
		int arrayLength = FileUtilities.readFromPositionInt(levelHorizonPath, 0);
		// Loop through vertical slices of the map
		for (int i = 0; i < arrayLength; i++) {
			if (i >= 0 && i < width) {
				// Remove fog up to 5 tiles beneath the ground
				int currentLimit = FileUtilities.readFromPositionInt(levelHorizonPath, (i + 1)*4) + 5;
				for (int j = horizon; j < currentLimit; j++) {
					if (j < currentLimit) {
						exploredTiles[i][j] = true;
					}
				}
				if (i % (width / 4) == 0) {
					for (int k = 0; k < 3 * Math.round((double) i/width * 4); k++) {
						FileUtilities.log("|");
					}
					for (int k = 0; k < 3 * (4 - Math.round((double) i/width * 4)); k++) {
						FileUtilities.log("-");
					}
					FileUtilities.log("\t" + (i * 100) / width + "%\n");
				}
				if (i == width - 1) {
					FileUtilities.log("||||||||||||\t100&\n");
				}
			}
		}
		
	}
	
	protected int getHorizon() {
		return horizon;
	}
	
	private void loadLevelFromImage(BufferedImage image) {
        	FileUtilities.log("Loading level " + index + " from " + filePath + "...\n\t");
        	
        	this.image = image;
        	
            this.width = this.image.getWidth();
            this.height = this.image.getHeight();
            tiles = new int[width][height];
            tileData = new int[width][height];
            durabilities = new double[width][height];
            this.loadTiles();
            FileUtilities.log("Level " + index + ", " + name + ", loaded from " + filePath + ":\n"
            		+ "\tSize: " + width + "x" + height + ", " + width * height + " tiles\n"
            		+ "\tSpawnpoint: " + spawnX + ", " + spawnY + "\n");
	}
	
	private void loadLevelFromFile(String filePath) {
        try {
        	FileUtilities.log("Loading level " + index + " from " + filePath + "...\n\t");
        	
        	this.image = ImageIO.read(FileUtilities.getFile(filePath));
        	
            this.width = this.image.getWidth();
            this.height = this.image.getHeight();
            tiles = new int[width][height];
            durabilities = new double[width][height];
            this.loadTiles();
            FileUtilities.log("Level " + index + ", " + name + ", loaded from " + filePath + ":\n"
            		+ "\tSize: " + width + "x" + height + ", " + width * height + " tiles\n"
            		+ "\tSpawnpoint: " + spawnX + ", " + spawnY + "\n");
        } catch (IOException e) {
        	FileUtilities.log("Error occured while loading level " + index + " from " + filePath);
            e.printStackTrace();
            System.exit(1);
        }
    }
	
	private void loadTiles() {
        int[] tileColours = this.image.getRGB(0, 0, width, height, null, 0, width);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tileCheck: for (Tile t : Tile.tiles) {
                    if (t != null && t.getLevelColour() == tileColours[x + y * width]) {
                        this.tiles[x][y] = t.getId();
                        break tileCheck;
                    }
                }
	    		if (Tile.tiles[tiles[x][y]].getClass() == RandomizedTile.class) {
	    			double rng = Math.random();
	    			int max = ((RandomizedTile) Tile.tiles[14]).possibleValues.length - 1;
	    			double index = rng * max;
	    			tiles[x][y] = ((RandomizedTile) Tile.tiles[14]).possibleValues[Math.toIntExact(Math.round(index))];
	    		}
	    		if (Tile.tiles[tiles[x][y]].getClass() == DestructibleTile.class) {
	    			durabilities[x][y] = ((DestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
	    		}
	    		if (Tile.tiles[tiles[x][y]].getClass() == BackgroundDestructibleTile.class) {
	    			durabilities[x][y] = ((BackgroundDestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
	    		}
            }
        }
	}
	
	public Tile getTile(int x, int y) {
		if (0 > x || x >= width || 0 > y || y >= height) return Tile.VOID;
		return Tile.tiles[tiles[x][y]];
	}
	
	public void setTile(int x, int y, int id) {
		if (!(0 > x || x >= width || 0 > y || y >= height)) tiles[x][y] = id;
	}
	
	public double getDurability(int x, int y) {
		if (0 > x || x >= width || 0 > y || y >= height) return 0.0;
		return durabilities[x][y];
	}
	
	public void setDurability(int x, int y, double durability) {
		if (!(0 > x || x >= width || 0 > y || y >= height)) durabilities[x][y] = durability;
	}
	
	public synchronized void tick() {
		boolean oxygenConnected = false;
		for (Entity e : getEntities()) {
			e.tick();
			if (e.getClass() == OxygenGenerator.class) {
				if (Math.pow(Math.abs(e.x * 32 - entities.get(0).x + 32)^2 + Math.abs(e.y * 32 - entities.get(0).y + 64)^2, 0.5) < 22) {
					((Player) entities.get(0)).connectOxygen();
					((Player) entities.get(0)).addOxygen(0.025);
					((Player) entities.get(0)).addOxygenPoint(e.x * 32, e.y * 32);
					oxygenConnected = true;
				}
			}
		}
		entities.removeIf(i -> i.markedForDeletion);
		if (game.ticks % 250 == 160) {
			for (int y = 0; y < height - 1; y++) {
				for (int x = 0; x < width; x++) {
					checkLeafDecay(x, y);
				}
			}
		}
		sky.tick();
		if (!oxygenConnected) {
			((Player) entities.get(0)).disconnectOxygen();
			((Player) entities.get(0)).removeOxygen(0.01);
		}
		for (int i = -16; i < 16; i++) {
			for (int j = -8; j < 8; j++) {
				if (((Player) entities.get(0)).x >= -16 * 32 && ((Player) entities.get(0)).x >> 5 < width + 16 * 32 && ((Player) entities.get(0)).y >= -8 * 32 && ((Player) entities.get(0)).y >> 5 < height + 8 * 32
					&& i * i + j * j <= 64 && (((Player) entities.get(0)).x >> 5) + i >= 0 && (((Player) entities.get(0)).x >> 5) + i < width
					&& (((Player) entities.get(0)).y >> 5) + j >= 0 && (((Player) entities.get(0)).y >> 5) + j < height) 
					
					exploredTiles[(((Player) entities.get(0)).x >> 5) + i][(((Player) entities.get(0)).y >> 5) + j] = true;
			}
		}
		int exploredArea = 0;
		for (int m = 0; m < width; m++) {
			for (int n = 0; n < height; n++) {
				if (exploredTiles[m][n]) exploredArea++;
			}
		}
		percentExplored = (double) exploredArea / (width * height) * 100;
	}
	
	public boolean isExplored(int x, int y) {
		return exploredTiles[x][y];
	}
		
	public void checkLeafDecay(int x, int y) {
		if (tiles[x][y] == Tile.LEAVES.getId()) {
			boolean woodFound = false;
			for (int i = -2; i <= 2; i++) {
				for (int j = 0; j < 3; j++) {
					if (tiles[x + i][y + j] == Tile.NATURAL_WOOD.getId()) woodFound = true;
				}
			}
			if ((tiles[x + 1][y] == Tile.VOID.getId() && tiles[x - 1][y] == Tile.VOID.getId() && tiles[x][y + 1] == Tile.VOID.getId() && tiles[x][y - 1] == Tile.VOID.getId())) {
				tiles[x][y] = Tile.VOID.getId();
			}
			if (!woodFound) {
				for (int i = 0; i < 5; i++) {
					for (int j = 0; j < 3; j++) {
						if (tiles[x + i][y + j] == Tile.LEAVES.getId()) tiles[x + i][y + j] = Tile.VOID.getId();
					}
				}
			}
		}
	}
	
	public void populatePlants() {
		for (int y = 0; y < height - 2; y++) {
			for (int x = 0; x < width; x++) {
				if (tiles[x][y] == Tile.SAND.getId() && tiles[x][y - 1] == Tile.SKY.getId() && Math.random() < 0.20) {
					placePlant(x, y);
				}
			}
		}
	}
	
	public void placePlant(int x, int y) {
		// Create a positive or negative distance to randomly displace the crop up to half a tile
		int horizontalDelta = (int) Math.round(16 - 32 * Math.random());
		
		// Cancel the horizontal offset if no room to the sides or if it will push the crop off the edge of the level
		if ( (x << 5) + horizontalDelta <= 0 || (x << 5) + horizontalDelta >= (width - 2) << 5 
		 || (!(tiles[x + 1][y] == Tile.SAND.getId() && tiles[x + 1][y - 1] == Tile.SKY.getId()) 
		 &&  !(tiles[x - 1][y] == Tile.SAND.getId() && tiles[x - 1][y - 1] == Tile.SKY.getId()))) horizontalDelta = 0;					
		
		// If no room to the right, force horizontal delta to be negative
		if (!(tiles[x + 1][y] == Tile.SAND.getId() && tiles[x + 1][y - 1] == Tile.SKY.getId()) && horizontalDelta > 0) horizontalDelta = -horizontalDelta;
		
		// If no room to the left, force horizontal delta to be positive
		if (!(tiles[x - 1][y] == Tile.SAND.getId() && tiles[x - 1][y - 1] == Tile.SKY.getId()) && horizontalDelta < 0)  horizontalDelta = -horizontalDelta;
		
		entities.add(new Crop(this, true, 0, (x << 5) + horizontalDelta, (y - 1) << 5));
	}
	
	public synchronized void checkPlantDestruction(int clickX, int clickY) {
		for (Entity e : entities) {
			if (e != null && e.getClass() == Crop.class) {
				//System.out.println("x: " + clickX + " y: " + clickY + " target x: " + e.x + " target y " + e.y + " target width: " + ((Crop) e).cropWidth + " target height: " + ((Crop) e).cropHeight);
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, e.x, e.y, ((Crop) e).cropWidth, ((Crop) e).cropHeight, true)) {
					e.markedForDeletion = true;
					
					for (InventoryItem i : ((Crop) e).returnDroppables()) {
						((Player) entities.get(0)).inventory.addItem(i);
					}
				}
			}
		}
	}
	
	public void explode(int x, int y) {
		// Remove tiles and replace with air
		if (x <= 8 || x + 8 >= width || y <= 8 || y + 8 >= height) return;
		for (int i = -8; i <= 8; i++) {
			for (int j = -8; j <= 8; j++) {
				if (i * i + j * j < 64) {
					tiles[x + i][y + j] = 2;
					
					// Remove fog horizontally
					if (i < 0) exploredTiles[x + i - 1][y + j] = true;
					else exploredTiles[x + i + 1][y + j] = true;
					
					// Remove fog vertically
					if (j < 0) exploredTiles[x + i][y + j - 1] = true;
					else exploredTiles[x + i][y + j + 1] = true;
				}
			}
		}
		
		// Damage player based on proximity
		if (Math.pow(Math.abs(((Player) entities.get(0)).x >> 5 - x), 2) + Math.pow(Math.abs(((Player) entities.get(0)).y >> 5 - y), 2) < 32) {
			((Player) entities.get(0)).damage(128);
		}
	}
	
	public BufferedImage drawMiniMap(int scaleX, int scaleY) {
		BufferedImage image = new BufferedImage(width / scaleX, height / scaleY, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < width; i += scaleX) {
			for (int j = 0; j < height; j += scaleY) {
				if (exploredTiles[i + scaleX / 2][j + scaleY / 2]) image.setRGB(i / scaleX, j / scaleY, Tile.tiles[tiles[i + scaleX / 2][j + scaleY / 2]].getLevelColour());
				else {
					if ((i % 2 == 0 && j % 2 == 1) || (i % 2 == 1 && j % 2 == 0)) image.setRGB(i / scaleX, j / scaleY, Color.LIGHT_GRAY.getRGB());
					else image.setRGB(i / scaleX, j / scaleY, Color.DARK_GRAY.getRGB());
				}
			}
		}
		return image;
	}
	
	public synchronized void draw(Graphics g, ImageObserver observer) { 
		drawEntities(g, observer);
	}
	
	public void drawSky(Graphics g, Dimension resolution) {
		sky.draw(g, resolution, game);
	}
	
	public GameLoop getGameLoop() {
		return game;
	}
	
	public synchronized List<Entity> getEntities() {
        return this.entities;
    }
	
	public double getPercentExplored() {
		return percentExplored;
	}

	public synchronized void drawEntities(Graphics g, ImageObserver observer) {
		for (Entity e : getEntities()) {
			if (e.getClass() == Crop.class) ((Crop) e).draw(g, observer);
			else e.draw(g);
		}
	}
	
	public synchronized void addEntity(Entity entity) {
		this.getEntities().add(entity);
	}
	
	public void destructible(int targetX, int targetY, double damage) {
		Tile t = getTile(targetX, targetY);
		if (t.getId() != 8191 && (t.getClass() == DestructibleTile.class || t.getClass() == BackgroundDestructibleTile.class || t.getClass() == Platform.class)) {
			double initialDurability = getDurability(targetX, targetY);
			setDurability(targetX, targetY, initialDurability - damage);
			if (getDurability(targetX, targetY) <= 0) {
				if (tiles[targetX][targetY] == 8) game.player.inventory.addItem(new InventoryTile(17, 1));
				else game.player.inventory.addItem(new InventoryTile(tiles[targetX][targetY], 1));
				setTile(targetX, targetY, 2);
			}
		}
	}
}
