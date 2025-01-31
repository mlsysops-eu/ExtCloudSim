package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;
import java.io.PrintWriter;

import org.cloudbus.cloudsim.examples.power.Constants;

/**
 * A simulation of a heterogeneous power aware data center that applies the Local Regression (LR) VM
 * allocation policy and Minimum Migration Time (MMT) VM selection policy.
 * 
 * This example uses a real PlanetLab workload: 20110303.
 * 
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
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
 * @since Jan 5, 2012
 */
public class LrMmt {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = false;
		boolean outputToFile = false;
		String inputFolder = LrMmt.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
		// String workload = "10000_3025_1_mem_004"; // PlanetLab workload
		String workload = "1000_1425_1"; // PlanetLab workload
		String vmAllocationPolicy = "lr"; // Local Regression (LR) VM allocation policy
		String vmSelectionPolicy = "mmt"; // Minimum Migration Time (MMT) VM selection policy
		String parameter = "1.2"; // the safety parameter of the LR policy

		Constants.overload_file = new PrintWriter(args[0] + "overload.txt", "UTF-8");
		Constants.migration_file = new PrintWriter(args[0] + "migration.txt", "UTF-8");
		Constants.crash_file = new PrintWriter(args[0] + "crash.txt", "UTF-8");
		Constants.active_nodes_file = new PrintWriter(args[0] + "active.txt", "UTF-8");
		Constants.nominal_hosts_file = new PrintWriter(args[0] + "nominal_hosts.txt", "UTF-8");
		Constants.vm_map_file = new PrintWriter(args[0] + "vm_map.txt", "UTF-8");

		Constants.THRESHOLD = Double.parseDouble(args[1]);
        Constants.FAILURE = Double.parseDouble(args[2]);
		new PlanetLabRunner(
				enableOutput,
				outputToFile,
				inputFolder,
				outputFolder,
				workload,
				vmAllocationPolicy,
				vmSelectionPolicy,
				parameter);
		
		Constants.overload_file.close();
		Constants.crash_file.close();
		Constants.migration_file.close();
		Constants.active_nodes_file.close();
		Constants.vm_map_file.close();
		Constants.nominal_hosts_file.close();
	}

}
