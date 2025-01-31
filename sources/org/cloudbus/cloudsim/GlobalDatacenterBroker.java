package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

/**
 * The Class GlobalDatacenterBroker.
 * 
 * To create a broker that could manage the datacenters
 * 
 * @author Bowen Sun
 */
public class GlobalDatacenterBroker extends DatacenterBroker {
	
	private List<PowerDatacenter> datacenterlist;
	
	private HashMap<Integer, List<PowerHost>> datacenterHostsMap;
	
    public GlobalDatacenterBroker(String name) throws Exception {
        super(name);
//        datacenterlist = new ArrayList<PowerDatacenter>();
//        setDatacenterList(new ArrayList<PowerDatacenter>());
        this.datacenterHostsMap = new HashMap<>();
    }
    
    public void setDatacenterHostsMap(int datacenterid, List<PowerHost> hostlist) {
    	datacenterHostsMap.put(datacenterid, hostlist);
    }

    // Method to get a user's age by their name
    public List<PowerHost> getDatacenterHostsMap(int datacenterid) {
        return datacenterHostsMap.get(datacenterid);
    }
    
    /**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
			case CloudSimTags.CROSS_DATACENTER_VM_MIGRATE:
				processCrossDatacenterMigrate(ev, false);
				break;
			case CloudSimTags.CROSS_DATACENTER_VM_MIGRATE_ACK:
				processCrossDatacenterMigrate(ev, true);
		}
	}
    
    private void processCrossDatacenterMigrate(SimEvent ev, boolean ack) {
    	Object tmp = ev.getData();
		if (!(tmp instanceof Map<?, ?>)) {
			throw new ClassCastException("The data object must be Map<String, Object>");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> migrate = (HashMap<String, Object>) tmp;

		Vm vm = (Vm) migrate.get("vm");
		Host host = (Host) migrate.get("host");

		getVmAllocationPolicy().deallocateHostForVm(vm);
		host.removeMigratingInVm(vm);
		boolean result = getVmAllocationPolicy().allocateHostForVm(vm, host);
		if (!result) {
			System.out.println("[Datacenter.processVmMigrate] VM allocation to the destination host failed");
			System.exit(0);
		}

		if (ack) {
			int[] data = new int[3];
			data[0] = getId();
			data[1] = vm.getId();

			if (result) {
				data[2] = CloudSimTags.TRUE;
			} else {
				data[2] = CloudSimTags.FALSE;
			}
			sendNow(ev.getSource(), CloudSimTags.VM_CREATE_ACK, data);
		}

		System.out.println(
				CloudSim.clock() + ": Migration of VM " + vm.getId() + " to Host " + host.getId() + " is completed."
				);
		vm.setInMigration(false);
	}
    
	protected void addDatacenter(PowerDatacenter datacenter) {
//		GlobalDatacenterBroker.datacenterlist.add(datacenter);
	}
	
    public void submitDataCenterList(List<? extends PowerDatacenter> list) {
		getDatacenterList().addAll(list);
	}
    
//    public void setDataCenterHostList(List<List<PowerHost>> list) {
//		this.hostofdatacenterList = list;
//	}
//	
	public List<PowerDatacenter> getDatacenterList() {
		return  (List<PowerDatacenter>) datacenterlist;
	}
	
//	public List<List<PowerHost>> getDatacenterHostList() {
//		return hostofdatacenterList;
//	}
	
	protected <T extends PowerDatacenter> void setDatacenterList(List<T> datacenterlist) {
		this.datacenterlist = datacenterlist;
	}
	
	
	
	
    
}
