package Entities;

import java.awt.Graphics;

import Networking.ServerLevel;
import SingleplayerClient.Level;

public class MultiPlayer {
	private String IPAddress = "";
	private static int spriteWidth = 64;
	private static int spriteHeight = 128;
	private int x, y;
	String name;
	ServerLevel level;

	public MultiPlayer(ServerLevel level, String name, int x, int y, String IPAddress) {
		this.name = name;
		this.level = level;
		this.x = x;
		this.y = y;
		this.IPAddress = IPAddress;
	}
	
	public String getIPAddress() {
		return IPAddress;
	}

	public boolean hasCollided(int deltaX, int deltaY) {
		return false;
	}

	public void tick() {
		
	}
	
	public boolean checkConflict() {
		return false;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void move(int deltaX, int deltaY) {
		x += deltaX;
		y += deltaY;
		if (x < 0) x = 0;
		if (y < 0) y = 0;
	}
}
