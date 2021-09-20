package UI;

public class Bow extends InventoryTool {
	
	private int cooldownTicks;
	private int cooldownTime;
	
	public Bow(int imageID, double hardness, double maxDurability, double durability, String name) {
		super(imageID, hardness, maxDurability, durability, name, new int[] {}, new int[] {}, new int[] {3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24});
		cooldownTime = 50;
	}
	
	public int getCooldownTicks() {
		return cooldownTicks;
	}
	
	public void resetCooldown() {
		cooldownTicks = cooldownTime;
	}
	
	public void tick() {
		super.tick();
		
		cooldownTicks--;
		if (cooldownTicks < 0) cooldownTicks = 0;
	}
	
	public boolean isAvailable() {
		return cooldownTicks == 0;
	}
}
