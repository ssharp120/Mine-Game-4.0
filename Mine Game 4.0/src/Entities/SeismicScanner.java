package Entities;

import java.awt.Graphics;

import Frame.Level;
import Libraries.MediaLibrary;

public class SeismicScanner extends Entity {
	private int currentScanningLevel = 0;
	
	public SeismicScanner(Level level, boolean active, int x, int y) {
		super(2, level, active, x, y);
		this.currentScanningLevel = level.getHorizon();
	}

	public void tick() {
		if (level != null && level.getGameLoop() != null && level.getGameLoop().ticks % 100 == 0 && currentScanningLevel < level.height && x > 8 && x < level.width - 8) {
			for (int i = -8; i <= 8; i++) {
				level.exploreTile(x + i, currentScanningLevel);
			}
			currentScanningLevel++;
		}
	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(7004), x * 32 - level.getGameLoop().xOffset - 2, y * 32 - level.getGameLoop().yOffset - 16, level.getGameLoop());
	}

	public boolean checkConflict() {
		return false;
	}

}
