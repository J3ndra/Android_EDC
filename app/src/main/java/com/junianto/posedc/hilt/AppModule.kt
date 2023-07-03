package com.junianto.posedc.hilt

import android.app.Activity
import android.app.Application
import android.content.Context
import com.junianto.posedc.util.BluetoothPermissionHelper
import com.junianto.posedc.util.NfcTagReader
import com.junianto.posedc.util.NfcTagReaderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideNfcTagReader(@ApplicationContext context: Context): NfcTagReader {
        return NfcTagReaderImpl()
    }

    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}