package UI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import Frame.InputHandler;
import Libraries.MediaLibrary;
import Utilities.PhysicsUtilities;

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
	
	private Button returnButton;
	
	Color rainbowColor;
	Color greenColor = new Color(0, 255, 55);
	
	private class Button {
		int imageID;
		int x;
		int y;
		int width;
		int height;
		String text;
		Font font;
		
		public Button(int imageID, int x, int y, int width, int height, String text, Font font) {
			this.imageID = imageID;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.text = text;
			this.font = font;
		}
		
		public void draw(Graphics2D g) {
			Image i = MediaLibrary.getImageFromLibrary(imageID);
			int iWidth = i.getWidth(null);
			int iHeight = i.getHeight(null);
			g.drawImage(MediaLibrary.getBufferedImageFromLibrary(imageID).getSubimage(0, 0, 2, iHeight), x, y, 2, height, observer);
			g.drawImage(MediaLibrary.getBufferedImageFromLibrary(imageID).getSubimage(2, 0, iWidth - 4, iHeight), x + 2, y, width - 4, height, observer);
			g.drawImage(MediaLibrary.getBufferedImageFromLibrary(imageID).getSubimage(iWidth - 2, 0, 1, iHeight), x + width - 3, y, 2, height, observer);
			g.setFont(font);
			g.setColor(new Color(66, 66, 66));
			g.drawString(text, x + width/2 - g.getFontMetrics(font).stringWidth(text)/2, y + 2*height/3);
		}
		
	}
	
	public PauseMenuGUI(ImageObserver observer, InputHandler controls) {
		this.controls = controls;
		this.observer = observer;
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
		if (controls.esc.isPressed()) {isActive = false; controls.esc.toggle(false); return;}
		ticks++;
		if (ticks > 255 * 10) ticks = 1;
		
		rainbowColor = new Color(Color.HSBtoRGB(((float) ticks) / 255, 0.7F, 0.4F));
		
		if (!toggleMonospace && controls.alt.isPressed()) {
			displayMonospace = !displayMonospace;
			toggleMonospace = true;
		} else if (!controls.alt.isPressed()) {
			toggleMonospace = false;
		}
	}
	
	public void draw(Graphics2D g, int screenWidth, int screenHeight) {
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
		
		returnButton = new Button(5012, 3 * screenWidth / 8, screenHeight / 4, screenWidth / 4, 64, "Return to game", MediaLibrary.getFontFromLibrary("ButtonFont"));
		returnButton.draw(g);
	}
	
	public void handleClick(int clickX, int clickY) { // Location on canvas
		//System.out.println("Click location: " + clickX + ", " + clickY + "; Target bounds:"
				//+ "\nHorizontal: " + clockX + "-" + (clockX + clockWidth)
				//+ "\nVertical: " + clockY + "-" + (clockY + clockHeight));
		if (clickX > clockX && clickX < clockX + clockWidth) {
			if (clickY > clockY - clockHeight / 2 && clickY < clockY) {
				displayLongTime = !displayLongTime;
			}
		}
		if (PhysicsUtilities.checkIntersection(clickX, clickY, returnButton.x, returnButton.y, returnButton.width, returnButton.height, true)) setActive(false);
	}
	
	public void handleScroll(int scrollDelta) {
		clockHue += 0.01F * scrollDelta;
		if (clockHue > 1.0F) clockHue = 0F;
		if (clockHue < 0F) clockHue = 1.0F;
	}
}
