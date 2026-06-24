package com.lab.opengarage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** 앱 진입 인증 상태. Loading=세션 복원 중(스플래시), 이후 로그인/비로그인 확정. */
enum class AuthUi { Loading, SignedIn, SignedOut }

@HiltViewModel
class RootViewModel @Inject constructor(
    auth: AuthRepository,
) : ViewModel() {
    val authState: StateFlow<AuthUi> = auth.currentUser
        .map { if (it != null) AuthUi.SignedIn else AuthUi.SignedOut }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthUi.Loading)
}
