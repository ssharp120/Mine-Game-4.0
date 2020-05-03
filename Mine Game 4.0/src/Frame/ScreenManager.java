package Frame;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class ScreenManager {
	
	private GraphicsDevice vc;
	public ScreenManager() {
		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		vc = e.getDefaultScreenDevice();
	}
	
	// Get the video card's list of DisplayModes //
	public DisplayMode[] getCompatibleDisplayModes() {
		return vc.getDisplayModes();
	}
	
	// Compare our DisplayModes to the video card's DisplayModes //
	public DisplayMode findFirstCompatibleMode(DisplayMode modes[]) {
		DisplayMode goodModes[] = vc.getDisplayModes();
		for(int x = 0; x < modes.length; x++) {
			for(int y = 0; y < goodModes.length; y++) {
				if (displayModesMatch(modes[x], goodModes[y])) {
					return modes[x];
				}
			}
		}
		return null;
	}
	
	public DisplayMode getCurrentDisplayMode() {
		return vc.getDisplayMode();
	}
	
	// Compare two DisplayModes //
	public boolean displayModesMatch(DisplayMode x, DisplayMode y) {
		if (x.getWidth() != y.getWidth() || x.getHeight() != y.getHeight()) {
			return false;
		}
		
		if (x.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI && y.getBitDepth() != DisplayMode.BIT_DEPTH_MULTI && x.getBitDepth() != y.getBitDepth()) {
			return false;
		}
		if (x.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN || y.getRefreshRate() != DisplayMode.REFRESH_RATE_UNKNOWN || x.getRefreshRate() != y.getRefreshRate()) {
			return false;
		}
		
		return true;
	}
	
	public void setFullScreen(DisplayMode dMode) {
		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		frame.setIgnoreRepaint(true);
		frame.setResizable(false);
		vc.setFullScreenWindow(frame);
		
		if (dMode != null && vc.isDisplayChangeSupported()) {
			try {
				vc.setDisplayMode(dMode);
			} catch(Exception e) {System.out.print(e.getMessage());}
		}
		frame.createBufferStrategy(2);
	}
	
	// Get new buffered Graphics object //
	public Graphics2D getGraphics() {
		Window window = vc.getFullScreenWindow();
		if (window != null) {
			BufferStrategy bs = window.getBufferStrategy();
			return (Graphics2D)bs.getDrawGraphics();
		}
		return null;
	}
	
	public void updateDisplay() {
		Window window = vc.getFullScreenWindow();
		if (window != null) {
			BufferStrategy bs = window.getBufferStrategy();
			if (!bs.contentsLost()) {
				bs.show();
			}
		}
	}
	
	public Window getFSWindow() {
		return vc.getFullScreenWindow();
	}
	
	public int getWidth() {
		Window window = vc.getFullScreenWindow();
		if (window != null) {
			return window.getWidth();
		}
		return 0;
	}
	
	public int getHeight() {
		Window window = vc.getFullScreenWindow();
		if (window != null) {
			return window.getHeight();
		}
		return 0;
	}
	
	// Exit fullscreen //
	public void restoreScreen() {
		Window window = vc.getFullScreenWindow();
		if (window != null) {
			window.dispose();
		}
		vc.setFullScreenWindow(null);
	}
	
	// Create image compatible with display //
	public BufferedImage createCompatibleImage(int width, int height, int trans) {
		Window window = vc.getFullScreenWindow();
		if (window != null) {
			GraphicsConfiguration gConf = window.getGraphicsConfiguration();
			return gConf.createCompatibleImage(width, height, trans);
		}
		return null;
	}
}
