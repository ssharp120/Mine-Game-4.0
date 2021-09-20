package Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import Frame.Level;
import Libraries.MediaLibrary;

public class Dummy extends Mob {
	
	private BufferedImage texture = MediaLibrary.getBufferedImageFromLibrary(7700);
	int hitboxWidth = 0;
	int hitboxHeight = 0;

	public Dummy(Level level, int x, int y) {
		super(level, "Dummy #" + System.currentTimeMillis(), x, y, 0, 100, 64, 128);
	}

	public boolean hasCollided(int deltaX, int deltaY) {
		return false;
	}

	public void tick() {
		hitboxHeight = texture.getHeight();
		hitboxWidth = texture.getWidth();
	}

	public void draw(Graphics g) {
		g.drawImage(texture, x - level.getGameLoop().xOffset, y - level.getGameLoop().yOffset, texture.getWidth(), texture.getHeight(), level.getGameLoop());
		System.out.println(hitboxWidth);
		g.setColor(Color.LIGHT_GRAY);
		g.drawString(this.name, x + (hitboxWidth / 2) - (g.getFontMetrics().stringWidth(this.name) / 2) - level.getGameLoop().xOffset,
				y - level.getGameLoop().yOffset - hitboxHeight + 128 - 48);
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.DARK_GRAY);
		g2.setStroke(new BasicStroke(4));
		g2.drawLine(x - level.getGameLoop().xOffset + (hitboxWidth / 2) - (g.getFontMetrics().stringWidth(this.name) / 2), y - level.getGameLoop().yOffset - hitboxHeight + 128 - 24, 
				(int) (x - level.getGameLoop().xOffset + (hitboxWidth / 2) - (g.getFontMetrics().stringWidth(this.name) / 2)
						+ this.getHealth() / this.baseHealth * g.getFontMetrics().stringWidth(this.name)), y - level.getGameLoop().yOffset - hitboxHeight + 128 - 24);
	}

	@Override
	public boolean checkConflict() {
		return false;
	}

}
