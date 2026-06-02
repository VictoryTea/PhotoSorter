package com.example.photosorter.data.model

sealed class SortMode {
    data object Recent : SortMode()
    data class Month(val year: Int, val month: Int) : SortMode() // month is 1-12
    data object OnThisDay : SortMode()

    companion object {
        fun fromString(str: String): SortMode {
            if (str == "recent") return Recent
            if (str == "on_this_day") return OnThisDay
            if (str.startsWith("month_")) {
                val parts = str.split("_")
                if (parts.size == 3) {
                    val year = parts[1].toIntOrNull() ?: 2024
                    val month = parts[2].toIntOrNull() ?: 1
                    return Month(year, month)
                }
            }
            return Recent
        }
    }

    override fun toString(): String {
        return when (this) {
            is Recent -> "recent"
            is OnThisDay -> "on_this_day"
            is Month -> "month_${year}_${month}"
        }
    }
}
