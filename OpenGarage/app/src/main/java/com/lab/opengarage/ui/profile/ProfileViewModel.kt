package com.lab.opengarage.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.model.User
import com.lab.opengarage.ui.common.Paginator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: AuthRepository,
    private val records: RecordRepository,
) : ViewModel() {
    private var uid: String? = null

    val user: StateFlow<User?> =
        auth.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val paginator = Paginator { cursor, limit -> records.myRecordsPage(uid ?: "", cursor, limit) }

    init { viewModelScope.launch { ensureUid(); paginator.refresh() } }

    private suspend fun ensureUid() {
        if (uid == null) uid = auth.currentUser.filterNotNull().first().uid
    }

    fun refresh() = viewModelScope.launch { ensureUid(); paginator.refresh() }
    fun loadMore() = viewModelScope.launch { paginator.loadMore() }
    fun signOut() = viewModelScope.launch { auth.signOut() }
}
