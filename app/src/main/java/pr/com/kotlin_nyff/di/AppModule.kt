package pr.com.kotlin_nyff.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pr.com.kotlin_nyff.BuildConfig
import pr.com.kotlin_nyff.data.local.NyffDatabase
import pr.com.kotlin_nyff.data.local.SavedOrderDao
import pr.com.kotlin_nyff.data.remote.NyffApi
import pr.com.kotlin_nyff.data.remote.NyffJson
import pr.com.kotlin_nyff.domain.ServerClock
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/** Ilova yashaydigan doira — ekran yopilsa ham tugashi kerak bo'lgan ishlar uchun (buyurtmani saqlash). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nyff")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addNetworkInterceptor { chain ->
            val response = chain.proceed(chain.request())
            response.headers.getDate("Date")?.let { ServerClock.update(it.time) }
            response
        }
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    @Provides
    @Singleton
    fun api(http: OkHttpClient): NyffApi = Retrofit.Builder()
        .baseUrl(BuildConfig.API_URL + "/")
        .client(http)
        .addConverterFactory(NyffJson.asConverterFactory("application/json; charset=UTF-8".toMediaType()))
        .build()
        .create(NyffApi::class.java)

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): NyffDatabase =
        Room.databaseBuilder(context, NyffDatabase::class.java, "nyff.db").build()

    @Provides
    fun orderDao(database: NyffDatabase): SavedOrderDao = database.orders()

    @Provides
    @Singleton
    fun dataStore(@ApplicationContext context: Context): DataStore<Preferences> = context.dataStore
}
