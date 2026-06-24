package com.lab.opengarage.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.lab.opengarage.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            val u = fa.currentUser
            trySend(u?.let { User(it.uid, it.displayName ?: "오너", it.photoUrl?.toString() ?: "") })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val cred = GoogleAuthProvider.getCredential(idToken, null)
        val res = auth.signInWithCredential(cred).await()
        val fu = res.user ?: error("로그인 실패")
        val user = User(
            uid = fu.uid,
            nickname = fu.displayName ?: "오너",
            photoUrl = fu.photoUrl?.toString() ?: "",
            createdAt = System.currentTimeMillis(),
        )
        db.collection("users").document(fu.uid).set(user).await()
        user
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount(idToken: String): Result<Unit> = runCatching {
        val u = auth.currentUser ?: error("로그인 필요")
        // 계정 삭제는 최근 로그인 필요 → Google 자격증명으로 재인증
        u.reauthenticate(GoogleAuthProvider.getCredential(idToken, null)).await()
        db.collection("users").document(u.uid).delete().await()
        u.delete().await()
    }
}
