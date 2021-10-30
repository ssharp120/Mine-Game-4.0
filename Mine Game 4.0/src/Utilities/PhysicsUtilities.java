package Utilities;

import Frame.Level;
import Tiles.Platform;
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
		for (int i = 1; i < (hitboxWidth / 2) - (hitboxWidth / 8); i++) {
			Tile tile = level.getTile((5 + x + 2*i)/32, (y+1)/32);
			// Resolve other tile interactions //
			if(tile.isSolid() && tile.getClass() != Platform.class) {
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
		int delta = 0;
		if (hitboxHeight > 32) delta = (hitboxHeight) / 8 - 4;
		if (delta > 8) delta = 8;
		for (int i = 1; i < (hitboxWidth / 2) - (hitboxWidth / 8); i++) {
			Tile tile = level.getTile((5 + x + 2*i)/32, (y + hitboxHeight - delta)/32);
			// Resolve other tile interactions //
			if(tile.isSolid()) {
				if (tile.getClass() == Platform.class) {
					Tile tileLow = level.getTile((5 + x + 2*i)/32, (y + hitboxHeight + 16)/32);
					if (tileLow.isSolid()) return true;
					continue;
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean arePlatformsAbove(int x, int y, int hitboxWidth, int hitboxHeight, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (y < 0) {
			return false;
		}
		if (Math.abs((x + 32)/32) >= level.width || Math.abs((y + hitboxHeight)/32) >= level.height || y < 0) {
			return false;
		}
		for (int i = 1; i < (hitboxWidth / 2) - (hitboxWidth / 8); i++) {
			Tile tile = level.getTile((5 + x + 2*i)/32, (y+1)/32);
			// Resolve other tile interactions //
			if(tile.isSolid() && tile.getClass() != Platform.class) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean arePlatformsBelow(int x, int y, int hitboxWidth, int hitboxHeight, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (Math.abs((y + hitboxHeight)/32) >= level.height) {
			return false;
		}
		if (Math.abs((x + 32)/32) >= level.width || x < 0 || y < 0) {
			return false;
		}
		int delta = 0;
		if (hitboxHeight > 32) delta = (hitboxHeight) / 8 - 4;
		if (delta > 8) delta = 8;
		for (int i = 1; i < (hitboxWidth / 2) - (hitboxWidth / 8); i++) {
			Tile tile = level.getTile((5 + x + 2*i)/32, (y + hitboxHeight - delta)/32);
			// Resolve other tile interactions //
			if(tile.isSolid() && tile.getClass() != Platform.class) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean collisionRight(int x, int y, int hitboxWidth, int hitboxHeight, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (Math.abs((x + hitboxWidth)/32) >= level.width) {
			return true;
		}
		if (Math.abs((y + hitboxHeight)/32) >= level.height || x < 0 || y < 0) {
			return false;
		}
		for (int i = 1; i < (hitboxHeight / 4) - (hitboxHeight / 32); i++) {
			Tile tile = level.getTile((x + hitboxWidth - 2)/32, (y + 4*i)/32);
			// Resolve other tile interactions //
			if(tile.isSolid() && tile.getClass() != Platform.class) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean collisionLeft(int x, int y, int hitboxWidth, int hitboxHeight, Level level) {
		if (level == null) { 
			throw new RuntimeException("No Level");
		}
		if (x <= 0) {
			return true;
		}
		if (Math.abs((x + 32)/32) >= level.width || Math.abs((y + hitboxHeight)/32) >= level.height || y < 0) {
			return false;
		}
		for (int i = 1; i < (hitboxHeight / 4) - (hitboxHeight / 32); i++) {
			Tile tile = level.getTile((x + 2)/32, (y + 4*i)/32);
			// Resolve other tile interactions //
			if(tile.isSolid() && tile.getClass() != Platform.class) {
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
	
	public static boolean checkIntersection(int x, int y, int targetX, int targetY, int targetWidth, int targetHeight, boolean includeEdges) {
		if (includeEdges) return (x >= targetX && x <= targetX + targetWidth && y >= targetY && y <= targetY + targetHeight);
		else return (x > targetX && x < targetX + targetWidth && y > targetY && y < targetY + targetHeight);
	}
}
