package Entities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import Libraries.MediaLibrary;
import SingleplayerClient.GameLoop;
import SingleplayerClient.Level;
import Tiles.Tile;
import UI.InventoryTile;
import Utilities.FileUtilities;

public class PowerDrill extends ElectricalDevice {
	public int currentYLevel;
	
	public PowerDrill(Level level, boolean active, int x, int y) {
		super(5, level, active, x, y, 120, 12, 50, 1, 23, 4);
		currentYLevel = y;
	}

	public void tick() {
		if (currentPowerStorage > 0 && level != null && level.getGameLoop() != null && level.getGameLoop().ticks % 100 == 0 && currentYLevel < level.height && x > 0 && x < level.width - 1) {
			int miningTileID = level.getTile(x, currentYLevel).getId();
			if (currentYLevel - y > 2 && miningTileID > 2) {
				level.getGameLoop().player.inventory.addItem(new InventoryTile(level.getTile(x, currentYLevel).getId(), 1));
				level.setTile(x, currentYLevel, Tile.SKY.getId());
				for (int i = -1; i <= 1; i++) {
					level.exploreTile(x + i,  currentYLevel);
				}
			}
			FileUtilities.log("Mined tile " + Tile.tiles[miningTileID].getName() + " at x = " + x + ", y = " + currentYLevel, true);
			currentYLevel++;
			currentPowerStorage -= 60;
		}
	}

	public void draw(Graphics g) {
		g.drawImage(MediaLibrary.getImageFromLibrary(7007), (x << 5) - level.getGameLoop().xOffset - 2, (y << 5) - level.getGameLoop().yOffset - 16, level.getGameLoop());
		GameLoop game = level.getGameLoop();
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.GRAY);
		g2.drawLine((x << 5) - game.xOffset + 36 / 2 - 2, (y << 5) - game.yOffset + 43 - 16, (x << 5) - game.xOffset + 36 / 2 - 2, (currentYLevel << 5) - game.yOffset);
		
		displayPowerCapacity(g);
		drawConnections(g);
	}

	public boolean checkConflict() {
		return false;
	}

}
