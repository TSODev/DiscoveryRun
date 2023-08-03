package models

data class ScanOptions(
    val CLOUD_HOST_DETECTION: Boolean = true,
    val MAX_ACTIVE_IAP_SESSIONS: Int = 50,
    val MAX_ACTIVE_SSM_SESSIONS: Int = 50,
    val MAX_START_SSM_SESSIONS: Int = 3,
    val NO_PING: Boolean = false,
    val SESSION_LOGGING: Boolean = false,
    val SKIP_IMPLICIT_SCANS: Boolean = false,
    val VMWARE_GUEST_IMPLICIT_SCANS: Boolean = true
)