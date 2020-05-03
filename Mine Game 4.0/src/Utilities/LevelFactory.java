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
import Tiles.Tile;

public class LevelFactory {
	public static Level constructLevel(int levelIndex, GameLoop game) {
		try {
			String[] levelAttributes = AttributeLibrary.getLevelAttributeFromLibrary(levelIndex).split(",");
			String filePath = levelAttributes[0];
			String name = levelAttributes[1];
			
			int spawnX = Integer.parseInt(levelAttributes[2]);
			int spawnY = Integer.parseInt(levelAttributes[3]);
			
			game.currentWorldValue = Integer.parseInt(levelAttributes[4]);
			
			return new Level(filePath, name, levelIndex, game, spawnX, spawnY);
		} catch (NumberFormatException e) {
			FileUtilities.log("Level values incorrectly formatted for level " + levelIndex + "\n");
			return new Level("AlphaMap.png", "Level loaded incorrectly", levelIndex, game, 128, 128);
		}
	}
	
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
	
	public static void generateLevel(String filepath, int worldType, int levelIndex, GameLoop game) {
		BufferedImage i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		int spawnX = 0;
		int spawnY = 0;
		int world = 0;
		int subWorld = 0;
		if (worldType == 0) {
			i = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
			for (int j = 0; j < i.getWidth(); j++) {
				for (int k = 0; k < i.getHeight(); k++) {
					i.setRGB(j, k, Tile.SKY.getLevelColour());
				}
			}
			
			for (int j = 0; j <= i.getWidth(); j++) {
				for (int k = 0; k <= i.getHeight() - 2; k++) {
					if (j > 5 && j < i.getWidth() - 5) {
						if (k > 512 + 32 - (int) Math.round(Math.random() * 7)) {
							i.setRGB(j, k, Tile.STONE.getLevelColour());
						} else if (k > 512 + 17 && k <= 512 + 72) {
							i.setRGB(j, k, Tile.DIRT.getLevelColour());
						}
						if (k == 512 + 17) i.setRGB(j, k, Tile.GRASS.getLevelColour());
					}
				}
			}
			
			for (int j = 5; j <= i.getWidth() - 5; j++) {
				if (Math.random() < 0.015) {
					int sandWidth = (int) Math.round(10 * Math.random() + 1);
					for (int k = 0; k <= sandWidth; k++) {
						for (int l = 0; l <= (int) Math.round(6 * Math.random() + 1); l++) {
							if (Math.random() > 0.15) i.setRGB(j + k, 512 + 17 + l, Tile.SAND.getLevelColour());
						}
					}
					j += 5;
					continue;
				}
				if (Math.random() < 0.025) {
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
			spawnX = i.getWidth() / 2;
			spawnY = 500 * 32;
			world = 1;
			subWorld = 1;
		}
		
		try {
			ImageIO.write(i, "png", FileUtilities.getFile(filepath));
		} catch (IOException e) {
			FileUtilities.log("Level Generation Failed" + "\n");
		}
		FileUtilities.writeToPosition("levels/index.txt", filepath + ",Generated Level " + world + "-" + subWorld + "," + spawnX + "," + spawnY + "," + world + "," + subWorld + "\n", 0);
	}
}
