package com.lab.opengarage.data

import com.lab.opengarage.model.User
import kotlinx.coroutines.flow.Flow

/** 계정 삭제에 최근 로그인(재인증)이 필요할 때 던지는 마커. */
class ReauthRequiredException : Exception("재인증 필요")

interface AuthRepository {
    /** 현재 로그인 사용자. 미로그인 시 null 방출. */
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
    /**
     * 사용자 문서 + Firebase Auth 계정 삭제(회원 탈퇴).
     * [idToken]=null 이면 재인증 없이 시도(최근 로그인 상태면 성공). 최근 로그인 필요 시
     * [ReauthRequiredException] 으로 실패 → 호출측이 Google [idToken] 받아 재호출.
     */
    suspend fun deleteAccount(idToken: String?): Result<Unit>
}
