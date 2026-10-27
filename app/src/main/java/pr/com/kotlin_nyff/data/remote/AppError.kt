package pr.com.kotlin_nyff.data.remote

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/** JSON sozlamasi — Retrofit, socket va Room surati uchun bitta. */
val NyffJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
    encodeDefaults = true
}

/** Ilova ko'rsatadigan xato. Matn — `ui/common/ErrorText.kt` da (`code` bo'yicha, o'z tilida). */
sealed class AppError(message: String?, cause: Throwable? = null) : Exception(message, cause) {

    /**
     * Backend xatosi `{ statusCode, code, message, detail }`. `code` bo'lmasa — NestJS / Express'ning o'z xatosi
     * (404 yo'l, 500): umumiy "Xatolik yuz berdi". `detail` foydalanuvchiga ko'rsatilmaydi.
     */
    class Server(val status: Int, val code: String?, val detail: String?) : AppError("HTTP $status ${code.orEmpty()}")

    /** Server javob bermadi — so'rov yetib borgan-bormagani noma'lum. */
    class Network(cause: Throwable) : AppError(cause.message, cause)

    /** Javobni o'qib bo'lmadi (server bilan ilova turlari mos emas). */
    class Unexpected(cause: Throwable) : AppError(cause.message, cause)

    val serverCode: String? get() = (this as? Server)?.code
}

@Serializable
private data class ErrorBody(val statusCode: Int = 0, val code: String? = null, val detail: String? = null)

/** Retrofit chaqiruvini o'rab, har qanday xatoni [AppError] ga aylantiradi. */
suspend fun <T> apiCall(block: suspend () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    throw e.toAppError()
} catch (e: IOException) {
    throw AppError.Network(e)
} catch (e: SerializationException) {
    throw AppError.Unexpected(e)
} catch (e: IllegalArgumentException) {
    // kotlinx.serialization ba'zi xatolarni IllegalArgumentException qilib beradi
    throw AppError.Unexpected(e)
}

internal fun HttpException.toAppError(): AppError.Server {
    val raw = runCatching { response()?.errorBody()?.string() }.getOrNull().orEmpty()
    val body = runCatching { NyffJson.decodeFromString<ErrorBody>(raw) }.getOrNull()
    return AppError.Server(status = code(), code = body?.code, detail = body?.detail)
}
