package Entities;

import java.awt.Graphics;

import Frame.Level;

public class PowerGenerator extends Entity {

	public PowerGenerator(Level level, boolean active, int x, int y) {
		super(level, active, x, y);
	}

	public void tick() {
		
	}

	public void draw(Graphics g) {
		
	}

	public boolean checkConflict() {
		return false;
	}

}
