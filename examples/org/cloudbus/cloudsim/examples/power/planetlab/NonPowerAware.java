package org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.examples.power.Helper;
import org.cloudbus.cloudsim.power.PowerDatacenterNonPowerAware;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;

/**
 * A simulation of a heterogeneous non-power aware data center: all hosts consume maximum power all
 * the time.
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
public class NonPowerAware {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		String experimentName = "planetlab_npa";
		String outputFolder = "output";
		String inputFolder = NonPowerAware.class.getClassLoader().getResource("workload/planetlab/20110303")
				.getPath();

		Log.setDisabled(!Constants.ENABLE_OUTPUT);
		Log.printLine("Starting " + experimentName);

		try {
			CloudSim.init(1, Calendar.getInstance(), false);

			DatacenterBroker broker = Helper.createBroker();
			int brokerId = broker.getId();

			List<Cloudlet> cloudletList = PlanetLabHelper.createCloudletListPlanetLab(brokerId, inputFolder);
			List<Vm> vmList = Helper.createVmList(brokerId, cloudletList.size(), null);
			List<PowerHost> hostList = Helper.createHostList(PlanetLabConstants.NUMBER_OF_HOSTS);

			PowerDatacenterNonPowerAware datacenter = (PowerDatacenterNonPowerAware) Helper.createDatacenter(
					"Datacenter",
					PowerDatacenterNonPowerAware.class,
					hostList,
					new PowerVmAllocationPolicySimple(hostList));

			datacenter.setDisableMigrations(true);

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);

			CloudSim.terminateSimulation(Constants.SIMULATION_LIMIT);
			double lastClock = CloudSim.startSimulation();

			List<Cloudlet> newList = broker.getCloudletReceivedList();
			Log.printLine("Received " + newList.size() + " cloudlets");

			CloudSim.stopSimulation();

			Helper.printResults(
					datacenter,
					vmList,
					lastClock,
					experimentName,
					Constants.OUTPUT_CSV,
					outputFolder);

		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
			System.exit(0);
		}

		Log.printLine("Finished " + experimentName);
	}

}
