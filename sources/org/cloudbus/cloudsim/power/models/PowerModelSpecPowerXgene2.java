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
 * The power model of an Applied Micro X-Gene 2 ARM microserver
 */
public class PowerModelSpecPowerXgene2 extends PowerModelSpecPower {

	private static double[] power = { 14.7, 18.7, 22.7, 26.7, 30.7, 34.7, 38.7, 43, 46.7, 50.7, 54.7 };
	private static final double[] frequency_list = {0.3, 1.0, 1.7, 2.4};
	private static final double[] voltage_nominal_list = {0.980, 0.980, 0.980, 0.980};
	private static final double[][] voltage_extended_list = {
			{0.980, 0.980, 0.980, 0.980},
			{0.780, 0.790, 0.930, 0.950},
			{0.780, 0.790, 0.930, 0.950},
	};
	
	
	private static final double[][] overhead = {
			{2.4, 1.23, 1.07, 1.0},
			{4.9, 1.60, 1.16, 1.0},
			{8, 2.4, 1.41, 1.0}
	};

	private static final double[] failure = {1.27e-06, 1.27e-06, 0.0003617441};
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
		
		if(utilization > 6e-4)
			my_power = 8.39 * utilization * Math.pow(0.98, 2) * frequency + 35.99;
		
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
