package com.lab.opengarage.di

import com.lab.opengarage.data.AuthRepository
import com.lab.opengarage.data.AuthRepositoryImpl
import com.lab.opengarage.data.CarRepository
import com.lab.opengarage.data.CarRepositoryImpl
import com.lab.opengarage.data.PhotoRepository
import com.lab.opengarage.data.PhotoRepositoryImpl
import com.lab.opengarage.data.RecordRepository
import com.lab.opengarage.data.RecordRepositoryImpl
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
