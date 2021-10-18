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
	
	public static BufferedImage generateTiles(int worldType, int width, int height) throws IndexOutOfBoundsException {		
		if (width < 1 || height < 1) throw new IndexOutOfBoundsException("Invalid dimensions: " + width + " x " + height);
		if (worldType < 0) throw new IndexOutOfBoundsException("World type must be a positive integer or zero: " + worldType);
		
		FileUtilities.log("Generating new " + width + " x " + height + "level of type " + worldType + "\n");
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		return image;
	}
	
	public static BufferedImage generateLevel(int worldType, GameLoop game) {
		BufferedImage i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		FileUtilities.log("Generating new level of type " + worldType + "\n");
		
		if (worldType == 0) {
			i = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
			for (int j = 0; j < i.getWidth(); j++) {
				for (int k = 0; k < i.getHeight(); k++) {
					i.setRGB(j, k, Tile.SKY.getLevelColour());
				}
			}
			
			int[] terrainHeights = new int[i.getWidth()];
			
			FileUtilities.log("\tFilling in terrain...\n");
			
			// Terrain generation
			int grassHeight = 0;
			int treeXtoSkip = 0;
			for (int j = 0; j < i.getWidth(); j++) {
				FileUtilities.logLevelGeneration("Generating terrain at x = " + j);
				if (j > 64 && Math.random() < 0.20) {
					double multiplier = Math.log(Math.abs(grassHeight));
					if (grassHeight > 10) grassHeight += (int) Math.round(0.6 - Math.random() * 2.2);
					else if (grassHeight < -10) grassHeight += (int) Math.round(1.6 - Math.random() * 2.2);
					else grassHeight += (int) Math.round(1 - Math.random() * 2.2);
				}
				terrainHeights[j] = 512 + 17 + grassHeight;
				FileUtilities.logLevelGeneration("\tTerrain height: " + terrainHeights[j]);
				
				// Base terrain
				for (int k = 0; k <= i.getHeight() - 2; k++) {
					if (j > 24 && j < i.getWidth() - 5) {
						if (k > 512 + 32 - (int) Math.round(Math.random() * 7)) {
							i.setRGB(j, k, Tile.STONE.getLevelColour());
						} else if (k > terrainHeights[j] && k <= 512 + 72) {
							i.setRGB(j, k, Tile.DIRT.getLevelColour());
						}
						if (k == terrainHeights[j]) i.setRGB(j, k, Tile.GRASS.getLevelColour());
					}
				}
				
				// Random terrain features
				if (j > 15 && j <= i.getWidth() - 15) {
					
					// Sand
					if (Math.random() < 0.015 && j > 32) {
						int sandWidth = (int) Math.round(10 * Math.random() + 1);
						for (int k = 0; k <= sandWidth; k++) {
							for (int l = 0; l <= (int) Math.round(6 * Math.random() + 1); l++) {
								if (!(j - k > i.getWidth() || j - k <= 0 || terrainHeights[j] + l > i.getHeight())) {
									if (Math.random() > 0.15) i.setRGB(j - k, terrainHeights[j] + l, Tile.SAND.getLevelColour());
								}
							}
						}
						
						FileUtilities.logLevelGeneration("\tSand patch of width " + sandWidth + " generated between:");
						FileUtilities.logLevelGeneration("\t\tx = "  + (j - sandWidth));
						FileUtilities.logLevelGeneration("\t\t and");
						FileUtilities.logLevelGeneration("\t\tx = "  + j);
						FileUtilities.logLevelGeneration("\t\tat height y = " + terrainHeights[j]);
					}
					
					// Iron ore
					if (Math.random() < 0.15) {
						int ironWidth = (int) Math.round(10 * Math.random() + 1);
						int ironHeight = 6;
						int depth = (int) Math.round(256 * Math.random());
						for (int k = 0; k <= ironWidth; k++) {
							for (int l = 0; l <= (int) Math.round(ironHeight * Math.random() + 1); l++) {
								if (!(j - k >= i.getWidth() || j - k <= 0 || 512 + 17 + 32 + l + depth >= i.getHeight())) {
									if (Math.random() > 0.15) i.setRGB(j - k, 512 + 17 + 32 + l + depth, Tile.IRON_ORE.getLevelColour());
								}
							}
						}
						
						FileUtilities.logLevelGeneration("\tIron ore patch of width " + ironWidth + " generated between:");
						FileUtilities.logLevelGeneration("\t\tx = "  + (j - ironWidth));
						FileUtilities.logLevelGeneration("\t\t and");
						FileUtilities.logLevelGeneration("\t\tx = "  + j);
						FileUtilities.logLevelGeneration("\t\tat depth y = " + (512 + 17 + 32 + depth));
					}
					
					// Copper ore
					if (Math.random() < 0.05) {
						int copperWidth = (int) Math.round(30 * Math.random() + 1);
						int copperHeight = 4;
						int depth = (int) Math.round(312 * Math.random());
						for (int k = 0; k <= copperWidth; k++) {
							for (int l = 0; l <= (int) Math.round(copperHeight * Math.random() + 1); l++) {
								if (!(j - k >= i.getWidth() || j - k <= 0 || 512 + 17 + 32 + l + depth >= i.getHeight())) {
									if (Math.random() > 0.15) i.setRGB(j - k, 512 + 17 + 32 + l + depth, Tile.COPPER_ORE.getLevelColour());
								}
							}
						}
						
						FileUtilities.logLevelGeneration("\tCopper ore patch of width " + copperWidth + " generated between:");
						FileUtilities.logLevelGeneration("\t\tx = "  + (j - copperWidth));
						FileUtilities.logLevelGeneration("\t\t and");
						FileUtilities.logLevelGeneration("\t\tx = "  + j);
						FileUtilities.logLevelGeneration("\t\tat depth y = " + (512 + 17 + 32 + depth));
					}
					
					// Coal ore
					if (Math.random() < 0.25) {
						int coalWidth = (int) Math.round(8 * Math.random() + 1);
						int coalHeight = 8;
						int depth = (int) Math.round(128 * Math.random());
						for (int k = 0; k <= coalWidth; k++) {
							for (int l = 0; l <= (int) Math.round(coalHeight * Math.random() + 1); l++) {
								if (!(j - k >= i.getWidth() || j - k <= 0 || 512 + 17 + 32 + l + depth >= i.getHeight())) {
									if (Math.random() > 0.25) i.setRGB(j - k, 512 + 17 + 32 + l + depth, Tile.COAL_ORE.getLevelColour());
								}
							}
						}
						
						FileUtilities.logLevelGeneration("\tCoal ore patch of width " + coalWidth + " generated between:");
						FileUtilities.logLevelGeneration("\t\tx = "  + (j - coalWidth));
						FileUtilities.logLevelGeneration("\t\t and");
						FileUtilities.logLevelGeneration("\t\tx = "  + j);
						FileUtilities.logLevelGeneration("\t\tat depth y = " + (512 + 17 + 32 + depth));
					}
					
					// Trees
					if (Math.random() < 0.025 && j > 72 && treeXtoSkip <= 0) {
						int l = (int) Math.round(6 * Math.random() + 4);
						for (int leafX = 0; leafX < 5; leafX++) {
							for (int leafY = 0; leafY < 5; leafY++) {
								if (!((leafX == 0 || leafX == 4) && (leafY == 0 || leafY == 4))) i.setRGB(j - 2 + leafX, 512 + 16 - l - 2 + leafY + grassHeight, Tile.LEAVES.getLevelColour());
							}
						}
						for (int k = 0; k <= l; k++) {
							i.setRGB(j, 512 + 16 - k + grassHeight, Tile.NATURAL_WOOD.getLevelColour());
						}
						treeXtoSkip = 5;
						
						FileUtilities.logLevelGeneration("\tTree generated at:");
						FileUtilities.logLevelGeneration("\t\tx = "  + j);
						FileUtilities.logLevelGeneration("\t\t and");
						FileUtilities.logLevelGeneration("\t\ty = "  + (512 + 16 + grassHeight));
					}
					treeXtoSkip--;
				}
			}
			
			FileUtilities.log("\tGenerating structures...\n");
			
			// Structures
			BasicGeneratedStructure boulder = (BasicGeneratedStructure) StructureLibrary.getStructureFromLibrary(1);
			for (int j = boulder.width + 16; j < i.getWidth() - boulder.width - 16; j++) {
				if (Math.random() < 0.015) {
					int startY = (int) Math.round(6 * Math.random());
					for (int k = 0; k < boulder.width; k++) {
						for (int m = 0; m < boulder.height; m++) {
							if (boulder.getTile(k, m) > 2 && boulder.getTile(k, m) < 8000) i.setRGB(j + k, 512 + 17 - 1 + startY + m, Tile.tiles[boulder.getTile(k, m)].getLevelColour());
						}
					}
					
					FileUtilities.logLevelGeneration("\tBoulder generated at:");
					FileUtilities.logLevelGeneration("\t\tx = "  + j);
					FileUtilities.logLevelGeneration("\t\t and");
					FileUtilities.logLevelGeneration("\t\ty = "  + (512 + 17 - 1 + startY));
				}
			}
			
			BasicGeneratedStructure shrine = (BasicGeneratedStructure) StructureLibrary.getStructureFromLibrary(2);
			int startX = i.getWidth() / 2 + 42 - (int) Math.round(100 * Math.random());
			for (int k = 0; k < shrine.width; k++) {
				for (int m = 0; m < shrine.height; m++) {
					if (shrine.getTile(k, m) > 2 && shrine.getTile(k, m) < 8000) i.setRGB(startX + k, terrainHeights[startX] - 16 + m, Tile.tiles[shrine.getTile(k, m)].getLevelColour());
				}
			}
			
			FileUtilities.logLevelGeneration("\tShrine generated at:");
			FileUtilities.logLevelGeneration("\t\tx = "  + startX);
			FileUtilities.logLevelGeneration("\t\t and");
			FileUtilities.logLevelGeneration("\t\ty = "  + (terrainHeights[startX] - 16));
				
			BasicGeneratedStructure crashed_ship = (BasicGeneratedStructure) StructureLibrary.getStructureFromLibrary(3);
			startX = 0;
			for (int k = 0; k < crashed_ship.width; k++) {
				for (int m = 0; m < crashed_ship.height; m++) {
					if (crashed_ship.getTile(k, m) > 1 && crashed_ship.getTile(k, m) < 8000) i.setRGB(startX + k, 512 - 8 + m, Tile.tiles[crashed_ship.getTile(k, m)].getLevelColour());
				}
			}
			
			FileUtilities.logLevelGeneration("\tCrashed ship generated at:");
			FileUtilities.logLevelGeneration("\t\tx = "  + startX);
			FileUtilities.logLevelGeneration("\t\t and");
			FileUtilities.logLevelGeneration("\t\ty = "  + (512 - 8));
			
			FileUtilities.logLevelGeneration("Saving level horizon");
			String levelHorizonPath = "level_" + FileUtilities.TIMESTAMP_AT_RUNTIME + "_horizon";
			FileUtilities.createFile(levelHorizonPath);
			FileUtilities.writeToPosition(levelHorizonPath, terrainHeights.length, 0);
			for (int j = 1; j <= terrainHeights.length; j++) {
				FileUtilities.writeToPosition(levelHorizonPath, terrainHeights[j - 1], j*4);
			}
		}
		
		FileUtilities.log("Level generation complete\n");
		
		return i;
	}
}
