package Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.ImageObserver;
import java.util.Observer;

import Frame.GameLoop;
import Frame.InputHandler;
import Frame.InputHandler.ControlScheme;
import Frame.Level;
import Libraries.MediaLibrary;
import Tiles.Tile;
import UI.*;
import Tiles.SolidTile;

import static Utilities.PhysicsUtilities.*;
import static Utilities.FileUtilities.*;

public class Player extends Mob {
	private GameLoop game;
	private InputHandler controls;
	public Inventory inventory;
	
	protected boolean shouldMove = true;
	protected boolean shouldFly = false;
	protected boolean isSwimming = false;
	protected boolean ground = false;
	protected boolean damaged = false;
	private String username;
	
	public int spriteWidth = 64, spriteHeight = 128;
	
	public double dX, dY;
	public double vX, vY;
	public double aX, aY;
	
	public boolean drawInfo;
	public boolean toggleInfo;
	public boolean toggleInventory;
	public boolean toggleCrafting;
	public boolean toggleCreativeBlocks;
	public boolean toggleCreativeIngredients;
	public boolean toggleCreativeTools;
	public boolean toggleCreativeFlying;
	private boolean toggleRefresh;
	
	public boolean canJump = true;
	public double maxVelocityX = 3, maxVelocityY = 20;
	
	public double airResistance = 0.001;
	public double friction = 0;
	
	public double mass = 100;
	
	public final Font USERNAME = new Font("Comic Sans MS", Font.PLAIN, 16);
	public final Font NormalFont = new Font("Console", Font.PLAIN, 12);
	public final Color white = Color.decode("#FFFFFF");
	public final Color text = Color.decode("#88FF77");
	
	public Player(Level level, String name, int x, int y, InputHandler input) {
		super(level, name, x, y, 1, 100);
		this.username = name;
		this.controls = input;
		game = level.getGameLoop();
		dX = x;
		dY = y;
		inventory = new Inventory(100, 80, controls);
	}

	public boolean hasCollided(int deltaX, int deltaY) {
		
		return false;
	}

	public void tick() {
		if (health <= 0.0) health = 0.0;
		if (health <= baseHealth) {
			heal(0.005 + Math.random() * 0.002);
		}
		calculatePhysics();
		if (controls.func3.isPressed()) {
			if (toggleInfo) toggleInfo();
			toggleInfo = false;
		} else {
			toggleInfo = true;
		}
		if (controls.inventory.isPressed()) {
			if (toggleInventory) inventory.toggleActive();
			toggleInventory = false;
		} else {
			toggleInventory = true;
		}
		if (controls.esc.isPressed()) {
			if (inventory.isActive()) inventory.setActive(false);
			if (game.basicCraftingGUI.isActive()) game.basicCraftingGUI.setActive(false);
		}
		if (controls.crafting.isPressed() && !inventory.isActive()) {
			if (toggleCrafting) game.basicCraftingGUI.toggleActive();
			toggleCrafting = false;
		} else {
			toggleCrafting = true;
		}
		if (controls.func5.isPressed()) {
			if (toggleCreativeBlocks) {
				for (int i = 3; i <= 13; i++) {
					inventory.addItem(new InventoryTile(i, inventory.getStackSize()/8));
				}
			}
			toggleCreativeBlocks = false;
		} else {
			toggleCreativeBlocks = true;
		}
		if (controls.func6.isPressed()) {
			if (toggleCreativeIngredients) {
				for (int i = 0; i <= 1; i++) {
					inventory.addItem(new Ingredient(i, inventory.getStackSize()/8));
				}
			}
			toggleCreativeIngredients = false;
		} else {
			toggleCreativeIngredients = true;
		}
		if (controls.func7.isPressed()) {
			if (toggleCreativeTools) {
				inventory.addItem(new Pickaxe(10000, 25.0, 200000.0, 175000.0, "Steel Pickaxe"));
				inventory.addItem(new Shovel(10001, 25.0, 200000.0, 175000.0, "Steel Shovel"));
				inventory.addItem(new Axe(10002, 25.0, 200000.0, 175000.0, "Steel Shovel"));
			}
			toggleCreativeTools = false;
		} else {
			toggleCreativeTools = true;
		}
		
		if (controls.func8.isPressed()) {
			if (toggleCreativeFlying) {
				shouldFly = !shouldFly;
			}
			toggleCreativeFlying = false;
		} else {
			toggleCreativeFlying = true;
		}
		
		if (controls.func12.isPressed()) {
			if (toggleRefresh) {
				for (int i = 0; i <= 1; i++) {
					game.resetWindow();
				}
			}
			toggleRefresh = false;
		} else {
			toggleRefresh = true;
		}
		
		if (inventory.isActive()) {
			controls.setControlScheme(InputHandler.ControlScheme.INVENTORY);
			game.disableAllGUIs();
		} else if (game != null && game.basicCraftingGUI != null && game.basicCraftingGUI.isActive()) {
			controls.setControlScheme(InputHandler.ControlScheme.BASIC_CRAFTING);
		}
		else controls.setControlScheme(InputHandler.ControlScheme.GAMEPLAY);
		
		inventory.tick();
		
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY) {
			if (controls.leftButtonHeld) {
				//System.out.println("" + Math.abs((controls.lastDestructibleX << 5) - x) + ", " + Math.abs((controls.lastDestructibleY << 5) - y));
				if (Math.abs((controls.lastDestructibleX << 5) - (x + 4)) < 128 && Math.abs((controls.lastDestructibleY << 5) - (y + spriteHeight / 3)) < 128 + 64 - 8) {
					double j = 0.1;
					if (inventory.getActiveItem() != null && inventory.getActiveItem().getClass().getSuperclass() == InventoryTool.class && level.getTile(controls.lastDestructibleX, controls.lastDestructibleY).getId() != Tile.SKY.getId()) {
						j = ((InventoryTool) inventory.getActiveItem()).getHardness() / 2;
						for (int i = 0; i < ((InventoryTool) inventory.getActiveItem()).getEffectiveTiles().length; i++) {
							if (level.getTile(controls.lastDestructibleX, controls.lastDestructibleY).getId() == ((InventoryTool) inventory.getActiveItem()).getEffectiveTiles()[i]) {
								j = ((InventoryTool) inventory.getActiveItem()).getHardness(); 
								//System.out.println("Effective Hit");
							}
						}
						for (int i = 0; i < ((InventoryTool) inventory.getActiveItem()).getNeutralTiles().length; i++) {
							if (level.getTile(controls.lastDestructibleX, controls.lastDestructibleY).getId() == ((InventoryTool) inventory.getActiveItem()).getNeutralTiles()[i]) {
								j = ((InventoryTool) inventory.getActiveItem()).getHardness() / 2; 
								//System.out.println("Neutral Hit");
							}
						}
						for (int i = 0; i < ((InventoryTool) inventory.getActiveItem()).getIneffectiveTiles().length; i++) {
							if (level.getTile(controls.lastDestructibleX, controls.lastDestructibleY).getId() == ((InventoryTool) inventory.getActiveItem()).getIneffectiveTiles()[i]) {
								j = ((InventoryTool) inventory.getActiveItem()).getHardness() / 10; 
								//System.out.println("Ineffective Hit");
							}
						}
						((InventoryTool) inventory.getActiveItem()).removeDurability(1 / j);
					} else {
						j = 0.1;
					}
					level.destructible(controls.lastDestructibleX, controls.lastDestructibleY, j);
					controls.updateTileInfo(controls.lastDestructibleX << 5, controls.lastDestructibleY << 5);
				}
			}
		}
	}
	
	public int getMovingDir() {
		return this.movingDir;
	}
	
	public void calculatePhysics() {
		aY = 0;
		aX = 0;
		boolean dirL = false;
		boolean dirR = false;
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && !collisionLeft(x, y, spriteWidth, spriteHeight, level) && controls.left.isPressed()) { 
			aX = -.05;
			dirL = true;
		}
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && !collisionRight(x, y, spriteWidth, spriteHeight, level) && controls.right.isPressed()) { 
			aX = .05;
			dirR = true;
		}
		if (!(dirL && dirR) && dirL) {
			this.movingDir = 2;
		} else if (!(dirL && dirR) && dirR) {
			this.movingDir = 3;
		}
		
		dirL = false;
		dirR = false;
		
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.left.isPressed() && controls.right.isPressed()) aX = 0;
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && !collisionAbove(x, y, spriteWidth, spriteHeight, level) && (collisionBelow(x, y, spriteWidth, spriteHeight, level) || shouldFly) && controls.up.isPressed()) { 
			if (shouldFly) {
				vY -= 0.125;
				if (controls.down.isPressed()) vY = 0;
			} else if (canJump) {
				vY = -2.5;
				canJump = false;
			}
		}
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.up.isPressed() && controls.down.isPressed()) aY = 0;
		if (!collisionBelow(x, y, spriteWidth, spriteHeight, level) || controls.down.isPressed() && arePlatformsBelow(x, y, spriteWidth, spriteHeight, level) 
				|| (!controls.up.isPressed() && arePlatformsAbove(x, y, spriteWidth, spriteHeight, level) & vY < 0) ) { 
			aY = .025;
			canJump = true;
			if (controls.getControlScheme() != InputHandler.ControlScheme.GAMEPLAY || (!controls.left.isPressed() && !controls.right.isPressed())) {
				if (vX > 0) {
					aX -= airResistance * vX;
				} else {
					aX += airResistance * -vX;
				}
			}
		}
		if (collisionBelow(x, y, spriteWidth, spriteHeight, level) && (controls.getControlScheme() != InputHandler.ControlScheme.GAMEPLAY || (!controls.left.isPressed() && !controls.right.isPressed()))) {
			if (vX > 0) {
				Tile t = findTile(x + 1, y + spriteHeight, level);
				Tile u = findTile(x + (spriteWidth / 2), y + spriteHeight, level);
				Tile v = findTile(x + spriteWidth - 1, y + spriteHeight, level);
				double partialFriction = 0;
				if (t != null && t.isSolid()) partialFriction += ((SolidTile) t).getFriction();
				if (u != null && u.isSolid()) partialFriction += ((SolidTile) u).getFriction();
				if (v != null && v.isSolid()) partialFriction += ((SolidTile) v).getFriction();
				friction = partialFriction / 3;
				aX -= friction * mass * 0.001 * vX;
			} else {
				Tile t = findTile(x + 1, y + spriteHeight, level);
				Tile u = findTile(x + (spriteWidth / 2), y + spriteHeight, level);
				Tile v = findTile(x + spriteWidth - 1, y + spriteHeight, level);
				double partialFriction = 0;
				if (t != null && t.isSolid()) partialFriction += ((SolidTile) t).getFriction();
				if (u != null && u.isSolid()) partialFriction += ((SolidTile) u).getFriction();
				if (v != null && v.isSolid()) partialFriction += ((SolidTile) v).getFriction();
				friction = partialFriction / 3;
				aX += friction * mass * 0.001 * -vX;
			}
		} else {
			friction = 0;
		}
		
		if (aX < 0.00001 && aX > -0.00001) aX = 0;
		if (aY < 0.00001 && aY > -0.00001) aY = 0;
		
		if ((aX < 0 && !collisionLeft(x, y, spriteWidth, spriteHeight, level)) || (aX > 0 && !collisionRight(x, y, spriteWidth, spriteHeight, level))) vX += aX;
		if ((aY < 0 && !collisionAbove(x, y, spriteWidth, spriteHeight, level)) || (aY > 0 && (!collisionBelow(x, y, spriteWidth, spriteHeight, level) 
				|| (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.down.isPressed() && arePlatformsBelow(x, y, spriteWidth, spriteHeight, level) ))
				|| (!controls.up.isPressed() && arePlatformsAbove(x, y, spriteWidth, spriteHeight, level) & vY < 0))) vY += aY;

		if ((vX < 0 && collisionLeft(x, y, spriteWidth, spriteHeight, level)) || (vX > 0 && collisionRight(x, y, spriteWidth, spriteHeight, level))) {
			if (vX > 0) {
				if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.right.isPressed()) {
					vX = 0;
				} else {
					vX = - Math.log(vX) / 4;
					if (Math.log(vX) - vX < .0000005) vX = 0;
				}
			} else {
				if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.left.isPressed()) {
					vX = 0;
				} else {
					vX = Math.log(-vX) / 4;
					if (Math.log(-vX) + vX < .0000005) vX = 0;
				}
			}
		}
		if ((vY < 0 && collisionAbove(x, y, spriteWidth, spriteHeight, level)) || (vY > 0 && (collisionBelow(x, y, spriteWidth, spriteHeight, level) 
				&& !controls.down.isPressed() || ( controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.down.isPressed() && !arePlatformsBelow(x, y, spriteWidth, spriteHeight, level))))) {
			if (vY < 0) {
				vY = Math.log(-vY) / 4;
			} else {
				if (vY > 5) {
					damage(Math.pow(Math.pow(99, 1D/15D), vY - 5));
				}
				vY = 0;
			}
		}
		if (vX > 0 && vX > maxVelocityX) {
			if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.right.isPressed())	vX = Math.log(vX) + maxVelocityX;
			else {
				vX = maxVelocityX + ((vX - maxVelocityX) / 1.875);
			}
		}
		if (vX < 0 && vX < -maxVelocityX) {
			if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.left.isPressed())	vX = -Math.log(-vX) - maxVelocityX;
			else {
				vX = -maxVelocityX + ((vX + maxVelocityX) / 1.875);
			}
		}
		if (vY > 0 && vY > maxVelocityY) {
			vY = maxVelocityY;
		}
		if (vY < 0 && vY < -maxVelocityY) {
			vY = -maxVelocityY;
		}
		
		if (vX < 0.001 && vX > -0.001) vX = 0;
		if (vY < 0.001 && vY > -0.001) vY = 0;
		
		if ((vX < 0 && !collisionLeft(x, y, spriteWidth, spriteHeight, level)) || (vX > 0 && !collisionRight(x, y, spriteWidth, spriteHeight, level))) dX += vX;
		if ((vY < 0 && !collisionAbove(x, y, spriteWidth, spriteHeight, level)) || (vY > 0 && (!collisionBelow(x, y, spriteWidth, spriteHeight, level) 
				|| ( controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.down.isPressed() && arePlatformsBelow(x, y, spriteWidth, spriteHeight, level))))) dY += vY;
		
		int deltaX = Math.toIntExact(Math.round(Math.floor(dX))) - x;
		if (deltaX > 0) {
			for (int i = 0; i < deltaX; i++) {
				if (level.getGameLoop() != null) level.getGameLoop().repaint();
				x++;
			}
		} else {		
			for (int i = 0; i < -deltaX; i++) {
				if (level.getGameLoop() != null) level.getGameLoop().repaint();
				x--;
			}
		}
		int deltaY = Math.toIntExact(Math.round(Math.floor(dY))) - y;
		if (deltaY > 0) {
			for (int i = 0; i < deltaY; i++) {
				if (level.getGameLoop() != null) level.getGameLoop().repaint();
				y++;
			}
		} else {		
			for (int i = 0; i < -deltaY; i++) {
				if (level.getGameLoop() != null) level.getGameLoop().repaint();
				y--;
			}
		}
		//x = Math.toIntExact(Math.round(Math.floor(dX)));
		//y = Math.toIntExact(Math.round(Math.floor(dY)));
	}

	public void printMovementInfo(Graphics g, int drawX, int drawY) {
		g.setColor(Color.GREEN);
		g.drawString("Mass: " + mass + " kg", drawX, drawY);
		g.drawString("Acceleration: " + Math.sqrt(Math.pow(aX, 2) + Math.pow(aY, 2)) + " pixels/s^2", drawX, drawY += 16);
		g.drawString("   Horizontal: " + aX + " pixels/s^2", drawX, drawY += 16);
		g.drawString("   Vertical: " + aY + " pixels/s^2", drawX, drawY += 16);
		g.drawString("Velocity: " + Math.sqrt(Math.pow(vX, 2) + Math.pow(vY, 2)) + " pixels/s", drawX, drawY += 16);
		g.drawString("   Horizontal: " + vX + " pixels/s", drawX, drawY += 16);
		g.drawString("   Vertical: " + vY + " pixels/s", drawX, drawY += 16);
		g.drawString("Collisions: ", drawX, drawY += 16);
		g.drawString("   Left: " + collisionLeft(x, y, spriteWidth, spriteHeight, level) + ", Right: " + collisionRight(x, y, spriteWidth, spriteHeight, level), drawX, drawY += 16);
		g.drawString("   Above: " + collisionAbove(x, y, spriteWidth, spriteHeight, level) + ", Below: " + collisionBelow(x, y, spriteWidth, spriteHeight, level), drawX, drawY  += 16);
		g.drawString("Platforms: ", drawX, drawY  += 16);
		g.drawString("   Above: " + arePlatformsAbove(x, y, spriteWidth, spriteHeight, level) + ", Below: " + arePlatformsBelow(x, y, spriteWidth, spriteHeight, level), drawX, drawY  += 16);
		g.drawString("Air Resistance: " + airResistance * 100 + "%, " + Math.abs(airResistance * vX) + " pixels/s^2", drawX, drawY += 16);
		g.drawString("Coefficient of friction: " + friction, drawX, drawY += 16);
		g.drawString("Frictional acceleration: " + Math.abs(friction * mass * 0.001 * vX) + " pixels/s^2", drawX, drawY += 16);
		g.drawString("Position (exact): " + dX + "," + dY, drawX, drawY += 16);
		g.drawString("Position (round): " + x + "," + y, drawX, drawY += 16);
		g.drawString("Position (tiles): " + x/32 + "," + y/32, drawX, drawY += 16);
		g.drawString("Health: " + health + "/" + baseHealth, drawX, drawY += 16);
	}
	
	public void toggleInfo() {
		drawInfo = !drawInfo;
	}
	
	public void setInfo(boolean b) {
		drawInfo = b;
	}
	
	public void draw(Graphics g) {
		if (drawInfo) printMovementInfo(g, 64, 172);
		if (inventory.isActive()) {
			game.drawHUD = false;
			inventory.draw(g, game.drawResolution.width, game.drawResolution.height, game);
		} else {
			game.drawHUD = true;
		}
		if (controls.getControlScheme() == ControlScheme.GAMEPLAY) {
			inventory.drawHotbar(g, game.drawResolution.width, game.drawResolution.height, game);
		}		
	}
	
	public void drawPlayerModel(Graphics g, int xOffset, int yOffset, ImageObserver observer) {
		g.setColor(Color.GRAY);
		
		if (getMovingDir() == 2) {
			g.drawImage(MediaLibrary.getImageFromLibrary(7500), x - game.xOffset + spriteWidth, y - game.yOffset - 3, -spriteWidth, spriteHeight, observer);
			if (shouldFly) g.drawImage(MediaLibrary.getImageFromLibrary(7499), x - game.xOffset + spriteWidth, y - game.yOffset - 3, -spriteWidth, spriteHeight, observer);
		} else if (getMovingDir() == 3) {
			g.drawImage(MediaLibrary.getImageFromLibrary(7500), x - game.xOffset, y - game.yOffset - 3, spriteWidth, spriteHeight, observer);
			if (shouldFly) g.drawImage(MediaLibrary.getImageFromLibrary(7499), x - game.xOffset, y - game.yOffset - 3, spriteWidth, spriteHeight, observer);
		}
		
		g.setColor(Color.DARK_GRAY);
		g.fillOval(32, 32, 128, 128);
		
		Graphics2D g2 = (Graphics2D) g;
		Stroke back1 = new BasicStroke(5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {4.0f, 8.0f}, 0.0f);
		Stroke h1 = new BasicStroke(20);
		Stroke h2 = new BasicStroke(4);
		Stroke h3 = new BasicStroke(6);
		g2.setStroke(h1);
		g2.setColor(Color.LIGHT_GRAY);
		g2.drawArc(32, 32, 128, 128, 90, 360);

		g2.setStroke(h2);
		g2.setColor(Color.BLUE);
		g2.drawArc(32, 32, 128, 128, 90, 360);
		g2.setStroke(back1);
		g2.setColor(Color.GRAY);
		g2.drawArc(32, 32, 128, 128, 90, 360);
		g2.setColor(Color.RED);
		g2.setStroke(h3);
		g2.drawArc(32, 32, 128, 128, 90, -(int) Math.round(3.6 * health));
		if (health > 100) {
			g2.setColor(Color.getHSBColor((float) ((health%100)/100), 1F, 0.5F));
			g2.drawArc(32, 32, 128, 128, 90, -(int) Math.round(3.6 * (health%100)));
		}
		
		g2.setStroke(h1);
		g2.setColor(Color.LIGHT_GRAY);
		g2.drawArc(52, 52, 88, 88, 90, 360);
		g2.setColor(Color.BLUE);
		g2.setStroke(h2);
		g2.drawArc(52, 52, 88, 88, 90, 360);
		g2.setColor(Color.GRAY);
		g2.setStroke(back1);
		g2.drawArc(52, 52, 88, 88, 90, 360);
		g2.setStroke(h2);
		g2.setColor(Color.RED);
		g2.drawArc(52, 52, 88, 88, 90 - ((int) Math.round(36 * (health % 10)) - 7), 14);
		
		g.setColor(Color.RED);
		int hr = (int) Math.round(health);
		if (hr == 0) hr = 1;
		g.setFont(MediaLibrary.getFontFromLibrary("Health"));
		String hrs = "" + hr;
		if (hrs.length() == 1) {
			hrs = "00" + hr;
		} else if (hrs.length() == 2) {
			hrs = "0" + hr;
		}
		g.drawString(hrs, 69, 106);
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String str) {
		username = str;
	}

	public void damage() {
		health -= 1.0;
	}
	
	public void damage(double d) {
		health -= d;
	}
	
	public void heal() {
		health += 1.0;
	}
	
	public void heal(double d) {
		health += d;
	}
	
	public void healTo(double d) {
		health = d;
	}
	
	public double getHealth() {
		return health;
	}

	public boolean checkConflict() {
		return false;
	}
}
