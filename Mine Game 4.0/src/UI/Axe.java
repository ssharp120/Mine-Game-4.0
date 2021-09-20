package UI;

public class Axe extends InventoryTool {
	public Axe(int imageID, double hardness, double maxDurability, double durability, String name) {
		super(imageID, hardness, maxDurability, durability, name, new int[] {3, 4, 9, 13}, new int[] {26, 27, 28, 29}, new int[] {5, 6, 7, 8, 10, 11, 12, 25});
	}
}
