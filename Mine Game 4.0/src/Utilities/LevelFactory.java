package Utilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import org.w3c.dom.html.HTMLImageElement;

import Libraries.AttributeLibrary;
import Libraries.StructureLibrary;
import SingleplayerClient.GameLoop;
import SingleplayerClient.Level;
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
			
			double stoneRoughness = 0.4;
			double dirtRoughness = 0.2;
			double sandRoughness = 0.2;
			
			double biomeDiversity = 0.002;
			
			int currentBiome = 0;
			
			int treeXtoSkip = 0;
			
			double treeOccurrence = 0.8;
			
			int minTreeHeight = 4;
			int maxTreeHeight = 16;
			int minTreeSpacing = 2;
			
			int cactusXtoSkip = 0;
			
			double cactusOccurrence = 0.5;
			
			int minCactusHeight = 2;
			int maxCactusHeight = 6;
			int minCactusSpacing = 5;
			
			double coalDensity = 0.375;
			double[] coalOccurrences = new double[256];
			
			int minCoalWidth = 2;
			int maxCoalWidth = 10;
			int minCoalHeight = 2;
			int maxCoalHeight = 6;
			
			double ironDensity = 0.5;
			double[] ironOccurrences = new double[512];
			
			int minIronWidth = 3;
			int maxIronWidth = 8;
			int minIronHeight = 3;
			int maxIronHeight = 8;
			
			double copperDensity = 0.625;
			double[] copperOccurrences = new double[512];
			
			int minCopperWidth = 6;
			int maxCopperWidth = 9;
			int minCopperHeight = 8;
			int maxCopperHeight = 11;
			
			double gypsumDensity = 0.75;
			double[] gypsumOccurrences = new double[256];
			
			int minGypsumWidth = 8;
			int maxGypsumWidth = 17;
			int minGypsumHeight = 1;
			int maxGypsumHeight = 3;
			
			double caveOccurrence = 0.01;
			double caveLinearity = 0.1;
			double caveDiameterVariety = 0.15;
			int minCaveLength = 200;
			int maxCaveLength = 1000;
			int maxCaveDiameter = 10;
			int minCaveDiameter = 4;
			int minCaves = Math.min(20, Math.max(5, width/100));
			
			// Define initial terrain heights
			stoneHeights[0] = height / 2;
			dirtHeights[0] = 32;
			sandHeights[0] = 32;
			
			// Initialize step-wise delta
			int stoneDelta = 0;
			int dirtDelta = 0;
			int sandDelta = 0;
			
			for (int i = 0; i < width; i++) {
				// Generate stone levels
				stoneDelta = (int) Math.round(2 - 4 * Math.random());
				
				// Smooth out the terrain
				if (Math.random() > stoneRoughness) stoneDelta = 0;
				
				if (i > 0) stoneHeights[i] = stoneHeights[i - 1] + stoneDelta;
				
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
						for (int k = 0; k < 8; k++) {
							for (int j = 1; j < dirtHeights[i - k]; j++) {
								if (Math.random() > (k + 2)/10D) image.setRGB(i - k, height - (stoneHeights[i - k] + j), Tile.SAND.getLevelColour());
							}
						}
					}
					else if (currentBiome == 1) {
						currentBiome = 0;
						dirtHeights[i - 1] = sandHeights[i - 1];
						
						// Add random dirt at biome transition
						for (int k = 0; k < 8; k++) {
							for (int j = 1; j < sandHeights[i - k]; j++) {
								if (Math.random() > (k + 2)/10D) image.setRGB(i - k, height - (stoneHeights[i - k] + j), Tile.DIRT.getLevelColour());
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
					
					if (i > 0) dirtHeights[i] = dirtHeights[i - 1] + dirtDelta;
					
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
							FileUtilities.logLevelGeneration("Generating new " + treeHeight + " tile tall cactus at (" + i + ", " + stoneHeights[i] + dirtHeights[i] + 1 + ")");
							int leafRadius = 2;
							if (treeHeight > maxTreeHeight - 2) leafRadius = 3;
							
							for (int k = -leafRadius; k <= leafRadius; k++) {
								for (int l = -leafRadius; l <= leafRadius; l++) {
									if (!((k == -leafRadius || k == leafRadius) && (l == -leafRadius || l == leafRadius))) {
										if (image.getRGB(i + k, height - (stoneHeights[i] + dirtHeights[i] + 1 + treeHeight + l)) == Tile.SKY.getLevelColour()) {
											image.setRGB(i + k, height - (stoneHeights[i] + dirtHeights[i] + 1 + treeHeight + l), Tile.LEAVES.getLevelColour());
										}
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
					
					if (i > 0) sandHeights[i] = sandHeights[i - 1] + sandDelta;
					
					if (sandHeights[i] < 0) sandHeights[i] = 0;
					if (stoneHeights[i] + sandHeights[i] >= height - 3) sandHeights[i] = 2;
					
					for (int j = 1; j < sandHeights[i]; j++) {
						image.setRGB(i, height - (stoneHeights[i] + j), Tile.SAND.getLevelColour());
					}
					
					// Generate cacti
					if (i > 5 && i < width - 5 && cactusXtoSkip <= 0) {
						if (Math.random() < cactusOccurrence) {
							int cactusHeight = minCactusHeight + (int) Math.round(Math.random() * (maxCactusHeight - minCactusHeight));
							FileUtilities.logLevelGeneration("Generating new " + cactusHeight + " tile tall cactus at (" + i + ", " + stoneHeights[i] + sandHeights[i] + 1 + ")");
							
							for (int j = 0; j < cactusHeight; j++) {
								image.setRGB(i, height - (stoneHeights[i] + sandHeights[i] + j), Tile.CACTUS.getLevelColour());
							}
						}
						
						cactusXtoSkip = minCactusSpacing;
					} else if (cactusXtoSkip > 0) {
						cactusXtoSkip--;
					}
				
				break;
				}
			
				// Add random stone at the transition
				for (int j = stoneHeights[i]; j < stoneHeights[i] + 8; j++) {
					if (Math.random() > (j - stoneHeights[i] + 2)/10D) image.setRGB(i, height - j, Tile.STONE.getLevelColour());
				}
			}
			
			FileUtilities.logLevelGeneration("Generating ores");
			for (int i = 0; i < 256; i++) {
				coalOccurrences[i] = 0.005 - Math.abs(90 - i) * 0.00005;
			}
			for (int i = 0; i < 512; i++) {
				ironOccurrences[i] = 0.0025 - Math.abs(236 - i) * 0.0000075;
			}
			for (int i = 0; i < 512; i++) {
				copperOccurrences[i] = 0.000875 - Math.abs(236 - i) * 0.0000025;
			}
			for (int i = 0; i < 256; i++) {
				gypsumOccurrences[i] = 0.000625 - Math.abs(54 - i) * 0.0000095;
			}
			
			// Generate ores
			int coalOccurrenceLength = coalOccurrences.length;
			int ironOccurrenceLength = ironOccurrences.length;
			int copperOccurrenceLength = copperOccurrences.length;
			int gypsumOccurrenceLength = gypsumOccurrences.length;
			
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < stoneHeights[i]; j++) {
					// Coal ore
					if (stoneHeights[i] - j < coalOccurrenceLength && Math.random() < coalOccurrences[stoneHeights[i] - j]) {
						int coalWidth = (int) Math.round(minCoalWidth + Math.random() * (maxCoalWidth - minCoalWidth));
						int coalHeight = (int) Math.round(minCoalHeight + Math.random() * (maxCoalHeight - minCoalHeight));
						for (int k = 0; k < coalWidth; k++) {
							for (int l = 0; l < coalHeight; l++) {
								if (i + k < width && height - j + l < height && Math.random() < coalDensity) {
									image.setRGB(i + k, height - j + l, Tile.COAL_ORE.getLevelColour());
								}
							}
						}
					}
					// Iron ore
					if (stoneHeights[i] - j < ironOccurrenceLength && Math.random() < ironOccurrences[stoneHeights[i] - j]) {
						int ironWidth = (int) Math.round(minIronWidth + Math.random() * (maxIronWidth - minIronWidth));
						int ironHeight = (int) Math.round(minIronHeight + Math.random() * (maxIronHeight - minIronHeight));
						for (int k = 0; k < ironWidth; k++) {
							for (int l = 0; l < ironHeight; l++) {
								if (i + k < width && height - j + l < height && Math.random() < ironDensity) {
									image.setRGB(i + k, height - j + l, Tile.IRON_ORE.getLevelColour());
								}
							}
						}
					}
					// Copper ore
					if (stoneHeights[i] - j < copperOccurrenceLength && Math.random() < copperOccurrences[stoneHeights[i] - j]) {
						int copperWidth = (int) Math.round(minCopperWidth + Math.random() * (maxCopperWidth - minCopperWidth));
						int copperHeight = (int) Math.round(minCopperHeight + Math.random() * (maxCopperHeight - minCopperHeight));
						for (int k = 0; k < copperWidth; k++) {
							for (int l = 0; l < copperHeight; l++) {
								if (i + k < width && height - j + l < height && Math.random() < copperDensity) {
									image.setRGB(i + k, height - j + l, Tile.COPPER_ORE.getLevelColour());
								}
							}
						}
					}
					// Gypsum deposits
					if (stoneHeights[i] - j < gypsumOccurrenceLength && Math.random() < gypsumOccurrences[stoneHeights[i] - j]) {
						int gypsumWidth = (int) Math.round(minGypsumWidth + Math.random() * (maxGypsumWidth - minGypsumWidth));
						int gypsumHeight = (int) Math.round(minGypsumHeight + Math.random() * (maxGypsumHeight - minGypsumHeight));
						for (int k = 0; k < gypsumWidth; k++) {
							for (int l = 0; l < gypsumHeight; l++) {
								if (i + k < width && height - j + l < height && Math.random() < gypsumDensity) {
									image.setRGB(i + k, height - j + l, Tile.GYPSUM_DEPOSIT.getLevelColour());
								}
							}
						}
					}
				}
			}
			
			// Generate caves
			int caves = 0;
			while (caves < minCaves) {
				for (int i = 0; i < width; i++) {
					if (Math.random() < caveOccurrence) {
						int depth = (int) Math.round(Math.random() * Math.random() * stoneHeights[i]);
						int length = (int) Math.round(Math.random() * maxCaveLength);
						int diameter = (int) Math.round(Math.max(minCaveDiameter, Math.random() * maxCaveDiameter));
						double slope = Math.random() * 4 - 2;
						
						int lastY = height - stoneHeights[i] + depth;
						
						int landTimer = (int) Math.round(Math.random() * 25) + 10;
						
						for (int k = -length; k < length && k + i > diameter && k + i < width - diameter; k++) {
							boolean encounteredLand = false;
							lastY = lastY + (int) Math.round(slope);
							
							for (int ii = -diameter; ii <= diameter; ii++) {
								for (int jj = -diameter; jj <= diameter; jj++) {
									int x = i + k + ii;
									int y = lastY + jj;
									if ((Math.pow(ii, 2) + Math.pow(jj, 2) < Math.pow(diameter, 2)) 
											&& x > 0 && x < width 
											&& y > 0 && y < height - 2) {
										if (image.getRGB(x, y) == Tile.STONE.getLevelColour()
												|| image.getRGB(x, y) == Tile.COAL_ORE.getLevelColour()
												|| image.getRGB(x, y) == Tile.IRON_ORE.getLevelColour()
												|| image.getRGB(x, y) == Tile.COPPER_ORE.getLevelColour()
												|| image.getRGB(x, y) == Tile.GYPSUM_DEPOSIT.getLevelColour()) {
											image.setRGB(x, y, Tile.SKY.getLevelColour());
										} else if (image.getRGB(x, y) == Tile.DIRT.getLevelColour()
												|| image.getRGB(x, y) == Tile.GRASS.getLevelColour()
												|| image.getRGB(x, y) == Tile.SAND.getLevelColour()) {
											image.setRGB(x, y, Tile.SKY.getLevelColour());
											encounteredLand = true;
										}
									}
								}
							}
							
							if (encounteredLand) landTimer--;
							if (landTimer <= 0) break;
							
							if (Math.random() > caveLinearity) {
								slope += Math.random() * 1 - 0.5;
								if (Math.abs(slope) > diameter / 4) {
									slope = Math.signum(slope) * diameter / 4;
								}
								if (Math.random() < 0.01) {
									slope = -slope;
								}
							}
							
							if (Math.random() < caveDiameterVariety) {
								diameter = Math.round(Math.max(minCaveDiameter, Math.min(maxCaveDiameter, diameter += Math.random() * 2.5 - 1.25)));
							}
							/*if (stoneHeights[i] + depth + (int) Math.round(k * slope) > 0 && stoneHeights[i] + depth + (int) Math.round(k * slope) < height) {
								image.setRGB(i + k, stoneHeights[i] + depth + (int) Math.round(k * slope), Tile.TNT.getLevelColour());
							}*/
						}
						
						if (length > minCaveLength) caves++;
					}
				}
			}
			
			// Generate structures
			if (width > 64 && height > 64) {
				FileUtilities.logLevelGeneration("Initializing structure library");
				StructureLibrary.populateStructureLibrary();
				
				BasicGeneratedStructure crashed_ship = (BasicGeneratedStructure) StructureLibrary.getStructureFromLibrary(3);
				int startX = width / 2 - 16;
				int startY = 23;
				
				int lowestY = 0;
				for (int i = 0; i < crashed_ship.width; i++) {
					for (int j = 23; j < height; j++) {
						if (!(image.getRGB(startX + i, j) == Tile.SKY.getLevelColour() 
								|| image.getRGB(startX + i, j) == Tile.NATURAL_WOOD.getLevelColour()
								|| image.getRGB(startX + i, j) == Tile.LEAVES.getLevelColour())) {
							
							if (j - crashed_ship.height + 2 > lowestY) lowestY = j - crashed_ship.height + 2;
							break;
						}
					}
				}
				
				if (lowestY > 0) startY = lowestY;
				else startY = height / 2;
				
				for (int i = 0; i < crashed_ship.width; i++) {
					for (int j = 0; j < crashed_ship.height; j++) {
						if (crashed_ship.getTile(i, j) > 1 && crashed_ship.getTile(i, j) < 8000 && startX + i < width && startY + j < height) image.setRGB(startX + i, startY + j, Tile.tiles[crashed_ship.getTile(i, j)].getLevelColour());
					}
				}
				
				FileUtilities.logLevelGeneration("\tCrashed ship generated at:");
				FileUtilities.logLevelGeneration("\t\tx = "  + startX);
				FileUtilities.logLevelGeneration("\t\t and");
				FileUtilities.logLevelGeneration("\t\ty = "  + startY);
				
				
				
			}
			
			// Determine cave wall start height
			for (int i = 0; i < width; i++) {
				image.setRGB(i, height - stoneHeights[i], Tile.CAVE_WALL.getLevelColour());
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
			
			int terrainHeightsLength = terrainHeights.length;
			
			FileUtilities.writeToPosition(levelHorizonPath, terrainHeightsLength, 0);
			for (int j = 1; j <= terrainHeightsLength; j++) {
				FileUtilities.writeToPosition(levelHorizonPath, terrainHeights[j - 1], j*4);
			}
		}
		
		FileUtilities.log("Level generation complete\n");
		
		return i;
	}
}
