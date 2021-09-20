package Entities;

import Frame.Level;
import UI.Ingredient;
import UI.InventoryItem;
import UI.Recipe;

public class Extruder extends ProductionDevice {

	public Extruder(Level level, boolean active, int x, int y) {
		super(8, level, active, x, y, 480, 12, 50, 1, 23, 4, 30, 60, createInitialAcceptableRecipes());
	}
	
	public static Recipe[] createInitialAcceptableRecipes() {
		Recipe[] initialRecipes = new Recipe[4];
		initialRecipes[0] = new Recipe(0, new InventoryItem[] {new Ingredient(1, 1)}, new InventoryItem[] {new Ingredient(13, 4)});
		initialRecipes[1] = new Recipe(0, new InventoryItem[] {new Ingredient(10, 1)}, new InventoryItem[] {new Ingredient(11, 8)});
		initialRecipes[2] = new Recipe(0, new InventoryItem[] {new Ingredient(1, 1)}, new InventoryItem[] {new Ingredient(14, 1)});
		initialRecipes[3] = new Recipe(0, new InventoryItem[] {new Ingredient(10, 1)}, new InventoryItem[] {new Ingredient(15, 1)});
		return initialRecipes;
	}
	
	public String toString() {
		return "Extruder | " + super.toString();
	}
}
