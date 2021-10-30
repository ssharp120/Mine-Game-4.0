package Entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Frame.Level;
import Libraries.MediaLibrary;
import Tiles.Tile;

public class OxygenGenerator extends Entity {
	public OxygenGenerator(Level level, boolean active, int x, int y) {
		super(0, level, active, x, y);
	}

	public void tick() {
		for (int i = -8; i <= 8; i++) {
			for (int j = -8; j <= 8; j++) {
				if (level.getTile(x + i, y + j).getId() == Tile.OXYGEN_TETHER.getId()) {
					level.activateOxygenTether(x + i, y + j);
				}
			}
		}
	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(7002), x * 32 - level.getGameLoop().xOffset, y * 32 - level.getGameLoop().yOffset, level.getGameLoop());
	}

	public boolean checkConflict() {
		return false;
	}

}
