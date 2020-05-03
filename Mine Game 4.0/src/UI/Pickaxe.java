package UI;

public class Pickaxe extends InventoryTool {

	public Pickaxe(int imageID, double hardness, double maxDurability, double durability, String name) {
		super(imageID, hardness, maxDurability, durability, name, new int[] {7, 8}, new int[] {3, 4}, new int[] {9, 10, 11, 12, 13});
	}
}
