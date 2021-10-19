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
		if (width < 16 || height < 16) throw new IndexOutOfBoundsException("Dimensions must be at least 16 x 16: " + width + " x " + height);
		if (worldType < 0) throw new IndexOutOfBoundsException("World type must be a positive integer or zero: " + worldType);
		
		FileUtilities.log("Generating new " + width + " x " + height + "level of type " + worldType + "\n");
		
		// Enable old level generation
		//if (width == 1024 && height == 1024) return generateLevel(worldType);
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		switch (worldType) {
		
		case 0:
			
			populateSkyTiles(image);
			
			int[] stoneHeights = new int[width];
			int[] dirtHeights = new int[width];
			int[] sandHeights = new int[width];
			
			double stoneRoughness = 0.6;
			double dirtRoughness = 0.4;
			double sandRoughness = 0.2;
			
			double biomeDiversity = 0.002;
			
			int currentBiome = 0;
			
			int treeXtoSkip = 0;
			
			double treeOccurrence = 0.3;
			
			int minTreeHeight = 4;
			int maxTreeHeight = 10;
			int minTreeSpacing = 5;
			
			// Define initial terrain heights
			stoneHeights[0] = height / 2;
			dirtHeights[0] = 32;
			sandHeights[0] = 32;
			
			// Initialize step-wise delta
			int stoneDelta = 0;
			int dirtDelta = 0;
			int sandDelta = 0;
			
			for (int i = 1; i < width; i++) {
				// Generate stone levels
				stoneDelta = (int) Math.round(2 - 4 * Math.random());
				
				// Smooth out the terrain
				if (Math.random() > stoneRoughness) stoneDelta = 0;
				
				stoneHeights[i] = stoneHeights[i - 1] + stoneDelta;
				
				if (stoneHeights[i] <= 5) stoneHeights[i] = 5;
				else if (stoneHeights[i] >= height - 5) stoneHeights[i] = height - 5;
				
				for (int j = 1; j <= stoneHeights[i]; j++) {
					image.setRGB(i, height - j, Tile.STONE.getLevelColour());
				}
				
				// Change biomes
				if (i > 8 && i < width - 8 && Math.random() < biomeDiversity) {
					if (currentBiome == 0) {
						currentBiome = 1;
						sandHeights[i - 1] = dirtHeights[i - 1];
						
						// Add random sand at biome transition
						for (int ii = 0; ii < 8; ii++) {
							for (int j = 1; j < dirtHeights[i - ii]; j++) {
								if (Math.random() > (ii + 2)/10D) image.setRGB(i - ii, height - (stoneHeights[i - ii] + j), Tile.SAND.getLevelColour());
							}
						}
					}
					else if (currentBiome == 1) {
						currentBiome = 0;
						dirtHeights[i - 1] = sandHeights[i - 1];
						
						// Add random dirt at biome transition
						for (int ii = 0; ii < 8; ii++) {
							for (int j = 1; j < sandHeights[i - ii]; j++) {
								if (Math.random() > (ii + 2)/10D) image.setRGB(i - ii, height - (stoneHeights[i - ii] + j), Tile.DIRT.getLevelColour());
							}
						}
					}
				}
				
				// Fill in terrain
				switch (currentBiome) {
				case 0:
					
					// Generate dirt levels
					dirtDelta = (int) Math.round(1 - 2 * Math.random());
					
					// Smooth out the terrain
					if (Math.random() > dirtRoughness) dirtDelta = 0;
					
					dirtHeights[i] = dirtHeights[i - 1] + dirtDelta;
					
					if (dirtHeights[i] < 0) dirtHeights[i] = 0;
					if (stoneHeights[i] + dirtHeights[i] >= height - 3) dirtHeights[i] = 2;
					
					for (int j = 1; j < dirtHeights[i]; j++) {
						image.setRGB(i, height - (stoneHeights[i] + j), Tile.DIRT.getLevelColour());
					}
					
					// Add grass
					if (dirtHeights[i] > 1) image.setRGB(i, height - (stoneHeights[i] + dirtHeights[i]), Tile.GRASS.getLevelColour());
					
					// Generate trees
					if (i > 5 && i < width - 5 && treeXtoSkip <= 0) {
						if (Math.random() < treeOccurrence) {
							int treeHeight = minTreeHeight + (int) Math.round(Math.random() * (maxTreeHeight - minTreeHeight));
							int leafRadius = 3;
							if (treeHeight > maxTreeHeight - 2) leafRadius = 4;
							
							for (int ii = -leafRadius; ii < leafRadius; ii++) {
								for (int jj = -leafRadius; jj < leafRadius; jj++) {
									if (!((ii == -leafRadius || ii == leafRadius - 1) && (jj == -leafRadius || jj == leafRadius - 1))) {
										image.setRGB(i + ii, height - (stoneHeights[i] + dirtHeights[i] + 1 + treeHeight + jj), Tile.LEAVES.getLevelColour());
									}
								}
							}
							
							for (int j = 0; j < treeHeight; j++) {
								image.setRGB(i, height - (stoneHeights[i] + dirtHeights[i] + 1 + j), Tile.NATURAL_WOOD.getLevelColour());
							}
						}
						
						treeXtoSkip = minTreeSpacing;
					} else if (treeXtoSkip > 0) {
						treeXtoSkip--;
					}
				
				break;
				
				case 1:
				
					// Generate sand levels
					sandDelta = (int) Math.round(1 - 2 * Math.random());
					
					// Smooth out the terrain
					if (Math.random() > sandRoughness) sandDelta = 0;
					
					sandHeights[i] = sandHeights[i - 1] + sandDelta;
					
					if (sandHeights[i] < 0) sandHeights[i] = 0;
					if (stoneHeights[i] + sandHeights[i] >= height - 3) sandHeights[i] = 2;
					
					for (int j = 1; j < sandHeights[i]; j++) {
						image.setRGB(i, height - (stoneHeights[i] + j), Tile.SAND.getLevelColour());
					}
				
				break;
				}
			
				// Add random stone at the transition
				for (int j = stoneHeights[i]; j < stoneHeights[i] + 8; j++) {
					if (Math.random() > (j - stoneHeights[i] + 2)/10D) image.setRGB(i, height - j, Tile.STONE.getLevelColour());
				}
			
			}
			
		break;
				
		default:
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					image.setRGB(i, j, Tile.SKY.getLevelColour());
				}
				for (int j = image.getHeight()/2; j < image.getHeight(); j++) {
					image.setRGB(i, j, Tile.STONE.getLevelColour());
				}
			}
		}
		
		return image;
	}
	
	public static void populateSkyTiles(BufferedImage image) {
		for (int j = 0; j < image.getWidth(); j++) {
			for (int k = 0; k < image.getHeight(); k++) {
				image.setRGB(j, k, Tile.SKY.getLevelColour());
			}
		}
	}
	
	/**
	 * @deprecated
	 * Old level generation.
	 * Use {@link #generateTiles()} instead. */
	@Deprecated public static BufferedImage generateLevel(int worldType) {
		BufferedImage i = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		FileUtilities.log("Generating new level of type " + worldType + "\n");
		
		StructureLibrary.populateStructureLibrary();
		
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
