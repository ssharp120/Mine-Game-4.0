package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import Libraries.MediaLibrary;
import Libraries.RecipeLibrary;
import SingleplayerClient.InputHandler;
import Utilities.TechTree;
import Utilities.TechTree.TechTreeNode;

public class TechTreeGUI extends CraftingMenu {

	private int scrollY = 0;
	private int scrollMax;
	int scrollValue = 10;
	
	int numNodesAbove = 0;
	int numNodesBelow = 0;

	int screenWidth = 1000;
	int screenHeight = 500;
	
	
	// Graphics offsets
	int screenBufferHorizontal = 64;
	int screenBufferVertical = 64;
	
	int itemBufferHorizontal = 32;
	int itemBufferVertical = 64;
	int iconWidth = 128;
	int iconHeight = 128;
	
	int standardOffsetHorizontal = itemBufferHorizontal + iconWidth;
	int standardOffsetVertical = (itemBufferVertical * 3 + iconHeight * 2);
	
	
	
	
	private TechTree techTree;
	
	private InputHandler controls;
	
	public TechTreeGUI(InputHandler controls, TechTree techTree) {
		super(new int[] {1});
		
		this.controls = controls;
		this.techTree = techTree;
	}
	
	public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer, InputHandler controls) {		
		screenBufferHorizontal = screenWidth / 6;
		screenBufferVertical = screenHeight / 6;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		
		g.setColor(new Color(255, 255, 255, 16));
		g.fill3DRect(screenBufferHorizontal, screenBufferVertical, screenWidth - (screenBufferHorizontal * 2), screenHeight, true);
		
		//System.out.println("Filled nodes: " + techTree.getFilledTechTreeNodes());
		
		g.translate(0, 32 - scrollY);
		
		// Draw nodes
		int finalNode = 0;
		numNodesBelow = 0;
		numNodesAbove = 0;
		for (int i = 0; i < techTree.getFilledTechTreeNodes(); i++) {
			InventoryItem[] input = techTree.getTechTreeNode(i).getInputs();
			Recipe[] output = techTree.getTechTreeNode(i).getOutputs();
			String header = techTree.getTechTreeNode(i).getHeader();

			boolean draw = true;
			
			if (screenBufferVertical + itemBufferVertical + i * standardOffsetVertical + 32 - scrollY > screenHeight - screenBufferVertical) {
				numNodesBelow++;
				draw = false;
			}
			if (screenBufferVertical + itemBufferVertical + i * standardOffsetVertical + 32 - scrollY < screenBufferVertical + 48) {
				numNodesAbove++;
				draw = false;
			}
			
			if (draw) {
				g.setColor(new Color(255, 234, 234, 8));
				g.fill3DRect(screenBufferHorizontal + itemBufferHorizontal / 2, screenBufferVertical + i * standardOffsetVertical, screenWidth - (screenBufferHorizontal * 2) - 36, iconHeight * 3, true);
				
				// Draw header
				g.setFont(MediaLibrary.getFontFromLibrary("TechTreeFont"));
				g.setColor(new Color(222, 222, 222));
				g.drawString(header, screenBufferHorizontal + itemBufferHorizontal, screenBufferVertical + itemBufferVertical + i * standardOffsetVertical);
				
				// Draw inputs [InventoryItem]
				for (int j = 0; j < input.length; j++) {
					if (!(input[j] == null)) {
						g.setColor(new Color(63, 63, 63, 127));
						if (techTree.getTechTreeNode(i).isActive()) g.setColor(new Color(63, 127, 63, 127));
						g.fill3DRect(screenBufferHorizontal + itemBufferHorizontal + j * standardOffsetHorizontal, screenBufferVertical + itemBufferVertical + i * standardOffsetVertical + 32, iconWidth, iconHeight, true);
						
						input[j].draw(g, screenBufferHorizontal + itemBufferHorizontal + j * standardOffsetHorizontal, screenBufferVertical + itemBufferVertical + i * standardOffsetVertical + 32, iconWidth, iconHeight, observer);
					}
				}
				
				// Draw outputs [Recipe]
				for (int j = 0; j < output.length; j++) {
					//System.out.println("j: " + j);
					if (!(output[j] == null)) {
						Recipe r = output[j];
						//System.out.println(r.toString());
						
						// Draw inputs [InventoryItem]
						int inputOffset = 0;
						for (int k = 0; k < r.getNumInputs(); k++) {
							//System.out.println("k: " + k);
							r.getInput(k).draw(g, screenBufferHorizontal + itemBufferHorizontal + k * standardOffsetHorizontal,
									screenBufferVertical + itemBufferVertical + i * standardOffsetVertical + itemBufferVertical * 3, iconWidth, iconHeight, observer);
							inputOffset = k + 1;
						}
						
						// Draw arrow [Image]
						int arrowID = 5000;
						if (techTree.getTechTreeNode(i).isComplete()) arrowID = 5001;
						g.drawImage(MediaLibrary.getImageFromLibrary(arrowID), screenBufferHorizontal + itemBufferHorizontal + inputOffset * standardOffsetHorizontal,
								screenBufferVertical + itemBufferVertical + i * standardOffsetVertical + itemBufferVertical * 3, iconWidth, iconHeight, observer);
						
						// Draw outputs [InventoryItem]
						int outputOffset = 0;
						for (int k = 0; k < r.getNumOutputs(); k++) {
							//System.out.println("k: " + k);
							r.getOutput(k).draw(g, screenBufferHorizontal + itemBufferHorizontal + (k + 1 + inputOffset) * standardOffsetHorizontal,
									screenBufferVertical + itemBufferVertical + i * standardOffsetVertical + itemBufferVertical * 3, iconWidth, iconHeight, observer);
							outputOffset = k + 1;
						}
					}
				}
			}
			
			finalNode = i;
		}
		
		g.translate(0, scrollY - 32);
		
		scrollMax = screenBufferVertical + itemBufferVertical + finalNode * standardOffsetVertical + itemBufferVertical - (screenHeight - 2 * screenBufferVertical);
		
		if (numNodesAbove > 0) {
			g.drawImage(MediaLibrary.getImageFromLibrary(5002), screenBufferHorizontal - iconWidth, screenBufferVertical, iconWidth / 2, iconHeight / 2, observer);
			g.setColor(Color.RED);
			g.setFont(MediaLibrary.getFontFromLibrary("Indicator"));
			g.drawString("" + numNodesAbove, screenBufferHorizontal + iconWidth / 4 - iconWidth - g.getFontMetrics().stringWidth("" + numNodesAbove) / 2, screenBufferVertical + iconHeight / 2 + g.getFontMetrics().getAscent());
		}
		if (numNodesBelow > 0) {
			g.drawImage(MediaLibrary.getImageFromLibrary(5002), screenBufferHorizontal - iconWidth, screenHeight - screenBufferVertical, iconWidth / 2, -iconHeight / 2, observer);
			g.setColor(Color.RED);
			g.setFont(MediaLibrary.getFontFromLibrary("Indicator"));
			g.drawString("" + numNodesBelow, screenBufferHorizontal + iconWidth / 4 - iconWidth - g.getFontMetrics().stringWidth("" + numNodesBelow) / 2, screenHeight - screenBufferVertical - iconHeight / 2 - g.getFontMetrics().getDescent());
		}
	}
	
	public void handleScroll(int scrollDelta) {
		scrollValue = 16;
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
	
	public void tick(InputHandler input, boolean canScroll, Inventory inventory) {
		if (input.end.isPressed()) setActive(false);
		if (canScroll && input.up.isPressed()) handleScroll(-1);
		if (canScroll && input.down.isPressed()) handleScroll(1);
				
		for (int i = 0; i < techTree.getFilledTechTreeNodes(); i++) {
			boolean active = false;
			
			if (checkNode(inventory.getItems(), techTree.getTechTreeNode(i))) {
				active = true;
			}
			
			techTree.getTechTreeNode(i).setActive(active);
		}
	}
	
	public void handleClick(int clickX, int clickY, Inventory inventory) {
		for (int i = 0; i < techTree.getFilledTechTreeNodes(); i++) {
			int x = screenBufferHorizontal + itemBufferHorizontal / 2;
			int y = screenBufferVertical + i * standardOffsetVertical;
			int width = screenWidth - (screenBufferHorizontal * 2) - 36;
			int height = iconHeight * 3;
			
			System.out.println("Click at x: " + clickX + " ? (" + x + ", " + (x + width) + "), y: " + clickY + " ? (" + y + ", " + (y + height) + ")");
			
			if (clickX > x && clickX < x + width && clickY > y && clickY < y + height) {
				//System.out.println("Click");
				if (techTree.getTechTreeNode(i).isActive() && !techTree.getTechTreeNode(i).isComplete()) {
					techTree.getTechTreeNode(i).complete();
					for (InventoryItem item : techTree.getTechTreeNode(i).getInputs()) {
						inventory.removeItem(item);
					}
				}
			}
		}
	}
	
	public boolean checkNode(InventoryItem in[], TechTreeNode node) {
		if (in.length >= node.getInputs().length) {
			for (InventoryItem j : node.getInputs()) {
				boolean inputSatisfied = false;
				for (int i = 0; i < in.length; i++) {
					if (in[i] != null) {
						if (in[i].getItemID() == j.getItemID()) {
							if (in[i].getClass() == j.getClass() && in[i].getClass() == Ingredient.class) {
								if (!inputSatisfied) inputSatisfied = ((Ingredient) in[i]).getQuantity() >= ((Ingredient) j).getQuantity();
							} else if (in[i].getClass() == j.getClass() && in[i].getClass() == InventoryTile.class) {
								if (!inputSatisfied) inputSatisfied = ((InventoryTile) in[i]).getQuantity() >= ((InventoryTile) j).getQuantity();
							} else if (in[i].getClass() == j.getClass() && in[i].getClass() == InventoryTool.class) {
								if (!inputSatisfied) inputSatisfied = ((InventoryTool) in[i]).getDurability() == ((InventoryTool) in[i]).getMaxDurability();
							}
						}
					}
				}
				if (!inputSatisfied) return false;
			}
			return true;
		}
		return false;
	}
}
