package Frame;

import static Utilities.FileUtilities.*;
import static Utilities.PhysicsUtilities.collisionLeft;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import Entities.Crop;
import Entities.ElectricalDevice;
import Entities.Entity;
import Entities.Mob;
import Entities.OxygenGenerator;
import Entities.PhysicalItem;
import Entities.Plant;
import Entities.Player;
import Entities.PowerGenerator;
import Entities.ProductionDevice;
import Entities.Projectile;
import Entities.StoneFurnace;
import Libraries.ItemFactory;
import Libraries.MediaLibrary;
import Tiles.BackgroundDestructibleTile;
import Tiles.DestructibleTile;
import Tiles.Platform;
import Tiles.RandomizedTile;
import Tiles.SolidTile;
import Tiles.Tile;
import UI.InventoryTile;
import UI.InventoryTool;
import UI.ColorSelector;
import UI.Ingredient;
import UI.InventoryEntity;
import UI.InventoryItem;
import Utilities.AudioManager;
import Utilities.FileUtilities;
import Utilities.PhysicsUtilities;

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
	private double[][] baseDurabilities;
	private byte[][] discreteLightLevels;
	private byte skyLightLevel = -120;
	public int width, height;
	public int xOffset, yOffset;
	public int spawnX, spawnY;
	public int currentMouseXOnPanel, currentMouseYOnPanel;
	public int index;
	protected BufferedImage image;
	public Sky sky;
	public ElectricalDevice floatingElectricalDevice = null;
	private boolean potentialConnection = false;
	private boolean leftButtonHeld;
	private int maxProjectiles = 65536;
	private int currentProjectiles;
	private double[][] conveyorSpeeds;
	private int[][] tileColors;
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Entity> queuedEntities = new ArrayList<Entity>();
	
	public Level(BufferedImage image, String name, int index, GameLoop mg, int x, int y) {
		this.index = index;
		spawnX = x;
		spawnY = y;
		sky = new Sky("sky1", game);
		this.name = name;
		this.loadLevelFromImage(image);
		game = mg;
		loadHorizon();
		
		discreteLightLevels = new byte[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				discreteLightLevels[i][j] = -100;
			}
		}
		
		initDiscreteLightLevels();
		initConveyorSpeeds();
		initTileColors();
	}
	
	public Level(String path, String name, int indes, GameLoop mg, int x, int y) {
		this.index = index;
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
		
		initDiscreteLightLevels();
		initConveyorSpeeds();
		initTileColors();
	}
	
	public void initDiscreteLightLevels() {
		discreteLightLevels = new byte[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				discreteLightLevels[i][j] = 0;
			}
		}
	}
	
	public void initConveyorSpeeds() {
		conveyorSpeeds = new double[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				conveyorSpeeds[i][j] = 0;
			}
		}
	}
	
	public void initTileColors() {
		tileColors = new int[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tileColors[i][j] = -1;
			}
		}
	}
	
	public boolean hasTileColor(int x, int y) {
		return (inBounds(x, y));
	}
	
	public boolean inBounds(int x, int y) {
		return x >= 0 && y >= 0 && x < width && y < height;
	}
	
	public Color getTileColor(int x, int y) {
		if (hasTileColor(x, y)) return new Color(tileColors[x][y]);
		else return null;
	}
	
	public void setTileColor(int x, int y, int color) {
		tileColors[x][y] = color;
	}
	
	public double getConveyorSpeed(int x, int y) {
		if (x > 0 && y > 0 && x < width && y < height) {
			return conveyorSpeeds[x][y];
		} else return 0;
	}
	
	public void flipConveyor(int x, int y) {
		int i = x;
		
		// Expand left
		while (i > 0 && tiles[i][y] >= Tile.CONVEYOR.getId() && tiles[i][y] <= Tile.CONVEYOR_MIDDLE.getId()) {
			conveyorSpeeds[i][y] = -conveyorSpeeds[i][y];
			i--;
		}
		
		// Expand right
		i = x + 1;
		while (i < width && tiles[i][y] >= Tile.CONVEYOR.getId() && tiles[i][y] <= Tile.CONVEYOR_MIDDLE.getId()) {
			conveyorSpeeds[i][y] = -conveyorSpeeds[i][y];
			i++;
		}
	}
	
	public void setConveyor(int x, int y, double speed) {
		int i = x;
		
		// Expand left
		while (i > 0 && tiles[i][y] >= Tile.CONVEYOR.getId() && tiles[i][y] <= Tile.CONVEYOR_MIDDLE.getId()) {
			conveyorSpeeds[i][y] = speed;
			i--;
		}
		
		// Expand right
		i = x + 1;
		while (i < width && tiles[i][y] >= Tile.CONVEYOR.getId() && tiles[i][y] <= Tile.CONVEYOR_MIDDLE.getId()) {
			conveyorSpeeds[i][y] = speed;
			i++;
		}
	}
	
	public void checkConveyor(int x, int y) {
		if (x > 0 && y >= 0 && x < width && y <= height) {
			if (tiles[x][y] >= Tile.CONVEYOR.getId() && tiles[x][y] <= Tile.CONVEYOR_MIDDLE.getId()) {
				if (tiles[x - 1][y] >= Tile.CONVEYOR.getId() && tiles[x - 1][y] <= Tile.CONVEYOR_MIDDLE.getId()) {
					setConveyor(x, y, conveyorSpeeds[x - 1][y]);
				} else if (tiles[x + 1][y] >= Tile.CONVEYOR.getId() && tiles[x + 1][y] <= Tile.CONVEYOR_MIDDLE.getId()) {
					setConveyor(x, y, conveyorSpeeds[x + 1][y]);
				}
			}
		}
	}
	
	/*	for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) { */
	
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
				if (i - 64 + j - horizon < 72) exploredTiles[i][j] = true;
			}
		}
		
		// Add teaser fog
		int p = (int) (Math.round(Math.random() * (double) (width - 16)) + 8);
		for (int n = p; n < width - 8; n++) {
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
		
		for (int i = 128; i < width; i++) {
			for (int j = horizon; j < horizon + 128; j++) {
				if (i - 64 - 128+ j - horizon > 72) exploredTiles[i][j] = false;
			}
		}
	}
	
	public int getHorizon() {
		return horizon;
	}
	
	public void exploreTile(int x, int y) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			exploredTiles[x][y] = true;
		}
	}
	
	private void loadLevelFromImage(BufferedImage image) {
        	FileUtilities.log("Loading level " + index + " from " + filePath + "...\n\t");
        	
        	this.image = image;
        	
            this.width = this.image.getWidth();
            this.height = this.image.getHeight();
            tiles = new int[width][height];
            tileData = new int[width][height];
            durabilities = new double[width][height];
            baseDurabilities = new double[width][height];
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
            baseDurabilities = new double[width][height];
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
	    			baseDurabilities[x][y] = ((DestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
	    		}
	    		if (Tile.tiles[tiles[x][y]].getClass() == BackgroundDestructibleTile.class) {
	    			durabilities[x][y] = ((BackgroundDestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
	    			baseDurabilities[x][y] = ((BackgroundDestructibleTile) Tile.tiles[tiles[x][y]]).baseDurability;
	    		}
            }
        }
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
		}
	}
	
	public double getDurability(int x, int y) {
		if (0 > x || x >= width || 0 > y || y >= height) return 0.0;
		return durabilities[x][y];
	}
	
	public void setDurability(int x, int y, double durability) {
		if (!(0 > x || x >= width || 0 > y || y >= height)) durabilities[x][y] = durability;
	}
	
	public synchronized void tick() {
		if (queuedEntities != null && queuedEntities.size() > 0) {
			entities.addAll(queuedEntities);
			queuedEntities.clear();
		}
		
		currentProjectiles = 0;
		
		updateTiles();
		
		boolean oxygenConnected = false;
		for (Entity e : getEntities()) {
			if (e == null) continue;
			e.tick();
			if (e.getClass() == OxygenGenerator.class) {
				if (entities.get(0).x > (e.x << 5) - 128 - 64 && entities.get(0).x < (e.x << 5) + 128 + 32
						&& entities.get(0).y > (e.y << 5) - 128 - 128 && entities.get(0).y < (e.y << 5) + 128) {
					((Player) entities.get(0)).connectOxygen();
					((Player) entities.get(0)).addOxygen(0.025);
					((Player) entities.get(0)).addOxygenPoint(e.x * 32, e.y * 32);
					oxygenConnected = true;
					//drawLightCircle(e.x, e.y, 4, 16);
				}
			}
			if (e.getClass() == Projectile.class) {
				currentProjectiles++;
				if (((Projectile) e).getDamagePotential() > 0) {
					for (Entity i : getEntities()) {
						if (i != null && Mob.class.isAssignableFrom(i.getClass())
								&& PhysicsUtilities.checkIntersection(e.x, e.y, i.x, i.y, ((Mob) i).getHitboxWidth(), ((Mob) i).getHitboxHeight(), true)) {
							((Mob) i).damage(((Projectile) e).getDamagePotential() * ((Projectile) e).getSpeed());
						}
					}
				}
			}

			if (e.getClass() == PhysicalItem.class) {
				if (((PhysicalItem) e).getItemTileID() == Tile.LAMP.getId() || ((PhysicalItem) e).getItemTileID() == Tile.TORCH.getId()) {
					//drawLightCircle(e.x >> 5, e.y >> 5, 4, 16);
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
		if (game != null && game.pauseMenuGUI != null && !game.pauseMenuGUI.isActive()) {
			//if (game.input.pageUp.isPressed()) sky.tick(33);
			//else if (game.input.pageDown.isPressed()) sky.tick(7);
			/*else*/ sky.tick(1);
		}
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
		
		potentialConnection = floatingElectricalDevice != null;
		
		if (game.input.drop.isPressed() && game.ticks % 16 == 0) {
			int verticalDelta = game.convertCoordinates(currentMouseXOnPanel, currentMouseYOnPanel).height - ((Player) entities.get(0)).y;
			
			double vY = Math.log(Math.sqrt(Math.abs(verticalDelta))) * Math.signum(verticalDelta);
			double vX = 0;
			int x = ((Player) entities.get(0)).x;
			if (((Player) entities.get(0)).getMovingDir() == 3) {
				x += game.player.spriteWidth;
				vX = 3.5 - Math.abs(vY);
			} else {
				vX = -3.5 + Math.abs(vY);
			}
			
			if (((Player) entities.get(0)).inventory.getActiveItem() != null) {
				if (((Player) entities.get(0)).inventory.getActiveItem().getClass() == Ingredient.class) {
					addEntity(new PhysicalItem(1000 + ((Player) entities.get(0)).inventory.getActiveItem().getItemID(), this, true, 
							x, ((Player) entities.get(0)).y + ((Player) entities.get(0)).spriteHeight / 4, vX, vY, new Ingredient(((Ingredient) ((Player) entities.get(0)).inventory.getActiveItem()).getItemID(), 1)));
					((Ingredient) ((Player) entities.get(0)).inventory.getActiveItem()).removeQuantity(1);
				} else if (((Player) entities.get(0)).inventory.getActiveItem().getClass() == InventoryTile.class) {
					addEntity(new PhysicalItem(1000 + ((Player) entities.get(0)).inventory.getActiveItem().getItemID(), this, true, 
							x, ((Player) entities.get(0)).y + ((Player) entities.get(0)).spriteHeight / 4, vX, vY, new InventoryTile(((InventoryTile) ((Player) entities.get(0)).inventory.getActiveItem()).getTileID(), 1)));
					((InventoryTile) ((Player) entities.get(0)).inventory.getActiveItem()).removeQuantity(1);
				} else {
					addEntity(new PhysicalItem(1000 + ((Player) entities.get(0)).inventory.getActiveItem().getItemID(), this, true, 
							x, ((Player) entities.get(0)).y + ((Player) entities.get(0)).spriteHeight / 4, vX, vY, ((Player) entities.get(0)).inventory.getActiveItem()));
					((Player) entities.get(0)).inventory.getActiveItem().markedForDeletion = true;
				}
			}
		}
		
		if (currentProjectiles > maxProjectiles) {
			int projectilesToRemove = currentProjectiles - maxProjectiles;
			for (Entity e : entities) {
				if (e.getClass() == Projectile.class) {
					e.markedForDeletion = true;
					projectilesToRemove--;
				}
				if (projectilesToRemove <= 0) return;
			}
		}
	}
	
	public synchronized void queueEntity(Entity e) {
		if (e != null) queuedEntities.add(e);
	}
	
	public void updateTiles() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (tiles != null) {
					// Apply gravity to sand not supported from the bottom
					if (tiles[i][j] == Tile.SAND.getId()) {
						if (j < height - 1 && tiles[i][j + 1] <= 2) {
							tiles[i][j] = tiles[i][j + 1];
							durabilities[i][j + 1] = ((DestructibleTile) Tile.SAND).baseDurability;
							tiles[i][j + 1] = Tile.SAND.getId();
						}
					} else { // Apply gravity to tiles not supported on any side
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
			}
		}
	}
	
	public void resetDestructibleTile(int x, int y) {
		durabilities[x][y] = baseDurabilities[x][y];
	}
	
	public void calculateDiscreteLighting() {
		if (discreteLightLevels == null) discreteLightLevels = new byte[width][height];
		
		//System.out.println((byte) Math.ceil(-127 * Math.sin(2 * Math.PI * (sky.time - (sky.totalDayTime / 3)) / (sky.totalDayTime * 1000))));
		
		double skySineFunction = Math.sin(2 * Math.PI * (sky.time + (sky.totalDayTime * 1000 / 4)) / (sky.totalDayTime * 1000));
		double flattenedSine = Math.signum(skySineFunction) * Math.max(0, Math.min(1, 1.25 * Math.pow(Math.abs(skySineFunction), 0.5)));
		
		skyLightLevel = (byte) Math.ceil(-127 * flattenedSine);
		
		//System.out.println("Sky light level: " + skyLightLevel + " (Raw sine: " + skySineFunction + ")"  + " (Flattened sine: " + flattenedSine + ")");
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				discreteLightLevels[i][j] = skyLightLevel;
			}
		}
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (tiles != null && tiles[i][j] == Tile.LAMP.getId()) {
					drawLightCircle(i, j, 16, 1);
				} else if (tiles != null && tiles[i][j] == Tile.TORCH.getId()) {
					drawLightCircle(i, j, 8, 3);
				} else if (tiles != null && tiles[i][j] == Tile.SHIP_BACKGROUND_LAMP.getId()) {
					drawLightCircle(i, j, 16, 2);
				}
			}
		}
		
		if (game.input.light.isPressed()) {
			if (entities != null && entities.get(0).getClass() == Player.class) {
				if (((Player) entities.get(0)).getMovingDir() == 2)	drawLightCircle(((Player) entities.get(0)).x >> 5, ((Player) entities.get(0)).y >> 5, 2, 8);
				else if (((Player) entities.get(0)).getMovingDir() == 3) drawLightCircle((((Player) entities.get(0)).x >> 5) + 3, ((Player) entities.get(0)).y >> 5, 2, 8);
			}
		}
	}
	
	public void drawLightCircle(int i, int j, int radius, int linearFalloff) {
		for (int k = 0; k < radius * 2; k++) {
			for (int l = 0; l < radius * 2; l++) {
				int distance = (((k - radius) * (k - radius)) + ((l - radius) * (l - radius))) * linearFalloff;
				//System.out.print((byte) (127 - (transparencyDepth * distance)) + " ");
				if (exploredTiles != null && exploredTiles[i + k - radius][j + l - radius] && discreteLightLevels != null 
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
	
	public void setMousePositionOnPanel(int x, int y) {
		currentMouseXOnPanel = x;
		currentMouseYOnPanel = y;
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
						if (tiles[x + i][y + j] == Tile.LEAVES.getId()) tiles[x + i][y + j] = Tile.SKY.getId();
						if (Math.random() < 0.25) ((Player) entities.get(0)).inventory.addItem(ItemFactory.createItem("i", new int[] {6, 1}));
					}
				}
			}
		}
	}
	
	public void generateTree(int x, int baseY) {
		int treeTop = (int) (baseY - Math.round(Math.random() * 8) - 5);
		if (x >= 8 && x < width - 8 && baseY >= 16 && baseY < height) {
			for (int i = -2; i <= 2; i++) {
				for (int j = -2; j <= 2; j++) {
					if(!(Math.abs(i) == 2 && Math.abs(j) == 2)) {
						setTile(x + i, treeTop + j, Tile.LEAVES.getId());
						setDurability(x + i, treeTop + j, ((BackgroundDestructibleTile) Tile.LEAVES).getDurability());
					}
				}
			}
			
			for (int j = treeTop; j < baseY; j++) {
				setTile(x, j, Tile.NATURAL_WOOD.getId());
				setDurability(x, j, ((BackgroundDestructibleTile) Tile.NATURAL_WOOD).getDurability());
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
	
	public synchronized void checkLeftClickEntityCollision(int clickX, int clickY) {
		for (Entity e : entities) {
			if (e != null && e.getClass() == Crop.class) {
				//System.out.println("x: " + clickX + " y: " + clickY + " target x: " + e.x + " target y " + e.y + " target width: " + ((Crop) e).cropWidth + " target height: " + ((Crop) e).cropHeight);
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, e.x, e.y, ((Crop) e).cropWidth, ((Crop) e).cropHeight, true)) {
					e.markedForDeletion = true;
					
					for (InventoryItem i : ((Crop) e).returnDroppables()) {
						((Player) entities.get(0)).inventory.addItem(i);
					}
				}
			} else if (e != null && ElectricalDevice.class.isAssignableFrom(e.getClass())) {
				//System.out.println("x: " + clickX + " y: " + clickY + " target x: " + ((e.x << 5) - 1) + " target y " + ((e.y << 5) - 16) + " target width: " + 36 + " target height: " + 48);
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, (e.x << 5) - 1, (e.y << 5) - 16, 36, 48, true)) {
					if (game.input.ctrl.isPressed() && game.input.shift.isPressed()) {
						FileUtilities.log("Picked up an electrical device\n");
						game.audioManager.play(2);
						((ElectricalDevice) e).clearAllConnections();
						((Player) entities.get(0)).inventory.addItem(new InventoryEntity(e));
						e.markedForDeletion = true;
						e = null;
					} else if (game.input.ctrl.isPressed()) {
						FileUtilities.log("Ctrl + Clicked on an electrical device\n");
						((ElectricalDevice) e).clearAllConnections();
						game.audioManager.play(4);
					} else {
						if (e.getClass() == PowerGenerator.class) {
							if (((Player) entities.get(0)).inventory.getActiveItem() != null 
									&& ((Player) entities.get(0)).inventory.getActiveItem().getClass() == Ingredient.class 
									&& ((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).getItemID() == 12)	{
								FileUtilities.log("Fueled up power generator ");
								int totalPower = 0;
								int quantity = 0;
								boolean fuelSuccessful = false;
								if (game.input.shift.isPressed()) {
									while (((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).getQuantity() >= 1) {
										double energy = 2000 + Math.floor(Math.random() * 1000);
										
										fuelSuccessful = ((PowerGenerator) e).insertFuel(energy);
										
										if (fuelSuccessful) {
											((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).removeQuantity(1);
											quantity++;
											totalPower += energy;
										} else {
											break;
										}
									}
								} else {
									double energy = 2000 + Math.floor(Math.random() * 1000);
									
									fuelSuccessful = ((PowerGenerator) e).insertFuel(energy);
									
									if (fuelSuccessful) {
										((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).removeQuantity(1);
										quantity++;
										totalPower += energy;
									}
								}
								FileUtilities.log(String.format("Inserted " + quantity + " x coal into power generator [%.3f kW]\n", totalPower / 1000D));
								if (fuelSuccessful) game.audioManager.play(0);
							}
						}
					}
				}
			} else if (e != null && e.getClass() == StoneFurnace.class) {
				//System.out.println("x: " + clickX + " y: " + clickY + " target x: " + ((e.x << 5) - 1) + " target y " + ((e.y << 5) - 16) + " target width: " + 36 + " target height: " + 48);
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, (e.x << 5), (e.y << 5), 32, 32, true)) {
					if (((Player) entities.get(0)).inventory.getActiveItem() != null 
							&& ((Player) entities.get(0)).inventory.getActiveItem().getClass() == Ingredient.class 
							&& ((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).getItemID() == 12)	{
						FileUtilities.log("Fueled up stone furnace with ");
						int coal = 0;
						if (!game.input.shift.isPressed()) {
							coal = 1;
							if (!((StoneFurnace) e).fuel()) coal = 0;
						} else {
							for(int i = 0; i <= ((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).getQuantity(); i++) {
								if (!((StoneFurnace) e).fuel()) break;
								coal++;
							}
						}
						FileUtilities.log(coal + " coal\n");
						if (coal > 0) ((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).removeQuantity(coal);
					} else if (((Player) entities.get(0)).inventory.getActiveItem() != null 
							&& ((Player) entities.get(0)).inventory.getActiveItem().getClass() == InventoryTile.class 
							&& (((InventoryTile) (((Player) entities.get(0)).inventory.getActiveItem())).getTileID() == Tile.IRON_ORE.getId()
							|| ((InventoryTile) (((Player) entities.get(0)).inventory.getActiveItem())).getTileID() == Tile.COPPER_ORE.getId()
							|| ((InventoryTile) (((Player) entities.get(0)).inventory.getActiveItem())).getTileID() == Tile.COBBLESTONE.getId()))	{
						((StoneFurnace) e).setStoredItem(((Player) entities.get(0)).inventory.getActiveItem(), game.input.shift.isPressed());
					}
				}
			}
			
			if (e != null && ProductionDevice.class.isAssignableFrom(e.getClass())) {
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, (e.x << 5) - 1, (e.y << 5) - 16, 36, 48, true)) {
					FileUtilities.log("Clicked on production device: " + e.toString() + "\n");
					if (game.input.alt.isPressed()) {
						FileUtilities.log("Toggled output direction of production device: " + e.toString() + "\n");
						((ProductionDevice) e).toggleOutputDirection();
					} else if (((Player) entities.get(0)).inventory.getActiveItem() != null && ((ProductionDevice) e).isAcceptableInput(((Player) entities.get(0)).inventory.getActiveItem())) {
						FileUtilities.log("Trying to insert " + ((Player) entities.get(0)).inventory.getActiveItem().toString() + " into " + e.toString() + "\n");
						if (!((ProductionDevice) e).tryToPopulateInputWithClonedItems(new InventoryItem[] {((Player) entities.get(0)).inventory.getActiveItem()})) {
							FileUtilities.log("\tInsertion failed");
							System.out.print(" :(");
							FileUtilities.log("\n");
						} else {
							FileUtilities.log("\tInsertion successful");
							System.out.print(" ;)");
							FileUtilities.log("\n");
							System.out.println("Manually added item " + ((Player) entities.get(0)).inventory.getActiveItem().toString() + " to production device " + ((ProductionDevice) e).toString());
							((Player) entities.get(0)).inventory.getActiveItem().markedForDeletion = true;
						}
					}
				}
			}
		}
	}
	
	public synchronized void checkRightClickEntityCollision(int clickX, int clickY) {
		for (Entity e : entities) {
			if (e != null && ElectricalDevice.class.isAssignableFrom(e.getClass())) {
				//System.out.println("x: " + clickX + " y: " + clickY + " target x: " + ((e.x << 5) - 1) + " target y " + ((e.y << 5) - 16) + " target width: " + 36 + " target height: " + 48);
				
				
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, (e.x << 5) - 1, (e.y << 5) - 16, 36, 48, true)) {
					if (game.input.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY) {
						FileUtilities.log("Right clicked on an electrical device\n");
							if (floatingElectricalDevice == null) floatingElectricalDevice = (ElectricalDevice) e;
							else if (!floatingElectricalDevice.equals(e)) {
								double wireDistance = Math.sqrt((floatingElectricalDevice.x - e.x) * (floatingElectricalDevice.x - e.x) + (floatingElectricalDevice.y - e.y) * (floatingElectricalDevice.y - e.y));
								// Check to see if the player has enough copper wire in their active item slot
								if (((Player) entities.get(0)).inventory.getActiveItem() != null 
										&& ((Player) entities.get(0)).inventory.getActiveItem().getClass() == Ingredient.class 
										&& ((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).getItemID() == 11
										&& ((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).getQuantity() > Math.ceil(wireDistance)) {
									((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).removeQuantity((int) Math.ceil(wireDistance));
									FileUtilities.log((int) Math.ceil(wireDistance) + " wire consumed\n");
									if (floatingElectricalDevice.connectDevice((ElectricalDevice) e, (int) Math.ceil(wireDistance))
											&& ((ElectricalDevice) e).connectDevice(floatingElectricalDevice, (int) Math.ceil(wireDistance))) {
										FileUtilities.log("Connected two electrical devices\n");
										game.audioManager.play(3);
									}
		
									else {
										FileUtilities.log("Connection failed: one or more devices refused connection ");
										((ElectricalDevice) e).removeConnection(floatingElectricalDevice);
										floatingElectricalDevice.removeConnection(((ElectricalDevice) e));
										FileUtilities.log("\n");
									}
								} else {
									FileUtilities.log("Connection failed: not enough wire (" + (int) Math.ceil(wireDistance) + " required)\n");
									return;
								}
								
								floatingElectricalDevice = null;
							}
							return;
						}
					}
				} else if (e != null && e.getClass() == StoneFurnace.class) {
					//System.out.println("x: " + clickX + " y: " + clickY + " target x: " + ((e.x << 5) - 1) + " target y " + ((e.y << 5) - 16) + " target width: " + 36 + " target height: " + 48);
					if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, (e.x << 5), (e.y << 5), 32, 32, true)) {
						((StoneFurnace) e).removeItem(((Player) entities.get(0)).inventory, game.input.shift.isPressed());
					}
				}
			}
		
		// Clear the selected device if the click does not occur on another device
		if (floatingElectricalDevice != null) FileUtilities.log("Connection canceled\n");
		floatingElectricalDevice = null;
		return;
	}
	
	public boolean checkLeftClickTileCollision(int clickX, int clickY) {
		if (clickX > 0 && clickY > 0 && clickX < width && clickY < height) {
			if ((((Player) entities.get(0)).inventory.getActiveItem() == null || !(((Player) entities.get(0)).inventory.getActiveItem().getClass().getSuperclass() == InventoryTool.class))
					&& tiles[clickX][clickY] >= Tile.CONVEYOR.getId() && tiles[clickX][clickY] <= Tile.CONVEYOR_MIDDLE.getId()) {
				if (conveyorSpeeds[clickX][clickY] == 0) setConveyor(clickX, clickY, 0.5);
				else if (game.input.ctrl.isPressed()) setConveyor(clickX, clickY, 0);
				else flipConveyor(clickX, clickY);
				resetDestructibleTile(clickX, clickY);
				return true;
			} else if (!(((Player) entities.get(0)).inventory.getActiveItem() == null) && ColorSelector.class.isAssignableFrom(((Player) entities.get(0)).inventory.getActiveItem().getClass())) {
				setTileColor(clickX, clickY, ((ColorSelector) ((Player) entities.get(0)).inventory.getActiveItem()).getColor().getRGB());
			}
		}
		return false;
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
		
		Graphics2D g2 = (Graphics2D) g;
		
		if (potentialConnection) {
			drawPotentialConnection(g2, floatingElectricalDevice);
		}
	}
	
	public void drawPotentialConnection(Graphics2D g2, ElectricalDevice d) {
		double wireDistance = Math.sqrt((d.getLevelX() - currentMouseXOnPanel) * (d.getLevelX() - currentMouseXOnPanel)
				+ (d.getLevelY() - currentMouseYOnPanel) * (d.getLevelY() - currentMouseYOnPanel));
		
		if (((Player) entities.get(0)).inventory.getActiveItem() != null 
				&& ((Player) entities.get(0)).inventory.getActiveItem().getClass() == Ingredient.class 
				&& ((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).getItemID() == 11
				&& ((Ingredient) (((Player) entities.get(0)).inventory.getActiveItem())).getQuantity() > Math.ceil(wireDistance / 32)) g2.setColor(Color.GREEN);
		else g2.setColor(Color.RED);
		
		g2.setStroke(new BasicStroke(4));
		g2.drawLine(currentMouseXOnPanel, currentMouseYOnPanel, d.getLevelX() + ((ElectricalDevice) d).getConnectionOffsetX(), d.getLevelY() + ((ElectricalDevice) d).getConnectionOffsetY());
		
		g2.setColor(Color.WHITE);
		
		g2.setFont(MediaLibrary.getFontFromLibrary("INFOFont"));
		g2.drawString("Required wire: " + (int) Math.ceil(wireDistance / 32), d.getLevelX(), d.getLevelY() - 64);
	} 
	
	public void drawSky(Graphics g, Dimension resolution) {
		sky.draw(g, resolution, game, !game.isPaused());
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
				else if (tiles[targetX][targetY] == 21) game.player.inventory.addItem(new Ingredient(12, 3 + (int) Math.ceil(8 * Math.random())));
				else game.player.inventory.addItem(new InventoryTile(tiles[targetX][targetY], 1));
				setTile(targetX, targetY, 2);
				if (game.tracker != null) game.tracker.incrementBasicStat("Tiles Mined");
			}
		}
	}
	
	public void setLeftButtonHeld(boolean held) {
		leftButtonHeld = held;
	}
}
