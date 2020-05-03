package UI;

import Entities.Entity;

public class InventoryEntity extends InventoryItem {
	public Entity entity;
	
	public InventoryEntity(Entity entity) {
		super(entity.dispTexture, 1000);
		this.entity = entity;
	}
}
