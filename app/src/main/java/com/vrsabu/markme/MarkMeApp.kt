package com.vrsabu.markme

import android.app.Application
import com.vrsabu.markme.data.remote.RetrofitInstance
import com.vrsabu.markme.data.repository.AuthRepository

class MarkMeApp : Application() {
    // Expose a single AuthRepository instance for the app
    val authRepository: AuthRepository by lazy { AuthRepository() }

    override fun onCreate() {
        super.onCreate()
        // Initialize repository prefs
        AuthRepository.init(applicationContext)
        // Initialize Retrofit (builds OkHttp client with auth interceptor)
        RetrofitInstance.init(applicationContext)
    }
}
