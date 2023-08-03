package models

class JobResults : ArrayList<JobResultsItem>()

data class JobResultsItem(
    val blocked: Boolean,
    val consolidating: Boolean,
    val done: Int,
    val finished: Boolean,
    val inferred: String,
    val key: String,
    val label: String,
    val pre_scanning: Int,
    val results: String,
    val scan_kind: String,
    val scan_level: String,
    val scan_options: ScanOptions,
    val scan_type: String,
    val scanning: Int,
    val scope: String,
    val starttime: String,
    val total: Int,
    val uri: String,
    val user: String,
    val uuid: String,
    val valid_ranges: String,
    val waiting: Int
)