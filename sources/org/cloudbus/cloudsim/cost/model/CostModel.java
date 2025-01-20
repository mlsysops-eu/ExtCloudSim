/*
	Implemented by the Computer Systems Lab, University of Thessaly (https://csl.e-ce.uth.gr)
	for the MLSysOps project (https://mlsysops.eu)
	 
	License: LGPL - https://www.gnu.org/licenses/lgpl-3.0.en.html
	 
	Copyright (c) 2024, The University of Thessaly, Greece
	 
	Contact: Bowen Sun bsun@uth.gr
	         Christos Antonopoulos  cda@uth.gr
 */

package org.cloudbus.cloudsim.cost.model;

import org.cloudbus.cloudsim.Vm;

/**
 * The PowerModel interface needs to be implemented in order to provide a model of power consumption
 * depending on utilization for system components.
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
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public interface CostModel {

	/**
	 * Get power consumption by the utilization percentage according to the power model.
	 * 
	 * @param utilization the utilization
	 * @return power consumption
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public double getSLAVMigration(Vm vm) throws IllegalArgumentException;
	public double getSLAVOverload(Vm vm) throws IllegalArgumentException;
	public double getSLAVCrash(Vm vm) throws IllegalArgumentException;

}
