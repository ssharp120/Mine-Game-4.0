package Libraries;

import static Utilities.FileUtilities.*;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Scanner;

import Utilities.FileUtilities;

public class MediaLibrary {
	private static Image[] imageLibrary = new Image[16384];
	private static String[] audioClipLibrary = new String[16384];
	private static HashMap<String, Font> fontLibrary = new HashMap<>();
	
	public static String getSoundFromLibrary(int clipIndex) {
		return audioClipLibrary[clipIndex];
	}
	
	public static void populateSoundLibrary() {
		Scanner audioFile = FileUtilities.getFileInternal("audio/index.txt");
		FileUtilities.log("Loading audio..." + "\n");
		try {
			int i = 0;
			while (audioFile.hasNextLine()) {
				audioClipLibrary[i] = audioFile.nextLine() + ".wav";
				FileUtilities.log("\tAudio clip " + i + " loaded - " + audioClipLibrary[i] + "\n");
				i++;
			}
		} catch (Exception e) {
			FileUtilities.log("Error while loading audio" + "\n");
			e.printStackTrace();
			System.exit(4);
		}
	}
	
	public static Image getImageFromLibrary(int imageIndex) {
		return imageLibrary[imageIndex];
	}
	
	public static BufferedImage getBufferedImageFromLibrary(int imageIndex) {
		Image i = getImageFromLibrary(imageIndex);
		BufferedImage b = new BufferedImage(i.getWidth(null), i.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g = b.createGraphics();
		g.drawImage(i, 0, 0, null);
		g.dispose();
			
		return b;
	}
	
	public static void populateImageLibrary() {
		Scanner imageFile = FileUtilities.getFileInternal("images.txt");
		FileUtilities.log("Loading images..." + "\n");
		try {
			int i = 0;
			while (imageFile.hasNextLine()) {
				int index = imageFile.nextInt();
				String filePath = (imageFile.nextLine() + ".png").substring(1);
				imageLibrary[index] = loadImage(filePath);
				FileUtilities.log("\tImage " + index + " loaded - " + filePath + "\n");
				i++;
			}
		} catch (Exception e) {
			FileUtilities.log("Error while loading images" + "\n");
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	public static void populateFontLibrary() {
		fontLibrary.put("HUDFont", new Font("Courier", Font.BOLD, 36));
		fontLibrary.put("INFOFont", new Font("Courier", Font.PLAIN, 12));
		fontLibrary.put("Heading", new Font("Courier", Font.PLAIN, 16));
		fontLibrary.put("Subtext", new Font("Courier", Font.PLAIN, 10));
		fontLibrary.put("Health", new Font("Bauhaus 93", Font.PLAIN, 32));
		fontLibrary.put("Numbering", new Font("Bauhaus 93", Font.BOLD, 64));
		fontLibrary.put("Indicator", new Font("Courier", Font.BOLD, 32));
		fontLibrary.put("ButtonFont", new Font("Copperplate Gothic", Font.BOLD, 32));
		fontLibrary.put("OSFriendlyFont", new Font("Courier", Font.BOLD, 32));
		fontLibrary.put("Monospace Numbering", new Font("Courier", Font.PLAIN, 32));
		fontLibrary.put("Offset Numbering", new Font("Georgia", Font.PLAIN, 32));
		fontLibrary.put("Monospace Numbering XL", new Font("Courier", Font.PLAIN, 128));
		fontLibrary.put("Offset Numbering XL", new Font("Georgia", Font.PLAIN, 128));
	}
	
	public static Font getFontFromLibrary(String fontIndex) {
		if (!System.getProperty("os.name").startsWith("Windows")) return fontLibrary.get("OSFriendlyFont");
		return fontLibrary.get(fontIndex);
	}
}
	
