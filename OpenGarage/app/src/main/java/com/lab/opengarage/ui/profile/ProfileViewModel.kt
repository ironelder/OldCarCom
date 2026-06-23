package com.lab.opengarage.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.Record
import com.lab.opengarage.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: AuthRepository,
    records: RecordRepository,
) : ViewModel() {
    val user: StateFlow<User?> =
        auth.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val myRecords: StateFlow<List<Record>> = auth.currentUser
        .filterNotNull()
        .flatMapLatest { records.observeMyPublicRecords(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun signOut() {
        viewModelScope.launch { auth.signOut() }
    }
}
