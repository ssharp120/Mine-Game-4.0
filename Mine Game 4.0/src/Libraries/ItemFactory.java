package Libraries;

import UI.Ingredient;
import UI.InventoryItem;
import UI.InventoryTile;

public class ItemFactory {
	public static InventoryItem createItem(String itemType, int[] attributes) {
		switch (itemType) {
		case "i": if (attributes.length > 1) return new Ingredient(attributes[0], attributes[1]);
		case "b": if (attributes.length > 1) return new InventoryTile(attributes[0], attributes[1]);
		}
		
		return null;
	}
}
