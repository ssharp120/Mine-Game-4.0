package Structures;
import Tiles.Tile;

public abstract class GeneratedStructure extends Structure {
	public int width;
	public int height;
	public int[][] tiles;
	
	public GeneratedStructure(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new int[width][height];
	}
	
	public int[][] getTiles() {
		return tiles;
	}
	
	public int getTile(int x, int y) {
		return tiles[x][y];
	}
	
	public abstract void populateTiles();
}
