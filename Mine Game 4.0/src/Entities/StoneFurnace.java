package Entities;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import Libraries.MediaLibrary;
import SingleplayerClient.Level;
import Tiles.Tile;
import UI.Ingredient;
import UI.Inventory;
import UI.InventoryItem;
import UI.InventoryTile;
import Utilities.FileUtilities;

public class StoneFurnace extends Entity {
	private int coal = 0;
	private int coalCapacity = 8;
	private InventoryItem storedItem;
	private int oreCapacity = 8;
	private int imageID = 7008;
	private InventoryItem output;

	public StoneFurnace(Level level, boolean active, int x, int y) {
		super(6, level, active, x, y);
		storedItem = null;
		output = null;
	}

	public void tick() {
		checkState();
		if (storedItem != null && storedItem.getClass() == InventoryTile.class && storedItem.markedForDeletion) storedItem = null; 
		if (output == null && storedItem != null && coal > 0 && level.getGameLoop().ticks % level.getGameLoop().UPS == 0) {
			if (storedItem.getClass() == InventoryTile.class) {
				if (((InventoryTile) storedItem).getTileID() == Tile.IRON_ORE.getId()) {
					output = new Ingredient(1, 1);
					((InventoryTile) storedItem).removeQuantity(1);
					if (((InventoryTile) storedItem).getQuantity() <= 0)  {storedItem = null; coal--; return;}
					if (coal > 0) coal--;
				} else if (((InventoryTile) storedItem).getTileID() == Tile.COPPER_ORE.getId()) {
					output = new Ingredient(10, 1);
					((InventoryTile) storedItem).removeQuantity(1);
					if (((InventoryTile) storedItem).getQuantity() <= 0)  {storedItem = null; coal--; return;}
					if (coal > 0) coal--;
				} else if (((InventoryTile) storedItem).getTileID() == Tile.COBBLESTONE.getId()) {
					output = new InventoryTile(Tile.STONE.getId(), 1);
					((InventoryTile) storedItem).removeQuantity(1);
					if (((InventoryTile) storedItem).getQuantity() <= 0)  {storedItem = null; coal--; return;}
					if (coal > 0) coal--;
				}
			}
		}
		
		try {
			if (output != null && output.getItemID() > 0 && y < level.height) {
				if (level.getTile(x, y + 1).getId() == Tile.FUNNEL.getId() || !level.getTile(x, y + 1).isSolid()) {
					FileUtilities.log(toString() + " produced " + (InventoryItem) output.clone() + " 1 tile below\n", true);
					level.queueEntity(new PhysicalItem(1000 + output.getItemID(), level, true, x << 5, (y + 1) << 5, (InventoryItem) output.clone()));
					output = null;
				} else if (!level.getTile(x - 1, y).isSolid() && !level.getTile(x + 1, y).isSolid()) {
					if (Math.random() > 0.5) {
						FileUtilities.log(toString() + " produced " + (InventoryItem) output.clone() + " 1 tile left\n", true);
						level.queueEntity(new PhysicalItem(1000 + output.getItemID(), level, true, (x - 1) << 5, y << 5, (InventoryItem) output.clone()));
					} else {
						FileUtilities.log(toString() + " produced " + (InventoryItem) output.clone() + " 1 tile right\n", true);
						level.queueEntity(new PhysicalItem(1000 + output.getItemID(), level, true, (x + 1) << 5, y << 5, (InventoryItem) output.clone()));
					}
					output = null;
				} else if (!level.getTile(x + 1, y).isSolid()) {
					FileUtilities.log(toString() + " produced " + (InventoryItem) output.clone() + " 1 tile right\n", true);
					level.queueEntity(new PhysicalItem(1000 + output.getItemID(), level, true, (x + 1) << 5, y << 5, (InventoryItem) output.clone()));
					output = null;
				} else if (!level.getTile(x - 1, y).isSolid()) {
					FileUtilities.log(toString() + " produced " + (InventoryItem) output.clone() + " 1 tile left\n", true);
					level.queueEntity(new PhysicalItem(1000 + output.getItemID(), level, true, (x - 1) << 5, y << 5, (InventoryItem) output.clone()));
					output = null;
				}
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(imageID), (x << 5) - level.getGameLoop().xOffset, (y << 5) - level.getGameLoop().yOffset, level.getGameLoop());
		g.setColor(Color.YELLOW);
		g.setFont(MediaLibrary.getFontFromLibrary("INFOFont"));
		if (level.getGameLoop() != null && level.getGameLoop().shouldDisplayUIs()) {
			if (level.getGameLoop().input.ctrl.isPressed()) drawTextUI((Graphics2D) g);
			else drawGUI((Graphics2D) g);
		}
	}
	
	public void drawTextUI(Graphics2D g) {
		g.drawString("Coal : " + coal + " / " + coalCapacity, (x << 5) - level.getGameLoop().xOffset, (y << 5) - level.getGameLoop().yOffset - 16);
		if (storedItem != null && storedItem.getClass() == InventoryTile.class) {
			if (((InventoryTile) storedItem).getTileID() == Tile.IRON_ORE.getId()) g.drawString("Iron ore: " + ((InventoryTile) storedItem).getQuantity() + " / " + oreCapacity,
					(x << 5) - level.getGameLoop().xOffset, (y << 5) - level.getGameLoop().yOffset - 36);
			if (((InventoryTile) storedItem).getTileID() == Tile.COPPER_ORE.getId()) g.drawString("Copper ore: " + ((InventoryTile) storedItem).getQuantity() + " / " + oreCapacity,
					(x << 5) - level.getGameLoop().xOffset, (y << 5) - level.getGameLoop().yOffset - 36);
		}
	}
	
	public void drawGUI(Graphics2D gA) {
		int fuelX = (x << 5) - level.getGameLoop().xOffset;
		int fuelY = (y << 5) - level.getGameLoop().yOffset - 16;
		int oreX = fuelX;
		int oreY = fuelY - 16;
	    gA.drawImage(MediaLibrary.getImageFromLibrary(5003), fuelX, fuelY, level.getGameLoop());
	    gA.drawImage(MediaLibrary.getBufferedImageFromLibrary(5004).getSubimage(0, 0, (coal < 0) ? 12 : ((coal < 8) ? 14 + 2 * coal : 14 + 2 * 8), 16), fuelX, fuelY, level.getGameLoop());
	    
	    if (storedItem != null && storedItem.getClass() == InventoryTile.class) {
	    	if (((InventoryTile) storedItem).getTileID() == Tile.IRON_ORE.getId()) {
		    	gA.drawImage(MediaLibrary.getImageFromLibrary(5005), oreX, oreY, level.getGameLoop());
		    	int iron = ((InventoryTile) storedItem).getQuantity();
				gA.drawImage(MediaLibrary.getBufferedImageFromLibrary(5006).getSubimage(0, 0, (iron < 0) ? 12 : ((iron < 8) ? 14 + 2 * iron : 14 + 2 * 8), 16), oreX, oreY, level.getGameLoop());
	    	} else if (((InventoryTile) storedItem).getTileID() == Tile.COPPER_ORE.getId()) {
	    		gA.drawImage(MediaLibrary.getImageFromLibrary(5007), oreX, oreY, level.getGameLoop());
	    		int copper = ((InventoryTile) storedItem).getQuantity();
	    		gA.drawImage(MediaLibrary.getBufferedImageFromLibrary(5008).getSubimage(0, 0, (copper < 0) ? 12 : ((copper < 8) ? 14 + 2 * copper : 14 + 2 * 8), 16), oreX, oreY, level.getGameLoop());
	    	} else if (((InventoryTile) storedItem).getTileID() == Tile.COBBLESTONE.getId()) {
	    		gA.drawImage(MediaLibrary.getImageFromLibrary(5009), oreX, oreY, level.getGameLoop());
	    		int stone = ((InventoryTile) storedItem).getQuantity();
	    		gA.drawImage(MediaLibrary.getBufferedImageFromLibrary(5010).getSubimage(0, 0, (stone < 0) ? 12 : ((stone < 8) ? 14 + 2 * stone : 14 + 2 * 8), 16), oreX, oreY, level.getGameLoop());
	    	}
	    }
	}

	public boolean checkConflict() {
		return false;
	}
	
	public InventoryItem getStoredItem() {
		return storedItem;
	}
	
	public void clearStoredItem() {
		storedItem = null;
	}
	
	public boolean setStoredItem(InventoryItem item, boolean stack) {
		if (item.getClass() == InventoryTile.class && (storedItem == null || storedItem.getClass() == InventoryTile.class)
				&& (storedItem == null || ((InventoryTile) storedItem).getTileID() == ((InventoryTile) item).getTileID())) {
			if (stack) {
				while (((InventoryTile) item).getQuantity() > 0 && (storedItem == null || ((InventoryTile) storedItem).getQuantity() < oreCapacity)) {
					if (addItem(item) == false) return false;
				} return true;
			} else {
				return addItem(item);
			}
		}
		return false;
	}
	
	public boolean addItem(InventoryItem item) {
		if (((InventoryTile) item).getTileID() == Tile.IRON_ORE.getId()) {
			if (storedItem == null) {
				storedItem = new InventoryTile(Tile.IRON_ORE.getId(), 1);
				((InventoryTile) item).removeQuantity(1);
				return true;
			} else if (((InventoryTile) storedItem).getTileID() == Tile.IRON_ORE.getId()) {
				if (((InventoryTile) storedItem).getQuantity() >= oreCapacity) return false;
				else {
					((InventoryTile) storedItem).addQuantity(1);
					((InventoryTile) item).removeQuantity(1);
					return true;
				}
			} else {
				return false;
			}
		} else if (((InventoryTile) item).getTileID() == Tile.COPPER_ORE.getId()) {
			if (storedItem == null) {
				storedItem = new InventoryTile(Tile.COPPER_ORE.getId(), 1);
				((InventoryTile) item).removeQuantity(1);
				return true;
			} else if (((InventoryTile) storedItem).getTileID() == Tile.COPPER_ORE.getId()) {
				if (((InventoryTile) storedItem).getQuantity() >= oreCapacity) return false;
				else {
					((InventoryTile) storedItem).addQuantity(1);
					((InventoryTile) item).removeQuantity(1);
					return true;
				}
			} else {
				return false;
			}
		} else if (((InventoryTile) item).getTileID() == Tile.COBBLESTONE.getId()) {
			if (storedItem == null) {
				storedItem = new InventoryTile(Tile.COBBLESTONE.getId(), 1);
				((InventoryTile) item).removeQuantity(1);
				return true;
			} else if (((InventoryTile) storedItem).getTileID() == Tile.COBBLESTONE.getId()) {
				if (((InventoryTile) storedItem).getQuantity() >= oreCapacity) return false;
				else {
					((InventoryTile) storedItem).addQuantity(1);
					((InventoryTile) item).removeQuantity(1);
					return true;
				}
			} else {
				return false;
			}
		} else return false;
	}
	
	public boolean removeItem(Inventory inventory, boolean stack) {
		if (storedItem == null) return false;
		else {
			if (stack) {
				inventory.addItem(new InventoryTile(((InventoryTile) storedItem).getTileID(), ((InventoryTile) storedItem).getQuantity()));
				((InventoryTile) storedItem).removeQuantity(((InventoryTile) storedItem).getQuantity());
			} else {
				inventory.addItem(new InventoryTile(((InventoryTile) storedItem).getTileID(), 1));
				((InventoryTile) storedItem).removeQuantity(1);
			}
			return true;
		} 
	}
	
	public boolean fuel() {
		if (coal >= coalCapacity) return false;
		coal++;
		return true;
	}
	
	public void checkState() {
		if (storedItem != null && storedItem.getClass() == InventoryTile.class) {
			if (((InventoryTile) storedItem).getTileID() == Tile.IRON_ORE.getId()) {
				if (coal > 0) {
					imageID = 15003;
				} else {
					imageID = 15001;
				}
			} else if (((InventoryTile) storedItem).getTileID() == Tile.COPPER_ORE.getId()) {
				if (coal > 0) {
					imageID = 15004;
				} else {
					imageID = 15002;
				}
			} else if (((InventoryTile) storedItem).getTileID() == Tile.COBBLESTONE.getId()) {
				if (coal > 0) {
					imageID = 15006;
				} else {
					imageID = 15005;
				}
			}
		} else {
			if (coal > 0) {
				imageID = 15000;
			} else {
				imageID = 7008;
			}
		}
	}
	
	public String toString() {
		return "Stone Furnace | (" + x + ", " + y + ")";
	}
}
