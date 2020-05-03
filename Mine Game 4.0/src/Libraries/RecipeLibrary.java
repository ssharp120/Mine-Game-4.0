package Libraries;

import java.util.Scanner;

import UI.Ingredient;
import UI.InventoryItem;
import UI.Recipe;
import Utilities.FileUtilities;

public class RecipeLibrary {
	private static Recipe[] recipeLibrary = new Recipe[16384];
	
	public static Recipe getRecipeFromLibrary(int recipeIndex) {
		return recipeLibrary[recipeIndex];
	}
	
	public static void setRecipe(int recipeIndex, Recipe recipe) {
		recipeLibrary[recipeIndex] = recipe;
	}
	
	public static int getFilledRecipeSlots() {
		int slots = 0;
		for (Recipe r : recipeLibrary) {
			if (r == null) return slots;
			slots++;
		}
		return slots;
	}
	
	public static void populateRecipeLibrary() {
		Scanner recipeFile = FileUtilities.getFileInternal("recipes.txt");
		FileUtilities.log("Loading recipes..." + "\n");
		try {
			int numInputs, numOutputs;
			InventoryItem[] inputs, outputs;
			int[] inputIDs, outputIDs;
			int libraryIndex = 0;
			while (recipeFile.hasNextLine()) {
				// Set delimiter to be a period, comma, space or a combination //
				recipeFile.useDelimiter("[., ]+");
				
				// Read item array lengths to determine how many inputs and outputs the Recipe has //
				if (recipeFile.hasNextInt()) numInputs = recipeFile.nextInt();
				else {FileUtilities.log("Malformed recipe format:\n \t Unable to read input item array length \n \t Aborting recipe library population..." + "\n"); return;}
				if (recipeFile.hasNextInt()) numOutputs = recipeFile.nextInt();
				else {FileUtilities.log("Malformed recipe format:\n \t Unable to read output item array length \n \t Aborting recipe library population..." + "\n"); return;}
				inputs = new InventoryItem[numInputs];
				outputs = new InventoryItem[numOutputs];
				
				FileUtilities.log("\tRecipe " + (libraryIndex + 1) + ": \n\t\tInputs: " + numInputs + ", Outputs: " + numOutputs + " " + "\n");
				
				// Loop index variables to check how many inputs or outputs have been populated into the arrays already //
				int inputIndex = 0;
				int outputIndex = 0;
				
				while(recipeFile.hasNext() && inputIndex < numInputs) {
					String next = recipeFile.next();
					switch (next) {
						// Default item //
						case "d": break;
						// Ingredient //
						case "i": {
							int[] attributes = new int[2];
							attributes[0] = recipeFile.nextInt();
							attributes[1] = recipeFile.nextInt();
							inputs[inputIndex] = ItemFactory.createItem("i", attributes);
						}
						// InventoryEntity //
						case "e": break;
						// InventoryTile //
						case "b": {
							int[] attributes = new int[2];
							attributes[0] = recipeFile.nextInt();
							attributes[1] = recipeFile.nextInt();
							inputs[inputIndex] = ItemFactory.createItem("b", attributes);
						}
						// InventoryTool //
						case "t": break;
					}
					inputIndex++;
				}
				
				if (inputs[0] != null) for (InventoryItem i : inputs) FileUtilities.log("\t\t\tInput: " + i.toString() + "\n");
				
				while(recipeFile.hasNext() && outputIndex < numOutputs) {
					String next = recipeFile.next();
					switch (next) {
						// Default item //
						case "d": break;
						// Ingredient //
						case "i": {
							int[] attributes = new int[2];
							attributes[0] = recipeFile.nextInt();
							attributes[1] = recipeFile.nextInt();
							outputs[outputIndex] = ItemFactory.createItem("i", attributes);
						}
						// InventoryEntity //
						case "e": break;
						// InventoryTile //
						case "b": {
							int[] attributes = new int[2];
							attributes[0] = recipeFile.nextInt();
							attributes[1] = recipeFile.nextInt();
							outputs[outputIndex] = ItemFactory.createItem("b", attributes);
						};
						// InventoryTool //
						case "t": break;
					}
					outputIndex++;
				}
				
				if (outputs[0] != null) for (InventoryItem o : outputs) FileUtilities.log("\t\t\tOutput: " + o.toString() + "\n");
				Recipe r = new Recipe(libraryIndex, inputs, outputs);
				setRecipe(libraryIndex, r);
					
				recipeFile.findInLine("[/ ]+");
				if (recipeFile.hasNextLine()) FileUtilities.log("\t\t" + recipeFile.nextLine() + "\n");
				else FileUtilities.log("\n\"Successfully\" loaded recipes." + "\n");
				libraryIndex++;
			}
		} catch (Exception e) {
			FileUtilities.log("Error while loading recipes" + "\n");
			e.printStackTrace();
			System.exit(80);
		}
	}
	
}
