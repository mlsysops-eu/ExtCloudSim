/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.cost.model.CostEstimation;
import org.cloudbus.cloudsim.examples.power.Constants;

/**
 * The Static Threshold (THR) VM allocation policy.
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
public class PowerVmAllocationPolicyMigrationStaticThreshold extends PowerVmAllocationPolicyMigrationAbstract {

	/** The utilization threshold. */
	private double utilizationThreshold = 0.9;

	/**
	 * Instantiates a new power vm allocation policy migration mad.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 * @param utilizationThreshold the utilization threshold
	 */
	public PowerVmAllocationPolicyMigrationStaticThreshold(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy,
			double utilizationThreshold) {
		super(hostList, vmSelectionPolicy);
		setUtilizationThreshold(utilizationThreshold);
	}

	/**
	 * Checks if is host over utilized.
	 * 
	 * @param _host the _host
	 * @return true, if is host over utilized
	 */

// 	@Override
// 	protected boolean isHostOverUtilized(PowerHost host) {
// 		addHistoryEntry(host, getUtilizationThreshold());
// 		double totalRequestedMips = 0;
// 		if (Constants.max) {
// 			for (Vm vm : host.getVmList()) {
// 				totalRequestedMips += vm.getCurrentRequestedTotalMipsMax();
// 			}
// 		}
// 		else {
// 			for (Vm vm : host.getVmList()) {
// 				totalRequestedMips += vm.getCurrentRequestedTotalMips();
// 			}
// 		}
// //		for (Vm vm : host.getVmList()) {
// //			totalRequestedMips += vm.getCurrentRequestedTotalMips();
// //		}
// 		double utilization = totalRequestedMips / host.getTotalMips();
// 		return utilization > getUtilizationThreshold();
// 	}
	
	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		addHistoryEntry(host, getUtilizationThreshold());
		int operatingPoint = Constants.POINTS-1;
		
		if( Constants.MY_POLICIES == 1)
			operatingPoint = host.getOperatingPoint();
		
		double load = 0;
		for (Vm vm : host.getVmList()) {
//			double vm_current = vm.getTotalUtilizationOfCpu(CloudSim.clock()) * vm.getMips();
//			double vm_current = vm.get_vm_normalized_req_previous() * vm.getMips();
			double change = host.getEstimationOverhead(vm.get_class(), operatingPoint);
			double vm_current = vm.get_vm_normalized_req_previous() * vm.getMips() * change;
			double mips = host.getTotalMips();
			vm_current = vm_current / mips;
			
			load += vm_current;
		}
		
		double utilization = CostEstimation.getUtilizationHost(load);
		return utilization > Constants.THRESHOLD;
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
