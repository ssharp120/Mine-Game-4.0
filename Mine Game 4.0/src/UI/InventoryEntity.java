package UI;

import Entities.*;
import Frame.Level;

public class InventoryEntity extends InventoryItem {
	public int entityIndex;
	
	public InventoryEntity(int entityIndex, int itemIndex) {
		super(0, 2);
		this.entityIndex = entityIndex;
		setImageIDs(entityIndex);
	}
	
	public InventoryEntity(Entity entity) {
		super(0, 2);
		this.entityIndex = entity.getEntityIndex();
		setImageIDs(entityIndex);
	}
	
	public void setImageIDs(int index) {
			switch (index) {
			case 0: setImageID(7002); break;
			case 1: setImageID(7003); break;
			case 2: setImageID(7004); break;
			case 3: setImageID(7005); break;
			case 4: setImageID(7006); break;
			case 5: setImageID(7007); break;
			case 6: setImageID(7008); break;
			case 7: setImageID(8500); break;
			case 8: setImageID(7009); break;
			case 9: setImageID(7700); break;
			case 10: setImageID(7011); break;
		}
	}
	
	public boolean checkGenerationConditions(Level level, int x, int y) {
		if (!(entityIndex == 7) && !(entityIndex == 9) && level != null && x > 0 && x < level.width && y > 0 && y < level.height - 1) {
			if (entityIndex >= 0 && entityIndex <= 8 || entityIndex == 10) {
				return (!level.getTile(x, y).isSolid() && (level.getTile(x, y + 1).isSolid() || level.getTile(x, y - 1).isSolid()));
			} 
		} else if (entityIndex == 7 && level != null) {
			return !level.getTile(x >> 5, y >> 5).isSolid();
		} else if (entityIndex == 9 && level != null) {
			for (int j = 0; j <= 3; j++) {
				if (level.getTile(x, y + j).isSolid() || level.getTile(x + 1, y + j).isSolid()) return false;
			} 
			if (level.getTile(x, y + 4).isSolid() || level.getTile(x + 1, y + 4).isSolid()) return true;
		}
		return false;
	}
	
	public Entity generateEntity(Level level, int x, int y) {
		
		if ((entityIndex > 1 && entityIndex < 6) || (entityIndex > 7 && entityIndex < 9) || (entityIndex > 9 && entityIndex < 11)) level.getGameLoop().audioManager.play(1);
		
		switch (entityIndex) {
			case 0: return new OxygenGenerator(level, true, x, y);
			case 1: return new StorageContainer(level, true, x, y, 10);
			case 2: return new SeismicScanner(level, true, x, y);
			case 3: return new PowerGenerator(level, true, x, y);
			case 4: return new Battery(level, true, x, y);
			case 5: return new PowerDrill(level, true, x, y);
			case 6: return new StoneFurnace(level, true, x, y);
			case 7: return new Projectile(0, level, true, x, y);
			case 8: return new Extruder(level, true, x, y);
			case 9: return new Dummy(level, x << 5, y << 5);
			case 10: return new ResearchTable(level, true, x, y);
		}
		return null;
	}
	
	@Override
    public Object clone() throws CloneNotSupportedException {
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
			case 7: return "Ball inventory item";
			case 8: return "Extruder inventory item";
			case 9: return "Dummy inventory item";
			case 11: return "Research Table inventory item";
		}
		return "Null entity inventory item";
	}
}
