package Client;

import static Utilities.FileUtilities.loadImage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import Entities.Player;
import Libraries.MediaLibrary;
import SingleplayerClient.FullscreenWindow;
import SingleplayerClient.InputHandler;
import SingleplayerClient.Level;
import Tiles.Tile;
import UI.BasicCraftingGUI;
import UI.PauseMenuGUI;
import UI.TechTreeGUI;
import UI.WorkbenchGUI;
import Utilities.AudioManager;
import Utilities.FileUtilities;
import Utilities.LevelFactory;
import Utilities.StatisticsTracker;
import Utilities.TechTree;

public class ClientGameLoop extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private boolean running = true;
	// Remove final if implementing dynamic tickrate
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

	public Graphics gStorage;
	
	private TechTree techTree;
	
	public BasicCraftingGUI basicCraftingGUI;
	public WorkbenchGUI workbenchGUI;
	public PauseMenuGUI pauseMenuGUI;
	public TechTreeGUI techTreeGUI;
	
	public StatisticsTracker tracker;
	
	private GraphicsThread graphicsThread;
	
	private final int defaultPort = 7777;
	private int currentPort;
	private String serverIP = "192.168.0.28";
	
	private DatagramSocket socket;
	
	private long[] graphicsTimes;
	private long levelUpdateTime;
	
	boolean connectedToServer;
	
	private int[][] tiles;
	private int tileWidth;
	private int tileHeight;
	
	public ClientGameLoop(Dimension resolution, boolean startFullscreen) {
		FileUtilities.log("Initializing Mine Game 4.0 client..." + "\n");
		
		initializeFrame(resolution, startFullscreen);
		checkDisplayMode();
		
		graphicsThread = new GraphicsThread();
		
		FileUtilities.log("Starting socket on port " + defaultPort + "\n");
		initializeSocket(defaultPort);
		
		initializeTiles(64, 64);
		initializeLibraries();
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
		frame = new JFrame("Mine Game 4.0 Client");
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
	
	public void initializeTiles(int tileWidth, int tileHeight) {
		if (tileWidth > 0 && tileHeight > 0) {
			tiles = new int[tileWidth][tileHeight];
			this.tileWidth = tileWidth;
			this.tileHeight = tileHeight;
		}
	}
	
	public void initializeLibraries() {
		MediaLibrary.populateImageLibrary();
	}
	
	public void resetWindow() {
		frame.remove(this);
		frame.setVisible(false);
		frame.setEnabled(false);
		frame = null;
		frame = new JFrame("Mine Game 4.0 Client");
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
	
	public void exitSequence() {
		//LevelFactory.saveLevel("save_" + FileUtilities.TIMESTAMP_AT_RUNTIME + ".png", 0, 0, level);
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
	
	public void tick() {
		if (!connectedToServer) {
			try {
				// Create connection packet
				DatagramPacket connectionPacket = createPacket("connect", serverIP, currentPort);
				
				// Send packet
				socket.send(connectionPacket);
				
				// Wait for response
				String response = receivePacket();
				if (response.contains("[ERROR]")) exitSequence();
				else {
					connectedToServer = true;
					FileUtilities.log("Connected to server " + response);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (ticks % 100 == 1) {
			try {
				// Create tile request packet
				DatagramPacket tileRequestPacket = createPacket("tiles", serverIP, currentPort);
				
				// Send packet
				socket.send(tileRequestPacket);
				
				// Wait for response
				byte[] response = receivePacketData();
				
				if (new String(response).contains("[ERROR]")) System.out.println(new String(response));
				else {
					if (tileWidth > 0 && tileHeight > 0 && response.length >= tileWidth * tileHeight) {
						for (int i = 0; i < tileWidth; i++) {
							for (int j = 0; j < tileHeight; j++) {
								//System.out.println(tiles[i][j]);
								tiles[i][j] = response[(j * tileWidth) + i];
							}
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		ticks++;
	}
	
	public void initializeSocket(int port) {
		try {
			socket = new DatagramSocket(port);
			currentPort = port;
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public DatagramPacket createPacket(String str, String IPAddress, int port) throws UnknownHostException {
		// Convert target data into an array of bytes
		System.out.println("[CLIENT] Sending data:   " + str);
		byte[] pendingData = str.getBytes();
		
		// Get server address
		InetAddress address;
		if (IPAddress.length() < 8) address = InetAddress.getLocalHost();
		else address = InetAddress.getByName(IPAddress);
		
		// Initialize packet
		return new DatagramPacket(pendingData, pendingData.length, address, port);
	}
	
	public String receivePacket() throws IOException {
		// Retrieve response from server
		byte[] responseRawData = new byte[1024];
		
		// Initialize packet
		DatagramPacket packet = new DatagramPacket(responseRawData, responseRawData.length);
		
		// Receive packet
		socket.receive(packet);
		return new String(packet.getData());
	}
	
	public byte[] receivePacketData() throws IOException {
		// Retrieve response from server
		byte[] responseRawData = new byte[4096];
		
		// Initialize packet
		DatagramPacket packet = new DatagramPacket(responseRawData, responseRawData.length);
		
		// Receive packet
		socket.receive(packet);
		return packet.getData();
	}
	
	public void paint(Graphics g) {
		graphicsThread.updateGraphicsObject(g);
		graphicsThread.run();
		//System.out.println(graphicsThread.getId());
	}
	
	public void render(Graphics g) {
		if (!(tiles == null)) {
			for (int i = 0; i < tileWidth; i++) {
				for (int j = 0; j < tileHeight; j++) {
					if (tiles[i][j] > 2) g.drawImage(MediaLibrary.getImageFromLibrary(tiles[i][j]), i << 5, j << 5, this);
				}
			}
		}
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

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

}
