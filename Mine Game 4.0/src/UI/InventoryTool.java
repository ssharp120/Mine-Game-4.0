package UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.ImageObserver;

public abstract class InventoryTool extends InventoryItem {

	public InventoryTool(int imageID, double hardness, double maxDurability, double durability, String name,
			int[] effectiveTiles, int[] neutralTiles, int[] ineffectiveTiles) {
		super(imageID, imageID);
		this.hardness = hardness;
		this.maxDurability = maxDurability;
		this.durability = durability;
		this.name = name;
		this.effectiveTiles = effectiveTiles;
		this.neutralTiles = neutralTiles;
		this.ineffectiveTiles = ineffectiveTiles;
		System.out.println("Tool created");
	}

	private double hardness;
	private double maxDurability;
	private double durability;
	private String name;
	private int[] effectiveTiles;
	private int[] neutralTiles;
	private int[] ineffectiveTiles;
	
	public int[] getEffectiveTiles() {
		return effectiveTiles;
	}

	public void setEffectiveTiles(int[] effectiveTiles) {
		this.effectiveTiles = effectiveTiles;
	}

	public int[] getNeutralTiles() {
		return neutralTiles;
	}

	public void setNeutralTiles(int[] neutralTiles) {
		this.neutralTiles = neutralTiles;
	}

	public int[] getIneffectiveTiles() {
		return ineffectiveTiles;
	}

	public void setIneffectiveTiles(int[] ineffectiveTiles) {
		this.ineffectiveTiles = ineffectiveTiles;
	}

	public double getHardness() {
		return hardness;
	}
	
	public void setHardness(double hardness) {
		this.hardness = hardness;
	}
	
	public double getMaxDurability() {
		return maxDurability;
	}
	
	public void setMaxDurability(double maxDurability) {
		this.maxDurability = maxDurability;
	}
	
	public double getDurability() {
		return durability;
	}
	
	public void setDurability(double durability) {
		this.durability = durability;
		if (durability <= 0) durability = 0;
		if (durability >= maxDurability) durability = maxDurability;
		checkBroken();
	}
	
	public void removeDurability(double d) {
		durability -= d;
		if (durability <= 0) durability = 0;
		if (durability >= maxDurability) durability = maxDurability;
		checkBroken();
	}
	
	public void addDurability(double d) {
		durability += d;
		if (durability <= 0) durability = 0;
		if (durability >= maxDurability) durability = maxDurability;
		checkBroken();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getImageID() {
		return imageID;
	}
	
	public void setImageID(int imageID) {
		this.imageID = imageID;
	}
	
	public boolean getMarkedForDeletion() {
		return markedForDeletion;
	}
	
	public void setMarkedForDeletion(boolean markedForDeletion) {
		this.markedForDeletion = markedForDeletion;
	}
	
	public boolean checkBroken() {
		if (durability <= 0) markedForDeletion = true;
		return durability <= 0;
	}
	
	public void draw(Graphics g, int x, int y, int iconWidth, int iconHeight, ImageObserver observer) {
		super.draw(g, x, y, iconWidth, iconHeight, observer);
		g.setColor(new Color(255, 255, 255));
		int offset = iconWidth / 8;
		g.fillRect(x + offset, y + offset + (iconHeight - (2 * offset)), (int) Math.round((iconWidth - (2 * offset)) * (durability / maxDurability)), 4);
		//System.out.println(toString());
	}
}
