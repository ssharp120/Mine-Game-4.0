package Entities;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import Libraries.MediaLibrary;
import SingleplayerClient.Level;
import Tiles.Tile;
import UI.Ingredient;
import UI.InventoryItem;
import UI.Recipe;
import Utilities.FileUtilities;
import UI.InventoryTile;
import UI.Ingredient;

public class ProductionDevice extends ElectricalDevice {
	public ArrayList<InventoryItem> input = new ArrayList<InventoryItem>();
	public InventoryItem[] output;
	private boolean active;
	private int outputOffsetX = 1;
	private int outputOffsetY = 0;
	private ArrayList<Recipe> acceptableRecipes = new ArrayList<Recipe>();
	private ArrayList<InventoryItem> acceptableInputs = new ArrayList<InventoryItem>();
	private double productionEnergyUsage = 30;
	private double currentEnergyUsage = 0;
	private double productionTime = 60;
	
	public ProductionDevice(int entityIndex, Level level, boolean active, int x, int y, double powerStorageCapacity,
			double batteryVoltage, double batteryCurrent, int maxConnectedDevices, int connectionPointOffsetX,
			int connectionPointOffsetY, double productionEnergyUsage, double productionTime, Recipe[] acceptableRecipes) {
		super(entityIndex, level, active, x, y, powerStorageCapacity, batteryVoltage, batteryCurrent, maxConnectedDevices,
				connectionPointOffsetX, connectionPointOffsetY);
		this.productionEnergyUsage = productionEnergyUsage;
		this.productionTime = productionTime;
		
		for (Recipe r : acceptableRecipes) {
			if (r != null) {
				this.acceptableRecipes.add(r);
				for (int i = 0; i < r.getNumInputs(); i++) {
					boolean alreadyExists = false;
					
					for (InventoryItem existingInput : acceptableInputs) {
						if (r.getInput(i).getClass() == existingInput.getClass()) {
							if (r.getInput(i).getClass() == Ingredient.class && ((Ingredient) r.getInput(i)).getItemID() == ((Ingredient) existingInput).getItemID()) {
								alreadyExists = true;
							} else if (r.getInput(i).getClass() == InventoryTile.class && ((InventoryTile) r.getInput(i)).getTileID() == ((InventoryTile) existingInput).getTileID()) {
								alreadyExists = true;
							} else {
								alreadyExists = false;
							}
						}
					}
					
					if (!alreadyExists) {
						this.acceptableInputs.add(r.getInput(i));
						System.out.println(r.getInput(i).toString());
					}
				}
			}
		}
	}

	public synchronized void tick() {
		currentEnergyUsage = 0;
		active = false;
		
		if (output == null && input != null && input.size() > 0) {
			active = true;
			if (currentPowerStorage > productionEnergyUsage && checkRecipesForInput(input.toArray(new InventoryItem[] {})) && level.getGameLoop().ticks % productionTime == 0) {
				output = getOutput(input.toArray(new InventoryItem[] {}));
				/*for (InventoryItem i : output) {
					System.out.println("output " + i.toString());
				}*/
				
				currentEnergyUsage = productionEnergyUsage;
			}
		}
		
		if (input != null && input.size() > 0 && currentPowerStorage > productionEnergyUsage && checkRecipesForInput(input.toArray(new InventoryItem[] {})) && level.getGameLoop().ticks % productionTime == 0) {
			level.getGameLoop().audioManager.play(5);
		}
		
		currentPowerStorage -= currentEnergyUsage;
		
		outputItems();
		
		/*for (InventoryItem i : input) {
			System.out.println("input " + i.toString());
		}*/
		
		input.removeIf(i -> i.markedForDeletion);
	}
	
	public void toggleOutputDirection() {
		if (outputOffsetX == 0) {
			outputOffsetX = -outputOffsetY;
			outputOffsetY = 0;
		} else {
			outputOffsetY = outputOffsetX;
			outputOffsetX = 0;
		}
	}
	
	public boolean checkRecipesForInput(InventoryItem[] item) {
		for (Recipe r : acceptableRecipes) {
			if (r.checkRecipe(input.toArray(new InventoryItem[] {}))) {
				return true;
			}
		}
		return false;
	}
	
	public InventoryItem[] getOutput(InventoryItem[] items) {
		if (checkRecipesForInput(items)) {
			for (Recipe r : acceptableRecipes) {
				if (r.checkRecipe(items)) {
					InventoryItem[] output = new InventoryItem[r.getNumOutputs()];
					InventoryItem[] inputsToRemove = new InventoryItem[r.getNumInputs()];
					try {
						for (int i = 0; i < r.getNumOutputs(); i++) {
							output[i] = (InventoryItem) r.getOutput(i).clone();
							FileUtilities.log(toString() + " produced " + output[i].toString() + "\n", true);
						}
						for (int i = 0; i < r.getNumInputs(); i++) {
							inputsToRemove[i] = (InventoryItem) r.getInput(i).clone();
						}
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
						return null;
					}
					
					for (InventoryItem item : input) {
						for (int i = 0; i < r.getNumInputs(); i++) {
							if (inputsToRemove[i].getClass() == Ingredient.class && item.getClass() == Ingredient.class) {
								if (((Ingredient) inputsToRemove[i]).getQuantity() <= ((Ingredient) item).getQuantity()) {
									((Ingredient) item).removeQuantity(((Ingredient) inputsToRemove[i]).getQuantity());
									((Ingredient) inputsToRemove[i]).removeQuantity(((Ingredient) inputsToRemove[i]).getQuantity());
									((Ingredient) item).quantityCheck();
									((Ingredient) inputsToRemove[i]).quantityCheck();
								} else {
									((Ingredient) inputsToRemove[i]).removeQuantity(((Ingredient) item).getQuantity());
									((Ingredient) item).removeQuantity(((Ingredient) item).getQuantity());
									((Ingredient) item).quantityCheck();
									((Ingredient) inputsToRemove[i]).quantityCheck();
								}
							} else if (inputsToRemove[i].getClass() == InventoryTile.class && item.getClass() == InventoryTile.class) {
								if (((InventoryTile) inputsToRemove[i]).getQuantity() <= ((InventoryTile) item).getQuantity()) {
									((InventoryTile) item).removeQuantity(((InventoryTile) inputsToRemove[i]).getQuantity());
									((InventoryTile) inputsToRemove[i]).removeQuantity(((InventoryTile) inputsToRemove[i]).getQuantity());
									((InventoryTile) item).quantityCheck();
									((InventoryTile) inputsToRemove[i]).quantityCheck();
								} else {
									((InventoryTile) inputsToRemove[i]).removeQuantity(((InventoryTile) item).getQuantity());
									((InventoryTile) item).removeQuantity(((InventoryTile) item).getQuantity());
									((InventoryTile) item).quantityCheck();
									((InventoryTile) inputsToRemove[i]).quantityCheck();
								}
							} else {

							}
						}
					}
					
					return output;
				}
			}
		}
		return null;
	}
	
	public int getOutputOffsetX() {
		return outputOffsetX;
	}
	
	public int getOutputOffsetY() {
		return outputOffsetY;
	}
	
	public boolean outputItems() {
		try {
			if (output != null && (x + outputOffsetX) > 0 && (x + outputOffsetX) < level.width
					&& (y + outputOffsetY) > 0 && (y + outputOffsetY) < level.height && (level.getTile(x, y + 1).getId() == Tile.FUNNEL.getId() || !level.getTile(x + outputOffsetX, y + outputOffsetY).isSolid())) {
				
				for (InventoryItem i : output) {
					if (i != null && i.getItemID() > 0) {
						level.queueEntity(new PhysicalItem(1000 + i.getItemID(), level, true, (x + outputOffsetX) << 5, (y + outputOffsetY) << 5, (InventoryItem) i.clone()));
					}
				}				
				
				output = null;
				return true;
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void draw(Graphics g) {
		
		int imageIndex = 7000 + entityIndex + 1;
		int activeIndex = imageIndex + 1;
		
		g.drawImage(MediaLibrary.getImageFromLibrary(active ? activeIndex : imageIndex), (x << 5) - level.getGameLoop().xOffset - 2, (y << 5) - level.getGameLoop().yOffset - 16, level.getGameLoop());
		if (level.getGameLoop() != null && level.getGameLoop().input.alt.isPressed()) {
			g.setColor(new Color(37, 44, 248));
			int levelX = (x << 5) - level.getGameLoop().xOffset;
			int levelY = (y << 5) - level.getGameLoop().yOffset;
				
			BufferedImage arrow = MediaLibrary.getBufferedImageFromLibrary(5002);
			
			int arrowOffsetX = 0;
			int arrowOffsetY = 0;
			
			if (outputOffsetX > 0) {
				arrow = MediaLibrary.rotateClockwise90(arrow);
				arrowOffsetX = 8;
			} else if (outputOffsetX < 0) {
				arrow = MediaLibrary.rotateClockwise90(MediaLibrary.rotateClockwise90(MediaLibrary.rotateClockwise90(arrow)));
				arrowOffsetX = -8;
			} else if (outputOffsetY > 0) {
				arrow = MediaLibrary.rotateClockwise90(MediaLibrary.rotateClockwise90(arrow));
				arrowOffsetY = 8;
			} else if (outputOffsetY < 0) {
				// arrow = arrow
				arrowOffsetY = -8;
			}
			
			g.drawImage(arrow, levelX + 8 + arrowOffsetX, levelY + 8 + arrowOffsetY, 16, 16, null);
			
			//g.drawLine(levelX + 16, levelY + 16, levelX + 16 + (outputOffsetX * 16), levelY + 16 + (outputOffsetY * 16));
		}
		
		displayPowerCapacity(g);
		drawConnections(g);
	}

	public boolean checkConflict() {
		return false;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public void toggleActive() {
		active = !active;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public boolean isAcceptableInput(InventoryItem item) {
		if (item == null) return false;
		
		for (InventoryItem i : acceptableInputs) {
			FileUtilities.log(toString() + " is comparing " + i.toString() + " to " + item.toString() + "\n", true);
			if (item.getClass() == Ingredient.class && i.getClass() == Ingredient.class) {
				if (item.getItemID() == i.getItemID()) return true;
			} else if (item.getClass() == InventoryTile.class && i.getClass() == InventoryTile.class) {
				if (((InventoryTile) item).getTileID() == ((InventoryTile) item).getTileID()) return true;
			} else if (item == i) return true;
		}
		
		return false;
	}
	
	public boolean isInputFilled() {
		return !(input == null);
	}
	
	public boolean tryToPopulateInputWithClonedItems(InventoryItem[] items) {
		if (items == null) return false;
		
		boolean addedItems = false;
		
		for (InventoryItem i : items) {
			if (i != null) {
				try {
					if (isAcceptableInput(i)) {
						input.add((InventoryItem) i.clone());
						addedItems = true;
						FileUtilities.log("Added item " + i.toString() + " to production device " + toString() + "\n", true);
					}
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		
		return addedItems;
	}
	
	public String toString() {
		return "Production Device | (" + x + ", " + y + ")";
	}
}
