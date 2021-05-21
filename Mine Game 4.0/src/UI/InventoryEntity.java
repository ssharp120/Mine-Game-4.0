package UI;

import Entities.*;
import Frame.Level;

public class InventoryEntity extends InventoryItem {
	public int entityIndex;
	
	public InventoryEntity(int entityIndex, int itemIndex) {
		super(0, 2);
		switch (entityIndex) {
			case 0: setImageID(7002); break;
			case 1: setImageID(7003); break;
			case 2: setImageID(7004); break;
			case 3: setImageID(7005); break;
			case 4: setImageID(7006); break;
			case 5: setImageID(7007); break;
			case 6: setImageID(7008); break;
		}
		this.entityIndex = entityIndex;
	}
	
	public InventoryEntity(Entity entity) {
		super(0, 2);
		this.entityIndex = entity.getEntityIndex();
		switch (entityIndex) {
			case 0: setImageID(7002); break;
			case 1: setImageID(7003); break;
			case 2: setImageID(7004); break;
			case 3: setImageID(7005); break;
			case 4: setImageID(7006); break;
			case 5: setImageID(7007); break;
			case 6: setImageID(7008); break;
		}
	}
	
	public boolean checkGenerationConditions(Level level, int x, int y) {
		if (level != null && x > 0 && x < level.width && y > 0 && y < level.height - 1) {
			if (entityIndex >= 0 && entityIndex <= 6) {
				return (level.getTile(x, y + 1).getId() > 2);
			}
		}
		return false;
	}
	
	public Entity generateEntity(Level level, int x, int y) {
		if (x > 0 && x < level.width && y > 0 && y < level.height) {
			switch (entityIndex) {
				case 0: return new OxygenGenerator(level, true, x, y);
				case 1: return new StorageContainer(level, true, x, y, 10);
				case 2: return new SeismicScanner(level, true, x, y);
				case 3: return new PowerGenerator(level, true, x, y);
				case 4: return new Battery(level, true, x, y);
				case 5: return new PowerDrill(level, true, x, y);
				case 6: return new StoneFurnace(level, true, x, y);
			}
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
			case 2: return "Seismic Scanner inventory item";
			case 3: return "Power Generator inventory item";
			case 4: return "Battery inventory item";
			case 5: return "Power Drill inventory item";
			case 6: return "Stone Furnace inventory item";
		}
		return "Null entity inventory item";
	}
}
