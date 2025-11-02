package com.habitmind.di

import android.content.Context
import androidx.room.Room
import com.habitmind.BuildConfig
import com.habitmind.data.local.HabitDatabase
import com.habitmind.data.remote.GeminiApiService
import com.habitmind.data.remote.OpenAIApiService
import com.habitmind.data.repository.AIRepository
import com.habitmind.data.repository.HabitRepository
import com.habitmind.data.repository.HabitRepositoryImpl
import com.habitmind.debug.OpenAIDebugger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHabitDatabase(@ApplicationContext context: Context): HabitDatabase {
        return Room.databaseBuilder(
            context,
            HabitDatabase::class.java,
            "habit_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideHabitRepository(database: HabitDatabase): HabitRepository {
        return HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitLogDao = database.habitLogDao()
        )
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("openai")
    fun provideOpenAIRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Named("gemini")
    fun provideGeminiRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenAIApiService(@Named("openai") retrofit: Retrofit): OpenAIApiService {
        return retrofit.create(OpenAIApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(@Named("gemini") retrofit: Retrofit): GeminiApiService {
        return retrofit.create(GeminiApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("openai_key")
    fun provideOpenAIApiKey(): String {
        return BuildConfig.OPENAI_API_KEY
    }

    @Provides
    @Singleton
    @Named("gemini_key")
    fun provideGeminiApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    @Provides
    @Singleton
    fun provideAIRepository(
        openAIApiService: OpenAIApiService,
        geminiApiService: GeminiApiService,
        @Named("openai_key") openAIApiKey: String,
        @Named("gemini_key") geminiApiKey: String
    ): AIRepository {
        return AIRepository(openAIApiService, geminiApiService, openAIApiKey, geminiApiKey)
    }

    @Provides
    @Singleton
    fun provideOpenAIDebugger(@Named("openai_key") apiKey: String): OpenAIDebugger {
        return OpenAIDebugger(apiKey)
    }
}

