package Entities;

import java.awt.Color;
import java.awt.Graphics;

import Frame.Level;
import Libraries.MediaLibrary;

public class Battery extends ElectricalDevice {
	public Battery(Level level, boolean active, int x, int y) {
		super(4, level, active, x, y, 230 * 3600, 12, 300, 2, 11, 20); // Capactity: 230 Ah, 12 V, 300 A
	}

	public void tick() {
		for (ElectricalDevice d : connectedDevices) {
			if (d != null && d.getClass() != PowerGenerator.class) {
				d.charge(batteryCurrent);
				drain(batteryCurrent);
			}
		}
	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(7006), x * 32 - level.getGameLoop().xOffset - 2, y * 32 - level.getGameLoop().yOffset - 16, level.getGameLoop());
		
		displayPowerCapacity(g);
		drawConnections(g);
	}

	public boolean checkConflict() {
		return false;
	}

}
