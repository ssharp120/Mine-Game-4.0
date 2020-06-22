package Utilities;

import java.awt.Image;
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

public class FileUtilities {
	private static final boolean DEBUG = true;
	public static final long TIMESTAMP_AT_RUNTIME = System.currentTimeMillis();
	
	 static FileSystemView fsv = FileSystemView.getFileSystemView();
	 static FileSystem fs = FileSystems.getDefault();
	 static FileWriter logger;
	
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
		 if (logger == null) logger = new FileWriter(fsv.getHomeDirectory() + fs.getSeparator() + "Mine Game 4.0 Data" + fs.getSeparator() + "client_" + TIMESTAMP_AT_RUNTIME + ".txt");
		 System.out.print(msg);
		 logger.write(msg);
		 } catch (Exception e) {
			 System.out.println("Ironic... \nError when logging messages");
			 e.printStackTrace();
			 System.exit(120);
		 }
		 
	 }
	 
	 public static void closeLog() {
		 try {
			if (logger != null) logger.close();
		} catch (IOException e) {
			 System.out.println("Ironic... \nError when closing logger");
			 e.printStackTrace();
			 System.exit(121);
		}
	 }
}
