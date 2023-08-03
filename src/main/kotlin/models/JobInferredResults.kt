package models

data class JobInferredResults(
    val Host: Host,
    val LoadBalancerService: LoadBalancerService,
    val MFPart: MFPart,
    val ManagementController: ManagementController,
    val NetworkDevice: NetworkDevice,
    val Printer: Printer,
    val SNMPManagedDevice: SNMPManagedDevice,
    val SoftwareInstance: SoftwareInstance,
    val StorageDevice: StorageDevice,
    val StorageSystem: StorageSystem,
    val StorageVolume: StorageVolume,
    val VirtualMachine: VirtualMachine
)