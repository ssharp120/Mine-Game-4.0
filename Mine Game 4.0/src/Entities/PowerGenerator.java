package Entities;

import java.awt.Color;
import java.awt.Graphics;

import Frame.Level;
import Libraries.MediaLibrary;
import Utilities.Battery;

public class PowerGenerator extends ElectricalDevice {
	public double power;
	public double fuel;
	public double fuelCapacity = 80000; // Wh
	
	public PowerGenerator(Level level, boolean active, int x, int y) {
		super(3, level, active, x, y, 30 * 3600, 12, 300, 2, 17, 4); // Capactity: 30 Ah, 12 V, 300 A
	}

	public void tick() {
		if (getNumberOfConnectedDevices() == 0 && fuel > 0) {
			charge(3500D / 12); // 3500 W at 12 V = 3000 / 12 A
			removeFuel(3500D / level.getGameLoop().UPS / 3600D);
		} else if (fuel > 0) {
			for (ElectricalDevice d : connectedDevices) {
				if (d != null) d.charge((3500D / 12) / getNumberOfConnectedDevices());
			}
			if (getNumberOfConnectedDevices() == 1) charge(3500D / 12 / 2);
			removeFuel(3500D / level.getGameLoop().UPS / 3600D * getNumberOfConnectedDevices());
		} else {} // Do nothing		
	}
	
	public boolean insertFuel(double fuelAmount) {
		if (fuel + fuelAmount > fuelCapacity || fuelAmount < 0) return false;
		else {
			fuel += fuelAmount;
			return true;
		}
	}
	
	public boolean removeFuel(double fuelAmount) {
		if (fuel == 0) return false;
		else {
			fuel -= fuelAmount;
			if (fuel < 0) fuel = 0;
			return true;
		}
	}

	public void draw(Graphics g) {
		int levelX = x * 32 - level.getGameLoop().xOffset - 2;
		int levelY = y * 32 - level.getGameLoop().yOffset - 16;
		g.drawImage(MediaLibrary.getImageFromLibrary(7005), levelX, levelY, level.getGameLoop());
		
		displayPowerCapacity(g);
		g.drawString("Fuel: " + String.format("%.3f", fuel / 1000) + "/" + (int) fuelCapacity / 1000 + " kWh", x * 32 - level.getGameLoop().xOffset - 32, y * 32 - level.getGameLoop().yOffset - 48 - 8);
		drawConnections(g);
	}

	public boolean checkConflict() {
		return false;
	}
}
