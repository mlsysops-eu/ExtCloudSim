/*
	Implemented by the Computer Systems Lab, University of Thessaly (https://csl.e-ce.uth.gr)
	for the MLSysOps project (https://mlsysops.eu)
	 
	License: LGPL - https://www.gnu.org/licenses/lgpl-3.0.en.html
	 
	Copyright (c) 2024, The University of Thessaly, Greece
	 
	Contact: Bowen Sun bsun@uth.gr
	         Christos Antonopoulos  cda@uth.gr
 */
package org.cloudbus.cloudsim.power;

import java.util.List;


import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;


public class PowerVmAllocationPolicyNodeIndependantNominal extends
		PowerVmAllocationPolicyMigrationAbstract {

	/** The safety parameter. */
	private double utilizationThreshold = 1.0;
	
	/**
	 * Instantiates a new power vm allocation policy migration mad.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 * @param safetyParameter the safety parameter
	 */
	public PowerVmAllocationPolicyNodeIndependantNominal(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy,
			double safetyParameter) {
		super(hostList, vmSelectionPolicy);
		setUtilizationThreshold(utilizationThreshold);
	}

	/**
	 * Checks if is host over utilized.
	 * 
	 * @param _host the _host
	 * @return true, if is host over utilized
	 */
	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		double upperThreshold = Constants.THRESHOLD;
		double utilization = 0.0;
		
		utilization = OptimalConfigurationNode(host);
		
		return utilization > upperThreshold;
	}
	
	public static double OptimalConfigurationNode (PowerHost _host) {
		PowerHostUtilizationHistory host = (PowerHostUtilizationHistory) _host;
		int operatingPoint = host.getOperatingPoint();
		
		double load = 0;
		for (Vm vm : host.getVmList()) {
			double vm_current = vm.get_vm_normalized_req_previous() * vm.getMips();
			double change = host.getEstimationOverhead(vm.get_class(), operatingPoint);
			double mips = host.getTotalMips() * change;
			vm_current = vm_current / mips;
			
			load += vm_current;
		}
		
		return load;
	}

	/**
	 * Sets the utilization threshold.
	 * 
	 * @param utilizationThreshold the new utilization threshold
	 */
	protected void setUtilizationThreshold(double utilizationThreshold) {
		this.utilizationThreshold = utilizationThreshold;
	}

	/**
	 * Gets the utilization threshold.
	 * 
	 * @return the utilization threshold
	 */
	protected double getUtilizationThreshold() {
		return utilizationThreshold;
	}

}
