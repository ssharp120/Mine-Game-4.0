package Libraries;

import java.util.Scanner;

import Utilities.FileUtilities;

public class AttributeLibrary {
private static String[] levelLibrary = new String[16384];
	
	public static void populateLevelLibrary() {
		Scanner levelFile = FileUtilities.getFileInternal("levels/index.txt");
		try {
			int i = 0;
			while (levelFile.hasNextLine()) {
				levelLibrary[i] = levelFile.nextLine();
				FileUtilities.log("Level attributes loaded for level " + i + " -");
				for (String s : levelLibrary[i].split(",")) {
					FileUtilities.log(" " + s);
				}
				FileUtilities.log("\n");
				i++;
			}
		} catch (Exception e) {
			FileUtilities.log("Error while loading level attributes" + "\n");
			e.printStackTrace();
			System.exit(2);
		}
	}
	
	public static String getLevelAttributeFromLibrary(int levelIndex) {
		return levelLibrary[levelIndex];
	}
	
	public static int[] ingredientStackSizes = new int[1000];
	public static String[] ingredientNames = new String[1000];
	
	public static void populateIngredientLibraries() {
		FileUtilities.log("Loading ingredients..." + "\n");
		Scanner ingredientFile = FileUtilities.getFileInternal("ingredients.txt");
		try {
			int i = 0;
			while (ingredientFile.hasNextLine()) {
				ingredientNames[i] = ingredientFile.nextLine();
				ingredientStackSizes[i] = ingredientFile.nextInt();
				FileUtilities.log("\tStack size for " + ingredientNames[i] + ", ID " + i + ": " + ingredientStackSizes[i] + "\n");
				if (ingredientFile.hasNextLine()) ingredientFile.nextLine();
				i++;
			}
		} catch (Exception e) {
			FileUtilities.log("Error while loading item stack sizes" + "\n");
			e.printStackTrace();
			System.exit(15);
		}
	}
	
	public static String getIngredientNameFromLibrary(int ingredientIndex) {
		if (ingredientNames[ingredientIndex] == null) return "Null ingredient " + ingredientIndex;
		else return ingredientNames[ingredientIndex];
	}
}
