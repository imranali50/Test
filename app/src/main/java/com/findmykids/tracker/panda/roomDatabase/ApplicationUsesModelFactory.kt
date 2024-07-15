package com.findmykids.tracker.panda.roomDatabase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ApplicationUsesModelFactory(
    private val repository: ApplicationUsesRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            val constructor = modelClass.getDeclaredConstructor(ApplicationUsesRepository::class.java)
            return constructor.newInstance(repository)
        } catch (e: Exception) {
//            Log.e(e.message.toString())
        }
        return super.create(modelClass)
    }
}