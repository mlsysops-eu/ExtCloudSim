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
 * The power model of an IBM server x3550 (2 x [Xeon X5675 3067 MHz, 6 cores], 16GB).
 * http://www.spec.org/power_ssj2008/results/res2011q2/power_ssj2008-20110406-00368.html
 * 
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public class PowerModelSpecPowerXgene3 extends PowerModelSpecPower {

	/** The power. */

	private static double[] power = { 14.7, 18.7, 22.7, 26.7, 30.7, 34.7, 38.7, 43, 46.7, 50.7, 54.7 };
	private static final double[] frequency_list = {0.4, 1.3, 2.2, 3.0};
	private static final double[] voltage_nominal_list = {0.880, 0.880, 0.880, 0.880};
	private static final double[][] voltage_extended_list = {
			{0.790, 0.790, 0.830, 0.840},
			{0.790, 0.790, 0.830, 0.840},
			{0.790, 0.790, 0.830, 0.840},
	};
	
//	private static final double[][] overhead = {
//			{0.410009, 0.776076, 0.914163, 1.0},
//			{0.194101, 0.567759, 0.852739, 1.0},
//			{0.133333, 0.433333, 0.733333, 1.0}
//	};
	
//	private static final double[][] overhead = {
//			{1.589991, 1.223924, 1.085837, 1.0},
//			{1.805899, 1.432241, 1.147261, 1.0},
//			{1.866667, 1.566667, 1.266667, 1.0}
//	};

	private static final double[][] overhead = {
			{2.438970852, 1.288533597, 1.083896821, 1.0},
			{5.151956971, 1.761310697, 1.142691761, 1.0},
			{7.50001875, 2.307694083, 1.303636983, 1.0}
	};

/*	private static final double[][] overhead = {
			{2.438970852, 1.288533597, 1.093896821, 1.0},
			{5.151956971, 1.761310697, 1.172691761, 1.0},
			{7.50001875, 2.307694083, 1.363636983, 1.0}
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
			my_power = 34.37582083 * utilization * Math.pow(voltage, 2) * frequency + 52.47789038;
		
		//System.out.println("p "+ my_power);
		
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
