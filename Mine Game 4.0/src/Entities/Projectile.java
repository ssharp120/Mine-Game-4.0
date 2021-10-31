package Entities;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import Libraries.MediaLibrary;
import SingleplayerClient.Level;
import Tiles.SolidTile;
import Tiles.Tile;
import UI.Ingredient;
import Utilities.FileUtilities;
import Utilities.PhysicsUtilities;

public class Projectile extends Entity {

	private int projectileID;
	protected double elasticity = 1;
	private double mass = 1;
	private double damagePotential = 0;
	private double aX;
	private double aY = 0.01;
	private double vX;
	private double vY;
	private double dX;
	private double dY;
	protected int hitboxWidth = 1;
	protected int hitboxHeight = 1;
	private double angle;
	
	public Projectile(int projectileID, Level level, boolean active, int x, int y) {
		super(8500 + projectileID, level, active, x, y);
		this.projectileID = projectileID;
		dX = x;
		dY = y;
		if (level != null && level.getGameLoop().input.shift.isPressed()) vX = 1 - (2 * Math.random());
		switch (projectileID) {
			case 0: {
				elasticity = 0.75;
				if (level != null && level.getGameLoop().input.ctrl.isPressed()) elasticity = 0;
				mass = 1;
				break;
			}
			case 1: {
				elasticity = 0;
				mass = 0.5;
				damagePotential = 0.1;
			}
		}
		if (projectileID < 999) {
			hitboxWidth = MediaLibrary.getImageFromLibrary(8500 + projectileID).getWidth(level.getGameLoop());
			hitboxHeight = MediaLibrary.getImageFromLibrary(8500 + projectileID).getHeight(level.getGameLoop());
		}
	}
	
	public Projectile(int projectileID, Level level, boolean active, int x, int y, double initialvX, double initialvY) {
		super(8500 + projectileID, level, active, x, y);
		this.projectileID = projectileID;
		dX = x;
		dY = y;
		if (level != null && level.getGameLoop().input.shift.isPressed()) vX = 1 - (2 * Math.random());
		switch (projectileID) {
			case 0: {
				elasticity = 0.75;
				if (level != null && level.getGameLoop().input.ctrl.isPressed()) elasticity = 0;
				mass = 1;
				break;
			}
			case 1: {
				elasticity = 0;
				mass = 0.5;
				damagePotential = 0.1;
			}
		}
		vX = initialvX;
		vY = initialvY;
		if (projectileID < 999) {
			hitboxWidth = MediaLibrary.getImageFromLibrary(8500 + projectileID).getWidth(level.getGameLoop());
			hitboxHeight = MediaLibrary.getImageFromLibrary(8500 + projectileID).getHeight(level.getGameLoop());
		}
	}

	public void tick() {
		if (elasticity < 0 || elasticity > 1) FileUtilities.log("Impossible elasticity! " + this.toString());
		
		vX += aX;
		vY += aY;
		
		dX += vX;
		
		x = (int) Math.round(dX);
		y = (int) Math.round(dY);
		
		aX = 0;
		aY = 0.025;
		
		if (elasticity == 0 && ((PhysicsUtilities.collisionLeft(x, y, hitboxWidth, hitboxHeight, level) && vX < 0) || (PhysicsUtilities.collisionRight(x, y, hitboxWidth, hitboxHeight, level) && vX > 0))) {
			aY = 0;
		}
		
		int originalX = x;
		int originalY = y;
		
		while (level.getTile((x + hitboxWidth / 2) >> 5, (y >> 5) + 1).getId() == Tile.FUNNEL.getId() && throughFunnel(originalX, originalY)) {}
		
		if (PhysicsUtilities.collisionBelow(x, y, hitboxWidth, hitboxHeight, level) && vY > 0.001) {
			vY = -vY * elasticity;
			aY = 0;
			checkGroundProperties();
		} else if (PhysicsUtilities.collisionBelow(x, y, hitboxWidth, hitboxHeight, level) && vY < 0.001 && vY > -0.001) {
			vY = 0; 
			aY = 0;
			checkGroundProperties();
		}
		else dY += vY;
		if (PhysicsUtilities.collisionAbove(x, y, hitboxWidth, hitboxHeight, level) && vY < 0) {vY = -vY * elasticity; aY = 0;}
		if ((PhysicsUtilities.collisionLeft(x, y, hitboxWidth, hitboxHeight, level) && vX < 0) || (PhysicsUtilities.collisionRight(x, y, hitboxWidth, hitboxHeight, level) && vX > 0)) vX = -vX * elasticity;
		if (PhysicsUtilities.collisionBelow(x, y, hitboxWidth, hitboxHeight, level) && PhysicsUtilities.collisionAbove(x, y, hitboxWidth, hitboxHeight, level) 
				&& PhysicsUtilities.collisionLeft(x, y, hitboxWidth, hitboxHeight, level) && PhysicsUtilities.collisionRight(x, y, hitboxWidth, hitboxHeight, level)) {
			vX = 0;
			vY = 0;
		}
		
		if (level.getGameLoop().input.collect.isPressed() && projectileID == 1 && PhysicsUtilities.checkIntersection(x, y, level.getGameLoop().player.x, level.getGameLoop().player.y, level.getGameLoop().player.spriteWidth, level.getGameLoop().player.spriteHeight, true)) {
			level.getGameLoop().player.inventory.addItem(new Ingredient(16, 1));
			markedForDeletion = true;
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
	
	public boolean throughFunnel(int originalX, int originalY) {
		x = x >> 5;
		x = x << 5;
		y = y >> 5;
		y += 2;
		y = y << 5;
		alignSubpixel(x, y);
		return true;
	}
	
	public double getDamagePotential() {
		return damagePotential;
	}

	public void draw(Graphics g) {
		
		if (vX > 0.01 || vX < -0.01) angle = Math.atan(vY / vX);
		
		BufferedImage image = MediaLibrary.getBufferedImageFromLibrary(8500 + projectileID);
		
		Boolean negativeVX = false;
		
		if (vX < -0.05 || negativeVX) {
			image = FileUtilities.rotateImage(MediaLibrary.getBufferedImageFromLibrary(8500 + projectileID), angle);
			g.drawImage(image, x - level.getGameLoop().xOffset, y - level.getGameLoop().yOffset, -image.getWidth(), -image.getHeight(), level.getGameLoop());
			negativeVX = true;
		} else if (vX > 0.05 || !negativeVX) {
			g.drawImage(FileUtilities.rotateImage(MediaLibrary.getBufferedImageFromLibrary(8500 + projectileID), angle), x - level.getGameLoop().xOffset, y - level.getGameLoop().yOffset, level.getGameLoop());
			negativeVX = false;
		}
		//System.out.println("x " + x + " y " + y);
	}

	public boolean checkConflict() {
		return false;
	}
	
	public void alignSubpixel(int x, int y) {
		dX = x;
		dY = y;
	}
	
	public void alignSubpixel(double x, double y) {
		dX = x;
		dY = y;
	}
	
	public Dimension getSubpixel() {
		Dimension subpixel = new Dimension();
		subpixel.setSize(dX, dY);
		return subpixel;
	}
	
	public double getSpeed() {
		return Math.sqrt(vX * vX + vY * vY);
	}
}
