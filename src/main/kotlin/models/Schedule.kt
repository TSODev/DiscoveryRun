package models

data class Schedule(
    val days_of_month: List<Int>,
    val days_of_week: List<String>,
    val duration: Int,
    val recurrence_type: String,
    val start_minute: Int,
    val start_times: List<Int>,
    val week_def: String
)