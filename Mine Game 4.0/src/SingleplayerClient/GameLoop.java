package SingleplayerClient;

import static Utilities.FileUtilities.loadImage;

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
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Client.PreloadDialog;
import Entities.OxygenGenerator;
import Entities.Player;
import Libraries.AttributeLibrary;
import Libraries.MediaLibrary;
import Libraries.RecipeLibrary;
import Libraries.StructureLibrary;
import SingleplayerClient.InputHandler.ControlScheme;
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
import Utilities.TechTree;

public class GameLoop extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private boolean running = true;
	// Remove final if implementing dynamic tick rate
	private final int MAXUPS = 100;
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
	public boolean displayLighting = true;
	
	public boolean drawHUD;
	public boolean drawMiniMap = true;
	public int miniMapScale = 4;
	private BufferedImage miniMap;

	public Graphics gStorage;
	
	private TechTree techTree;
	
	public BasicCraftingGUI basicCraftingGUI;
	public WorkbenchGUI workbenchGUI;
	public PauseMenuGUI pauseMenuGUI;
	public TechTreeGUI techTreeGUI;
	
	public StatisticsTracker tracker;
	
	private GraphicsThread graphicsThread;
	
	private long[] graphicsTimes;
	private long levelUpdateTime;
	
	public static void main(String[] args) {
		new PreloadDialog();
	}

	public void tick() {
		long startTime = System.currentTimeMillis();
		
		ticks++;
		
		checkFullscreenFocus();
		level.tick();
		
		// Measure level time
		levelUpdateTime = System.currentTimeMillis() - startTime;
		
		if (pauseMenuGUI != null && pauseMenuGUI.isActive()) {input.setControlScheme(ControlScheme.PAUSE_MENU); pauseMenuGUI.tick(); return;}
		if (techTreeGUI != null && techTreeGUI.isActive()) {input.setControlScheme(ControlScheme.TECH_TREE); techTreeGUI.tick(input, ticks % 2 == 0, player.inventory); return;}
		
		if (input != null && input.esc.isPressed()) {pauseMenuGUI.setActive(true); input.esc.toggle(false); return;}
		
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
		
		displayConveyorSpeeds = input.getControlScheme() == ControlScheme.GAMEPLAY && input.alt.isPressed();
		
		//System.out.println("Total time " + (System.currentTimeMillis() - startTime));
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
			if (window != null && !window.getManager().getFSWindow().isFocused()) {
				setWindowed();
				return;
			}
			if (window != null && !window.getManager().getFSWindow().isShowing()) {
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
		
		initializeFrame(resolution, startFullscreen);
		checkDisplayMode();
		
		initializeGameElements();
		initializeLevel(LevelFactory.generateTiles(0, 4096, 1024));
		level.updateTiles();
		initializeGUIs();
	}
	
	public GameLoop(Dimension resolution, boolean startFullscreen, BufferedImage levelImage) {
		FileUtilities.log("Initializing Mine Game 4.0" + "\n");
		
		initializeFrame(resolution, startFullscreen);
		checkDisplayMode();
		
		initializeGameElements();
		initializeLevel(levelImage);
		level.updateTiles();
		initializeGUIs();
	}
	
	public void initializeFrame(Dimension resolution, boolean startFullscreen) {
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
			window.getManager().getFSWindow().setIconImage(loadImage("icon.png"));
			window.getManager().getFSWindow().setName("Mine Game 3.0");
			window.getManager().getFSWindow().setFocusable(true);
			window.getManager().getFSWindow().requestFocusInWindow();
			window.getManager().getFSWindow().addKeyListener(this);
			window.getManager().getFSWindow().addMouseListener(this);
			window.getManager().getFSWindow().addMouseMotionListener(this);
			window.getManager().getFSWindow().addMouseWheelListener(this);
		} else {
			if (window != null) {
				window.getManager().restoreScreen();
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
		graphicsTimes = new long[6];
		graphicsThread = new GraphicsThread();
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
	}
	
	public void initializeLevel(BufferedImage image) {
		if (image == null) throw new IllegalArgumentException("Input image must not be null");
		if (image.getWidth() < 16 || image.getHeight() < 16) throw new IllegalArgumentException("Input image must be at least 16 x 16");
		
		int spawnX = 16;
		int spawnY = 8;
		int startingOxyGenX = 0;
		int startingOxyGenY = 0;
		

		// Locate the spawnpoint at the crashed ship
		spawn:
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (image.getRGB(i, j) == Tile.SHIP_TILE.getLevelColour()) {
					spawnX = i + 20;
					spawnY = j + 10;
					startingOxyGenX = i + 24;
					startingOxyGenY = j + 19;
					break spawn;
				}
			}
		}
		
		if (spawnX == 16 && spawnY == 8) {
			// Find highest ground tile and set the spawnpoint just above that
			for (int j = 4; j < image.getHeight(); j++) {
				if (!(image.getRGB(spawnX, j) == Tile.SKY.getLevelColour())) {
					spawnY = j - 12;
					break;
				}
			}
		}
		
		level = new Level(image, "Supplied", 0, this, spawnX << 5, spawnY << 5);
		player = new Player(level, "Test", level.spawnX, level.spawnY, input);
		level.addEntity(player);
		
		level.populatePlants();
		if (startingOxyGenX > 0 && startingOxyGenY > 0) level.addEntity(new OxygenGenerator(level, true, startingOxyGenX, startingOxyGenY));
		else level.addEntity(new OxygenGenerator(level, true, spawnX, spawnY));
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
	
	public void toggleLighting() {
		displayLighting = !displayLighting;
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
	
	public class GraphicsThread extends Thread {
		Graphics g;
		
		public void updateGraphicsObject(Graphics g) {
			this.g = g;
		}
		
		public void run() {
			long startTime = System.currentTimeMillis();
			if (g == null) throw new IllegalArgumentException("No graphics object provided");
			else render(g);
			if (ticks % 20 == 0) FPS = System.currentTimeMillis() - startTime > 0 ? (1000 / (int) (System.currentTimeMillis() - startTime)) : 1000;
		}
	}
	
	public void renderTiles(Graphics g, boolean renderLevel) {
		try {
			if (level != null) {				
				int xShift = 0, yShift = 0;
				if (level.width << 5 < (int) drawResolution.getWidth()) xShift = ((int) drawResolution.getWidth() - (level.width << 5)) / 2; 
				if (level.height << 5 < (int) drawResolution.getHeight()) yShift = ((int) drawResolution.getHeight() - (level.height << 5)) / 2;
				xOffset += xShift;
				yOffset += yShift;
				
				//int tilesRendered = 0;
				
				Image defaultImage = MediaLibrary.getImageFromLibrary(1);
				
				// Draw tile Images within the bounds of the window //
				for (int y = (yOffset >> 5) - 1; y < ((yOffset + ((int) drawResolution.getHeight())) >> 5) + 1; y++) {
					for (int x = (xOffset >> 5) - 1; x < ((xOffset + ((int) drawResolution.getWidth())) >> 5) + 1; x++) {
			            	boolean ds = false;
		            	    int id0 = level.getTile(x, y).getId();
		            	    		
		            	    Image im0 = defaultImage;
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
		            	    			break;
		            	    		}
		            	    	}
		            	    }
		            	    
	            	    	if (level.drawCaveWall(x, y) && (id0 == Tile.SKY.getId() || id0 == Tile.BARRIER.getId() || id0 == Tile.VOID.getId())) {
	            	    		im0 = MediaLibrary.getImageFromLibrary(Tile.CAVE_WALL.getId());
	            	    	} else if (level.drawCaveWall(x, y) && (id0 == Tile.TORCH.getId() || id0 == Tile.WOOD_PLATFORM.getId())) {
	            	    		g.drawImage(MediaLibrary.getImageFromLibrary(Tile.CAVE_WALL.getId()), (x << 5) - xOffset, (y << 5) - yOffset, this);
	            	    	}
		            	    
		            	    boolean transparentFogFlag = displayFog && !level.isVisible(x, y) && (level.isVisibleNear(x, y) || level.visibleProximity(x, y, 2) || level.visibleProximity(x, y, 3));
		            	    
		            	    if (transparentFogFlag) {
		            	    	g.drawImage(im0, (x << 5) - xOffset, (y << 5) - yOffset, this);
			            	    if (ds) g.drawImage(MediaLibrary.getImageFromLibrary(8192), (x << 5) - xOffset, (y << 5) - yOffset, this);
			            	    
			            	    int transparency = 200;
			            	    if (level.visibleProximity(x, y, 2)) transparency = 128;
			            	    if (level.isVisibleNear(x, y)) transparency = 64;
			            	    
		            	    	g.setColor(new Color(0, 0, 0, transparency));
		            	    	g.fillRect((x << 5) - xOffset, (y << 5) - yOffset, 32, 32);
		            	    	continue;
		            	    } else if (displayFog && (!level.isVisible(x, y) || im0 == null)) {
		            	    	g.setColor(new Color(0, 0, 0));
		            	    	g.fillRect((x << 5) - xOffset, (y << 5) - yOffset, 32, 32);
		            	    	continue;
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
		            	//tilesRendered++;
	                }
				}
				
				//System.out.println("Tiles rendered: " + tilesRendered);
			}
		} catch (NullPointerException e) {
			FileUtilities.log("\t[Null tile]", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void render(Graphics g) {
		long startTime = System.currentTimeMillis();
		long currentTime = System.currentTimeMillis();
		
		// Globalize the graphics object
		gStorage = g;
		
		if (fullscreen) {
			drawResolution = new Dimension(window.getManager().getWidth(), window.getManager().getHeight());
		} else {
			drawResolution = this.getSize();
		}
		
		g.setColor(Color.GRAY);
		g.fillRect(0, 0, (int) drawResolution.getWidth(), (int) drawResolution.getHeight());
		
		if (level != null) {
			level.drawSky(g, drawResolution);
		}
		
		// Measure sky time
		graphicsTimes[0] = System.currentTimeMillis() - startTime;
		currentTime = System.currentTimeMillis();
		
		if (pauseMenuGUI != null && pauseMenuGUI.isActive()) {
			renderTiles(g, false);
			
			// What is this language? Imagine not casting every function input inline...
			pauseMenuGUI.draw((Graphics2D) g, (int) drawResolution.getWidth(), (int) drawResolution.getHeight());
			return;
		} else {
			renderTiles(g, true);
		}
		
		// Measure tile rendering time
		graphicsTimes[1] = System.currentTimeMillis() - currentTime;
		currentTime = System.currentTimeMillis();
		
		int tempXOffset = xOffset;
		int tempYOffset = yOffset;
		
		level.draw(g, this);
		
		// Measure level rendering time
		graphicsTimes[2] = System.currentTimeMillis() - currentTime;
		currentTime = System.currentTimeMillis();
		
		if (displayLighting) {
			if (!(level == null)) level.calculateDiscreteLighting();
		
			for (int y = (tempYOffset >> 5) - 1; y < ((tempYOffset + ((int) drawResolution.getHeight())) >> 5) + 1; y++) {
				for (int x = (tempXOffset >> 5) - 1; x < ((tempXOffset + ((int) drawResolution.getWidth())) >> 5) + 1; x++) {
	    	    	// Render lighting
	    	    	if ((level.isVisible(x, y) || level.isVisibleNear(x, y) || level.visibleProximity(x, y, 2) || level.visibleProximity(x, y, 3))
	    	    			&& (level.getTile(x, y).getId() > 2 || level.drawCaveWall(x, y))
	    	    			&& level.getDiscreteLightLevel(x, y) >= -127 && level.getDiscreteLightLevel(x, y) < 127) {
	    	    		g.setColor(new Color(0, 0, 0, 127 - level.getDiscreteLightLevel(x, y)));
	        	    	g.fillRect((x << 5) - tempXOffset, (y << 5) - tempYOffset, 32, 32);
	    	    	}
				}
			}
		}
		
		// Measure lighting time
		graphicsTimes[3] = System.currentTimeMillis() - currentTime;
		currentTime = System.currentTimeMillis();
		
		if (player != null) {
			xOffset = (int) (player.x - (drawResolution.getWidth()/2));
			yOffset = (int) (player.y - (drawResolution.getHeight()/2));
			
			if (xOffset < 0) {
				player.drawPlayerModel((Graphics2D) g, 0, yOffset, this);
			} else if (xOffset > (level.width << 5) - drawResolution.getWidth()) {
				player.drawPlayerModel((Graphics2D) g, (level.width << 5) - drawResolution.width, yOffset, this);
			} else player.drawPlayerModel(((Graphics2D) g), xOffset, yOffset, this);
		}
		
		// Measure player rendering time
		graphicsTimes[4] = System.currentTimeMillis() - currentTime;
		currentTime = System.currentTimeMillis();
		
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
		
		// Measure GUI and HUD rendering time
		graphicsTimes[5] = System.currentTimeMillis() - currentTime;
		currentTime = System.currentTimeMillis() - currentTime;
		
		if (!(player == null)) player.draw(g, this);
		
		g.setFont(MediaLibrary.getFontFromLibrary("INFOFont"));
	
		if (!(player == null) && player.drawInfo) {
			g.setColor(Color.GREEN);
			g.drawString("Level update time (ms): " + levelUpdateTime, (int) drawResolution.getWidth() - 232, (int) drawResolution.getHeight() - 32 - 16 * 6);
			g.drawString("Render times (ms):", (int) drawResolution.getWidth() - 232, (int) drawResolution.getHeight() - 32 - 16 * 5);
			for (int i = 0; i < 6; i++) {
				String timeLabel = new String();
				switch (i) {
				case 0: timeLabel = "Sky ------"; break;
				case 1: timeLabel = "Tiles ----"; break;
				case 2: timeLabel = "Level ----"; break;
				case 3: timeLabel = "Lights ---"; break;
				case 4: timeLabel = "Player ---"; break;
				case 5: timeLabel = "GUI/HUD --"; break;
				}
				
				if (graphicsTimes[i] < 1000 / 120) g.setColor(Color.GREEN);
				else if (graphicsTimes[i] < 1000 / 60) g.setColor(Color.YELLOW);
				else g.setColor(Color.RED);
				g.drawString(timeLabel + graphicsTimes[i], (int) drawResolution.getWidth() - 232, (int) drawResolution.getHeight() - 32 - 16 * (4 - i));
			}
			g.drawString("UPS: " + UPS, (int) drawResolution.getWidth() - 100, (int) drawResolution.getHeight() - 48);
		} else g.setColor(Color.DARK_GRAY);
		
		g.drawString("FPS: " + FPS, (int) drawResolution.getWidth() - 100, (int) drawResolution.getHeight() - 32);
	}
	
	public void renderMiniMap(Graphics g) {
		boolean hasMap = false;
		// Check if the player has a map
		InventoryItem[] currentItems = player.inventory.getItems();
		for (InventoryItem item : currentItems) {
			if (item != null && item.getClass() == Ingredient.class && ((Ingredient) item).getItemID() == 5) hasMap = true;
		}
		
		if (!hasMap) miniMapScale = 4;
					
		try {
			if (miniMapScale > 1 || ticks % 100 == 0) miniMap = level.drawMiniMap(miniMapScale, miniMapScale, player.x >> 5, player.y >> 5);
			drawMiniMap = true;
		} catch (Exception e) {
			FileUtilities.log(e.getMessage());
			drawMiniMap = false;
		}
		
		if (drawMiniMap && miniMapScale == 1) {
			g.drawImage(miniMap, drawResolution.width - 1024 - 16, 16, 1024, 1024, this);
		} else if (drawMiniMap) {
			g.drawImage(miniMap, drawResolution.width - 256 - 16, 16, 256, 256, this);
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
			g.drawImage(MediaLibrary.getImageFromLibrary(displayTile.getId()), ((int) drawResolution.getWidth() / 2) - 32, i, 64, 64, this);
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
		graphicsThread.updateGraphicsObject(g);
		graphicsThread.run();
		//System.out.println(graphicsThread.getId());
	}
	
	public void run() {
		long last = System.nanoTime();
		double nspt = (1000000000D)/(MAXUPS);
		long startTime = System.currentTimeMillis();
		double delta = 0;
		
		while (running) {
			long current = System.nanoTime();
			
			delta += (current - last)/nspt;
			last = current;
			
			boolean render = false;
						
			while (delta >= 1) {
				tick();
				delta--;
				render = true;
				
				if (ticks % 20 == 0) UPS = System.currentTimeMillis() - startTime > 0 ? (1000 / (int) (System.currentTimeMillis() - startTime)) : 1000;
				startTime = System.currentTimeMillis();
			}
			
			if (render) {
				if (fullscreen && level != null) {
					graphicsThread.updateGraphicsObject((Graphics) window.getManager().getGraphics());
					graphicsThread.run();
					window.getManager().updateDisplay();
				} else {
					repaint();
				}
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
