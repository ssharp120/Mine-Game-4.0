package Utilities;

import Frame.Level;
import Tiles.Tile;

public class PhysicsUtilities {
	public static boolean collisionAbove(int x, int y, int hitboxWidth, int hitboxHeight, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (y < 0) {
			return true;
		}
		if (Math.abs((x + 32)/32) >= level.width || Math.abs((y + hitboxHeight)/32) >= level.height || y < 0) {
			return false;
		}
		for (int i = 1; i < (hitboxWidth / 2) - 5; i++) {
			Tile tile = level.getTile((5 + x + 2*i)/32, (y+1)/32);
			// Resolve other tile interactions //
			if(tile.isSolid()) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean collisionBelow(int x, int y, int hitboxWidth, int hitboxHeight, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (Math.abs((y + hitboxHeight)/32) >= level.height) {
			return true;
		}
		if (Math.abs((x + 32)/32) >= level.width || x < 0 || y < 0) {
			return false;
		}
		for (int i = 1; i < (hitboxWidth / 2) - 5; i++) {
			Tile tile = level.getTile((5 + x + 2*i)/32, (y + hitboxHeight - 8)/32);
			// Resolve other tile interactions //
			if(tile.isSolid()) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean collisionRight(int x, int y, int hitboxWidth, int hitboxHeight, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (Math.abs((x + 32)/32) >= level.width) {
			return true;
		}
		if (Math.abs((y + hitboxHeight)/32) >= level.height || x < 0 || y < 0) {
			return false;
		}
		for (int i = 1; i < (hitboxHeight / 4) - 2; i++) {
			Tile tile = level.getTile((x + hitboxWidth - 2)/32, (y + 4*i)/32);
			// Resolve other tile interactions //
			if(tile.isSolid()) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean collisionLeft(int x, int y, int hitboxWidth, int hitboxHeight, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (y < 0) {
			return true;
		}
		if (Math.abs((x + 32)/32) >= level.width || Math.abs((y + hitboxHeight)/32) >= level.height || x < 0) {
			return false;
		}
		for (int i = 1; i < (hitboxHeight / 4) - 2; i++) {
			Tile tile = level.getTile((x + 2)/32, (y + 4*i)/32);
			// Resolve other tile interactions //
			if(tile.isSolid()) {
				return true;
			}
		}
		return false;
	}
	
	public static Tile findTile(int x, int y, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (x < 0 || y < 0 || x > level.width << 5 || y > level.height << 5) {
			return null;
		}
		Tile tile = level.getTile(x >> 5, y >> 5);
		return tile;
	}
}
