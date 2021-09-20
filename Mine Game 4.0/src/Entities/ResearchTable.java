package Entities;

import Frame.Level;
import UI.Ingredient;
import UI.InventoryItem;
import UI.Recipe;

public class ResearchTable extends ProductionDevice {

	public ResearchTable(Level level, boolean active, int x, int y) {
		super(10, level, active, x, y, 480, 12, 50, 1, 23, 4, 30, 120, createInitialAcceptableRecipes());
		
		/*int entityIndex, Level level, boolean active, int x, int y, double powerStorageCapacity,
		double batteryVoltage, double batteryCurrent, int maxConnectedDevices, int connectionPointOffsetX,
		int connectionPointOffsetY, double productionEnergyUsage, Recipe[] acceptableRecipes*/
	}
	
	public static Recipe[] createInitialAcceptableRecipes() {
		Recipe[] initialRecipes = new Recipe[4];
		initialRecipes[0] = new Recipe(0, new InventoryItem[] {new Ingredient(19, 1)}, new InventoryItem[] {new Ingredient(20, 1)});
		return initialRecipes;
	}

	public String toString() {
		return "Research Table | " + super.toString();
	}
}
