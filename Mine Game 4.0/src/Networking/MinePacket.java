package Networking;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MinePacket {
	public enum packetType {
		CONNECTION,
		TILE_REQUEST,
		MOVEMENT_REQUEST
	}
	public enum packetSource {
		CLIENT,
		SERVER
	}
	
	private byte[] processedData;
	private packetType type;
	private packetSource source;
	
	private boolean validState;
	
	public MinePacket(packetType type, packetSource source) {
		this.type = type;
		this.source = source;
		processData();
	}
	
	public MinePacket(packetType type, packetSource source, byte[] inputData) {
		this.type = type;
		this.source = source;
		processData(inputData);
	}
	
	public void processData() {
		if (source == packetSource.CLIENT) {
			switch (type) {
			case CONNECTION:
				processedData = "connect".getBytes();
				validState = true;
				break;
			case TILE_REQUEST: 
				processedData = "tiles".getBytes();
				validState = true;
				break;				
			default: validState = false;
			}
		}
	}
	
	public void processData(byte[] inputData) {
		if (inputData == null) return;
		
		if (source == packetSource.SERVER) {
			switch (type) {
			case TILE_REQUEST: validState = true; break;
			case CONNECTION: validState = true;	break;
			default: validState = false;
			}
		} if (source == packetSource.CLIENT) {
			switch (type) {
			case MOVEMENT_REQUEST: validState = true; break;
			default: validState = false;
			}
		}
		
		if (validState) processedData = inputData;
	}
	
	public byte[] getProcessedData() {
		return processedData;
	}
	
	public boolean valid() {
		return validState;
	}
	
	public boolean send(DatagramSocket socket, InetAddress IPAddress, int port) {
		if (port < 1024 || port > 65535 || IPAddress == null) throw new IllegalArgumentException("Failed to send packet: invalid address");
		if (processedData == null && processedData.length > 65535) throw new IllegalArgumentException("Failed to send packet: invalid data");
		
		try {
			DatagramPacket pendingPacket = new DatagramPacket(processedData, processedData.length, IPAddress, port);
			socket.send(pendingPacket);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
