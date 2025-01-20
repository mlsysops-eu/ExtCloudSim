# ExtCloudSim
The simulation of cloud server infrastructure enables scale-out experiments on the management of cloud infrastructure, experimentation with configuration parameters not available to end-users of public cloud infrastructures (such as jobs consolidation, node allocation control, node performance/power step configuration), and controlled experimentation with varying energy prices and green energy availability, which is not possible to perform (in a reproducible way) even if one had access to real cloud infrastructures. The cloud simulation environment scales to hundreds of datacenter nodes, that may span multiple datacenters. It supports dynamically switching nodes on/off, controlling multiple CPU voltage/frequency steps, nodes with multiple memory capacities, and different CPU and memory power profiles based on the characterization of real systems (ARM and Intel). It also supports different Service Level Agreements (SLA) models, as well as fine-grained energy price variability, which, in turn, can be used to model the dynamic availability of green energy.  

The framework that was chosen as a basis (and respectively extended) for this work is CloudSim, developed by CLOUDS Lab at the University of Melbourne and distributed as a Java-based open-source software.  

A more detailed description, with relevant references can be found in deliverable [D4.3 “Final versions of system simulators”](https://mlsysops.eu/public-deliverables/).

**Detailed instructions will be uploaded soon.**

