package Entities;

import java.awt.Color;
import java.awt.Graphics;

import Libraries.MediaLibrary;
import SingleplayerClient.Level;
import Tiles.Tile;
import UI.ColorSelector;
import UI.Ingredient;
import UI.InventoryItem;
import UI.InventoryTile;
import Utilities.FileUtilities;
import Utilities.PhysicsUtilities;

public class PhysicalItem extends Projectile {
	private InventoryItem item;
	
	public PhysicalItem(int projectileID, Level level, boolean active, int x, int y, InventoryItem item) {
		super(projectileID, level, active, x, y);
		this.item = item;
		this.elasticity = 0.1;
		FileUtilities.log(String.format("Item with %s created at (%d, %d):\n\t%s", getItemClass(), x, y, item.toString()), false);
	}
	
	public PhysicalItem(int projectileID, Level level, boolean active, int x, int y, double initialvX,
			double initialvY, InventoryItem item) {
		super(projectileID, level, active, x, y, initialvX, initialvY);
		this.item = item;
		this.elasticity = 0.1;
		FileUtilities.log(String.format("Item with %s created at (%d, %d):\n\t%s", getItemClass(), x, y, item.toString()), false);
	}

	public void draw(Graphics g) {
		item.draw(g, x - level.getGameLoop().xOffset, y - level.getGameLoop().yOffset, 32, 32, level.getGameLoop());
		
		if (getItemClass() == Ingredient.class && ((Ingredient) item).getQuantity() > 1) {
			g.setColor(Color.WHITE);
			g.setFont(MediaLibrary.getFontFromLibrary("NumberingFont"));
			
			g.drawString("" + ((Ingredient) item).getQuantity(), x - level.getGameLoop().xOffset + 24, y - level.getGameLoop().yOffset + 32);
		} else if (getItemClass() == InventoryTile.class && ((InventoryTile) item).getQuantity() > 1) {
			g.setColor(Color.WHITE);
			g.setFont(MediaLibrary.getFontFromLibrary("NumberingFont"));
			
			g.drawString("" + ((InventoryTile) item).getQuantity(), x - level.getGameLoop().xOffset + 24, y - level.getGameLoop().yOffset + 32);
		}
		//System.out.println("x " + x + " y " + y);
	}
	
	public void tick() {
		super.tick();
		hitboxWidth = MediaLibrary.getImageFromLibrary(item.getImageID()).getWidth(level.getGameLoop());
		hitboxHeight = MediaLibrary.getImageFromLibrary(item.getImageID()).getHeight(level.getGameLoop()) * 7 / 8;
		
		if (PhysicsUtilities.checkIntersection(x + (hitboxWidth / 2), y + (hitboxWidth / 2), level.getGameLoop().player.x, level.getGameLoop().player.y, level.getGameLoop().player.spriteWidth, level.getGameLoop().player.spriteHeight, true)) {
			item.markedForDeletion = false;
			level.getGameLoop().player.inventory.addItem(item);
			this.markedForDeletion = true;
		}
	}
	
	public Class getItemClass() {
		if (item != null) return item.getClass();
		else return Object.class;
	}
	
	public int getItemQuantity() {
		if (getItemClass() == InventoryTile.class) {
			return ((InventoryTile) item).getQuantity();
		} else if (getItemClass() == Ingredient.class) {
			return ((Ingredient) item).getQuantity();
		}
		
		return 0;
	}
	
	public int getItemTileID() {
		if (item != null && getItemClass() == InventoryTile.class) {
			return ((InventoryTile) item).getTileID();
		}
		
		return 0;
	}
	
	public int getItemID() {
		if (item != null) {
			return item.getItemID();
		}
		
		return 0;
	}
	
	public boolean throughFunnel(int originalX, int originalY) {
		x = (x + hitboxWidth / 2) >> 5;
		x = x << 5;
		y = y >> 5;
		y += 2;
		y = y << 5;
		alignSubpixel(x, y);
		
		if (item != null) {
			for (Entity e : level.getEntities()) {
				if (e.getClass() == StoneFurnace.class && PhysicsUtilities.checkIntersection((e.x << 5), (e.y << 5), x, y, hitboxWidth, hitboxHeight, true)) {
					// Coal
					if (getItemClass() == Ingredient.class && getItemID() == 12) {
						boolean full = true;
						
						for (int i = 0; i < getItemQuantity(); i++) {
							if (((StoneFurnace) e).fuel()) full = false;
						}
						markedForDeletion = !full;
						if (full) {
							x = originalX;
							y = originalY;
							alignSubpixel(x, y);
							return false;
						}
					}
					else if (getItemClass() == InventoryTile.class && (getItemTileID() == Tile.IRON_ORE.getId() || getItemTileID() == Tile.COPPER_ORE.getId() || getItemTileID() == Tile.COBBLESTONE.getId())) {
						if (((StoneFurnace) e).addItem(item)) markedForDeletion = true;
						else {
							x = originalX;
							y = originalY;
							alignSubpixel(x, y);
							return false;
						}
					}
					else {
						x = originalX;
						y = originalY;
						alignSubpixel(x, y);
						return false;
					}
				} else if (ProductionDevice.class.isAssignableFrom(e.getClass()) && PhysicsUtilities.checkIntersection((e.x << 5), (e.y << 5), x, y, hitboxWidth, hitboxHeight, true)) {
					//System.out.println(((e.x << 5) + 32) + ", " + ((e.y << 5) + 32) + ":" + x + ", " + y);
					//System.out.println(PhysicsUtilities.checkIntersection((e.x << 5), (e.y << 5), x, y, hitboxWidth, hitboxHeight, true));
					//System.out.println(1);
					if (((ProductionDevice) e).tryToPopulateInputWithClonedItems(new InventoryItem[] {item})) {
						markedForDeletion = true;
						return true;
					} else {
						x = originalX;
						y = originalY;
						alignSubpixel(x, y);
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
