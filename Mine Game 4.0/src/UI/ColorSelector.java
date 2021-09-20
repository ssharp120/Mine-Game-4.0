package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import Libraries.MediaLibrary;

public class ColorSelector extends Ingredient {
	private Color color = new Color(255, 0, 0);
	private Color defaultColor = new Color(255, 0, 0);

	public ColorSelector(int imageID, int itemID) {
		super(imageID, itemID);
	}
	
	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public void shiftHue(float delta) {
		if (!(color == null)) {
			//System.out.println(shift);
			adjustColor(delta, 0);
		} else reset();
	}
	
	public void shiftSaturation(float delta) {
		if (!(color == null)) {
			//System.out.println(shift);
			adjustColor(5*delta, 1);
		} else reset();
	}
	
	public void shiftBrightness(float delta) {
		if (!(color == null)) {
			//System.out.println(shift);
			adjustColor(5*delta, 2);
		} else reset();
	}
	
	public void adjustColor(float delta, int index) {
		if (index < 0 || index > 2) return;
		float HSB[] = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), HSB);
		if (index == 0) HSB[index] = HSB[index] + delta % 1F;
		else {
			HSB[index] = HSB[index] + delta;
			if (HSB[index] < 0) HSB[index] = 0;
			if (HSB[index] > 1) HSB[index] = 1;			
		}
		System.out.println(HSB[index]);
		//System.out.println(Color.HSBtoRGB(HSB[0], HSB[1], HSB[2]));
		color = Color.getHSBColor(HSB[0], HSB[1], HSB[2]);
	}
	
	public void reset() {
		if (defaultColor != null) color = defaultColor;
		else defaultColor = new Color(255, 255, 255);
	}
	
	public void draw(Graphics g, int x, int y, int iconWidth, int iconHeight, ImageObserver observer) {
		int offset = iconWidth / 8;
		g.drawImage(MediaLibrary.getImageFromLibrary(imageID), x + offset, y + offset, iconWidth - (2 * offset), iconHeight - (2 * offset), observer);
		
		BufferedImage overlay = MediaLibrary.getBufferedImageFromLibrary(imageID + 1); 
		
		//System.out.println(toString());
		
		for (int i = 0; i < overlay.getWidth(); i++) {
			for (int j = 0; j < overlay.getHeight(); j++) {
				//System.out.println(overlay.getRGB(i, j));
				if (overlay.getRGB(i, j) == 0xFFFFFFFF) {
					overlay.setRGB(i, j, color.getRGB());
					//System.out.println("x: " + i + ", y: " + j);
				}
			}
		}
		g.drawImage(overlay, x + offset, y + offset, iconWidth - (2 * offset), iconHeight - (2 * offset), observer);
	}

	public String toString() {
		return String.format("%d color selector", color.getRGB());
	}
}
