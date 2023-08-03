package models

data class JobRun(
    val company: String,
    val label: String,
    val outpost_id: String?,
    val ranges: List<String>,
    val restricted_org_id: String?,
    val scan_kind: String?,
    val scan_level: String?,
    val scan_options: ScanOptions,
    val scan_params: ScanParams,
    val scope: String?
)
