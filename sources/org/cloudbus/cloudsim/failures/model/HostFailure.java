/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.failures.model;

import java.util.Random;

import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.power.PowerHost;

/**
 * PowerHost class enables simulation of power-aware hosts.
 * 
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * 
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class HostFailure {

	public static final double maxRange = 1.0;
	public static final double minRange = 0.0;
	public static final double beta[] = {0.005, 0.004, 0.0033, 0.00303}; 
	public static final double gamma = 100;
		
	public static void hostFailure (PowerHost host, int operatingPoint, int configuration) {
		Random rand = new Random();
		double n = rand.nextDouble();
		
		if (n < Constants.FAILURE && configuration == 1) {
			System.out.println("MPOOOOOOOOOOOOOOOOOOOOOOOOOOOM");
			host.setIsHostCrashed(1);
		}
	}
}
