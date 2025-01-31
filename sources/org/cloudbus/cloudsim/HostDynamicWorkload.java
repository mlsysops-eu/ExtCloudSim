/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */
/*
	Extended by the Computer Systems Lab, University of Thessaly (https://csl.e-ce.uth.gr)
	for the MLSysOps project (https://mlsysops.eu)
	 
	Copyright (c) 2024, The University of Thessaly, Greece
	 
	Contact: Bowen Sun bsun@uth.gr
	         Christos Antonopoulos  cda@uth.gr
*/

package org.cloudbus.cloudsim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/**
 * The class of a host supporting dynamic workloads and performance degradation.
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class HostDynamicWorkload extends Host {

	/** The utilization mips. */
	private double utilizationMips;

	/** The previous utilization mips. */
	private double previousUtilizationMips;

	/** The state history. */
	private final List<HostStateHistoryEntry> stateHistory = new LinkedList<HostStateHistoryEntry>();


	/**
	 * Instantiates a new host.
	 * 
	 * @param id the id
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage
	 * @param peList the pe list
	 * @param vmScheduler the VM scheduler
	 */
	public HostDynamicWorkload(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setUtilizationMips(0);
		setPreviousUtilizationMips(0);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.Host#updateVmsProcessing(double)
	 */
	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = super.updateVmsProcessing(currentTime);
		setPreviousUtilizationMips(getUtilizationMips());
		setUtilizationMips(0);
		double hostTotalRequestedMips = 0;
		double penalty = 0;

		for (Vm vm : getVmList()) {
			getVmScheduler().deallocatePesForVm(vm);
		}

		for (Vm vm : getVmList()) {
			getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
		}
		
		// CSV Header for History
	    String AllocationHistoryHeader = String.join(",",
	            "Time", "Vm_Id", "Host_Id", "Vm_Host_Id", "VM_TotalAllocatedMips", 
	            "VM_TotalRequestedMips", "Vm_TotalMips", "VM_TotalRequest_VmTotal_Ratio", 
	            "VM_Class", "VM_UserId", "VM_Mips", "VM_Rate", "VM_NumberOfPes", "VM_Ram",
				"VM_Bw", "VM_Size", "VM_Vmm", "VM_Price", "VM_CurrentAllocatedBw", "VM_CurrentAllocatedMips",
				"VM_CurrentAllocatedRam", "VM_CurrentAllocatedSize", "VM_CurrentRequestedBw",
				"VM_CurrentRequestedMaxMips", "VM_CurrentRequestedMips", "VM_CurrentRequestedRam",
				"VM_CurrentRequestedTotalMips", "VM_Penalty", "VM_normalized_alloc", 
				"VM_normalized_req", "VM_normalized_req_previous", "VM_MaxRam", 
				"Host_AvailableMips", "Host_AllocatedMipsForVm", "Host_Bw",
				"Host_Configuration", "Host_DatacenterId", "Host_Load",
				"Host_MaxAvailableMips", "Host_MaxUtilization", "Host_MaxUtilizationAmongVmsPes",
				"Host_MyUtilization", "Host_MyUtilizationHistory", "Host_NodeClass",
				"Host_NodePriority", "Host_NumberOfFreePes", "Host_NumberOfPes",
				"Host_OperatingPoint", "Host_OptimalConfiguration", "Host_PeriodsActive",
				"Host_PreviousUtilizationMips", "Host_PreviousUtilizationOfCpu",
				"Host_Ram", "Host_Storage", "Host_TempConfiguration", "Host_TempCost", 
				"Host_TempLoad", "Host_TempOperatingPoint", "Host_TempUtilization",
				"Host_TotalAllocatedMipsForVm", "Host_TotalMips", "Host_UtilizationMips",
				"Host_UtilizationOfBw", "Host_UtilizationOfCpu", "Host_UtilizationOfCpuMips",
				"Host_UtilizationOfRam", "Host_VoltageClass",
	            "Host_PE_Capacity", "Host_PE_String",
	            "VM_BeingMigrated", "VM_MigrationSource", "VM_MigrationDestination", 
	            "UnderAllocated", "UnderDiff", "Host_TotalRequestedMips",
	            "Host_Datacenter_Power", "Host_Datacenter_MyPower");
	    
	    StringBuilder data = new StringBuilder();
		String delimeter = ",";

		for (Vm vm : getVmList()) {
			double totalRequestedMips = vm.getCurrentRequestedTotalMips();
			double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

			if (!Log.isDisabled()) {
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
								+ " (Host #" + vm.getHost().getId()
								+ ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
						CloudSim.clock(),
						totalAllocatedMips,
						totalRequestedMips,
						vm.getMips(),
						totalRequestedMips / vm.getMips() * 100);
				
				
				data.append(String.format("%.2f", CloudSim.clock()) + delimeter);
				data.append(String.format("%d", vm.getId()) + delimeter);
				data.append(String.format("%d", getId()) + delimeter);
				data.append(String.format("%d", vm.getHost().getId()) + delimeter);
				data.append(String.format("%.5f", totalAllocatedMips) + delimeter);
				data.append(String.format("%.5f", totalRequestedMips) + delimeter);
				data.append(String.format("%.5f", vm.getMips()) + delimeter);
				data.append(String.format("%.5f", totalRequestedMips / vm.getMips() * 100) + delimeter);
				

				List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
				StringBuilder pesString = new StringBuilder();
				StringBuilder pesStringforCSV = new StringBuilder();
				for (Pe pe : pes) {
					pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
							.getTotalAllocatedMipsForVm(vm)));
					pesStringforCSV.append(String.format("#" + pe.getId() + ":%.2f", pe.getPeProvisioner()
							.getTotalAllocatedMipsForVm(vm)));
				}
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
								+ getNumberOfPes() + " * " + getVmScheduler().getPeCapacity() + ")."
								+ pesString,
						CloudSim.clock());
				
				// VM characteristics
				data.append(String.format("%d", vm.get_class()) + delimeter);
				data.append(String.format("%d", vm.getUserId()) + delimeter);
				data.append(String.format("%.5f", vm.getMips()) + delimeter);
				data.append(String.format("%.5f", vm.getRate()) + delimeter);
				data.append(String.format("%d", vm.getNumberOfPes()) + delimeter);
				data.append(String.format("%d", vm.getRam()) + delimeter);
				data.append(String.format("%d", vm.getBw()) + delimeter);
				data.append(String.format("%d", vm.getSize()) + delimeter);
				data.append(vm.getVmm() + delimeter);
				data.append(String.format("%.5f", vm.getPrice()) + delimeter);
				data.append(String.format("%d", vm.getCurrentAllocatedBw()) + delimeter);
				String formattedString = formatList(vm.getCurrentAllocatedMips(), "%.2f", ", ");
				data.append(formattedString + delimeter);
				data.append(String.format("%d", vm.getCurrentAllocatedRam()) + delimeter);
				data.append(String.format("%d", vm.getCurrentAllocatedSize()) + delimeter);
				data.append(String.format("%d", vm.getCurrentRequestedBw()) + delimeter);
				data.append(String.format("%.5f", vm.getCurrentRequestedMaxMips()) + delimeter);
				String formattedString1 = formatList(vm.getCurrentRequestedMips(), "%.2f", ", ");
				data.append(formattedString1 + delimeter);
				data.append(String.format("%d", vm.getCurrentRequestedRam()) + delimeter);
				data.append(String.format("%.5f", vm.getCurrentRequestedTotalMips()) + delimeter);
				data.append(String.format("%.2f", vm.getPenalty()) + delimeter);
				data.append(String.format("%.5f", vm.get_vm_normalized_alloc()) + delimeter);
				data.append(String.format("%.5f", vm.get_vm_normalized_req()) + delimeter);
				data.append(String.format("%.5f", vm.get_vm_normalized_req_previous()) + delimeter);
				data.append(String.format("%.5f", vm.getMaxRam()) + delimeter);
				
				// Host characteristics
				data.append(String.format("%.5f", this.getAvailableMips()) + delimeter);
				String formattedString2 = formatList(this.getAllocatedMipsForVm(vm), "%.2f", ", ");
				data.append(formattedString2 + delimeter);
				data.append(String.format("%d", this.getBw()) + delimeter);
				data.append(String.format("%d", this.getConfiguration()) + delimeter);
				data.append(String.format("%d", this.getDatacenter().getId()) + delimeter);
				data.append(String.format("%.5f", this.getLoad()) + delimeter);
				data.append(String.format("%.5f", this.getMaxAvailableMips()) + delimeter);
				data.append(String.format("%.5f", this.getMaxUtilization()) + delimeter);
				data.append(String.format("%.5f", this.getMaxUtilizationAmongVmsPes(vm)) + delimeter);
				data.append(String.format("%.5f", this.getMyUtilization()) + delimeter);
				data.append(String.format("%.5f", this.getMyUtilizationHistory()) + delimeter);
				data.append(String.format("%d", this.getNodeClass()) + delimeter);
				data.append(String.format("%d", this.getNodePriority()) + delimeter);
				data.append(String.format("%d", this.getNumberOfFreePes()) + delimeter);
				data.append(String.format("%d", this.getNumberOfPes()) + delimeter);
				data.append(String.format("%d", this.getOperatingPoint()) + delimeter);
				data.append(String.format("%d", this.getOptimalConfiguration()) + delimeter);
				data.append(String.format("%d", this.getPeriodsActive()) + delimeter);
				data.append(String.format("%.5f", this.getPreviousUtilizationMips()) + delimeter);
				data.append(String.format("%.5f", this.getPreviousUtilizationOfCpu()) + delimeter);
				data.append(String.format("%d", this.getRam()) + delimeter);
				data.append(String.format("%d", this.getStorage()) + delimeter);
				data.append(String.format("%d", this.getTempConfiguration()) + delimeter);
				data.append(String.format("%.5f", this.getTempCost()) + delimeter);
				data.append(String.format("%.5f", this.getTempLoad()) + delimeter);
				data.append(String.format("%d", this.getTempOperatingPoint()) + delimeter);
				data.append(String.format("%.5f", this.getTempUtilization()) + delimeter);
				data.append(String.format("%.5f", this.getTotalAllocatedMipsForVm(vm)) + delimeter);
				data.append(String.format("%d", this.getTotalMips()) + delimeter);
				data.append(String.format("%.5f", this.getUtilizationMips()) + delimeter);
				data.append(String.format("%.5f", this.getUtilizationOfBw()) + delimeter);
				data.append(String.format("%.5f", this.getUtilizationOfCpu()) + delimeter);
				data.append(String.format("%.5f", this.getUtilizationOfCpuMips()) + delimeter);
				data.append(String.format("%.5f", this.getUtilizationOfRam()) + delimeter);
				data.append(String.format("%d", this.getVoltageClass()) + delimeter);
				data.append(String.format("%.5f", getVmScheduler().getPeCapacity()) + delimeter);
				data.append(pesStringforCSV + delimeter);
				
			}
			
			boolean MigrateFlagforCSV = false;
			int MigrationSource = -1;
			int MigrationDestination = -1;
			boolean UnderAllocatedFlagforCSV = false;
			double UnderAllocatedDiff = -1;

			if (getVmsMigratingIn().contains(vm)) {
				Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + vm.getId()
						+ " is being migrated to Host #" + getId(), CloudSim.clock());
				MigrateFlagforCSV = true;
				MigrationDestination = getId();
			} else {
				
				if (totalAllocatedMips + 0.1 < totalRequestedMips) {
					Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + vm.getId()
							+ ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
				}
				
				UnderAllocatedFlagforCSV = true;
				UnderAllocatedDiff = (totalRequestedMips - totalAllocatedMips);

				vm.addStateHistoryEntry(
						currentTime,
						penalty,
						totalAllocatedMips,
						totalRequestedMips,
						(vm.isInMigration() && !getVmsMigratingIn().contains(vm)));
				
				if (vm.isInMigration()) {
					Log.formatLine(
							"%.2f: [Host #" + getId() + "] VM #" + vm.getId() + " is in migration",
							CloudSim.clock());
					
					MigrateFlagforCSV = true;
					MigrationSource = getId();
					
					totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
					if (vm.getTotalUtilizationOfCpu(CloudSim.clock() - Constants.SCHEDULING_INTERVAL) > 8e-6)
						vm.setWasInMigration(1);
				}
			}
			
			data.append(String.format("%b", MigrateFlagforCSV) + delimeter);
			data.append(String.format("%d", MigrationSource) + delimeter);
			data.append(String.format("%d", MigrationDestination) + delimeter);
			data.append(String.format("%b", UnderAllocatedFlagforCSV) + delimeter);
			data.append(String.format("%.5f", UnderAllocatedDiff) + delimeter);

			setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
			hostTotalRequestedMips += totalRequestedMips;
			
			data.append(String.format("%.5f", hostTotalRequestedMips) + delimeter);
			PowerDatacenter powerDatacenter = (PowerDatacenter) this.getDatacenter();
			data.append(String.format("%.5f", powerDatacenter.getPower()) + delimeter);
			data.append(String.format("%.5f", powerDatacenter.getMyPower()));
			data.append("\n");
		}
		
		writeDataRow(data.toString(), AllocationHistoryHeader, Constants.HistoricalInfoPath);

		addStateHistoryEntry(
				currentTime,
				getUtilizationMips(),
				hostTotalRequestedMips,
				(getUtilizationMips() > 0));

			return smallerTime;
	}
	public static <T> String formatList(List<T> list, String format, String delimiter) {
        StringBuilder sb = new StringBuilder();
        if (list==null) {return null;}
        for (int i = 0; i < list.size(); i++) {
            sb.append(String.format(format, list.get(i)));
            if (i < list.size() - 1) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

	/**
	 * Write data row.
	 * 
	 * @param data the data
	 * @param outputPath the output path
	 */
	public static void writeDataRow(String data, String header, String outputPath) {
		File file = new File(outputPath);
		boolean exists = file.exists(); // Check if file already exists
		
		try {
			file.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(0);
		}
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
			if (!exists && header != null) {
	            writer.write(header);
	            writer.newLine();
	        }
			writer.write(data);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Gets the completed vms.
	 * 
	 * @return the completed vms
	 */
	public List<Vm> getCompletedVms() {
		List<Vm> vmsToRemove = new ArrayList<Vm>();
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				continue;
			}
			if (vm.getCurrentRequestedTotalMips() == 0) {
				vmsToRemove.add(vm);
			}
		}
		return vmsToRemove;
	}

	/**
	 * Gets the max utilization among by all PEs.
	 * 
	 * @return the utilization
	 */
	public double getMaxUtilization() {
		return PeList.getMaxUtilization(getPeList());
	}

	/**
	 * Gets the max utilization among by all PEs allocated to the VM.
	 * 
	 * @param vm the vm
	 * @return the utilization
	 */
	public double getMaxUtilizationAmongVmsPes(Vm vm) {
		return PeList.getMaxUtilizationAmongVmsPes(getPeList(), vm);
	}

	/**
	 * Gets the utilization of memory.
	 * 
	 * @return the utilization of memory
	 */
	public double getUtilizationOfRam() {
		return getRamProvisioner().getUsedRam();
	}

	/**
	 * Gets the utilization of bw.
	 * 
	 * @return the utilization of bw
	 */
	public double getUtilizationOfBw() {
		return getBwProvisioner().getUsedBw();
	}

	/**
	 * Get current utilization of CPU in percentage.
	 * 
	 * @return current utilization of CPU in percents
	 */
	public double getUtilizationOfCpu() {
		double utilization = getUtilizationMips() / getTotalMips();
		if (utilization > 1 && utilization < 1.01) {
			utilization = 1;
		}
		return utilization;
	}

	/**
	 * Gets the previous utilization of CPU in percentage.
	 * 
	 * @return the previous utilization of cpu
	 */
	public double getPreviousUtilizationOfCpu() {
		double utilization = getPreviousUtilizationMips() / getTotalMips();
		if (utilization > 1 && utilization < 1.01) {
			utilization = 1;
		}
		return utilization;
	}

	/**
	 * Get current utilization of CPU in MIPS.
	 * 
	 * @return current utilization of CPU in MIPS
	 */
	public double getUtilizationOfCpuMips() {
		return getUtilizationMips();
	}

	/**
	 * Gets the utilization mips.
	 * 
	 * @return the utilization mips
	 */
	public double getUtilizationMips() {
		return utilizationMips;
	}

	/**
	 * Sets the utilization mips.
	 * 
	 * @param utilizationMips the new utilization mips
	 */
	protected void setUtilizationMips(double utilizationMips) {
		this.utilizationMips = utilizationMips;
	}

	/**
	 * Gets the previous utilization mips.
	 * 
	 * @return the previous utilization mips
	 */
	public double getPreviousUtilizationMips() {
		return previousUtilizationMips;
	}

	/**
	 * Sets the previous utilization mips.
	 * 
	 * @param previousUtilizationMips the new previous utilization mips
	 */
	protected void setPreviousUtilizationMips(double previousUtilizationMips) {
		this.previousUtilizationMips = previousUtilizationMips;
	}

	/**
	 * Gets the state history.
	 * 
	 * @return the state history
	 */
	public List<HostStateHistoryEntry> getStateHistory() {
		return stateHistory;
	}

	/**
	 * Adds the state history entry.
	 * 
	 * @param time the time
	 * @param allocatedMips the allocated mips
	 * @param requestedMips the requested mips
	 * @param isActive the is active
	 */
	public
			void
			addStateHistoryEntry(double time, double allocatedMips, double requestedMips, boolean isActive) {

		HostStateHistoryEntry newState = new HostStateHistoryEntry(
				time,
				allocatedMips,
				requestedMips,
				isActive);
		if (!getStateHistory().isEmpty()) {
			HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
			if (previousState.getTime() == time) {
				getStateHistory().set(getStateHistory().size() - 1, newState);
				return;
			}
		}
		getStateHistory().add(newState);
	}

}
