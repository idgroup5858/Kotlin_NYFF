package pr.com.kotlin_nyff.courier.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import pr.com.kotlin_nyff.BuildConfig
import pr.com.kotlin_nyff.courier.data.remote.AuthInterceptor
import pr.com.kotlin_nyff.courier.data.remote.CourierApi
import pr.com.kotlin_nyff.data.remote.NyffJson
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/** Kuryer qismining DataStore'i (sessiya, tanlangan filial) — mijoz sozlamalaridan alohida fayl. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CourierStore

private val Context.courierDataStore: DataStore<Preferences> by preferencesDataStore(name = "nyff_courier")

/**
 * Kuryer qismi tarmog'i: mijoz OkHttp'i ustiga token qo'shuvchi [AuthInterceptor] — mijoz so'rovlariga (ochiq
 * endpointlar) kuryer tokeni yuborilmaydi.
 */
@Module
@InstallIn(SingletonComponent::class)
object CourierModule {

    @Provides
    @Singleton
    fun courierApi(base: OkHttpClient, auth: AuthInterceptor): CourierApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL + "/")
        .client(base.newBuilder().addInterceptor(auth).build())
        .addConverterFactory(NyffJson.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
        .build()
        .create(CourierApi::class.java)

    @Provides
    @Singleton
    @CourierStore
    fun courierDataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.courierDataStore
}
