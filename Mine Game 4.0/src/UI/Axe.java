package UI;

public class Axe extends InventoryTool {
	public Axe(int imageID, double hardness, double maxDurability, double durability, String name) {
		super(imageID, hardness, maxDurability, durability, name, new int[] {3, 4, 9, 13, 16, 23, 32, 33, 37}, new int[] {22, 26, 27, 28, 29, 30, 34}, new int[] {5, 6, 7, 8, 10, 11, 12, 17, 18, 20, 21, 24, 25, 31, 36});
	}
}
