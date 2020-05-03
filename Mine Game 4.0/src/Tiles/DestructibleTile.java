package Tiles;

public class DestructibleTile extends SolidTile {
	public boolean isCurrentlyDestructible;
	public double baseDurability;
	public double hardness;
	public double durability;
	
	public DestructibleTile(int id, int levelColour, String name, double friction, boolean startsDestructible, double hardness, double durability) {
		super(id, levelColour, name, friction);
		isCurrentlyDestructible = startsDestructible;
		this.hardness = hardness;
		this.baseDurability = durability;
		this.durability = durability;
	}

	public boolean isCurrentlyDestructible() {
		return isCurrentlyDestructible;
	}

	public void setCurrentlyDestructible(boolean isCurrentlyDestructible) {
		this.isCurrentlyDestructible = isCurrentlyDestructible;
	}

	public double getDurability() {
		return durability;
	}

	public void setDurability(double durability) {
		this.durability = durability;
	}
	
	public boolean isDestroyed() {
		return this.durability <= 0;
	}
	
	public boolean damage(double durability) {
		this.durability -= durability;
		if (this.durability <= 0) {
			this.durability = 0;
			return true;
		}
		return false;
	}
	
	public void heal(double durability) {
		this.durability += durability;
	}

	public double getBaseDurability() {
		return baseDurability;
	}

	public double getHardness() {
		return hardness;
	}
}
