package org.cloudbus.cloudsim.examples.power;

import org.cloudbus.cloudsim.power.models.PowerModel;
//import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
//import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G5Xeon3075;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerLenovoXeon1220;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerXgene3;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerXgene2;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * If you are using any algorithms, policies or workload included in the power package, please cite
 * the following paper:
 *
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 *
 * @author Anton Beloglazov
 * @since Jan 6, 2012
 */
/*
	Extended by the Computer Systems Lab, University of Thessaly (https://csl.e-ce.uth.gr)
	for the MLSysOps project (https://mlsysops.eu)
	 
	Copyright (c) 2024, The University of Thessaly, Greece
	 
	Contact: Bowen Sun bsun@uth.gr
	         Christos Antonopoulos  cda@uth.gr
 */
public class Constants {

	public final static boolean ENABLE_OUTPUT = true;
	public final static boolean OUTPUT_CSV    = false;
	public final static boolean multiDatacenter = false;
//	public final static boolean max = true;
	public final static boolean max = false;
	
	public static PrintWriter sla_file;
	public static PrintWriter overload_file;
	public static PrintWriter migration_file;
	public static PrintWriter crash_file;
	public static PrintWriter active_nodes_file;
	public static PrintWriter nominal_hosts_file;
	public static PrintWriter vm_map_file;

	public static String HistoricalInfoPath;
	public static String HostUtilInfoPath;
	
	public final static int k = 30;
	public final static double SCHEDULING_INTERVAL = 300;
	public final static double TIME_SLOT = SCHEDULING_INTERVAL/k;
	public final static double SIMULATION_LIMIT = 86000;
	public final static int MAXSIZE = (int)(SIMULATION_LIMIT/SCHEDULING_INTERVAL) + 2; // Max length of energy price file and utilization reading

	public final static int CLOUDLET_LENGTH	= 2500 * (int) SIMULATION_LIMIT;
	public final static int CLOUDLET_PES	= 1;

	public final static String energyFilename = "energy_price";

	/*
	 * VM instance types:
	 *   High-Memory Extra Large Instance: 3.25 EC2 Compute Units, 8.55 GB // too much MIPS
	 *   High-CPU Medium Instance: 2.5 EC2 Compute Units, 0.85 GB
	 *   Extra Large Instance: 2 EC2 Compute Units, 3.75 GB
	 *   Small Instance: 1 EC2 Compute Unit, 1.7 GB
	 *   Micro Instance: 0.5 EC2 Compute Unit, 0.633 GB
	 *   We decrease the memory size two times to enable oversubscription
	 *
	 */
	public final static int CLASSES = 3;
	public final static int VM_TYPES	= 4;
	public final static double[] VM_MIPS	= { 0.75, 0.6, 0.3, 0.15 };
	public final static int[] VM_PES	= { 1, 1, 1, 1 };
	public final static int[] VM_RAM	= { 870,  1740, 1740, 613 };
	public final static int VM_BW		= 1000; // 100 Mbit/s
	public final static int VM_SIZE		= 25; // 2.5 GB
	public final static double[] PERFORMANCE = { 1.0, 1.0, 1.0 };

	/*
	 * Host types:
	 *   HP ProLiant ML110 G4 (1 x [Xeon 3040 1860 MHz, 2 cores], 4GB)
	 *   HP ProLiant ML110 G5 (1 x [Xeon 3075 2660 MHz, 2 cores], 4GB)
	 *   We increase the memory size to enable over-subscription (x4)
	 */
/*	public final static int HOST_TYPES	 = 2;
	public final static int[] HOST_MIPS	 = { 1860, 2660 };
	public final static int[] HOST_PES	 = { 2, 2 };
	public final static int[] HOST_RAM	 = { 4096, 4096 };
	public final static int HOST_BW		 = 1000000; // 1 Gbit/s
	public final static int HOST_STORAGE = 1000000; // 1 GB
*/
	public static PowerModel[] HOST_POWER = {
		new PowerModelSpecPowerLenovoXeon1220(),
		new PowerModelSpecPowerLenovoXeon1220()
	};
	
//	public static PowerModel[] HOST_POWER = {
//		new PowerModelSpecPowerXgene3(),
//		new PowerModelSpecPowerXgene3()
//	};
	
	public final static double CONFIGURATION_AV = 1;
	public final static double CPU = 4.0;

	public static double THRESHOLD = 0.7;

	public final static int HOST_TYPES	 = 1;
	public final static double[] HOST_MIPS	 = { 4.0 };
	public final static double[] HOST_PES	 = { 1 };
	public final static int[] HOST_RAM	 = { 32768 };
	public final static int HOST_BW		 = 1000000; // 1 Gbit/s
	public final static int HOST_STORAGE = 1000000; // 1 GB

//	public final static int HOST_STORAGE = 60;
	public final static int POINTS = 4;
	public final static double[] LIST_MIPS = {2000.0, 2500.0, 3000.0, 3300.0};
	
	public final static int SPECIAL_NODE_TYPES	 = 1;
	public final static int[] SPECIAL_NODE_MIPS	 = { Integer.MAX_VALUE };
	public final static int[] SPECIAL_NODE_PES	 = {4 };
	public final static int[] SPECIAL_NODE_RAM	 = { Integer.MAX_VALUE };
	public final static int SPECIAL_NODE_BW		 = 1000000; // 1 Gbit/s
	public final static int SPECIAL_NODE_STORAGE = 1000000; // 1 GB
	

	public static double ENERGY_PRICE = 4.2e-8;
	public final static double vmClass[] = { 1.0, 1.0, 1.0, 1.0 };
	
	
	        //      public final static double FAILURE = 0.000072338; // /16
//      public final static double FAILURE = 0.000144676; // /4
//      public final static double FAILURE = 0.000289352; // /2

//	public final static double FAILURE = 0.000578703; //
//	public final static double FAILURE = 0.0000000075;

//	public final static double FAILURE = 0.00000014;

//	public final static double FAILURE = 0.0021; 
//	public final static double FAILURE = 0.0000073;
//	public final static double FAILURE = 2.82e-6;
//	public final static double FAILURE = 0.00000029;

//      public final static double FAILURE = 0.001157407; // *2
//      public final static double FAILURE = 0.002314814; // *4
//      public final static double FAILURE = 0.004629628; // *8
//      public final static double FAILURE = 0.009259256; // *16
//      public final static double FAILURE = 0.018518512; // *32

    public static double FAILURE = 0.074074048; // *128
//      public final static double FAILURE = 0.148148096; // *128

	public static int GOOGLE_TRACES = 0;
	public static int MY_POLICIES = 1;
	public static int DVFS = 0;
	public static int PENALTY = 1;
	public static ArrayList<Double> energy_prices = new ArrayList<Double>();
	public static int VARIANT_ENERGY = 0;


	public final static int numDatacenter = 2;


}
