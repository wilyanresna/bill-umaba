package com.pndnwngi.billumaba.di

import com.pndnwngi.billumaba.data.repository.CulinaryRepository
import com.pndnwngi.billumaba.data.repository.CulinaryRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCulinaryRepository(
        culinaryRepositoryImpl: CulinaryRepositoryImpl
    ): CulinaryRepository
}
