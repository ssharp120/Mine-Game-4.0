package Tiles;

public class RandomizedTile extends Tile {
	public int startingID;
	public int possibleValues[];
	
	public RandomizedTile(int id, int levelColour, String name, int possibleValues[], boolean solid) {
		super(id, levelColour, name);
		this.solid = solid;
		startingID = id;
		this.possibleValues = possibleValues;
	}
}
