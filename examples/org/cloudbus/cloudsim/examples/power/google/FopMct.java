package org.cloudbus.cloudsim.examples.power.google;

import java.io.IOException;
import java.io.PrintWriter;

import org.cloudbus.cloudsim.examples.power.Constants;

/*
	Implemented by the Computer Systems Lab, University of Thessaly (https://csl.e-ce.uth.gr)
	for the MLSysOps project (https://mlsysops.eu)
	 
	License: LGPL - https://www.gnu.org/licenses/lgpl-3.0.en.html
	 
	Copyright (c) 2024, The University of Thessaly, Greece
	 
	Contact: Bowen Sun bsun@uth.gr
	         Christos Antonopoulos  cda@uth.gr
*/
public class FopMct {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		boolean enableOutput = false;
		boolean outputToFile = false;
		String inputFolder = FopMct.class.getClassLoader().getResource("workload/planetlab").getPath();
		String outputFolder = "output";
//		String workload = "10000_3025_1_mem_004"; // PlanetLab workload
		String workload = "1000_1425_1";
		String vmAllocationPolicy = "fop"; // DVFS policy without VM migrations
		String vmSelectionPolicy = "mct";
		String parameter = "1.0";

		Constants.overload_file = new PrintWriter(args[0] + "/overload.txt", "UTF-8");
		Constants.migration_file = new PrintWriter(args[0] + "/migration.txt", "UTF-8");
		Constants.crash_file = new PrintWriter(args[0] + "/crash.txt", "UTF-8");
		Constants.active_nodes_file = new PrintWriter(args[0] + "/active.txt", "UTF-8");
		Constants.nominal_hosts_file = new PrintWriter(args[0] + "/nominal_hosts.txt", "UTF-8");
		Constants.vm_map_file = new PrintWriter(args[0] + "/vm_map.txt", "UTF-8");

        // Constants.THRESHOLD = Double.parseDouble(args[1]);
        // Constants.FAILURE = Double.parseDouble(args[2]);
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
