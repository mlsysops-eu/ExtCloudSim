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
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.power.PowerHost;

public abstract class CostEstimation{

	/*
	 * (non-Javadoc)
	 * @see gridsim.virtualization.power.PowerModel#getPower(double)
	 */
	
	public static double getRequestedVM(PowerHost host, Vm vm, int op) {
		double change = host.getEstimationOverhead(vm.get_class(), op);
		double vm_current = vm.getTotalUtilizationOfCpu(CloudSim.clock() - Constants.SCHEDULING_INTERVAL) * vm.getMips() * change;
		double host_mips = host.getTotalMips();
		double load = vm_current / host_mips;
		
		return load;
	}
	
	public static double getRequestedVmPrevious(PowerHost host, Vm vm, int op) {
		double change = host.getEstimationOverhead(vm.get_class(), op);
		double vm_current = vm.get_vm_normalized_req_previous() * vm.getMips() * change;
		double host_mips = host.getTotalMips();
		double load = vm_current / host_mips;
		
//		System.out.println("previous " + vm.get_vm_normalized_req_previous() + " " + load + " " + vm.getMips());
		
		return load;
	}
	
	public static double getRequestedVmHistory(PowerHost host, Vm vm, int op)  {
		double change = host.getEstimationOverhead(vm.get_class(), op);
		double vm_current = vm.getHistory() * vm.getMips() * change;
		double host_mips = host.getTotalMips();
		double load = vm_current / host_mips;
		
		return load;
	}
	
	public static double getRequestedVM2(PowerHost host, Vm vm, int op, double vm_current) {
		double change = host.getEstimationOverhead(vm.get_class(), op);
		vm_current *= vm.getMips() * change;
		double host_mips = host.getTotalMips();
		double load = vm_current / host_mips;
		
		return load;
	}
	
	public static double getVmMaxRequestOP(PowerHost host, Vm vm, int op) {
		double change = host.getEstimationOverhead(vm.get_class(), op);
		double vm_current = vm.getMips() * change;
		double host_mips = host.getTotalMips();
		double load = vm_current / host_mips;
		
		return load;
	}
	
	public static double getAllocatedVM (double load, double requested) {
		double cpu_current_alloc = 0.0;
		
		if (load > 1.0)
			cpu_current_alloc = requested / load;
		else
			cpu_current_alloc = requested;
		
//		System.out.println("get " + cpu_current_alloc);
		
		return cpu_current_alloc;
	}
	
	public static double getHostPriority (PowerHost host) {
		int size = host.getVmList().size();
		double priority = 0;
		
		for (Vm vm : host.getVmList())
			priority += (double)vm.getRate();
		
		if (size != 0)
			priority /= host.getVmList().size();
		
		return priority;
	}
	
	public static double getUtilizationHost (double load) {
		double utilization = 0.0;
		
		if (load > 1.0)
			utilization = 1.0;
		else
			utilization = load;
		
		return utilization;
	}
	
	public static double getLoadHost(PowerHost host, int op) {
		double load = 0.0;
		
		for (Vm vm : host.getVmList()) {
			double change = host.getEstimationOverhead(vm.get_class(), op);
			double vm_current = vm.getTotalUtilizationOfCpu(CloudSim.clock() - Constants.SCHEDULING_INTERVAL) * vm.getMips() * change;
			double host_mips = host.getTotalMips();
			vm_current = vm_current / host_mips;
			
			load += vm_current;
		}
		
		return load;
	}
	
	public static double getPreviousLoadHost(PowerHost host, int op) {
		double load = 0.0;
		
		for (Vm vm : host.getVmList()) {
			double change = host.getEstimationOverhead(vm.get_class(), op);
			double vm_current = vm.get_vm_normalized_req_previous() * vm.getMips() * change;
			double mips = host.getTotalMips();
			vm_current = vm_current / mips;
			
			load += vm_current;			
		}
		return load;
	}
	
	public static double getHistoryLoadHost (PowerHost host, int op) {
		double load = 0.0;
		
		for (Vm vm : host.getVmList()) {
			double change = host.getEstimationOverhead(vm.get_class(), op);
			double vm_current = vm.getHistory() * vm.getMips() * change;
			double mips = host.getTotalMips();
			vm_current = vm_current / mips;
			
			load += vm_current;
		}		
		return load;
	}
	
	public static double getSLAVMigration(Vm vm) throws IllegalArgumentException {
		double rate = vm.getRate();
		double price = vm.getPrice();
		double time = Constants.TIME_SLOT;
		
		return rate * 1.0 * price * time;
	}
	
	public static double getSLAVOverload(PowerHost host, Vm vm, int op) throws IllegalArgumentException {
		double rate = vm.getRate();
//		double requested = getRequestedVM(host, vm, op);
		double requested = vm.get_vm_normalized_req();
		double requested_max = getVmMaxRequestOP(host, vm, op);
		double allocated = vm.get_vm_normalized_alloc();
		double price = vm.getPrice();
		double time = Constants.TIME_SLOT;
		
		if (requested > allocated) {
			if (Constants.PENALTY == 0)
				return vm.getRate() * vm.getPrice() * time;
			else
				return vm.getRate() * (requested_max - allocated)/requested_max * vm.getPrice() * time;
		}
		else
			return 0.0;
	}
	
	
	public static double getSLAVCrash(Vm vm) throws IllegalArgumentException {
		double rate = vm.getRate();
		double price = vm.getPrice();
		double time = Constants.TIME_SLOT;
		
		return rate * 1.0 * price * time;
	}
}
