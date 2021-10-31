package Networking;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.awt.Dimension;

import Entities.Entity;
import Entities.OxygenGenerator;
import Entities.PhysicalItem;
import Entities.Player;
import Tiles.BackgroundDestructibleTile;
import Tiles.DestructibleTile;
import Tiles.RandomizedTile;
import Tiles.Tile;
import UI.Ingredient;
import UI.InventoryTile;
import Utilities.FileUtilities;

public class ServerLevel {
	private int[][] tiles;
	private boolean[][] exploredTiles;
	private boolean[][] visibleTiles;
	private boolean[][] activatedOxygenTethers;
	private double percentExplored;
	private double[][] durabilities;
	private double[][] baseDurabilities;
	private byte[][] discreteLightLevels;
	
	private boolean queueUpdate;
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Entity> queuedEntities = new ArrayList<Entity>();
	
	private int spawnX, spawnY;
	
	private int width;
	private int height;
	
	public ServerLevel(BufferedImage image, int spawnX, int spawnY) {
		loadLevelFromImage(image);
		
		this.spawnX = spawnX;
		this.spawnY = spawnY;
		fillUnexploredAreas();
		initDiscreteLightLevels();
		initOxygenTethers();
		updateTiles();
	}
	
	private void loadLevelFromImage(BufferedImage image) {
		if (image == null) throw new IllegalArgumentException("No immage supplied, cannot load level");
    	FileUtilities.log("Loading level from " + image.getWidth() + " x " + image.getHeight() + " image...\n\t");
    	
        width = image.getWidth();
        height = image.getHeight();
        tiles = new int[width][height];
        durabilities = new double[width][height];
        baseDurabilities = new double[width][height];
        
        loadTiles(image);
	}
	
	private void loadTiles(BufferedImage image) {
        int[] tileColours = image.getRGB(0, 0, width, height, null, 0, width);
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
	    			baseDurabilities[x][y] = ((DestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
	    		}
	    		if (Tile.tiles[tiles[x][y]].getClass() == BackgroundDestructibleTile.class) {
	    			durabilities[x][y] = ((BackgroundDestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
	    			baseDurabilities[x][y] = ((BackgroundDestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
	    		}
            }
        }
	}
	
	private void fillUnexploredAreas() {
		exploredTiles = new boolean[width][height];
		visibleTiles = new boolean[width][height];
		
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// Leave all tiles unexplored except for a small area around the starting ship
				if (tiles[i][j] == Tile.SHIP_TILE.getId() 
						|| tiles[i][j] == Tile.SHIP_BACKGROUND.getId()
						|| tiles[i][j] == Tile.SHIP_BACKGROUND_LAMP.getId()) {
					for (int k = -5; k <= 5; k++) {
						for (int l = -5; l <= 5; l++) {
							exploredTiles[i + k][j + l] = (i + k >= 0 && i + k < width && j + l >= 0 && j + l < height);
							visibleTiles[i + k][j + l] = (i + k >= 0 && i + k < width && j + l >= 0 && j + l < height);
						}
					}
				} else if (tiles[i][j] == Tile.SKY.getId()) {
					visibleTiles[i][j] = true;
					if (j < height - 1 && !(tiles[i][j] == tiles[i][j + 1])) {
						byte remainingDistance = 5;
						for (int l = 0; l <= remainingDistance; l++) {
							if (j + l < height) visibleTiles[i][j + l] = true;
							if(!Tile.tiles[tiles[i][j + l]].isSolid()) remainingDistance++;
						}
					}	
				}
			}
		}
	}
	
	private void initDiscreteLightLevels() {
		discreteLightLevels = new byte[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				discreteLightLevels[i][j] = 127;
			}
		}
	}
	
	public void initOxygenTethers() {
		activatedOxygenTethers = new boolean[width][height];
	}
	
	public synchronized List<Entity> getEntities() {
        return this.entities;
    }
	
	public synchronized void addEntity(Entity entity) {
		this.getEntities().add(entity);
	}
	
	public void queueUpdate() {
		queueUpdate = true;
	}
	
	public Tile getTile(int x, int y) {
		if (0 > x || x >= width || 0 > y || y >= height) return Tile.VOID;
		return Tile.tiles[tiles[x][y]];
	}
	
	public void setTile(int x, int y, int id) {
		if (!(0 > x || x >= width || 0 > y || y >= height) && id > 0 && id < Tile.tiles.length) {
			tiles[x][y] = id;
			if (Tile.tiles[id].getClass() == DestructibleTile.class) {
				baseDurabilities[x][y] = ((DestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
			}
			if (Tile.tiles[id].getClass() == BackgroundDestructibleTile.class) {
				baseDurabilities[x][y] = ((BackgroundDestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
			}
			queueUpdate = true;
		}
	}
	
	public double getDurability(int x, int y) {
		if (0 > x || x >= width || 0 > y || y >= height) return 0.0;
		return durabilities[x][y];
	}
	
	public void setDurability(int x, int y, double durability) {
		if (!(0 > x || x >= width || 0 > y || y >= height)) durabilities[x][y] = durability;
	}
	
	public boolean isExplored(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) return false;
		else return exploredTiles[x][y];
	}
	
	public boolean isVisible(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) return false;
		else return visibleTiles[x][y];
	}
	
	public void activateOxygenTether(int x, int y) {
		if (x > 0 && x < width && y > 0 && y < height) {
			activatedOxygenTethers[x][y] = true;
		}
	}
	
	public boolean activatedOxygenTether(int x, int y) {
		if (x > 0 && x < width && y > 0 && y < height) return activatedOxygenTethers[x][y];
		return false;
	}
	
	public void drawLightCircle(int i, int j, int radius, int linearFalloff) {
		for (int k = 0; k < radius * 2; k++) {
			for (int l = 0; l < radius * 2; l++) {
				int distance = (((k - radius) * (k - radius)) + ((l - radius) * (l - radius))) * linearFalloff;
				//System.out.print((byte) (127 - (transparencyDepth * distance)) + " ");
				if (exploredTiles != null && i + k - radius > 0 && i + k - radius < width &&
						j + l - radius > 0 && j + l - radius < height && 
						exploredTiles[i + k - radius][j + l - radius] && discreteLightLevels != null 
						&& i + k - radius > 0 && i + k - radius < width && j + l - radius > 0 && j + l - radius < height
							&& distance <= 255 && distance >= 0
								&& (byte) (127 - distance) > discreteLightLevels[i + k - radius][j + l - radius]) {
					discreteLightLevels[i + k - radius][j + l - radius] = (byte) (127 - distance);
				}
			}
			//System.out.println("");
		}
		//System.out.println("");
	}
	
	public int getDiscreteLightLevel(int x, int y) {
		if (discreteLightLevels == null || x < 0 || y < 0 || x >= width || y >= height) return 0;
		return discreteLightLevels[x][y];
	}
	
	public void resetDestructibleTile(int x, int y) {
		durabilities[x][y] = baseDurabilities[x][y];
	}
	
	public Dimension getSpawnPoint() {
		return new Dimension(spawnX, spawnY);
	}
	
	public byte[] getTileData() {
		if (tiles == null) return "[ERROR] Tile array is undefined".getBytes();
		
		byte[] tileData = new byte[width * height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tileData[j * width + i] = (byte) tiles[i][j];
			}
		}
		return tileData;
	}
	
	public void tick() {
		// System.out.println(toString());
		
		// queueUpdate = true;
		
		if (queueUpdate) updateTiles();
		
		queueUpdate = false;
	}
	
	public BufferedImage drawMiniMap(int scaleX, int scaleY, int playerX, int playerY) {
		int imageWidth, imageHeight;
		if (scaleX == 4) {
			imageWidth = 64;
			imageHeight = 64;
		} else if (scaleX == 2) {
			imageWidth = 256;
			imageHeight = 256;
		} else {
			imageWidth = 1024;
			imageHeight = 1024;
		}
		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		for (int i = playerX - 512 / scaleX; i <= playerX + 512 / scaleX; i += scaleX) {
			for (int j = playerY - 512 / scaleY; j <= playerY + 512 / scaleY; j += scaleY) {
				//System.out.println("i " + i + " j " + j);
				int x = (i - playerX + 512 / scaleX) / scaleX;
				int y = (j - playerY + 512 / scaleY) / scaleY;
				if (i + scaleX / 2 > 0 && j + scaleY / 2 > 0 && i + scaleX / 2 < width && j + scaleY / 2 < height
						&& x > 0 && x < image.getWidth() && y > 0 && y < image.getHeight()) {
					if (exploredTiles[i + scaleX / 2][j + scaleY / 2]) {
						if (activatedOxygenTethers[i + scaleX / 2][j + scaleY / 2]) image.setRGB(x, y, Color.CYAN.getRGB());
						else image.setRGB(x, y, Tile.tiles[tiles[i + scaleX / 2][j + scaleY / 2]].getLevelColour());
					}
					else {
						if ((x % 2 == 0 && y % 2 == 1) || (x % 2 == 1 && y % 2 == 0)) image.setRGB(x, y, Color.LIGHT_GRAY.getRGB());
						else image.setRGB(x, y, Color.GRAY.getRGB());
					}
				}
			}
		}
		
		return image;
	}
	
	public void updateTiles() {
		final long startTime = System.currentTimeMillis();
		for (Entity e : getEntities()) {
			if (e == null) continue;
			if (e.getClass() == OxygenGenerator.class) e.tick();
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				boolean change = false;
				
				if (!(tiles == null || exploredTiles == null) && exploredTiles[i][j] && tiles[i][j] > 2) {
					// Apply gravity to sand not supported from the bottom
					if (tiles[i][j] == Tile.SAND.getId()) {
						if (j < height - 1 && tiles[i][j + 1] <= 2) {
							tiles[i][j] = tiles[i][j + 1];
							durabilities[i][j + 1] = ((DestructibleTile) Tile.SAND).baseDurability;
							tiles[i][j + 1] = Tile.SAND.getId();
							change = true;
						}
					} else if (tiles[i][j] == Tile.CACTUS.getId()) {
						if (j < height - 1 && (!(tiles[i][j + 1] == Tile.SAND.getId() || tiles[i][j + 1] == Tile.CACTUS.getId())
								// Conditions for breakage:
								|| tiles[i][j + 1] == Tile.SHIP_BACKGROUND.getId()
								|| tiles[i][j + 1] == Tile.SHIP_BACKGROUND_LAMP.getId()
								|| tiles[i][j + 1] == Tile.SHIP_TILE.getId())) {
							// Destroy cactus and produce an item
							//addEntity(new PhysicalItem(1033, this, true, i << 5, j << 5, 0.2 - Math.random() * 0.4, 0.2 - Math.random() * 0.4, new InventoryTile(33, 1)));
							tiles[i][j] = Tile.SKY.getId();
							change = true;
						}
					} else if (tiles[i][j] == Tile.NATURAL_WOOD.getId()){
						if (i > 1 && i < width - 1 && j < height - 1) {
							if (!(tiles[i][j +1] == Tile.NATURAL_WOOD.getId() 
									|| Tile.tiles[tiles[i + 1][j]].isSolid() 
									|| Tile.tiles[tiles[i - 1][j]].isSolid() 
									|| Tile.tiles[tiles[i][j + 1]].isSolid()
									|| tiles[i + 1][j] == Tile.NATURAL_WOOD.getId()
									|| tiles[i - 1][j] == Tile.NATURAL_WOOD.getId())
									// Conditions for breakage:
									|| tiles[i][j + 1] == Tile.SHIP_BACKGROUND.getId()
									|| tiles[i][j + 1] == Tile.SHIP_BACKGROUND_LAMP.getId()
									|| tiles[i][j + 1] == Tile.SHIP_TILE.getId()
									|| tiles[i - 1][j] == Tile.SHIP_BACKGROUND.getId()
									|| tiles[i - 1][j] == Tile.SHIP_BACKGROUND_LAMP.getId()
									|| tiles[i - 1][j] == Tile.SHIP_TILE.getId()
									|| tiles[i + 1][j] == Tile.SHIP_BACKGROUND.getId()
									|| tiles[i + 1][j] == Tile.SHIP_BACKGROUND_LAMP.getId()
									|| tiles[i + 1][j] == Tile.SHIP_TILE.getId()) {
								// Destroy wood and produce an item
								//addEntity(new PhysicalItem(1004, this, true, i << 5, j << 5, 0.2 - Math.random() * 0.4, 0.2 - Math.random() * 0.4, new InventoryTile(4, 1)));
								tiles[i][j] = Tile.SKY.getId();
								change = true;
							}
						}
					} else if (tiles[i][j] == Tile.LEAVES.getId()){
						if (i > 1 && i < width - 1 && j > 0 && j < height - 1) {
							if (!(tiles[i][j + 1] == Tile.NATURAL_WOOD.getId() 
									|| tiles[i][j + 1] == Tile.LEAVES.getId()
									|| Tile.tiles[tiles[i + 1][j]].isSolid() 
									|| Tile.tiles[tiles[i - 1][j]].isSolid() 
									|| Tile.tiles[tiles[i][j + 1]].isSolid()
									|| tiles[i + 1][j] == Tile.NATURAL_WOOD.getId()
									|| tiles[i - 1][j] == Tile.NATURAL_WOOD.getId()
									|| tiles[i + 2][j] == Tile.NATURAL_WOOD.getId()
									|| tiles[i - 2][j] == Tile.NATURAL_WOOD.getId()
									|| tiles[i + 3][j] == Tile.NATURAL_WOOD.getId()
									|| tiles[i - 3][j] == Tile.NATURAL_WOOD.getId())
									// Conditions for breakage:
									|| tiles[i][j + 1] == Tile.SHIP_BACKGROUND.getId()
									|| tiles[i][j + 1] == Tile.SHIP_BACKGROUND_LAMP.getId()
									|| tiles[i][j + 1] == Tile.SHIP_TILE.getId()
									|| tiles[i - 1][j] == Tile.SHIP_BACKGROUND.getId()
									|| tiles[i - 1][j] == Tile.SHIP_BACKGROUND_LAMP.getId()
									|| tiles[i - 1][j] == Tile.SHIP_TILE.getId()
									|| tiles[i + 1][j] == Tile.SHIP_BACKGROUND.getId()
									|| tiles[i + 1][j] == Tile.SHIP_BACKGROUND_LAMP.getId()
									|| tiles[i + 1][j] == Tile.SHIP_TILE.getId()) {
								// Destroy leaves and produce an item
								//if (Math.random() < 0.33) addEntity(new PhysicalItem(1009, this, true, i << 5, j << 5, 0.2 - Math.random() * 0.4, 0.2 - Math.random() * 0.4, new InventoryTile(9, 1)));
								//else if (Math.random() < 0.66) addEntity(new PhysicalItem(12006, this, true, i << 5, j << 5, 0.1 - Math.random() * 0.4, 0.3 - Math.random() * 0.4, new Ingredient(6,1)));
								//else addEntity(new PhysicalItem(12000, this, true, i << 5, j << 5, 0.3 - Math.random() * 0.6, 0.1 - Math.random() * 0.4, new Ingredient(0,1)));
								tiles[i][j] = Tile.SKY.getId();
								change = true;
							}
						}
					} else if (tiles[i][j] == Tile.OXYGEN_TETHER.getId()) {
						activatedOxygenTethers[i][j] = false;
						if (j < height - 1 && !Tile.tiles[tiles[i][j + 1]].isSolid()) {
							//addEntity(new PhysicalItem(1034, this, true, i << 5, j << 5, 0.05 - Math.random() * 0.1, - Math.random() * 0.075, new InventoryTile(34, 1)));
							tiles[i][j] = Tile.SKY.getId();
							change = true;
						} else {
						oxygenTetherLoop:
							for (int k = -16; k <= 16; k++) {
								for (int l = -16; l <= 16; l++) {
									if (i + k > 0 && i + k < width && j + l > 0 && j + l < height) {
										if (activatedOxygenTethers[i + k][j + l]) {
											activatedOxygenTethers[i][j] = true;
											break oxygenTetherLoop;
										}
									}
								}
							}
						}
					} else {// Apply gravity to tiles not supported on any side
						activatedOxygenTethers[i][j] = false;
						if (i > 1 && i < width - 1 && j > 1 && j < height - 1) {
							if (tiles[i - 1][j] <= 2 && tiles[i + 1][j] <= 2 && tiles[i][j - 1] <= 2 && tiles[i][j + 1] <= 2) {
								int replacementID = tiles[i][j + 1];
								int tileID = tiles[i][j];
								if (Tile.tiles[tileID].getClass() == DestructibleTile.class) {
									durabilities[i][j + 1] = ((DestructibleTile) Tile.tiles[tileID]).getBaseDurability();
									baseDurabilities[i][j + 1] = ((DestructibleTile) Tile.tiles[tileID]).getBaseDurability();
								} else if (Tile.tiles[tileID].getClass() == BackgroundDestructibleTile.class) {
									durabilities[i][j + 1] = ((BackgroundDestructibleTile) Tile.tiles[tileID]).getBaseDurability();
									baseDurabilities[i][j + 1] = ((BackgroundDestructibleTile) Tile.tiles[tileID]).getBaseDurability();
								}
								tiles[i][j] = replacementID;
								tiles[i][j + 1] = tileID;
								change = true;
							}
						}
					}
					
					// Determine conveyor position
					if (tiles[i][j] >= Tile.CONVEYOR.getId() && tiles[i][j] <= Tile.CONVEYOR_MIDDLE.getId()) {
						if (i > 0 && tiles[i - 1][j] >= Tile.CONVEYOR.getId() && tiles[i - 1][j] <= Tile.CONVEYOR_MIDDLE.getId()) {
							if (i < width && tiles[i + 1][j] >= Tile.CONVEYOR.getId() && tiles[i + 1][j] <= Tile.CONVEYOR_MIDDLE.getId()) {
								tiles[i][j] = Tile.CONVEYOR_MIDDLE.getId();
							} else {
								tiles[i][j] = Tile.CONVEYOR_RIGHT_END.getId();
							}
						} else if (i < width && tiles[i + 1][j] >= Tile.CONVEYOR.getId() && tiles[i + 1][j] <= Tile.CONVEYOR_MIDDLE.getId()) {
							tiles[i][j] = Tile.CONVEYOR_LEFT_END.getId();
						} else {
							tiles[i][j] = Tile.CONVEYOR.getId();
						}
					}
					
					// Causes inconsistent performance
					/*if (!(i == game.input.lastDestructibleX && j == game.input.lastDestructibleY) && durabilities[i][j] < baseDurabilities[i][j]) {
						durabilities[i][j] = baseDurabilities[i][j];
					}
					
					if (i == game.input.lastDestructibleX && j == game.input.lastDestructibleY && !leftButtonHeld && durabilities[i][j] < baseDurabilities[i][j]) {
						durabilities[i][j] = baseDurabilities[i][j];
					}*/
				}
				
				if (!(tiles == null || exploredTiles == null || activatedOxygenTethers == null) && exploredTiles[i][j] && activatedOxygenTethers[i][j] && !(tiles[i][j] == Tile.OXYGEN_TETHER.getId())) {
					activatedOxygenTethers[i][j] = false;
				}
				
				if (change) {
					i--;
					j--;
				}
			}
		}
		
		System.out.println("Updated " + width * height + " tiles in " + (System.currentTimeMillis() - startTime) + " ms");
	}
	
	public String toString() {
		return "[SERVER] " + width + " x " + height + " server level";
	}
}
