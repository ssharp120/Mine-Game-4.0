package Networking;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import Client.PreloadDialog;
import SingleplayerClient.GameLoop;
import Utilities.LevelFactory;

import static Utilities.FileUtilities.*;

public class UDPServer implements Runnable {

	private final boolean printInputChecks = false;
	
	// Global variables
	private DatagramSocket serverSocket;
	private int currentPort;
	
	private final int defaultPort = 7777;
	
	private boolean active = true;
	
	private byte[] lastReceivedData = new byte[1024];
	
	private String log = "";
	
	private BufferedImage queuedWorld;
	
	// GUI elements and settings
	private JFrame frame;
	private JPanel panel;
	
	private GridBagLayout layout;
	
	private final Color defaultBackgroundColor = new Color(8, 8, 8);
	private final Color defaultForegroundColor = new Color(64, 255, 16);
	
	private final Dimension defaultPreferredSize = new Dimension(800, 600);
	
	private JTextArea consoleOutput;
	private JScrollPane consoleOutputScrollPane;
	private JTextField consoleInput;
	
	private int logIndex;
	
	private Font consoleFont = new Font("Consolas", Font.PLAIN, 12);
	
	private Color commandColor = defaultForegroundColor;
	private Color warningColor = new Color(215, 201, 32);
	
	private ArrayList<Command> commandList = new ArrayList<Command>();
	
	private ServerGameLoop game;
	
	public UDPServer() {
		log("Starting server...", true);
		
		currentPort = defaultPort;
		
		try {
			initializeSocket(defaultPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		log("Loading user interface...", true);
		
		initializeUIElements(defaultBackgroundColor, defaultForegroundColor, defaultPreferredSize);
		
		initializeCommands();
		
		updateConsoleElements();
	}
	
	// May enable once advanced enough to justify running headless
	/*public static void main(String args[]) throws IOException {
		new UDPServer();
	}*/
	
	private class Command {
		private String[] keywords;
		private int shortestKeywordLength;
		private boolean takesArguments;
		private boolean warning;
		private int minArgs, maxArgs;
		private CommandAction action;
		
		// No argument cases
		public Command(String[] keywords, CommandAction action) {
			constructCommand(keywords, false, false, 0, 0, action);
		}
		
		public Command(String[] keywords, CommandAction action, boolean warning) {
			constructCommand(keywords, false, warning, 0, 0, action);
		}

		// Argument cases
		public Command(String[] keywords, int minArgs, int maxArgs, CommandAction action) {
			constructCommand(keywords, true, false, minArgs, maxArgs, action);
		}
		
		public Command(String[] keywords, int minArgs, int maxArgs, CommandAction action, boolean warning) {
			constructCommand(keywords, true, warning, minArgs, maxArgs, action);
		}
		
		// Full constructor
		private void constructCommand(String[] keywords, boolean takesArguments, boolean warning, int minArgs, int maxArgs, CommandAction action) {
			if (keywords == null) throw new IllegalArgumentException("Command must contain at least one keyword");
			if (minArgs < 0 || maxArgs < 0) throw new IllegalArgumentException("Number of arguments must be a positive integer or zero");
			if (minArgs > maxArgs) throw new IllegalArgumentException("Minimum number of arguments cannot exceed maximum number of arguments");
			if (action == null) throw new IllegalArgumentException("Command must define an action");
			
			this.keywords = keywords;
			this.takesArguments = takesArguments;
			this.warning = warning;
			this.minArgs = minArgs;
			this.maxArgs = maxArgs;
			this.action = action;
			
			determineShortestKeyword();
		}
		
		private void determineShortestKeyword() {
			if (multipleKeywords()) {
				for (String keyword : keywords) {
					if (shortestKeywordLength == 0 || keyword.length() < shortestKeywordLength) shortestKeywordLength = keyword.length();
				}
			}
			else shortestKeywordLength = mainKeyword().length();
		}
		
		public boolean multipleKeywords() {
			return keywords.length > 1;
		}
		
		public String mainKeyword() {
			return keywords[0];
		}
		
		public String[] getKeywords() {
			return keywords;
		}
		
		public boolean takesArguments() {
			return takesArguments;
		}
		
		public boolean requiresArguments() {
			return minArgs > 0;
		}
		
		public boolean strictArguments() {
			return minArgs == maxArgs;
		}
		
		public int minimumNumberOfArguments() {
			return minArgs;
		}
		
		public int maximumNumberOfArguments() {
			return maxArgs;
		}
		
		public boolean warns() {
			return warning;
		}
		
		public void setWarning(boolean warning) {
			this.warning = warning;
		}
		
		public void toggleWarning() {
			warning = !warning;
		}
		
		public String getResponse() {
			return action.getResponse();
		}
		
		public boolean parse(String input) {
			if (input == null) return false;
			
			//System.out.println(input.length() + " | " + shortestKeywordLength);
			if (input.length() < shortestKeywordLength) return false;
			
			if (takesArguments) {
				String[] inputWords = input.split(" ", (maxArgs + 2));
				
				// Check keyword and whether there are enough arguments
				if (inputWords.length < minArgs + 1 || !checkKeyword(inputWords[0]) 
						|| (inputWords.length == minArgs + 1 && inputWords[inputWords.length - 1].isBlank())) return false;
				
				// Set the arguments of the action to the rest of the string (sans keyword)
				String[] argWords = new String[inputWords.length - 1];
				for (int i = 0; i < argWords.length; i++) {
					argWords[i] = inputWords[i + 1];
				}
				action.setArgs(argWords);
				return true;
			} else {
				String inputWord = input.split(" ", 2)[0];
				return checkKeyword(inputWord);
			}
		}
		
		private boolean checkKeyword(String inputWord) {
			if (inputWord == null) return false;
			if (printInputChecks) System.out.println("Checking input " + inputWord + " against command " + mainKeyword());
			
			if (multipleKeywords()) {
				for (String keyword : keywords) {
					boolean match = keyword.toLowerCase().trim().equals(inputWord.toLowerCase().trim());
					if (printInputChecks) System.out.println(inputWord + " | " + keyword + " (" + match + ")");
					if (match) return true;
				} 
			} else {
				boolean match = mainKeyword().toLowerCase().trim().equals(inputWord.toLowerCase().trim());
				if (printInputChecks) System.out.println(inputWord + " | " + mainKeyword() + " (" + match + ")");
				if (match) return true;
			}
			
			return false;
		}
		
		public void action() {
			action.resetResponse();
			if (takesArguments && action.getArgumentQuantity() > maxArgs) {
				action.setResponse("Trimmed " + action.getExcessArguments() + " from command " + mainKeyword(), true); 
				log("[WARNING] Too many arguments at " + mainKeyword() + " - ignored " + action.getExcessArguments(), true);
			}
			action.run();
		}
		
		public String toString() {
			String state = "";
			
			// Describe keywords
			if (multipleKeywords()) {
				for (int i = 0; i < keywords.length; i++) {
					state += (i == 0 ? keywords[i] : "|" + keywords[i]);
				}
			} else state += mainKeyword();
			
			// Describe arguments in command by overriding this function
			if (takesArguments)	state += " ";
			
			return state;
		}
	}
	
	private abstract class CommandAction implements Runnable {
		String[] args;
		private String response;
		
		public void setArgs(String[] args) {
			this.args = args;
		}
		
		public boolean containsArgs() {
			return (!(args == null) && args.length > 0);
		}
		
		public int getArgumentQuantity() {
			return containsArgs() ? args.length : 0;
		}
		
		public String getExcessArguments() {
			return containsArgs() ? args[args.length - 1] : "";
		}
		
		public String getResponse() {
			return response;
		}
		
		public void setResponse(String str) {
			response = str;
		}
		
		public void setResponse(String str, boolean includeNewline) {
			if (includeNewline) setResponse(str + "\n");
			else setResponse(str);
		}
		
		public void appendResponse(String str) {
			response += str;
		}
		
		public void appendResponse(String str, boolean includeNewline) {
			if (includeNewline) appendResponse(str + "\n");
			else appendResponse(str);
		}
		
		public void resetResponse() {
			response = "";
		}
		
		public abstract void run();
	}
	
	private void initializeSocket(int port) throws SocketException {
		if (port <= 1024 || port > 65535) throw new IllegalArgumentException("Port " + port + " out of bounds or reserved");
		
		// Close the existing socket so we can use the port again later
		if (!(serverSocket == null)) serverSocket.close();
		
		serverSocket = new DatagramSocket(port);
		
		if (!(port == currentPort)) {
			currentPort = port;
			log("Changed port to " + port, true);
		}
		else log("Initialized socket on port " + port, true);
	}
	
	private void initializeCommands() {
		CommandAction shutdownAction = new CommandAction() {
			public void run() {
				if (JOptionPane.showConfirmDialog(frame, "Are you sure you would like to exit?") == JOptionPane.YES_OPTION) close();
			}};
		Command shutdownCommand = new Command(new String[] {"shutdown", "exit", "halt"}, shutdownAction, true);
		commandList.add(shutdownCommand);
		
		CommandAction clearAction = new CommandAction() {
			public void run() {
				clearLog();
				setResponse("Cleared server log", true);
			}};
		Command clearCommand = new Command(new String[] {"clear"}, clearAction);
		commandList.add(clearCommand);
		
		CommandAction resetAction = new CommandAction() {
			public void run() {
				resetLog();
				setResponse("Reset server log", true);
			}};
		Command resetCommand = new Command(new String[] {"reset"}, resetAction);
		commandList.add(resetCommand);
		
		CommandAction helpAction = new CommandAction() {
			public void run() {
				String msg = "[INFO] Available commands:\n";
				for (Command command : commandList) msg += "\t\t" + command.toString() + "\n";
				log(msg, true);
			}};
		Command helpCommand = new Command(new String[] {"help", "info"}, helpAction);
		commandList.add(helpCommand);
		
		CommandAction portAction = new CommandAction() {
			public void run() {
				if (containsArgs()) {
					int newPort;
					try {
						newPort = Integer.parseInt(args[0]);
					} catch (NumberFormatException e) {
						log("[ERROR] Port must be an integer", true);
						appendResponse("Ignored non-integer argument at command port", true);
						return;
					}
					if (newPort <= 1024 || newPort >= 65536) log("Failed to change port to " + newPort + ": Must specify a valid, non-reserved port", true);
					else {
						log("Closing port " + currentPort, true);
						appendResponse("Changed port from " + currentPort + " to " + newPort, true);
						currentPort = newPort;
						log("Changed port to " + currentPort, true);
					}
				} else {
					log("Current port: " + currentPort, true);
				}	
			}};
		Command portCommand = new Command(new String[] {"port"}, 0, 1, portAction) {
			public String toString() {
				return super.toString() + "[new port]";
			}};
		commandList.add(portCommand);
		
		CommandAction worldgenAction = new CommandAction() {
			public void run() {
				BufferedImage image;
				int worldType;
				int width;
				int height;
				
				if (getArgumentQuantity() >= 3) {
					try {
						worldType = Integer.valueOf(args[2]);
						width = Integer.valueOf(args[0]);
						height = Integer.valueOf(args[1]);
					} catch (NumberFormatException e) {
						log("[ERROR] World type, width and height must all be integers", true);
						appendResponse("Aborted command worldgen due to one or multiple non-integer arguments", true);
						return;
					}
				} else {
					try {
						worldType = Integer.valueOf(args[1]);
					} catch (NumberFormatException e) {
						log("[ERROR] World type must be an integer", true);
						appendResponse("Aborted command worldgen due to non-integer argument: invalid world type", true);
						return;
					}
					switch (args[0]) {
						case "small": width = 1024; height = 1024; break;
						case "medium": width = 2048; height = 1024; break;
						case "large": width = 4096; height = 1024; break;
						default:
							log("[ERROR] World size must be small, medium, or large", true);
							appendResponse("Aborted command worldgen due to invalid argument: invalid world size", true);
							return;
					}
				}
				
				image = LevelFactory.generateTiles(worldType, width, height);
				
				JFrame imageFrame = new JFrame("World output");
				ImageIcon icon = new ImageIcon(image);
				JLabel label = new JLabel(icon);
				imageFrame.add(label);
				imageFrame.pack();
				imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				imageFrame.addKeyListener(new KeyListener() {
					public void keyTyped(KeyEvent e) {}

					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							EventQueue.invokeLater(new Runnable() {
					            public void run() {
					            	queuedWorld = image;
					            	log("Selected world #" + System.currentTimeMillis(), true);
					            	//new Thread(new GameLoop(new Dimension(800, 600), false, image)).start();             
					            }
					        });
							imageFrame.dispose();
						}
					}

					public void keyReleased(KeyEvent e) {}
				});
				imageFrame.setVisible(true);
			}};
		Command worldgenCommand = new Command(new String[] {"worldgen"}, 2, 3, worldgenAction) {
			public String toString() {
				return super.toString() + "(-small | -medium | -large) | (width height) worldType";
			}};
		commandList.add(worldgenCommand);
		
		CommandAction playAction = new CommandAction() {
			public void run() {
				if (queuedWorld == null) log("Unable to start game - no world selected", true);
				else new Thread(new GameLoop(new Dimension(800, 600), false, queuedWorld)).start();
			}};
		Command playCommand = new Command(new String[] {"play"}, playAction);
		commandList.add(playCommand);
		
		CommandAction startAction = new CommandAction() {
			public void run() {
				if (queuedWorld == null) {
					log("No world selected", true);
					log("Generating new world with default settings...", true);
					queuedWorld = LevelFactory.generateTiles(0, 1024, 1024);
				} 
				log("Starting server from stored " + queuedWorld.getWidth() + " x " + queuedWorld.getHeight() + " world");
				game = new ServerGameLoop(queuedWorld);
				new Thread(game).start();
			}};
		Command startCommand = new Command(new String[] {"start"}, startAction);
		commandList.add(startCommand);
	}
	
	public void close() {
		// In the future, take care of anything pending before shutting down
		log("Shutting down...", true, true);
		System.exit(0);
	}
	
	public void clearLog() {
		logIndex = log.length();
	}
	
	public void resetLog() {
		logIndex = 0;
	}
	
	private void setActive(boolean active) {
		this.active = active;
	}
	
	public byte[] processData(String data, String IPAddress) { 
		// Connection request
		if (data.length() > 6 && data.contains("connect")) {
			log("Received connection request from client " + IPAddress);
			if (game == null) {
				log("[ERROR] Server not running");
				return "[SERVER] [ERROR] Server not running".getBytes();
			}
			else {
				log("Sending server info to client " + IPAddress);
				try {
					return InetAddress.getLocalHost().toString().getBytes();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		} else if (data.length() > 4 && data.contains("tiles")) {
			log("Received tile request from client" + IPAddress);
			
			if (game == null) {
				log("[ERROR] Server not running");
				return "[SERVER] [ERROR] Server not running".getBytes();
			}
			else {
				log("Sending tile info to client " + IPAddress);
				byte[] tileData = game.getTileData();
				return tileData;
			}
		}
		
		return data.toUpperCase().getBytes();
	}
	
	public String processCommand(String input) {
		boolean successful = false;
		String response = "No command issued; invalid input: " + input;
		
		for (Command command : commandList) {
			if (command.parse(input)) {
				command.action();
				return command.getResponse() == null ? "" : command.getResponse();
			}
		}
		
		return response;
	}
	
	public String getTimestamp() {
		return "[" + System.currentTimeMillis() + "]";
	}
	
	public void log(String str) {
		log = log + getTimestamp() + " " + str;
	}
	
	public void log(String str, boolean includeNewline) {
		if (includeNewline) log(str + "\n");
		else log(str);
	}
	
	public void log(String str, boolean includeNewline, boolean printToConsole) {
		if (includeNewline) log(str + "\n");
		else log(str);
		if (printToConsole) {
			System.out.print(str);
			if (includeNewline) System.out.print("\n");
		}
	}
	
	public void updateConsoleElements() {		
		consoleOutput.setText(log.substring(logIndex));
		
		consoleInput.setForeground(new Color(231, 231, 231));

		String input = consoleInput.getText();
		
		for (Command command : commandList) {
			if (command.parse(input)) {
				if (command.warns()) consoleInput.setForeground(warningColor);
				else consoleInput.setForeground(commandColor);
			}
		}
	}
	
	public void initializeUIElements(Color backgroundColor, Color foregroundColor, Dimension preferredSize) {
		// Parent elements
		frame = new JFrame("MineGame 4.0 Server Terminal");
		frame.setIconImage(loadImage("iconServer.png"));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		panel.setBackground(backgroundColor);
		panel.setPreferredSize(preferredSize);
		panel.setMinimumSize(new Dimension(256, 256));
		
		layout = new GridBagLayout();
		panel.setLayout(layout);
		
		// Sub-elements
		consoleOutput = new JTextArea();
		consoleOutput.setFont(consoleFont);
		consoleOutput.setEditable(false);
		consoleOutput.setText("Default Text");
		consoleOutput.setBackground(backgroundColor);
		consoleOutput.setForeground(foregroundColor);
		
		GridBagConstraints c = generateConstraints(GridBagConstraints.BOTH, 0, 0);
		c.weightx = 1;
		c.weighty = 0.75;
		c.anchor = GridBagConstraints.PAGE_START;
		consoleOutputScrollPane = new JScrollPane(consoleOutput, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(consoleOutputScrollPane, c);

		consoleInput = new JTextField();
		consoleInput.setFont(consoleFont);
		consoleInput.setEditable(true);
		consoleInput.setBackground(backgroundColor);
		consoleInput.setForeground(new Color(231, 231, 231));
		consoleInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.print(processCommand(consoleInput.getText()));
			}});
		
		consoleInput.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {updateConsoleElements();}

			public void keyPressed(KeyEvent e) {updateConsoleElements();}

			public void keyReleased(KeyEvent e) {updateConsoleElements();}
		});
		
		c = generateConstraints(GridBagConstraints.HORIZONTAL, 0, 1);
		c.weightx = 0;
		c.weighty = 0;
		c.anchor = GridBagConstraints.PAGE_END;
		panel.add(consoleInput, c);
		
		frame.setMinimumSize(new Dimension(800, 600));
		
		// Activate UI
		panel.setVisible(true);
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

	public void run() {
		while (active) {			
			// Initialize packet
			lastReceivedData = new byte[1024];
			DatagramPacket packet = new DatagramPacket(lastReceivedData, lastReceivedData.length);
			
			// Receive data
			try {
				serverSocket.receive(packet);
			} catch (IOException e) {
				log("[WARNING] Canceled receiving data", true);
				continue;
			}

			// Convert data into String
			String receivedData = new String(packet.getData());
			
			// Get client IP address and port
			InetAddress IPAddress = packet.getAddress();
			int port = packet.getPort();
			
			// Process data
			byte[] pendingData = processData(receivedData, IPAddress.toString().substring(1) + ":" + port);
			
			// Console output
			//log("Received \"" + receivedData.trim() + "\" from client " + IPAddress.toString().substring(1) + ":" + port, true);
			//log("Sending \"" + processedData.trim() + "\" to client " + IPAddress.toString().substring(1) + ":" + port, true);
			
			// Initialize packet to send to client
			DatagramPacket pendingPacket = new DatagramPacket(pendingData, pendingData.length, IPAddress, port);
			
			// Send the packet
			try {
				serverSocket.send(pendingPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			updateConsoleElements();
		}
		
		System.exit(0);
	}
	
	public String toString() {
		return "Mine Game 4.0 UDP Server on port " + currentPort;
	}
}
