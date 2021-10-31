package Entities;

import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Libraries.MediaLibrary;
import SingleplayerClient.Level;
import Tiles.Tile;
import UI.InventoryItem;

public class Crop extends Plant {
	public int growthStage;
	public int cropWidth;
	public int cropHeight;
	
	public Crop(Level level, boolean active, int plantIdentifier, int x, int y) {
		super(level, active, plantIdentifier, x, y);
		switch (plantIdentifier) {
			case 0: {
				dispTexture = 7401; 
				growthStage = 1;
				cropWidth = 32;
				cropHeight = 32;
				break;
			}
		}
	}

	public void tick() {
		switch (plantIdentifier) {
			case 0: {
				if (growthStage < 3 && level.getGameLoop().ticks % 2000 == 0 && Math.random() < 0.20) {
					growthStage ++;
					// Move y location up so the height does not extend under the plant
					y -= 32;
				}
				cropHeight = growthStage * 32;
				
				// Remove if there is no sand under the crop
				if (level.getTile(x >> 5, (y >> 5) + growthStage).getId() != Tile.SAND.getId()) {
					this.markedForDeletion = true;
				}
				break;
			}
		}
	}

	public void draw(Graphics g, ImageObserver observer) {
		if (!level.isExplored(x >> 5, y >> 5)) {
			switch (plantIdentifier) {
				case 0: {
					for (int i = 0; i < growthStage; i++) {
						g.drawImage(MediaLibrary.getImageFromLibrary(dispTexture), x - level.getGameLoop().xOffset, y - level.getGameLoop().yOffset + 32 * i, observer);
					}
					break;
				}
			}
		}
	}
	
	public InventoryItem[] returnDroppables() {
		switch (plantIdentifier) {
			case 0: {
				return new InventoryItem[] {
						Libraries.ItemFactory.createItem("i", new int[] {2, (growthStage - 1) * (((int) Math.round(Math.random() * 15)) + 5)}),
						Libraries.ItemFactory.createItem("i", new int[] {3, Math.abs(((int) Math.round((Math.random() - 0.25) * 3))) + growthStage})
				};
			}
		}
		return null;
	}

	public boolean checkConflict() {
		
		return false;
	}

	public void draw(Graphics g) {
		
	}

}
