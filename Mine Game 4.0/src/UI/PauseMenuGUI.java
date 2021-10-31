package UI;

import Utilities.StatisticsTracker.*;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import Entities.Entity;
import Libraries.MediaLibrary;
import SingleplayerClient.GameLoop;
import SingleplayerClient.InputHandler;
import Utilities.PhysicsUtilities;
import Utilities.StatisticsTracker;

public class PauseMenuGUI {
	private boolean isActive;
	private ImageObserver observer;
	private InputHandler controls;
	private short ticks;
	private boolean toggleMonospace;
	private boolean displayMonospace;
	private int clockX, clockY, clockWidth, clockHeight;
	private DateTimeFormatter formatter;
	private boolean displayLongTime = true;
	private float clockHue = 0.3F;
	private boolean initialEsc = true;
	private int selectedButtonIndex;
	private int screenWidth;
	private int screenHeight;
	private GameLoop game;
	private boolean displayStatisticsView;
	private StatisticsTracker tracker;
	
	private List<Button> buttons = new ArrayList<Button>();
	
	Color rainbowColor;
	Color greenColor = new Color(0, 255, 55);
	
	private class Button {
		int imageID;
		String buttonID;
		int x;
		int y;
		int width;
		int height;
		String text;
		Font font;
		
		public Button(String buttonID, int imageID, int x, int y, int width, int height, String text, Font font) {
			this.buttonID = buttonID;
			this.imageID = imageID;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.text = text;
			this.font = font;
		}
		
		public void buttonAction() {
			if (buttonID == "Return Button") {
				if (!displayStatisticsView) setActive(false);
				else displayStatisticsView = false;
			}
			else if (buttonID == "Fullscreen Button") game.switchDisplayMode();
			else if (buttonID == "Exit Button") {
				if (game.fullscreen) game.switchDisplayMode();
				game.exitDialog();
			}
			else if (buttonID == "Statistics Button") {
				displayStatisticsView = !displayStatisticsView;
			}
			else if (buttonID == "Clear Inventory Button") {
				if (game.player != null && game.player.inventory != null) {
					if (game.fullscreen || JOptionPane.showConfirmDialog(game.frame, 
				            "Are you sure to want to clear inventory?", "Clear Inventory?", 
				            JOptionPane.YES_NO_OPTION,
				            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) game.player.inventory.clearInventory();
				}
			}
		}
		
		public String getID() {
			return buttonID;
		}
		
		public void draw(Graphics2D g) {
			Image i = MediaLibrary.getImageFromLibrary(imageID);
			int iWidth = i.getWidth(null);
			int iHeight = i.getHeight(null);
			
			if (buttonID == "Return Button") {
				if (displayStatisticsView) text = "Return to Menu";
				else text = "Return to Game";
			}
			
			g.drawImage(MediaLibrary.getBufferedImageFromLibrary(imageID).getSubimage(0, 0, 2, iHeight), x, y, 2, height, observer);
			g.drawImage(MediaLibrary.getBufferedImageFromLibrary(imageID).getSubimage(2, 0, iWidth - 4, iHeight), x + 2, y, width - 4, height, observer);
			g.drawImage(MediaLibrary.getBufferedImageFromLibrary(imageID).getSubimage(iWidth - 2, 0, 1, iHeight), x + width - 3, y, 2, height, observer);
			
			g.setFont(font);
			
			if (game.fullscreen && buttonID == "Clear Inventory Button") g.setColor(new Color(95, 55, 55));
			else g.setColor(new Color(66, 66, 66));
			
			g.drawString(text, x + width/2 - g.getFontMetrics(font).stringWidth(text)/2, y + 2*height/3);
		}
		
	}
	
	public PauseMenuGUI(GameLoop game, ImageObserver observer, InputHandler controls, StatisticsTracker tracker) {
		this.game = game;
		this.controls = controls;
		this.observer = observer;
		this.tracker = tracker;
		tracker.populateStatLibrary();
	}
	
	public void initButtons() {
		constructAddButton("Return Button", 5012, 3 * screenWidth / 8, screenHeight / 4, screenWidth / 4, 64, "Return to game", MediaLibrary.getFontFromLibrary("ButtonFont"));
		constructAddButton("Fullscreen Button", 5012, 3 * screenWidth / 8, screenHeight / 4 + 64 + 32, screenWidth / 4, 64, "Fullscreen " + (game.fullscreen ? "ON" : "OFF"), MediaLibrary.getFontFromLibrary("ButtonFont"));
		constructAddButton("Statistics Button", 5012, 3 * screenWidth / 8, screenHeight / 4 + 64*2 + 32*2, screenWidth / 4, 64, "Statistics", MediaLibrary.getFontFromLibrary("ButtonFont"));
		constructAddButton("Clear Inventory Button", 5012, 3 * screenWidth / 8, screenHeight / 4 + 64*3 + 32*3, screenWidth / 4, 64, "Clear Inventory", MediaLibrary.getFontFromLibrary("ButtonFont"));
		
		constructAddButton("Exit Button", 5012, 3 * screenWidth / 8, screenHeight - 64 - 32, screenWidth / 4, 64, "Exit Game ", MediaLibrary.getFontFromLibrary("ButtonFont"));
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	public void toggleActive() {
		isActive = !isActive;
	}
	
	public void tick() {
		if (controls.esc.isPressed()) {
			if (!displayStatisticsView)	{isActive = false; controls.esc.toggle(false); return;}
			else displayStatisticsView = false;
		}
		ticks++;
		if (ticks > 255 * 10) ticks = 1;
		
		screenWidth = game.getDrawResolution().width;
		screenHeight = game.getDrawResolution().height;
		
		initButtons();
		
		rainbowColor = new Color(Color.HSBtoRGB(((float) ticks) / 255, 0.7F, 0.4F));
		
		if (!toggleMonospace && controls.alt.isPressed()) {
			displayMonospace = !displayMonospace;
			toggleMonospace = true;
		} else if (!controls.alt.isPressed()) {
			toggleMonospace = false;
		}
	}
	
	public synchronized void addButton(Button b) {
		buttons.add(b);
	}
	
	public synchronized void constructAddButton(String buttonID, int imageID, int x, int y, int width, int height, String text, Font font) {
		buttons.removeIf(i -> i.getID() == buttonID);
		buttons.add(new Button(buttonID, imageID, x, y, width, height, text, font));
	}
	
	public synchronized void removeButton(Button b) {
		buttons.add(b);
	}
	
	public synchronized void draw(Graphics2D g, int screenWidth, int screenHeight) {
		for (int i = 0; i < (screenWidth >> 5) + 1; i++) {
			for (int j = 0; j < (screenWidth >> 5) + 1; j++) {
				//g.drawImage(MediaLibrary.getBufferedImageFromLibrary(5011), i << 5, j << 5, observer);
			}
		}
		
		if (displayLongTime) formatter = DateTimeFormatter.ofPattern("hh:mm:ss"); 
		else formatter = DateTimeFormatter.ofPattern("hh:mm");
		
		if (displayMonospace) {
			if (displayLongTime) formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
			else formatter = DateTimeFormatter.ofPattern("HH:mm");
			g.setFont(MediaLibrary.getFontFromLibrary("Monospace Numbering XL"));
		}
		else g.setFont(MediaLibrary.getFontFromLibrary("Offset Numbering XL"));
		
		LocalDateTime currentLocalDateTime = LocalDateTime.now();  
		String currentTime = formatter.format(currentLocalDateTime);
		
		
		clockX = screenWidth/2 - g.getFontMetrics(g.getFont()).stringWidth(currentTime)/2;
		clockY = g.getFontMetrics(g.getFont()).getHeight() + 32;
		
		clockWidth = g.getFontMetrics(g.getFont()).stringWidth(currentTime);
		clockHeight = g.getFontMetrics(g.getFont()).getHeight();
		
		g.setColor(new Color(0, 0, 0));
		g.drawRect(clockX - 32, clockY - clockHeight / 2 - 32, clockWidth + 64, clockHeight);
		g.setColor(new Color(32, 32, 32));
		g.fillRect(clockX - 30, clockY - clockHeight / 2 - 30, clockWidth + 61, clockHeight);
		
		g.setColor(new Color(Color.HSBtoRGB(clockHue, 1F, 0.8F)));
		g.drawString(currentTime, clockX, clockY);
		
		clockX = screenWidth/2 - g.getFontMetrics(g.getFont()).stringWidth(
				DateTimeFormatter.ofPattern("HH:mm:ss").format(currentLocalDateTime)
				)/2;
		
		clockWidth = g.getFontMetrics(g.getFont()).stringWidth(
				DateTimeFormatter.ofPattern("HH:mm:ss").format(currentLocalDateTime)
				);
		
		if (!displayStatisticsView) {			
			for (Button b : buttons) b.draw(g);
		} else {
			for (Button b : buttons) {
				if (b.getID() == "Return Button") b.draw(g);
			}
			g.setFont(MediaLibrary.getFontFromLibrary("Numbering"));
			displayStatistics(g, observer);
		}
	}
	
	public void displayStatistics(Graphics2D g, ImageObserver observer) {
		int j = screenHeight / 4;
		for (int i = 0; i < tracker.getMaxBasicStats(); i++) {
			BufferedImage image = tracker.getBasicStatGraphics(i, MediaLibrary.getFontFromLibrary("StatisticFont"), Color.white);
			j += image.getHeight();
		}
		
		displayStatisticsBackground(g, observer, j);
		
		j = screenHeight / 4;
		for (int i = 0; i < tracker.getMaxBasicStats(); i++) {
			BufferedImage image = tracker.getBasicStatGraphics(i, MediaLibrary.getFontFromLibrary("StatisticFont"), Color.white);
			j += image.getHeight();
			g.drawImage(image, screenWidth / 4, j, observer);
		}
	}
	
	public void displayStatisticsBackground(Graphics2D g, ImageObserver observer, int height) {
		g.setColor(new Color(32, 32, 32));
		g.setComposite(AlphaComposite.SrcOver.derive(0.8f));
		g.fillRect(screenWidth / 4, screenHeight / 4 + tracker.getBasicStatGraphics(1, g.getFont(), Color.white).getHeight(), screenWidth / 2, height - screenHeight / 4);
		
		g.setColor(new Color(100, 100, 100));
		g.drawRect(screenWidth / 4, screenHeight / 4 + tracker.getBasicStatGraphics(1, g.getFont(), Color.white).getHeight(), screenWidth / 2, height - screenHeight / 4);
	}
	
	public synchronized void handleClick(int clickX, int clickY) { // Location on canvas
		//System.out.println("Click location: " + clickX + ", " + clickY + "; Target bounds:"
				//+ "\nHorizontal: " + clockX + "-" + (clockX + clockWidth)
				//+ "\nVertical: " + clockY + "-" + (clockY + clockHeight));
		if (clickX > clockX && clickX < clockX + clockWidth) {
			if (clickY > clockY - clockHeight / 2 && clickY < clockY) {
				displayLongTime = !displayLongTime;
			}
		}
		for (Button b : buttons) {
			if (PhysicsUtilities.checkIntersection(clickX, clickY, b.x, b.y, b.width, b.height, true)) b.buttonAction();
		}
	}
	
	public void handleScroll(int scrollDelta) {
		clockHue += 0.01F * scrollDelta;
		if (clockHue > 1.0F) clockHue = 0F;
		if (clockHue < 0F) clockHue = 1.0F;
	}
}
