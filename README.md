# ExtCloudSim
The simulation of cloud server infrastructure enables scale-out experiments on the management of cloud infrastructure, experimentation with configuration parameters not available to end-users of public cloud infrastructures (such as jobs consolidation, node allocation control, node performance/power step configuration), and controlled experimentation with varying energy prices and green energy availability, which is not possible to perform (in a reproducible way) even if one had access to real cloud infrastructures. The cloud simulation environment scales to hundreds of datacenter nodes, that may span multiple datacenters. It supports dynamically switching nodes on/off, controlling multiple CPU voltage/frequency steps, nodes with multiple memory capacities, and different CPU and memory power profiles based on the characterization of real systems (ARM and Intel). It also supports different Service Level Agreements (SLA) models, as well as fine-grained energy price variability, which, in turn, can be used to model the dynamic availability of green energy.  

The framework that was chosen as a basis (and respectively extended) for this work is CloudSim, developed by CLOUDS Lab at the University of Melbourne and distributed as a Java-based open-source software.  

A more detailed description, with relevant references can be found in deliverable [D4.3 “Final versions of system simulators”](https://mlsysops.eu/public-deliverables/).

## Prerequisites
* [Java Development Kit](https://www.oracle.com/java/technologies/downloads/#java8) (tested with Java8). 
* [Apache Commons Math Library](https://commons.apache.org/proper/commons-math/). Version 3.6.1 is distributed with ExtCloudSim.

## Download and installation
The extended CloudSim simulator can be downloaded from the [MLSysOps repository](https://github.com/mlsysops-eu/ExtCloudSim). The repository can either be cloned, or the latest release can be downloaded and extracted.

## Execution
Set up a Java-based IDE like Eclipse, and add the CloudSim jar files to the project's classpath. For more details, please refer to the CloudSim official tutorial.

## Extensions
The following sections provide instructions for using the extensions, developed by MLSysOps, over the original CloudSim.

### Host configuration control at simulation-time
We have introduced explicit Voltage and Frequency Scaling (VFS) control for Processing Elements (PEs), enabling host-level policies to adjust operating points dynamically.
1.	Configuration: The number of available operating points is defined in the attribute `POINTS` of class `Constants` in file [examples/org/cloudbus/cloudsim/examples/power/Constants.java](./examples/org/cloudbus/cloudsim/examples/power/Constants.java):
```Java
	public final static int POINTS = 4;
```

2.	Usage: To activate VFS control, the user needs to enable the frequency-aware placement during the VM placement via `frequencyAwarePlacement` function in `PowerVmAllocationPolicyMigrationAbstract` class [sources/org/cloudbus/cloudsim/power/PowerVmAllocationPolicyMigrationAbstract.java](./sources/org/cloudbus/cloudsim/power/PowerVmAllocationPolicyMigrationAbstract.java). This is achieved by setting the attribute `DVFS` in the class `Constants` in file [examples/org/cloudbus/cloudsim/examples/power/Constants.java](./examples/org/cloudbus/cloudsim/examples/power/Constants.java):
```Java
	public static int DVFS = 1;
```
The simulator, then, considers multiple DVFS points when selecting the optimal hosts for VM placement / migration. Moreover, it sets the DVFS step for each host as the last step at each scheduling interval.
3.	If DVFS functionality is enabled, the selected configuration (DVFS step) for each node will be written to the output file `active.txt`.


### Enhanced power modelling 
ExtCloudSim extends CloudSim's power models with accurate models which estimate power to the plug based on: (a) CPU utilization, and (b) the effective VFS operating point. The models have been implemented and validated for three real-world architectures: (a) A Lenovo Intel Xeon 1220-based system, (b) an ARM-Based Applied Micro X-Gene 2 microserver, and (c) an ARM-Based Applied Micro X-Gene 3 server.
1. Configuration: Power calculations are implemented in the `updateCost` method in [sources/org/cloudbus/cloudsim/power/PowerDatacenter.java](./sources/org/cloudbus/cloudsim/power/PowerDatacenter.java), where custom power models (`MyPower` variables) are used. 
```Java
	utilization = CostEstimation.getUtilizationHost(load);
	double voltage = host.getVoltage(operatingPoint, host.getVoltageClass(), host.getConfiguration());
	double frequency = host.getFrequency(operatingPoint);
			
	myPowerFrame += host.getPowerEstimation(utilization, voltage, frequency, host.getConfiguration()) * Constants.TIME_SLOT;
```
The power models are implemented in [sources/org/cloudbus/cloudsim/power/models](./sources/org/cloudbus/cloudsim/power/models).


 2. Usage: Set the `HOST_POWER` variable defined in Constants class [examples/org/cloudbus/cloudsim/examples/power/Constants.java](./examples/org/cloudbus/cloudsim/examples/power/Constants.java) to one of the following:
 * PowerModelSpecPowerLenovoXeon1220
 * PowerModelSpecPowerXgene2
 * PowerModelSpecPowerXgene3
 ```Java
 	public static PowerModel[] HOST_POWER = {
		new PowerModelSpecPowerLenovoXeon1220(),
		new PowerModelSpecPowerLenovoXeon1220()
	};
```

 3. The power model used affects the energy consumption reported in `active.txt`

### Enhanced performance modelling
ExtCloudSim implements 3 classes of performance sensitivity to frequency scaling: (a) Compute-bound, (b) memory-bound, and (c) hybrid. VMs can be assigned to one of these classes.
1. Configuration: The level of performance sensitivity for each of the three VM classes is platform dependent. Therefore, it is defined as the `overhead` attribute of the class that implements the power model for each architecture, implemented within the [sources/org/cloudbus/cloudsim/power/models](./sources/org/cloudbus/cloudsim/power/models) directory. For example, for the Applied Micro X-gene 2 architecture the respective declaration is:
```Java
	private static final double[][] overhead = {
			{2.4, 1.23, 1.07, 1.0},
			{4.9, 1.60, 1.16, 1.0},
			{8, 2.4, 1.41, 1.0}
	};
```
Each row of the 2D array corresponds to a VM class (memory-bound, hybrid, compute-bound respectively) and columns correspond to frequency steps. The constants in the array are execution time multiplers for lower frequencies, with respect to the performance at the highest available frequency.

2. Usage: When creating VM lists ([examples/org/cloudbus/cloudsim/examples/power/Helper.java](./examples/org/cloudbus/cloudsim/examples/power/Helper.java)) VMs are assigned [^1] to one of the three performance sensitivity classes. 
```Java
	int vmClass = rand.nextInt(3);
```

The overhead multipliers are used by the  `estimationOverhead()` method in the respective power model file
```Java
	protected double estimationOverhead(int vmClass, int operatingPoint) {
		return overhead[vmClass][operatingPoint];
	}
```
To deactivate the extended performance modelling functionality the user can substitute the return value of the method by `1.0`.

[^1]: The assigment is random in the sample code, however the respective information can be easily read -- if available -- from the input trace.

### Modelling of SLAs and SLA violation costs
ExtCloudSim models 3 components of SLA violations (and SLA violation costs), due to (i) node overloads (due to CPU resources overcommitement), (ii) node crashes, and (iii) VM migrations. 
1. Configuration: The methods that implement the 3 components of SLA violations are defined in the interface class `CostModel` in [sources/org/cloudbus/cloudsim/cost/model/CostModel.java](./sources/org/cloudbus/cloudsim/cost/model/CostModel.java).
```Java
    public interface CostModel {
	    public double getSLAVMigration(Vm vm) throws IllegalArgumentException;
	    public double getSLAVOverload(Vm vm) throws IllegalArgumentException;
	    public double getSLAVCrash(Vm vm) throws IllegalArgumentException;
}
```
The cost estimation is, in turn, implemented in the `CostEstimation` class in [sources/org/cloudbus/cloudsim/cost/model/CostEstimation.java](./sources/org/cloudbus/cloudsim/cost/model/CostEstimation.java).

2. Usage: The cost due to SLA Violations is updated by the `updateCost()` method of the `PowerDatacenter` class in [sources/org/cloudbus/cloudsim/power/PowerDatacenter.java](./sources/org/cloudbus/cloudsim/power/PowerDatacenter.java) and the sla_penalty calculated is registered to the core simulator data structures with the `setPenaltyCost()` method of the `PowerDatacenter` class. 
```Java
	protected void setPenaltyCost(double penaltyCost) {
		this.penaltyCost = penaltyCost;
	}
```
The cost is calculated by the `updateCost()` method in `PowerDatacenter`, which is automatically triggered during event processing.

To deactivate SLA cost accounting, the user may always reset `penaltyCost` to `0.0` in `setPenaltyCost()`. Alternatively, the user may reset the `PENALTY` attribute in `Constants` class in [examples/org/cloudbus/cloudsim/examples/power/Constants.java](./examples/org/cloudbus/cloudsim/examples/power/Constants.java).
```Java
	public static int PENALTY = 0;
```

3. The calculated SLA violation costs (itemized per reason) are logged in the `slapen.txt` output file. Also, the SLA violation cost is used as a component of the total cost (together with the energy cost) in [sources/org/cloudbus/cloudsim/power/PowerDatacenter.java](./sources/org/cloudbus/cloudsim/power/PowerDatacenter.java)
```Java
	setCost(getPenaltyCost() + getEnergyCost()))
```

### Support for popular input traces
The original Cloudsim is designed to consume PlanetLab traces. In the ExtCloudsim release we provide text preprocessors that enable the use of other popular, publicly available traces. More specifically, ExtCloudSim supports [Google cluster-data traces](https://github.com/google/cluster-data) and [Azure public dataset traces](https://github.com/Azure/AzurePublicDataset).
* Google traces: The preprocessor is provided as a [python script](./examples/workload/googleTraceParser/gtraces_parser.py). The user needs to modify three variables, namely `usage_folder` (the folder where google resource usage traces have been stored), `events_folder` (where google event traces have been stored), and `output_folder` (the folder where the output trace will be stored). The output trace can then be directly used by the simulator. 
* Azure traces: The preprocessor is provided as a [Jypyter notebook](examples/workload/AzureTraceParser/AzureTraceParser_Example.ipynb). The user needs to modify the `readpath` and `file_pattern` variables, which specify the path and the pattern of the Azure input files that will be processed. Similarly, by modifying `output_file_path` the user can specify the path where the output trace will be stored). Similarly to google traces, the output trace can be directly used by the simulator.

When invoking the simulator, the input and output folders are specified as the 1st and 2nd command-line argument, respectively. The 3rd command-line argument specifies the name of the input trace file.

### Energy operation costs
ExtCloudSim introduces support for variable energy pricing. 
The trace file name to be used by the simulation is specified in [examples/org/cloudbus/cloudsim/examples/power/Constants.java](./examples/org/cloudbus/cloudsim/examples/power/Constants.java), by modifying the `energyFilename` attribute in the `Constants` class. 

The variable energy price trace is a text file that provides the energy price (in $/MWh) in 5 min steps (one value per line). An example is provided in [examples/workload/energy_price](./examples/workload/energy_price).

To deactivate variable energy pricing, the user may reset the `VARIANT_ENERGY` attribute in `Constants` class in [examples/org/cloudbus/cloudsim/examples/power/Constants.java](./examples/org/cloudbus/cloudsim/examples/power/Constants.java).
```Java
	public static int VARIANT_ENERGY = 0;
```
In this case, the simulator uses the value of the `ENERGY_PRICE` attribute in in `Constants` class in [examples/org/cloudbus/cloudsim/examples/power/Constants.java](./examples/org/cloudbus/cloudsim/examples/power/Constants.java) for energy cost calculations.
```Java
	public static double ENERGY_PRICE = 4.2e-8;
```

### Multi-datacenter simulation
ExtCloudSim departs from the multi-datacenter implementation offered by the original CloudSim, in order to enable global management (potentially centralized, using a single broker), across multiple datacenters. ExtCloudSim extends the host list to include datacenter IDs, representing different datacenters. During migrations, the simulator distinguishes between intra-datacenter and inter-datacenter ones by checking the datacenter ID of both source and destination hosts in [sources/org/cloudbus/cloudsim/power/PowerDatacenter.java]](./sources/org/cloudbus/cloudsim/power/PowerDatacenter.java). Users can further customize allocation and migration strategies for multi-datacenter management.

This functionality is enabled/disabled by setting the `multiDatacenter` attribute in the `Constants` class in [examples/org/cloudbus/cloudsim/examples/power/Constants.java](./examples/org/cloudbus/cloudsim/examples/power/Constants.java) to true/false respectively.
```Java
	public final static boolean multiDatacenter = true;
```

A logfile excerpt with multi-datacenter functionality enabled follows:
```
300.1: A new period of Allocation Start... The hosts mainly Powered by Brown Energy: Host #0 in Datacenter #3 Host #1 in Datacenter #3
300.1: Detected Datacenter #3 is mainly powered by brown energy... Start calculating migrationMap for VMs and the brown energy-powered hosts: 
300.1: Host #0 has an overutilization based on the defined threshold
300.1: Migration Process starts... 
300.1: Migration of VM 0 from Host #0 (Datacenter #3) to Host 0 (Datacenter #4) is started. NOTE: This is an Inter-Datacenter Migration, from Datacenter #3 to Datacenter #4 
300.1: Migration of VM 1 from Host #1 (Datacenter #3) to Host 0 (Datacenter #4) is started. NOTE: This is an Inter-Datacenter Migration, from Datacenter #3 to Datacenter #4
No candidate over-utilized host! Start calculating migrationMap for VMs and the OVER-utilized hosts: No VMs are attached with such Hosts! Start calculating migrationMap for VMs and the UNDER-utilized hosts: 
304.268000000003: VM #0 has been allocated to the host #0 in Datacenter #4 
304.268000000003: Migration of VM 0 to Host 0 is completed. 
304.268000000003: VM #1 has been allocated to the host #0 in Datacenter #4 
304.268000000003: Migration of VM 1 to Host 0 is completed.
```

&nbsp;
&nbsp;
&nbsp;
&nbsp;
&nbsp;
