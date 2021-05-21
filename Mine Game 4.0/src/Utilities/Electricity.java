package Utilities;

public interface Electricity {
	static double calculatePower(double voltage, double current) {
		return voltage * current;
	}
	
	static double findResistance(double voltage, double current) {
		return voltage / current;
	}
	
	static double findCurrent(double voltage, double resistance) {
		return voltage / resistance;
	}
	
	static double findVoltage(double current, double resistance) {
		return current * resistance;
	}
}
