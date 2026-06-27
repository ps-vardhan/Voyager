package com.example.voyagers.di

import android.content.Context
import androidx.room.Room
import com.example.voyagers.data.local.AppDatabase
import com.example.voyagers.data.local.ContactDao
import com.example.voyagers.data.local.TripDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.example.voyagers.data.local.SmsLogDao

/**
 * Hilt module that provides Database and DAO singletons.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "voyagers_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideContactDao(db: AppDatabase): ContactDao {
        return db.contactDao()
    }

    @Provides
    @Singleton
    fun provideTripDao(db: AppDatabase): TripDao {
        return db.tripDao()
    }

    @Provides
    @Singleton
    fun provideSmsLogDao(db: AppDatabase): SmsLogDao {
        return db.smsLogDao()
    }
}
