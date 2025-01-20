/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.Random;
import java.util.Set;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.lists.PowerVmList;
import org.cloudbus.cloudsim.util.ExecutionTimeMeasurer;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.cost.model.CostEstimation;
import org.cloudbus.cloudsim.power.PowerHost;

/**
 * The class of an abstract power-aware VM allocation policy that dynamically optimizes the VM
 * allocation using migration.
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
public abstract class PowerVmAllocationPolicyMigrationAbstract extends PowerVmAllocationPolicyAbstract {

	/** The vm selection policy. */
	private PowerVmSelectionPolicy vmSelectionPolicy;

	/** The saved allocation. */
	private final List<Map<String, Object>> savedAllocation = new ArrayList<Map<String, Object>>();

	/** The utilization history. */
	private final Map<Integer, List<Double>> utilizationHistory = new HashMap<Integer, List<Double>>();

	/** The metric history. */
	private final Map<Integer, List<Double>> metricHistory = new HashMap<Integer, List<Double>>();

	/** The time history. */
	private final Map<Integer, List<Double>> timeHistory = new HashMap<Integer, List<Double>>();

	/** The execution time history vm selection. */
	private final List<Double> executionTimeHistoryVmSelection = new LinkedList<Double>();

	/** The execution time history host selection. */
	private final List<Double> executionTimeHistoryHostSelection = new LinkedList<Double>();

	/** The execution time history vm reallocation. */
	private final List<Double> executionTimeHistoryVmReallocation = new LinkedList<Double>();

	/** The execution time history total. */
	private final List<Double> executionTimeHistoryTotal = new LinkedList<Double>();

	/**
	 * Instantiates a new power vm allocation policy migration abstract.
	 * 
	 * @param hostList the host list
	 * @param vmSelectionPolicy the vm selection policy
	 */
	
	public PowerVmAllocationPolicyMigrationAbstract(
			List<? extends Host> hostList,
			PowerVmSelectionPolicy vmSelectionPolicy) {
		super(hostList);
		setVmSelectionPolicy(vmSelectionPolicy);
	}

	public void setVmHistory () {
		for (PowerHost host : this.<PowerHost> getHostList()) {
			PowerHostUtilizationHistory _host = (PowerHostUtilizationHistory) host;
			
			for(Vm vm : host.getVmList()) {
				double predictedUtilization = 0;
				double[] utilizationHistory = _host.getVmUtilizationHistory(host, (PowerVm)vm);
				int length = 10;
				
				if (utilizationHistory.length >= length) {
					for (int i=0; i<length; i++) {
						
						if (i < 5)
							predictedUtilization += 0.15 * utilizationHistory[i];
						else
							predictedUtilization += 0.05 * utilizationHistory[i];
					}
					
					//predictedUtilization = predictedUtilization / length;
					vm.setHistory(predictedUtilization);
					
//					System.out.println("sethistory " + predictedUtilization);
					
				}
				else
					vm.setHistory(vm.get_vm_normalized_req_previous());
			}
		}
	}
	
	/**
	 * Optimize allocation of the VMs according to current utilization.
	 * 
	 * @param vmList the vm list
	 * 
	 * @return the array list< hash map< string, object>>
	 */
	
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		
		ExecutionTimeMeasurer.start("optimizeAllocationTotal");

		ExecutionTimeMeasurer.start("optimizeAllocationHostSelection");
		List<PowerHostUtilizationHistory> overUtilizedHosts = getOverUtilizedHosts();
		getExecutionTimeHistoryHostSelection().add(
				ExecutionTimeMeasurer.end("optimizeAllocationHostSelection"));

		printOverUtilizedHosts(overUtilizedHosts);
		
		saveAllocation();

		ExecutionTimeMeasurer.start("optimizeAllocationVmSelection");
		List<? extends Vm> vmsToMigrate = getVmsToMigrateFromHosts(overUtilizedHosts);
		
		getExecutionTimeHistoryVmSelection().add(ExecutionTimeMeasurer.end("optimizeAllocationVmSelection"));

		// Log.printLine("Reallocation of VMs from the over-utilized hosts:");
		// ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");
		// List<Map<String, Object>> migrationMap = getNewVmPlacement(vmsToMigrate, new HashSet<Host>(
		// 		overUtilizedHosts));
		// getExecutionTimeHistoryVmReallocation().add(
		// 		ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));
		// Log.printLine();

		// migrationMap.addAll(getMigrationMapFromUnderUtilizedHosts(overUtilizedHosts));

		System.out.println("Start calculating magrationMap for VMs and the OVER-utilized hosts:");
		ExecutionTimeMeasurer.start("optimizeAllocationVmReallocation");
		List<Map<String, Object>> migrationMap = getNewVmPlacement(vmsToMigrate, new HashSet<Host>(
				overUtilizedHosts));
		if (migrationMap.isEmpty()) {System.out.println("No VMs are attached with such Hosts!");}
		getExecutionTimeHistoryVmReallocation().add(
				ExecutionTimeMeasurer.end("optimizeAllocationVmReallocation"));
//		System.out.println();

		System.out.println("Start calculating magrationMap for VMs and the UNDER-utilized hosts:");
		migrationMap.addAll(getMigrationMapFromUnderUtilizedHosts(overUtilizedHosts));

		restoreAllocation();

		getExecutionTimeHistoryTotal().add(ExecutionTimeMeasurer.end("optimizeAllocationTotal"));

		return migrationMap;
	}

	public List<Map<String, Object>> optimizeAllocationPower(List<? extends Vm> vmList) {
		
			// Do the energy-aware migration
		
			List<PowerHostUtilizationHistory> brownPoweredHosts = getBrownPoweredHosts();
			
			printBrownPoweredHosts(brownPoweredHosts);
			
			saveAllocation();
			
			List<? extends Vm> vmsToMigrate = getVmsToMigrateFromHostsPower(brownPoweredHosts);
			
			System.out.println(CloudSim.clock() + ": Detected Datacenter #" + brownPoweredHosts.get(0).getDatacenter().getId() + " is mainly powered by brown energy...");
			System.out.println("Start calculating magrationMap for VMs and the brown energy-powered hosts:");
			
			List<Map<String, Object>> migrationMap = getNewVmPlacementPower(vmsToMigrate, new HashSet<Host>(
					brownPoweredHosts));
			
			if (migrationMap.isEmpty()) {System.out.println("No VMs are attached with such Hosts!");}
			
//			System.out.println();
			
//			migrationMap.addAll(getMigrationMapFromUnderUtilizedHosts(brownPoweredHosts));
			restoreAllocation();
			
			return migrationMap;
			
	}
	
	// =========Start - For random data generation=========
		/**
		 * Gets the over utilized hosts.
		 * 
		 * @return the over utilized hosts
		 */
	public List<Map<String, Object>> optimizeAllocationRandom(List<? extends Vm> vmList) {
		
		// Do the energy-aware migration
	
		List<PowerHostUtilizationHistory> randomHosts = getRandomHosts();
		
		printRandomHosts(randomHosts);
		
		saveAllocation();
		
		List<? extends Vm> vmsToMigrate = getVmsToMigrateFromHostsRandom(randomHosts);
		
		System.out.println("Start calculating magrationMap for random-selected VMs and hosts:");
		
		List<Map<String, Object>> migrationMap = getNewVmPlacementPower(vmsToMigrate, new HashSet<Host>(
				randomHosts));
		
		if (migrationMap.isEmpty()) {System.out.println("No VMs are attached with such Hosts!");}
		
//		System.out.println("Start calculating magrationMap for VMs and the UNDER-utilized hosts:");
//		migrationMap.addAll(getMigrationMapFromUnderUtilizedHosts(randomHosts));
		
		restoreAllocation();
		
		return migrationMap;
		
	}
	
	
	protected List<PowerHostUtilizationHistory> getRandomHosts() {
		List<PowerHostUtilizationHistory> allHosts = new LinkedList<PowerHostUtilizationHistory>();
		List<PowerHostUtilizationHistory> overUtilizedHosts = new LinkedList<PowerHostUtilizationHistory>();

		allHosts = this.<PowerHostUtilizationHistory> getHostList();
		
		// Shuffle the list of hosts
	    Collections.shuffle(allHosts);
	    
	    Random random = new Random();
	    int count = random.nextInt(allHosts.size());
	    
	    overUtilizedHosts = allHosts.subList(0, count);
	    
		return overUtilizedHosts;
	}
	
	/**
	 * Prints the over utilized hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 */
	protected void printRandomHosts(List<PowerHostUtilizationHistory> overUtilizedHosts) {
		
		if (overUtilizedHosts.isEmpty()) {
			System.out.println("Ha! No random-picked host since random number is 0!");
		} else {System.out.println("Random-picked hosts: ");}
		for (PowerHostUtilizationHistory host : overUtilizedHosts) {
			System.out.println("Host #" + host.getId() + " in Datacenter #" + host.getDatacenter().getId());
		}
//		System.out.println();
	}
	
	/**
	 * Gets the vms to migrate from hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 * @return the vms to migrate from hosts
	 */
	protected
			List<? extends Vm>
			getVmsToMigrateFromHostsRandom(List<PowerHostUtilizationHistory> overUtilizedHosts) {
		List<Vm> vmsToMigrate = new LinkedList<Vm>();
		System.out.print("Random-picked vms:");
		for (PowerHostUtilizationHistory host : overUtilizedHosts) {
			while (true) {
				Vm vm = getVmSelectionPolicy().getVmToMigrate(host);
				if (vm == null) {
					break;
				}
				System.out.print(" VM #" + vm.getId());
				vmsToMigrate.add(vm);
				host.vmDestroy(vm);
				if (!isHostOverUtilized(host)) {
					System.out.print('\n');
					break;
				}
			}
		}
		
		return vmsToMigrate;
	}
	
	// =========End - For random data generation=========
	
	
	protected List<Map<String, Object>> getNewVmPlacementPower(
			List<? extends Vm> vmsToMigrate,
			Set<? extends Host> excludedHosts) {
		PowerHost allocatedHost = null;
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		PowerVmList.sortByCpuUtilization(vmsToMigrate);
		for (Vm vm : vmsToMigrate) {
			if (Constants.DVFS == 1)
				allocatedHost = frequencyAwarePlacement(vm, excludedHosts);
			else
				allocatedHost = findHostForVm(vm, excludedHosts);
			if (allocatedHost != null) {
				allocatedHost.vmCreate(vm);
				
				if (Constants.MY_POLICIES == 1) {
				}
				else {
				}
				Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());

				Map<String, Object> migrate = new HashMap<String, Object>();
				migrate.put("vm", vm);
				migrate.put("host", allocatedHost);
				migrationMap.add(migrate);
			}
		}
		return migrationMap;
	}
	
	protected
			List<? extends Vm>
			getVmsToMigrateFromHostsPower(List<PowerHostUtilizationHistory> brownPoweredHosts) {
		List<Vm> vmsToMigrate = new LinkedList<Vm>();
		for (PowerHostUtilizationHistory host : brownPoweredHosts) {
			while (true) {
				Vm vm = getVmSelectionPolicy().getVmToMigrate(host);
				if (vm == null) {
					break;
				}
				vmsToMigrate.add(vm);
				host.vmDestroy(vm);
				if (!isBrownPowered(host)) {
					break;
				}
			}
		}
		
		return vmsToMigrate;
		}
	
	/*
	 * Modified to get the host influenced by energy status
	 */
	protected List<PowerHostUtilizationHistory> getBrownPoweredHosts() {
		List<PowerHostUtilizationHistory> grownPoweredHosts = new LinkedList<PowerHostUtilizationHistory>();
		for (PowerHostUtilizationHistory host : this.<PowerHostUtilizationHistory> getHostList()) {
			if (isBrownPowered(host)) {
				grownPoweredHosts.add(host);
			}
		}
		return grownPoweredHosts;
	}
	
	protected boolean isBrownPowered(PowerHost host) {
		// Assume datacenter 3 is brown-powered
		if (host.getDatacenter().getId() == 3) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the migration map from under utilized hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 * @return the migration map from under utilized hosts
	 */
	protected List<Map<String, Object>> getMigrationMapFromUnderUtilizedHosts(
			List<PowerHostUtilizationHistory> overUtilizedHosts) {
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		List<PowerHost> switchedOffHosts = getSwitchedOffHosts();

		// over-utilized hosts + hosts that are selected to migrate VMs to from over-utilized hosts
		Set<PowerHost> excludedHostsForFindingUnderUtilizedHost = new HashSet<PowerHost>();
		excludedHostsForFindingUnderUtilizedHost.addAll(overUtilizedHosts);
		excludedHostsForFindingUnderUtilizedHost.addAll(switchedOffHosts);
		excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(migrationMap));

		// over-utilized + under-utilized hosts
		Set<PowerHost> excludedHostsForFindingNewVmPlacement = new HashSet<PowerHost>();
		excludedHostsForFindingNewVmPlacement.addAll(overUtilizedHosts);
		excludedHostsForFindingNewVmPlacement.addAll(switchedOffHosts);

		int numberOfHosts = getHostList().size();

		while (true) {
			if (numberOfHosts == excludedHostsForFindingUnderUtilizedHost.size()) {
				break;
			}

			PowerHost underUtilizedHost = getUnderUtilizedHost(excludedHostsForFindingUnderUtilizedHost);
			if (underUtilizedHost == null) {
				break;
			}

			// Log.printLine("Under-utilized host: host #" + underUtilizedHost.getId() + "\n");
			System.out.println("Under-utilized host: host #" + underUtilizedHost.getId());


			excludedHostsForFindingUnderUtilizedHost.add(underUtilizedHost);
			excludedHostsForFindingNewVmPlacement.add(underUtilizedHost);

			List<? extends Vm> vmsToMigrateFromUnderUtilizedHost = getVmsToMigrateFromUnderUtilizedHost(underUtilizedHost);
			if (vmsToMigrateFromUnderUtilizedHost.isEmpty()) {
				continue;
			}

			// Log.print("Reallocation of VMs from the under-utilized host: ");
			// if (!Log.isDisabled()) {
			// 	for (Vm vm : vmsToMigrateFromUnderUtilizedHost) {
			// 		Log.print(vm.getId() + " ");
			// 	}
			// }
			// Log.printLine();
			System.out.print("Try to do the reallocation of VMs from the under-utilized host: ");
			if (true) {
				for (Vm vm : vmsToMigrateFromUnderUtilizedHost) {
					System.out.print("VM #" + vm.getId() + " ");
				}
			}
			System.out.println();

			List<Map<String, Object>> newVmPlacement = getNewVmPlacementFromUnderUtilizedHost(
					vmsToMigrateFromUnderUtilizedHost,
					excludedHostsForFindingNewVmPlacement);

			excludedHostsForFindingUnderUtilizedHost.addAll(extractHostListFromMigrationMap(newVmPlacement));

			migrationMap.addAll(newVmPlacement);

			// Log.printLine();
			System.out.println();
		}

		return migrationMap;
	}

	/**
	 * Prints the over utilized hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 */
	protected void printOverUtilizedHosts(List<PowerHostUtilizationHistory> overUtilizedHosts) {
		// if (!Log.isDisabled()) {
		// 	Log.printLine("Over-utilized hosts:");
		// 	for (PowerHostUtilizationHistory host : overUtilizedHosts) {
		// 		Log.printLine("Host #" + host.getId());
		// 	}
		// 	Log.printLine();
		// }
		
		if (overUtilizedHosts.isEmpty()) {
			System.out.println("No candidate over-utilized host!");
		} else {System.out.println("Over-utilized hosts: ");}
		for (PowerHostUtilizationHistory host : overUtilizedHosts) {
			System.out.println("Host #" + host.getId() + " in Datacenter #" + host.getDatacenter().getId());
		}
	}
	
	protected void printBrownPoweredHosts(List<PowerHostUtilizationHistory> brownPoweredHosts) {
		
		if (brownPoweredHosts.isEmpty()) {
			System.out.println("No candidate brown enenery-powered host!");
		} else {System.out.println("The hosts mainly Powered by Brown Energy: ");}
		for (PowerHostUtilizationHistory host : brownPoweredHosts) {
			System.out.println("Host #" + host.getId() + " in Datacenter #" + host.getDatacenter().getId());
		}
	}

	/**
	 * Find host for vm.
	 * 
	 * @param vm the vm
	 * @param excludedHosts the excluded hosts
	 * @return the power host
	 */
	public PowerHost findHostForVm(Vm vm, Set<? extends Host> excludedHosts) {
		PowerHost allocatedHost = null;
		double minCost = Double.MAX_VALUE;
		double diffpriority = Double.MAX_VALUE;
		double diff;
		double afterAllocation;
		int priority = 0;
		int vm_priority = 0;
		double rating = 0;

		for (PowerHost host : this.<PowerHost> getHostList()) { 
			if (excludedHosts.contains(host)) {
				continue;
			}

			if (host.isSuitableForVm(vm)) {
				if ((getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) ) {
					continue;
				}
				
				try {
					if (Constants.MY_POLICIES == 1) {
						afterAllocation = getUtilizationAfterAllocation(host, vm);
						priority = (int)host.getNodePriority();
					}
					else
						afterAllocation = getPowerAfterAllocation(host, vm);

					
					if (afterAllocation != -1) {
						if (Constants.MY_POLICIES == 1)
							diff = Constants.THRESHOLD - afterAllocation;
						else
							diff = afterAllocation - host.getPowerEstimation(host.getMyUtilization(), 1.146, 3.3, 0);

						rating = vm.getRate() * vm.getPrice();
						
						if (rating > 1.67e-05)
							vm_priority = 2;
						else
							vm_priority = 1;

						if (diff < minCost && afterAllocation < Constants.THRESHOLD) {
							minCost = diff;
							allocatedHost = host;
							diffpriority = Math.abs(priority - (double)vm.getRate());
						}
					}
				} catch (Exception e) {}
			}
		}
		
		if (allocatedHost != null) {
			allocatedHost.setNodeClass(vm.get_class());
			allocatedHost.setLoad(allocatedHost.getTempLoad());
			allocatedHost.setNodePriority(vm_priority);
		}
		
		return allocatedHost;
	}

	/**
	 * Checks if is host over utilized after allocation.
	 * 
	 * @param host the host
	 * @param vm the vm
	 * @return true, if is host over utilized after allocation
	 */
	protected boolean isHostOverUtilizedAfterAllocation(PowerHost host, Vm vm) {
		boolean isHostOverUtilizedAfterAllocation = true;
		if (host.vmCreate(vm)) {
			isHostOverUtilizedAfterAllocation = isHostOverUtilized(host);
			if(isHostOverUtilizedAfterAllocation) {
				System.out.println(CloudSim.clock() + ": Host #" + host.getId() + " has an overutilization if allocate VM #" + vm.getId() + " based on the defined threshold");
			}
			host.vmDestroy(vm);
		}
		return isHostOverUtilizedAfterAllocation;
	}

	/**
	 * Find host for vm.
	 * 
	 * @param vm the vm
	 * @return the power host
	 */
	@Override
	public PowerHost findHostForVm(Vm vm) {
		Set<Host> excludedHosts = new HashSet<Host>();
		if (vm.getHost() != null) {
			excludedHosts.add(vm.getHost());
		}
		
		if (Constants.DVFS == 1)
			return frequencyAwarePlacement(vm, excludedHosts);
		else
			return findHostForVm(vm, excludedHosts);
	}

	/**
	 * Extract host list from migration map.
	 * 
	 * @param migrationMap the migration map
	 * @return the list
	 */
	protected List<PowerHost> extractHostListFromMigrationMap(List<Map<String, Object>> migrationMap) {
		List<PowerHost> hosts = new LinkedList<PowerHost>();
		for (Map<String, Object> map : migrationMap) {
			hosts.add((PowerHost) map.get("host"));
		}
		return hosts;
	}
	
	protected PowerHost frequencyAwarePlacement (
			Vm vm,
			Set<? extends Host> excludedHosts) {
		double bestPlacement = -10000000;
		PowerHost bestHost = null;
		int bestOperatingPoint = Constants.POINTS - 1;
		double finalUtilization = 0;
		double finalLoad = 0;                                                                               
	
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			
			if (host.isSuitableForVm(vm)) {
				if (getUtilizationOfCpuMips(host) != 0 && isHostOverUtilizedAfterAllocation(host, vm)) {
					continue;
				}
				
				perfConfNode( host, vm );
				double utilization = host.getTempUtilization();
				double frequencyIncrement = host.getFrequency(host.getTempOperatingPoint()) - host.getFrequency(host.getOperatingPoint());
				double placement = utilization - frequencyIncrement;
				
				if (placement > bestPlacement) {
					bestHost = host;
					bestPlacement = placement;
					bestOperatingPoint = host.getTempOperatingPoint();
					finalUtilization = host.getTempUtilization();
					finalLoad = host.getTempLoad();
				}
			}
		}
		
		if (bestHost != null) {
			bestHost.setOperatingPoint(bestOperatingPoint);
			bestHost.setMyUtilization(finalUtilization);
			bestHost.setLoad(finalLoad);
		}
		
		//System.out.println(bestHost.getId() + " " + vm.getId());
		return bestHost;
	}
	
	public double perfConfNode ( PowerHost host, Vm vm1 ) {
		double utilization = 0;
		double frequency = host.getFrequency(Constants.POINTS-1);
		
		host.setTempOperatingPoint(Constants.POINTS-1);
		double load = CostEstimation.getPreviousLoadHost(host, Constants.POINTS-1);
		double vm_current = CostEstimation.getRequestedVmPrevious(host, vm1, Constants.POINTS-1);
	
		load += vm_current;
	
		utilization = CostEstimation.getUtilizationHost(load);
		host.setTempUtilization(utilization);
		host.setTempLoad(0);
		
		for (int i=0; i<Constants.POINTS; i++) {
		
			load = CostEstimation.getPreviousLoadHost(host, i);
			vm_current = CostEstimation.getRequestedVmPrevious(host, vm1, i);
		
			load += vm_current;
		
			utilization = CostEstimation.getUtilizationHost(load);
			if (Constants.THRESHOLD > utilization) {
				frequency = host.getFrequency(i);
				host.setTempOperatingPoint(i);
				host.setTempUtilization(utilization);
				host.setTempLoad(load);
				break;
			}
		}
		
		return frequency;
	}
	
	/**
	 * Gets the new vm placement.
	 * 
	 * @param vmsToMigrate the vms to migrate
	 * @param excludedHosts the excluded hosts
	 * @return the new vm placement
	 */
	protected List<Map<String, Object>> getNewVmPlacement(
			List<? extends Vm> vmsToMigrate,
			Set<? extends Host> excludedHosts) {
		PowerHost allocatedHost = null;
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		PowerVmList.sortByCpuUtilization(vmsToMigrate);
		for (Vm vm : vmsToMigrate) {
			if (Constants.DVFS == 1)
				allocatedHost = frequencyAwarePlacement(vm, excludedHosts);
			else
				allocatedHost = findHostForVm(vm, excludedHosts);
			if (allocatedHost != null) {
				allocatedHost.vmCreate(vm);
				
				Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());

				Map<String, Object> migrate = new HashMap<String, Object>();
				migrate.put("vm", vm);
				migrate.put("host", allocatedHost);
				migrationMap.add(migrate);
			}
		}
		return migrationMap;
	}

	/**
	 * Gets the new vm placement from under utilized host.
	 * 
	 * @param vmsToMigrate the vms to migrate
	 * @param excludedHosts the excluded hosts
	 * @return the new vm placement from under utilized host
	 */
	protected List<Map<String, Object>> getNewVmPlacementFromUnderUtilizedHost(
			List<? extends Vm> vmsToMigrate,
			Set<? extends Host> excludedHosts) {
		List<Map<String, Object>> migrationMap = new LinkedList<Map<String, Object>>();
		PowerHost allocatedHost = null;
		PowerVmList.sortByCpuUtilization(vmsToMigrate);
		for (Vm vm : vmsToMigrate) {
			if (Constants.DVFS == 1)
				allocatedHost = frequencyAwarePlacement(vm, excludedHosts);
			else
				allocatedHost = findHostForVm(vm, excludedHosts);
			if (allocatedHost != null) {
				allocatedHost.vmCreate(vm);

				// Log.printLine("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());
				System.out.print("VM #" + vm.getId() + " allocated to host #" + allocatedHost.getId());


				Map<String, Object> migrate = new HashMap<String, Object>();
				migrate.put("vm", vm);
				migrate.put("host", allocatedHost);
				migrationMap.add(migrate);
			} else {
				// Log.printLine("Not all VMs can be reallocated from the host, reallocation cancelled");
				System.out.print("Not all VMs can be reallocated from the host, reallocation cancelled");

				for (Map<String, Object> map : migrationMap) {
					((Host) map.get("host")).vmDestroy((Vm) map.get("vm"));
				}
				migrationMap.clear();
				break;
			}
		}
		return migrationMap;
	}

	/**
	 * Gets the vms to migrate from hosts.
	 * 
	 * @param overUtilizedHosts the over utilized hosts
	 * @return the vms to migrate from hosts
	 */
	protected
			List<? extends Vm>
			getVmsToMigrateFromHosts(List<PowerHostUtilizationHistory> overUtilizedHosts) {
		List<Vm> vmsToMigrate = new LinkedList<Vm>();
		for (PowerHostUtilizationHistory host : overUtilizedHosts) {
			while (true) {
				Vm vm = getVmSelectionPolicy().getVmToMigrate(host);
				if (vm == null) {
					break;
				}
				vmsToMigrate.add(vm);
				host.vmDestroy(vm);
				if (!isHostOverUtilized(host)) {
					break;
				}
			}
		}
		
		return vmsToMigrate;
	}

	/**
	 * Gets the vms to migrate from under utilized host.
	 * 
	 * @param host the host
	 * @return the vms to migrate from under utilized host
	 */
	protected List<? extends Vm> getVmsToMigrateFromUnderUtilizedHost(PowerHost host) {
		List<Vm> vmsToMigrate = new LinkedList<Vm>();
		for (Vm vm : host.getVmList()) {
			if (!vm.isInMigration()) {
				vmsToMigrate.add(vm);
			}
		}
		return vmsToMigrate;
	}

	/**
	 * Gets the over utilized hosts.
	 * 
	 * @return the over utilized hosts
	 */
	protected List<PowerHostUtilizationHistory> getOverUtilizedHosts() {
		List<PowerHostUtilizationHistory> overUtilizedHosts = new LinkedList<PowerHostUtilizationHistory>();
		for (PowerHostUtilizationHistory host : this.<PowerHostUtilizationHistory> getHostList()) {
			if (isHostOverUtilized(host)) {
				overUtilizedHosts.add(host);
			}
			
				if (isHostOverUtilized(host)) {
					overUtilizedHosts.add(host);
				}
			}
		return overUtilizedHosts;
	}

	/**
	 * Gets the switched off host.
	 * 
	 * @return the switched off host
	 */
	protected List<PowerHost> getSwitchedOffHosts() {
		List<PowerHost> switchedOffHosts = new LinkedList<PowerHost>();
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (host.getUtilizationOfCpu() == 0) {
				switchedOffHosts.add(host);
			}
		}
		return switchedOffHosts;
	}

	/**
	 * Gets the under utilized host.
	 * 
	 * @param excludedHosts the excluded hosts
	 * @return the under utilized host
	 */
	protected PowerHost getUnderUtilizedHost(Set<? extends Host> excludedHosts) {
		double minUtilization = 1;
		PowerHost underUtilizedHost = null;
		for (PowerHost host : this.<PowerHost> getHostList()) {
			if (excludedHosts.contains(host)) {
				continue;
			}
			double utilization = host.getUtilizationOfCpu();
			if (utilization > 0 && utilization < minUtilization
					&& !areAllVmsMigratingOutOrAnyVmMigratingIn(host)) {
				minUtilization = utilization;
				underUtilizedHost = host;
			}
		}
		return underUtilizedHost;
	}

	/**
	 * Checks whether all vms are in migration.
	 * 
	 * @param host the host
	 * @return true, if successful
	 */
	protected boolean areAllVmsMigratingOutOrAnyVmMigratingIn(PowerHost host) {
		for (PowerVm vm : host.<PowerVm> getVmList()) {
			if (!vm.isInMigration()) {
				return false;
			}
			if (host.getVmsMigratingIn().contains(vm)) {
				return true;
			}
		}
		return true;
	}

	/**
	 * Checks if is host over utilized.
	 * 
	 * @param host the host
	 * @return true, if is host over utilized
	 */
	protected abstract boolean isHostOverUtilized(PowerHost host);

	/**
	 * Adds the history value.
	 * 
	 * @param host the host
	 * @param metric the metric
	 */
	protected void addHistoryEntry(HostDynamicWorkload host, double metric) {
		int hostId = host.getId();
		if (!getTimeHistory().containsKey(hostId)) {
			getTimeHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getUtilizationHistory().containsKey(hostId)) {
			getUtilizationHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getMetricHistory().containsKey(hostId)) {
			getMetricHistory().put(hostId, new LinkedList<Double>());
		}
		if (!getTimeHistory().get(hostId).contains(CloudSim.clock())) {
			getTimeHistory().get(hostId).add(CloudSim.clock());
			getUtilizationHistory().get(hostId).add(host.getUtilizationOfCpu());
			getMetricHistory().get(hostId).add(metric);
		}
	}

	/**
	 * Save allocation.
	 */
	protected void saveAllocation() {
		getSavedAllocation().clear();
		for (Host host : getHostList()) {
			for (Vm vm : host.getVmList()) {
				if (host.getVmsMigratingIn().contains(vm)) {
					continue;
				}
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("host", host);
				map.put("vm", vm);
				getSavedAllocation().add(map);
			}
		}
	}

	/**
	 * Restore allocation.
	 */
	protected void restoreAllocation() {
		for (Host host : getHostList()) {
			host.vmDestroyAll();
			host.reallocateMigratingInVms();
		}
		for (Map<String, Object> map : getSavedAllocation()) {
			Vm vm = (Vm) map.get("vm");
			PowerHost host = (PowerHost) map.get("host");
			if (!host.vmCreate(vm)) {
				// Log.printLine("Couldn't restore VM #" + vm.getId() + " on host #" + host.getId());
				System.out.println("Couldn't restore VM #" + vm.getId() + " on host #" + host.getId());
				System.exit(0);
			}
			getVmTable().put(vm.getUid(), host);
		}
	}

	/**
	 * Gets the power after allocation.
	 * 
	 * @param host the host
	 * @param vm the vm
	 * 
	 * @return the power after allocation
	 */
	protected double getPowerAfterAllocation(PowerHost host, Vm vm) {
		double power = 0;
		try {
			//power = host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
			power = host.getPowerEstimation(getMaxUtilizationAfterAllocation(host, vm), 1.146, 3.3, 0);
			host.setTempUtilization(getMaxUtilizationAfterAllocation(host, vm));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}
	
	/**
	 * Gets the power after allocation.
	 * 
	 * @param host the host
	 * @param vm the vm
	 * 
	 * @return the power after allocation
	 */
	
	protected double getCost (PowerHost host) {
		double allocated;
		double penalty = 0;
		double load = host.getTempLoad();
		double utilization = 0.0;
		
		for (Vm vm : host.getVmList()) {
			double vm_current = vm.getTotalUtilizationOfCpu(CloudSim.clock()) * vm.getMips();
			double change = host.getEstimationOverhead(vm.get_class(), host.getOperatingPoint());
			double mips = host.getTotalMips() * change;
			vm_current = vm_current / mips;
			
			if (load > 1.0) {
				allocated = vm_current / load;
				utilization = 1.0;
			}
			else {
				allocated = vm_current;
				utilization = load;
			}
			
			penalty += vm.getRate() * (vm_current - allocated) * Constants.SCHEDULING_INTERVAL * vm.getPrice();
		}
		
		double voltage = host.getVoltage(host.getOperatingPoint(), host.getConfiguration(), 0);
		double frequency = host.getFrequency(host.getOperatingPoint());
		double power = host.getPowerEstimation(utilization, voltage, frequency, 0);
		
		return power * Constants.SCHEDULING_INTERVAL * Constants.ENERGY_PRICE + penalty;
	}
	
	protected double getUtilizationAfterAllocation(PowerHost host, Vm vm) {
		double vm_current = CostEstimation.getRequestedVmPrevious(host, vm, host.getOperatingPoint());
		double load = CostEstimation.getPreviousLoadHost(host, host.getOperatingPoint()) + vm_current;
		
//		System.out.println("after " + host.getId() + " " + host.getOperatingPoint() + " " + host.getLoad() + " " + vm_current);
		//double utilization = CostEstimation.getUtilizationHost(load);
		host.setTempLoad(load);
		
		return load;
	}
	
	protected double getUtilizationHistoryAfterAllocation(PowerHost host, Vm vm) {
		PowerHostUtilizationHistory _host = (PowerHostUtilizationHistory) host;
		double[] utilizationHistory = _host.getVmNormalizedUtilizationHistory(host, (PowerVm)vm);
		double predictedUtilization = 0;
		int length = 11; // we use 10 to make the regression responsive enough to latest values
		
		if (utilizationHistory.length < length)
			return getUtilizationAfterAllocation(host, vm);
		
//		for (int i=0; i<length-1; i++) {
//		//	System.out.println("aaaaaaaaaaaaaaa " + utilizationHistory[i]);
//			predictedUtilization += utilizationHistory[i+1];
//		}
		
//		predictedUtilization = predictedUtilization / (length - 1);

		predictedUtilization = CostEstimation.getRequestedVmHistory(host, vm, host.getOperatingPoint());
		double load = host.getLoad() + predictedUtilization;
		
		
		host.setTempLoad(load);
		return load;
	}
	
	protected double getCostAfterAllocation(PowerHost host, Vm vm) {
		double cost = 0;
		
		double requestedTotalMips = vm.getCurrentRequestedTotalMips();
		//System.out.println(vm.getCurrentRequestedTotalMips() + " ");
		double hostUtilizationMips = getUtilizationOfCpuMips(host);
		double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
		double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips(); host.setTempUtilization(pePotentialUtilization);
	//	System.out.println(vm.getCurrentRequestedTotalMips() + " " + pePotentialUtilization);
		try {
			//cost = host.getCostModel().getCost(host, pePotentialUtilization, Constants.POINTS-1, 0);
			cost = host.getPowerEstimation(pePotentialUtilization, 1.146, 3.3, 0) * Constants.ENERGY_PRICE * Constants.SCHEDULING_INTERVAL;
//			System.out.println(cost);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return cost;
	}

	/**
	 * Gets the power after allocation. We assume that load is balanced between PEs. The only
	 * restriction is: VM's max MIPS < PE's MIPS
	 * 
	 * @param host the host
	 * @param vm the vm
	 * 
	 * @return the power after allocation
	 */
	protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
		double requestedTotalMips = vm.getCurrentRequestedTotalMips();
		double hostUtilizationMips = getUtilizationOfCpuMips(host);
		double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
		double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips();
		return pePotentialUtilization;
	}
	
	
	protected double getMyMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
		double vm_current = CostEstimation.getRequestedVM(host, vm, host.getOperatingPoint());
		double load = CostEstimation.getLoadHost(host, Constants.POINTS-1);
		load += vm_current;
		host.setTempLoad(load);
		
		return load;
	}
	
	/**
	 * Gets the utilization of the CPU in MIPS for the current potentially allocated VMs.
	 *
	 * @param host the host
	 *
	 * @return the utilization of the CPU in MIPS
	 */
	protected double getUtilizationOfCpuMips(PowerHost host) {
		double hostUtilizationMips = 0;
		for (Vm vm2 : host.getVmList()) {
			if (host.getVmsMigratingIn().contains(vm2)) {
				// calculate additional potential CPU usage of a migrating in VM
				hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2) * 0.9 / 0.1;
			}
			hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
		}
		return hostUtilizationMips;
	}

	/**
	 * Gets the saved allocation.
	 * 
	 * @return the saved allocation
	 */
	protected List<Map<String, Object>> getSavedAllocation() {
		return savedAllocation;
	}

	/**
	 * Sets the vm selection policy.
	 * 
	 * @param vmSelectionPolicy the new vm selection policy
	 */
	protected void setVmSelectionPolicy(PowerVmSelectionPolicy vmSelectionPolicy) {
		this.vmSelectionPolicy = vmSelectionPolicy;
	}

	/**
	 * Gets the vm selection policy.
	 * 
	 * @return the vm selection policy
	 */
	protected PowerVmSelectionPolicy getVmSelectionPolicy() {
		return vmSelectionPolicy;
	}

	/**
	 * Gets the utilization history.
	 * 
	 * @return the utilization history
	 */
	public Map<Integer, List<Double>> getUtilizationHistory() {
		return utilizationHistory;
	}

	/**
	 * Gets the metric history.
	 * 
	 * @return the metric history
	 */
	public Map<Integer, List<Double>> getMetricHistory() {
		return metricHistory;
	}

	/**
	 * Gets the time history.
	 * 
	 * @return the time history
	 */
	public Map<Integer, List<Double>> getTimeHistory() {
		return timeHistory;
	}

	/**
	 * Gets the execution time history vm selection.
	 * 
	 * @return the execution time history vm selection
	 */
	public List<Double> getExecutionTimeHistoryVmSelection() {
		return executionTimeHistoryVmSelection;
	}

	/**
	 * Gets the execution time history host selection.
	 * 
	 * @return the execution time history host selection
	 */
	public List<Double> getExecutionTimeHistoryHostSelection() {
		return executionTimeHistoryHostSelection;
	}

	/**
	 * Gets the execution time history vm reallocation.
	 * 
	 * @return the execution time history vm reallocation
	 */
	public List<Double> getExecutionTimeHistoryVmReallocation() {
		return executionTimeHistoryVmReallocation;
	}

	/**
	 * Gets the execution time history total.
	 * 
	 * @return the execution time history total
	 */
	public List<Double> getExecutionTimeHistoryTotal() {
		return executionTimeHistoryTotal;
	}

}
