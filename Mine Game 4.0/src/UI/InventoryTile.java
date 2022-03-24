package UI;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import javax.swing.JComponent;
import javax.swing.JPanel;

import Libraries.MediaLibrary;
import Tiles.Tile;

public class InventoryTile extends InventoryItem {
	private int tileID;
	private int quantity;
	
	public InventoryTile(int tileID, int quantity) {
		super(tileID, tileID);
		this.tileID = tileID;
		this.quantity = quantity;
		if (tileID > Tile.CONVEYOR.getId() && tileID <= Tile.CONVEYOR_MIDDLE.getId()) {
			tileID = Tile.CONVEYOR.getId();
		}
	}
	
	public int getTileID() {
		return tileID;
	}
	
	public void quantityCheck() {
		if (quantity <= 0) {
			quantity = 0;
			markedForDeletion = true;
		}
	}
	
	public int getQuantity() {
		quantityCheck();
		return quantity;
	}
	
	public void setQuantity(int i) {
		quantity = i;
		quantityCheck();
	}
	
	public void addQuantity(int i) {
		quantity += i;
		quantityCheck();
	}
	
	public void removeQuantity(int i) {
		quantity -= i;
		quantityCheck();
	}
	
	public void tick() {
		quantityCheck();
		if (tileID > Tile.CONVEYOR.getId() && tileID <= Tile.CONVEYOR_MIDDLE.getId()) {
			tileID = Tile.CONVEYOR.getId();
		}
	}
	
	public void draw(Graphics g, int x, int y, int iconWidth, int iconHeight, ImageObserver observer) {
		int offset = iconWidth / 8;
		g.drawImage(MediaLibrary.getImageFromLibrary(tileID), x + offset, y + offset, iconWidth - (2 * offset), iconHeight - (2 * offset), observer);
		Font tileFont = MediaLibrary.getFontFromLibrary("Item Numbering").deriveFont((float) (iconHeight / 4) + 3);
		FontMetrics metr;
		int spacing = 2;
		if (iconWidth > 100) {
			g.setFont(tileFont);
			g.setColor(new Color(0, 0, 0, 248));
			metr = new JPanel().getFontMetrics(tileFont);
			if (quantity > 1 && quantity < 10) {
				g.drawString("" + quantity, x + iconWidth - (offset) - 10, y + iconHeight - (offset) + 8);
			}
			else if (quantity > 1) {
				g.drawString("" + (int) (quantity / 10), x + iconWidth - (2 * offset) - 7, y + iconHeight - (offset) + 8);
				g.drawString("" + quantity % 10, x + iconWidth - (offset) - 2, y + iconHeight - (offset) + 8);
			}
			spacing = 5;
		}
		
		tileFont = MediaLibrary.getFontFromLibrary("Item Numbering").deriveFont((float) (iconHeight / 4));
		g.setFont(tileFont);
		g.setColor(new Color(248, 248, 248, 232));
		metr = new JPanel().getFontMetrics(tileFont);
		if (quantity > 1 && quantity < 10) {
			g.drawString("" + quantity, x + iconWidth - (offset) - 8, y + iconHeight - (offset) + 8);
		}
		else if (quantity > 1) {
			g.drawString("" + (int) (quantity / 10), x + iconWidth - (2 * offset) - 5, y + iconHeight - (offset) + 8);
			g.drawString("" + quantity % 10, x + iconWidth - (offset) - 5 + spacing, y + iconHeight - (offset) + 8);
		}
	}
	
	public String toString() {
		return quantity + " x " + Tile.tiles[tileID].getName() + ", tile " + getItemID();
	}
}
