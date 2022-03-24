package UI;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import javax.swing.JPanel;

import Libraries.AttributeLibrary;
import Libraries.MediaLibrary;

public class Ingredient extends InventoryItem {
	public int quantity;
	
	public Ingredient(int ingredientID, int quantity) {
		super(ingredientID + 12000, ingredientID);
		this.quantity = quantity;
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
	}
	
	public void draw(Graphics g, int x, int y, int iconWidth, int iconHeight, ImageObserver observer) {
		int offset = iconWidth / 8;
		g.drawImage(MediaLibrary.getImageFromLibrary(imageID), x + offset, y + offset, iconWidth - (2 * offset), iconHeight - (2 * offset), observer);
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
		return quantity + " x " + AttributeLibrary.getIngredientNameFromLibrary(getItemID()) + ", ingredient " + getItemID();
	}
}
