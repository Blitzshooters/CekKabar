package com.tanilink.cekkabar.di

import android.content.Context
import com.tanilink.cekkabar.BuildConfig
import com.tanilink.cekkabar.data.local.KeystoreManager
import com.tanilink.cekkabar.data.local.LocalHistoryDatabase
import com.tanilink.cekkabar.data.remote.CekKabarApi
import com.tanilink.cekkabar.data.repository.CekKabarRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api: CekKabarApi = retrofit.create(CekKabarApi::class.java)
    private val appContext = context.applicationContext
    private val keystoreManager = KeystoreManager(appContext)
    private val localHistoryDatabase = LocalHistoryDatabase(appContext, keystoreManager)

    val repository: CekKabarRepository = CekKabarRepository(api, localHistoryDatabase, appContext)
}
