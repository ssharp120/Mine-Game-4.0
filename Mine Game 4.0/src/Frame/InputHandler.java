package Frame;

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

import Entities.Furniture;
import Libraries.MediaLibrary;
import Tiles.BackgroundDestructibleTile;
import Tiles.DestructibleTile;
import Tiles.Tile;
import UI.InventoryEntity;
import UI.InventoryTile;
import static Utilities.FileUtilities.*;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	@SuppressWarnings("unused")
	
	private int inventoryKey = KeyEvent.VK_I;
	
	public enum ControlScheme {
		GAMEPLAY, MENU, INVENTORY, BASIC_CRAFTING;
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
	
	public boolean leftButtonHeld;
	public int lastDestructibleX, lastDestructibleY;
	
	public Key up = new Key();
	public Key down = new Key();
	public Key left = new Key();
	public Key right = new Key();
	public Key arrowUp = new Key();
	public Key arrowDown = new Key();
	public Key arrowLeft = new Key();
	public Key arrowRight = new Key();
	public Key esc = new Key();
	public Key inventory = new Key();
	public Key crafting = new Key();
	
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
	
	public Key shift = new Key();
	public Key ctrl = new Key();
	public Key alt = new Key();
	
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
	
	public Key dispose = new Key();
	
	public void keyPressed(KeyEvent arg0) {
		toggleKey(arg0.getKeyCode(), true);
		if (arg0.getKeyCode() == KeyEvent.VK_W) {up.inc();}
		if (arg0.getKeyCode() == KeyEvent.VK_S) {down.inc();}
		if (arg0.getKeyCode() == KeyEvent.VK_A) {left.inc();}
		if (arg0.getKeyCode() == KeyEvent.VK_D) {right.inc();}
	}

	public void keyReleased(KeyEvent arg0) {
		toggleKey(arg0.getKeyCode(), false);
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
		if (keycode == KeyEvent.VK_ALT) {alt.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_F1) {func1.toggle(isPressed);}
		if (keycode == KeyEvent.VK_F2) {new KeyRebindWindow();}
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
		
		if (keycode == KeyEvent.VK_ESCAPE) {esc.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_Q) {dispose.toggle(isPressed);}
		
		if (keycode == inventoryKey) {inventory.toggle(isPressed);}
		
		if (keycode == KeyEvent.VK_C) {crafting.toggle(isPressed);}
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
			}
		}
		if (arg0.getButton() == MouseEvent.BUTTON2) {
			
		}
		if (arg0.getButton() == MouseEvent.BUTTON3) {
			rightClickOrDrag(clickX, clickY);
		}
	}

	public void rightClickOrDrag(int clickX, int clickY) {
		if (gameIn.player.inventory.getTileFromHotbar() != 2) {
			placeTile(clickX, clickY);
		}
		if (gameIn.player.inventory.getActiveItem() != null && gameIn.player.inventory.getActiveItem().getClass() == InventoryEntity.class) {
			InventoryEntity j = ((InventoryEntity) gameIn.player.inventory.getActiveItem());
			gameIn.level.addEntity(j.generateEntity(gameIn.level, clickX >> 5, clickY >> 5));
			gameIn.player.inventory.clearActiveItem();
		}
	}
	
	public void placeTile(int clickX, int clickY) {
		if (gameIn.player != null && gameIn.level != null) {
			if (gameIn.level.getTile(clickX >> 5, clickY >> 5).getId() == 2) {
				if (gameIn.level.getTile((clickX >> 5) + 1, clickY >> 5).getId() == 2 && gameIn.level.getTile((clickX >> 5) - 1, clickY >> 5).getId() == 2 && gameIn.level.getTile(clickX >> 5, (clickY >> 5) + 1).getId() == 2 && gameIn.level.getTile(clickX >> 5, (clickY >> 5) - 1).getId() == 2) return;
				
				int t = gameIn.player.inventory.getTileFromHotbar();
				
				//System.out.println("Player x: Left: " + gameIn.player.x + ", Right: " + (gameIn.player.x + gameIn.player.spriteWidth) + ", Tile x: Left: " + ((clickX >> 5) << 5) + ", Right: " + (((clickX >> 5) << 5) + 32));
				if (((clickX >> 5) << 5) + 32 >= gameIn.player.x && (clickX >> 5) << 5 <= gameIn.player.x + gameIn.player.spriteWidth 
						&& ((clickY >> 5) << 5) + 32 >= gameIn.player.y && (clickY >> 5) << 5 <= gameIn.player.y + gameIn.player.spriteHeight
						&& Tile.tiles[t].getClass() != BackgroundDestructibleTile.class) return;
				
				((InventoryTile) gameIn.player.inventory.getActiveItem()).removeQuantity(1);
				gameIn.level.setTile(clickX >> 5, clickY >> 5, t);
				if (Tile.tiles[t].getClass() == DestructibleTile.class) {
					gameIn.level.setDurability(clickX >> 5, clickY >> 5, ((DestructibleTile) Tile.tiles[t]).getBaseDurability());
				}
				if (Tile.tiles[t].getClass() == BackgroundDestructibleTile.class) {
					gameIn.level.setDurability(clickX >> 5, clickY >> 5, ((BackgroundDestructibleTile) Tile.tiles[t]).getBaseDurability());
				}
			}
		}
	}
	
	public void updateTileInfo(int clickX, int clickY) {
		if (gameIn.level != null) {
			if (gameIn.level.getTile(clickX >> 5, clickY >> 5).getId() != 2) {
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
			if (arg0.getButton() == MouseEvent.BUTTON1) {
				Dimension clickLocation = gameIn.convertCoordinates(arg0.getX(), arg0.getY());
				int clickX = clickLocation.width;
				int clickY = clickLocation.height;
				lastDestructibleX = clickX >> 5;
				lastDestructibleY = clickY >> 5;
			}
		}
		
		if (arg0.getButton() == MouseEvent.BUTTON1) leftButtonHeld = true;
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
		
		leftButtonHeld = false;
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
		}
		if (controlScheme == ControlScheme.INVENTORY) {
			int clickX = arg0.getX();
			int clickY = arg0.getY();
			gameIn.player.inventory.setMouseLocation(new Dimension(clickX, clickY));
		}
	}

	public void mouseWheelMoved(MouseWheelEvent arg0) {
		if (controlScheme == ControlScheme.GAMEPLAY) {
			if (arg0.getWheelRotation() > 0) for (int i = 0; i < arg0.getWheelRotation(); i++)  gameIn.player.inventory.incrementHotbarSlot();
			if (arg0.getWheelRotation() < 0) for (int i = 0; i < -arg0.getWheelRotation(); i++) gameIn.player.inventory.decrementHotbarSlot();
		} else if (controlScheme == ControlScheme.BASIC_CRAFTING) {
			gameIn.basicCraftingGUI.handleScroll(arg0.getWheelRotation());
		}
	}
}
