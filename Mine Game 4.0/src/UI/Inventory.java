package UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

import Entities.Furniture;
import Frame.InputHandler;
import Frame.InputHandler.ControlScheme;
import Frame.Level;

public class Inventory {
	private InventoryItem[] items;
	private int slots;
	private int stackSize;
	private int iconSize = 64;
	private int offset = 64;
	private int slotsPerRow = 10;
	private InventoryItem freeItem;
	private InventoryItem activeItem;

	private int freeItemOrigin;
	private Dimension mouseLocation = new Dimension(offset, offset);
	private InputHandler controls;
	
	public int getIconSize() {
		return iconSize;
	}

	public Dimension getTopLeft(int i) {
		return topLeft[i];
	}

	private boolean isActive;
	private int selectedHotbarSlot = 0;
	private int selectedSlot = 0;

	private Dimension topLeft[];
	
	public Inventory(int slots, int stackSize, InputHandler controls) {
		this.slots = slots;
		this.stackSize = stackSize;
		this.controls = controls;
		items = new InventoryItem[slots];
		//items[0] = new Pickaxe(10000, 25.0, 200000.0, 175000.0, "Steel Pickaxe");
		//items[1] = new Shovel(10001, 25.0, 200000.0, 175000.0, "Steel Shovel");
		//items[2] = new Axe(10002, 25.0, 200000.0, 175000.0, "Steel Shovel");
		//items[2] = new InventoryEntity(new Furniture(364/32, 16765/32, controls.gameIn.level, Furniture.FURNITURE_ID.WORKBENCH));
		topLeft = new Dimension[slots];
	}
	
	public void tick() {
		for (int i = 0; i < slots; i++) {
			if (items[i] != null) {
				items[i].tick();
				if (items[i].markedForDeletion) items[i] = null;
			}
		}
		determineActiveSlot();
		if (!isActive) {
			freeItem = null;
		}
	}
	
	public void draw(Graphics g, int screenWidth, int screenHeight, ImageObserver observer) {
		int targetX = screenWidth - (2 * offset), targetY = screenHeight - (2 * offset);
		g.translate(offset, offset);
		g.setColor(new Color(255, 255, 255, 132));
		g.fillRect(0, 0, targetX, targetY);
		g.setColor(Color.darkGray);
		g.draw3DRect(0, 0, targetX, targetY, true);
		
		if (targetX / 16 < targetY / 9) iconSize = targetX / 16;
		else iconSize = targetY / 9;
		
		int spacing = 8;
		slotsPerRow = (targetX - offset) / (iconSize + spacing);
		int iconOffset = (targetX - offset - (slotsPerRow * (iconSize + spacing) - spacing)) / 2;
		for (int i = 0; i < slots; i++) {
			g.setColor(new Color(i * (128/slots) + 16, i * (128/slots) + 16, i * (128/slots) + 16, 255));
			g.fillRect((i % slotsPerRow) * iconSize + offset / 2 + iconOffset + ((i % slotsPerRow) * spacing), (i / slotsPerRow) * iconSize + offset / 2 + ((i / slotsPerRow) * spacing), iconSize, iconSize);
			topLeft[i] = new Dimension(offset + (i % slotsPerRow) * iconSize + offset / 2 + iconOffset + ((i % slotsPerRow) * spacing), offset + (i / slotsPerRow) * iconSize + offset / 2 + ((i / slotsPerRow) * spacing));
			if (items[i] != null && !(isFreeItemSet() && i == freeItemOrigin)) items[i].draw(g, (i % slotsPerRow) * iconSize + offset / 2 + iconOffset + ((i % slotsPerRow) * spacing), (i / slotsPerRow) * iconSize + offset / 2 + ((i / slotsPerRow) * spacing), iconSize, iconSize, observer);
		}
		g.translate(-offset, -offset);
		if (isFreeItemSet()) drawFreeItem(g, observer);
	}
	
	public void drawHotbar(Graphics g, int screenWidth, int screenHeight, ImageObserver observer) {
		int targetX = screenWidth - (2 * offset), targetY = screenHeight - (2 * offset);
		int spacing = 8;
		slotsPerRow = 10;
		int iconOffset = (targetX - (slotsPerRow * (iconSize + spacing) - spacing) + offset) / 2;
		if (targetX / 16 < targetY / 9) iconSize = targetX / 16;
		else iconSize = targetY / 9;
		int outline = 6 + iconSize / 6;
		g.setColor(new Color(255, 255, 255, 178));
		g.fillRect(offset / 2 + iconOffset - outline / 2, (offset - outline) / 2, ((iconSize + spacing) * 10) - spacing + outline, iconSize + outline);
		for (int i = 0; i < 10; i++) {
			g.setColor(new Color(i * (128/10) + 16, i * (128/10) + 16, i * (128/10) + 16, 255));
			g.fillRect((i % slotsPerRow) * iconSize + offset / 2 + iconOffset + ((i % slotsPerRow) * spacing), (i / slotsPerRow) * iconSize + offset / 2 + ((i / slotsPerRow) * spacing), iconSize, iconSize);
			if (items[i] != null) items[i].draw(g, (i % slotsPerRow) * iconSize + offset / 2 + iconOffset + ((i % slotsPerRow) * spacing), (i / slotsPerRow) * iconSize + offset / 2 + ((i / slotsPerRow) * spacing), iconSize, iconSize, observer);
			if (i == selectedHotbarSlot + 1 || (i == 9 && i == selectedHotbarSlot)) {
				int j = 10 - selectedHotbarSlot;
				g.setColor(new Color(j * (128/10) + 16, j * (128/10) + 16, j * (128/10) + 16, 255));
				g.fillRect((selectedHotbarSlot % slotsPerRow) * iconSize + (offset - outline) / 2 + iconOffset + ((selectedHotbarSlot % slotsPerRow) * spacing), (offset - outline) / 2, iconSize + outline, iconSize + outline);
				g.setColor(new Color(selectedHotbarSlot * (128/10) + 16, selectedHotbarSlot * (128/10) + 16, selectedHotbarSlot * (128/10) + 16, 255));
				g.fillRect((selectedHotbarSlot % slotsPerRow) * iconSize + offset / 2 + iconOffset + ((selectedHotbarSlot % slotsPerRow) * spacing), (selectedHotbarSlot / slotsPerRow) * iconSize + offset / 2 + ((selectedHotbarSlot / slotsPerRow) * spacing), iconSize, iconSize);
				if (items[selectedHotbarSlot] != null) items[selectedHotbarSlot].draw(g, (selectedHotbarSlot % slotsPerRow) * iconSize + offset / 2 + iconOffset + ((selectedHotbarSlot % slotsPerRow) * spacing), 
						(selectedHotbarSlot / slotsPerRow) * iconSize + offset / 2 + ((selectedHotbarSlot / slotsPerRow) * spacing), iconSize, iconSize, observer);
			}
		}
	}
	
	public boolean addItem(InventoryItem item) {
		InventoryTile t = null;
		if (item.getClass() == InventoryTile.class) {
			t = (InventoryTile) item;
			for (int i = 0; i < slots; i++) {
				if (items[i] != null && item.getClass() == InventoryTile.class && items[i].getClass() == InventoryTile.class) {
					if (t.getTileID() == ((InventoryTile) items[i]).getTileID()) {
						if (t.getQuantity() + ((InventoryTile) items[i]).getQuantity() <= stackSize) {
							((InventoryTile) items[i]).addQuantity(t.getQuantity());
							return true;
						} else if (((InventoryTile) items[i]).getQuantity() <= stackSize) {
							int k = stackSize - ((InventoryTile) items[i]).getQuantity();
							((InventoryTile) items[i]).addQuantity(k);
							t.removeQuantity(k);
						}
					}
				}
			}
		}

		Ingredient n = null;
		if (item.getClass() == Ingredient.class) {
			n = (Ingredient) item;
			for (int i = 0; i < slots; i++) {
				if (items[i] != null && item.getClass() == Ingredient.class && items[i].getClass() == Ingredient.class) {
					if (n.getItemID() == ((Ingredient) items[i]).getItemID()) {
						if (n.getQuantity() + ((Ingredient) items[i]).getQuantity() <= stackSize) {
							((Ingredient) items[i]).addQuantity(n.getQuantity());
							return true;
						} else if (((Ingredient) items[i]).getQuantity() <= stackSize) {
							int k = stackSize - ((Ingredient) items[i]).getQuantity();
							((Ingredient) items[i]).addQuantity(k);
							n.removeQuantity(k);
						}
					}
				}
			}
		}
		
		for (int i = 0; i < slots; i++) {
			if (items[i] == null) {
				if (item.getClass() == InventoryTile.class && ((InventoryTile) item).markedForDeletion) {
					break;
				}
				if (item.getClass() == Ingredient.class && ((Ingredient) item).markedForDeletion) {
					break;
				}
				items[i] = item;
				return true;
			}
		}
		
		return false;
	}
	
	public boolean removeItem(InventoryItem item) {
		int remainingQuantity = 0;
		InventoryTile t = null;
		if (item.getClass() == InventoryTile.class) {
			t = (InventoryTile) item;
			remainingQuantity = ((InventoryTile) item).getQuantity();
			for (int i = 0; i < slots; i++) {
				if (items[i] != null && item.getClass() == InventoryTile.class && items[i].getClass() == InventoryTile.class) {
					if (t.getTileID() == ((InventoryTile) items[i]).getTileID()) {
						if (remainingQuantity <= ((InventoryTile) items[i]).getQuantity()) {
							((InventoryTile) items[i]).removeQuantity(remainingQuantity);
							return true;
						} else {
							remainingQuantity = remainingQuantity - ((InventoryTile) items[i]).getQuantity();
							((InventoryTile) items[i]).removeQuantity(((InventoryTile) items[i]).getQuantity());
						}
					}
				}
			}
		}

		Ingredient n = null;
		if (item.getClass() == Ingredient.class) {
			n = (Ingredient) item;
			remainingQuantity = ((Ingredient) item).getQuantity();
			for (int i = 0; i < slots; i++) {
				if (items[i] != null && item.getClass() == Ingredient.class && items[i].getClass() == Ingredient.class) {
					if (n.getItemID() == ((Ingredient) items[i]).getItemID()) {
						if (remainingQuantity <= ((Ingredient) items[i]).getQuantity()) {
							((Ingredient) items[i]).removeQuantity(remainingQuantity);
							return true;
						} else {
							remainingQuantity = remainingQuantity - ((Ingredient) items[i]).getQuantity();
							((Ingredient) items[i]).removeQuantity(((Ingredient) items[i]).getQuantity());
						}
					}
				}
			}
			
		}
		return false;
	}
	
	public void drawFreeItem(Graphics g, ImageObserver observer) {
		freeItem.draw(g, mouseLocation.width - iconSize / 2, mouseLocation.height - iconSize / 2, iconSize, iconSize, observer);
	}
	
	public int getFreeItemOrigin() {
		return freeItemOrigin;
	}

	public void setFreeItem(int i) {
		if (items[i] != null) {
			freeItem = items[i];
			freeItemOrigin = i;
		}
	}
	
	public InventoryItem[] getItems() {
		return items;
	}
	
	public void dropFreeItem(int i) {
		if (freeItem == null) return;
		if (items[i] == null || i == freeItemOrigin) {
			items[freeItemOrigin] = null;
			items[i] = freeItem;
		} else {
			if (freeItem.getClass() == InventoryTile.class && items[i].getClass() == InventoryTile.class) {
				if (((InventoryTile) freeItem).getTileID() == ((InventoryTile) items[i]).getTileID()) {
					if (((InventoryTile) freeItem).getQuantity() + ((InventoryTile) items[i]).getQuantity() <= stackSize) {
						((InventoryTile) items[i]).addQuantity(((InventoryTile) freeItem).getQuantity());
						freeItem = null;
						items[freeItemOrigin] = null;
						return;
					} else {
						int k = stackSize - ((InventoryTile) items[i]).getQuantity();
						((InventoryTile) items[i]).addQuantity(k);
						InventoryTile t = (InventoryTile) freeItem;
						t.removeQuantity(k);
						items[freeItemOrigin] = t;
						freeItem = null;
						return;
					}
				}
			}
			InventoryItem j = freeItem;
			InventoryItem k = items[i];
			freeItem = k;
			items[i] = j;
			items[freeItemOrigin] = null;
			return;
		}
		freeItem = null;
	}
	
	public void determineActiveSlot() {
		if (isActive()) {
			if (controls.left.isPressed()) selectedSlot--;
			if (controls.right.isPressed()) selectedSlot++;
			if (controls.down.isPressed()) selectedSlot += slotsPerRow;
			if (controls.up.isPressed()) selectedSlot -= slotsPerRow;
		} else if (controls.getControlScheme() == ControlScheme.GAMEPLAY){
			if (controls.num0.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(9);
				}
				setSelectedHotbarSlot(9);
			}
			if (controls.num1.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(0);
				}
				setSelectedHotbarSlot(0);
			}
			if (controls.num2.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(1);
				}
				setSelectedHotbarSlot(1);
			}
			if (controls.num3.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(2);
				}
				setSelectedHotbarSlot(2);
			}
			if (controls.num4.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(3);
				}
				setSelectedHotbarSlot(3);
			}
			if (controls.num5.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(4);
				}
				setSelectedHotbarSlot(4);
			}
			if (controls.num6.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(5);
				}
				setSelectedHotbarSlot(5);
			}
			if (controls.num7.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(6);
				}
				setSelectedHotbarSlot(6);
			}
			if (controls.num8.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(7);
				}
				setSelectedHotbarSlot(7);
			}
			if (controls.num9.isPressed()) {
				if (controls.shift.isPressed()) {
					setFreeItem(selectedHotbarSlot);
					dropFreeItem(8);
				}
				setSelectedHotbarSlot(8);
			}
		}
		
		activeItem = items[selectedHotbarSlot];
	}
	
	public boolean isFreeItemSet() {
		return freeItem != null;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getSelectedHotbarSlot() {
		return selectedHotbarSlot;
	}

	public void setSelectedHotbarSlot(int selectedHotbarSlot) {
		this.selectedHotbarSlot = selectedHotbarSlot;
		hotbarSlotCheck();
	}
	
	public void incrementHotbarSlot() {
		selectedHotbarSlot++;
		hotbarSlotCheck();
	}
	
	public void decrementHotbarSlot() {
		selectedHotbarSlot--;
		hotbarSlotCheck();
	}

	public void hotbarSlotCheck() {
		if (selectedHotbarSlot < 0) selectedHotbarSlot = 9;
		if (selectedHotbarSlot > 9) selectedHotbarSlot = 0;
	}

	public int getSlots() {
		return slots;
	}

	public void setSlots(int slots) {
		this.slots = slots;
	}

	public int getStackSize() {
		return stackSize;
	}

	public void setStackSize(int stackSize) {
		this.stackSize = stackSize;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public void toggleActive() {
		this.isActive = !isActive;
	}
	
	public int getTileFromHotbar() {
		if (items[selectedHotbarSlot] != null && items[selectedHotbarSlot].getClass() == InventoryTile.class) {
			return ((InventoryTile) items[selectedHotbarSlot]).getTileID();
		}
		return 2;
	}

	public Dimension getMouseLocation() {
		return mouseLocation;
	}

	public void setMouseLocation(Dimension mouseLocation) {
		this.mouseLocation = mouseLocation;
	}
	
	public int getSelectedSlot() {
		return selectedSlot;
	}

	public void setSelectedSlot(int selectedSlot) {
		this.selectedSlot = selectedSlot;
	}
	
	public InventoryItem getActiveItem() {
		return activeItem;
	}

	public void setActiveItem(InventoryItem activeItem) {
		this.activeItem = activeItem;
	}
}
