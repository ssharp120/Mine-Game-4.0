package UI;

public class Pickaxe extends InventoryTool {

	public Pickaxe(int imageID, double hardness, double maxDurability, double durability, String name) {
		super(imageID, hardness, maxDurability, durability, name, new int[] {8, 11, 12, 14, 15, 17, 18, 20, 21, 22, 25, 26, 27, 28, 29, 30, 31, 32, 34, 36}, new int[] {10, 13, 16, 19, 33, 37}, new int[] {3, 4, 5, 6, 7, 9, 24});
	}
}
