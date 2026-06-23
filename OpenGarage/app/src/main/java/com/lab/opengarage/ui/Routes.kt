package com.lab.opengarage.ui

object Routes {
    const val FEED = "feed"
    const val GARAGE = "garage"
    const val PROFILE = "profile"
    const val CAR_DETAIL = "car/{carId}"
    const val CAR_EDIT = "carEdit?carId={carId}"
    const val RECORD_EDIT = "recordEdit?carId={carId}&recordId={recordId}"
    const val RECORD_DETAIL = "record/{recordId}"

    fun carDetail(carId: String) = "car/$carId"
    fun carEdit(carId: String? = null) = "carEdit?carId=${carId ?: ""}"
    fun recordEdit(carId: String? = null, recordId: String? = null) =
        "recordEdit?carId=${carId ?: ""}&recordId=${recordId ?: ""}"
    fun recordDetail(recordId: String) = "record/$recordId"
}
