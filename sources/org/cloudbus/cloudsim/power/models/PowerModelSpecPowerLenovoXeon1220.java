
/*
	Implemented by the Computer Systems Lab, University of Thessaly (https://csl.e-ce.uth.gr)
	for the MLSysOps project (https://mlsysops.eu)
	 
	License: LGPL - https://www.gnu.org/licenses/lgpl-3.0.en.html
	 
	Copyright (c) 2024, The University of Thessaly, Greece
	 
	Contact: Bowen Sun bsun@uth.gr
	         Christos Antonopoulos  cda@uth.gr
 */


package org.cloudbus.cloudsim.power.models;

import org.cloudbus.cloudsim.examples.power.Constants;

/**
 * The power model of a Lenovo node based on an Intel Xeon 1220 processor
 */
public class PowerModelSpecPowerLenovoXeon1220 extends PowerModelSpecPower {

	/** The power. */
	
	private static final double[] power = { 14.7, 18.7, 22.7, 26.7, 30.7, 34.7, 38.7, 43, 46.7, 50.7, 54.7 };
	private static final double[] frequency_list = {2.0, 2.5, 3.0, 3.3};
	private static final double[] voltage_nominal_list = {0.851, 0.922, 1.075, 1.146};
	private static final double[][] voltage_extended_list = { 
		{0.666, 0.741, 0.865, 0.929},
		{0.655, 0.730, 0.852, 0.916},
		{0.643, 0.718, 0.838, 0.902}
	};


	private static final double[][] overhead = {
			{1.348819874, 1.152160567, 1.037112444, 1.0},
			{1.038686363, 1.022875707, 1.020408163, 1.0},
			{1.611362471, 1.306596118, 1.102805429, 1.0},
	};
	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.power.models.PowerModelSpecPower#getPowerData(int)
	 */
	@Override
	protected double getPowerData(int index) {
		return power[index];
	}
	
	protected double powerEstimation(double utilization, double voltage, double frequency) {
		double my_power = 0.0;
		//System.out.println("f " +  frequency + "v " + voltage + "u " + utilization);
		
		if(utilization > 6e-4)
			my_power = 18.98 * utilization * Math.pow(voltage, 2) * frequency + 34.01;
		
		return my_power;
	}
	
	protected double frequency(int operatingPoint) {
		if (operatingPoint > -1 && operatingPoint < 4)
			return frequency_list[operatingPoint];
		
		return frequency_list[Constants.POINTS-1];
	}
	
	protected double voltage(int operatingPoint, int voltageClass, int configuration) {
		if (operatingPoint > -1 && operatingPoint < Constants.POINTS) {
			if (configuration == 0)
				return voltage_nominal_list[operatingPoint];
			else if (configuration == 1)
				return voltage_extended_list[voltageClass][operatingPoint];
		}
		
		return voltage_nominal_list[Constants.POINTS-1];
	}

	protected double estimationOverhead(int vmClass, int operatingPoint) {
		return overhead[vmClass][operatingPoint];
	}

}
