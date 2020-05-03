package Entities;

import java.awt.Graphics;

import Frame.Level;
import Libraries.MediaLibrary;
import UI.InventoryEntity;

public class Furniture extends Entity {
	public enum FURNITURE_ID {
		WORKBENCH,
		FURNACE,
		CABINET,
		LOCKER;
	}
	FURNITURE_ID id;
	public int texture;
	public int width, height = 1;
	private Level level;
	
	public Furniture(int x, int y, Level level, FURNITURE_ID id) {
		super(level);
		this.x = x;
		this.y = y;
		this.level = level;
		this.id = id;
		switch (this.id) {
			case WORKBENCH: 
				texture = 7001;
				width = 256;
				height = 128;
			break;
			case FURNACE: texture = 7011;
			break;
			case CABINET: texture = 7021;
			break;
			case LOCKER: texture = 7031;
		}
		dispTexture = texture;
	}
	
	public Furniture(int x, int y, Level level, FURNITURE_ID id, boolean active) {
		super(level);
		this.x = x;
		this.y = y;
		this.level = level;
		this.id = id;
		switch (this.id) {
			case WORKBENCH: texture = 7001;
			break;
			case FURNACE: texture = 7011;
			break;
			case CABINET: texture = 7021;
			break;
			case LOCKER: texture = 7031;
		}
		dispTexture = texture;
		this.active = active;
	}

	public void tick() {
		if (checkConflict()) {
			markedForDeletion = true;
			InventoryEntity j = new InventoryEntity(new Furniture(x, y, level, id));
			level.getGameLoop().player.inventory.addItem(j);
		}
	}
	
	public boolean checkConflict() {
		switch (this.id) {
			case WORKBENCH: 
				for (int i = x + 2; i <= x + 5; i++) {
					for (int j = y; j <= y + 3; j++) {
						if (level.getTile(i, j).getId() != 2 && level.getTile(i, j).getId() != 8191) {
							return true;
						}
					}
					if (level.getTile(i, y + 4).getId() == 2 || level.getTile(i, y + 4).getId() == 8191) return true;
				}
			break;
			case FURNACE: texture = 7011;
			break;
			case CABINET: texture = 7021;
			break;
			case LOCKER: texture = 7031;
		}
		return false;
	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(texture), x * 32 - level.getGameLoop().xOffset, y * 32 - level.getGameLoop().yOffset, width, height, level.getGameLoop());
	}

}
