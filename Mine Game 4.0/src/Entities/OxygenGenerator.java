package Entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Frame.Level;
import Libraries.MediaLibrary;

public class OxygenGenerator extends Entity {
	public OxygenGenerator(Level level, boolean active, int x, int y) {
		super(0, level, active, x, y);
	}

	public void tick() {
		
	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(7002), x * 32 - level.getGameLoop().xOffset, y * 32 - level.getGameLoop().yOffset, level.getGameLoop());
	}

	public boolean checkConflict() {
		return false;
	}

}
