package com.lab.opengarage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** 로그인 게이트용: 현재 사용자 관찰. null=미로그인. */
@HiltViewModel
class RootViewModel @Inject constructor(
    auth: AuthRepository,
) : ViewModel() {
    val currentUser: StateFlow<User?> =
        auth.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
