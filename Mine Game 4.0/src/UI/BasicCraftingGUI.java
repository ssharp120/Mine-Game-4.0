package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Libraries.MediaLibrary;
import Libraries.RecipeLibrary;

public class BasicCraftingGUI extends CraftingMenu {
	public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer) {
		g.setColor(Color.BLUE);
		g.drawRect(100, 150, screenWidth - 200, screenHeight - 200);
		g.setColor(Color.GRAY);
		g.fillRect(100, 150, screenWidth - 200, screenHeight - 200);
		g.setColor(Color.BLACK);
		int y = 155;
		int x = 60;
		int itemSize = 128;
		int padding = 8;
		for (int i = 0; i < RecipeLibrary.getFilledRecipeSlots(); i++) {
			g.setFont(MediaLibrary.getFontFromLibrary("Numbering"));
			g.setColor(new Color((int) Math.abs(Math.round(254 * Math.sin(i/10 + 1))),(int) Math.abs(Math.round(254 * Math.sin(i/10 + 2))),(int) Math.abs(Math.round(254 * Math.sin(i/10 + 2)))));
			g.drawString(i + ".", x += itemSize + padding, y + itemSize/2 + g.getFontMetrics().getAscent()/3);
			for (int j = 0; j < RecipeLibrary.getRecipeFromLibrary(i).getNumInputs(); j++) {
				RecipeLibrary.getRecipeFromLibrary(i).getInput(j).draw(g, x += itemSize + padding, y, itemSize, itemSize, observer);
			}
			g.drawImage(MediaLibrary.getImageFromLibrary(5001), x += itemSize + padding, y, itemSize, itemSize, observer);
			for (int k = 0; k < RecipeLibrary.getRecipeFromLibrary(k).getNumInputs(); k++) {
				RecipeLibrary.getRecipeFromLibrary(i).getOutput(k).draw(g, x += itemSize + padding, y, itemSize, itemSize, observer);
			}
			y += itemSize + padding;
			x = 60;
		}
		
	}
	
	public BasicCraftingGUI() {
		super(new int[] {1});
	}
	
	public void handleClick(int clickX, int clickY) {
		
	}
}
