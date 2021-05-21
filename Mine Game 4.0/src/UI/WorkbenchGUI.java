package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Frame.InputHandler;

public class WorkbenchGUI extends CraftingMenu {
	public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer, InputHandler controls) {
		g.setColor(Color.GREEN);
		g.drawRect(50, 50, screenWidth - 100, screenHeight - 100);
	}
	
	public WorkbenchGUI() {
		super(new int[] {1});
	}
}
