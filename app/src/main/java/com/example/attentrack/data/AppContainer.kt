package com.example.attentrack.data

import android.content.Context
import com.example.attentrack.BuildConfig
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Chhoto manual dependency container (Hilt lagbe na). */
class AppContainer(context: Context) {

    val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    val sessionStore = SessionStore(context.applicationContext)
    val credentialStore = CredentialStore(context.applicationContext)

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            // OkHttp background thread e cholay, tai runBlocking ekhane thik ache
            val token = runBlocking { sessionStore.token() }
            val builder = chain.request().newBuilder().header("Accept", "application/json")
            if (token != null) builder.header("Authorization", "Bearer $token")
            chain.proceed(builder.build())
        }
        .apply {
            if (BuildConfig.DEBUG) {
                // BASIC: shudhu URL + status log hoy, password/token body te log hobe na
                addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            }
        }
        .build()

    private val baseUrl: String = BuildConfig.BASE_URL.let { if (it.endsWith("/")) it else "$it/" }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val api: ApiService = retrofit.create(ApiService::class.java)

    val authRepository = AuthRepository(api, sessionStore, gson)
    val teacherRepository = TeacherRepository(api, gson)
    val studentRepository = StudentRepository(api, gson)
}
