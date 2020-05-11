package UI;

public class Pickaxe extends InventoryTool {

	public Pickaxe(int imageID, double hardness, double maxDurability, double durability, String name) {
		super(imageID, hardness, maxDurability, durability, name, new int[] {8, 11, 12}, new int[] {10}, new int[] {3, 4, 5, 6, 7, 9});
	}
}
