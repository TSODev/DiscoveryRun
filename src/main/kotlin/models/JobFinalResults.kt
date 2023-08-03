package models

data class JobFinalResults(
    val Dropped: Dropped,
    val Error: Error,
    val NoAccess: NoAccess,
    val NoResponse: NoResponse,
    val Skipped: Skipped,
    val Success: Success
)

data class Dropped(
    val count: Int,
    val uri: String
)

data class Error(
    val count: Int,
    val uri: String
)

data class NoAccess(
    val count: Int,
    val uri: String
)

data class NoResponse(
    val count: Int,
    val uri: String
)

data class Skipped(
    val count: Int,
    val uri: String
)

data class Success(
    val count: Int,
    val uri: String
)