package pr.com.kotlin_nyff.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/**
 * Telefonda saqlangan buyurtma (tarix o'chirilmaydi). `token` — kuzatish tokeni: faqat buyurtma berilgan javobda
 * keladi, yo'qolsa buyurtmani kuzatib bo'lmaydi.
 */
@Entity(tableName = "orders")
data class SavedOrderEntity(
    @PrimaryKey val id: String,
    val token: String,
    /** Buyurtma berilgan vaqt, epoch ms — ro'yxat tartibi. */
    val createdAt: Long,
    /** Buyurtma berilgandagi hudud narxi (dostavka); `null` — narxni kuryer aytadi yoki olib ketish. */
    val deliveryFee: Long?,
    /** Serverdan olingan oxirgi holat — `CustomerOrder` JSON. */
    val snapshot: String,
    val updatedAt: Long,
)

@Dao
interface SavedOrderDao {

    /** Eng yangi [limit] ta — tarix sahifalab o'qiladi (hammasi birdan xotiraga olinmaydi). */
    @Query("SELECT * FROM orders ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SavedOrderEntity>>

    @Query("SELECT COUNT(*) FROM orders")
    fun observeCount(): Flow<Int>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun observe(id: String): Flow<SavedOrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun get(id: String): SavedOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: SavedOrderEntity)

    @Query("UPDATE orders SET snapshot = :snapshot, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSnapshot(id: String, snapshot: String, updatedAt: Long)
}

@Database(entities = [SavedOrderEntity::class], version = 1, exportSchema = true)
abstract class NyffDatabase : RoomDatabase() {
    abstract fun orders(): SavedOrderDao
}
