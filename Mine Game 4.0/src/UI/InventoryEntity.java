package UI;

import Entities.Entity;
import Entities.OxygenGenerator;
import Entities.StorageContainer;
import Frame.Level;

public class InventoryEntity extends InventoryItem {
	public int entityIndex;
	
	public InventoryEntity(int entityIndex, int itemIndex) {
		super(0, 2);
		switch (entityIndex) {
			case 0: setImageID(7002); break;
			case 1: setImageID(7003); break;
		}
		this.entityIndex = entityIndex;
	}
	
	public Entity generateEntity(Level level, int x, int y) {
		switch (entityIndex) {
			case 0: return new OxygenGenerator(level, true, x, y);
			case 1: return new StorageContainer(level, true, x, y, 10);
		}
		return null;
	}
	
	@Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
	public String toString() {
		switch (entityIndex) {
			case 0: return "Oxygen Generator inventory item";
			case 1: return "Storage Container inventory item";
		}
		return "Null entity inventory item";
	}
}
