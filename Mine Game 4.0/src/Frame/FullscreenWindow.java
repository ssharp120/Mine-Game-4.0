package Frame;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.Window;

public class FullscreenWindow {
	// Define possible resolutions
	private static DisplayMode modes[] = {
			new DisplayMode(3840, 2160, 32, 0),
			new DisplayMode(3840, 2160, 24, 0),
			new DisplayMode(3840, 2160, 16, 0),
			
			new DisplayMode(2560, 1440, 32, 0),
			new DisplayMode(2560, 1440, 24, 0),
			new DisplayMode(2560, 1440, 16, 0),
			
			new DisplayMode(2048, 1080, 32, 0),
			new DisplayMode(2048, 1080, 24, 0),
			new DisplayMode(2048, 1080, 16, 0),
			
			new DisplayMode(1920, 1080, 32, 0),
			new DisplayMode(1920, 1080, 24, 0),
			new DisplayMode(1920, 1080, 16, 0),
			
			new DisplayMode(1200, 900, 32, 0),
			new DisplayMode(1200, 900, 24, 0),
			new DisplayMode(1200, 900, 16, 0),
			
			new DisplayMode(1280, 800, 32, 0),
			new DisplayMode(1280, 800, 24, 0),
			new DisplayMode(1280, 800, 16, 0),
			
			new DisplayMode(1366, 768, 32, 0),
			new DisplayMode(1366, 768, 24, 0),
			new DisplayMode(1366, 768, 16, 0),
			
			new DisplayMode(1024, 768, 32, 0),
			new DisplayMode(1024, 768, 24, 0),
			new DisplayMode(1024, 768, 16, 0),
			
			new DisplayMode(1280, 720, 32, 0),
			new DisplayMode(1280, 720, 24, 0),
			new DisplayMode(1280, 720, 16, 0),
			
			new DisplayMode(800, 600, 32, 0),
			new DisplayMode(800, 600, 24, 0),
			new DisplayMode(800, 600, 16, 0),
		};
	
	protected ScreenManager manager;
	
	public void init() {
		// Initialize screen manager object
		manager = new ScreenManager();
		
		// Find the highest compatible resolution and set the fullscreen resolution to it
		DisplayMode dMode = manager.findFirstCompatibleMode(modes);
		manager.setFullScreen(dMode);
		
		// Set the default font and screen color
		Window window = manager.getFSWindow();
		window.setFont(new Font("Consolas", Font.PLAIN, 36));
		window.setBackground(Color.BLUE);
		window.setForeground(Color.WHITE);
	}
}
