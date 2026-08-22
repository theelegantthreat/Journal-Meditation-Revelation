package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.WisdomQuote
import kotlinx.coroutines.flow.Flow

@Dao
interface WisdomDao {

    @Query("SELECT * FROM wisdom_quotes ORDER BY timestamp DESC")
    fun getAllWisdom(): Flow<List<WisdomQuote>>

    @Query("SELECT * FROM wisdom_quotes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteWisdom(): Flow<List<WisdomQuote>>

    @Query("SELECT * FROM wisdom_quotes WHERE journalEntryId = :entryId ORDER BY timestamp DESC")
    fun getWisdomForEntry(entryId: Long): Flow<List<WisdomQuote>>

    @Query("SELECT * FROM wisdom_quotes WHERE id = :id")
    fun getWisdomById(id: Long): Flow<WisdomQuote?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWisdom(quote: WisdomQuote): Long

    @Update
    suspend fun updateWisdom(quote: WisdomQuote)

    @Delete
    suspend fun deleteWisdom(quote: WisdomQuote)

    @Query("DELETE FROM wisdom_quotes WHERE id = :id")
    suspend fun deleteWisdomById(id: Long)

    @Query("SELECT COUNT(*) FROM wisdom_quotes")
    fun getWisdomCount(): Flow<Int>
}
