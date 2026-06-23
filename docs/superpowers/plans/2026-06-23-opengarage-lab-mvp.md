# OpenGarage Lab (오픈개러지) MVP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 올드카 오너가 정비기록을 공개 공유하고 같은 차종 사람끼리 열람하는 Android 앱의 MVP를 만든다. 주유기록은 개인 비공개 차계부로 둔다.

**Architecture:** Single-module Android app. Jetpack Compose UI → ViewModel (StateFlow) → Repository interface → Firebase SDK (Auth/Firestore/Storage). Repository는 인터페이스로 추상화해 ViewModel을 Fake로 단위테스트한다. UI·Firebase 연동은 실기기/에뮬레이터 수동 검증.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), Navigation-Compose, Firebase (Auth + Firestore + Storage), Coil(이미지), Hilt(DI), Coroutines/Flow, JUnit4 + kotlinx-coroutines-test (단위테스트).

## Global Constraints

- **minSdk 26, targetSdk 35, compileSdk 35** (올드폰까지 커버, 최신 빌드툴).
- **단일 모듈 `:app`** — 회사 멀티모듈 아키텍처 적용 안 함 (개인 사이드, 가볍게).
- **패키지 루트**: `com.opengaragelab.app`
- **언어/네이밍**: UI 표시 문자열 한글. 코드(클래스/함수/변수) 영문.
- **데이터 분기 규칙**: `type=FUEL` 기록은 항상 `isPublic=false` (피드 미노출). `type=MAINTENANCE` 기본 `isPublic=true`.
- **피드 쿼리 키**: `modelKey` = `"${make}_${model}"` 차 등록 시 자동 생성·기록에 비정규화 복사.
- **DI**: Hilt. 모든 Repository는 인터페이스 + Firebase 구현체 바인딩.
- **TDD 범위**: Repository 인터페이스를 가진 ViewModel은 Fake로 JVM 단위테스트(빨강→초록→커밋). Firebase 구현체·Composable은 단위테스트 대신 태스크 말미 수동 검증 체크리스트로 확인.
- **커밋**: 각 태스크 말미 1커밋. 메시지 한글 `[feat] ...` / `[test] ...` / `[chore] ...`.
- **비밀키**: `google-services.json` 은 `.gitignore` 처리. 레포에 커밋 금지.

---

## File Structure

```
app/
  build.gradle.kts
  google-services.json            (gitignore)
  src/main/java/com/opengaragelab/app/
    OpenGarageApp.kt               # @HiltAndroidApp Application
    MainActivity.kt                # setContent { AppRoot() }
    di/
      FirebaseModule.kt            # Firebase 인스턴스 provide
      RepositoryModule.kt          # 인터페이스 → 구현 바인딩
    model/
      User.kt
      Car.kt
      Record.kt                    # Record + RecordType + FuelType
    data/
      AuthRepository.kt            # interface
      AuthRepositoryImpl.kt
      CarRepository.kt             # interface
      CarRepositoryImpl.kt
      RecordRepository.kt          # interface
      RecordRepositoryImpl.kt
      PhotoRepository.kt           # interface (Storage 업로드)
      PhotoRepositoryImpl.kt
    ui/
      AppRoot.kt                   # NavHost + 하단탭
      Routes.kt                    # 화면 경로 상수
      theme/                       # Compose Theme (Studio 생성물)
      login/        LoginScreen.kt        LoginViewModel.kt
      feed/         FeedScreen.kt         FeedViewModel.kt
      garage/       GarageScreen.kt       GarageViewModel.kt
                    CarDetailScreen.kt    CarDetailViewModel.kt
                    CarEditScreen.kt      CarEditViewModel.kt
      record/       RecordEditScreen.kt   RecordEditViewModel.kt
                    RecordDetailScreen.kt RecordDetailViewModel.kt
      profile/      ProfileScreen.kt      ProfileViewModel.kt
      common/       UiState.kt    components.kt (공용 Composable)
  src/test/java/com/opengaragelab/app/
    fake/          FakeCarRepository.kt  FakeRecordRepository.kt  FakeAuthRepository.kt
    ui/...         각 ViewModel 테스트
firestore.rules                    # 보안 규칙
```

각 화면은 Screen(Composable, stateless-ish) + ViewModel(StateFlow) 한 쌍. 파일당 책임 1개.

---

## Task 0: 프로젝트 스캐폴딩 + 의존성

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`(root), `app/build.gradle.kts`, `gradle/libs.versions.toml`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `.gitignore`
- Create: `app/src/main/java/com/opengaragelab/app/OpenGarageApp.kt`, `MainActivity.kt`

**Interfaces:**
- Produces: 빌드 가능한 빈 Compose 앱 + Hilt 부트스트랩.

- [ ] **Step 1: Android Studio 새 프로젝트 생성**

Android Studio → New Project → **Empty Activity (Compose)**. Name: `OpenGarage Lab`, Package: `com.opengaragelab.app`, minSdk 26, Language Kotlin, Build config: Kotlin DSL. 기존 `OldCarCom` 폴더에 생성 또는 생성 후 내용 이동.

- [ ] **Step 2: `gradle/libs.versions.toml` 에 의존성 카탈로그 정의**

```toml
[versions]
agp = "8.5.2"
kotlin = "2.0.20"
composeBom = "2024.09.02"
hilt = "2.52"
hiltNav = "1.2.0"
firebaseBom = "33.4.0"
googleServices = "4.4.2"
coil = "2.7.0"
navigation = "2.8.1"
credentials = "1.3.0"
googleid = "1.1.1"
coroutinesTest = "1.9.0"
lifecycle = "2.8.6"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version = "1.13.1" }
androidx-lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version = "1.9.2" }
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hiltNav" }
firebase-bom = { module = "com.google.firebase:firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { module = "com.google.firebase:firebase-auth-ktx" }
firebase-firestore = { module = "com.google.firebase:firebase-firestore-ktx" }
firebase-storage = { module = "com.google.firebase:firebase-storage-ktx" }
credentials = { module = "androidx.credentials:credentials", version.ref = "credentials" }
credentials-play-services = { module = "androidx.credentials:credentials-play-services-auth", version.ref = "credentials" }
googleid = { module = "com.google.android.libraries.identity.googleid:googleid", version.ref = "googleid" }
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
junit = { module = "junit:junit", version = "4.13.2" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutinesTest" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.0.20-1.0.25" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

- [ ] **Step 3: `app/build.gradle.kts` 작성**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.opengaragelab.app"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.opengaragelab.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes { release { isMinifyEnabled = false } }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.googleid)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
```

Root `build.gradle.kts` 에 plugins alias `apply false` 등록 (android.application, kotlin.android, kotlin.compose, hilt, ksp, google-services).

- [ ] **Step 4: `.gitignore` 에 비밀·빌드 산출물 추가**

```
*.iml
.gradle/
/local.properties
/.idea/
build/
app/google-services.json
*.keystore
```

- [ ] **Step 5: Application + Manifest**

`OpenGarageApp.kt`:
```kotlin
package com.opengaragelab.app
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenGarageApp : Application()
```

`AndroidManifest.xml` 의 `<application>` 에 `android:name=".OpenGarageApp"` + `<uses-permission android:name="android.permission.INTERNET"/>` 추가. `MainActivity` 에 `@AndroidEntryPoint`.

- [ ] **Step 6: 빌드 검증**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL (google-services.json 은 Task 1에서 추가 — 그 전엔 google-services 플러그인 줄을 잠시 주석 처리하거나 Task 1 먼저 수행).

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "[chore] Android Compose + Firebase + Hilt 프로젝트 스캐폴딩"
```

---

## Task 1: Firebase 프로젝트 연결 (수동 설정)

**Files:**
- Create: `app/google-services.json` (Firebase 콘솔에서 다운로드, gitignore됨)
- Create: `firestore.rules` (Task 14에서 채움 — 여기선 빈 파일 생성 생략)

**Interfaces:**
- Produces: Firebase 백엔드 연결. Auth/Firestore/Storage 사용 가능.

- [ ] **Step 1: Firebase 콘솔에서 프로젝트 생성**

[console.firebase.google.com](https://console.firebase.google.com) → 프로젝트 추가 `opengarage-lab`. Android 앱 등록, 패키지명 `com.opengaragelab.app`.

- [ ] **Step 2: SHA-1 등록 (Google 로그인 필수)**

Run: `./gradlew signingReport`
디버그 SHA-1 복사 → Firebase 콘솔 프로젝트 설정 → 내 앱 → SHA 인증서 지문 추가.

- [ ] **Step 3: `google-services.json` 다운로드 → `app/` 에 배치**

- [ ] **Step 4: Firebase 콘솔에서 서비스 활성화**

- Authentication → Sign-in method → **Google** 사용 설정
- Firestore Database → 데이터베이스 만들기 (프로덕션 모드, 리전 `asia-northeast3` 서울)
- Storage → 시작하기

- [ ] **Step 5: Web Client ID 확보 (Credential Manager용)**

Firebase 콘솔 Authentication → Google 공급자 → **웹 SDK 구성**의 웹 클라이언트 ID 복사 → `app/src/main/res/values/strings.xml` 에 추가:
```xml
<string name="web_client_id">여기에-웹클라이언트-ID.apps.googleusercontent.com</string>
```

- [ ] **Step 6: 빌드 재검증**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL (google-services 플러그인이 json 인식).

- [ ] **Step 7: Commit** (json 은 gitignore라 strings.xml만)

```bash
git add app/src/main/res/values/strings.xml
git commit -m "[chore] Firebase 연결 + 웹 클라이언트 ID"
```

---

## Task 2: 도메인 모델

**Files:**
- Create: `model/User.kt`, `model/Car.kt`, `model/Record.kt`

**Interfaces:**
- Produces: `User`, `Car`, `Record`, `RecordType`, `FuelType` 데이터 클래스. 모든 Repository·ViewModel이 사용.

- [ ] **Step 1: 모델 작성** (Firestore `toObject()` 위해 빈 기본값 필수)

`model/User.kt`:
```kotlin
package com.opengaragelab.app.model

data class User(
    val uid: String = "",
    val nickname: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L,
)
```

`model/Car.kt`:
```kotlin
package com.opengaragelab.app.model

data class Car(
    val carId: String = "",
    val ownerUid: String = "",
    val make: String = "",
    val model: String = "",
    val year: Int = 0,
    val modelKey: String = "",
    val nickname: String = "",
    val photoUrl: String = "",
    val createdAt: Long = 0L,
) {
    companion object {
        fun makeModelKey(make: String, model: String): String =
            "${make.trim()}_${model.trim()}"
    }
}
```

`model/Record.kt`:
```kotlin
package com.opengaragelab.app.model

enum class RecordType { MAINTENANCE, FUEL }
enum class FuelType { GASOLINE, DIESEL, LPG }

data class Record(
    val recordId: String = "",
    val carId: String = "",
    val ownerUid: String = "",
    val ownerNickname: String = "",
    val modelKey: String = "",
    val type: RecordType = RecordType.MAINTENANCE,
    val date: Long = 0L,
    val mileageKm: Int = 0,
    val title: String = "",
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val cost: Long = 0L,
    val liters: Double? = null,
    val fuelType: FuelType? = null,
    val isPublic: Boolean = true,
    val createdAt: Long = 0L,
)
```

- [ ] **Step 2: modelKey 단위테스트**

`src/test/java/com/opengaragelab/app/model/CarTest.kt`:
```kotlin
package com.opengaragelab.app.model
import org.junit.Assert.assertEquals
import org.junit.Test

class CarTest {
    @Test fun modelKey_joins_make_and_model_with_underscore() {
        assertEquals("현대_프라이드", Car.makeModelKey(" 현대 ", " 프라이드 "))
    }
}
```

- [ ] **Step 3: 테스트 실행 (실패 확인)**

Run: `./gradlew :app:testDebugUnitTest --tests "*CarTest*"`
Expected: FAIL (makeModelKey 미구현이면) — 이미 Step1에서 구현했으면 바로 PASS. 그 경우 통과로 진행.

- [ ] **Step 4: 테스트 PASS 확인**

Run: `./gradlew :app:testDebugUnitTest --tests "*CarTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/opengaragelab/app/model app/src/test/java/com/opengaragelab/app/model
git commit -m "[feat] 도메인 모델(User/Car/Record) + modelKey 테스트"
```

---

## Task 3: Repository 인터페이스 + Result 래퍼

**Files:**
- Create: `data/AuthRepository.kt`, `data/CarRepository.kt`, `data/RecordRepository.kt`, `data/PhotoRepository.kt`
- Create: `ui/common/UiState.kt`

**Interfaces:**
- Produces: 모든 Repository 인터페이스 시그니처. ViewModel·Fake·Impl이 의존.

- [ ] **Step 1: UiState 작성**

`ui/common/UiState.kt`:
```kotlin
package com.opengaragelab.app.ui.common

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

- [ ] **Step 2: Repository 인터페이스 작성**

`data/AuthRepository.kt`:
```kotlin
package com.opengaragelab.app.data
import com.opengaragelab.app.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signOut()
}
```

`data/CarRepository.kt`:
```kotlin
package com.opengaragelab.app.data
import com.opengaragelab.app.model.Car
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    fun observeMyCars(ownerUid: String): Flow<List<Car>>
    suspend fun getCar(carId: String): Result<Car>
    suspend fun upsertCar(car: Car): Result<String>   // returns carId
    suspend fun deleteCar(carId: String): Result<Unit>
}
```

`data/RecordRepository.kt`:
```kotlin
package com.opengaragelab.app.data
import com.opengaragelab.app.model.Record
import kotlinx.coroutines.flow.Flow

interface RecordRepository {
    fun observeFeed(modelKey: String): Flow<List<Record>>          // isPublic==true
    fun observeCarRecords(carId: String): Flow<List<Record>>       // 정비+주유 전체
    fun observeMyPublicRecords(ownerUid: String): Flow<List<Record>>
    suspend fun getRecord(recordId: String): Result<Record>
    suspend fun upsertRecord(record: Record): Result<String>       // returns recordId
    suspend fun deleteRecord(recordId: String): Result<Unit>
}
```

`data/PhotoRepository.kt`:
```kotlin
package com.opengaragelab.app.data
import android.net.Uri

interface PhotoRepository {
    suspend fun uploadRecordPhotos(uid: String, recordId: String, uris: List<Uri>): Result<List<String>>
}
```

- [ ] **Step 3: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/opengaragelab/app/data app/src/main/java/com/opengaragelab/app/ui/common/UiState.kt
git commit -m "[feat] Repository 인터페이스 + UiState"
```

---

## Task 4: Firebase 구현체 + Hilt DI

**Files:**
- Create: `data/AuthRepositoryImpl.kt`, `data/CarRepositoryImpl.kt`, `data/RecordRepositoryImpl.kt`, `data/PhotoRepositoryImpl.kt`
- Create: `di/FirebaseModule.kt`, `di/RepositoryModule.kt`

**Interfaces:**
- Consumes: Task 3 인터페이스, Task 2 모델.
- Produces: Hilt가 주입하는 Firebase 백엔드 구현. 수동 검증 대상.

- [ ] **Step 1: FirebaseModule**

`di/FirebaseModule.kt`:
```kotlin
package com.opengaragelab.app.di
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides @Singleton fun auth(): FirebaseAuth = Firebase.auth
    @Provides @Singleton fun firestore(): FirebaseFirestore = Firebase.firestore
    @Provides @Singleton fun storage(): FirebaseStorage = Firebase.storage
}
```

- [ ] **Step 2: AuthRepositoryImpl** (Firebase Auth + Google idToken)

```kotlin
package com.opengaragelab.app.data
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.opengaragelab.app.model.User
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
        val user = User(fu.uid, fu.displayName ?: "오너", fu.photoUrl?.toString() ?: "",
            createdAt = System.currentTimeMillis())
        db.collection("users").document(fu.uid).set(user).await()
        user
    }

    override suspend fun signOut() { auth.signOut() }
}
```

- [ ] **Step 3: CarRepositoryImpl**

```kotlin
package com.opengaragelab.app.data
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.opengaragelab.app.model.Car
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CarRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
) : CarRepository {
    private val cars = db.collection("cars")

    override fun observeMyCars(ownerUid: String): Flow<List<Car>> = callbackFlow {
        val reg = cars.whereEqualTo("ownerUid", ownerUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null) { close(e); return@addSnapshotListener }
                trySend(snap?.toObjects(Car::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    override suspend fun getCar(carId: String): Result<Car> = runCatching {
        cars.document(carId).get().await().toObject(Car::class.java) ?: error("차량 없음")
    }

    override suspend fun upsertCar(car: Car): Result<String> = runCatching {
        val ref = if (car.carId.isBlank()) cars.document() else cars.document(car.carId)
        val toSave = car.copy(
            carId = ref.id,
            modelKey = Car.makeModelKey(car.make, car.model),
            createdAt = if (car.createdAt == 0L) System.currentTimeMillis() else car.createdAt,
        )
        ref.set(toSave).await()
        ref.id
    }

    override suspend fun deleteCar(carId: String): Result<Unit> = runCatching {
        cars.document(carId).delete().await(); Unit
    }
}
```

- [ ] **Step 4: RecordRepositoryImpl**

```kotlin
package com.opengaragelab.app.data
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.opengaragelab.app.model.Record
import com.opengaragelab.app.model.RecordType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecordRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
) : RecordRepository {
    private val records = db.collection("records")

    private fun query(q: Query) = callbackFlow {
        val reg = q.addSnapshotListener { snap, e ->
            if (e != null) { close(e); return@addSnapshotListener }
            trySend(snap?.toObjects(Record::class.java) ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    override fun observeFeed(modelKey: String): Flow<List<Record>> = query(
        records.whereEqualTo("modelKey", modelKey)
            .whereEqualTo("isPublic", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
    )

    override fun observeCarRecords(carId: String): Flow<List<Record>> = query(
        records.whereEqualTo("carId", carId)
            .orderBy("date", Query.Direction.DESCENDING)
    )

    override fun observeMyPublicRecords(ownerUid: String): Flow<List<Record>> = query(
        records.whereEqualTo("ownerUid", ownerUid)
            .whereEqualTo("type", RecordType.MAINTENANCE.name)
            .orderBy("createdAt", Query.Direction.DESCENDING)
    )

    override suspend fun getRecord(recordId: String): Result<Record> = runCatching {
        records.document(recordId).get().await().toObject(Record::class.java) ?: error("기록 없음")
    }

    override suspend fun upsertRecord(record: Record): Result<String> = runCatching {
        val ref = if (record.recordId.isBlank()) records.document() else records.document(record.recordId)
        val forced = record.copy(
            recordId = ref.id,
            isPublic = if (record.type == RecordType.FUEL) false else record.isPublic,
            createdAt = if (record.createdAt == 0L) System.currentTimeMillis() else record.createdAt,
        )
        ref.set(forced).await()
        ref.id
    }

    override suspend fun deleteRecord(recordId: String): Result<Unit> = runCatching {
        records.document(recordId).delete().await(); Unit
    }
}
```

- [ ] **Step 5: PhotoRepositoryImpl**

```kotlin
package com.opengaragelab.app.data
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val storage: FirebaseStorage,
) : PhotoRepository {
    override suspend fun uploadRecordPhotos(uid: String, recordId: String, uris: List<Uri>): Result<List<String>> = runCatching {
        uris.mapIndexed { i, uri ->
            val ref = storage.reference.child("users/$uid/records/$recordId/photo_$i.jpg")
            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        }
    }
}
```

- [ ] **Step 6: RepositoryModule (인터페이스 바인딩)**

```kotlin
package com.opengaragelab.app.di
import com.opengaragelab.app.data.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun auth(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun car(impl: CarRepositoryImpl): CarRepository
    @Binds @Singleton abstract fun record(impl: RecordRepositoryImpl): RecordRepository
    @Binds @Singleton abstract fun photo(impl: PhotoRepositoryImpl): PhotoRepository
}
```

- [ ] **Step 7: 컴파일 + 수동 검증**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL. (실데이터 검증은 화면 붙은 뒤 Task별 수동 체크리스트에서.)

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/opengaragelab/app/data app/src/main/java/com/opengaragelab/app/di
git commit -m "[feat] Firebase Repository 구현체 + Hilt DI 바인딩"
```

---

## Task 5: Fake Repository (테스트 기반)

**Files:**
- Create: `app/src/test/java/com/opengaragelab/app/fake/FakeAuthRepository.kt`, `FakeCarRepository.kt`, `FakeRecordRepository.kt`

**Interfaces:**
- Consumes: Task 3 인터페이스.
- Produces: 인메모리 Fake. 이후 모든 ViewModel 테스트가 사용.

- [ ] **Step 1: FakeCarRepository**

```kotlin
package com.opengaragelab.app.fake
import com.opengaragelab.app.data.CarRepository
import com.opengaragelab.app.model.Car
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCarRepository : CarRepository {
    val store = MutableStateFlow<List<Car>>(emptyList())
    override fun observeMyCars(ownerUid: String) =
        store.map { list -> list.filter { it.ownerUid == ownerUid } }
    override suspend fun getCar(carId: String) =
        store.value.find { it.carId == carId }?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("없음"))
    override suspend fun upsertCar(car: Car): Result<String> {
        val id = car.carId.ifBlank { "car_${store.value.size + 1}" }
        val saved = car.copy(carId = id, modelKey = Car.makeModelKey(car.make, car.model))
        store.value = store.value.filterNot { it.carId == id } + saved
        return Result.success(id)
    }
    override suspend fun deleteCar(carId: String): Result<Unit> {
        store.value = store.value.filterNot { it.carId == carId }
        return Result.success(Unit)
    }
}
```

- [ ] **Step 2: FakeRecordRepository** (피드 isPublic 필터 + FUEL 강제 비공개 재현)

```kotlin
package com.opengaragelab.app.fake
import com.opengaragelab.app.data.RecordRepository
import com.opengaragelab.app.model.Record
import com.opengaragelab.app.model.RecordType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecordRepository : RecordRepository {
    val store = MutableStateFlow<List<Record>>(emptyList())
    override fun observeFeed(modelKey: String) =
        store.map { l -> l.filter { it.modelKey == modelKey && it.isPublic }.sortedByDescending { it.createdAt } }
    override fun observeCarRecords(carId: String) =
        store.map { l -> l.filter { it.carId == carId }.sortedByDescending { it.date } }
    override fun observeMyPublicRecords(ownerUid: String) =
        store.map { l -> l.filter { it.ownerUid == ownerUid && it.type == RecordType.MAINTENANCE } }
    override suspend fun getRecord(recordId: String) =
        store.value.find { it.recordId == recordId }?.let { Result.success(it) }
            ?: Result.failure(IllegalStateException("없음"))
    override suspend fun upsertRecord(record: Record): Result<String> {
        val id = record.recordId.ifBlank { "rec_${store.value.size + 1}" }
        val forced = record.copy(recordId = id,
            isPublic = if (record.type == RecordType.FUEL) false else record.isPublic)
        store.value = store.value.filterNot { it.recordId == id } + forced
        return Result.success(id)
    }
    override suspend fun deleteRecord(recordId: String): Result<Unit> {
        store.value = store.value.filterNot { it.recordId == recordId }
        return Result.success(Unit)
    }
}
```

- [ ] **Step 3: FakeAuthRepository**

```kotlin
package com.opengaragelab.app.fake
import com.opengaragelab.app.data.AuthRepository
import com.opengaragelab.app.model.User
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {
    val state = MutableStateFlow<User?>(null)
    override val currentUser = state
    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        val u = User("uid1", "테스터", "")
        state.value = u
        return Result.success(u)
    }
    override suspend fun signOut() { state.value = null }
}
```

- [ ] **Step 4: 컴파일 확인 (테스트 소스셋)**

Run: `./gradlew :app:compileDebugUnitTestKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add app/src/test/java/com/opengaragelab/app/fake
git commit -m "[test] 인메모리 Fake Repository"
```

---

## Task 6: 네비게이션 + 하단 탭 골격

**Files:**
- Create: `ui/Routes.kt`, `ui/AppRoot.kt`
- Modify: `MainActivity.kt`

**Interfaces:**
- Consumes: (화면 Composable은 이후 태스크에서 채움 — 여기선 빈 placeholder Composable로 시작)
- Produces: `AppRoot()` NavHost + 3탭. 라우트 상수.

- [ ] **Step 1: Routes**

```kotlin
package com.opengaragelab.app.ui
object Routes {
    const val LOGIN = "login"
    const val FEED = "feed"
    const val GARAGE = "garage"
    const val PROFILE = "profile"
    const val CAR_DETAIL = "car/{carId}"
    const val CAR_EDIT = "carEdit?carId={carId}"
    const val RECORD_EDIT = "recordEdit?carId={carId}"
    const val RECORD_DETAIL = "record/{recordId}"
    fun carDetail(carId: String) = "car/$carId"
    fun carEdit(carId: String? = null) = "carEdit?carId=${carId ?: ""}"
    fun recordEdit(carId: String? = null) = "recordEdit?carId=${carId ?: ""}"
    fun recordDetail(recordId: String) = "record/$recordId"
}
```

- [ ] **Step 2: AppRoot (임시 placeholder 화면으로 NavHost 구성)**

3개 탭(FEED/GARAGE/PROFILE)을 `Scaffold + NavigationBar` 로, 각 라우트는 일단 `Text("...")` placeholder. 로그인 상태에 따라 시작 목적지 분기(`AuthViewModel.currentUser`). 이후 태스크가 placeholder를 실제 화면으로 교체.

```kotlin
package com.opengaragelab.app.ui
// 골격: Scaffold(bottomBar = NavigationBar { 3 items }) { NavHost(startDestination = FEED) {
//   composable(FEED){ Text("피드") }; composable(GARAGE){ Text("차고") }; composable(PROFILE){ Text("프로필") } } }
// 실제 코드는 Material3 NavigationBar + NavController 표준 패턴 사용.
```
> 구현 시 placeholder Composable 3개를 실제 라우트로 등록하고, Task 7~13에서 본문 교체. 각 화면 ViewModel은 `hiltViewModel()` 로 주입.

- [ ] **Step 3: MainActivity 연결**

```kotlin
// setContent { OpenGarageTheme { AppRoot() } }, @AndroidEntryPoint
```

- [ ] **Step 4: 빌드 + 실행 검증**

Run: `./gradlew :app:installDebug` 후 앱 실행.
Expected: 하단 탭 3개 전환 동작, 각 placeholder 텍스트 표시.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "[feat] Navigation + 하단탭 골격"
```

---

## Task 7: 로그인 (Google + Credential Manager)

**Files:**
- Create: `ui/login/LoginViewModel.kt`, `ui/login/LoginScreen.kt`
- Create: `app/src/test/java/com/opengaragelab/app/ui/login/LoginViewModelTest.kt`

**Interfaces:**
- Consumes: `AuthRepository.signInWithGoogle(idToken)`, `currentUser`.
- Produces: `LoginViewModel.onIdToken(idToken: String)`, `uiState: StateFlow<UiState<Unit>>`. 로그인 성공 시 `currentUser` 갱신 → AppRoot이 FEED로 이동.

- [ ] **Step 1: 실패테스트 — 로그인 성공 시 Success**

```kotlin
package com.opengaragelab.app.ui.login
import com.opengaragelab.app.fake.FakeAuthRepository
import com.opengaragelab.app.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @Before fun setup() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun tear() = Dispatchers.resetMain()

    @Test fun signIn_success_sets_Success_state() = runTest {
        val auth = FakeAuthRepository()
        val vm = LoginViewModel(auth)
        vm.onIdToken("token")
        advanceUntilIdle()
        assertTrue(vm.uiState.value is UiState.Success)
        assertTrue(auth.state.value != null)
    }
}
```

- [ ] **Step 2: 실행 (실패 확인)**

Run: `./gradlew :app:testDebugUnitTest --tests "*LoginViewModelTest*"`
Expected: FAIL (LoginViewModel 미존재 → 컴파일 에러)

- [ ] **Step 3: LoginViewModel 구현**

```kotlin
package com.opengaragelab.app.ui.login
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opengaragelab.app.data.AuthRepository
import com.opengaragelab.app.ui.common.UiState
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
```

- [ ] **Step 4: 실행 (PASS)**

Run: `./gradlew :app:testDebugUnitTest --tests "*LoginViewModelTest*"`
Expected: PASS

- [ ] **Step 5: LoginScreen 구현 (Credential Manager로 Google idToken 획득)**

`LoginScreen` 은 "Google로 시작" 버튼 → `CredentialManager.getCredential` 로 `GetGoogleIdOption(serverClientId = web_client_id)` 호출 → 받은 idToken을 `vm.onIdToken()` 에 전달. 표준 Credential Manager + GoogleIdTokenCredential 패턴 사용. `web_client_id` 는 `stringResource(R.string.web_client_id)`.

- [ ] **Step 6: AppRoot에 로그인 분기 연결 + 실기기 검증**

`currentUser == null` 이면 LOGIN, 아니면 탭 그래프. 실기기에서 Google 계정 선택 → FEED 진입 확인. Firestore `users/{uid}` 문서 생성 확인.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "[feat] Google 로그인 (Credential Manager) + LoginViewModel 테스트"
```

---

## Task 8: 내 차고 + 차 추가/수정

**Files:**
- Create: `ui/garage/GarageViewModel.kt`, `ui/garage/GarageScreen.kt`
- Create: `ui/garage/CarEditViewModel.kt`, `ui/garage/CarEditScreen.kt`
- Create: test `ui/garage/CarEditViewModelTest.kt`

**Interfaces:**
- Consumes: `CarRepository`, `AuthRepository.currentUser`, `PhotoRepository`(차 사진은 선택 — MVP는 사진 1장 optional, 생략 가능).
- Produces: `GarageViewModel.cars: StateFlow<List<Car>>`; `CarEditViewModel.save(make,model,year,nickname): suspend`, `saved: StateFlow<Boolean>`.

- [ ] **Step 1: 실패테스트 — 차 저장 시 store 반영 + modelKey 생성**

```kotlin
package com.opengaragelab.app.ui.garage
import com.opengaragelab.app.fake.FakeAuthRepository
import com.opengaragelab.app.fake.FakeCarRepository
import com.opengaragelab.app.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarEditViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun save_persists_car_with_modelKey_and_owner() = runTest {
        val cars = FakeCarRepository()
        val auth = FakeAuthRepository().apply { state.value = User("uid1","테스터","") }
        val vm = CarEditViewModel(cars, auth)
        vm.save("현대", "프라이드", 2005, "은마")
        advanceUntilIdle()
        val saved = cars.store.value.single()
        assertEquals("현대_프라이드", saved.modelKey)
        assertEquals("uid1", saved.ownerUid)
        assertTrue(vm.saved.value)
    }
}
```

- [ ] **Step 2: 실행 (실패)** — `./gradlew :app:testDebugUnitTest --tests "*CarEditViewModelTest*"` → FAIL

- [ ] **Step 3: CarEditViewModel 구현**

```kotlin
package com.opengaragelab.app.ui.garage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opengaragelab.app.data.AuthRepository
import com.opengaragelab.app.data.CarRepository
import com.opengaragelab.app.model.Car
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarEditViewModel @Inject constructor(
    private val cars: CarRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    val saved = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)
    fun save(make: String, model: String, year: Int, nickname: String) {
        viewModelScope.launch {
            val uid = auth.currentUser.first()?.uid ?: run { error.value = "로그인 필요"; return@launch }
            cars.upsertCar(Car(ownerUid = uid, make = make, model = model, year = year, nickname = nickname))
                .onSuccess { saved.value = true }
                .onFailure { error.value = it.message }
        }
    }
}
```

- [ ] **Step 4: 실행 (PASS)** — 동일 명령 → PASS

- [ ] **Step 5: GarageViewModel + 화면**

```kotlin
package com.opengaragelab.app.ui.garage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opengaragelab.app.data.AuthRepository
import com.opengaragelab.app.data.CarRepository
import com.opengaragelab.app.model.Car
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GarageViewModel @Inject constructor(
    cars: CarRepository,
    auth: AuthRepository,
) : ViewModel() {
    val cars: StateFlow<List<Car>> = auth.currentUser
        .filterNotNull()
        .flatMapLatest { cars.observeMyCars(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```
`GarageScreen`: 차 카드 LazyColumn + FAB(차 추가 → CAR_EDIT). 카드 탭 → CAR_DETAIL. `CarEditScreen`: make/model/year/nickname 입력 폼 + 저장 버튼(`vm.saved` true 시 popBack).

- [ ] **Step 6: 실기기 검증** — 차 추가 → 차고 리스트 즉시 반영(스냅샷 리스너). Firestore `cars` 문서 확인.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "[feat] 내 차고 + 차 추가/수정 + CarEditViewModel 테스트"
```

---

## Task 9: 기록 작성 (정비/주유 + 사진 업로드)

**Files:**
- Create: `ui/record/RecordEditViewModel.kt`, `ui/record/RecordEditScreen.kt`
- Create: test `ui/record/RecordEditViewModelTest.kt`

**Interfaces:**
- Consumes: `RecordRepository`, `PhotoRepository`, `CarRepository`(차 선택 목록), `AuthRepository`.
- Produces: `RecordEditViewModel.save(form: RecordForm, photoUris: List<Uri>)`; `RecordForm` data class; `saved: StateFlow<Boolean>`.

- [ ] **Step 1: RecordForm + 실패테스트 (FUEL은 isPublic 강제 false)**

```kotlin
package com.opengaragelab.app.ui.record
import com.opengaragelab.app.fake.*
import com.opengaragelab.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordEditViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    private fun vm(): Triple<RecordEditViewModel, FakeRecordRepository, FakeCarRepository> {
        val rec = FakeRecordRepository(); val car = FakeCarRepository()
        val auth = FakeAuthRepository().apply { state.value = User("uid1","테스터","") }
        val photo = FakePhotoRepository()
        car.store.value = listOf(Car("c1","uid1","현대","프라이드",2005,"현대_프라이드","은마"))
        return Triple(RecordEditViewModel(rec, photo, car, auth), rec, car)
    }

    @Test fun fuel_record_forced_private() = runTest {
        val (vm, rec, _) = vm()
        vm.save(RecordForm(carId="c1", type=RecordType.FUEL, title="주유", mileageKm=120000,
            cost=70000, liters=40.0, fuelType=FuelType.GASOLINE, isPublic=true), emptyList())
        advanceUntilIdle()
        assertFalse(rec.store.value.single().isPublic)   // 강제 false
    }

    @Test fun maintenance_record_keeps_public_and_modelKey() = runTest {
        val (vm, rec, _) = vm()
        vm.save(RecordForm(carId="c1", type=RecordType.MAINTENANCE, title="타이밍벨트", mileageKm=130000,
            cost=300000, isPublic=true), emptyList())
        advanceUntilIdle()
        val r = rec.store.value.single()
        assertTrue(r.isPublic)
        assertEquals("현대_프라이드", r.modelKey)   // 차에서 비정규화 복사
    }
}
```
> `FakePhotoRepository`: `uploadRecordPhotos` 가 빈 리스트 반환하는 단순 Fake — `fake/FakePhotoRepository.kt` 에 추가.
```kotlin
package com.opengaragelab.app.fake
import android.net.Uri
import com.opengaragelab.app.data.PhotoRepository
class FakePhotoRepository : PhotoRepository {
    override suspend fun uploadRecordPhotos(uid: String, recordId: String, uris: List<Uri>) =
        Result.success(emptyList<String>())
}
```

- [ ] **Step 2: 실행 (실패)** — `./gradlew :app:testDebugUnitTest --tests "*RecordEditViewModelTest*"` → FAIL

- [ ] **Step 3: RecordForm + RecordEditViewModel 구현**

```kotlin
package com.opengaragelab.app.ui.record
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opengaragelab.app.data.*
import com.opengaragelab.app.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordForm(
    val carId: String,
    val type: RecordType,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val mileageKm: Int = 0,
    val description: String = "",
    val cost: Long = 0L,
    val liters: Double? = null,
    val fuelType: FuelType? = null,
    val isPublic: Boolean = true,
)

@HiltViewModel
class RecordEditViewModel @Inject constructor(
    private val records: RecordRepository,
    private val photos: PhotoRepository,
    private val cars: CarRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    val saved = MutableStateFlow(false)
    val error = MutableStateFlow<String?>(null)

    fun save(form: RecordForm, photoUris: List<Uri>) {
        viewModelScope.launch {
            val user = auth.currentUser.first() ?: run { error.value = "로그인 필요"; return@launch }
            val car = cars.getCar(form.carId).getOrElse { error.value = "차량 없음"; return@launch }
            val base = Record(
                carId = form.carId, ownerUid = user.uid, ownerNickname = user.nickname,
                modelKey = car.modelKey, type = form.type, date = form.date,
                mileageKm = form.mileageKm, title = form.title, description = form.description,
                cost = form.cost, liters = form.liters, fuelType = form.fuelType,
                isPublic = if (form.type == RecordType.FUEL) false else form.isPublic,
            )
            val id = records.upsertRecord(base).getOrElse { error.value = it.message; return@launch }
            if (photoUris.isNotEmpty()) {
                photos.uploadRecordPhotos(user.uid, id, photoUris)
                    .onSuccess { urls -> records.upsertRecord(base.copy(recordId = id, photoUrls = urls)) }
            }
            saved.value = true
        }
    }
}
```

- [ ] **Step 4: 실행 (PASS)** — 동일 명령 → PASS

- [ ] **Step 5: RecordEditScreen** — 타입 토글(정비/주유), 차 드롭다운, 날짜/주행거리/금액/메모, 정비=공개스위치, 주유=주유량+유종, 사진은 `PickVisualMedia` 런처로 다중 선택. 저장 → `saved` 시 popBack.

- [ ] **Step 6: 실기기 검증** — 정비기록 작성→피드 노출 / 주유기록 작성→피드 미노출·차상세만 노출. 사진 Storage 업로드 확인.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "[feat] 기록 작성(정비/주유) + 사진 업로드 + ViewModel 테스트"
```

---

## Task 10: 차 상세 (정비+주유 타임라인)

**Files:**
- Create: `ui/garage/CarDetailViewModel.kt`, `ui/garage/CarDetailScreen.kt`
- Create: test `ui/garage/CarDetailViewModelTest.kt`

**Interfaces:**
- Consumes: `RecordRepository.observeCarRecords(carId)`, `CarRepository.getCar`.
- Produces: `CarDetailViewModel(savedStateHandle)` → `records: StateFlow<List<Record>>`, `car: StateFlow<Car?>`.

- [ ] **Step 1: 실패테스트 — carId의 정비+주유 모두 최신 date순**

```kotlin
package com.opengaragelab.app.ui.garage
import androidx.lifecycle.SavedStateHandle
import com.opengaragelab.app.fake.*
import com.opengaragelab.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After; import org.junit.Assert.*; import org.junit.Before; import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarDetailViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun shows_both_fuel_and_maintenance_for_car() = runTest {
        val rec = FakeRecordRepository()
        rec.store.value = listOf(
            Record("r1","c1", type=RecordType.MAINTENANCE, date=2, modelKey="현대_프라이드", isPublic=true),
            Record("r2","c1", type=RecordType.FUEL, date=1, isPublic=false),
            Record("r3","cX", type=RecordType.MAINTENANCE, date=9, isPublic=true),
        )
        val car = FakeCarRepository().apply { store.value = listOf(Car("c1","uid1","현대","프라이드")) }
        val vm = CarDetailViewModel(rec, car, SavedStateHandle(mapOf("carId" to "c1")))
        advanceUntilIdle()
        val list = vm.records.first { it.isNotEmpty() }
        assertEquals(listOf("r1","r2"), list.map { it.recordId })   // c1만, date desc
    }
}
```

- [ ] **Step 2: 실행 (실패)** → FAIL

- [ ] **Step 3: CarDetailViewModel 구현**

```kotlin
package com.opengaragelab.app.ui.garage
import androidx.lifecycle.*
import com.opengaragelab.app.data.CarRepository
import com.opengaragelab.app.data.RecordRepository
import com.opengaragelab.app.model.Car
import com.opengaragelab.app.model.Record
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarDetailViewModel @Inject constructor(
    records: RecordRepository,
    cars: CarRepository,
    handle: SavedStateHandle,
) : ViewModel() {
    private val carId: String = handle["carId"] ?: ""
    val records: StateFlow<List<Record>> =
        records.observeCarRecords(carId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val car = MutableStateFlow<Car?>(null)
    init { viewModelScope.launch { cars.getCar(carId).onSuccess { car.value = it } } }
}
```

- [ ] **Step 4: 실행 (PASS)** → PASS

- [ ] **Step 5: CarDetailScreen** — 상단 차 정보(애칭/연식/주행), 아래 기록 타임라인 LazyColumn. 정비/주유 칩으로 구분. FAB → RECORD_EDIT(carId 프리필). 정비카드 탭 → RECORD_DETAIL.

- [ ] **Step 6: 실기기 검증** — 차 상세에서 정비+주유 모두 보임.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "[feat] 차 상세 타임라인(정비+주유) + ViewModel 테스트"
```

---

## Task 11: 차종 피드

**Files:**
- Create: `ui/feed/FeedViewModel.kt`, `ui/feed/FeedScreen.kt`
- Create: test `ui/feed/FeedViewModelTest.kt`

**Interfaces:**
- Consumes: `RecordRepository.observeFeed(modelKey)`.
- Produces: `FeedViewModel.setModelKey(key)`, `records: StateFlow<List<Record>>`.

- [ ] **Step 1: 실패테스트 — 선택 modelKey의 공개기록만, 주유 제외**

```kotlin
package com.opengaragelab.app.ui.feed
import com.opengaragelab.app.fake.FakeRecordRepository
import com.opengaragelab.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After; import org.junit.Assert.*; import org.junit.Before; import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun feed_shows_only_public_maintenance_of_modelKey() = runTest {
        val rec = FakeRecordRepository()
        rec.store.value = listOf(
            Record("r1", modelKey="현대_프라이드", type=RecordType.MAINTENANCE, isPublic=true, createdAt=2),
            Record("r2", modelKey="현대_프라이드", type=RecordType.FUEL, isPublic=false, createdAt=3),
            Record("r3", modelKey="기아_프라이드", type=RecordType.MAINTENANCE, isPublic=true, createdAt=1),
        )
        val vm = FeedViewModel(rec)
        vm.setModelKey("현대_프라이드")
        advanceUntilIdle()
        val list = vm.records.first { it.isNotEmpty() }
        assertEquals(listOf("r1"), list.map { it.recordId })
    }
}
```

- [ ] **Step 2: 실행 (실패)** → FAIL

- [ ] **Step 3: FeedViewModel 구현**

```kotlin
package com.opengaragelab.app.ui.feed
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opengaragelab.app.data.RecordRepository
import com.opengaragelab.app.model.Record
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FeedViewModel @Inject constructor(
    records: RecordRepository,
) : ViewModel() {
    private val modelKey = MutableStateFlow("")
    fun setModelKey(key: String) { modelKey.value = key }
    val records: StateFlow<List<Record>> = modelKey
        .filter { it.isNotBlank() }
        .flatMapLatest { records.observeFeed(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

- [ ] **Step 4: 실행 (PASS)** → PASS

- [ ] **Step 5: FeedScreen** — 상단 차종 입력(제조사+모델 → `Car.makeModelKey` 로 변환 후 `setModelKey`). 결과 카드 LazyColumn(썸네일+제목+작성자닉+주행+날짜). 카드 탭 → RECORD_DETAIL. 빈 결과 시 "아직 기록 없음" 안내.

- [ ] **Step 6: 실기기 검증** — 차종 입력 → 해당 차종 공개 정비기록만 노출. 주유·타차종 미노출.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "[feat] 차종 피드 + FeedViewModel 테스트"
```

---

## Task 12: 기록 상세

**Files:**
- Create: `ui/record/RecordDetailViewModel.kt`, `ui/record/RecordDetailScreen.kt`
- Create: test `ui/record/RecordDetailViewModelTest.kt`

**Interfaces:**
- Consumes: `RecordRepository.getRecord(recordId)`.
- Produces: `RecordDetailViewModel(savedStateHandle)` → `state: StateFlow<UiState<Record>>`.

- [ ] **Step 1: 실패테스트**

```kotlin
package com.opengaragelab.app.ui.record
import androidx.lifecycle.SavedStateHandle
import com.opengaragelab.app.fake.FakeRecordRepository
import com.opengaragelab.app.model.Record
import com.opengaragelab.app.ui.common.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After; import org.junit.Assert.*; import org.junit.Before; import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordDetailViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun loads_record_by_id() = runTest {
        val rec = FakeRecordRepository().apply {
            store.value = listOf(Record("r1", title="타이밍벨트"))
        }
        val vm = RecordDetailViewModel(rec, SavedStateHandle(mapOf("recordId" to "r1")))
        advanceUntilIdle()
        val s = vm.state.first { it is UiState.Success }
        assertEquals("타이밍벨트", (s as UiState.Success).data.title)
    }
}
```

- [ ] **Step 2: 실행 (실패)** → FAIL

- [ ] **Step 3: 구현**

```kotlin
package com.opengaragelab.app.ui.record
import androidx.lifecycle.*
import com.opengaragelab.app.data.RecordRepository
import com.opengaragelab.app.model.Record
import com.opengaragelab.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordDetailViewModel @Inject constructor(
    records: RecordRepository,
    handle: SavedStateHandle,
) : ViewModel() {
    val state = MutableStateFlow<UiState<Record>>(UiState.Loading)
    init {
        val id: String = handle["recordId"] ?: ""
        viewModelScope.launch {
            records.getRecord(id)
                .onSuccess { state.value = UiState.Success(it) }
                .onFailure { state.value = UiState.Error(it.message ?: "불러오기 실패") }
        }
    }
}
```

- [ ] **Step 4: 실행 (PASS)** → PASS

- [ ] **Step 5: RecordDetailScreen** — 사진 캐러셀(HorizontalPager + Coil), 제목/작성자닉/주행/날짜/금액/내용. 본인 기록이면 수정·삭제 메뉴(옵션).

- [ ] **Step 6: 실기기 검증** — 피드/차상세에서 진입 → 상세 정상 표시.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "[feat] 기록 상세 + ViewModel 테스트"
```

---

## Task 13: 프로필 + 로그아웃

**Files:**
- Create: `ui/profile/ProfileViewModel.kt`, `ui/profile/ProfileScreen.kt`
- Create: test `ui/profile/ProfileViewModelTest.kt`

**Interfaces:**
- Consumes: `AuthRepository`, `RecordRepository.observeMyPublicRecords(uid)`.
- Produces: `ProfileViewModel.user`, `myRecords`, `signOut()`.

- [ ] **Step 1: 실패테스트 — 내 공개 정비기록만 노출 + 로그아웃 시 user null**

```kotlin
package com.opengaragelab.app.ui.profile
import com.opengaragelab.app.fake.*
import com.opengaragelab.app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After; import org.junit.Assert.*; import org.junit.Before; import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @Before fun s() = Dispatchers.setMain(StandardTestDispatcher())
    @After fun t() = Dispatchers.resetMain()

    @Test fun signOut_clears_user() = runTest {
        val auth = FakeAuthRepository().apply { state.value = User("uid1","테스터","") }
        val rec = FakeRecordRepository()
        val vm = ProfileViewModel(auth, rec)
        vm.signOut(); advanceUntilIdle()
        assertEquals(null, auth.state.value)
    }
}
```

- [ ] **Step 2: 실행 (실패)** → FAIL

- [ ] **Step 3: 구현**

```kotlin
package com.opengaragelab.app.ui.profile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.opengaragelab.app.data.AuthRepository
import com.opengaragelab.app.data.RecordRepository
import com.opengaragelab.app.model.Record
import com.opengaragelab.app.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    fun signOut() { viewModelScope.launch { auth.signOut() } }
}
```

- [ ] **Step 4: 실행 (PASS)** → PASS

- [ ] **Step 5: ProfileScreen** — 닉/사진(Coil), 내 공개 정비기록 리스트, 로그아웃 버튼(→ AppRoot이 LOGIN으로).

- [ ] **Step 6: 실기기 검증** — 로그아웃 → 로그인 화면 복귀.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "[feat] 프로필 + 로그아웃 + ViewModel 테스트"
```

---

## Task 14: Firestore/Storage 보안 규칙 + 인덱스

**Files:**
- Create: `firestore.rules`, `storage.rules`
- Create: `firestore.indexes.json` (복합 인덱스)

**Interfaces:**
- Produces: 백엔드 권한·인덱스. 배포 대상.

- [ ] **Step 1: firestore.rules**

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == uid;
    }
    match /cars/{carId} {
      allow read: if true;
      allow create: if request.auth != null && request.resource.data.ownerUid == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.ownerUid == request.auth.uid;
    }
    match /records/{recordId} {
      // 공개 기록은 누구나, 비공개(주유)는 본인만 읽기
      allow read: if resource.data.isPublic == true
                  || (request.auth != null && resource.data.ownerUid == request.auth.uid);
      allow create: if request.auth != null && request.resource.data.ownerUid == request.auth.uid;
      allow update, delete: if request.auth != null && resource.data.ownerUid == request.auth.uid;
    }
  }
}
```

- [ ] **Step 2: storage.rules**

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{uid}/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

- [ ] **Step 3: 복합 인덱스 생성**

피드/내기록 쿼리는 복합 인덱스 필요. 앱 첫 실행 시 Logcat에 뜨는 "create index" 링크 클릭이 가장 쉬움. 또는 `firestore.indexes.json`:
```json
{
  "indexes": [
    {"collectionGroup":"records","queryScope":"COLLECTION","fields":[
      {"fieldPath":"modelKey","order":"ASCENDING"},
      {"fieldPath":"isPublic","order":"ASCENDING"},
      {"fieldPath":"createdAt","order":"DESCENDING"}]},
    {"collectionGroup":"records","queryScope":"COLLECTION","fields":[
      {"fieldPath":"ownerUid","order":"ASCENDING"},
      {"fieldPath":"type","order":"ASCENDING"},
      {"fieldPath":"createdAt","order":"DESCENDING"}]},
    {"collectionGroup":"cars","queryScope":"COLLECTION","fields":[
      {"fieldPath":"ownerUid","order":"ASCENDING"},
      {"fieldPath":"createdAt","order":"DESCENDING"}]}
  ],
  "fieldOverrides": []
}
```

- [ ] **Step 4: 배포**

Firebase 콘솔에서 규칙 붙여넣기 → 게시. 또는 `firebase deploy --only firestore:rules,storage,firestore:indexes` (firebase-tools 설치 시).

- [ ] **Step 5: 검증** — 다른 계정으로 타인 주유기록 직접 접근 차단 확인. 피드 쿼리 인덱스 정상.

- [ ] **Step 6: Commit**

```bash
git add firestore.rules storage.rules firestore.indexes.json
git commit -m "[chore] Firestore/Storage 보안 규칙 + 복합 인덱스"
```

---

## Self-Review 결과

- **Spec coverage**: README의 IN 항목 8개 → Task 7(로그인), 8(차고/차추가), 9(정비+주유 작성), 11(차종피드), 12(상세), 9·4(사진 Storage), 13(프로필/로그아웃), 10(차 상세 주유 열람) 전부 매핑됨. 보안(Task 14)·모델(Task 2)·DI(Task 4) 추가.
- **Placeholder scan**: UI Composable 일부는 본문 코드 대신 구성 명세로 기술(테스트 불가 영역) — 단위테스트 대상(ViewModel)은 전부 실코드. 의도적 경계.
- **Type consistency**: `modelKey`(String), `isPublic`(Boolean), `RecordType`/`FuelType` enum, Repository 시그니처가 Fake·Impl·ViewModel 전반 일치 확인.
- **Firestore 주의**: enum은 `.name`(String)으로 저장됨 → 쿼리 시 `RecordType.MAINTENANCE.name` 사용(Task 4 반영). `toObject` 역직렬화는 enum 지원.

## OCR (2차, MVP 밖)
주유 기록 작성 화면에 영수증 사진 → ML Kit Text Recognition → 정규식으로 리터/유종/금액 파싱 후 `RecordForm` 프리필. 별도 spec·plan으로 진행.
