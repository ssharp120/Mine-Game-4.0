package UI;

import Entities.Entity;
import Entities.OxygenGenerator;
import Frame.Level;

public class InventoryEntity extends InventoryItem {
	public int entityIndex;
	
	public InventoryEntity(int entityIndex, int itemIndex) {
		super(0, 2);
		switch (entityIndex) {
			case 0:  this.setImageID(7002);
					break;
		}
		this.entityIndex = entityIndex;
	}
	
	public Entity generateEntity(Level level, int x, int y) {
		switch (entityIndex) {
			case 0:  return new OxygenGenerator(level, true, x, y);
		}
		return null;
	}
	
	@Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
	public String toString() {
		switch (entityIndex) {
			case 0:  return "Oxygen Generator inventory item";
		}
		return "Null entity inventory item";
	}
}
