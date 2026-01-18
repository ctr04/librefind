package com.jksalcedo.librefind

import android.app.Application
import com.jksalcedo.librefind.di.appModule
import com.jksalcedo.librefind.di.networkModule
import com.jksalcedo.librefind.di.repositoryModule
import com.jksalcedo.librefind.di.useCaseModule
import com.jksalcedo.librefind.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LibreFindApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LibreFindApp)
            modules(appModule, networkModule, repositoryModule, useCaseModule, viewModelModule)
        }
    }
}
