package com.pndnwngi.billumaba.di

import android.content.Context
import androidx.room.Room
import com.pndnwngi.billumaba.data.database.AppDatabase
import com.pndnwngi.billumaba.data.database.dao.MenuDao
import com.pndnwngi.billumaba.data.database.dao.VisitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bill_umaba_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideVisitDao(appDatabase: AppDatabase): VisitDao {
        return appDatabase.visitDao()
    }

    @Provides
    @Singleton
    fun provideMenuDao(appDatabase: AppDatabase): MenuDao {
        return appDatabase.menuDao()
    }
}
