package com.lab.opengarage.data

import com.lab.opengarage.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** 현재 로그인 사용자. 미로그인 시 null 방출. */
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    /** Google [idToken] 으로 재인증 후 사용자 문서 + Firebase Auth 계정 삭제(회원 탈퇴). */
    suspend fun deleteAccount(idToken: String): Result<Unit>
}
