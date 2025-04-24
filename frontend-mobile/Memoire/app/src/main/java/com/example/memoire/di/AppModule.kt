//package com.example.memoire.di
//
//import android.content.Context
//import androidx.work.ListenableWorker
//import androidx.work.WorkerFactory
//import androidx.work.WorkerParameters
//import com.example.memoire.utils.NotificationUtils
//
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.android.qualifiers.ApplicationContext
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Module
//@InstallIn(SingletonComponent::class)
//object HiltWorkModule {
//    @Provides
//    @Singleton
//    fun provideWorkerFactory(notificationUtils: NotificationUtils): HiltWorkerFactory {
//        return HiltWorkerFactory(notificationUtils)
//    }
//}
//
//class HiltWorkerFactory @Inject constructor(
//    private val notificationUtils: NotificationUtils
//) : WorkerFactory() {
//    override fun createWorker(
//        appContext: Context,
//        workerClassName: String,
//        workerParameters: WorkerParameters
//    ): ListenableWorker? {
//        return null // Return your worker instances if you have any
//    }
//}