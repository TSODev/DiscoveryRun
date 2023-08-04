package models

data class JobRunScheduled(
    val allow_tpl_scan: Boolean = true,
    val company: String,
    val enabled: Boolean,
    val label: String,
    val outpost_id: String?,
    val ranges: List<String>,
    val restricted_org_id: String?,
    val scan_kind: String?,
    val scan_level: String?,
    val scan_options: ScanOptions,
    val scan_params: ScanParams,
    val scan_type: String?,
    val schedule: Schedule,
    val scope: String?
)
