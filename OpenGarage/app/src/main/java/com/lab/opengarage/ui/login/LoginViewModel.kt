package com.lab.opengarage.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {
    val uiState = MutableStateFlow<UiState<Unit>>(UiState.Success(Unit))

    fun onIdToken(idToken: String) {
        uiState.value = UiState.Loading
        viewModelScope.launch {
            auth.signInWithGoogle(idToken)
                .onSuccess { uiState.value = UiState.Success(Unit) }
                .onFailure { uiState.value = UiState.Error(it.message ?: "로그인 실패") }
        }
    }
}
