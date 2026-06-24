package com.lab.opengarage.data

import com.lab.opengarage.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** 현재 로그인 사용자. 미로그인 시 null 방출. */
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    /** 사용자 문서 + Firebase Auth 계정 삭제(회원 탈퇴). 최근 로그인 필요 시 실패할 수 있음. */
    suspend fun deleteAccount(): Result<Unit>
}
