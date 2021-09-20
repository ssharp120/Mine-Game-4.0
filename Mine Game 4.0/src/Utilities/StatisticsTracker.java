package Utilities;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.HashMap;

import Frame.GameLoop;

public class StatisticsTracker {
	
	private GameLoop game;
	private Statistic[] basicStats;
	
	public class Statistic {
		String statKey;
		int value;
		
		public Statistic(String statKey, int initialValue) {
			this.statKey = statKey;
			this.value = initialValue;
		}
		
		public String getStatKey() {
			return statKey;
		}
		
		public int getValue() {
			return value;
		}
		
		public void setValue(int i) {
			value = i;
		}
		
		public void incrementValue() {
			value++;
		}
		
		public void decrementValue() {
			value--;
		}
	}
	
	public StatisticsTracker(GameLoop g) {
		game = g;
		basicStats = new Statistic[128];
	}
	
	private boolean overwriteBasicStat(int index, Statistic s) {
		if (checkBasicStatIndex(index)) {basicStats[index] = s; return true;}
		else return false;
	}
	
	public boolean replaceBasicStatIfEmpty(int index, Statistic s) {
		if (checkBasicStatIndex(index) && basicStats[index] == null) {basicStats[index] = s; return true;}
		else return false;
	}
	
	public Statistic getBasicStat(int index) {
		if (checkBasicStatIndex(index)) return basicStats[index];
		else return null;
	}
	
	public int getBasicStatValue(int index) {
		if (checkBasicStatIndex(index) && basicStats[index] != null) return basicStats[index].getValue();
		else return 0;
	}
	
	public void incrementBasicStat(int index) {
		if (checkBasicStatIndex(index) && basicStats[index] != null) basicStats[index].incrementValue();
	}
	
	public void decrementBasicStat(int index) {
		if (checkBasicStatIndex(index) && basicStats[index] != null) basicStats[index].decrementValue();
	}
	
	public boolean incrementBasicStat(String key) {
		boolean successful = false;
		if (basicStats != null) {
			for (Statistic s : basicStats) {
				if (s != null && s.getStatKey() == key) {s.incrementValue(); successful = true;}
			}
		} return successful;
	}
	
	public boolean decrementBasicStat(String key) {
		boolean successful = false;
		if (basicStats != null) {
			for (Statistic s : basicStats) {
				if (s.getStatKey() == key) {s.decrementValue(); successful = true;}
			}
		} return successful;
	}
	
	public void setBasicStatValue(int index, int value) {
		if (checkBasicStatIndex(index) && basicStats[index] != null) basicStats[index].setValue(value);
	}
	
	public BufferedImage getBasicStatGraphics(int index, Font font, Color color) {
		if (checkBasicStatIndex(index) && basicStats[index] != null) {
			BufferedImage image = new BufferedImage(728, font.getSize() + 32, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.setFont(font);
			g.setColor(color);
			String s = "" + basicStats[index].getStatKey() + ": " + basicStats[index].getValue();
			g.drawString(s, 16, font.getSize());
			return image;
		}
		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	}
	
	private boolean checkBasicStatIndex(int index) {
		return basicStats != null && index >= 0 && index < basicStats.length;
	}
	
	public int getBasicStatSlotsFilledInitially() {
		if (basicStats == null) return 0;
		for (int i = 0; i < basicStats.length; i++) {
			if (basicStats[i] == null) return i;
		} return basicStats.length;
	}
	
	public int getBasicStatSlotsFilledTotal() {
		if (basicStats == null) return 0;
		int j = 0;
		for (int i = 0; i < basicStats.length; i++) {
			if (basicStats[i] != null) j++;
		} return j;
	}
	
	public int getMaxBasicStats() {
		return basicStats.length;
	}
	
	public void populateStatLibrary() {
		overwriteBasicStat(1, new Statistic("Jumps", 0));
		overwriteBasicStat(2, new Statistic("Tiles Mined", 0));
		overwriteBasicStat(3, new Statistic("Tiles Placed", 0));
		overwriteBasicStat(4, new Statistic("Times Crafted", 0));
	}
}
