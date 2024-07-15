package com.findmykids.tracker.panda.service.bind

import com.findmykids.tracker.panda.service.util.AndroidLocationProvider
import com.findmykids.tracker.panda.service.util.LocationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationModule {

    @Binds
    @Singleton
    abstract fun bindLocationProvider(androidLocationProvider: AndroidLocationProvider): LocationProvider
}