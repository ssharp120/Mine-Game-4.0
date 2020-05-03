package Entities;

import java.awt.Graphics;

import Frame.Level;

public abstract class Entity {
	public int x, y;
	public Level level;
	public boolean markedForDeletion;
	public int dispTexture;
	public boolean active = true;
	
	public Entity(Level level) {
		initialize(level);
	}
	
	public Entity(Level level, boolean active) {
		initialize(level);
		this.active = active;
	}
	
	public final void initialize(Level level) {
		this.level = level;
	}
	
	public void switchLevel(Level level) {
		this.level = level;
	}
	
	public void resetPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public abstract void tick();
	public abstract void draw(Graphics g);
	public abstract boolean checkConflict();
}
