package com.junianto.posedc.hilt

import android.app.Activity
import com.junianto.posedc.util.BluetoothPermissionHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object BluetoothModule {
    @Provides
    fun provideBluetoothPermissionHelper(activity: Activity): BluetoothPermissionHelper {
        return BluetoothPermissionHelper(activity)
    }
}