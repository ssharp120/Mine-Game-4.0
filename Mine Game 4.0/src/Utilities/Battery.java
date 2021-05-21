package Utilities;

public interface Battery extends Electricity {
	void charge(double current);
	void drain(double current);
	double getPowerStorageCapacity();
	double getCurrentPowerStored();
}
