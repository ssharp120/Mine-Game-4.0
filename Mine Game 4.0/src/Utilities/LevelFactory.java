package Utilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import Frame.GameLoop;
import Frame.Level;
import Libraries.AttributeLibrary;
import Libraries.StructureLibrary;
import Structures.BasicGeneratedStructure;
import Tiles.Tile;

public class LevelFactory {
	public static void saveLevel(String filepath, int worldType, int levelIndex, Level level) {
		FileUtilities.log("Saving level..." + "\n\t");
		BufferedImage i = new BufferedImage(level.width, level.height, BufferedImage.TYPE_INT_RGB);
		for (int j = 0; j < level.width; j++) {
			for (int k = 0; k < level.height; k++) {
				i.setRGB(j, k, level.getTile(j, k).getLevelColour());
			}
		}
		try {
			ImageIO.write(i, "png", FileUtilities.getFile(filepath));
		} catch (IOException e) {
			FileUtilities.log("Level Saving Failed" + "\n");
			e.printStackTrace();
		}
	}
	
	public static BufferedImage generateLevel(int worldType, GameLoop game) {
		BufferedImage i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		
		if (worldType == 0) {
			i = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
			for (int j = 0; j < i.getWidth(); j++) {
				for (int k = 0; k < i.getHeight(); k++) {
					i.setRGB(j, k, Tile.SKY.getLevelColour());
				}
			}
			
			for (int j = 0; j <= i.getWidth(); j++) {
				for (int k = 0; k <= i.getHeight() - 2; k++) {
					if (j > 24 && j < i.getWidth() - 5) {
						if (k > 512 + 32 - (int) Math.round(Math.random() * 7)) {
							i.setRGB(j, k, Tile.STONE.getLevelColour());
						} else if (k > 512 + 17 && k <= 512 + 72) {
							i.setRGB(j, k, Tile.DIRT.getLevelColour());
						}
						if (k == 512 + 17) i.setRGB(j, k, Tile.GRASS.getLevelColour());
					}
				}
			}
			
			BasicGeneratedStructure boulder = (BasicGeneratedStructure) StructureLibrary.getStructureFromLibrary(1);
			for (int j = boulder.width + 16; j < i.getWidth() - boulder.width - 16; j++) {
				if (Math.random() < 0.015) {
					int startY = (int) Math.round(6 * Math.random());
					for (int k = 0; k < boulder.width; k++) {
						for (int m = 0; m < boulder.height; m++) {
							if (boulder.getTile(k, m) > 2 && boulder.getTile(k, m) < 8000) i.setRGB(j + k, 512 + 17 - 1 + startY + m, Tile.tiles[boulder.getTile(k, m)].getLevelColour());
						}
					}
				}
			}
			
			BasicGeneratedStructure shrine = (BasicGeneratedStructure) StructureLibrary.getStructureFromLibrary(2);
			int startX = i.getWidth() / 2 + 42 - (int) Math.round(100 * Math.random());
			for (int k = 0; k < shrine.width; k++) {
				for (int m = 0; m < shrine.height; m++) {
					if (shrine.getTile(k, m) > 2 && shrine.getTile(k, m) < 8000) i.setRGB(startX + k, 512 + 17 - 16 + m, Tile.tiles[shrine.getTile(k, m)].getLevelColour());
				}
			}
				
			BasicGeneratedStructure crashed_ship = (BasicGeneratedStructure) StructureLibrary.getStructureFromLibrary(3);
			startX = 0;
			for (int k = 0; k < crashed_ship.width; k++) {
				for (int m = 0; m < crashed_ship.height; m++) {
					if (crashed_ship.getTile(k, m) > 1 && crashed_ship.getTile(k, m) < 8000) i.setRGB(startX + k, 512 - 8 + m, Tile.tiles[crashed_ship.getTile(k, m)].getLevelColour());
				}
			}
			
			for (int j = 15; j <= i.getWidth() - 15; j++) {
				if (Math.random() < 0.015 && j > 32) {
					int sandWidth = (int) Math.round(10 * Math.random() + 1);
					for (int k = 0; k <= sandWidth; k++) {
						for (int l = 0; l <= (int) Math.round(6 * Math.random() + 1); l++) {
							if (j + k > i.getWidth() || j + k <= 0 || 512 + 17 + l > i.getHeight()) continue;
							if (Math.random() > 0.15) i.setRGB(j + k, 512 + 17 + l, Tile.SAND.getLevelColour());
						}
					}
					j += 5;
					continue;
				}
				if (Math.random() < 0.15) {
					int ironWidth = (int) Math.round(10 * Math.random() + 1);
					int depth = (int) Math.round(100 * Math.random());
					for (int k = 0; k <= ironWidth; k++) {
						for (int l = 0; l <= (int) Math.round(6 * Math.random() + 1); l++) {
							if (j + k >= i.getWidth() || j + k <= 0 || 512 + 17 + 32 + l + depth >= i.getHeight()) continue;
							if (Math.random() > 0.15) i.setRGB(j + k, 512 + 17 + 32 + l + depth, Tile.IRON_ORE.getLevelColour());
						}
					}
					j += 5;
					continue;
				}
				if (Math.random() < 0.025 && j > 72) {
					int l = (int) Math.round(6 * Math.random() + 4);
					for (int leafX = 0; leafX < 5; leafX++) {
						for (int leafY = 0; leafY < 5; leafY++) {
							if (!((leafX == 0 || leafX == 4) && (leafY == 0 || leafY == 4))) i.setRGB(j - 2 + leafX, 512 + 16 - l - 2 + leafY, Tile.LEAVES.getLevelColour());
						}
					}
					for (int k = 0; k <= l; k++) {
						i.setRGB(j, 512 + 16 - k, Tile.NATURAL_WOOD.getLevelColour());
					}
					j += 2;
					continue;
				}
			}
		}
		
		return i;
	}
}
