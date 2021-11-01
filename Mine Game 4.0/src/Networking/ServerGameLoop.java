package Networking;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.awt.Image;

import static Utilities.FileUtilities.loadImage;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.event.MenuKeyEvent;

import Entities.MultiPlayer;
import Tiles.Tile;

public class ServerGameLoop implements Runnable {
	private long ticks;
	private int UPS;
	private boolean running = true;
	// Remove final if implementing dynamic tick rate
	private final int MAXUPS = 100;
	
	private JFrame frame;
	private JPanel panel;
	private JLabel UPSlabel;
	private JProgressBar UPSbar;
	private JLabel mapLabel;
	private JButton updateButton;
	private JTextArea clientList;
	
	private Color backgroundColor = Color.GRAY;
	
	private GridBagLayout layout;
	
	private ServerLevel level;
	
	private List<String> connectedClients = new ArrayList<String>();
	
	public ServerGameLoop(BufferedImage image) {
		initGUI();
		initLevel(image);
	}
	
	public String toString() {
		String response = "";
		
		response += "[SERVER] Server game loop\n";
		response += "[SERVER] Ticks: " + ticks + "\n";
		if (!(level == null)) response += "[SERVER] Level:\n" + level.toString() + "\n";
		
		return response;
	}
	
	public void initLevel(BufferedImage image) {		
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
		
		level = new ServerLevel(image, spawnX << 5, spawnY << 5);
	}
	
	public void initGUI() {
		frame = new JFrame("UDP Server Monitor");
		panel = new JPanel();
		UPSlabel = new JLabel("UPS: ");
		UPSbar = new JProgressBar(0, MAXUPS);
		mapLabel = new JLabel("", JLabel.CENTER);
		updateButton = new JButton("Update tiles");
		clientList = new JTextArea("Clients:");
		
		updateButton.setBackground(Color.LIGHT_GRAY);
		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!(level == null)) level.queueUpdate();
			}});
		
		clientList.setBackground(Color.LIGHT_GRAY);
		clientList.setForeground(Color.DARK_GRAY);
		
		frame.setBackground(backgroundColor);
		frame.setFocusable(true);
		frame.setIconImage(loadImage("iconServer.png"));
		
		frame.requestFocusInWindow();
		
		frame.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					if (JOptionPane.showConfirmDialog(frame, 
				            "Are you sure to want to stop the server?", "Stop the server?", 
				            JOptionPane.YES_NO_OPTION,
				            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
							frame.dispose();
				            running = false;
				    }
				}
			}

			public void keyReleased(KeyEvent e) {}
		});
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	Runtime.getRuntime().gc();
		        if (JOptionPane.showConfirmDialog(frame, 
		            "Are you sure to want to stop the server?", "Stop?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
		            running = false;
		        }
		    }
		});
		
		panel.setBackground(backgroundColor);
		
		layout = new GridBagLayout();
		panel.setLayout(layout);
		
		GridBagConstraints c = generateConstraints(GridBagConstraints.VERTICAL, 0, 0);
		c.weightx = 0.5;
		c.weighty = 0;
		c.anchor = GridBagConstraints.PAGE_START;
		panel.add(UPSlabel, c);
		panel.add(UPSbar, c);
		
		c = generateConstraints(GridBagConstraints.HORIZONTAL, 0, 1);
		c.weightx = 1;
		c.weighty = 1;
		panel.add(mapLabel, c);
		
		c = generateConstraints(GridBagConstraints.NONE, 0, 2);
		c.weightx = 0;
		c.weighty = 0.25;
		panel.add(updateButton, c);
		
		c = generateConstraints(GridBagConstraints.NONE, 0, 3);
		c.weightx = 0.5;
		c.weighty = 0.25;
		c.ipadx = 200;
		c.ipady = 40;
		panel.add(clientList, c);
		
		panel.setPreferredSize(new Dimension(800, 600));
		frame.setMinimumSize(new Dimension(800, 600));
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	public GridBagConstraints generateConstraints(int gridX, int gridY) {
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridx = gridX;
		constraints.gridy = gridY;
		
		return constraints;
	}
	
	public GridBagConstraints generateConstraints(int fill, int gridX, int gridY) {
		GridBagConstraints constraints = generateConstraints(gridX, gridY);
		
		constraints.fill = fill;
		
		return constraints;
	}
	
	public GridBagConstraints generateConstraints(int fill, int gridX, int gridY, int gridWidth, int gridHeight) {
		GridBagConstraints constraints = generateConstraints(fill, gridX, gridY);
		
		constraints.gridwidth = gridWidth;
		constraints.gridheight = gridHeight;
		
		return constraints;
	}
	
	public byte[] getTileData() {
		if (level == null) return "[ERROR] No level".getBytes();
		else return level.getTileData();
	}
	
	public byte[] getTileData(String IPAddress) {
		if (level == null) return "[ERROR] No level".getBytes();
		else return level.getTileData(IPAddress);
	}
	
	public void tick() {
		if (ticks % 20 == 0) {
			// Test different UPS values
			// UPS = (int) Math.round(Math.random() * 100);
			// System.out.println(UPS);
			UPSlabel.setText("UPS: " + UPS);
			Color foregroundColor = new Color(255 - 255 * UPS / MAXUPS, 255 * UPS / MAXUPS, 0);
			UPSbar.setForeground(foregroundColor);
			UPSbar.setValue(UPS);
			
			if (mapLabel.getWidth() > 0 && mapLabel.getHeight() > 0) {
				BufferedImage icon = level.drawMiniMap(2, 2, level.getSpawnPoint().width >> 5, level.getSpawnPoint().height >> 5);
				int imageWidth, imageHeight;
				// Prevent the map from taking up the whole panel
				imageWidth = mapLabel.getWidth() <= panel.getWidth() / 2 ? mapLabel.getWidth() : panel.getWidth() / 2;
				imageHeight = imageWidth;
				//System.out.println(imageWidth + " " + mapLabel.getWidth());
				ImageIcon scaledIcon = new ImageIcon(icon.getScaledInstance(imageWidth, imageHeight, Image.SCALE_SMOOTH));
				mapLabel.setIcon(scaledIcon);
			} else {
				// Initialize the map image
				BufferedImage icon = level.drawMiniMap(2, 2, level.getSpawnPoint().width >> 5, level.getSpawnPoint().height >> 5);
				ImageIcon scaledIcon = new ImageIcon(icon);
				mapLabel.setIcon(scaledIcon);
			}
		}
		
		level.tick();
		
		updateGUIElements();
		
		ticks++;
	}
	
	private void updateGUIElements() {
		String clientText = "Clients:";
		if (clientsConnected()) {
			for (String client : this.getConnectedClients()) {
				clientText = clientText + "\n\t" + client.toString();
			}
		}
		clientList.setText(clientText);
	}
	
	private synchronized List<String> getConnectedClients() {
		return connectedClients;
	}
	
	protected synchronized void connectClient(String client) {
		if (client == null) return;
		connectedClients.add(client);
		if (level == null || level.playerConnected(client)) return;
		level.addMultiPlayer(new MultiPlayer(level, client, level.getSpawnPoint().width, level.getSpawnPoint().height, client));
	}
	
	private synchronized boolean clientsConnected() {
		return connectedClients.size() > 0;
	}
	
	public boolean movePlayer(String IPAddress, int x, int y) {
		return level.moveMultiPlayer(IPAddress, x, y);
	}

	public void run() {
		long last = System.nanoTime();
		double nspt = (1000000000D)/(MAXUPS);
		long startTime = System.currentTimeMillis();
		double delta = 0;
		
		while (running) {
			long current = System.nanoTime();
			
			delta += (double) (current - last)/nspt;
			last = current;
			
			boolean render = false;
						
			while (delta >= 1) {
				tick();
				delta--;
				render = true;
				
				if (ticks % 20 == 0) {
					UPS = System.currentTimeMillis() - startTime > 0 ? (int) Math.round((double) (1000 / (System.currentTimeMillis() - startTime))) : 1000;
					UPS = UPS <= MAXUPS ? UPS : MAXUPS;
				}
				startTime = System.currentTimeMillis();
			}
		}		
	}
}
