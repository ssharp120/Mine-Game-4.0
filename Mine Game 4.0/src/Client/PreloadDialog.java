package Client;

import static Utilities.FileUtilities.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

import Frame.GameLoop;
import Networking.UDPServer;

@SuppressWarnings("serial")
public class PreloadDialog extends JFrame {
	private PreloadDialogPanel panel;
	public PreloadDialog() {
		panel = new PreloadDialogPanel();
		add(panel);
		setResizable(false);
		setTitle("Settings");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setIconImage(loadImage("iconSelect.png"));
        setVisible(true);
	}
	
	class PreloadDialogPanel extends JPanel implements ActionListener, KeyListener {
		Color backgroundColor = Color.decode("#9A42FF");
		Color optionColor = Color.decode("#1298DC");
		Color highlightColor = Color.decode("#09CCEF");
		Color selectedColor = Color.decode("#02EEFE");
		Border defaultBorder = BorderFactory.createLineBorder(Color.black);
		Dimension[] availableResolutions = {
					new Dimension(3840, 2160),
					new Dimension(2560, 1440),
					new Dimension(2048, 1080),
					new Dimension(1920, 1080),
					new Dimension(1280, 1024),
					new Dimension(1280, 800),
					new Dimension(1200, 900),
					new Dimension(1366, 768),
					new Dimension(1024, 768),
					new Dimension(1280, 720),
					new Dimension(800, 600)
				};
		JComboBox resolutionSelect;
		JCheckBox fullscreenSelect;
		JButton launchButton;
		JButton serverButton;
		JButton exitButton;
		
		public PreloadDialogPanel() {			
			setBackground(backgroundColor);
			setFocusable(true);
	        requestFocusInWindow();
	        setPreferredSize(new Dimension(256, 384));
	        
	        addKeyListener(this);
	        
	        add(new JLabel(new ImageIcon(loadImage("options/logo.png"))));
	        
	        JPanel resolutionPanel = new JPanel();
	        resolutionSelect = new JComboBox(availableResolutions);
	        
	        ComboBoxRenderer renderer = new ComboBoxRenderer();
	        resolutionSelect.setRenderer(renderer);
	        resolutionSelect.setSelectedIndex(3);
	        resolutionSelect.addActionListener(this);
	        resolutionSelect.setBackground(highlightColor);
	        
	        resolutionPanel.add(new JLabel("<html><u>R</u>esolution: </html>"));
	        resolutionPanel.add(resolutionSelect);
	        resolutionPanel.setBackground(optionColor);
	        resolutionPanel.setBorder(defaultBorder);
	        add(resolutionPanel);
	        
	        JPanel fullscreenPanel = new JPanel();
	        fullscreenSelect = new JCheckBox();
	        fullscreenSelect.setMnemonic(KeyEvent.VK_F); 
	        fullscreenSelect.setSelected(false);
	        fullscreenSelect.addActionListener(this);
	        fullscreenSelect.setOpaque(false);
	        
	        fullscreenPanel.add(new JLabel("<html>Start <u>f</u>ullscreen: </html>"));
	        fullscreenPanel.add(fullscreenSelect);
	        fullscreenPanel.setBackground(optionColor);
	        fullscreenPanel.setBorder(defaultBorder);
	        add(fullscreenPanel);
	        
	        JPanel launchPanel = new JPanel();
	        launchButton = new JButton("Launch");
	        serverButton = new JButton("Server");
	        exitButton = new JButton("Exit");
	        launchButton.addActionListener(this);
	        serverButton.addActionListener(this);
	        exitButton.addActionListener(this);
	        launchButton.setBackground(highlightColor);
	        serverButton.setBackground(highlightColor);
	        exitButton.setBackground(highlightColor);
	        launchButton.setMnemonic(KeyEvent.VK_L);
	        serverButton.setMnemonic(KeyEvent.VK_S);
	        exitButton.setMnemonic(KeyEvent.VK_E);
	        
	        launchPanel.add(launchButton);
	        launchPanel.add(serverButton);
	        launchPanel.add(exitButton);
	        launchPanel.setBackground(optionColor);
	        launchPanel.setBorder(defaultBorder);
	        add(launchPanel);
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == exitButton) System.exit(0);
			if (e.getSource() == serverButton) {
				initThreadAndClose(new UDPServer());
			}
			if (e.getSource() == launchButton) {
				Dimension resolution = (Dimension) resolutionSelect.getSelectedItem();
				boolean startFullscreen = fullscreenSelect.isSelected();
				initThreadAndClose(new GameLoop(resolution, startFullscreen));
			}
		}
		
		public void initThreadAndClose(Runnable o) {
			setVisible(false);
			removeAll();
			dispose();
			EventQueue.invokeLater(new Runnable() {
	            public void run() {                
	            	new Thread(o).start();             
	            }
	        });
		}
		
		class ComboBoxRenderer extends JLabel implements ListCellRenderer {
			public ComboBoxRenderer() {
		        setOpaque(true);
		        setHorizontalAlignment(CENTER);
		        setVerticalAlignment(CENTER);
		    }
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				if (isSelected) {
		            setBackground(selectedColor);
		            setForeground(list.getSelectionForeground());
		        } else {
		            setBackground(list.getBackground());
		            setForeground(list.getForeground());
		        }
		        String resolution = "" + (int) ((Dimension) value).getWidth() + " x " + (int) ((Dimension) value).getHeight();
		        setText(resolution);
		        return this;
			}	
		}
		
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
				case KeyEvent.VK_E: exitButton.doClick(); break;
				case KeyEvent.VK_S: serverButton.doClick(); break;
				case KeyEvent.VK_L: launchButton.doClick(); break;
				case KeyEvent.VK_F: fullscreenSelect.doClick(); break;
				case KeyEvent.VK_R: resolutionSelect.setPopupVisible(!resolutionSelect.isPopupVisible()); break;
			}
		}
		public void keyReleased(KeyEvent e) {}
		public void keyTyped(KeyEvent e) {}
	}
}
