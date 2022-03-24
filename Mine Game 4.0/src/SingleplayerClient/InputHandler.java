package SingleplayerClient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import Entities.Crop;
import Entities.Entity;
import Entities.Furniture;
import Entities.Projectile;
import Libraries.MediaLibrary;
import Tiles.BackgroundDestructibleTile;
import Tiles.DestructibleTile;
import Tiles.Tile;
import UI.Ingredient;
import UI.InventoryEntity;
import UI.InventoryItem;
import UI.InventoryTile;
import UI.InventoryTool;
import UI.Bow;
import Utilities.FileUtilities;

import static Utilities.FileUtilities.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	@SuppressWarnings("unused")
	
	private int inventoryKey = KeyEvent.VK_I;
	
	public enum ControlScheme {
		GAMEPLAY, MENU, INVENTORY, BASIC_CRAFTING, PAUSE_MENU, TECH_TREE;
	}
	
	public GameLoop gameIn;
	private ControlScheme controlScheme = ControlScheme.GAMEPLAY;
	private boolean mouseJustPressed;
	
	public ControlScheme getControlScheme() {
		return controlScheme;
	}

	public void setControlScheme(ControlScheme controlScheme) {
		this.controlScheme = controlScheme;
	}

	public InputHandler(GameLoop game) {
		gameIn = game;
	}
	
	public class Key {
		private int numTimesPressed = 0;
		private boolean pressed = false;
		public boolean isPressed() {
			return pressed;
		}
		public int getNumTimesPressed() {
			return numTimesPressed;
		}
		public void toggle(boolean isPressed) {
			pressed = isPressed;
		}
		public void inc() {
			numTimesPressed++;
		}
	}
	
	public boolean leftButtonHeld, rightButtonHeld;
	public int lastDestructibleX, lastDestructibleY;
	
	public boolean toggleUIs;
	
	public Key up = new Key();
	public Key esc = new Key();
	public Key down = new Key();
	public Key left = new Key();
	public Key right = new Key();
	public Key arrowUp = new Key();
	public Key arrowDown = new Key();
	public Key arrowLeft = new Key();
	public Key arrowRight = new Key();
	public Key inventory = new Key();
	public Key techTree = new Key();
	public Key crafting = new Key();
	public Key miniMap = new Key();
	public Key collect = new Key();
	public Key light = new Key();
	public Key drop = new Key();
	
	public Key shift = new Key();
	public Key ctrl = new Key();
	public Key alt = new Key();
	
	public Key insert = new Key();
	public Key delete = new Key();
	public Key home = new Key();
	public Key end = new Key();
	public Key pageUp = new Key();
	public Key pageDown = new Key();
	
	public Key func1 = new Key();
	public Key func2 = new Key();
	public Key func3 = new Key();
	public Key func4 = new Key();
	public Key func5 = new Key();
	public Key func6 = new Key();
	public Key func7 = new Key();
	public Key func8 = new Key();
	public Key func9 = new Key();
	public Key func10 = new Key();
	public Key func11 = new Key();
	public Key func12 = new Key();
	
	public Key num1 = new Key();
	public Key num2 = new Key();
	public Key num3 = new Key();
	public Key num4 = new Key();
	public Key num5 = new Key();
	public Key num6 = new Key();
	public Key num7 = new Key();
	public Key num8 = new Key();
	public Key num9 = new Key();
	public Key num0 = new Key();
	
	public void keyPressed(KeyEvent arg0) {
		toggleKey(arg0.getKeyCode(), true);
		if (arg0.getKeyCode() == KeyEvent.VK_W) {up.inc();}
		if (arg0.getKeyCode() == KeyEvent.VK_S) {down.inc();}
		if (arg0.getKeyCode() == KeyEvent.VK_A) {left.inc();}
		if (arg0.getKeyCode() == KeyEvent.VK_D) {right.inc();}
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) esc.toggle(false);
	}

	public void keyReleased(KeyEvent arg0) {
		toggleKey(arg0.getKeyCode(), false);
		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) esc.toggle(true);
	}

	public void keyTyped(KeyEvent arg0) {
		
	}
	
	public void toggleKey(int keycode, boolean isPressed) {
		if (keycode == KeyEvent.VK_W) {up.toggle(isPressed);}
		if (keycode == KeyEvent.VK_S) {down.toggle(isPressed);}
		if (keycode == KeyEvent.VK_A) {left.toggle(isPressed);}
		if (keycode == KeyEvent.VK_D) {right.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_UP) {up.toggle(isPressed);}
		if (keycode == KeyEvent.VK_DOWN) {down.toggle(isPressed);}
		if (keycode == KeyEvent.VK_LEFT) {left.toggle(isPressed);}
		if (keycode == KeyEvent.VK_RIGHT) {right.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_1) {num1.toggle(isPressed);}
		if (keycode == KeyEvent.VK_2) {num2.toggle(isPressed);}
		if (keycode == KeyEvent.VK_3) {num3.toggle(isPressed);}
		if (keycode == KeyEvent.VK_4) {num4.toggle(isPressed);}
		if (keycode == KeyEvent.VK_5) {num5.toggle(isPressed);}
		if (keycode == KeyEvent.VK_6) {num6.toggle(isPressed);}
		if (keycode == KeyEvent.VK_7) {num7.toggle(isPressed);}
		if (keycode == KeyEvent.VK_8) {num8.toggle(isPressed);}
		if (keycode == KeyEvent.VK_9) {num9.toggle(isPressed);}
		if (keycode == KeyEvent.VK_0) {num0.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_SHIFT) {shift.toggle(isPressed);}
		if (keycode == KeyEvent.VK_CONTROL) {ctrl.toggle(isPressed);}
		if (keycode == KeyEvent.VK_ALT) {
			alt.toggle(isPressed);
			if (toggleUIs) {
				gameIn.toggleDisplayUIs();
				toggleUIs = false;
			} else if (!alt.isPressed()) {
				gameIn.toggleDisplayUIs();
				toggleUIs = true;
			}
		}
		
		if (keycode == KeyEvent.VK_F1) {func1.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F2) {func2.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F3) {func3.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F4) {func4.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F5) {func5.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F6) {func6.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F7) {func7.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F8) {func8.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F9) {func9.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F10) {func10.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F11) {func11.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F12) {func12.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_INSERT) {insert.toggle(isPressed);}
		if (keycode == KeyEvent.VK_DELETE) {delete.toggle(isPressed);}
		if (keycode == KeyEvent.VK_HOME) {home.toggle(isPressed);}
		if (keycode == KeyEvent.VK_END) {end.toggle(isPressed);}
		if (keycode == KeyEvent.VK_PAGE_UP) {pageUp.toggle(isPressed);}
		if (keycode == KeyEvent.VK_PAGE_DOWN) {pageDown.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_Q) {drop.toggle(isPressed);}
		
		if (keycode == inventoryKey) {inventory.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_C) {crafting.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_M) {miniMap.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_L) {light.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_F) {collect.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_T) {techTree.toggle(isPressed);}
	}
	
	public class KeyRebindWindow extends JPanel implements ActionListener, KeyListener {
		JFrame frame = new JFrame();
		JLabel currentLabel;
		
		JButton inventoryButton;
		JButton clearButton;
		
		int currentControl;
		
		public KeyRebindWindow() {
			initPanel();
			initFrame();
		}
		
		public void initPanel() {
			setPreferredSize(new Dimension(300, 600));
			setBackground(Color.LIGHT_GRAY);
			
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			
			currentControl = 0;
			
			currentLabel = new JLabel("No control selected");
			currentLabel.setFont(MediaLibrary.getFontFromLibrary("Heading"));
			add(currentLabel);
			
			clearButton = new JButton("Clear");
			add(clearButton);
			
			inventoryButton = new JButton("Inventory");
			add(inventoryButton);
		}
		
		public void initFrame() {
			frame.setTitle("Rebind keys");
			frame.setBackground(Color.BLACK);
			frame.setResizable(true);
			frame.add(this);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	        frame.setIconImage(loadImage("iconSelect.png"));
			frame.pack();
			frame.setVisible(true);
			setVisible(true);
		}

		public void keyPressed(KeyEvent arg0) {
			
		}

		public void keyReleased(KeyEvent arg0) {
			
		}

		public void keyTyped(KeyEvent arg0) {
			
		}

		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource() == inventoryButton) {
				currentControl = 1;
				currentLabel.setText("Inventory selected");
			}
		}
	}

	public void mouseClicked(MouseEvent arg0) {
		Dimension clickLocation = gameIn.convertCoordinates(arg0.getX(), arg0.getY());
		int clickX = clickLocation.width;
		int clickY = clickLocation.height;
		if (arg0.getButton() == MouseEvent.BUTTON1) {
			if (controlScheme == ControlScheme.BASIC_CRAFTING) {
				gameIn.basicCraftingGUI.handleClick(arg0.getX(), arg0.getY());
			} else if (controlScheme == ControlScheme.PAUSE_MENU) {
				gameIn.pauseMenuGUI.handleClick(arg0.getX(), arg0.getY());
			} else if (controlScheme == ControlScheme.TECH_TREE) {
				gameIn.techTreeGUI.handleClick(arg0.getX(), arg0.getY(), gameIn.player.inventory);
			}
		}
		if (arg0.getButton() == MouseEvent.BUTTON2) {
			
		}
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			rightClickOrDrag(clickX, clickY);
		}
	}

	public void rightClickOrDrag(int clickX, int clickY) {
		if (gameIn.level.getTile(clickX >> 5, clickY >> 5).getId() == Tile.TNT.getId()) {
			gameIn.level.explode(clickX >> 5, clickY >> 5);
		}
		if (gameIn.player.inventory.getTileFromHotbar() != 2) {
			gameIn.level.placeTile(clickX, clickY);
			gameIn.player.queueMeleeImage(20);
		}
		if (gameIn.player.inventory.getActiveItem() != null) {			
			if (gameIn.player.inventory.getActiveItem().getClass() == Ingredient.class) {
				Ingredient i = (Ingredient) gameIn.player.inventory.getActiveItem();
				// Place crop seeds
				if (((Ingredient) i).getItemID() == 3 && gameIn.level.getTile(clickX >> 5, clickY >> 5).getId() == Tile.SAND.getId()) {
					gameIn.level.placePlant(clickX >> 5, clickY >> 5);
					i.removeQuantity(1);
				}
				// Place tree
				if (((Ingredient) i).getItemID() == 6 && gameIn.level.getTile(clickX >> 5, clickY >> 5).getId() == Tile.GRASS.getId()) {
					gameIn.level.generateTree(clickX >> 5, clickY >> 5);
					i.removeQuantity(1);
				}
			}
			
			// Place entity
			if (gameIn.player.inventory.getActiveItem().getClass() == InventoryEntity.class) {
				InventoryEntity j = ((InventoryEntity) gameIn.player.inventory.getActiveItem());
				int x = 0, y = 0;
				if (j.entityIndex == 7) {
					x = clickX;
					y = clickY;
				} else {
					x = clickX >> 5;
					y = clickY >> 5;
				}
				if (j.checkGenerationConditions(gameIn.level, x, y)) {
					for (Entity e : gameIn.level.getEntities()) {
						if (x == e.x && y == e.y) {
							FileUtilities.log("Attempted to place an electrical device on an existing device at x = " + x + ", y = " + y, false);
							return;
						}
					}
					gameIn.level.addEntity(j.generateEntity(gameIn.level, x, y));
					gameIn.player.inventory.clearActiveItem();
				}
			}
			
			// Shoot bow
			if (gameIn.player.inventory.getActiveItem() != null && gameIn.player.inventory.getActiveItem().getClass() == Bow.class && ((Bow) gameIn.player.inventory.getActiveItem()).isAvailable()) {
				if (((InventoryTool) gameIn.player.inventory.getActiveItem()).getImageID() == 10009 && gameIn.level != null) { // This is pretty lazy
					for (InventoryItem i : gameIn.player.inventory.getItems()) {
						if (i != null && i.getClass() == Ingredient.class && ((Ingredient) i).getItemID() == 16 && ((Ingredient) i).getQuantity() > 0) {
							//System.out.println("bow");
							int x = 0, y = 0;
							x = gameIn.player.x;
							y = gameIn.player.y + gameIn.player.spriteHeight / 2;
							double vX = 0, vY = 0;
							double verticalDelta = (clickY - y) / 32D;
							vY = Math.min(3.5, Math.pow(Math.abs(verticalDelta), 0.375)) * Math.signum(verticalDelta);
							if (gameIn.player.getMovingDir() == 3) {
								x += gameIn.player.spriteWidth;
								vX = 3.5 - Math.abs(vY);
							} else {
								vX = -3.5 + Math.abs(vY);
							}
							gameIn.level.addEntity(new Projectile(1, gameIn.level, true, x, y, vX, vY));
							if (gameIn.player.inventory.getActiveItem() != null && gameIn.player.inventory.getActiveItem().getClass() == Bow.class) ((Bow) gameIn.player.inventory.getActiveItem()).resetCooldown();
							((Ingredient) i).removeQuantity(1);
							return;
						}
					}
				}
			}
		}
	}
	
	public void updateTileInfo(int clickX, int clickY) {
		if (gameIn.level != null) {
			gameIn.selectedTileX = clickX >> 5;
			gameIn.selectedTileY = clickY >> 5;
			if (gameIn.level.getTile(clickX >> 5, clickY >> 5).getId() != 2 && gameIn.level.isVisible(clickX >> 5, clickY >> 5)) {
				gameIn.displayTileInfo = true;
				gameIn.displayTile = gameIn.level.getTile(clickX >> 5, clickY >> 5);
				gameIn.tileDurability = gameIn.level.getDurability(clickX >> 5, clickY >> 5);
			}
			else gameIn.displayTileInfo = false;
		}
	}
	
	public void mouseEntered(MouseEvent arg0) {
				
	}

	public void mouseExited(MouseEvent arg0) {
		
	}

	public void mousePressed(MouseEvent arg0) {
		if (controlScheme == ControlScheme.INVENTORY) {
			int clickX = arg0.getX();
			int clickY = arg0.getY();
			
			for (int i = 0; i < gameIn.player.inventory.getSlots(); i++) {
				if (clickX > gameIn.player.inventory.getTopLeft(i).width && clickX < gameIn.player.inventory.getTopLeft(i).width + gameIn.player.inventory.getIconSize()) {
					if (clickY > gameIn.player.inventory.getTopLeft(i).height && clickY < gameIn.player.inventory.getTopLeft(i).height + gameIn.player.inventory.getIconSize()) {
						if (!gameIn.player.inventory.isFreeItemSet()) {
							gameIn.player.inventory.setFreeItem(i);
							mouseJustPressed = true;
						}
					}
				}
			}
			
			clickX = arg0.getX();
			clickY = arg0.getY();
			gameIn.player.inventory.setMouseLocation(new Dimension(clickX, clickY));
		} else if (controlScheme == ControlScheme.GAMEPLAY) {
			Dimension clickLocation = gameIn.convertCoordinates(arg0.getX(), arg0.getY());
			int clickX = clickLocation.width;
			int clickY = clickLocation.height;
			if (SwingUtilities.isLeftMouseButton(arg0)) {
				lastDestructibleX = clickX >> 5;
				lastDestructibleY = clickY >> 5;
				gameIn.level.checkLeftClickEntityCollision(clickX, clickY);
				gameIn.level.checkLeftClickTileCollision(clickX >> 5, clickY >> 5);
			} else if (SwingUtilities.isRightMouseButton(arg0)) {
				gameIn.level.checkRightClickEntityCollision(clickX, clickY);
			}
		}
		
		if (SwingUtilities.isLeftMouseButton(arg0)) leftButtonHeld = true;
		if (SwingUtilities.isRightMouseButton(arg0)) rightButtonHeld = true;
	}

	public void mouseReleased(MouseEvent arg0) {
		if (controlScheme == ControlScheme.INVENTORY) {
			int clickX = arg0.getX();
			int clickY = arg0.getY();
			
			for (int i = 0; i < gameIn.player.inventory.getSlots(); i++) {
				if (clickX > gameIn.player.inventory.getTopLeft(i).width && clickX < gameIn.player.inventory.getTopLeft(i).width + gameIn.player.inventory.getIconSize()) {
					if (clickY > gameIn.player.inventory.getTopLeft(i).height && clickY < gameIn.player.inventory.getTopLeft(i).height + gameIn.player.inventory.getIconSize()) {
						if (mouseJustPressed) {
							mouseJustPressed = false;
							leftButtonHeld = false;
							return;
						}
						gameIn.player.inventory.dropFreeItem(i);
					}
				}
			}
		}
		
		if (SwingUtilities.isLeftMouseButton(arg0)) {
			leftButtonHeld = false;
			gameIn.level.resetDestructibleTile(lastDestructibleX, lastDestructibleY);
			updateTileInfo(lastDestructibleX, lastDestructibleY);
		}
		if (SwingUtilities.isRightMouseButton(arg0)) leftButtonHeld = false;
	}

	public void mouseDragged(MouseEvent arg0) {
		if (controlScheme == ControlScheme.GAMEPLAY) {
			Dimension clickLocation = gameIn.convertCoordinates(arg0.getX(), arg0.getY());
			int clickX = clickLocation.width;
			int clickY = clickLocation.height;
			if (SwingUtilities.isRightMouseButton(arg0)) {
				rightClickOrDrag(clickX, clickY);
			}
			if (SwingUtilities.isLeftMouseButton(arg0)) {
				lastDestructibleX = clickX >> 5;
				lastDestructibleY = clickY >> 5;
				updateTileInfo(clickX, clickY);
			}
		}
		if (controlScheme == ControlScheme.INVENTORY) {
			int clickX = arg0.getX();
			int clickY = arg0.getY();
			gameIn.player.inventory.setMouseLocation(new Dimension(clickX, clickY));
		}
		
		if (SwingUtilities.isLeftMouseButton(arg0))	leftButtonHeld = true;
	}

	public void mouseMoved(MouseEvent arg0) {
		if (controlScheme == ControlScheme.GAMEPLAY) {
			Dimension clickLocation = gameIn.convertCoordinates(arg0.getX(), arg0.getY());
			int clickX = clickLocation.width;
			int clickY = clickLocation.height;
	
			updateTileInfo(clickX, clickY);
						
			gameIn.level.setMousePositionOnPanel(arg0.getX(), arg0.getY());
		}
		if (controlScheme == ControlScheme.INVENTORY) {
			int clickX = arg0.getX();
			int clickY = arg0.getY();
			gameIn.player.inventory.setMouseLocation(new Dimension(clickX, clickY));
		}
	}

	public void mouseWheelMoved(MouseWheelEvent arg0) {
		int scrollDelta = arg0.getWheelRotation();
		if (controlScheme == ControlScheme.GAMEPLAY) {
			if (scrollDelta > 0) for (int i = 0; i < scrollDelta; i++)  {
				gameIn.player.inventory.incrementHotbarSlot();
				gameIn.player.queuePlayerModelUpdate();
			}
			if (scrollDelta < 0) for (int i = 0; i < -scrollDelta; i++) {
				gameIn.player.inventory.decrementHotbarSlot();
				gameIn.player.queuePlayerModelUpdate();
			}
		} else if (controlScheme == ControlScheme.BASIC_CRAFTING) {
			gameIn.basicCraftingGUI.handleScroll(scrollDelta);
		} else if (controlScheme == ControlScheme.PAUSE_MENU) {
			gameIn.pauseMenuGUI.handleScroll(scrollDelta);
		} else if (controlScheme == ControlScheme.TECH_TREE) {
			gameIn.techTreeGUI.handleScroll(scrollDelta);
		} else if (controlScheme == ControlScheme.INVENTORY) {
			if (scrollDelta > 0) for (int i = 0; i < scrollDelta; i++)  {
				gameIn.player.inventory.incrementHotbarSlot();
				gameIn.player.queuePlayerModelUpdate();
			}
			if (scrollDelta < 0) for (int i = 0; i < -scrollDelta; i++) {
				gameIn.player.inventory.decrementHotbarSlot();
				gameIn.player.queuePlayerModelUpdate();
			}
		}
	}
}
