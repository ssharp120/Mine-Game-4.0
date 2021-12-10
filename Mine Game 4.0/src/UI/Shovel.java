package UI;

public class Shovel extends InventoryTool {

	public Shovel(int imageID, double hardness, double maxDurability, double durability, String name) {
		super(imageID, hardness, maxDurability, durability, name, new int[] {5, 6, 7}, new int[] {22, 23, 32, 33}, new int[] {3, 4, 8, 9, 10, 11, 12, 13, 16, 17, 18, 20, 21, 24, 25, 26, 27, 28, 29, 30, 31, 34, 36, 37});
	}
}
