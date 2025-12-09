package com.vrsabu.markme.data.remote

import android.content.Context
import com.vrsabu.markme.data.remote.api.ApiService
import com.vrsabu.markme.data.repository.AuthRepository
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://api.mark-me.in/"

    // Backing field for the ApiService; initialized in init(context)
    private var _api: ApiService? = null

    val api: ApiService
        get() = _api ?: throw IllegalStateException("RetrofitInstance not initialized. Call RetrofitInstance.init(context) in Application.onCreate()")

    /**
     * Initialize Retrofit with an OkHttp client that attaches Authorization header when available.
     * Call this once from Application.onCreate().
     */
    fun init(context: Context) {
        // Ensure AuthRepository prefs are initialized as well (safe to call again)
        try {
            AuthRepository.init(context)
        } catch (_: Exception) {
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()

            // Read token from AuthRepository (reads SharedPreferences)
            val token = AuthRepository().getAccessToken()
            if (!token.isNullOrEmpty()) {
                builder.addHeader("Authorization", "Bearer $token")
            }

            builder.addHeader("Accept", "application/json")
            val request = builder.build()
            chain.proceed(request)
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // Certificate pinning placeholder - enable and set your pins for production
        /*
        val certificatePinner = CertificatePinner.Builder()
            .add("your.backend.domain", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
            .build()
        clientBuilder.certificatePinner(certificatePinner)
        */

        val client = clientBuilder.build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        _api = retrofit.create(ApiService::class.java)
    }
}