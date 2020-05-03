package Tiles;

public class SolidTile extends Tile {
	double friction;
	public SolidTile(int id, int levelColour, String name, double friction) {
		super(id, levelColour, name);
		this.solid = true;
		this.friction = friction;
	}
	public double getFriction() {
		return friction;
	}
}
