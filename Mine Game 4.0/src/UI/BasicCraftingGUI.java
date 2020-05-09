package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Frame.GameLoop;
import Libraries.MediaLibrary;
import Libraries.RecipeLibrary;

public class BasicCraftingGUI extends CraftingMenu {
	private int scrollY;
	private int[] yValues = new int[100];
	int itemSize = 128;
	int padding = 8;
	GameLoop gameIn;
	
	public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer) {
		g.setColor(Color.BLUE);
		g.drawRect(100, 150, screenWidth - 200, screenHeight - 200);
		g.setColor(Color.GRAY);
		g.fillRect(100, 150, screenWidth - 200, screenHeight - 200);
		g.setColor(Color.BLACK);
		int y = 155;
		int x = 60;
		for (int i = 0; i < RecipeLibrary.getFilledRecipeSlots(); i++) {
			g.setFont(MediaLibrary.getFontFromLibrary("Numbering"));
			g.setColor(new Color((int) Math.abs(Math.round(254 * Math.sin(i/10 + 1))),(int) Math.abs(Math.round(254 * Math.sin(i/10 + 2))),(int) Math.abs(Math.round(254 * Math.sin(i/10 + 2)))));
			
			yValues[i] = y;
			g.setColor(Color.BLACK);
			g.fillRect(60 + itemSize * 4 / 5 + padding *2, y + padding, itemSize, itemSize);
			g.setColor(Color.DARK_GRAY);
			g.fillRect(60 + itemSize * 4 / 5 + padding, y, itemSize, itemSize);
			
			g.drawString(i + ".", x += itemSize + padding, y + itemSize/2 + g.getFontMetrics().getAscent()/3);
			for (int j = 0; j < RecipeLibrary.getRecipeFromLibrary(i).getNumInputs(); j++) {
				RecipeLibrary.getRecipeFromLibrary(i).getInput(j).draw(g, x += itemSize + padding, y, itemSize, itemSize, observer);
			}
			g.drawImage(MediaLibrary.getImageFromLibrary(5001), x += itemSize + padding, y, itemSize, itemSize, observer);
			for (int k = 0; k < RecipeLibrary.getRecipeFromLibrary(k).getNumOutputs(); k++) {
				RecipeLibrary.getRecipeFromLibrary(i).getOutput(k).draw(g, x += itemSize + padding, y, itemSize, itemSize, observer);
			}
			
			y += itemSize + padding;
			x = 60;
		}
	}
	
	public BasicCraftingGUI(GameLoop game) {
		super(new int[] {1});
		gameIn = game;
	}
	
	public void handleClick(int clickX, int clickY) {
		System.out.println("" + clickX + ", " + clickY);
		if (clickX >= 60 + itemSize * 4 / 5 + padding && clickX <= 60 + itemSize * 4 / 5 + padding + itemSize) {
			for (int i = 0; i < RecipeLibrary.getFilledRecipeSlots(); i++) {
				if (clickY >= yValues[i] && clickY <= yValues[i] + itemSize) {
					System.out.println("Item " + i + " button presses");
					InventoryItem[] currentItems = gameIn.player.inventory.getItems();
					System.out.println(RecipeLibrary.getRecipeFromLibrary(i).checkRecipe(currentItems));
					for (int j = 0; j < RecipeLibrary.getRecipeFromLibrary(i).getNumOutputs(); j++) {
						gameIn.player.inventory.addItem(RecipeLibrary.getRecipeFromLibrary(i).getOutput(j));
					}
				}
			}
		}
	}

	public int getScrollY() {
		return scrollY;
	}
}
