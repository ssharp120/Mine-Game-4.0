package UI;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.awt.image.BufferedImage;

import Libraries.MediaLibrary;
import Utilities.FileUtilities;

public class InventoryItem implements Cloneable {
	protected int imageID;
	private int itemID;
	public boolean markedForDeletion;
	
	public InventoryItem(int imageID, int itemID) {
		this.imageID = imageID;
		this.itemID = itemID;
	}
	
	public int getImageID() {
		return imageID;
	}
	
	public void setImageID(int i) {
		imageID = i;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
	public void draw(Graphics g, int x, int y, int iconWidth, int iconHeight, ImageObserver observer) {
		int offset = iconWidth / 8;
		g.drawImage(MediaLibrary.getImageFromLibrary(imageID), x + offset, y + offset, iconWidth - (2 * offset), iconHeight - (2 * offset), observer);
	}
	
	public void drawRotated(Graphics2D g, int x, int y, int iconWidth, int iconHeight, double angle, ImageObserver observer) {
		int offset = iconWidth / 8;
		g.drawImage(FileUtilities.rotateImage(MediaLibrary.getBufferedImageFromLibrary(imageID), angle), x + offset, y + offset, iconWidth - (2 * offset), iconHeight - (2 * offset), observer);
	}
	
	public void tick() {
		
	}
	
	public int getItemID() {
		return itemID;
	}
}
