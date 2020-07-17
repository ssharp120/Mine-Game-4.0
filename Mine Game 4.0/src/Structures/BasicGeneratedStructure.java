package Structures;

import java.awt.image.BufferedImage;

import Tiles.Tile;
import Utilities.FileUtilities;

public class BasicGeneratedStructure extends GeneratedStructure {
	public String imagepath;
	public int startX;
	public int startY;
	
	public BasicGeneratedStructure(String path) {
		super(FileUtilities.loadBufferedImage("structures/" + path + ".png").getWidth(), FileUtilities.loadBufferedImage("structures/" + path + ".png").getHeight());
		imagepath = "structures/" + path + ".png";
		this.width = FileUtilities.loadBufferedImage(imagepath).getWidth();
		this.height = FileUtilities.loadBufferedImage(imagepath).getHeight();
		this.startX = 0;
		this.startY = 0;
		populateTiles();
	}
	
	public BasicGeneratedStructure(String path, int width, int height, int startX, int startY) {
		super(width, height);
		this.startX = startX;
		this.startY = startY;
		imagepath = "structures/" + path + ".png";
		populateTiles();
	}

	public void populateTiles() {
		
		BufferedImage image = FileUtilities.loadBufferedImage(imagepath);		
		int[] tileColours = image.getRGB(0, 0, width, height, null, 0, width);
		
	    for (int y = startY; y < height; y++) {
	        for (int x = startX; x < width; x++) {
	            tileCheck: for (Tile t : Tile.tiles) {
	                if (t != null && t.getLevelColour() == tileColours[x + y * width]) {
	                    tiles[x][y] = t.getId();
	                    break tileCheck;
	                }
	            }
	        }
	    }
	}
}
