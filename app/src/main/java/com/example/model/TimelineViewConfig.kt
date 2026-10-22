package com.example.model

enum class TimelineViewMode {
    GRID,
    MASONRY
}

enum class TimelineSortField(val label: String) {
    DATE_TAKEN("Date taken"),
    DATE_ADDED("Date added"),
    FILE_NAME("File name"),
    FILE_SIZE("File size")
}

enum class TimelineSortDirection {
    ASCENDING,
    DESCENDING
}

enum class TimelineGroupBy(val label: String) {
    NONE("None"),
    DAY("Day"),
    MONTH("Month"),
    YEAR("Year"),
    DEVICE_FOLDER("Device folder"),
    INITIAL("Initial")
}

data class TimelineViewConfig(
    val viewMode: TimelineViewMode = TimelineViewMode.MASONRY,
    val columnCount: Int = 4,
    val sortField: TimelineSortField = TimelineSortField.DATE_TAKEN,
    val sortDirection: TimelineSortDirection = TimelineSortDirection.DESCENDING,
    val groupBy: TimelineGroupBy = TimelineGroupBy.DAY
)
