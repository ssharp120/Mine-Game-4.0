package SingleplayerClient;

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
import Entities.Enemy;
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
	private boolean[][] exploredTiles;
	private boolean[][] visibleTiles;
	private boolean[][] activatedOxygenTethers;
	private double percentExplored;
	private double[][] durabilities;
	private double[][] baseDurabilities;
	private byte[][] discreteLightLevels;
	private boolean[][] drawCaveWall;
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
	private Player player;
	private boolean queueUpdate = false;
	private boolean playerConnectedToOxygenGenerator = false;
	private int enemyCount;
	private final int enemyCap = 255;
	private double attractionStrength = 0.05;
	private double attractionRadius = 128;
	
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
		
		discreteLightLevels = new byte[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				discreteLightLevels[i][j] = -100;
			}
		}
		
		initDiscreteLightLevels();
		initConveyorSpeeds();
		initTileColors();
		initOxygenTethers();
		fillUnexploredAreas();
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
		initOxygenTethers();
		fillUnexploredAreas();
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
	
	public void initCaveWalls() {
		drawCaveWall = new boolean[width][height];
	}
	
	public void initOxygenTethers() {
		activatedOxygenTethers = new boolean[width][height];
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
					visibleTiles[i][j] = !drawCaveWall[i][j];
					if (j < height - 1 && !(tiles[i][j] == tiles[i][j + 1])) {
						byte remainingDistance = 5;
						for (int l = 0; l <= remainingDistance; l++) {
							if (j + l < height) visibleTiles[i][j + l] = !drawCaveWall[i][j + l];
							if (j + l < height && !Tile.tiles[tiles[i][j + l]].isSolid()) remainingDistance++;
						}
					}	
				}
			}
		}
	}
	
	public void exploreTile(int x, int y) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			exploredTiles[x][y] = true;
		}
	}
	
	private void loadLevelFromImage(BufferedImage image) {
        	this.image = image;
        	
            this.width = this.image.getWidth();
            this.height = this.image.getHeight();
            
            FileUtilities.log("Loading level " + index + " from " + width + " x " + height + " image...\n\t");
            
            tiles = new int[width][height];
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
        	
            loadLevelFromImage(this.image);
        } catch (IOException e) {
        	FileUtilities.log("Error occured while loading level " + index + " from " + filePath);
            e.printStackTrace();
            System.exit(1);
        }
    }
	
	private void loadTiles() {
		initCaveWalls();
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
        
        for (int x = 0; x < width; x++) {
        	boolean caveWall = false;
        	for (int y = 0; y < height; y++) {
        		if (tiles[x][y] == Tile.CAVE_WALL.getId()) {
        			caveWall = true;
        			if (y + 1 < height) tiles[x][y] = tiles[x][y + 1];
        		}
        		drawCaveWall[x][y] = caveWall;
        	}
        }
	}
	
	public boolean drawCaveWall(int x, int y) {
		return (x >= 0 && x < width && y >= 0 && y < height) && drawCaveWall[x][y];
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
		
		getPlayer().queuePlayerModelUpdate();
	}
	
	public double getDurability(int x, int y) {
		if (0 > x || x >= width || 0 > y || y >= height) return 0.0;
		return durabilities[x][y];
	}
	
	public void setDurability(int x, int y, double durability) {
		if (!(0 > x || x >= width || 0 > y || y >= height)) durabilities[x][y] = durability;
	}
	
	public void placeTile(int clickX, int clickY, int tileID) {
		if (getPlayer() != null) {
			if (getTile(clickX >> 5, clickY >> 5).getId() == 2) {
				if (getTile((clickX >> 5) + 1, clickY >> 5).getId() == 2 && getTile((clickX >> 5) - 1, clickY >> 5).getId() == 2 && getTile(clickX >> 5, (clickY >> 5) + 1).getId() == 2 && getTile(clickX >> 5, (clickY >> 5) - 1).getId() == 2) return;
				
				int t = tileID;
				
				//System.out.println("Player x: Left: " + gameIn.player.x + ", Right: " + (gameIn.player.x + gameIn.player.spriteWidth) + ", Tile x: Left: " + ((clickX >> 5) << 5) + ", Right: " + (((clickX >> 5) << 5) + 32));
				if (((clickX >> 5) << 5) + 32 >= getPlayer().x && (clickX >> 5) << 5 <= getPlayer().x + getPlayer().spriteWidth 
						&& ((clickY >> 5) << 5) + 32 >= getPlayer().y && (clickY >> 5) << 5 <= getPlayer().y + getPlayer().spriteHeight
						&& Tile.tiles[t].getClass() != BackgroundDestructibleTile.class) return;
				
				setTile(clickX >> 5, clickY >> 5, t);
				if (Tile.tiles[t].getClass() == DestructibleTile.class) {
					setDurability(clickX >> 5, clickY >> 5, ((DestructibleTile) Tile.tiles[t]).getBaseDurability());
				}
				if (Tile.tiles[t].getClass() == BackgroundDestructibleTile.class) {
					setDurability(clickX >> 5, clickY >> 5, ((BackgroundDestructibleTile) Tile.tiles[t]).getBaseDurability());
				}
			}
		}
	}
	
	public void placeTile(int clickX, int clickY) {
		if (getPlayer() != null) {
			if (getTile(clickX >> 5, clickY >> 5).getId() == 2) {
				if (!drawCaveWall[clickX >> 5][clickY >> 5] && getTile((clickX >> 5) + 1, clickY >> 5).getId() == 2 && getTile((clickX >> 5) - 1, clickY >> 5).getId() == 2 && getTile(clickX >> 5, (clickY >> 5) + 1).getId() == 2 && getTile(clickX >> 5, (clickY >> 5) - 1).getId() == 2) return;
				
				int t = getPlayer().inventory.getTileFromHotbar();
				
				//System.out.println("Player x: Left: " + player.x + ", Right: " + (player.x + player.spriteWidth) + ", Tile x: Left: " + ((clickX >> 5) << 5) + ", Right: " + (((clickX >> 5) << 5) + 32));
				if (((clickX >> 5) << 5) + 32 >= getPlayer().x && (clickX >> 5) << 5 <= getPlayer().x + getPlayer().spriteWidth 
						&& ((clickY >> 5) << 5) + 32 >= getPlayer().y && (clickY >> 5) << 5 <= getPlayer().y + getPlayer().spriteHeight
						&& Tile.tiles[t].getClass() != BackgroundDestructibleTile.class) return;
				
				((InventoryTile) getPlayer().inventory.getActiveItem()).removeQuantity(1);
				if (game.tracker != null) game.tracker.incrementBasicStat("Tiles Placed");
				setTile(clickX >> 5, clickY >> 5, t);
				if (Tile.tiles[t].getClass() == DestructibleTile.class) {
					setDurability(clickX >> 5, clickY >> 5, ((DestructibleTile) Tile.tiles[t]).getBaseDurability());
				}
				if (Tile.tiles[t].getClass() == BackgroundDestructibleTile.class) {
					setDurability(clickX >> 5, clickY >> 5, ((BackgroundDestructibleTile) Tile.tiles[t]).getBaseDurability());
				}
				if (t == Tile.CONVEYOR.getId()) {
					if (clickX >> 5 > 0 && clickX >> 5 < width && clickY >> 5 > 0 && clickY >> 5 < height) {
						checkConveyor(clickX >> 5, clickY >> 5);
					}
				}
			}
		}
	}
	
	public synchronized void tick() {
		if (queuedEntities != null && queuedEntities.size() > 0) {
			entities.addAll(queuedEntities);
			queuedEntities.clear();
		}
		
		currentProjectiles = 0;
		
		if (queueUpdate && game.ticks % 2 == 1) {
			updateTiles();
			queueUpdate = false;
		}
		
		boolean oxygenConnected = false;
		
		if (!(entities.get(0) == null)) entities.get(0).tick();
		
		playerConnectedToOxygenGenerator = false;
		
		enemyCount = 0;
		
		for (Entity e : getEntities()) {
			if (e == null) continue;
			e.tick();
			if (e.getClass() == OxygenGenerator.class) {
				if (entities.get(0).x > (e.x << 5) - 128 - 64 && entities.get(0).x < (e.x << 5) + 128 + 32
						&& entities.get(0).y > (e.y << 5) - 128 - 128 && entities.get(0).y < (e.y << 5) + 128) {
					getPlayer().connectOxygen();
					getPlayer().addOxygen(0.0875);
					getPlayer().addOxygenPoint(e.x * 32, e.y * 32);
					oxygenConnected = true;
					playerConnectedToOxygenGenerator = true;
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
				
				if (attractionStrength > 0 && attractionRadius > 0) {
					if (Math.sqrt(Math.pow(Math.abs(e.x - player.x), 2) + Math.pow(Math.abs(e.y - player.y), 2)) < attractionRadius) {
						double horizontalAttractionStrength = attractionStrength / 2;
						double verticalAttractionStrength = attractionStrength * Math.sqrt(3) / 2;
						double vX = horizontalAttractionStrength * Math.signum(player.x + player.spriteWidth / 2 - e.x + 16);
						double vY = verticalAttractionStrength * Math.signum(player.y + player.spriteHeight / 2 - e.y + 16);
						if (((PhysicalItem) e).getSpeed() < attractionStrength * 10) ((PhysicalItem) e).addSpeed(vX, vY);
					}
				}
			}
			
			if (e.getClass() == Enemy.class) {
				enemyCount++;
			}
		}
		entities.removeIf(i -> i.markedForDeletion);
		/*if (game.ticks % 250 == 160) {
			for (int y = (player.y >> 5); y < height - 1; y++) {
				for (int x = 0; x < width; x++) {
					checkLeafDecay(x, y);
				}
			}
		}*/
		if (game != null && game.pauseMenuGUI != null && !game.pauseMenuGUI.isActive()) {
			//if (game.input.pageUp.isPressed()) sky.tick(33);
			//else if (game.input.pageDown.isPressed()) sky.tick(7);
			/*else*/ sky.tick(1);
		}
		if (!oxygenConnected) {
			getPlayer().disconnectOxygen();
			getPlayer().removeOxygen(0.01);
		}
		for (int i = -64; i < 64; i++) {
			for (int j = -32; j < 32; j++) {
				if (getPlayer().x >= -16 * 32 && getPlayer().x >> 5 < width + 16 * 32 && getPlayer().y >= -8 * 32 && getPlayer().y >> 5 < height + 8 * 32
						&& i * i + j * j <= 64 && (getPlayer().x >> 5) + i >= 0 && (getPlayer().x >> 5) + i < width
						&& (getPlayer().y >> 5) + j >= 0 && (getPlayer().y >> 5) + j < height
						&& !visibleTiles[(getPlayer().x >> 5) + i][(getPlayer().y >> 5) + j]) visibleTiles[(getPlayer().x >> 5) + i][(getPlayer().y >> 5) + j] = true;
				if (getPlayer().x >= -16 * 32 && getPlayer().x >> 5 < width + 16 * 32 && getPlayer().y >= -8 * 32 && getPlayer().y >> 5 < height + 8 * 32
					&& i * i + j * j <= 32 * 32 && (getPlayer().x >> 5) + i >= 0 && (getPlayer().x >> 5) + i < width
					&& (getPlayer().y >> 5) + j >= 0 && (getPlayer().y >> 5) + j < height
					&& visibleTiles[(getPlayer().x >> 5) + i][(getPlayer().y >> 5) + j]) exploredTiles[(getPlayer().x >> 5) + i][(getPlayer().y >> 5) + j] = true;
			}
		}
		
		if (!playerConnectedToOxygenGenerator) {
			int bestX = 1024;
			int bestY = 1024;
			boolean connection = false;
			
			for (int i = -16; i < 16; i++) {
				for (int j = -16; j < 16; j++) {
	
					if ((getPlayer().x >> 5) + i >= 0 && (getPlayer().x >> 5) + i < width
						&& (getPlayer().y >> 5) + j >= 0 && (getPlayer().y >> 5) + j < height
						&& visibleTiles[(getPlayer().x >> 5) + i][(getPlayer().y >> 5) + j]
						&& exploredTiles[(getPlayer().x >> 5) + i][(getPlayer().y >> 5) + j]) {
						//System.out.println("i " + i + " j " + j);
						if (tiles[(getPlayer().x >> 5) + i][(getPlayer().y >> 5) + j] == Tile.OXYGEN_TETHER.getId()
								&& activatedOxygenTethers[(getPlayer().x >> 5) + i][(getPlayer().y >> 5) + j]) {
							connection = true;
							if (i * i + j * j < bestX * bestX + bestY + bestY) {
								bestX = i;
								bestY = j;
							}
						}
						
					}
				}
			}
			
			if (connection) {
				getPlayer().connectOxygen();
				double distanceMultiplier = Math.pow(((double) (bestX * bestX + bestY * bestY))/16, -1.8);
				getPlayer().addOxygen(distanceMultiplier < 0.0875 ? distanceMultiplier : 0.0875);
				getPlayer().addOxygenPoint((((getPlayer().x >> 5) + bestX) << 5) - 4, (((getPlayer().y >> 5) + bestY) << 5) + 2);
			}
		}
		
		
		int exploredArea = 0;
		for (int m = 0; m < width; m++) {
			for (int n = 0; n < height; n++) {
				if (exploredTiles[m][n]) {
					exploredArea++;
					visibleTiles[m][n] = true;
				}
			}
		}
		percentExplored = (double) exploredArea / (width * height) * 100;
		
		potentialConnection = floatingElectricalDevice != null;
		
		if (game.input.drop.isPressed() && game.ticks % 16 == 0) {
			int verticalDelta = game.convertCoordinates(currentMouseXOnPanel, currentMouseYOnPanel).height - getPlayer().y;
			
			double vY = Math.log(Math.sqrt(Math.abs(verticalDelta))) * Math.signum(verticalDelta);
			double vX = 0;
			int x = getPlayer().x - 16;
			if (getPlayer().getMovingDir() == 3) {
				x += game.player.spriteWidth + 16;
				vX = 3.5 - Math.abs(vY);
			} else {
				vX = -3.5 + Math.abs(vY);
			}
			
			if (getPlayer().inventory.getActiveItem() != null) {
				boolean stack = game.input.ctrl.isPressed();
				int quantity = 1;
				if (getPlayer().inventory.getActiveItem().getClass() == Ingredient.class) {
					if (stack) quantity = ((Ingredient) getPlayer().inventory.getActiveItem()).getQuantity();
					addEntity(new PhysicalItem(1000 + getPlayer().inventory.getActiveItem().getItemID(), this, true, 
							x, getPlayer().y + getPlayer().spriteHeight / 4, vX, vY, new Ingredient(((Ingredient) getPlayer().inventory.getActiveItem()).getItemID(), quantity)));
					((Ingredient) getPlayer().inventory.getActiveItem()).removeQuantity(quantity);
				} else if (getPlayer().inventory.getActiveItem().getClass() == InventoryTile.class) {
					if (stack) quantity = ((InventoryTile) getPlayer().inventory.getActiveItem()).getQuantity();
					addEntity(new PhysicalItem(1000 + getPlayer().inventory.getActiveItem().getItemID(), this, true, 
							x, getPlayer().y + getPlayer().spriteHeight / 4, vX, vY, new InventoryTile(((InventoryTile) getPlayer().inventory.getActiveItem()).getTileID(), quantity)));
					((InventoryTile) getPlayer().inventory.getActiveItem()).removeQuantity(quantity);
				} else {
					addEntity(new PhysicalItem(1000 + getPlayer().inventory.getActiveItem().getItemID(), this, true, 
							x, getPlayer().y + getPlayer().spriteHeight / 4, vX, vY, getPlayer().inventory.getActiveItem()));
					getPlayer().inventory.getActiveItem().markedForDeletion = true;
				}
				getPlayer().queuePlayerModelUpdate();
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
		
		if (enemyCount > enemyCap || game.drawResolution == null) return;
		
		for (int i = game.xOffset >> 5; i < (game.xOffset + game.drawResolution.width) >> 5; i++) {
			for (int j = game.yOffset >> 5; j < (game.yOffset + game.drawResolution.height) >> 5; j++) {
				if (i >= 0 && j >= 0 && i < width && j < height) {
					if (drawCaveWall[i][j] && tiles[i][j] <= 2 && discreteLightLevels[i][j] < 122) {
						if (Math.random() < 0.00000024) generateEnemy("Slime", i << 5, j << 5, 1, 10, 32, 32, 0);
					}
				}
			}
		}
	}
	
	public void setAttractionStrength(double strength) {
		this.attractionStrength = strength;
	}
	
	public void setAttractionRadius(double radius) {
		this.attractionRadius = radius;
	}
	
	public double getAttractionStrength() {
		return attractionStrength;
	}
	
	public double getAttractionRadius() {
		return attractionRadius;
	}
 	
	private void generateEnemy(String name, int x, int y, int speed, int baseHealth, int hitboxWidth, int hitboxHeight, int type) {
		switch (type) {
		case 0: this.addEntity(new Enemy(7701 + type, this, name, x, y, speed, baseHealth, hitboxWidth, hitboxHeight, getPlayer().getLocalGravity(), 0.8, 14.5939)); break;
		default: break;
		}
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
	
	public synchronized void queueEntity(Entity e) {
		if (e != null) queuedEntities.add(e);
	}
	
	public void updateTiles() {
		initOxygenTethers();
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
							addEntity(new PhysicalItem(1033, this, true, i << 5, j << 5, 0.2 - Math.random() * 0.4, 0.2 - Math.random() * 0.4, new InventoryTile(33, 1)));
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
								addEntity(new PhysicalItem(1004, this, true, i << 5, j << 5, 0.2 - Math.random() * 0.4, 0.2 - Math.random() * 0.4, new InventoryTile(4, 1)));
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
								if (Math.random() < 0.33) addEntity(new PhysicalItem(1009, this, true, i << 5, j << 5, 0.2 - Math.random() * 0.4, 0.2 - Math.random() * 0.4, new InventoryTile(9, 1)));
								else if (Math.random() < 0.66) addEntity(new PhysicalItem(12006, this, true, i << 5, j << 5, 0.1 - Math.random() * 0.4, 0.3 - Math.random() * 0.4, new Ingredient(6,1)));
								else addEntity(new PhysicalItem(12000, this, true, i << 5, j << 5, 0.3 - Math.random() * 0.6, 0.1 - Math.random() * 0.4, new Ingredient(0,1)));
								tiles[i][j] = Tile.SKY.getId();
								change = true;
							}
						}
					} else if (tiles[i][j] == Tile.OXYGEN_TETHER.getId()) {
						activatedOxygenTethers[i][j] = false;
						if (j < height - 1 && !Tile.tiles[tiles[i][j + 1]].isSolid()) {
							addEntity(new PhysicalItem(1034, this, true, i << 5, j << 5, 0.05 - Math.random() * 0.1, - Math.random() * 0.075, new InventoryTile(34, 1)));
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
					} else if (tiles[i][j] == Tile.TORCH.getId() || tiles[i][j] == Tile.WOOD_PLATFORM.getId() && drawCaveWall[i][j]) {} // Do nothing
					else { // Apply gravity to tiles not supported on any side
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
					} else conveyorSpeeds[i][j] = 0;
					
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
					
					//i = game.xOffset >> 5;
					//j = game.yOffset >> 5;
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
		
		boolean[][] skipLight = new boolean[width][height];
		for (int i = (getPlayer().x - game.getDrawResolution().width / 2) >> 5; 
				i < (getPlayer().x + game.getDrawResolution().width) >> 5; i++) {
			for (int j = (getPlayer().y - game.getDrawResolution().height) >> 5; 
					j < (getPlayer().y + game.getDrawResolution().height) >> 5; j++) {
				if (i < 1 || i >= width - 1 || j < 0 || j >= height) continue;
				if (!drawCaveWall[i][j] && skyLightLevel > discreteLightLevels[i][j]) discreteLightLevels[i][j] = skyLightLevel;
				else if (drawCaveWall[i][j]) {
					if (!drawCaveWall[i][j - 1] && tiles[i][j - 1] <= 2) {
						int l = j;
						byte exposedLightLevel = discreteLightLevels[i][j - 1];
						while (l < height && exposedLightLevel >= -127 && !Tile.tiles[tiles[i][l - 2]].isSolid()) {
							//System.out.println(exposedLightLevel);
							if (exposedLightLevel <= -5) exposedLightLevel = (byte) (exposedLightLevel * 6 / 5 < -127 ? -127 : exposedLightLevel * 6 / 5);
							else if (exposedLightLevel > 1) exposedLightLevel = (byte) (Math.sqrt(exposedLightLevel));
							else exposedLightLevel = -5;
							if (exposedLightLevel < -127) exposedLightLevel = -127;
							discreteLightLevels[i][l] = exposedLightLevel;

							byte newExposedLightLevel = exposedLightLevel;
							if (newExposedLightLevel <= -5) newExposedLightLevel = (byte) (newExposedLightLevel * 9 / 5 < -127 ? -127 : newExposedLightLevel * 9 / 5);
							else if (newExposedLightLevel > 1) newExposedLightLevel = (byte) (-Math.sqrt(newExposedLightLevel));
							discreteLightLevels[i - 1][l] = newExposedLightLevel > discreteLightLevels[i - 1][l] ? newExposedLightLevel : discreteLightLevels[i - 1][l];
							discreteLightLevels[i + 1][l] = newExposedLightLevel > discreteLightLevels[i + 1][l] ? newExposedLightLevel : discreteLightLevels[i + 1][l];
							
							skipLight[i][l] = true;
							l++;
						}
					}
					if (!skipLight[i][j]) discreteLightLevels[i][j] = -127;
				}
			}
		}
		
		for (int i = (getPlayer().x - game.getDrawResolution().width) >> 5; 
				i < (getPlayer().x + 3 * game.getDrawResolution().width) >> 5; i++) {
			for (int j = (getPlayer().y - game.getDrawResolution().height) >> 5; 
					j < (getPlayer().y + 3 * game.getDrawResolution().height) >> 5; j++) {
				if (i < 0 || i >= width || j < 0 || j >= height) continue;
				if (tiles != null && tiles[i][j] == Tile.LAMP.getId()) {
					drawLightCircle(i, j, 16, 1);
				} else if (tiles != null && tiles[i][j] == Tile.TORCH.getId()) {
					drawLightCircle(i, j, 8, 3);
				} else if (tiles != null && tiles[i][j] == Tile.SHIP_BACKGROUND_LAMP.getId()) {
					drawLightCircle(i, j, 16, 2);
				} else if (activatedOxygenTethers != null && activatedOxygenTethers[i][j]) {
					drawLightCircle(i, j, 12, 3);
				}
			}
		}
		
		//System.out.println("Sky light level: " + skyLightLevel + " (Raw sine: " + skySineFunction + ")"  + " (Flattened sine: " + flattenedSine + ")");

		
		if (game.input.light.isPressed()) {
			if (entities != null && entities.get(0).getClass() == Player.class) {
				if (getPlayer().getMovingDir() == 2)	drawLightCircle(getPlayer().x >> 5, getPlayer().y >> 5, 2, 8);
				else if (getPlayer().getMovingDir() == 3) drawLightCircle((getPlayer().x >> 5) + 3, getPlayer().y >> 5, 2, 8);
			}
		}
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
					if ((byte) (127 - distance) > discreteLightLevels[i + k - radius][j + l - radius]) discreteLightLevels[i + k - radius][j + l - radius] = (byte) (127 - distance);
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
		return (x >= 0 && x < width && y >= 0 && y < height && exploredTiles[x][y]);
	}
	
	public boolean isVisibleNear(int x, int y) {
		return (x > 0 && x < width - 1 && y > 0 && y < height - 1 
				&& (visibleTiles[x + 1][y] || visibleTiles[x - 1][y] 
						|| visibleTiles[x][y + 1] || visibleTiles[x][y - 1]
				|| visibleTiles[x + 1][y + 1] || visibleTiles[x + 1][y - 1]
				|| visibleTiles[x - 1][y + 1] || visibleTiles[x - 1][y - 1]));
	}
	
	public boolean visibleProximity(int x, int y, int length) {
		if (length == 1) return isVisibleNear(x, y);
		else if (x < length || y < length || x >= width - length || y >= height - length) return false;
		else if (length == 2) return ((visibleTiles[x + length][y] || visibleTiles[x - length][y] 
						|| visibleTiles[x][y + length] || visibleTiles[x][y - length])
						|| visibleTiles[x + length][y + length] || visibleTiles[x + length][y - length]
						|| visibleTiles[x - length][y + length] || visibleTiles[x - length][y - length]);
		return (x >= length && x < width - length && y >= length && y < height - length && (visibleTiles[x + length][y] || visibleTiles[x - length][y] || visibleTiles[x][y + length] || visibleTiles[x][y - length]));
	}
	
	public boolean isVisible(int x, int y) {
		return (x >= 0 && x < width && y >= 0 && y < height && visibleTiles[x][y]);
	}
		
	public boolean getLeftButtonHeld() {
		return leftButtonHeld;
	}
	
	public void checkLeafDecay(int x, int y) {
		if (tiles[x][y] == Tile.LEAVES.getId()) {
			boolean woodFound = false;
			int woodRadius = 6;
			for (int i = -woodRadius; i <= woodRadius; i++) {
				for (int j = 0; j < woodRadius; j++) {
					if (x + i > 0 && x + i < width && y + j > 0 && y + j < height && tiles[x + i][y + j] == Tile.NATURAL_WOOD.getId()) woodFound = true;
				}
			}
			if ((tiles[x + 1][y] == Tile.VOID.getId() && tiles[x - 1][y] == Tile.VOID.getId() && tiles[x][y + 1] == Tile.VOID.getId() && tiles[x][y - 1] == Tile.VOID.getId())) {
				tiles[x][y] = Tile.VOID.getId();
			}
			if (!woodFound) {
				for (int i = 0; i < woodRadius; i++) {
					for (int j = 0; j < woodRadius; j++) {
						if (tiles[x + i][y + j] == Tile.LEAVES.getId()) tiles[x + i][y + j] = Tile.SKY.getId();
						if (Math.random() < 0.25) getPlayer().inventory.addItem(ItemFactory.createItem("i", new int[] {6, 1}));
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
		return;
		/*
		for (int y = 0; y < height - 2; y++) {
			for (int x = 0; x < width; x++) {
				if (tiles[x][y] == Tile.SAND.getId() && tiles[x][y - 1] == Tile.SKY.getId() && Math.random() < 0.20) {
					placePlant(x, y);
				}
			}
		}
		*/
	}
	
	public void placePlant(int x, int y) {
		// Temporarily disabled
		return;
		
		/*
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
		*/
	}
	
	public synchronized void checkLeftClickEntityCollision(int clickX, int clickY) {
		for (Entity e : entities) {
			if (e != null && e.getClass() == Crop.class) {
				//System.out.println("x: " + clickX + " y: " + clickY + " target x: " + e.x + " target y " + e.y + " target width: " + ((Crop) e).cropWidth + " target height: " + ((Crop) e).cropHeight);
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, e.x, e.y, ((Crop) e).cropWidth, ((Crop) e).cropHeight, true)) {
					e.markedForDeletion = true;
					
					for (InventoryItem i : ((Crop) e).returnDroppables()) {
						getPlayer().inventory.addItem(i);
					}
				}
			} else if (e != null && ElectricalDevice.class.isAssignableFrom(e.getClass())) {
				//System.out.println("x: " + clickX + " y: " + clickY + " target x: " + ((e.x << 5) - 1) + " target y " + ((e.y << 5) - 16) + " target width: " + 36 + " target height: " + 48);
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, (e.x << 5) - 1, (e.y << 5) - 16, 36, 48, true)) {
					if (game.input.ctrl.isPressed() && game.input.shift.isPressed()) {
						FileUtilities.log("Picked up an electrical device\n");
						game.audioManager.play(2);
						((ElectricalDevice) e).clearAllConnections();
						getPlayer().inventory.addItem(new InventoryEntity(e));
						e.markedForDeletion = true;
						e = null;
					} else if (game.input.ctrl.isPressed()) {
						FileUtilities.log("Ctrl + Clicked on an electrical device\n");
						((ElectricalDevice) e).clearAllConnections();
						game.audioManager.play(4);
					} else {
						if (e.getClass() == PowerGenerator.class) {
							if (getPlayer().inventory.getActiveItem() != null 
									&& getPlayer().inventory.getActiveItem().getClass() == Ingredient.class 
									&& ((Ingredient) (getPlayer().inventory.getActiveItem())).getItemID() == 12)	{
								FileUtilities.log("Fueled up power generator ");
								int totalPower = 0;
								int quantity = 0;
								boolean fuelSuccessful = false;
								if (game.input.shift.isPressed()) {
									while (((Ingredient) (getPlayer().inventory.getActiveItem())).getQuantity() >= 1) {
										double energy = 2000 + Math.floor(Math.random() * 1000);
										
										fuelSuccessful = ((PowerGenerator) e).insertFuel(energy);
										
										if (fuelSuccessful) {
											((Ingredient) (getPlayer().inventory.getActiveItem())).removeQuantity(1);
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
										((Ingredient) (getPlayer().inventory.getActiveItem())).removeQuantity(1);
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
					if (getPlayer().inventory.getActiveItem() != null 
							&& getPlayer().inventory.getActiveItem().getClass() == Ingredient.class 
							&& ((Ingredient) (getPlayer().inventory.getActiveItem())).getItemID() == 12)	{
						FileUtilities.log("Fueled up stone furnace with ");
						int coal = 0;
						if (!game.input.shift.isPressed()) {
							coal = 1;
							if (!((StoneFurnace) e).fuel()) coal = 0;
						} else {
							for(int i = 0; i <= ((Ingredient) (getPlayer().inventory.getActiveItem())).getQuantity(); i++) {
								if (!((StoneFurnace) e).fuel()) break;
								coal++;
							}
						}
						FileUtilities.log(coal + " coal\n");
						if (coal > 0) ((Ingredient) (getPlayer().inventory.getActiveItem())).removeQuantity(coal);
					} else if (getPlayer().inventory.getActiveItem() != null 
							&& getPlayer().inventory.getActiveItem().getClass() == InventoryTile.class 
							&& (((InventoryTile) (getPlayer().inventory.getActiveItem())).getTileID() == Tile.IRON_ORE.getId()
							|| ((InventoryTile) (getPlayer().inventory.getActiveItem())).getTileID() == Tile.COPPER_ORE.getId()
							|| ((InventoryTile) (getPlayer().inventory.getActiveItem())).getTileID() == Tile.COBBLESTONE.getId()))	{
						((StoneFurnace) e).setStoredItem(getPlayer().inventory.getActiveItem(), game.input.shift.isPressed());
					}
				}
			}
			
			if (e != null && ProductionDevice.class.isAssignableFrom(e.getClass())) {
				if (Utilities.PhysicsUtilities.checkIntersection(clickX, clickY, (e.x << 5) - 1, (e.y << 5) - 16, 36, 48, true)) {
					FileUtilities.log("Clicked on production device: " + e.toString() + "\n");
					if (game.input.alt.isPressed()) {
						FileUtilities.log("Toggled output direction of production device: " + e.toString() + "\n");
						((ProductionDevice) e).toggleOutputDirection();
					} else if (getPlayer().inventory.getActiveItem() != null && ((ProductionDevice) e).isAcceptableInput(getPlayer().inventory.getActiveItem())) {
						FileUtilities.log("Trying to insert " + getPlayer().inventory.getActiveItem().toString() + " into " + e.toString() + "\n");
						if (!((ProductionDevice) e).tryToPopulateInputWithClonedItems(new InventoryItem[] {getPlayer().inventory.getActiveItem()})) {
							FileUtilities.log("\tInsertion failed");
							System.out.print(" :(");
							FileUtilities.log("\n");
						} else {
							FileUtilities.log("\tInsertion successful");
							System.out.print(" ;)");
							FileUtilities.log("\n");
							System.out.println("Manually added item " + getPlayer().inventory.getActiveItem().toString() + " to production device " + ((ProductionDevice) e).toString());
							getPlayer().inventory.getActiveItem().markedForDeletion = true;
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
								if (getPlayer().inventory.getActiveItem() != null 
										&& getPlayer().inventory.getActiveItem().getClass() == Ingredient.class 
										&& ((Ingredient) (getPlayer().inventory.getActiveItem())).getItemID() == 11
										&& ((Ingredient) (getPlayer().inventory.getActiveItem())).getQuantity() > Math.ceil(wireDistance)) {
									((Ingredient) (getPlayer().inventory.getActiveItem())).removeQuantity((int) Math.ceil(wireDistance));
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
						((StoneFurnace) e).removeItem(getPlayer().inventory, game.input.shift.isPressed());
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
			if ((getPlayer().inventory.getActiveItem() == null || !(getPlayer().inventory.getActiveItem().getClass().getSuperclass() == InventoryTool.class))
					&& tiles[clickX][clickY] >= Tile.CONVEYOR.getId() && tiles[clickX][clickY] <= Tile.CONVEYOR_MIDDLE.getId()) {
				if (conveyorSpeeds[clickX][clickY] == 0) setConveyor(clickX, clickY, 0.5);
				else if (game.input.ctrl.isPressed()) setConveyor(clickX, clickY, 0);
				else flipConveyor(clickX, clickY);
				resetDestructibleTile(clickX, clickY);
				return true;
			} else if (!(getPlayer().inventory.getActiveItem() == null) && ColorSelector.class.isAssignableFrom(getPlayer().inventory.getActiveItem().getClass())) {
				setTileColor(clickX, clickY, ((ColorSelector) getPlayer().inventory.getActiveItem()).getColor().getRGB());
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
		if (Math.pow(Math.abs(getPlayer().x >> 5 - x), 2) + Math.pow(Math.abs(getPlayer().y >> 5 - y), 2) < 32) {
			getPlayer().damage(128);
		}
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
	
	public synchronized void draw(Graphics g, ImageObserver observer) { 
		drawEntities(g, observer);
		
		Graphics2D g2 = (Graphics2D) g;
		
		if (potentialConnection) {
			drawPotentialConnection(g2, floatingElectricalDevice);
		}
		
		for (int i = game.xOffset >> 5; i < (game.xOffset + game.drawResolution.width) >> 5; i++) {
			for (int j = game.yOffset >> 5; j < (game.yOffset + game.drawResolution.height) >> 5; j++) {
				if (activatedOxygenTether(i, j)) {
					int bestXLeft = 1024;
					int bestYLeft = 1024;
					int bestXRight = 1024;
					int bestYRight = 1024;
					boolean connectionLeft = false;
					boolean connectionRight = false;
					
					
					for (int k = -16; k < 0; k++) {
						for (int l = -16; l < 16; l++) {
							if (i + k > 0 && i + k < width && j + l > 0 && j + l < height && activatedOxygenTethers[i + k][j + l]) {
								connectionLeft = true;
								//System.out.println("k " + k + " l " + l);
								if (Math.pow(k, 2) + Math.pow(l, 2) < Math.pow(bestXLeft, 2) + Math.pow(bestYLeft, 2)) {
									bestXLeft = k;
									bestYLeft = l;
								}
							}
						}
					}
					
					for (int k = 1; k < 16; k++) {
						for (int l = -16; l < 16; l++) {
							if (i + k > 0 && i + k < width && j + l > 0 && j + l < height && activatedOxygenTethers[i + k][j + l]) {
								connectionRight = true;
								//System.out.println("k " + k + " l " + l);
								if (Math.pow(k, 2) + Math.pow(l, 2) < Math.pow(bestXRight, 2) + Math.pow(bestYRight, 2)) {
									bestXRight = k;
									bestYRight = l;
								}
							}
						}
					}
					
					if (connectionLeft) {
						g2.setStroke(new BasicStroke(5));
						g2.setColor(Color.CYAN);
						g2.drawLine((i << 5) - game.xOffset + 20, (j << 5) - game.yOffset + 4, ((i + bestXLeft) << 5) - game.xOffset + 20, ((j + bestYLeft) << 5) - game.yOffset + 4);
					}
					if (connectionRight) {
						g2.setStroke(new BasicStroke(5));
						g2.setColor(Color.CYAN);
						g2.drawLine((i << 5) - game.xOffset + 20, (j << 5) - game.yOffset + 4, ((i + bestXRight) << 5) - game.xOffset + 20, ((j + bestYRight) << 5) - game.yOffset + 4);
					}
				}				
			}
		}
	}
	
	public void drawPotentialConnection(Graphics2D g2, ElectricalDevice d) {
		if (d == null) return;
		
		double wireDistance = Math.sqrt((d.getLevelX() - currentMouseXOnPanel) * (d.getLevelX() - currentMouseXOnPanel)
				+ (d.getLevelY() - currentMouseYOnPanel) * (d.getLevelY() - currentMouseYOnPanel));
		
		if (getPlayer().inventory.getActiveItem() != null 
				&& getPlayer().inventory.getActiveItem().getClass() == Ingredient.class 
				&& ((Ingredient) (getPlayer().inventory.getActiveItem())).getItemID() == 11
				&& ((Ingredient) (getPlayer().inventory.getActiveItem())).getQuantity() > Math.ceil(wireDistance / 32)) g2.setColor(Color.GREEN);
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
			e.draw(g);
			
			if (e.getClass() == OxygenGenerator.class) {
				for (int i = -8; i <= 8; i++) {
					for (int j = -8; j <= 8; j++) {
						if (e.x + i > 0 && e.x + i < width && e.y + j > 0 && e.y + j < height && getTile(e.x + i, e.y + j).getId() == Tile.OXYGEN_TETHER.getId()) {
							Graphics2D g2 = (Graphics2D) g;
							g2.setStroke(new BasicStroke(5));
							g2.setColor(Color.CYAN);
							g2.drawLine((e.x << 5) - game.xOffset + 20, (e.y << 5) - game.yOffset + 4, ((e.x + i) << 5) - game.xOffset + 16, ((e.y + j) << 5) - game.yOffset + 8);
						}
					}
				}
			}
		}
	}
	
	public synchronized void addEntity(Entity entity) {
		if (entity.getClass() == Player.class) player = (Player) entity;
		this.getEntities().add(entity);
	}
	
	public void destructible(int targetX, int targetY, double damage) {
		Tile t = getTile(targetX, targetY);
		if (t.getId() != 8191 && t.getId() > 2 && (t.getClass() == DestructibleTile.class || t.getClass() == BackgroundDestructibleTile.class || t.getClass() == Platform.class)) {
			double initialDurability = getDurability(targetX, targetY);
			setDurability(targetX, targetY, initialDurability - damage);
			player.queuePlayerModelUpdate();
			player.queueMeleeImage();
			
			// Tile destroyed
			if (getDurability(targetX, targetY) <= 0) {
				/*
				if (tiles[targetX][targetY] == 8) player.inventory.addItem(new InventoryTile(17, 1));
				else if (tiles[targetX][targetY] == 21) player.inventory.addItem(new Ingredient(12, 3 + (int) Math.ceil(8 * Math.random())));
				else player.inventory.addItem(new InventoryTile(tiles[targetX][targetY], 1));
				*/
				
				int tileID = t.getId();
				double vX = Math.random() * 0.2 - 0.1;
				double vY = Math.random() * 0.1 - 0.075;
				PhysicalItem item;
				if (tileID == Tile.STONE.getId()) {
					item = new PhysicalItem(1000 + tileID, this, true, targetX << 5, targetY << 5, vX, vY, new InventoryTile(Tile.COBBLESTONE.getId(), 1));
				} else if (tileID == Tile.COAL_ORE.getId()) {
					item = new PhysicalItem(1000 + 12, this, true, targetX << 5, targetY << 5, vX, vY, new Ingredient(12, 3 + (int) Math.ceil(8 * Math.random())));
				} else {
					item = new PhysicalItem(1000 + tileID, this, true, targetX << 5, targetY << 5, vX, vY, new InventoryTile(tileID, 1));
				}
				queueEntity(item);
				
				player.queuePlayerModelUpdate();
				setTile(targetX, targetY, 2);
				if (game.tracker != null) game.tracker.incrementBasicStat("Tiles Mined");
				queueUpdate = true;
				player.queueMeleeImage(25);
			}
		}
	}
	
	public void setLeftButtonHeld(boolean held) {
		leftButtonHeld = held;
	}

	public Player getPlayer() {
		return player;
	}
}