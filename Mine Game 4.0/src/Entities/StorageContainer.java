package Entities;

import java.awt.Graphics;

import Frame.Level;
import Libraries.MediaLibrary;
import UI.InventoryItem;
import Utilities.FileUtilities;

public class StorageContainer extends Entity {
	public InventoryItem[] inventory;
	
	public StorageContainer(Level level, boolean active, int x, int y, int capacity) {
		super(3, level, active, x, y);
		inventory = new InventoryItem[capacity];
	}

	public void tick() {

	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(7003), x * 32 - level.getGameLoop().xOffset, y * 32 - level.getGameLoop().yOffset, level.getGameLoop());
	}
	
	public void addItem(int slot, InventoryItem item) {
		if (slot > 0 && slot < inventory.length) {
			if (inventory[slot] == null) inventory[slot] = item;
			else {
				for (int i = inventory.length - 1; i >= slot; i--) {
					
				}
			}
		} else {
			FileUtilities.log("Error: Index " + slot + " out of bounds at storage container:\n" + toString() + "\n");
		}
	}
	
	public void removeItem(int slot) {
		if (slot > 0 && slot < inventory.length) {
			if (inventory[slot] != null) {
				inventory[slot] = null;
				for (int i = slot; i < inventory.length - 1; i++) {
					if (inventory[i + 1] != null) inventory[i] = inventory[i + 1];
				}
			} else {
				FileUtilities.log("No item to remove!\n");
			}
		} else {
			FileUtilities.log("Error: Index " + slot + " out of bounds at storage container:\n" + toString() + "\n");
		}
	}
	
	public InventoryItem getItem(int slot) {
		if (slot > 0 && slot < inventory.length) {
			return inventory[slot];
		} else {
			FileUtilities.log("Error: Index " + slot + " out of bounds at storage container:\n" + toString() + "\n");
			return null;
		}
	}
	
	public String toString() {
		return "Storage container at " + x + ", " + y + " of capacity " + inventory.length;
	}

	public boolean checkConflict() {

		return false;
	}

}
