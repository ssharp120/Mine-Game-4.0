package UI;

import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Frame.InputHandler;

public abstract class CraftingMenu {
	private boolean isActive;
	public int[] possibleRecipes;
	
	public CraftingMenu(int[] possibleRecipes) {
		this.possibleRecipes = possibleRecipes;
		isActive = false;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public void toggleActive() {
		isActive = !isActive;
	}
	
	public abstract void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer, InputHandler controls);
}
