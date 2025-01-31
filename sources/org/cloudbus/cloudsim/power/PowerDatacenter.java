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

package org.cloudbus.cloudsim.power;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import java.util.Map;

import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.failures.model.HostFailure;
import org.cloudbus.cloudsim.cost.model.CostEstimation;


/**
 * PowerDatacenter is a class that enables simulation of power-aware data centers.
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
public class PowerDatacenter extends Datacenter {

	/** The power. */
	private double power;

	/** The disable migrations. */
	private boolean disableMigrations;

	/** The cloudlet submited. */
	private double cloudletSubmitted;

	/** The migration count. */
	private int migrationCount;
	
	private double MyPower;
	private double nextPower;
	
	private boolean myPolicies;
	
	private double energyCost;
	private double penaltyCost;
	private double cost;
	
	private int crashes;
	private int overload;
	private int migrations;
	
	private double crashesCost;
	private double overloadCost;
	private double migrationsCost;
	
	private int overloadPoint;
	
	private int nodes;
	
	private double util;
	
	private int times;
	
	private int[] points = new int[4];
	/**
	 * Instantiates a new datacenter.
	 * 
	 * @param name the name
	 * @param characteristics the res config
	 * @param schedulingInterval the scheduling interval
	 * @param utilizationBound the utilization bound
	 * @param vmAllocationPolicy the vm provisioner
	 * @param storageList the storage list
	 * @throws Exception the exception
	 */
	public PowerDatacenter(
			String name,
			DatacenterCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

		setPenaltyCost(0);
		setEnergyCost(0);
		setCost(0);
		setPower(0.0);
		setDisableMigrations(false);
		setCloudletSubmitted(-1);
		setMigrationCount(0);
		setMyPower(0.0);
		setNextPower(0.0);
		setCrashes(0);
		setMigrations(0);
		setOverload(0);
		setCrashesCost(0);
		setMigrationsCost(0);
		setOverloadCost(0);
		setOverloadPoint(0);
		setNodes(0);
		findload(0);
		
		setExtended(0);
		
		for(int i=0; i<4; i++)
			points[i] = 0;
	}

	/**
	 * Updates processing of each cloudlet running in this PowerDatacenter. It is necessary because
	 * Hosts and VirtualMachines are simple objects, not entities. So, they don't receive events and
	 * updating cloudlets inside them must be called from the outside.
	 * 
	 * @pre $none
	 * @post $none
	 */
	
	@Override
	protected void vmHistory() {
		getVmAllocationPolicy().setVmHistory();
	}
	
	@Override 
	protected void updateCost () {
			double myPowerFrame = getMyPower(); //Power consumption - total power consumption for current simulation frame
			Constants.nominal_hosts_file.println(CloudSim.clock());
			
			//setMyPower(getNextPower());
			int active_nodes = 0;
			int extended_nodes = 0;
			double percentage = 0;

			for (PowerHost host: this.<PowerHost> getHostList()) { // iterating through the list of active hosts
				if (Constants.MY_POLICIES == 1)
					HostFailure.hostFailure(host, host.getOperatingPoint(), host.getConfiguration());
				
				double rate = 0;
				if (host.getVmList().size() != 0) {
					for (Vm vm : host.getVmList()) {
						rate += vm.getRate();
					}
					
					rate /= host.getVmList().size();
					Constants.nominal_hosts_file.println("[Host id: " + host.getId() + ", " + host.getOperatingPoint() + ", " + host.getConfiguration() +", " + rate + "]");
				}
				double utilization = 1;
				
				int inactive = 0;
				for (Vm vm : host.getVmList()) {
					if (vm.getInactive() == 1) {
						inactive += vm.getInactive();
						//vm.setInactive(0);
					}
				}
				
				if (host.getVmList().size() == inactive)
					continue;
				double load=0;
				for (int i=0; i<Constants.k; i++) {
					load = 0;
					
					int operatingPoint = host.getOperatingPoint();
					
					for (Vm vm : host.getVmList()) {
						if (vm.getWasInMigration() == 1  && i < vm.getMigrationSlotNumber(vm.getRam()))
							continue;
						double allocated = vm.get_vm_normalized_alloc();
						load += allocated;
						
//						System.out.println("power " + vm.getId() + " " + allocated);
					}
					
					
					utilization = CostEstimation.getUtilizationHost(load);
					double voltage = host.getVoltage(operatingPoint, host.getVoltageClass(), host.getConfiguration());
					double frequency = host.getFrequency(operatingPoint);
					
					myPowerFrame += host.getPowerEstimation(utilization, voltage, frequency, host.getConfiguration()) * Constants.TIME_SLOT;
				}
					if (host.getVmList().size() != 0) {
						
						if (host.getConfiguration() == 1) {
							setExtended(getExtended() + 1);
							extended_nodes++;
						}
						
						set_points(host.getOperatingPoint());
						
						setNodes(getNodes() + 1);
						active_nodes++;
						findload(loads() + utilization);
						}
				}
			
			Constants.nominal_hosts_file.println();
			percentage = (double)extended_nodes/(double)active_nodes;
			Constants.active_nodes_file.println(Constants.ENERGY_PRICE + " " + percentage + " " + active_nodes + " " + extended_nodes);
			
			
			setEnergyCost(myPowerFrame * Constants.ENERGY_PRICE);
			setMyPower(myPowerFrame);
			
			double sla_penalty = getPenaltyCost();
			int crashes_num = 0;
			int overload_num = 0;
			int migrations_num = 0;
			
			Constants.vm_map_file.println(CloudSim.clock());
			Constants.sla_file.println(CloudSim.clock());
			for (PowerHost host: this.<PowerHost> getHostList()) {
				
				double migrations_penalty;
                double overload_penalty = 0.0;
                double crash_penalty = 0.0;
                int op = host.getOperatingPoint();
                
                int num = 0;
                double mean = 0;
                if(host.getVmList().size() != 0)
                	Constants.vm_map_file.print("Host " + host.getId() + ": ");
                
                double mean_priority = 0;
				for (Vm vm : host.getVmList()) {
				
					mean_priority += vm.getRate();
					if (vm.getInactive() == 1) {
						vm.setInactive(0);
						continue;
					}
					Constants.vm_map_file.print("[" + vm.getId() + "," + vm.getRate() + "] ");
					mean += vm.getRate();
					num++;
					
					migrations_penalty = 0.0;
                    
					for (int timeslot=0; timeslot<Constants.k; timeslot++) {
						//if (vm.getTotalUtilizationOfCpu(CloudSim.clock() - 2*Constants.SCHEDULING_INTERVAL) < 0.00001)
						if (host.isHostCrashed() == 0) {
							if (vm.getWasInMigration() == 1  && timeslot < vm.getMigrationSlotNumber(vm.getRam()) ) {
								sla_penalty += CostEstimation.getSLAVMigration(vm);
								migrations_penalty += CostEstimation.getSLAVMigration(vm);
								
							}
							else if (vm.get_vm_normalized_req() > vm.get_vm_normalized_alloc()) {
								sla_penalty += CostEstimation.getSLAVOverload(host, vm, op);
								overload_penalty += CostEstimation.getSLAVOverload(host, vm, op);
								
							}
						}
						else {
							sla_penalty += CostEstimation.getSLAVCrash(vm);
							crash_penalty = CostEstimation.getSLAVCrash(vm);
							setCrashesCost(getCrashesCost() + crash_penalty);
						}
					}
					
					vm.setWasInMigration(0);
					vm.setPenalty(sla_penalty);
					
					if (migrations_penalty > 0.0) {
						setMigrations (getMigrations() + 1);
						migrations_num++;
					}
					
					setMigrationsCost(getMigrationsCost() + migrations_penalty);
					
					Constants.sla_file.println("[" + migrations_penalty + "," + overload_penalty + "," + crash_penalty + "," + sla_penalty + "] ");
				}

				
				mean_priority /= host.getVmList().size();
				
				if (host.getVmList().size() != 0)
					Log.printLine("mean " + mean_priority + " " + host.getId());
				
				setOverloadCost(getOverloadCost() + overload_penalty);
				
				mean /= num;
				Constants.vm_map_file.println(mean);
				
				if (overload_penalty > 0.0) {
					setOverload(getOverload() + 1);
					overload_num++;
				}
				
				if (host.isHostCrashed() == 1 && host.getVmList().size() != 0) {
					setCrashes(getCrashes() + 1);
					crashes_num++;
				}
				
				host.setIsHostCrashed(0);
				
				if (host.getLoad() > 1) {
					setOverloadPoint(getOverloadPoint() + 1);
					//overload_num++;
				}
			}
			
			for (PowerHost host: this.<PowerHost> getHostList()) {
				if (host.getVmList().size() == 0)
					continue;
				
				int optimalConfiguration = host.getOptimalConfiguration();
				double load = CostEstimation.getLoadHost(host, optimalConfiguration);
				double utilization = CostEstimation.getUtilizationHost(load);
				
				double utilizationHistory = host.getMyUtilizationHistory();
				int periodsActive = host.getPeriodsActive();
				
				utilizationHistory *= periodsActive;
				utilizationHistory += utilization;
				utilizationHistory /= (periodsActive + 1);
				
				host.setPeriodsActive(periodsActive + 1);
				host.setUtilizationHistory(utilizationHistory);
			}
			
			setPenaltyCost(sla_penalty);
			setCost(getPenaltyCost() + getEnergyCost());
			
			Constants.crash_file.println(crashes_num);
			Constants.migration_file.println(migrations_num);
			Constants.overload_file.println(overload_num);
			
			
	}
	@Override
	protected void updateCloudletProcessing() {
		if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
			CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
			schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			return;
		}
		double currentTime = CloudSim.clock();

		// if some time passed since last processing
		if (currentTime > getLastProcessTime()) {
			System.out.println(currentTime + ": A new period of Allocation Start...");

			double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

			
			for (PowerHost host : this.<PowerHost> getHostList()) {
				host.setTempConfiguration(0);
				host.setTempCost(0);
				host.setTempUtilization(host.getUtilizationOfCpu());
			}
			
			if (!isDisableMigrations()) {
				if(Constants.multiDatacenter) {
					if(CloudSim.clock() > 100) { //Uncomment if considering power
						
						List<Map<String, Object>> powerMigrationMap = getVmAllocationPolicy().optimizeAllocationPower(
								getVmList());				
						if (powerMigrationMap != null) {
							System.out.println(CloudSim.clock() + ": Migration Process starts...");
							for (Map<String, Object> migrate : powerMigrationMap) {
								Vm vm = (Vm) migrate.get("vm");
								PowerHost targetHost = (PowerHost) migrate.get("host");
								PowerHost oldHost = (PowerHost) vm.getHost();
		
								if (oldHost == null) {
									System.out.println(currentTime + ": Migration of VM " + vm.getId() + " to Host #" + targetHost.getId() 
									+ " (Datacenter #" + targetHost.getDatacenter().getId() + ") is started.");
								} else {
									System.out.println(currentTime + ": Migration of VM " + vm.getId() + " from Host #" + oldHost.getId() 
									+ " (Datacenter #" + oldHost.getDatacenter().getId() + ") to Host " + targetHost.getId() + " (Datacenter # " + targetHost.getDatacenter().getId() + ") is started.");
									if (oldHost.getDatacenter().getId() == targetHost.getDatacenter().getId()) {
										System.out.println("NOTE: This is an Intra-Datacenter Migration whinin Datacenter #" 
									+ oldHost.getDatacenter().getId() + ".");
									} else { 
										System.out.println("NOTE: This is an Inter-Datacenter Migration, from Datacenter #" 
									+ oldHost.getDatacenter().getId() + " to Datacenter #" + targetHost.getDatacenter().getId());}
								}
		
								targetHost.addMigratingInVm(vm);
								incrementMigrationCount();
		
								send(
										getId(),
										vm.getRam() / ((double) targetHost.getBw() / (8000)),
										CloudSimTags.VM_MIGRATE,
										migrate);
							}
						}
					}
				}
				else {
				
					// Uncomment for normal simulation
					List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
							getVmList());
					
	//				// Uncomment for random data generation
	//				List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocationRandom(
	//						getVmList());
					
	//				System.out.println(CloudSim.clock() + ": Migration Process starts...");
					if (migrationMap != null) {
						for (Map<String, Object> migrate : migrationMap) {
							Vm vm = (Vm) migrate.get("vm");
							PowerHost targetHost = (PowerHost) migrate.get("host");
							PowerHost oldHost = (PowerHost) vm.getHost();
	
							if (oldHost == null) {
								System.out.println(currentTime + ": Migration of VM " + vm.getId() + " to Host " + targetHost.getId() + " is started.");
							} else {
								System.out.println(currentTime + ": Migration of VM " + vm.getId() + " from Host" + oldHost.getId() + " to Host " + targetHost.getId() + " is started.");
								if (oldHost.getDatacenter().getId() == targetHost.getDatacenter().getId()) {
									System.out.println("NOTE: This is an Intra-Datacenter Migration whinin Datacenter #" + oldHost.getDatacenter().getId() + ".");
								} else { 
									System.out.println("NOTE: This is an Inter-Datacenter Migration, from Datacenter #" + oldHost.getDatacenter().getId() + " to Datacenter #" + targetHost.getDatacenter().getId());}
							}
	
							targetHost.addMigratingInVm(vm);
							incrementMigrationCount();
	
							send(
									getId(),
									vm.getRam() / ((double) targetHost.getBw() / (8000)),
									CloudSimTags.VM_MIGRATE,
									migrate);
						}
					}
				}
			}
			
			
			// schedules an event to the next time
			if (minTime != Double.MAX_VALUE) {
				CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
				send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
			}

			setLastProcessTime(currentTime);
		}
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEvents() {
		if (CloudSim.clock() > getLastProcessTime()) {
			return updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
		return 0;
	}

	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		Log.printLine("\n\n--------------------------------------------------------------\n\n");
		Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);
		//System.out.println("TTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
		for (PowerHost host : this.<PowerHost> getHostList()) {
			Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			if (time < minTime) {
				minTime = time;
			}

			Log.formatLine(
					"%.2f: [Host #%d] utilization is %.8f%%",
					currentTime,
					host.getId(),
					host.getUtilizationOfCpu() * 100);
		}
		
		// CSV Header for HostUtil
		String HostUtilHeader = String.join(",",
	           "Time", "Host_Id", "LastTime", "Utilization", "PreviousUtil", 
	           "EnergyConsumption");

		StringBuilder data = new StringBuilder();
		String delimeter = ",";
		
		if (timeDiff > 0) {
			Log.formatLine(
					"\nEnergy consumption for the last time frame from %.2f to %.2f:",
					getLastProcessTime(),
					currentTime);

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
						previousUtilizationOfCpu,
						utilizationOfCpu,
						timeDiff);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				Log.printLine();
				Log.formatLine(
						"%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
						currentTime,
						host.getId(),
						getLastProcessTime(),
						previousUtilizationOfCpu * 100,
						utilizationOfCpu * 100);
				Log.formatLine(
						"%.2f: [Host #%d] energy is %.2f W*sec",
						currentTime,
						host.getId(),
						timeFrameHostEnergy);
				

				data.append(String.format("%.2f", currentTime) + delimeter);
				data.append(String.format("%d", host.getId()) + delimeter);
				data.append(String.format("%.2f", getLastProcessTime()) + delimeter);
				data.append(String.format("%.5f", utilizationOfCpu) + delimeter);
				data.append(String.format("%.5f", previousUtilizationOfCpu) + delimeter);
				data.append(String.format("%.5f", timeFrameHostEnergy));
				data.append("\n");
				writeDataRow(data.toString(), HostUtilHeader, Constants.HostUtilInfoPath);
				
				/*double myPowerFrameHost = PowerModelSpecPowerLenovoXeon1220.PowerEstimation(host) * timeDiff;
				double myPowerDatacenter = getMyPower() + myPowerFrameHost;
				setMyPower(myPowerDatacenter);
				
				System.out.println("D " + myPowerDatacenter);*/
			}
			
			Log.formatLine(
					"\n%.2f: Data center's energy is %.2f W*sec\n",
					currentTime,
					timeFrameDatacenterEnergy);
		}


		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}

		Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}
	
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

	/*
	 * (non-Javadoc)
	 * @see org.cloudbus.cloudsim.Datacenter#processVmMigrate(org.cloudbus.cloudsim.core.SimEvent,
	 * boolean)
	 */
	@Override
	protected void processVmMigrate(SimEvent ev, boolean ack) {
		updateCloudetProcessingWithoutSchedulingFutureEvents();
		super.processVmMigrate(ev, ack);
		SimEvent event = CloudSim.findFirstDeferred(getId(), new PredicateType(CloudSimTags.VM_MIGRATE));
		if (event == null || event.eventTime() > CloudSim.clock()) {
			updateCloudetProcessingWithoutSchedulingFutureEventsForce();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.Datacenter#processCloudletSubmit(cloudsim.core.SimEvent, boolean)
	 */
	@Override
	protected void processCloudletSubmit(SimEvent ev, boolean ack) {
		super.processCloudletSubmit(ev, ack);
		setCloudletSubmitted(CloudSim.clock());
	}

	/**
	 * Gets the power.
	 * 
	 * @return the power
	 */
	public double getPower() {
		return power;
	}

	/**
	 * Sets the power.
	 * 
	 * @param power the new power
	 */
	protected void setPower(double power) {
		this.power = power;
	}
	
	public double getMyPower() {
		return MyPower;
	}
	
	protected void setMyPower(double MyPower) {
		this.MyPower = MyPower;
	}
	
	public double getNextPower() {
		return nextPower;
	}
	
	protected void setNextPower(double nextPower) {
		this.nextPower = nextPower;
	}

	/**
	 * Checks if PowerDatacenter is in migration.
	 * 
	 * @return true, if PowerDatacenter is in migration
	 */
	protected boolean isInMigration() {
		boolean result = false;
		for (Vm vm : getVmList()) {
			if (vm.isInMigration()) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Checks if is disable migrations.
	 * 
	 * @return true, if is disable migrations
	 */
	public boolean isDisableMigrations() {
		return disableMigrations;
	}

	/**
	 * Sets the disable migrations.
	 * 
	 * @param disableMigrations the new disable migrations
	 */
	public void setDisableMigrations(boolean disableMigrations) {
		this.disableMigrations = disableMigrations;
	}

	/**
	 * Checks if is cloudlet submited.
	 * 
	 * @return true, if is cloudlet submited
	 */
	protected double getCloudletSubmitted() {
		return cloudletSubmitted;
	}

	/**
	 * Sets the cloudlet submited.
	 * 
	 * @param cloudletSubmitted the new cloudlet submited
	 */
	protected void setCloudletSubmitted(double cloudletSubmitted) {
		this.cloudletSubmitted = cloudletSubmitted;
	}

	/**
	 * Gets the migration count.
	 * 
	 * @return the migration count
	 */
	public int getMigrationCount() {
		return migrationCount;
	}

	/**
	 * Sets the migration count.
	 * 
	 * @param migrationCount the new migration count
	 */
	protected void setMigrationCount(int migrationCount) {
		this.migrationCount = migrationCount;
	}

	/**
	 * Increment migration count.
	 */
	protected void incrementMigrationCount() {
		setMigrationCount(getMigrationCount() + 1);
	}
	
	public double getCost() {
		return cost;
	}
	
	protected void setCost(double cost) {
		this.cost = cost;
	}
	
	public double getEnergyCost() {
		return energyCost;
	}
	
	protected void setEnergyCost(double energyCost) {
		this.energyCost = energyCost;
	}
	
	public double getPenaltyCost() {
		return penaltyCost;
	}
	
	protected void setPenaltyCost(double penaltyCost) {
		this.penaltyCost = penaltyCost;
	}
	
	public boolean getMyPolicies() {
		return myPolicies;
	}
	
	public void setOverload (int overload) {
		this.overload = overload;
	}
	
	public int getOverload () {
		return overload;
	}
	
	public void setMigrations (int migrations) {
		this.migrations = migrations;
	}
	
	public int getMigrations () {
		return migrations;
	}
	
	public void setOverloadPoint (int overloadPoint) {
		this.overloadPoint = overloadPoint;
	}
	
	public int getOverloadPoint () {
		return overloadPoint;
	}
	
	public void setCrashes (int crashes) {
		this.crashes = crashes;
	}
	
	public int getCrashes () {
		return crashes;
	}
		
	public void setOverloadCost (double overloadCost) {
		this.overloadCost = overloadCost;
	}
	
	public double getOverloadCost () {
		return overloadCost;
	}
	
	public void setMigrationsCost (double migrationsCost) {
		this.migrationsCost = migrationsCost;
	}
	
	public double getMigrationsCost () {
		return migrationsCost;
	}
	
	public void setCrashesCost (double crashesCost) {
		this.crashesCost = crashesCost;
	}
	
	public double getCrashesCost () {
		return crashesCost;
	}
	
	/**
	 * Sets the disable migrations.
	 * 
	 * @param disableMigrations the new disable migrations
	 */
	public void setMyPolicies(boolean myPolicies) {
		this.myPolicies = myPolicies;
	}
	
	public void setNodes (int nodes) {
		this.nodes = nodes;
	}
	
	public int getNodes () {
		return nodes;
	}
	
	public double loads () {
		return util;
	}
	
	public void findload (double util) {
		this.util = util;
	}

	public void setExtended (int times) {
		this.times = times;	
	}
	
	public int getExtended () {
		return times;
	}
	
	public void set_points (int i) {
		points[i]++;
	}
	
	public int getPoints(int i) {
		int a = points[i];
				
		return a;
	}
}
