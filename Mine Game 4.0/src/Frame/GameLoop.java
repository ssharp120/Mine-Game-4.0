package Frame;

import static Utilities.FileUtilities.*;
import Utilities.StatisticsTracker.*;
import Utilities.TechTree;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Client.PreloadDialog;
import Entities.OxygenGenerator;
import Entities.Player;
import Frame.InputHandler.ControlScheme;
import Libraries.AttributeLibrary;
import Libraries.MediaLibrary;
import Libraries.RecipeLibrary;
import Libraries.StructureLibrary;
import Tiles.BackgroundDestructibleTile;
import Tiles.DestructibleTile;
import Tiles.Tile;
import UI.BasicCraftingGUI;
import UI.Ingredient;
import UI.InventoryItem;
import UI.PauseMenuGUI;
import UI.TechTreeGUI;
import UI.WorkbenchGUI;
import Utilities.AudioManager;
import Utilities.Calendar;
import Utilities.FileUtilities;
import Utilities.LevelFactory;
import Utilities.StatisticsTracker;

public class GameLoop extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private boolean running = true;
	private int MAXFPS = 165;
	public int FPS, UPS;
	
	public Dimension resolution;
	public Dimension drawResolution;
	public boolean startFullscreen;
	public boolean fullscreen;
	
	public FullscreenWindow window;
	public JFrame frame;
	
	public long ticks;
	
	public InputHandler input;
	public AudioManager audioManager;
	
	public Level level;
	
	public int xOffset, yOffset;
	public int currentWorldValue;
	
	public Player player;
	
	public boolean displayTileInfo;
	public Tile displayTile;
	public double tileDurability;
	
	public boolean displayFog = true;
	public boolean displayUIs = false;
	public boolean displayConveyorSpeeds = true;
	
	public boolean drawHUD;
	public boolean drawMiniMap = true;
	public int miniMapScale = 4;

	public Graphics gStorage;
	
	private TechTree techTree;
	
	public BasicCraftingGUI basicCraftingGUI;
	public WorkbenchGUI workbenchGUI;
	public PauseMenuGUI pauseMenuGUI;
	public TechTreeGUI techTreeGUI;
	
	public StatisticsTracker tracker;
	
	public static void main(String[] args) {
		new PreloadDialog();
	}

	public void tick() {
		ticks++;
		if (pauseMenuGUI != null && pauseMenuGUI.isActive()) {input.setControlScheme(ControlScheme.PAUSE_MENU); pauseMenuGUI.tick(); return;}
		if (techTreeGUI != null && techTreeGUI.isActive()) {input.setControlScheme(ControlScheme.TECH_TREE); techTreeGUI.tick(input, ticks % 2 == 0, player.inventory); return;}
		
		if (input != null && input.esc.isPressed()) {pauseMenuGUI.setActive(true); input.esc.toggle(false); return;}
				
		checkFullscreenFocus();
		level.tick();
		
		if (input.getControlScheme() == ControlScheme.BASIC_CRAFTING && basicCraftingGUI != null) {
			if (input.up.isPressed() && input.down.isPressed()) {
				// Do nothing
			} else if (input.up.isPressed()) {
				basicCraftingGUI.handleScroll(-1);
			} else if (input.down.isPressed()) {
				basicCraftingGUI.handleScroll(1);
			}
			if (input.home.isPressed()) basicCraftingGUI.returnToTop();
			if (input.end.isPressed()) basicCraftingGUI.scrollToBottom();
		}
		
		if (input.getControlScheme() == ControlScheme.GAMEPLAY && input.alt.isPressed()) displayConveyorSpeeds = true;
		else displayConveyorSpeeds = false;
	}
	
	public void resetWindow() {
		frame.remove(this);
		frame.setVisible(false);
		frame.setEnabled(false);
		frame = null;
		frame = new JFrame("Mine Game 4.0");
        frame.setIconImage(loadImage("icon.png"));
		frame.setResizable(true);
		frame.setMinimumSize(new Dimension(800, 600));
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	Runtime.getRuntime().gc();
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure to want to exit the game?", "Quit now?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		        	exitSequence();
		            System.exit(0);
		        }
		    }
		});
		frame.setBackground(Color.BLUE);
		frame.add(this);
		
		checkDisplayMode();
	}
	
	public void exitDialog() {
		if (JOptionPane.showConfirmDialog(frame, 
	            "Are you sure to want to exit the game?", "Quit now?", 
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
	        	exitSequence();
	            System.exit(0);
	        }
	}
	
	public void checkFullscreenFocus() {
		if (fullscreen) {
			if (window != null && !window.manager.getFSWindow().isFocused()) {
				setWindowed();
				return;
			}
			if (window != null && !window.manager.getFSWindow().isShowing()) {
				setWindowed();
				Runtime.getRuntime().gc();
				if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure to want to exit the game?", "Quit now?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
					exitSequence();
		            System.exit(0);
				}
			}
		}
	}
	
	public GameLoop(Dimension resolution, boolean startFullscreen) {
		FileUtilities.log("Initializing Mine Game 4.0" + "\n");
		
		// Set initial parameters
		
		this.resolution = resolution;
		this.startFullscreen = startFullscreen;
		this.fullscreen = startFullscreen;
		
		setMinimumSize(resolution);
		setMaximumSize(resolution);
		setPreferredSize(resolution);
		setFocusable(true);
		
		/* Initialize the containing JFrame	and set its parameters	
		 * The JFrame will remain active but will become invisible 
		 * if the constructor starts in fullscreen mode.		 
		 */
		frame = new JFrame("Mine Game 4.0");
        frame.setIconImage(loadImage("icon.png"));
		frame.setResizable(true);
		frame.setMinimumSize(new Dimension(800, 600));
		
		// Set the default close event to do nothing so we can run a custom dialog
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		// Set a custom dialog confirming exit intention
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	Runtime.getRuntime().gc();
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure to want to exit the game?", "Quit now?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		        	exitSequence();
		            System.exit(0);
		        }
		    }
		});
		
		// Set the background to blue for debugging purposes
		frame.setBackground(Color.BLUE);
		
		// Finally add the game panel to the frame
		frame.add(this);
		
		checkDisplayMode();
		
		initializeGameElements();
		initializeGUIs();
	}
	
	public void exitSequence() {
		LevelFactory.saveLevel("save_" + FileUtilities.TIMESTAMP_AT_RUNTIME + ".png", 0, 0, level);
		FileUtilities.log("Exit successful, see you next time.");
		FileUtilities.closeLog();
	}
	
	public void switchDisplayMode() {
		fullscreen = !fullscreen;
		checkDisplayMode();
	}
	
	public void setFullscreen() {
		fullscreen = true;
		checkDisplayMode();
	}
	
	public void setWindowed() {
		fullscreen = false;
		checkDisplayMode();
	}
	
	public void checkDisplayMode() {
		if (fullscreen) {
			frame.setVisible(false);
			window = new FullscreenWindow();
			window.init();
			window.manager.getFSWindow().setIconImage(loadImage("icon.png"));
			window.manager.getFSWindow().setName("Mine Game 3.0");
			window.manager.getFSWindow().setFocusable(true);
			window.manager.getFSWindow().requestFocusInWindow();
			window.manager.getFSWindow().addKeyListener(this);
			window.manager.getFSWindow().addMouseListener(this);
			window.manager.getFSWindow().addMouseMotionListener(this);
			window.manager.getFSWindow().addMouseWheelListener(this);
		} else {
			if (window != null) {
				window.manager.restoreScreen();
			}
			addKeyListener(this);
			addMouseListener(this);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			frame.pack();
		}
	}
	
	public void initializeGameElements() {
		input = new InputHandler(this);
		MediaLibrary.populateSoundLibrary();
		MediaLibrary.populateImageLibrary();
		MediaLibrary.populateFontLibrary();
		AttributeLibrary.populateLevelLibrary();
		AttributeLibrary.populateIngredientLibraries();
		RecipeLibrary.populateRecipeLibrary();
		StructureLibrary.populateStructureLibrary();
		techTree = new TechTree();
		audioManager = new AudioManager();
		
		Calendar.prepareCalendar(System.currentTimeMillis());
		level = new Level(LevelFactory.generateLevel(0, this), "Generated", 0, this, (40 + 16) * 32, 526*32);
		player = new Player(level, "Test", level.spawnX, level.spawnY, input);
		level.addEntity(player);
		
		level.populatePlants();
		level.addEntity(new OxygenGenerator(level, true, 60, 528));
	}
	
	public void initializeGUIs() {
		workbenchGUI = new WorkbenchGUI();
		basicCraftingGUI = new BasicCraftingGUI(this);
		tracker = new StatisticsTracker(this);
		pauseMenuGUI = new PauseMenuGUI(this, this, input, tracker);
		techTreeGUI = new TechTreeGUI(input, techTree);
	}
	
	public void disableAllGUIs() {
		workbenchGUI.setActive(false);
		basicCraftingGUI.setActive(false);
		techTreeGUI.setActive(false);
	}
	
	public void toggleFog() {
		displayFog = !displayFog;
	}
	
	public boolean shouldDisplayUIs() {
		return displayUIs;
	}
	
	public void toggleDisplayUIs() {
		displayUIs = !displayUIs;
	}
	
	public boolean isPaused() {
		return (pauseMenuGUI != null && pauseMenuGUI.isActive());
	}
	
	public Dimension getDrawResolution() {
		return drawResolution;
	}
	
	public void render(Graphics g) {
		// Globalize the graphics object
		gStorage = g;
		
		if (fullscreen) {
			drawResolution = new Dimension(window.manager.getWidth(), window.manager.getHeight());
		} else {
			drawResolution = this.getSize();
		}
		
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, (int) drawResolution.getWidth(), (int) drawResolution.getHeight());
		
		if (level != null) {
			level.drawSky(g, drawResolution);
		}
		
		if (pauseMenuGUI != null && pauseMenuGUI.isActive()) {
			renderTiles(g, false);
			
			// What is this language? Imagine not casting every function input inline...
			pauseMenuGUI.draw((Graphics2D) g, (int) drawResolution.getWidth(), (int) drawResolution.getHeight());
			return;
		} else {
			renderTiles(g, true);
		}
		
		int tempXOffset = xOffset;
		int tempYOffset = yOffset;
		
		if (player != null) {
			xOffset = (int) (player.x - (drawResolution.getWidth()/2));
			yOffset = (int) (player.y - (drawResolution.getHeight()/2));	
			player.drawPlayerModel(((Graphics2D) g), xOffset, yOffset, this);
		}
		
		for (int y = (tempYOffset >> 5) - 1; y < ((tempYOffset + ((int) drawResolution.getHeight())) >> 5) + 1; y++) {
			for (int x = (tempXOffset >> 5) - 1; x < ((tempXOffset + ((int) drawResolution.getWidth())) >> 5) + 1; x++) {
				int id0 = level.getTile(x, y).getId();
    	    	// Render lighting
    	    	if (id0 >= 0 && level.getDiscreteLightLevel(x, y) >= -127 && level.getDiscreteLightLevel(x, y) <= 127) {
    	    		g.setColor(new Color(0, 0, 0, 127 - level.getDiscreteLightLevel(x, y)));
        	    	g.fillRect((x << 5) - tempXOffset, (y << 5) - tempYOffset, 32, 32);
    	    	}
			}
		}
		
		if (player != null) player.draw(g, this);
		
		if (drawHUD) drawHUD(g);
		
		if (xOffset <= 0) {
			xOffset = 0;
		}
		if (yOffset <= 0) {
			yOffset = 0;
		}
		
		if (level != null) {
			if (xOffset >= ((level.width << 5) - (drawResolution.getWidth()))) {
				xOffset = (int) ((level.width << 5) - (drawResolution.getWidth()));
			}
			if (yOffset >= ((level.height << 5) - (drawResolution.getHeight()))) {
				yOffset = (int) ((level.height << 5) - (drawResolution.getHeight()));
			}
		}
		
		if (drawMiniMap) {
			renderMiniMap(g);
		}
		
		if (basicCraftingGUI.isActive()){
			basicCraftingGUI.draw(g, drawResolution.width, drawResolution.height, this, input);
		}
		
		if (techTreeGUI.isActive()){
			techTreeGUI.draw(g, drawResolution.width, drawResolution.height, this, input);
		}
		
		g.setFont(MediaLibrary.getFontFromLibrary("INFOFont"));
		//g.drawString("FPS: " + FPS, (int) drawResolution.getWidth() - 100, (int) drawResolution.getHeight() - 32);
		g.drawString("UPS: " + FPS, (int) drawResolution.getWidth() - 100, (int) drawResolution.getHeight() - 32);
	}
	
	public void renderTiles(Graphics g, boolean renderLevel) {
		try {
			if (level != null) {				
				int xShift = 0, yShift = 0;
				if (level.width << 5 < (int) drawResolution.getWidth()) xShift = ((int) drawResolution.getWidth() - (level.width << 5)) / 2; 
				if (level.height << 5 < (int) drawResolution.getHeight()) yShift = ((int) drawResolution.getHeight() - (level.height << 5)) / 2;
				xOffset += xShift;
				yOffset += yShift;
				
				level.calculateDiscreteLighting();
				
				// Draw tile Images //
				for (int y = (yOffset >> 5) - 1; y < ((yOffset + ((int) drawResolution.getHeight())) >> 5) + 1; y++) {
					for (int x = (xOffset >> 5) - 1; x < ((xOffset + ((int) drawResolution.getWidth())) >> 5) + 1; x++) {
			            	boolean ds = false;
		            	    int id0 = level.getTile(x, y).getId();
		            	    Image im0 = null;
		            	    for (Tile t : Tile.tiles) {
		            	    	if (t != null) {
		            	    		if (id0 == t.getId()) {
		            	    			im0 = MediaLibrary.getImageFromLibrary(t.getId());
		            	    			if (t.getClass() == DestructibleTile.class) {
		            	    				double d = level.getDurability(x, y);
		            	    				if (d != ((DestructibleTile) t).durability) {
		            	    					ds = true;
		            	    				}
		            	    			}
		            	    			if (t.getClass() == BackgroundDestructibleTile.class) {
		            	    				double d = level.getDurability(x, y);
		            	    				if (d != ((BackgroundDestructibleTile) t).durability) {
		            	    					ds = true;
		            	    				}
		            	    			}
		            	    		}
		            	    	}
		            	    }
		            	    
		            	    if (displayFog && x > 0 && y > 0 && y > level.getHorizon() && x < level.width && y < level.width && (!level.isExplored(x, y) || im0 == null)) {
		            	    	im0 = MediaLibrary.getImageFromLibrary(1);
		            	    }
		            	    g.drawImage(im0, (x << 5) - xOffset, (y << 5) - yOffset, this);
		            	    if (ds) g.drawImage(MediaLibrary.getImageFromLibrary(8192), (x << 5) - xOffset, (y << 5) - yOffset, this);
		            	    
		            	    if (id0 == Tile.DRY_WALL.getId()) {
		            	    	Color color = level.getTileColor(x, y);
		            	    	if (!(color == null)) {
			            	    	g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 128));
			            	    	g.fillRect((x << 5) - xOffset, (y << 5) - yOffset, 32, 32);
		            	    	}
		            	    }
		            	    
		            	    // Show conveyor speeds
		            	    if (id0 >= Tile.CONVEYOR.getId() && id0 <= Tile.CONVEYOR_MIDDLE.getId() && !(level.getConveyorSpeed(x, y) == 0)) {
		            	    	String conveyorSpeed = "";
		            	    	g.setColor(Color.WHITE);
		            	    	g.setFont(MediaLibrary.getFontFromLibrary("NumberingFont"));
		            	    	
		            	    	if (level.getConveyorSpeed(x, y) > 0) {
		            	    		conveyorSpeed = ">";
		            	    	} else if (level.getConveyorSpeed(x, y) < 0) {
		            	    		conveyorSpeed = "<";
		            	    	}

		            	    	g.drawString(conveyorSpeed, (x << 5) - xOffset + 12, (y << 5) - yOffset + 13);
		            	    }
	                }
	            }
				
				if (renderLevel) level.draw(g, this);
			}
		} catch (NullPointerException e) {
			FileUtilities.log("\t[Null tile]", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void renderMiniMap(Graphics g) {
		boolean hasMap = false;
		// Check if the player has a map
		InventoryItem[] currentItems = player.inventory.getItems();
		for (InventoryItem item : currentItems) {
			if (item != null && item.getClass() == Ingredient.class && ((Ingredient) item).getItemID() == 5) hasMap = true;
		}
		
		if (!hasMap) miniMapScale = 4;
		
		Image miniMap;
		int shiftX = 256;
		int shiftY = 256;
		if (miniMapScale > 2) {
			shiftX = 64;
			shiftY = 64;
		}
		
		if (miniMapScale == 1) {
			miniMap = (Image) level.drawMiniMap(miniMapScale, miniMapScale);
			g.drawImage(miniMap, (int) drawResolution.getWidth() - miniMap.getWidth(this) - shiftX, shiftY, this);
			g.setColor(Color.RED);
			g.drawOval((int) drawResolution.getWidth() - miniMap.getWidth(this) - shiftX - 2 + (player.x >> 5), shiftY - 2 + (player.y >> 5), 4, 4);
		}
		else {
			int playerX;
			int playerY;
			boolean isCenteredX = true;
			boolean isCenteredY = true;
			int drawTrackerX = 128;
			int drawTrackerY = 128;
			int modifier;
			
			if (miniMapScale == 4) modifier = 2;
			else modifier = 1;
			
			// Determine whether the minimap will be within the bounds
			if (player.x >> 5 + level.width / (miniMapScale * miniMapScale * 2) * modifier < level.width && player.x >> 5 > level.width / (miniMapScale * miniMapScale * 2) * modifier) {
				playerX = player.x >> 5;
			} else if (player.x >> 5 + level.width / (miniMapScale * miniMapScale * 2) * modifier >= level.width) {
				playerX = level.width - level.width / (miniMapScale * miniMapScale * 2) * modifier - 1;
			}
			else {
				playerX = level.width / (miniMapScale * miniMapScale * 2) * modifier + 1;
				isCenteredX = false;
			}
			
			if (player.y >> 5 + level.height / (miniMapScale * miniMapScale * 2) * modifier < level.height && player.y >> 5 > level.height / (miniMapScale * miniMapScale * 2) * modifier) {
				playerY = player.y >> 5;
			} else if (player.y >> 5 + level.height / (miniMapScale * miniMapScale * 2) * modifier >= level.height) {
				playerY = level.height - level.height / (miniMapScale * miniMapScale * 2) * modifier - 1;
			}
			else {
				playerY = level.height / (miniMapScale * miniMapScale * 2) * modifier + 1;
				isCenteredY = false;
			}
			
			miniMap = null;
			try {
				miniMap = (Image) level.drawMiniMap(miniMapScale, miniMapScale).getSubimage(
						(playerX - level.width / (miniMapScale * miniMapScale * 2) * modifier)/miniMapScale, 
						(playerY - level.height / (miniMapScale * miniMapScale * 2) * modifier)/miniMapScale, 
						level.width / (miniMapScale * miniMapScale * miniMapScale) * modifier, 
						level.height / (miniMapScale * miniMapScale * miniMapScale) * modifier);
				drawMiniMap = true;
			} catch (Exception e) {
				FileUtilities.log(e.getMessage());
				drawMiniMap = false;
			}
			
			if (drawMiniMap) {
				if (miniMapScale == 2) {
					if (isCenteredX) drawTrackerX = (int) drawResolution.getWidth() - miniMap.getWidth(this) - drawResolution.width / 10 + miniMap.getWidth(this);
					else {
						drawTrackerX = (int) drawResolution.getWidth() - miniMap.getWidth(this) - drawResolution.width / 10 + (player.x >> 5);
					}
					if (isCenteredY) drawTrackerY = drawResolution.height / 80 + miniMap.getHeight(this);
					else {
						drawTrackerY = drawResolution.height / 80 + (player.y >> 5);
					}
					g.drawImage(miniMap, (int) drawResolution.getWidth() - miniMap.getWidth(this) - drawResolution.width / 10, drawResolution.height / 80, 2 * miniMap.getWidth(this), 2 * miniMap.getHeight(this), this);
				}
				if (miniMapScale == 4) {
					if (isCenteredX) drawTrackerX = (int) drawResolution.getWidth() - miniMap.getWidth(this) * 4 - drawResolution.width / 10 + 4 * miniMap.getWidth(this);
					else {
						drawTrackerX = (int) drawResolution.getWidth() - miniMap.getWidth(this) * 4 - drawResolution.width / 10 + 4 * (player.x >> 6);
					}
					if (isCenteredY) drawTrackerY = drawResolution.height / 80 + 4 * miniMap.getHeight(this);
					else {
						drawTrackerY = drawResolution.height / 80 + 4 * (player.y >> 6);
					}
					g.drawImage(miniMap, (int) drawResolution.getWidth() - miniMap.getWidth(this) * 4 - drawResolution.width / 10, drawResolution.height / 80, 8 * miniMap.getWidth(this), 8 * miniMap.getHeight(this), this);
				}
				if (miniMapScale == 2 || miniMapScale == 4) {
					g.setColor(Color.RED);
					g.drawOval(drawTrackerX - 2, drawTrackerY - 2, 4, 4);
				}
			}
		}
	}
	 
	public void drawHUD(Graphics g) {
		FontMetrics metr = getFontMetrics(MediaLibrary.getFontFromLibrary("HUDFont"));
		
		g.setColor(Color.DARK_GRAY);
		g.setFont(MediaLibrary.getFontFromLibrary("HUDFont"));
		
		String str = "";
		
		if (displayTileInfo) {
			int offset = 64;
			int targetX = drawResolution.width - (2 * offset), targetY = drawResolution.height - (2 * offset);
			int slotsPerRow = 10;
			int spacing = 8;
			int iconSize = 64;
			if (targetX / 16 < targetY / 9) iconSize = targetX / 16;
			else iconSize = targetY / 9;
			int outline = 24;
			int i = (offset - outline) / 2 + iconSize + outline + 32;
			if (displayTile.getId() >= 13 && displayTile.getId() <= 18) {
				g.drawImage(MediaLibrary.getImageFromLibrary(13), ((int) drawResolution.getWidth() / 2) - 32, i, this);
				g.drawImage(MediaLibrary.getImageFromLibrary(14), ((int) drawResolution.getWidth() / 2), i, this);
				g.drawImage(MediaLibrary.getImageFromLibrary(15), ((int) drawResolution.getWidth() / 2) - 32, i + 32, this);
				g.drawImage(MediaLibrary.getImageFromLibrary(16), ((int) drawResolution.getWidth() / 2), i + 32, this);
			} else g.drawImage(MediaLibrary.getImageFromLibrary(displayTile.getId()), ((int) drawResolution.getWidth() / 2) - 32, i, 64, 64, this);
			if (displayTile.getClass() == DestructibleTile.class) {
				str = String.format("%.3f", tileDurability) + " / ";
				str += String.format("%.3f", ((DestructibleTile) displayTile).baseDurability);
			}
			if (displayTile.getClass() == BackgroundDestructibleTile.class) {
				str = String.format("%.3f", tileDurability) + " / ";
				str += String.format("%.3f", ((BackgroundDestructibleTile) displayTile).baseDurability);
			}
			g.drawString(str, ((int) drawResolution.getWidth() / 2) - (metr.stringWidth(str) / 2), i + 100);
		}
		
		g.setColor(Color.GREEN);

	}
	
	public Dimension convertCoordinates(int x, int y) {
		return new Dimension(x + xOffset, y + yOffset);
	}
	
	public void paint(Graphics g) {
		render(g);
	}
	
	public void run() {
		long last = System.nanoTime();
		double nspt = (1000000000D)/(MAXFPS);
		int frameCount = 0;
		int tickCount = 0;
		long lastT = System.currentTimeMillis();
		double delta = 0;
		
		while (running) {
			long current = System.nanoTime();
			
			delta += (current - last)/nspt;
			last = current;
			
			boolean render = false;
			
			while (delta >= 1) {
				tickCount++;
				tick();
				delta--;
				render = true;
			}
			
			if (render) {
				if (fullscreen && level != null) {
					Graphics2D g = window.manager.getGraphics();
					render(g);
					g.dispose();
					window.manager.updateDisplay();
				} else {
					repaint();
				}
				frameCount++;
			}
			
			if (System.currentTimeMillis() - lastT >= 1000) {
				lastT += 1000;
				FPS = frameCount;
				UPS = tickCount;
				frameCount = 0;
				tickCount = 0;
			}
		}
	}

	public void keyPressed(KeyEvent e) {
		input.keyPressed(e);
	}

	public void keyReleased(KeyEvent e) {
		input.keyReleased(e);
	}

	public void keyTyped(KeyEvent e) {
		input.keyTyped(e);
	}

	public void mouseWheelMoved(MouseWheelEvent e) {
		input.mouseWheelMoved(e);
	}

	public void mouseDragged(MouseEvent e) {
		input.mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e) {
		input.mouseMoved(e);
	}

	public void mouseClicked(MouseEvent e) {
		input.mouseClicked(e);
	}
	
	public void mouseEntered(MouseEvent e) {
		input.mouseEntered(e);
	}

	public void mouseExited(MouseEvent e) {
		input.mouseExited(e);
	}

	public void mousePressed(MouseEvent e) {
		input.mousePressed(e);
	}

	public void mouseReleased(MouseEvent e) {
		input.mouseReleased(e);
	}
}
