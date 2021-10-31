package Entities;

import java.awt.Dimension;
import java.awt.Font;

import SingleplayerClient.Level;
import UI.Inventory;

public abstract class Mob extends Entity {
	public final Font USERNAME = new Font("Comic Sans MS", Font.PLAIN, 16);
	protected String name;
	protected int speed;
	protected int numSteps = 0;
	protected int distTraveled = 0;
	protected boolean isMoving;
	protected int movingDir = 2;
	protected double baseHealth;
	protected double health;
	protected int hitboxWidth;
	protected int hitboxHeight;
	
	/* 0 = up
	 * 1 = down
	 * 2 = left
	 * 3 = right
	 */
	protected int scale = 1;
	public Mob(Level level, String name, int x, int y, int speed, int baseHealth, int hitboxWidth, int hitboxHeight) {
		super(level);
		this.name = name;
		this.x = x;
		this.y = y;
		this.speed = speed;
		this.baseHealth = baseHealth;
		this.health = baseHealth;
		this.hitboxWidth = hitboxWidth;
		this.hitboxHeight = hitboxHeight;
	}
	
	public void move(int deltaX, int deltaY) {
		if(deltaX != 0 && deltaY != 0) {
			move(deltaX, 0);
			move(0, deltaY);
			numSteps--;
			return;
		}
		distTraveled += (Math.sqrt((deltaX * speed)^2 + (deltaY * speed)^2));
		numSteps++;
		if (!hasCollided(deltaX, deltaY)) {
			if (deltaY < 0) movingDir = 0;
			if (deltaY > 0) movingDir = 1;
			if (deltaX < 0) movingDir = 2;
			if (deltaX > 0) movingDir = 3;
			
			x += deltaX * speed;
			y += deltaY * speed;
		}
	}
	
	public String getName() {
		return name;
	}
	
	public abstract boolean hasCollided(int deltaX, int deltaY);

	public void setNumSteps(int numSteps) {
		this.numSteps = numSteps;
	}

	public void setMoving(boolean isMoving) {
		this.isMoving = isMoving;
	}

	public void setMovingDir(int movingDir) {
		this.movingDir = movingDir;
	}
	
	public void damage() {
		health -= 1.0;
		checkDeath();
	}
	
	public void damage(double d) {
		health -= d;
		checkDeath();
	}
	
	public void checkDeath() {
		if (health <= 0) {
			markedForDeletion = true;
		}
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
	
	public int getHitboxWidth() {
		return hitboxWidth;
	}
	
	public int getHitboxHeight() {
		return hitboxHeight;
	}
	
	public Dimension[] getHitboxVectors() {
		return new Dimension[] { new Dimension(hitboxWidth, 0), new Dimension(0, hitboxHeight) };
	}
	
	public Dimension getPositionVector() {
		return new Dimension(x, y);
	}
	
	public Dimension getHitboxDimensione() {
		return new Dimension(hitboxWidth, hitboxHeight);
	}
}

