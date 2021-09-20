package Libraries;

import Entities.OxygenGenerator;
import Entities.PowerDrill;
import UI.Axe;
import UI.Bow;
import UI.ColorSelector;
import UI.Ingredient;
import UI.InventoryEntity;
import UI.InventoryItem;
import UI.InventoryTile;
import UI.Pickaxe;
import UI.Shovel;
import UI.UtilityTool;

public class ItemFactory {
	public static InventoryItem createItem(String itemType, int[] attributes) {
		switch (itemType) {
		case "i": if (attributes.length > 1) return new Ingredient(attributes[0], attributes[1]);
		case "b": if (attributes.length > 1) return new InventoryTile(attributes[0], attributes[1]);
		case "e": if (attributes.length >= 1 && attributes[0] >= 0) return new InventoryEntity(attributes[0], 2);
		case "c": if (attributes.length > 1) return new ColorSelector(attributes[0], attributes[1]);
		}
		
		return null;
	}
	
	public static InventoryItem createItem(String itemType, int[] attributes, double[] attributesDoubles, String[] attributesStrings) {
		switch (itemType) {
			case "t":{
				if (attributes.length > 1) {
					switch (attributes[0]) {
						case 0: return new Pickaxe(attributes[1], attributesDoubles[0], attributesDoubles[1], attributesDoubles[2], attributesStrings[0]);
						// Pickaxe(int imageID, double hardness, double maxDurability, double durability, String name)
						case 1: return new Axe(attributes[1], attributesDoubles[0], attributesDoubles[1], attributesDoubles[2], attributesStrings[0]);
						case 2: return new Shovel(attributes[1], attributesDoubles[0], attributesDoubles[1], attributesDoubles[2], attributesStrings[0]);
						case 3: return new Bow(attributes[1], attributesDoubles[0], attributesDoubles[1], attributesDoubles[2], attributesStrings[0]);
						case 4: return new UtilityTool(attributes[1], attributesDoubles[0], attributesDoubles[1], attributesDoubles[2], attributesStrings[0]);
					}
				}
			}
		}
		
		return null;
	}
}
