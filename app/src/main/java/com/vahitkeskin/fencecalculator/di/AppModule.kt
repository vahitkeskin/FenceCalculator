package com.vahitkeskin.fencecalculator.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class) // Bu modül tüm uygulama ömrü boyunca yaşar
object AppModule {

    // Profesyonel projelerde Dispatcher'ları inject etmek,
    // test yazarken (Unit Test) IO thread yerine Test thread kullanabilmeyi sağlar.

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    // Örnek: İleride SharedPreferences veya başka bir Context gerektiren sınıf eklemek isterseniz:
    /*
    @Singleton
    @Provides
    fun provideSharedPrefs(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("fence_prefs", Context.MODE_PRIVATE)
    }
    */
}

// Dispatcher'ları ayırt etmek için özel Annotation'lar (Qualifier)
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher