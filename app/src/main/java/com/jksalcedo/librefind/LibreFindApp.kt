package com.jksalcedo.librefind

import android.app.Application
import com.jksalcedo.librefind.di.appModule
import com.jksalcedo.librefind.di.networkModule
import com.jksalcedo.librefind.di.repositoryModule
import com.jksalcedo.librefind.di.supabaseModule
import com.jksalcedo.librefind.di.useCaseModule
import com.jksalcedo.librefind.di.viewModelModule
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.jksalcedo.librefind.ui.common.CoilConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class LibreFindApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@LibreFindApp)
            modules(appModule, networkModule, repositoryModule, useCaseModule, viewModelModule, supabaseModule)
        }
    }

    override fun newImageLoader(): ImageLoader {
        return CoilConfig.createImageLoader(this)
    }
}
