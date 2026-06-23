package com.lab.opengarage.ui.common

/** 화면 비동기 상태 표현용 공용 sealed 타입. */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
