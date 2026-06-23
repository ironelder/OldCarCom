package com.lab.opengarage.ui

object Routes {
    const val FEED = "feed"
    const val GARAGE = "garage"
    const val PROFILE = "profile"
    const val CAR_DETAIL = "car/{carId}"
    const val CAR_EDIT = "carEdit"
    const val RECORD_EDIT = "recordEdit?carId={carId}"
    const val RECORD_DETAIL = "record/{recordId}"

    fun carDetail(carId: String) = "car/$carId"
    fun recordEdit(carId: String? = null) = "recordEdit?carId=${carId ?: ""}"
    fun recordDetail(recordId: String) = "record/$recordId"
}
