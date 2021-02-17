package Entities;

import java.awt.Graphics;

import Frame.Level;

public abstract class Plant extends Entity {	
	public int plantIdentifier;
	
	public Plant(Level level, boolean active, int plantIdentifier, int x, int y) {
		super(level, active, x, y);
		this.plantIdentifier = plantIdentifier;
	}

	public abstract void tick();
	public abstract void draw(Graphics g);
	public abstract boolean checkConflict();
}
