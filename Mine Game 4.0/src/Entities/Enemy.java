package Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import Libraries.MediaLibrary;
import SingleplayerClient.Level;
import Tiles.SolidTile;
import Tiles.Tile;
import UI.Ingredient;
import Utilities.FileUtilities;
import Utilities.PhysicsUtilities;

public class Enemy extends Mob {
	private int imageID;
	private double localGravity;
	private double aX, aY;
	private double vX, vY;
	private double dX, dY;
	private double elasticity;
	private double mass;
	private int playerDeltaX, playerDeltaY;
	
	public Enemy(int imageID, Level level, String name, int x, int y, int speed, int baseHealth, int hitboxWidth, int hitboxHeight, double localGravity, double elasticity, double mass) {
		super(level, name, x, y, speed, baseHealth, hitboxWidth, hitboxHeight);
		dX = x;
		dY = y;
		this.imageID = imageID;
		this.localGravity = localGravity;
		this.elasticity = elasticity;
		this.mass = mass;
		FileUtilities.log("Enemy #" + imageID + ": " + name + " generated at (" + x + ", " + y + ")\n");
	}
	public boolean hasCollided(int deltaX, int deltaY) {
		return (PhysicsUtilities.collisionAbove(x + deltaX, y + deltaY, hitboxWidth, hitboxHeight, level) 
				|| PhysicsUtilities.collisionBelow(x + deltaX, y + deltaY, hitboxWidth, hitboxHeight, level)
				|| PhysicsUtilities.collisionLeft(x + deltaX, y + deltaY, hitboxWidth, hitboxHeight, level) 
				|| PhysicsUtilities.collisionRight(x + deltaX, y + deltaY, hitboxWidth, hitboxHeight, level));
	}

	public void tick() {
		if (PhysicsUtilities.collisionBelow(x, y, hitboxWidth, hitboxHeight, level) && vY > 0) {
			vY = -vY * elasticity;
			aY = 0;
			checkGroundProperties();
		} else if (PhysicsUtilities.collisionBelow(x, y, hitboxWidth, hitboxHeight, level) && vY < 0.001) {
			vY = 0; 
			aY = 0;
			checkGroundProperties();
		} else {
			aY = localGravity;
			aX = 0;
		}
		
		// Get distance from player
		playerDeltaX = (level.getPlayer().x + level.getPlayer().spriteWidth / 2) - (x + hitboxWidth / 2);
		playerDeltaY = (level.getPlayer().y + level.getPlayer().getHitboxHeight() / 2) - (y + hitboxHeight / 2);
		
		if (playerDeltaX * playerDeltaX + playerDeltaY * playerDeltaY < 34 * 34) level.getPlayer().damage(0.1);
		
		if (PhysicsUtilities.collisionAbove(x, y, hitboxWidth, hitboxHeight, level) && vY < 0) {vY = -vY * elasticity * 0.25; aY = 0;}
		if ((PhysicsUtilities.collisionLeft(x, y, hitboxWidth, hitboxHeight, level) && vX < 0) || (PhysicsUtilities.collisionRight(x, y, hitboxWidth, hitboxHeight, level) && vX > 0)) vX = -vX * elasticity * 0.25;
		else if (PhysicsUtilities.collisionBelow(x, y, hitboxWidth, hitboxHeight, level)) {
			vY = vY > -2.5 ? -2.5 : vY;
			vX += (int) Math.min(2.5, Math.floor(Math.pow(Math.log(Math.abs(playerDeltaX)), 1/3))) * Math.signum(playerDeltaX);
		}
		
		// Propagate velocity components
		vX += aX;
		vY += aY;
		
		// Propagate position components
		dX += vX;
		dY += vY;
		
		// Set position integers
		x = (int) Math.round(dX);
		y = (int) Math.round(dY);
		x = x < 0 ? 0 : (x >= level.width << 5 ? (level.width - 1) << 5 : x);
		y = y < 0 ? 0 : (y >= level.width << 5 ? (level.width - 1) << 5 : y);
		
		// Check current tile
		if (imageID == 7701 && x > 0 && x >> 5 < level.width - hitboxWidth - 1 
				&& (level.getTile((x - 1) >> 5, (y + hitboxHeight / 2) >> 5).getId() == Tile.CACTUS.getId()
				|| level.getTile((x + hitboxWidth + 1) >> 5, (y + hitboxHeight / 2) >> 5).getId() == Tile.CACTUS.getId())) {
			this.damage(0.1);
		}
	}
	
	public void checkGroundProperties() {
		//System.out.println(level.getTile(x >> 5, (y >> 5) + 1).getClass());
		if (level.getTile(x >> 5, (y >> 5) + 1).isSolid()) {
			double friction = ((SolidTile) level.getTile(x >> 5, (y >> 5) + 1)).getFriction() / 3;
			aX += friction * mass * 0.1 * -vX;
			if (level.getTile(x >> 5, (y >> 5) + 1).getId() >= Tile.CONVEYOR.getId() && level.getTile(x >> 5, (y >> 5) + 1).getId() <= Tile.CONVEYOR_MIDDLE.getId()) {
				if (vX > level.getConveyorSpeed(x >> 5, (y >> 5) + 1)) {
					vX -= Math.abs(vX - level.getConveyorSpeed(x >> 5, (y >> 5) + 1)) / 4;
				} else if (vX < level.getConveyorSpeed(x >> 5, (y >> 5) + 1)) {
					vX += Math.abs(vX - level.getConveyorSpeed(x >> 5, (y >> 5) + 1)) / 4;
				}
			}
		}
	}
	
	public void checkDeath() {
		if (health <= 0) {
			if (imageID == 7701) level.queueEntity(new PhysicalItem(1021, level, true, x, y, new Ingredient(21, (int) Math.ceil(5 * Math.random()))));
			markedForDeletion = true;
		}
	}
	
	public void addVelocity(double vX, double vY) {
		this.vX += vX;
		this.vY += vY;
	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(imageID), x - level.getGameLoop().xOffset, y - level.getGameLoop().yOffset, level.getGameLoop());
		
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(this.name, x + (hitboxWidth / 2) - (g.getFontMetrics().stringWidth(this.name) / 2) - level.getGameLoop().xOffset,
				y - level.getGameLoop().yOffset - hitboxHeight);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(new Color((int) (255 - (255 * this.getHealth()) / this.baseHealth), (int) ((255 * this.getHealth()) / this.baseHealth), 0));
		g2.setStroke(new BasicStroke(4));
		g2.drawLine(x - level.getGameLoop().xOffset + (hitboxWidth / 2) - (g.getFontMetrics().stringWidth(this.name) / 2), y - level.getGameLoop().yOffset - hitboxHeight / 2, 
				(int) (x - level.getGameLoop().xOffset + (hitboxWidth / 2) - (g.getFontMetrics().stringWidth(this.name) / 2)
						+ this.getHealth() / this.baseHealth * g.getFontMetrics().stringWidth(this.name)), y - level.getGameLoop().yOffset - hitboxHeight / 2);
	}

	public boolean checkConflict() {
		return false;
	}
	

}
