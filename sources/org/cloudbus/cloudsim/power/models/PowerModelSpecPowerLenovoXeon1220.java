
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
	
//	private static final double[][] overhead = {
//			{0.741388838666667, 0.867934581833334, 0.964215602, 1.0},
//			{0.962754529090909, 0.977635887818182, 0.98, 1.0},
//			{0.6205928323125, 0.7653474448125, 0.9067782706875, 1.0}
//	};
	

//	private static final double[][] overhead = {
//			{1.2586111613, 1.1320654182, 1.035784398, 1.0},
//			{1.0372454709, 1.0223641122, 1.02, 1.0},
//			{1.3794071677, 1.2346525552, 1.0932217293, 1.0}
//	};

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
