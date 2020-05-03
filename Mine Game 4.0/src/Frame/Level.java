package Frame;

import static Utilities.FileUtilities.*;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import Entities.Entity;
import Tiles.BackgroundDestructibleTile;
import Tiles.DestructibleTile;
import Tiles.RandomizedTile;
import Tiles.Tile;
import UI.InventoryTile;
import Utilities.FileUtilities;

public class Level {
	
	protected GameLoop game;
	protected String filePath;
	public String name;
	private int[][] tiles;
	private double[][] durabilities;
	public int width, height;
	public int xOffset, yOffset;
	public int spawnX, spawnY;
	public int index;
	protected BufferedImage image;
	public Sky sky;
	
	private List<Entity> entities = new ArrayList<Entity>();
	
	public Level(String path, String name, int i, GameLoop mg, int x, int y) {
		index = i;
		spawnX = x;
		spawnY = y;
		sky = new Sky("sky1", game);
		this.name = name;
		if (path != null) {
            this.filePath = path;
            this.loadLevelFromFile();
            game = mg;
        }
	}
	
	private void loadLevelFromFile() {
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
		for (Entity e : getEntities()) {
			e.tick();
		}
		entities.removeIf(i -> i.markedForDeletion);
		for (int y = 0; y < height - 1; y++) {
			for (int x = 0; x < width; x++) {
				if (tiles[x][y] == 15 && tiles[x][y+1] == 2 &&  tiles[x + 1][y+1] == 2) {
					tiles[x][y] = 2;
					tiles[x][y - 1] = 2;
					tiles[x + 1][y] = 2;
					tiles[x + 1][y - 1] = 2;
				}
			}
		}
		sky.tick();
	}
	
	public synchronized void draw(Graphics g) { 
		drawEntities(g);
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
	
	public synchronized void drawEntities(Graphics g) {
		for (Entity e : getEntities()) {
			e.draw(g);
		}
	}
	
	public synchronized void addEntity(Entity entity) {
		this.getEntities().add(entity);
	}
	
	public void destructible(int targetX, int targetY, double damage) {
		Tile t = getTile(targetX, targetY);
		if (t.getId() != 8191 && (t.getClass() == DestructibleTile.class || t.getClass() == BackgroundDestructibleTile.class)) {
			double initialDurability = getDurability(targetX, targetY);
			setDurability(targetX, targetY, initialDurability - damage);
			if (getDurability(targetX, targetY) <= 0) {
				game.player.inventory.addItem(new InventoryTile(tiles[targetX][targetY], 1));
				setTile(targetX, targetY, 2);
			}
		}
	}
}
