package com.lab.opengarage.fake

import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.model.User
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {
    val state = MutableStateFlow<User?>(null)
    override val currentUser = state

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        val u = User("uid1", "테스터", "")
        state.value = u
        return Result.success(u)
    }

    override suspend fun signOut() {
        state.value = null
    }

    override suspend fun deleteAccount(idToken: String): Result<Unit> {
        state.value = null
        return Result.success(Unit)
    }
}
