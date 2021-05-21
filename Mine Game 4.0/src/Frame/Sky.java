package Frame;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import Libraries.MediaLibrary;
import Utilities.Calendar;
import Utilities.FileUtilities;

public class Sky {
	public long time;
	public long finetime;
	public int day;
	public long currentTime;
	public int[] data = new int[4];
	public double[] ratios = new double[1];
	public Image[] images = new Image[4];
	public int totalDayTime;
	public int[] starX = new int[300];
	public int[] starY = new int[300];
	public Color[][] gradientColors = new Color[16][8];
	BufferedImage gradientSlice = new BufferedImage(1, 8, BufferedImage.TYPE_INT_ARGB);
	BufferedImage stars;
	
	public Sky(String folder, ImageObserver observer) {
		Scanner scanner = FileUtilities.getFileInternal("skies/" + folder + "/properties.txt");
		FileUtilities.log("Loading sky properties for sky " + folder + ":" + "\n");
		for (int i = 0; i <= data.length; i++) {
			if (scanner.hasNextInt()) {
				data[i] = scanner.nextInt();
			}
		}
		FileUtilities.log("\tNighttime length: " + data[0] + " seconds" + "\n");
		FileUtilities.log("\tDawn length: " + data[1] + " seconds" + "\n");
		FileUtilities.log("\tDaytime length: " + data[2] + " seconds" + "\n");
		FileUtilities.log("\tDusk length: " + data[3] + " seconds" + "\n");
		totalDayTime = data[0] + data[1] + data[2] + data[3];
		int j = 0;
		while (scanner.hasNextDouble()) {
			if (j < ratios.length) {
				ratios[j] = scanner.nextDouble();
			}
			j++;
		}
		FileUtilities.log("\tStar density: " + ratios[0] + "\n");
		images[0] = FileUtilities.loadImage("background/" + folder + "/gradient.png");
		images[1] = FileUtilities.loadImage("background/" + folder + "/star.png");
		images[2] = FileUtilities.loadImage("background/" + folder + "/smallstar.png");
		images[3] = FileUtilities.loadImage("background/" + folder + "/moon.png");
		try {
			generateStarPos(observer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void tick() {
		time = Calendar.timeElapsedMillis() % (totalDayTime * 1000);
		day =  (int) (Calendar.timeElapsed() / totalDayTime);
		BufferedImage buffered = new BufferedImage(1000, 8, BufferedImage.TYPE_INT_ARGB);
		buffered.getGraphics().drawImage(images[0], 0, 0, null);
		int x = (int) (time / totalDayTime);
		for (int i = 0; i < 16; i++) {
			gradientSlice = buffered.getSubimage((x + i)%1000, 0, 1, 8);
			for (int j = 0; j < 8; j++) {
				gradientColors[i][j] = Color.decode("" + gradientSlice.getRGB(0, j));
			}
		}
	}
	
	public void generateStarPos(ImageObserver observer) throws Exception {
		if (starX.length != starY.length) {
			throw new Exception("Star position arrays have mismatched dimensions");
		} else {
			for (int i = 0; i < starX.length; i++) {
				starX[i] = (int) Math.round(Math.random() * 5000);
				starY[i] = (int) Math.round(Math.random() * 5000);
			}
		}
		
		stars = new BufferedImage(5000, 5000, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < starX.length; i++) {
			Graphics gStar = stars.getGraphics();
			gStar.drawImage(images[2], starX[i], starY[i], 8, 8, observer);
		}
	}
	
	public void draw(Graphics g, Dimension screen, ImageObserver observer, boolean updateTime) {
		g.drawImage(MediaLibrary.getImageFromLibrary(6001), 20, 20, observer);
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 8; j++) {
				g.setColor(gradientColors[i][j]);
				g.fillRect(i * (int) screen.getWidth()/16, j * (int) screen.getHeight()/8, (int) screen.getWidth()/16, (int) screen.getHeight()/8);
			}
		}
		/*g.drawImage(images[0], 32, 64, 1000, 160, observer);
		g.setColor(Color.WHITE);
		g.drawLine((int) (time/totalDayTime) + 32, 64, (int) (time/totalDayTime) + 32, 64 + 160);
		g.drawLine(((int) (time/totalDayTime) + 48)%1000, 64, ((int) (time/totalDayTime) + 48)%1000, 64 + 160);*/
		if (updateTime) {
			time = Calendar.timeElapsed() % totalDayTime;
			finetime = Calendar.timeElapsedMillis() % (totalDayTime * 1000);
			currentTime = Calendar.timeElapsedMillis();
		}
		
		double xFactor = screen.getWidth()/2560;
		double yFactor = screen.getHeight()/1440;
		
		g.drawImage(images[1], (int) Math.round(xFactor * (3300 - ((double) ((double) ((double) ((currentTime % (totalDayTime * 1000)) - (data[0] - data[1]) * 1000))/((double) (data[2] * 1000)) * 2200)))), (int) Math.round(yFactor * (Math.pow(((double) ((double) ((double) ((currentTime % (totalDayTime * 1000)) - (data[0] - data[1]) * 1000))/((double) (data[2] * 1000)) * 500)), 1.05) - 300)), 256, 256,  observer);
		
		if (time <= data[0] / 2 || time >= data[0] + data[1] + data[2] + data[3]/2) {
			Graphics2D g1 = (Graphics2D) g;
			int xpos2 = -3000 + (int) Math.round(xFactor * (3300 - ((double) ((double) ((double) (((currentTime + (0.7 * data[0] * 1000)) % (totalDayTime * 1000))))/((double) (data[0] * 1000)) * 2200))));
			int ypos2 = -1800 + (int) Math.round(yFactor * (Math.pow(((double) ((double) ((double) (((currentTime + (0.7 * data[0] * 1000)) % (totalDayTime * 1000))))/((double) (data[0] * 1000)) * 600)), 1.05) - 300));
			float opacity = 1.0f;
			if (Math.random() > 0.88) opacity = Math.max(0.565f, (float) (1.0 - Math.pow(Math.random(), 8.95)));
			g1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g1.drawImage(stars, xpos2, ypos2, observer);
			g1.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
			g.drawImage(images[3], 3150 - (1024/6) + xpos2, 1800 - (1024/6) + ypos2, 1024/3, 1024/3, observer);
		}
		if (time > data[0] / 2 && time < data[0] * 3 / 4) {
			BufferedImage translucentStars = stars;
			Graphics2D g2 = (Graphics2D) g;
			float opacity = Math.min(1.0f, 1.2f - 1.2f * (float) (((float) (finetime - data[0] * 1000/2))/((float) (data[0] * 1000/4))));
			int xpos2 = -3000 + (int) Math.round(xFactor * (3300 - ((double) ((double) ((double) (((currentTime + (0.7 * data[0] * 1000)) % (totalDayTime * 1000))))/((double) (data[0] * 1000)) * 2200))));
			int ypos2 = -1800 + (int) Math.round(yFactor * (Math.pow(((double) ((double) ((double) (((currentTime + (0.7 * data[0] * 1000)) % (totalDayTime * 1000))))/((double) (data[0] * 1000)) * 600)), 1.05) - 300));
			g2.drawImage(images[3], 3150 - (1024/6) + xpos2, 1800 - (1024/6) + ypos2, 1024/3, 1024/3, observer);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g2.drawImage(stars, xpos2, ypos2, observer);
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
		if (time >= data[0] + data[1] + data[2] && time < data[0] + data[1] + data[2] + data[3]/2) {
			BufferedImage translucentStars = stars;
			Graphics2D g3 = (Graphics2D) g;
			float opacity = Math.max(0, Math.min(1.0f, 1.2f * (float) (((float) (finetime - (data[0] + data[1] + data[2]) * 1000))/((float) (data[3] * 1000/2)))));
			int xpos3 = -3000 + (int) Math.round(xFactor * (3300 - ((double) ((double) ((double) (((currentTime + (0.7 * data[0] * 1000)) % (totalDayTime * 1000))))/((double) (data[0] * 1000)) * 2200))));
			int ypos3 = -1800 + (int) Math.round(yFactor * (Math.pow(((double) ((double) ((double) (((currentTime + (0.7 * data[0] * 1000)) % (totalDayTime * 1000))))/((double) (data[0] * 1000)) * 600)), 1.05) - 300));
			g3.drawImage(images[3], 3150 - (1024/6) + xpos3, 1800 - (1024/6) + ypos3, 1024/3, 1024/3, observer);
			g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g3.drawImage(stars, xpos3, ypos3, observer);
			g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
		}
	}
}
