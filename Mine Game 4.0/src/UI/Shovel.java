package UI;

public class Shovel extends InventoryTool {

	public Shovel(int imageID, double hardness, double maxDurability, double durability, String name) {
		super(imageID, hardness, maxDurability, durability, name, new int[] {5, 6, 7}, new int[] {}, new int[] {3, 4, 8, 9, 10, 11, 12, 13});
	}
}
