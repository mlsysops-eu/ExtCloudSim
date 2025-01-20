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
public class PowerModelSpecPowerXgene2 extends PowerModelSpecPower {

	/** The power. */

//	private static double[] power = { 14.7, 18.7, 22.7, 26.7, 30.7, 34.7, 38.7, 43, 46.7, 50.7, 54.7 };
//	private static final double[] frequency_list = {0.3, 1.2, 2.4};
//	private static final double[] voltage_nominal_list = {0.980, 0.980, 0.980};
//	private static final double[][] voltage_extended_list = {
//			{0.656, 0.770, 0.910},
//			{0.649, 0.760, 0.900},
//			{0.634, 0.750, 0.890}
//	};
//	
//	private static final double[][] overhead = {
//			{0.145892621541667, 0.5478534322, 1.0},
//			{0.238958685166667, 0.754750840166667, 1.0},
//			{0.530572986, 0.9170316185, 1.0}
//	};
	
	private static double[] power = { 14.7, 18.7, 22.7, 26.7, 30.7, 34.7, 38.7, 43, 46.7, 50.7, 54.7 };
	private static final double[] frequency_list = {0.3, 1.0, 1.7, 2.4};
	private static final double[] voltage_nominal_list = {0.980, 0.980, 0.980, 0.980};
	private static final double[][] voltage_extended_list = {
			{0.980, 0.980, 0.980, 0.980},
			{0.780, 0.790, 0.930, 0.950},
			{0.780, 0.790, 0.930, 0.950},
	};
	
	//private static final double[][] overhead = {
	//		{0.416707, 0.810722, 0.934223, 1.0},
	//		{0.203694, 0.622126, 0.86056, 1.0},
	//		{0.125, 0.416667, 0.708333, 1.0}
	//};

	
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
		//System.out.println("f " +  frequency + "v " + voltage + "u " + utilization);
		
		if(utilization > 6e-4)
			my_power = 8.39 * utilization * Math.pow(0.98, 2) * frequency + 35.99;
		
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
