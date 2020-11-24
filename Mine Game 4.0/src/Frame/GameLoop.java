package Frame;

import static Utilities.FileUtilities.*;

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

import Entities.OxygenGenerator;
import Entities.Player;
import Libraries.AttributeLibrary;
import Libraries.MediaLibrary;
import Libraries.RecipeLibrary;
import Libraries.StructureLibrary;
import Startup.PreloadDialog;
import Tiles.BackgroundDestructibleTile;
import Tiles.DestructibleTile;
import Tiles.Tile;
import UI.BasicCraftingGUI;
import UI.WorkbenchGUI;
import Utilities.AudioManager;
import Utilities.Calendar;
import Utilities.FileUtilities;
import Utilities.LevelFactory;

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
	
	public boolean drawHUD;

	public Graphics gStorage;
	
	public BasicCraftingGUI basicCraftingGUI;
	public WorkbenchGUI workbenchGUI;
	
	public static void main(String[] args) {
		new PreloadDialog();
	}

	public void tick() {
		ticks++;
		checkFullscreenFocus();
		level.tick();
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
		
		this.resolution = resolution;
		this.startFullscreen = startFullscreen;
		this.fullscreen = startFullscreen;
		
		setMinimumSize(resolution);
		setMaximumSize(resolution);
		setPreferredSize(resolution);
		setFocusable(true);
		
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
		
		initializeGUIs();
		initializeGameElements();
		
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
		audioManager = new AudioManager();
		
		Calendar.prepareCalendar(System.currentTimeMillis());
		level = new Level(LevelFactory.generateLevel(0, this), "Generated", 0, this, (40 + 16) * 32, 526*32);
		player = new Player(level, "Test", level.spawnX, level.spawnY, input);
		level.addEntity(player);
		
		level.addEntity(new OxygenGenerator(level, true, 60, 528));
	}
	
	public void initializeGUIs() {
		workbenchGUI = new WorkbenchGUI();
		basicCraftingGUI = new BasicCraftingGUI(this);
	}
	
	public void disableAllGUIs() {
		workbenchGUI.setActive(false);
		basicCraftingGUI.setActive(false);
	}
	
	public void render(Graphics g) {

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
		
		try {
			if (level != null) {				
				int xShift = 0, yShift = 0;
				if (level.width << 5 < (int) drawResolution.getWidth()) xShift = ((int) drawResolution.getWidth() - (level.width << 5)) / 2; 
				if (level.height << 5 < (int) drawResolution.getHeight()) yShift = ((int) drawResolution.getHeight() - (level.height << 5)) / 2;
				xOffset += xShift;
				yOffset += yShift;
				
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
		            	    if (im0 == null) {
		            	    	im0 = MediaLibrary.getImageFromLibrary(1);
		            	    }
		            	    g.drawImage(im0, (x << 5) - xOffset, (y << 5) - yOffset, this);
		            	    if (ds) g.drawImage(MediaLibrary.getImageFromLibrary(8192), (x << 5) - xOffset, (y << 5) - yOffset, this);
	                }
	            }
				
				level.draw(g);
			}
		} catch (NullPointerException e) {
			FileUtilities.log("Null tile");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (drawHUD) drawHUD(g);
		
		if (player != null) {
			xOffset = (int) (player.x - (drawResolution.getWidth()/2));
			yOffset = (int) (player.y - (drawResolution.getHeight()/2));	
		}
		
		
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
		
		try {
		player.drawPlayerModel(g, xOffset, yOffset, this);
		} catch (NullPointerException e) {
			FileUtilities.log(e.getMessage());
			new PreloadDialog();
			this.setEnabled(false);
		}
		
		if (basicCraftingGUI.isActive()){
			basicCraftingGUI.draw(g, drawResolution.width, drawResolution.height, this);
		}
		
		g.setFont(MediaLibrary.getFontFromLibrary("INFOFont"));
		g.drawString("FPS: " + FPS, (int) drawResolution.getWidth() - 100, (int) drawResolution.getHeight() - 32);
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
			if (displayTile.getClass() == DestructibleTile.class) str = tileDurability + " / " + ((DestructibleTile) displayTile).baseDurability;
			if (displayTile.getClass() == BackgroundDestructibleTile.class) str = tileDurability + " / " + ((BackgroundDestructibleTile) displayTile).baseDurability;
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
