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


public interface CostModel {

	/**
	 * Get SLA cost due to Migrations, Server overloads or Server crashes.
	 * 
	 * @param the target VM
	 * @return power consumption
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public double getSLAVMigration(Vm vm) throws IllegalArgumentException;
	public double getSLAVOverload(Vm vm) throws IllegalArgumentException;
	public double getSLAVCrash(Vm vm) throws IllegalArgumentException;
}
