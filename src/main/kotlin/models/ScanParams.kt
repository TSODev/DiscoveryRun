package models

data class ScanParams(
    val cluster_url: String?,
    val credential: String?,
    val provider: String?,
    val region: List<String>
)