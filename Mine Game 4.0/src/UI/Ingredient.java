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
		if (quantity <= 0) markedForDeletion = true;
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
		Font tileFont = MediaLibrary.getFontFromLibrary("HUDFont").deriveFont((float) (iconHeight / 3));
		g.setFont(tileFont);
		g.setColor(Color.decode("#AFAFAF"));
		if (quantity < 10) {
			FontMetrics metr = new JPanel().getFontMetrics(tileFont);
			g.drawString("" + quantity, metr.stringWidth("0") + x + iconWidth - (3 * offset) - 3, y + iconHeight - (offset) + 4);
		}
		else g.drawString("" + quantity, x + iconWidth - (3 * offset) - 3, y + iconHeight - (offset) + 4);
	}
	
	public String toString() {
		return quantity + " x " + AttributeLibrary.getIngredientNameFromLibrary(getItemID()) + ", ingredient " + getItemID();
	}
}
