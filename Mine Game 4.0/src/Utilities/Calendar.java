package Utilities;

public class Calendar {
public static long initialTime;
	
	public static void prepareCalendar(long time) {
		initialTime = time;
	}
	
	public static long timeElapsedMillis() {
		return System.currentTimeMillis() - initialTime;
	}
	
	public static long timeElapsed() {
		return (System.currentTimeMillis() - initialTime)/1000;
	}
}
