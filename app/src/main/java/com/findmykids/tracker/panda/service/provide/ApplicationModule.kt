package com.findmykids.tracker.panda.service.provide

import android.app.Application
import android.app.NotificationManager
import android.location.LocationManager
import com.findmykids.tracker.panda.service.notification.LocationNotification
import com.google.android.gms.location.FusedLocationProviderClient
import com.findmykids.tracker.panda.service.util.AndroidLocationProvider
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Singleton
    @Provides
    fun provideLocationManager(application: Application): LocationManager =
        application.getSystemService(LocationManager::class.java)

    @Singleton
    @Provides
    fun provideNotificationManager(application: Application): NotificationManager =
        application.getSystemService(NotificationManager::class.java)

    @Singleton
    @Provides
    fun provideLocationNotification(
        application: Application,
        notificationManager: NotificationManager,
    ): LocationNotification = LocationNotification(
        context = application,
        manager = notificationManager
    )

    @Provides
    @Singleton
    fun provideAndroidLocationProvider(
        application: Application,
        locationManager: LocationManager,
        fusedLocationProviderClient: FusedLocationProviderClient,
    ): AndroidLocationProvider = AndroidLocationProvider(
        context = application,
        manager = locationManager,
        client = fusedLocationProviderClient
    )

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(application: Application): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)


}