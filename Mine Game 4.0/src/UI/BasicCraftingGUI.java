package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Libraries.AttributeLibrary;
import Libraries.MediaLibrary;
import Libraries.RecipeLibrary;
import SingleplayerClient.GameLoop;
import SingleplayerClient.InputHandler;

public class BasicCraftingGUI extends CraftingMenu {
	private int scrollY;
	private int scrollMax;
	private int[] yValues = new int[100];
	private InputHandler controls;
	int itemSize = 128;
	int offset = 64;
	int padding = 8;
	int yInitial = 155;
	int xInitial = 90;
	int scrollValue = 10;
	int numItemsAbove = 0;
	int numItemsBelow = 0;
	private boolean freeCrafting;
	GameLoop gameIn;
	
	public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer, InputHandler controls) {
		this.controls = controls;
		InventoryItem[] currentItems = gameIn.player.inventory.getItems();
		
		int targetX = screenWidth - (2 * offset), targetY = screenHeight - (2 * offset);
		if (targetX / 16 < targetY / 9) itemSize = targetX / 16;
		else itemSize = targetY / 9;
		int outline = 6 + itemSize / 6;
		// Start below hotbar
		yInitial = (offset - outline) / 2 + itemSize + outline + padding;		
		
		numItemsAbove = 0;
		numItemsBelow = 0;
		
		if (gameIn != null & gameIn.player != null) {
			gameIn.player.inventory.drawHotbar(g, screenWidth, screenHeight, observer);
			if (gameIn.player.drawInfo) {
				if (scrollMax > screenHeight) {
					g.setColor(Color.CYAN);
					g.drawLine(0, scrollY * (screenHeight - 16) / scrollMax, 100, scrollY * (screenHeight - 16) / scrollMax);
					g.setColor(Color.RED);
					g.drawLine(0, screenHeight - 16, 100, screenHeight - 16);
				} else {
					g.setColor(Color.CYAN);
					g.drawLine(0, scrollY, 100, scrollY);
					g.setColor(Color.RED);
					g.drawLine(0, scrollMax, 100, scrollMax);
				}
				g.setColor(Color.BLACK);
			}
			
			if (gameIn.input.right.isPressed() && !gameIn.input.left.isPressed()) adjustColorSelectors(0.01F);
			else if (gameIn.input.left.isPressed() && !gameIn.input.right.isPressed()) adjustColorSelectors(-0.01F);
		}
		
		int y = yInitial + scrollValue - scrollY;
		int x = xInitial;

		for (int i = 0; i < RecipeLibrary.getFilledRecipeSlots(); i++) {
			g.setFont(MediaLibrary.getFontFromLibrary("Numbering"));
			g.setColor(new Color((int) Math.abs(Math.round(254 * Math.sin(i/10 + 1))),(int) Math.abs(Math.round(254 * Math.sin(i/10 + 2))),(int) Math.abs(Math.round(254 * Math.sin(i/10 + 2)))));
			
			yValues[i] = y;
			if (y >= yInitial && y + itemSize <= screenHeight - 50) {
				g.setColor(Color.BLACK);
				g.fillRect(x += itemSize * 4 / 5 + padding * 2, y + padding, itemSize, itemSize);
				if (freeCrafting) {
					for (int j = 0; j < itemSize; j++) {
						g.setColor(new Color(Color.HSBtoRGB(j/100F, 1, 1)));
						g.fillRect(x - padding + j, y, 1, itemSize);
					}
				}
				else {
					g.setColor(Color.DARK_GRAY);
					g.fillRect(x - padding, y, itemSize, itemSize);
				}
				
				for (int j = 0; j < RecipeLibrary.getRecipeFromLibrary(i).getNumInputs(); j++) {
					RecipeLibrary.getRecipeFromLibrary(i).getInput(j).draw(g, x += itemSize + padding, y, itemSize, itemSize, observer);
				}
				int arrowID = 5000;
				if (freeCrafting || RecipeLibrary.getRecipeFromLibrary(i).checkRecipe(currentItems)) arrowID = 5001;
				g.drawImage(MediaLibrary.getImageFromLibrary(arrowID), x += itemSize + padding, y, itemSize, itemSize, observer);
				for (int k = 0; k < RecipeLibrary.getRecipeFromLibrary(i).getNumOutputs(); k++) {
					RecipeLibrary.getRecipeFromLibrary(i).getOutput(k).draw(g, x += itemSize + padding, y, itemSize, itemSize, observer);
				}
			} else if (y < yInitial) numItemsAbove++;
			else if (y + itemSize >= screenHeight - 50) numItemsBelow++;
			y += itemSize + padding;
			x = xInitial;
		}
		if (numItemsAbove > 0) {
			int indicatorX = xInitial + (itemSize * 4 / 5 + padding)/2 - itemSize / 4;
			g.drawImage(MediaLibrary.getImageFromLibrary(5002), indicatorX, yInitial, itemSize / 2, itemSize / 2, observer);
			g.setColor(Color.RED);
			g.setFont(MediaLibrary.getFontFromLibrary("Indicator"));
			g.drawString("" + numItemsAbove, indicatorX + itemSize / 4 - g.getFontMetrics().stringWidth("" + numItemsAbove) / 2, yInitial + itemSize / 2 + padding / 4 + g.getFontMetrics().getAscent());
		}
		if (numItemsBelow > 0) {
			int indicatorX = xInitial + (itemSize * 4 / 5 + padding)/2 - itemSize / 4;
			g.drawImage(MediaLibrary.getImageFromLibrary(5002), indicatorX, screenHeight - 50, itemSize / 2, -itemSize / 2, observer);
			g.setColor(Color.RED);
			g.setFont(MediaLibrary.getFontFromLibrary("Indicator"));
			g.drawString("" + numItemsBelow, indicatorX + itemSize / 4 - g.getFontMetrics().stringWidth("" + numItemsAbove) / 2, screenHeight - 50 - itemSize / 2 - padding / 4 - g.getFontMetrics().getDescent());
		}
		scrollMax = y - screenHeight + 200 + scrollY;
	}
	
	public BasicCraftingGUI(GameLoop game) {
		super(new int[] {1});
		gameIn = game;
		scrollY = 0;
		scrollMax = 1000;
	}
	
	public void handleClick(int clickX, int clickY) {
		//System.out.println("" + clickX + ", " + clickY);
		if (clickX >= xInitial + itemSize * 4 / 5 + padding && clickX <= xInitial + itemSize * 4 / 5 + padding + itemSize) {
			for (int i = 0; i < RecipeLibrary.getFilledRecipeSlots(); i++) {
				if (clickY >= yValues[i] && clickY <= yValues[i] + itemSize) {
					//System.out.println("Item " + i + " button presses");
					InventoryItem[] currentItems = gameIn.player.inventory.getItems();
					if (controls.shift.isPressed()) {
						while (RecipeLibrary.getRecipeFromLibrary(i).checkRecipe(currentItems)) {
							for (int j = 0; j < RecipeLibrary.getRecipeFromLibrary(i).getNumOutputs(); j++) {
								gameIn.player.inventory.addItem(RecipeLibrary.getRecipeFromLibrary(i).getOutput(j));
							}
							for (int k = 0; k < RecipeLibrary.getRecipeFromLibrary(i).getNumInputs(); k++) {
								gameIn.player.inventory.removeItem(RecipeLibrary.getRecipeFromLibrary(i).getInput(k));
							}
							if (gameIn.tracker != null) gameIn.tracker.incrementBasicStat("Times Crafted");
						}
					} else if (controls.ctrl.isPressed()) {
						int minStackSizeToQuantityRatio = 80;
						for (int k = 0; k < RecipeLibrary.getRecipeFromLibrary(i).getNumInputs(); k++) {
							if (RecipeLibrary.getRecipeFromLibrary(i).getInput(k).getClass() == InventoryTile.class) {
								if (80 / ((InventoryTile) RecipeLibrary.getRecipeFromLibrary(i).getInput(k)).getQuantity() < minStackSizeToQuantityRatio) {
									minStackSizeToQuantityRatio = 80 / ((InventoryTile) RecipeLibrary.getRecipeFromLibrary(i).getInput(k)).getQuantity();
								}
							} else if (RecipeLibrary.getRecipeFromLibrary(i).getInput(k).getClass() == Ingredient.class) {
								if (AttributeLibrary.getIngredientStackSizeFromLibrary(((Ingredient) RecipeLibrary.getRecipeFromLibrary(i).getInput(k)).getItemID()) / ((Ingredient) RecipeLibrary.getRecipeFromLibrary(i).getInput(k)).getQuantity() < minStackSizeToQuantityRatio) {
									minStackSizeToQuantityRatio =AttributeLibrary.getIngredientStackSizeFromLibrary(((Ingredient) RecipeLibrary.getRecipeFromLibrary(i).getInput(k)).getItemID()) / ((Ingredient) RecipeLibrary.getRecipeFromLibrary(i).getInput(k)).getQuantity();
								}
							}
						}
						
						int n = 0;
						while ((freeCrafting || RecipeLibrary.getRecipeFromLibrary(i).checkRecipe(currentItems)) && n < minStackSizeToQuantityRatio) {
							for (int j = 0; j < RecipeLibrary.getRecipeFromLibrary(i).getNumOutputs(); j++) {
								gameIn.player.inventory.addItem(RecipeLibrary.getRecipeFromLibrary(i).getOutput(j));
							}
							for (int k = 0; k < RecipeLibrary.getRecipeFromLibrary(i).getNumInputs(); k++) {
								gameIn.player.inventory.removeItem(RecipeLibrary.getRecipeFromLibrary(i).getInput(k));
							}
							if (gameIn.tracker != null) gameIn.tracker.incrementBasicStat("Times Crafted");
							n++;
						}
					}
					else {
						if (freeCrafting || RecipeLibrary.getRecipeFromLibrary(i).checkRecipe(currentItems)) {
							for (int j = 0; j < RecipeLibrary.getRecipeFromLibrary(i).getNumOutputs(); j++) {
								gameIn.player.inventory.addItem(RecipeLibrary.getRecipeFromLibrary(i).getOutput(j));
							}
							for (int k = 0; k < RecipeLibrary.getRecipeFromLibrary(i).getNumInputs(); k++) {
								gameIn.player.inventory.removeItem(RecipeLibrary.getRecipeFromLibrary(i).getInput(k));
							}
							if (gameIn.tracker != null) gameIn.tracker.incrementBasicStat("Times Crafted");
						}	
					}
				}
			}
		}
	}

	public void handleScroll(int scrollDelta) {
		scrollValue = itemSize / 10;
		if (scrollMax < scrollValue * 2) {
			scrollY = 0;
			return;
		}
		if (scrollY + scrollDelta * scrollValue > 0 && scrollY + scrollDelta * scrollValue < scrollMax) scrollY += scrollDelta * scrollValue;
		if (scrollY < scrollValue) scrollY = scrollValue;
		if (scrollY > scrollMax) scrollY = scrollMax;
	}
	
	public void returnToTop() {
		scrollY = 0;
	}
	
	public void scrollToBottom() {
		scrollY = scrollMax;
	}
	
	public int getScrollY() {
		return scrollY;
	}
	
	public void toggleFreeCrafting() {
		freeCrafting = !freeCrafting;
	}
	
	public void adjustColorSelectors(float delta) {
		for (int i = 0; i < RecipeLibrary.getFilledRecipeSlots(); i++) {
							for (int k = 0; k < RecipeLibrary.getRecipeFromLibrary(i).getNumOutputs(); k++) {
					if (!(RecipeLibrary.getRecipeFromLibrary(i).getOutput(k) == null) && ColorSelector.class.isAssignableFrom(RecipeLibrary.getRecipeFromLibrary(i).getOutput(k).getClass())) {
						RecipeLibrary.getRecipeFromLibrary(i).shiftColorSelectorHue(k, delta);
					}
				}
			}
	}
}
