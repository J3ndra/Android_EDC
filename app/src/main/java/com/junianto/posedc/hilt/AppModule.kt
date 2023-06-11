package com.junianto.posedc.hilt

import android.app.Application
import android.content.Context
import com.junianto.posedc.database.repository.TransactionRepository
import com.junianto.posedc.menu.sale.viewmodel.SaleViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}