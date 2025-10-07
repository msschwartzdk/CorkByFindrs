package com.example.corkbyfindrs.data

import android.content.Context
import com.example.corkbyfindrs.data.UserSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUserSettingsRepository(
        @ApplicationContext context: Context
    ): UserSettingsRepository {
        return UserSettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideServerClient(
        @ApplicationContext context: Context,
        userSettingsRepository: UserSettingsRepository
    ): ServerClient {
        return ServerClient(context, userSettingsRepository)
    }
}