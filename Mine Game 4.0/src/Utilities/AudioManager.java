package Utilities;


import javax.sound.sampled.Clip;

import Libraries.MediaLibrary;

public class AudioManager {
	Clip currentClip;
	
	public void play(int index) {
		currentClip = FileUtilities.getAudioFile(MediaLibrary.getSoundFromLibrary(index));
		currentClip.start();
	}
	
	public synchronized void clearAudioStreams() {
		if (currentClip != null && !currentClip.isRunning()) {
			currentClip.close();
		}
	}
}
