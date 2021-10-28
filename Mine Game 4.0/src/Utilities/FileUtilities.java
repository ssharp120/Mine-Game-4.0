package Utilities;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import Libraries.MediaLibrary;

public class FileUtilities {
	private static final boolean DEBUG = true;
	public static final long TIMESTAMP_AT_RUNTIME = System.currentTimeMillis();
	
	 static FileSystemView fsv = FileSystemView.getFileSystemView();
	 static FileSystem fs = FileSystems.getDefault();
	 static FileWriter logger;
	 static FileWriter levelLogger;
	
	public static boolean isRunningAsJAR() {
		return !DEBUG;
	}
	
	private static Image loadImageFromJAR(String path) {
		Image tmp = null;
		try {
			tmp = ImageIO.read(Frame.GameLoop.class.getResourceAsStream("/org/res/" + path));
		} catch (IOException e){
			e.printStackTrace();
		}
		return tmp;
	}
	 
	private static Image loadImageFromProject(String path) {
		ImageIcon icon = new ImageIcon("res/" + path);
	    return icon.getImage();
	}
	 
	public static Image loadImage(String path) {
		 if (!DEBUG) {
			return loadImageFromJAR(path);
		 } else {
			return loadImageFromProject(path);
		 }
	 }
	
	public static BufferedImage loadBufferedImage(String path) {
		Image img = loadImage(path);
		BufferedImage bimg = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D bg = bimg.createGraphics();
		bg.drawImage(img, 0, 0, null);
		bg.dispose();
		
		return bimg;
	}
	
	public static BufferedImage rotateImage(BufferedImage image, double angle) {
		final double sin = Math.abs(Math.sin(angle));
		final double cos = Math.abs(Math.cos(angle));
		
		final int width =  Math.abs((int) Math.floor(cos * image.getWidth() + sin * image.getHeight()));
		final int height = Math.abs((int) Math.floor(sin * image.getWidth() + cos * image.getHeight()));
		
		if (width == 0 || height == 0) return null;
		
		final BufferedImage rotatedImage = new BufferedImage(width, height, image.getType());
		
		final AffineTransform transform = new AffineTransform();
		
		transform.translate(width / 2, height / 2);
		
		transform.rotate(angle, 0, 0);
		
		transform.translate(-image.getWidth() / 2, -image.getHeight() / 2);
		
		final AffineTransformOp rotateOp = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
		
		rotateOp.filter(image, rotatedImage);
		
		return rotatedImage;
	}
	
	 public static Scanner getFileInternal(String path) {
		 if (!DEBUG) {
			 return getFileFromJAR(path);
		 } else {
			 return getFileNonJAR(path);
		 }
	 }
	 
	 public static Scanner getFileFromJAR(String path) {
		 try {
			 BufferedReader txtReader = new BufferedReader(new InputStreamReader(Frame.GameLoop.class.getResourceAsStream("/org/data/" + path)));
			 Scanner scanner = new Scanner(txtReader);
			 return scanner;
		 } catch (Exception e) { e.printStackTrace(); }
		 return null;
	 }
	 
	 public static Scanner getFileNonJAR(String path) {
		 try {
			 File file = new File("data/" + path);
			 Scanner scanner = new Scanner(file);
			 return scanner;
		 } catch (Exception e) { e.printStackTrace(); }
		 return null;
	 }
	 
	 public static boolean isFile(String path) {
		 try {
			 FileSystemView fsv = FileSystemView.getFileSystemView();
			 FileSystem fs = FileSystems.getDefault();
			 File file = new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path);
			 return file.isFile();
		 } catch (Exception e) {
			 return false;
		 }
	 }
	 
	 public static File getFile(String path) {
		 try {
			 FileSystemView fsv = FileSystemView.getFileSystemView();
			 FileSystem fs = FileSystems.getDefault();
			 File dir = new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data");
			 dir.mkdir();
			 File file = new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path);
			 if (!file.isFile()) {
				 file.createNewFile();
				 FileUtilities.log("File " + path + " created in directory " + fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "\n");
			 } else {
				 FileUtilities.log("File " + path + " already exists in directory " + fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "\n");
	         }
			 return file;
		 } catch (Exception e) {
			 FileUtilities.log("File loading failed");
		 }
		 return null;
	 }
	 
	 public static void createFile(String path) {
		 try {
			 FileSystemView fsv = FileSystemView.getFileSystemView();
			 FileSystem fs = FileSystems.getDefault();
			 File dir = new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data");
			 dir.mkdir();
			 File file = new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path);
			 if (!file.isFile()) {
				 file.createNewFile();
				 FileUtilities.log("File " + path + " created in directory " + fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "\n");
			 } else {
				 FileUtilities.log("File " + path + " already exists in directory " + fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "\n");
	         }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public static void deleteFile(String path) {
		 try {
			 FileSystemView fsv = FileSystemView.getFileSystemView();
			 FileSystem fs = FileSystems.getDefault();
			 File file = new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path);
			 if (file.isFile()) {
				 file.delete();
				 FileUtilities.log("File " + path + " removed in directory " + fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "\n");
			 } else {
				 FileUtilities.log("File " + path + " already does not exist in directory " + fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "\n");
	         }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public static void clearFile(String path) {
		 try {
			 FileSystemView fsv = FileSystemView.getFileSystemView();
			 FileSystem fs = FileSystems.getDefault();
			 File file = new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path);
			 if (file.isFile()) {
				 file.delete();
				 file.createNewFile();
				 FileUtilities.log("File " + path + " reset in directory " + fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "\n");
			 } else {
				 FileUtilities.log("File " + path + " already does not exist in directory " + fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "\n");
	         }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	 }
	 
	 public static void writeToPosition(String path, String str, long position) {
		 try {
			 	FileSystemView fsv = FileSystemView.getFileSystemView();
			 	FileSystem fs = FileSystems.getDefault();
			 	if (!(new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path)).isFile()) {FileUtilities.log("File does not exist to write to" + "\n"); return;}
			    RandomAccessFile writer = new RandomAccessFile(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path, "rw");
			    writer.seek(position);
			    writer.writeChars(str);
			    writer.close();
			 } catch (Exception e) {
				 e.printStackTrace();
			 }
		}
	 
	 public static void writeToPosition(String path, int str, long position) {
		 try {
			 	FileSystemView fsv = FileSystemView.getFileSystemView();
			 	FileSystem fs = FileSystems.getDefault();
			 	if (!(new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path)).isFile()) {FileUtilities.log("File does not exist to write to" + "\n"); return;}
			    RandomAccessFile writer = new RandomAccessFile(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path, "rw");
			    writer.seek(position);
			    writer.writeInt(str);
			    writer.close();
			 } catch (Exception e) {
				 e.printStackTrace();
			 }
		}
	 
	 public static void writeToPosition(String path, double str, long position) {
		 try {
			 	FileSystemView fsv = FileSystemView.getFileSystemView();
			 	FileSystem fs = FileSystems.getDefault();
			 	if (!(new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path)).isFile()) {FileUtilities.log("File does not exist to write to" + "\n"); return;}
			    RandomAccessFile writer = new RandomAccessFile(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path, "rw");
			    writer.seek(position);
			    writer.writeDouble(str);
			    writer.close();
			 } catch (Exception e) {
				 e.printStackTrace();
			 }
		}
	 
	 public static String readFromPosition(String path, int length, long position) {
		 try {
			FileSystemView fsv = FileSystemView.getFileSystemView();
		 	FileSystem fs = FileSystems.getDefault();
		 	if (!(new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path)).isFile())  {FileUtilities.log("File does not exist to read from" + "\n"); return "";}
		 	RandomAccessFile writer = new RandomAccessFile(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path, "r");
		    writer.seek(position);
		    String readString = "";
		    for (int i = 0; i < length; i++) {
		    	try {
		    		readString += writer.readChar();
		    	} catch (EOFException e) {
		    		FileUtilities.log("End of file reached" + "\n");
		    		break;
		    	}
		    }
		    writer.close();
		    return readString;
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return "";
	 }
	 
	 public static int readFromPositionInt(String path, long position) {
		 try {
			FileSystemView fsv = FileSystemView.getFileSystemView();
		 	FileSystem fs = FileSystems.getDefault();
		 	if (!(new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path)).isFile())  {FileUtilities.log("File does not exist to read from" + "\n"); return 0;}
		 	RandomAccessFile writer = new RandomAccessFile(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path, "r");
		    writer.seek(position);
		    int readInt = writer.readInt();
		    writer.close();
		    return readInt;
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return 0;
	 }
	 
	 public static double readFromPositionDouble(String path, long position) {
		 try {
			FileSystemView fsv = FileSystemView.getFileSystemView();
		 	FileSystem fs = FileSystems.getDefault();
		 	if (!(new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path)).isFile())  {FileUtilities.log("File does not exist to read from" + "\n"); return 0.0;}
		 	RandomAccessFile writer = new RandomAccessFile(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path, "r");
		    writer.seek(position);
		    double readDouble = writer.readDouble();
		    writer.close();
		    return readDouble;
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return 0.0;
	 }
	 
	 public static String readEntireFile(String path) {
		 try {
			FileSystemView fsv = FileSystemView.getFileSystemView();
		 	FileSystem fs = FileSystems.getDefault();
		 	if (!(new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path)).isFile())  {FileUtilities.log("File does not exist to read from" + "\n"); return "";}
		 	File fileIn = new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + path);
		 	Scanner scanner = new Scanner(fileIn);
		 	scanner.useDelimiter("\\Z");
		 	String file = scanner.next();
		 	scanner.close();
		 	scanner = null;
		    return file;
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		 return "";
	 }
	 
	 public static Clip getAudioFile(String path) {
		 try {
			 AudioInputStream inputStream;
			 if (DEBUG) {
				 File file = new File("data/audio/" + path);
				 inputStream = AudioSystem.getAudioInputStream(file);
			 } else {
				 inputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(Frame.GameLoop.class.getResourceAsStream("/org/data/audio/" + path)));
			 }
			 Clip audioClip = (Clip) AudioSystem.getClip();
			 audioClip.open(inputStream);
			 inputStream.close();
			 inputStream = null;
			 Runtime.getRuntime().gc();
			 return audioClip;
		 } catch (UnsupportedAudioFileException e) {
			 FileUtilities.log("Audio format of file data/audio/" + path + " not supported" + "\n");
	 	 } catch (IOException e) {
			 FileUtilities.log("Error while loading audio file data/audio/" + path + "\n");
		 } catch (Exception e) {
			 e.printStackTrace();
		 } 
		 return null;
	 }
	 
	 public static void log(String msg) {
		 
		 try {
		 if (logger == null) {
			 //fsv.createNewFolder(new File(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data"));
			 logger = new FileWriter(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "client_" + TIMESTAMP_AT_RUNTIME + ".txt");
		 }
		 System.out.print(msg);
		 logger.write(msg);
		 } catch (Exception e) {
			 System.out.println("Ironic... \nError when logging messages");
			 e.printStackTrace();
			 System.exit(120);
		 }
		 
	 }
	 
	 public static void log(String msg, boolean logVerbose) {
		 if (!logVerbose) log(msg);
		 else {
			 try {
			 if (logger == null) logger = new FileWriter(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "client_" + TIMESTAMP_AT_RUNTIME + ".txt");
			 //System.out.print(msg);
			 logger.write(msg);
			 } catch (Exception e) {
				 System.out.println("Ironic... \nError when logging messages");
				 e.printStackTrace();
				 System.exit(120);
			 }
		 }
	 }
	 
	 public static void logLevelGeneration(String msg) {
		 
		 try {
		 if (levelLogger == null) levelLogger = new FileWriter(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "world_" + TIMESTAMP_AT_RUNTIME + ".txt");
		 levelLogger.write(msg + "\n");
		 } catch (Exception e) {
			 System.out.println("Ironic... \nError when logging messages");
			 e.printStackTrace();
			 System.exit(120);
		 }
		 
	 }
	 
	 public static void closeLog() {
		 try {
			if (logger != null) logger.close();
			if (levelLogger != null) levelLogger.close();
		} catch (IOException e) {
			 System.out.println("Ironic... \nError when closing logger");
			 e.printStackTrace();
			 System.exit(121);
		}
	 }
}
