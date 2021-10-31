package Entities;

import java.awt.Graphics;
import java.awt.Graphics2D;

import SingleplayerClient.Level;

public abstract class Entity {
	public int x, y;
	public Level level;
	public boolean markedForDeletion;
	public int dispTexture;
	public boolean active = true;
	public int entityIndex;
	
	public Entity(Level level) {
		initialize(level);
	}
	
	public Entity(Level level, boolean active) {
		initialize(level);
		this.active = active;
	}
	
	public Entity(Level level, boolean active, int x, int y) {
		initialize(level);
		this.active = active;
		this.x = x;
		this.y = y;
	}
	
	public Entity(int entityIndex, Level level, boolean active, int x, int y) {
		initialize(level);
		this.active = active;
		this.entityIndex = entityIndex;
		this.x = x;
		this.y = y;
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
	
	public int getEntityIndex() {
		return entityIndex;
	}
	
	public abstract void tick();
	public abstract void draw(Graphics g);
	public abstract boolean checkConflict();
}
