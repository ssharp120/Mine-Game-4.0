package Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Observer;

import Libraries.MediaLibrary;
import SingleplayerClient.GameLoop;
import SingleplayerClient.InputHandler;
import SingleplayerClient.Level;
import SingleplayerClient.InputHandler.ControlScheme;
import Tiles.Tile;
import UI.*;
import Utilities.Electricity;
import Utilities.FileUtilities;
import Tiles.DestructibleTile;
import Tiles.SolidTile;

import static Utilities.PhysicsUtilities.*;
import static Utilities.FileUtilities.*;

public class Player extends Mob implements Electricity {
	// Parent GameLoop and InputHandler
	private GameLoop game;
	private InputHandler controls;
	
	// Owned inventory
	public Inventory inventory;
	
	// State variables
	protected boolean shouldMove = true;
	protected boolean shouldFly = false;
	protected boolean isSwimming = false;
	protected boolean ground = false;
	protected boolean damaged = false;
	protected boolean canJump = true;

	private String username;
	
	public static int spriteWidth = 64;
	public static int spriteHeight = 128;
	
	private BufferedImage playerModel;
	
	public int lastX, lastY;
	private boolean graphicsPositionLocked = false;
	
	private boolean showInventoryItem;
	
	// Position, velocity and acceleration stored as doubles
	public double dX, dY;
	public double vX, vY;
	public double aX, aY;
	
	private double localGravity = 0.025;
	
	public boolean drawInfo;
	
	private int lastImageID = 7001;
	
	// Toggles for function keys and cheats
	private boolean toggleInfo;
	private boolean toggleInventory;
	private boolean toggleCrafting;
	private boolean toggleCreativeCrafting;
	private boolean toggleCreativeTools;
	private boolean toggleCreativeView;
	private boolean toggleCreativeFlying;
	private boolean toggleCreativeEntities;
	private boolean toggleRefresh;
	private boolean toggleMiniMap;
	private boolean toggleTechTree;
	private boolean toggleLighting;
	private boolean queuePlayerModelUpdate = false;
	
	// Physics variables
	protected double airResistance = 0.001, friction = 0, mass = 100;
	public double maxVelocityX = 3, maxVelocityY = 20;
	
	// Oxygen variables
	protected double oxygen = 10D;
	protected boolean oxygenConnected;
	private List<Point> oxyPoints = new ArrayList<Point>();
	
	private double meleeDamage = 0.25;
	private boolean meleeImage = false;
	private int meleeTimer = 0;
	
	private double maximumStoredPower = 100;
	private double storedPower = 100;
	
	private double powerUsage = 0;
	
	public Player(Level level, String name, int x, int y, InputHandler input) {
		super(level, name, x, y, 1, 100, spriteWidth, spriteHeight);
		this.username = name;
		this.controls = input;
		game = level.getGameLoop();
		dX = x;
		dY = y;
		inventory = new Inventory(60, 80, controls, this);
	}

	public void tick() {
		meleeImage = false;
		oxyPoints.removeIf(i -> Math.pow(Math.abs(i.x - x + 32)^2 + Math.abs(i.y * 32 - y + 64)^2, 0.5) > 22);
		
		// Ensure health is never negative
		if (health <= 0.0) health = 0.0;
		
		// Java...
		if (controls.esc.isPressed() && controls.getControlScheme() == ControlScheme.GAMEPLAY) controls.setControlScheme(ControlScheme.PAUSE_MENU); 
		
		powerUsage = 0;
		
		if (oxygen == 0) {
			// Suffocate if out of oxygen
			damage(0.010 + Math.random() * 0.002);
		} else if (oxygen <= 10 && storedPower > 0) {
			powerUsage += 0.015;
			addOxygen(0.005);
		}else if (oxygen <= 25 && storedPower > 0) {
			powerUsage += 0.01;
			addOxygen(0.0005);
		} else if (health < 100){
			// Heal the player if not actively taking damage
			heal(0.005 + Math.random() * 0.002);
		}
		
		calculatePhysics();
		
		handleToggles();
		
		// Modify the current control scheme with the inventory taking precedence over crafting
		if (inventory.isActive()) {
			controls.setControlScheme(InputHandler.ControlScheme.INVENTORY);
			game.disableAllGUIs();
		} else if (game != null && game.basicCraftingGUI != null && game.basicCraftingGUI.isActive()) {
			controls.setControlScheme(InputHandler.ControlScheme.BASIC_CRAFTING);
		}
		else controls.setControlScheme(InputHandler.ControlScheme.GAMEPLAY);
		
		inventory.tick();
		
		if (level.getGameLoop().input.leftButtonHeld) {
			for (Entity e : level.getEntities()) {
				if (e.getClass() == Enemy.class) {
					if (getMovingDir() == 2) {
						if (x - 4 > e.x && x - 4 < e.x + ((Enemy) e).hitboxWidth
								&& (y + spriteHeight / 2 - 48 > e.y && y + spriteHeight / 2 - 48 < e.y + ((Enemy) e).hitboxHeight
										|| y + spriteHeight / 2 - 24 > e.y && y + spriteHeight / 2 - 24 < e.y + ((Enemy) e).hitboxHeight
										|| y + spriteHeight / 2 > e.y && y + spriteHeight / 2 < e.y + ((Enemy) e).hitboxHeight)) {
							((Enemy) e).damage(meleeDamage);
							((Enemy) e).addVelocity(Math.random() * -0.5, Math.random() * -0.375);
							meleeImage = true;
						}
						//x - xOffset - 8, y - yOffset + spriteHeight / 2 - 48, 8, 48);
					} else if (getMovingDir() == 3) {
						if (x + spriteWidth + 4 > e.x && x + spriteWidth + 4 < e.x + ((Enemy) e).hitboxWidth
								&& (y + spriteHeight / 2 - 48 > e.y && y + spriteHeight / 2 - 48 < e.y + ((Enemy) e).hitboxHeight
										|| y + spriteHeight / 2 - 24 > e.y && y + spriteHeight / 2 - 24 < e.y + ((Enemy) e).hitboxHeight
										|| y + spriteHeight / 2 > e.y && y + spriteHeight / 2 < e.y + ((Enemy) e).hitboxHeight)) {
							((Enemy) e).damage(meleeDamage);
							((Enemy) e).addVelocity(Math.random() * 0.5, Math.random() * -0.375);
							meleeImage = true;
						}
						//x - xOffset + spriteWidth, y - yOffset + spriteHeight / 2 - 48, 8, 48);
					}
				}
			}
		}
		
		// Handle tile destruction
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY) {
			if (controls.leftButtonHeld) {
				if (Math.abs((controls.lastDestructibleX << 5) - (x + 4)) < 128 && Math.abs((controls.lastDestructibleY << 5) - (y + spriteHeight / 3)) < 128 + 64 - 8) {
					double j = 0.1;
					if (inventory.getActiveItem() != null && inventory.getActiveItem().getClass().getSuperclass() == InventoryTool.class && level.getTile(controls.lastDestructibleX, controls.lastDestructibleY).getId() != Tile.SKY.getId()) {
						j = ((InventoryTool) inventory.getActiveItem()).getHardness() / 2;
						
						int effectiveTilesLength = ((InventoryTool) inventory.getActiveItem()).getEffectiveTiles().length;
						int neutralTilesLength = ((InventoryTool) inventory.getActiveItem()).getNeutralTiles().length;
						int ineffectiveTilesLength = ((InventoryTool) inventory.getActiveItem()).getIneffectiveTiles().length;
						
						// Check if the targeted tile is categorized as effective, neutral or ineffective based on the currently held tool
						for (int i = 0; i < effectiveTilesLength; i++) {
							if (level.getTile(controls.lastDestructibleX, controls.lastDestructibleY).getId() == ((InventoryTool) inventory.getActiveItem()).getEffectiveTiles()[i]) {
								// Effective hit
								j = ((InventoryTool) inventory.getActiveItem()).getHardness(); 
							}
						}
						for (int i = 0; i < neutralTilesLength; i++) {
							if (level.getTile(controls.lastDestructibleX, controls.lastDestructibleY).getId() == ((InventoryTool) inventory.getActiveItem()).getNeutralTiles()[i]) {
								// Neutral hit
								j = ((InventoryTool) inventory.getActiveItem()).getHardness() / 2; 
							}
						}
						for (int i = 0; i < ineffectiveTilesLength; i++) {
							if (level.getTile(controls.lastDestructibleX, controls.lastDestructibleY).getId() == ((InventoryTool) inventory.getActiveItem()).getIneffectiveTiles()[i]) {
								// Ineffective hit
								j = ((InventoryTool) inventory.getActiveItem()).getHardness() / 10; 
							}
						}
						
						// Damage the currently held tool
						((InventoryTool) inventory.getActiveItem()).removeDurability(Math.pow(j, -2));
					} else {
						j = 0.1;
					}
					
					if (j > 0) j += Math.random() * 0.01;
					
					level.setLeftButtonHeld(true);
					level.destructible(controls.lastDestructibleX, controls.lastDestructibleY, j);
					controls.updateTileInfo(controls.lastDestructibleX << 5, controls.lastDestructibleY << 5);
				}
			} else level.setLeftButtonHeld(false);
			
			if (!(inventory.getActiveItem() == null) && ColorSelector.class.isAssignableFrom(inventory.getActiveItem().getClass())) {
				float delta = 0.001F;
				if (game.input.insert.isPressed()) ((ColorSelector) inventory.getActiveItem()).shiftHue(delta);
				else if (game.input.delete.isPressed()) ((ColorSelector) inventory.getActiveItem()).shiftHue(-delta);
				if (game.input.home.isPressed()) ((ColorSelector) inventory.getActiveItem()).shiftSaturation(delta);
				else if (game.input.end.isPressed()) ((ColorSelector) inventory.getActiveItem()).shiftSaturation(-delta);
				if (game.input.pageUp.isPressed()) ((ColorSelector) inventory.getActiveItem()).shiftBrightness(delta);
				else if (game.input.pageDown.isPressed()) ((ColorSelector) inventory.getActiveItem()).shiftBrightness(-delta);
			}
		}
		
		if (meleeTimer > 0) {
			meleeImage = true;
			meleeTimer--;
		}
		else meleeTimer = 0;
		
		storedPower -= powerUsage;
	}
	
	public boolean addPower(double power) {
		storedPower += power;
		if (storedPower >= maximumStoredPower) {
			storedPower = maximumStoredPower;
			return false;
		} else if (storedPower < 0) {
			storedPower = 0;
			return true;
		}
		return true;	
	}
	
	public void calculatePhysics() {
		// Reset acceleration each iteration
		aY = 0;
		aX = 0;
		
		// Initialize temporary direction variables
		boolean dirL = false;
		boolean dirR = false;
		
		// Set coordinates in bounds
		if (dX < 0) {dX = 0; x = 0;}
		else if (dX > level.width << 5) {dX = level.width << 5; x = level.width << 5;}
		if (dY < 0) {
			dY = 0; y = 0; 
			if (vY < 0) vY = -vY / 3;
		}
		else if (dY > level.height << 5) {dY = level.height << 5; y = level.height << 5;}
		
		
		// Set horizontal acceleration and temporary direction variables based on currently held direction
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && !collisionLeft(x, y, spriteWidth, spriteHeight, level) && controls.left.isPressed()) { 
			aX = -.05;
			dirL = true;
		}
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && !collisionRight(x, y, spriteWidth, spriteHeight, level) && controls.right.isPressed()) { 
			aX = .05;
			dirR = true;
		}
		
		// Transfer temporary direction variable states to global variables, but not if both left and right are held
		if (!(dirL && dirR) && dirL) {
			this.movingDir = 2;
		} else if (!(dirL && dirR) && dirR) {
			this.movingDir = 3;
		}
		
		// Cancel horizontal acceleration if both left and right are held
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.left.isPressed() && controls.right.isPressed()) aX = 0;
		
		// Use jet pack or jump if eligible and up is pressed
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && !collisionAbove(x, y, spriteWidth, spriteHeight, level) && (collisionBelow(x, y, spriteWidth, spriteHeight, level) || shouldFly) && controls.up.isPressed()) { 
			if (shouldFly) {
				vY -= 0.125;
				if (controls.down.isPressed()) vY = 0;
			} else if (canJump) {
				vY = -2.5;
				canJump = false;
				if (game.tracker != null) game.tracker.incrementBasicStat("Jumps");
			}
		}
		
		// Cancel vertical acceleration if both up and down are held (before gravity is applied)
		if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.up.isPressed() && controls.down.isPressed()) aY = 0;
		
		// Handle climbing on platforms
		if (!collisionBelow(x, y, spriteWidth, spriteHeight, level) || controls.down.isPressed() && arePlatformsBelow(x, y, spriteWidth, spriteHeight, level) 
				|| (!controls.up.isPressed() && arePlatformsAbove(x, y, spriteWidth, spriteHeight, level) & vY < 0) ) { 
			aY = getLocalGravity();
			canJump = true;
			queuePlayerModelUpdate();
			if (controls.getControlScheme() != InputHandler.ControlScheme.GAMEPLAY || (!controls.left.isPressed() && !controls.right.isPressed())) {
				if (vX > 0) {
					aX -= airResistance * vX;
				} else {
					aX += airResistance * -vX;
				}
			}
		}
		
		// Apply effects of friction if neither left nor right are held
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
		
		// Round extremely small values of acceleration to zero
		if (aX < 0.00001 && aX > -0.00001) aX = 0;
		if (aY < 0.00001 && aY > -0.00001) aY = 0;
		
		// Given a lack of collision in the corresponding direction, apply the components of acceleration to the respective components of velocity
		if ((aX < 0 && !collisionLeft(x, y, spriteWidth, spriteHeight, level)) || (aX > 0 && !collisionRight(x, y, spriteWidth, spriteHeight, level))) vX += aX;
		if ((aY < 0 && !collisionAbove(x, y, spriteWidth, spriteHeight, level)) || (aY > 0 && (!collisionBelow(x, y, spriteWidth, spriteHeight, level) 
				|| (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.down.isPressed() && arePlatformsBelow(x, y, spriteWidth, spriteHeight, level) ))
				|| (!controls.up.isPressed() && arePlatformsAbove(x, y, spriteWidth, spriteHeight, level) & vY < 0))) vY += aY;

		// Handle horizontal collisions
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
		
		// Handle vertical collisions
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
		
		// Smoothly limit horizontal acceleration that results in exceeding horizontal maximum velocity
		if (vX > 0 && vX > maxVelocityX) {
			if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.right.isPressed()) vX = Math.log(vX) + maxVelocityX;
			else {
				vX = maxVelocityX + ((vX - maxVelocityX) / 1.875);
			}
		}
		if (vX < 0 && vX < -maxVelocityX) {
			if (controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.left.isPressed()) vX = -Math.log(-vX) - maxVelocityX;
			else {
				vX = -maxVelocityX + ((vX + maxVelocityX) / 1.875);
			}
		}
		
		// Abruptly limit vertical acceleration that results in exceeding vertical maximum velocity
		if (vY > 0 && vY > maxVelocityY) {
			vY = maxVelocityY;
		}
		if (vY < 0 && vY < -maxVelocityY) {
			vY = -maxVelocityY;
		}
		
		// Round reasonably small values of velocity to zero		
		if (vX < 0.001 && vX > -0.001) vX = 0;
		if (vY < 0.001 && vY > -0.001) vY = 0;
		
		// Given a lack of collision in the corresponding direction, apply the components of velocity to the respective components of position
		if ((vX < 0 && !collisionLeft(x, y, spriteWidth, spriteHeight, level)) || (vX > 0 && !collisionRight(x, y, spriteWidth, spriteHeight, level))) dX += vX;
		if ((vY < 0 && !collisionAbove(x, y, spriteWidth, spriteHeight, level)) || (vY > 0 && (!collisionBelow(x, y, spriteWidth, spriteHeight, level) 
				|| ( controls.getControlScheme() == InputHandler.ControlScheme.GAMEPLAY && controls.down.isPressed() && arePlatformsBelow(x, y, spriteWidth, spriteHeight, level))))) dY += vY;
		
		// For each pixel in the delta between the new double position components and old integer position components, render the entire game and step one pixel in the corresponding direction
		int deltaX = Math.toIntExact(Math.round(Math.floor(dX))) - x;
		//x += deltaX;
		if (deltaX > 0) {
			for (int i = 0; i < deltaX; i++) {
				//if (level.getGameLoop() != null) level.getGameLoop().repaint();
				x++;
			}
		} else {		
			for (int i = 0; i < -deltaX; i++) {
				//if (level.getGameLoop() != null) level.getGameLoop().repaint();
				x--;
			}
		}
		int deltaY = Math.toIntExact(Math.round(Math.floor(dY))) - y;
		//y += deltaY;
		if (deltaY > 0) {
			for (int i = 0; i < deltaY; i++) {
				//if (level.getGameLoop() != null) level.getGameLoop().repaint();
				y++;
			}
		} else {		
			for (int i = 0; i < -deltaY; i++) {
				//if (level.getGameLoop() != null) level.getGameLoop().repaint();
				y--;
			}
		}
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
	
	public void draw(Graphics g, ImageObserver observer) {
		if (inventory.isActive()) {
			game.drawHUD = false;
			inventory.draw(g, game.drawResolution.width, game.drawResolution.height, game);
			inventory.drawHotbar(g, game.drawResolution.width, game.drawResolution.height, game);
		} else {
			game.drawHUD = true;
		}
		if (controls.getControlScheme() == ControlScheme.GAMEPLAY || controls.getControlScheme() == ControlScheme.TECH_TREE) {
			inventory.drawHotbar(g, game.drawResolution.width, game.drawResolution.height, game);
		}		
		
		drawMeter(health, Color.RED, Color.getHSBColor((float) ((health%100)/100), 1F, 0.5F), g, observer, 0, 0);
		drawMeter(oxygen, Color.CYAN, Color.CYAN, g, observer, 0, 128 + 32);
		drawMeter(storedPower, Color.YELLOW, Color.YELLOW, g, observer, 0, 256 + 64);
		
		g.setFont(MediaLibrary.getFontFromLibrary("INFOFont"));
		if (drawInfo) printMovementInfo(g, 196, 196);
	}
	
	public void drawOxygenLine(Graphics g, int oxyX, int oxyY) {
		if (connectedToOxygen()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setStroke(new BasicStroke(5));
			Color color = Color.CYAN;
			float HSB[] = new float[3];
			Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), HSB);
			float distanceMultiplier = (float) ((x - oxyX) * (x - oxyX) + (y - oxyY) * (y - oxyY))/(32 * 32 * 16 * 16 * 2);
			//System.out.println(distanceMultiplier);
			HSB[2] = distanceMultiplier < HSB[2] ? HSB[2] - distanceMultiplier : 0;
			color = Color.getHSBColor(HSB[0], HSB[1], HSB[2]);
			g2.setColor(color);
			g2.drawLine(x - game.xOffset + spriteWidth / 2, y - game.yOffset - 3 + spriteHeight / 3, oxyX  - game.xOffset + 20, oxyY - game.yOffset + 4);
		}
	}
	
	public void drawPlayerModel(Graphics2D g, int xOffset, int yOffset, int drawResolutionWidth, int drawResolutionHeight, ImageObserver observer) {
		g.setColor(Color.GRAY);
		
		try {
			for (Point p : oxyPoints) {
				drawOxygenLine(g, p.x, p.y);
			}
		} catch (ConcurrentModificationException c) {
			// Do nothing
		} catch (NullPointerException n) {
			// Do nothing
		} catch (Exception e) {
			if (!(e.getMessage() == null)) FileUtilities.log(e.getMessage());
		}
		
		int newImageID = lastImageID;
		
		if (getMovingDir() == 2) {
			if (vX >= -0.001) newImageID = 7501;
			else if (collisionBelow(x, y, spriteWidth, spriteHeight, level) && vX < -2.5) newImageID = 7501 + (((int) (game.ticks / 20))) % 4;
			else newImageID = 7501 + (((int) (game.ticks / 60))) % 4;
			
			if (meleeImage) newImageID = 7505;
		} else if (getMovingDir() == 3) {
			if (vX <= 0.001) newImageID = 7501;
			else if (collisionBelow(x, y, spriteWidth, spriteHeight, level) && vX > 2.5) newImageID = 7501 + (((int) (game.ticks / 20))) % 4;
			else newImageID = 7501 + (((int) (game.ticks / 60))) % 4;
			
			if (meleeImage) newImageID = 7505;
		}		
		
		showInventoryItem = !(newImageID == 7505) || (inventory.getActiveItem() != null && InventoryTool.class.isAssignableFrom(inventory.getActiveItem().getClass()));
		
		// Redraw player model only when the movement state changes
		if (!game.displayLighting) {
			playerModel = MediaLibrary.getBufferedImageFromLibrary(newImageID);
			drawInventoryItem(playerModel, observer, newImageID);
		} else if (!(newImageID == lastImageID) 
				|| level.getDiscreteLightLevel((x + playerModel.getWidth() - 1) >> 5, y >> 5) == -127 
				|| level.getDiscreteLightLevel(x >> 5, y >> 5) == -127
				|| queuePlayerModelUpdate) {
			queuePlayerModelUpdate = false;
			playerModel = MediaLibrary.getBufferedImageFromLibrary(newImageID);
			lastImageID = newImageID;
			
			drawInventoryItem(playerModel, observer, newImageID);
			
			int pixel;
			float[] HSB = new float[3];
			int alpha;
			int red;
			int green;
			int blue;
			
			int newPixelRGB;
			int newRed;
			int newGreen;
			int newBlue;
			int newPixelARGB;
			
			float lightLevel;
			
			int width = playerModel.getWidth();
			int height = playerModel.getHeight();
			
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					pixel = playerModel.getRGB(i, j);
					
					// Skip fully transparent pixels
					if (pixel == 0x00FFFFFF) continue;
					
					// Take the current pixel's RGBA value and extract its components
					alpha = (pixel>>24)&0xFF;
					red = (pixel>>16)&0xFF;
					green = (pixel>>8)&0xFF;
					blue = pixel&0xFF;
					
					// Convert the pixel's RGB components to HSB
					Color.RGBtoHSB(red, green, blue, HSB);
					
					// Change the brightness channel based on the current light level
					lightLevel = getMovingDir() == 2 ? level.getDiscreteLightLevel((x + playerModel.getWidth() - 1 - i) >> 5, (y + j) >> 5) : level.getDiscreteLightLevel((x + i) >> 5, (y + j) >> 5);
					if (lightLevel >= -127 && lightLevel <= 127 && (127F + lightLevel) / 255F < HSB[2]) HSB[2] = (127F + lightLevel) / 255F;
					
					// Convert the modified HSB components to RGB
					newPixelRGB = Color.HSBtoRGB(HSB[0], HSB[1], HSB[2]);
					newRed = (newPixelRGB>>16)&0xFF;
					newGreen = (newPixelRGB>>8)&0xFF;
					newBlue = newPixelRGB&0xFF;
					newPixelARGB = (alpha<<24|newRed<<16|newGreen<<8|newBlue);
					
					// Write to the BufferedImage
					playerModel.setRGB(i, j, newPixelARGB);
				}
			}
		}
		
		int tempSpriteWidth = playerModel.getWidth();
		int tempSpriteHeight = playerModel.getHeight();
		
		xOffset = (int) (x - (drawResolutionWidth/2));
		yOffset = (int) (y - (drawResolutionHeight/2));
		
		if (getMovingDir() == 2) {
			g.drawImage(playerModel, x - xOffset + spriteWidth, y - yOffset - 3, -tempSpriteWidth, tempSpriteHeight, observer);
			if (shouldFly) g.drawImage(MediaLibrary.getImageFromLibrary(7499), x - xOffset + tempSpriteWidth, y - yOffset - 3, -tempSpriteWidth, tempSpriteHeight, observer);
			
			if (drawInfo) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(1));
				g2.setColor(Color.RED);
				g2.drawLine(x - xOffset - 4, y - yOffset + tempSpriteHeight / 2 - 48, x - xOffset - 4, y - yOffset + tempSpriteHeight / 2);
				//g.drawRect(x - xOffset - 8, y - yOffset + tempSpriteHeight / 2 - 48, 8, 48);
			}
		} else if (getMovingDir() == 3) {
			g.drawImage(playerModel, x - xOffset, y - yOffset - 3, tempSpriteWidth, tempSpriteHeight, observer);
			if (shouldFly) g.drawImage(MediaLibrary.getImageFromLibrary(7499), x - xOffset, y - yOffset - 3, tempSpriteWidth, tempSpriteHeight, observer);
			
			if (drawInfo) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setStroke(new BasicStroke(1));
				g2.setColor(Color.RED);
				g2.drawLine(x - xOffset + tempSpriteWidth + 4, y - yOffset + tempSpriteHeight / 2 - 48, x - xOffset + tempSpriteWidth + 4, y - yOffset + tempSpriteHeight / 2);
				//g.drawRect(x - xOffset + tempSpriteWidth, y - yOffset + tempSpriteHeight / 2 - 48, 8, 48);
			}
		}
		
		
		/*if (getMovingDir() == 2) {
			if (vX >= -0.001) lastImageID = 7501;
			else if (collisionBelow(x, y, spriteWidth, spriteHeight, level) && vX < -2.5) lastImageID = 7501 + (((int) (game.ticks / 20))) % 4;
			else lastImageID = 7501 + (((int) (game.ticks / 60))) % 4;
			g.drawImage(MediaLibrary.getImageFromLibrary(lastImageID), x - xOffset + spriteWidth, y - yOffset - 3, -spriteWidth, spriteHeight, observer);
			if (shouldFly) g.drawImage(MediaLibrary.getImageFromLibrary(7499), x - xOffset + spriteWidth, y - yOffset - 3, -spriteWidth, spriteHeight, observer);
		} else if (getMovingDir() == 3) {
			if (vX <= 0.001) lastImageID = 7501;
			else if (collisionBelow(x, y, spriteWidth, spriteHeight, level) && vX > 2.5) lastImageID = 7501 + (((int) (game.ticks / 20))) % 4;
			else lastImageID = 7501 + (((int) (game.ticks / 60))) % 4;
			g.drawImage(MediaLibrary.getImageFromLibrary(lastImageID), x - xOffset, y - yOffset - 3, spriteWidth, spriteHeight, observer);
			if (shouldFly) g.drawImage(MediaLibrary.getImageFromLibrary(7499), x - xOffset, y - yOffset - 3, spriteWidth, spriteHeight, observer);
		}*/
	}
	
	public void drawInventoryItem(BufferedImage playerModel, ImageObserver observer, int imageID) {
		Graphics2D playerModelGraphics;
		if (showInventoryItem && !(inventory.getActiveItem() == null)) {
			if (meleeImage) {
				BufferedImage enlargedPlayerModel = new BufferedImage(spriteWidth + 64, spriteHeight, BufferedImage.TYPE_INT_ARGB);
				playerModelGraphics = (Graphics2D) enlargedPlayerModel.getGraphics();
				playerModelGraphics.drawImage(playerModel, 0, 0, observer);
				this.playerModel = enlargedPlayerModel;
				playerModelGraphics.dispose();
			} else playerModelGraphics = (Graphics2D) playerModel.getGraphics();
			
			int width = 24;
			int height = 24;		
			
			int newXOffset = 0;
			int newYOffset = 0;
			
			// Change if player image numbers change
			switch (imageID % 5) {
			case 2: newXOffset = -1; break;
			case 4: newXOffset = 1; break;
			}
			
			double angle = 0;
			
			int armYOffset = 0;
			
			if (meleeImage) {
				newXOffset = 32;
				newYOffset = 32;
				armYOffset = (int) Math.round(8 * Math.sin(game.ticks / 4));
				newYOffset = -32 + armYOffset;
			} else angle = Math.PI / 2;
			
			if (InventoryTool.class.isAssignableFrom(inventory.getActiveItem().getClass())) {
				width = 32;
				height = 32;
			}	
			
			inventory.getActiveItem().drawRotated(playerModelGraphics, spriteWidth / 2 - 14 + newXOffset, spriteHeight / 2 - 6 + newYOffset, width, height, angle, observer);
			
			// Draw arm
			playerModelGraphics.drawImage(MediaLibrary.getImageFromLibrary(imageID + 5), 0, armYOffset, observer);
			playerModelGraphics.dispose();
			
		} else {
			if (meleeImage) {
				BufferedImage enlargedPlayerModel = new BufferedImage(spriteWidth + 64, spriteHeight, BufferedImage.TYPE_INT_ARGB);
				playerModelGraphics = (Graphics2D) enlargedPlayerModel.getGraphics();
				playerModelGraphics.drawImage(playerModel, 0, 0, observer);
				this.playerModel = enlargedPlayerModel;
			} else playerModelGraphics = (Graphics2D) playerModel.getGraphics();
			
			// Draw arm
			playerModelGraphics.drawImage(MediaLibrary.getImageFromLibrary(imageID + 5), 0, 0, observer);
			playerModelGraphics.dispose();
		}
	}
	
	public void drawMeter(double percent, Color color, Color blendedColor, Graphics g, ImageObserver observer, int xOffset, int yOffset) {
		g.translate(xOffset, yOffset);
		
		Color darkBackgroundColor = new Color(64, 64, 64, 64);
		Color lightBackgroundColor = new Color(128, 128, 128, 128);
		Color wireColor = new Color(144, 144, 144, 128);
		
		g.setColor(Color.DARK_GRAY);
		g.fillOval(32, 32, 128, 128);
		
		Graphics2D g2 = (Graphics2D) g;
		Stroke back1 = new BasicStroke(5, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {4.0f, 8.0f}, 0.0f);
		Stroke h1 = new BasicStroke(20);
		Stroke h2 = new BasicStroke(4);
		Stroke h3 = new BasicStroke(6);
		g2.setStroke(h1);
		g2.setColor(lightBackgroundColor);
		g2.drawArc(32, 32, 128, 128, 90, 360);

		g2.setStroke(h2);
		g2.setColor(wireColor);
		g2.drawArc(32, 32, 128, 128, 90, 360);
		g2.setStroke(back1);
		g2.setColor(darkBackgroundColor);
		g2.drawArc(32, 32, 128, 128, 90, 360);
		g2.setColor(color);
		g2.setStroke(h3);
		g2.drawArc(32, 32, 128, 128, 90, -(int) Math.round(3.6 * percent));
		if (health > 100) {
			g2.setColor(blendedColor);
			g2.drawArc(32, 32, 128, 128, 90, -(int) Math.round(3.6 * (percent%100)));
		}
		
		g2.setStroke(h1);
		g2.setColor(lightBackgroundColor);
		g2.drawArc(52, 52, 88, 88, 90, 360);
		g2.setColor(wireColor);
		g2.setStroke(h2);
		g2.drawArc(52, 52, 88, 88, 90, 360);
		g2.setColor(darkBackgroundColor);
		g2.setStroke(back1);
		g2.drawArc(52, 52, 88, 88, 90, 360);
		g2.setStroke(h2);
		g2.setColor(color);
		g2.drawArc(52, 52, 88, 88, 90 - ((int) Math.round(36 * (percent % 10)) - 7), 14);
		
		g.setColor(color);
		int hr = (int) Math.round(percent);
		g.setFont(MediaLibrary.getFontFromLibrary("Health"));
		String hrs = "" + hr;
		if (hrs.length() == 1) {
			hrs = "00" + hr;
		} else if (hrs.length() == 2) {
			hrs = "0" + hr;
		}
		g.drawString(hrs, 69, 106);
		
		g.translate(-xOffset, -yOffset);
	}
	
	// Utility functions, getters and setters	
	
	public void queueMeleeImage() {
		meleeImage = true;
	}
	
	public void queueMeleeImage(int meleeTime) {
		if (meleeTime > meleeTimer) meleeTimer = meleeTime;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String str) {
		username = str;
	}
	
	public void removeOxygen(double d) {
		oxygen -= d;
		if (oxygen < 0) oxygen = 0;
	}
	
	public void addOxygen(double d) {
		oxygen += d;
		if (oxygen > 100D) oxygen = 100D;
	}
	
	public void setOxygen(double d) {
		oxygen = d;
	}
	
	public double getOxygen() {
		return oxygen;
	}

	public boolean checkConflict() {
		return false;
	}

	public boolean hasCollided(int deltaX, int deltaY) {
		return false;
	}
	
	public int getMovingDir() {
		return this.movingDir;
	}
	
	public void toggleInfo() {
		drawInfo = !drawInfo;
	}
	
	public void setInfo(boolean b) {
		drawInfo = b;
	}
	
	public void addOxygenPoint(int x, int y) {
		oxyPoints.add(new Point(x, y));
	}
	
	public void connectOxygen() {
		oxygenConnected = true;
	}
	
	public void disconnectOxygen() {
		oxygenConnected = false;
	}
	
	public void setOxygenConnection(boolean oxygenConnected) {
		this.oxygenConnected = oxygenConnected;
	}
	
	public boolean connectedToOxygen() {
		return oxygenConnected;
	}
	
	public void checkDeath() {
		if (health <= 0) {
			x = level.spawnX;
			y = level.spawnY;
			dX = level.spawnX;
			dY = level.spawnY;
			vX = 0;
			vY = 0;
			aX = 0;
			aY = 
			health = 100.0;
			inventory = new Inventory(100, 80, controls, this);
			queuePlayerModelUpdate();
		}
	}
	
	public void handleToggles() {
		if (controls.func1.isPressed()) {
			game.miniMapScale = 1;
			game.drawMiniMap = true;
		}
		if (controls.func2.isPressed()) {
			game.miniMapScale = 2;
			game.drawMiniMap = true;
		}
		if (controls.func4.isPressed()) {
			game.miniMapScale = 4;
			game.drawMiniMap = true;
		}
		if (controls.miniMap.isPressed()) {
			if (toggleMiniMap) {
				game.drawMiniMap = !game.drawMiniMap;
			}
			toggleMiniMap = false;
		} else {
			toggleMiniMap = true;
		}
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
		if (controls.techTree.isPressed() && !inventory.isActive()) {
			if (toggleTechTree) game.techTreeGUI.toggleActive();
			toggleTechTree = false;
		} else {
			toggleTechTree = true;
		}
		if (controls.func5.isPressed()) {
			if (toggleCreativeCrafting) game.basicCraftingGUI.toggleFreeCrafting();
			toggleCreativeCrafting = false;
		} else {
			toggleCreativeCrafting = true;
		}
		if (controls.func6.isPressed()) {
			if (toggleCreativeView) game.toggleFog();
			toggleCreativeView = false;
		} else {
			toggleCreativeView = true;
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
		
		if (controls.func9.isPressed()) {
			if (toggleCreativeEntities) {
				inventory.addItem(new InventoryEntity(0, 2));
			}
			toggleCreativeEntities = false;
		} else {
			toggleCreativeEntities = true;
		}
		if (controls.func10.isPressed()) {
			if (toggleLighting) {
				game.toggleLighting();
			} 
			toggleLighting = false;
		} else {
			toggleLighting = true;
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
	}

	public void queuePlayerModelUpdate() {
		queuePlayerModelUpdate = true;
	}
	
	public void draw(Graphics g) {
		
	}

	public double getLocalGravity() {
		return localGravity;
	}

	public void setLocalGravity(double localGravity) {
		this.localGravity = localGravity;
	}
}
