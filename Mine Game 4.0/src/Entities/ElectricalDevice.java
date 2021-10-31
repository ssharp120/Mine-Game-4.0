package Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import Libraries.MediaLibrary;
import SingleplayerClient.Level;
import UI.Ingredient;
import Utilities.Battery;
import Utilities.FileUtilities;

public abstract class ElectricalDevice extends Entity implements Battery {

	public double powerStorageCapacity;
	public double currentPowerStorage; // A * s
	public double batteryVoltage;
	public double batteryCurrent;
	
	private int width = 36;
	private int height = 48;
	
	private int connectionPointOffsetX, connectionPointOffsetY;
	
	private int totalWireLength;
	
	protected ElectricalDevice[] connectedDevices;
	private int maxConnectedDevices;
	
	public ElectricalDevice(int entityIndex, Level level, boolean active, 
			int x, int y, 
			double powerStorageCapacity, double batteryVoltage, double batteryCurrent, int maxConnectedDevices, 
			int connectionPointOffsetX, int connectionPointOffsetY) {
		super(entityIndex, level, active, x, y);
		this.powerStorageCapacity = (powerStorageCapacity < 0 ? 0 : powerStorageCapacity); // Beautiful
		this.batteryVoltage = (batteryVoltage < 0 ? 0 : batteryVoltage);
		this.currentPowerStorage = 0;
		if (maxConnectedDevices > 0) {
			this.maxConnectedDevices = maxConnectedDevices;
			connectedDevices = new ElectricalDevice[maxConnectedDevices];
		} else this.maxConnectedDevices = 0;
		this.connectionPointOffsetX = connectionPointOffsetX;
		this.connectionPointOffsetY = connectionPointOffsetY;
	}
	
	public double getPowerStorageCapacity() {
		return powerStorageCapacity;
	}

	public double getCurrentPowerStored() {
		return currentPowerStorage;
	}
	
	public int getMaxConnectedDevices() {
		return maxConnectedDevices;
	}
	
	public void displayPowerCapacity(Graphics g) {
		g.setColor(Color.YELLOW);
		g.setFont(MediaLibrary.getFontFromLibrary("INFOFont"));
		if (level.getGameLoop().input.alt.isPressed()) {
			g.drawString(String.format("%.3f kWh / %.3f kWh", Math.round(currentPowerStorage / 3600 * 100)  * batteryVoltage / 1000F / 100F, Math.round(powerStorageCapacity / 3600 * 100) * batteryVoltage / 1000F / 100F),  x * 32 - level.getGameLoop().xOffset - 32, y * 32 - level.getGameLoop().yOffset - 32);
		} else g.drawString(String.format("%.3f Ah / %.3f Ah", Math.round(currentPowerStorage / 3600 * 100) / 100F, Math.round(powerStorageCapacity / 3600 * 100) / 100F),  x * 32 - level.getGameLoop().xOffset - 32, y * 32 - level.getGameLoop().yOffset - 32);
	}
	
	public void charge(double current) {
		/*
			Limitation of the charging current is not required under floating condition. During the initial or an equalizing charge, the current should
			be limited to 20% of the Ah rating of the battery.
			https://www.sbsbattery.com/media/PDFs/SBS-VRLA-Battery-Instruction-Manual.pdf
		*/
		
		currentPowerStorage += current / level.getGameLoop().UPS;
		
		if (currentPowerStorage > powerStorageCapacity) {
			currentPowerStorage = powerStorageCapacity;
		}
	}

	public void drain(double current) {
		if (current > batteryCurrent) current = batteryCurrent;
		if (current < 0) return;
		
		currentPowerStorage -= current / level.getGameLoop().UPS;
		
		if (currentPowerStorage < 0) {
			currentPowerStorage = 0;
		}
	}
	
	public boolean connectDevice(ElectricalDevice d, int connectionLength) {
		if (d == null) return false;
		for (int i = 0; i < connectedDevices.length; i++) {
			if (connectedDevices[i] == null) {
				connectedDevices[i] = d;
				totalWireLength += connectionLength;
				return true;
			}
		}
		return false;
	}
	
	public void clearAllConnections() {
		FileUtilities.log("Connections cleared ");
		
		for (int i = 0; i < connectedDevices.length; i++) {
			if (connectedDevices[i] != null) {
				FileUtilities.log("[Connection removed from clearing all connections] ");
				connectedDevices[i].removeConnection(this);
				connectedDevices[i] = null;
			}
		}
		
		level.getGameLoop().player.inventory.addItem(new Ingredient(11, totalWireLength));
		FileUtilities.log("[" + totalWireLength + " wire returned] ");
		totalWireLength = 0;
		FileUtilities.log("\n");
	}
	
	public void removeConnection(ElectricalDevice d) {
		for (int i = 0; i < connectedDevices.length; i++) {
			if (connectedDevices[i] != null && connectedDevices[i].equals(d)) {
				connectedDevices[i] = null;
				FileUtilities.log("[Connection removed from external function call] ");
			}
		}
	}
	
	public int getNumberOfConnectedDevices() {
		int num = 0;
		if (connectedDevices == null) return num;
		for (int i = 0; i < connectedDevices.length; i++) {
			if (connectedDevices[i] != null) num++;
		}
		return num;
	}
	
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public int getConnectionOffsetX() {return connectionPointOffsetX;}
	public int getConnectionOffsetY() {return connectionPointOffsetY;}
	public int getLevelX() {return (x << 5) - level.getGameLoop().xOffset;}
	public int getLevelY() {return (y << 5) - level.getGameLoop().yOffset;}
	
	public void drawConnections(Graphics g) {
		int levelX = (x << 5) - level.getGameLoop().xOffset;
		int levelY = (y << 5) - level.getGameLoop().yOffset;
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(4));
		g2.setColor(Color.YELLOW);
		
		for (int i = 0; i < connectedDevices.length; i++) {
			ElectricalDevice d = connectedDevices[i];
			if (d != null) {
				g2.drawLine(levelX + connectionPointOffsetX, levelY + connectionPointOffsetY, 
						(d.x << 5) - level.getGameLoop().xOffset + d.getConnectionOffsetX() + 2, (d.y << 5) - level.getGameLoop().yOffset + d.getConnectionOffsetY() + 2);
			}
		}
	}
	
	public String toString() {
		return "Electrical Device | (" + x + ", " + y + ")";
	}
	
	public String toString(boolean verbose) {
		if (verbose) {
			String description = toString() + "\n";
			
			description += "\tCurrent Power Storage:" + Math.round(currentPowerStorage / 3600 * 100) + "kWh\n";
			description += "\tCapacity:" + Math.round(powerStorageCapacity / 3600 * 100) + "kWh\n";		
			
			return description;
		} else return toString();
	}
}
