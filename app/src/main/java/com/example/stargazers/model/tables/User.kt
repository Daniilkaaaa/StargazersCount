package com.example.githubstars.model.tables

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "user",
)
data class User(
    val name: String,
    val url: String,
    val avatar: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(authorRepository: User): Long

    @Query("SELECT * FROM user")
    fun getAll(): LiveData<List<User>>

    @Query("SELECT * FROM user")
    fun getAllFlow(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE id = :id")
    fun getById(id: Int): Flow<User>

    @Query("SELECT * FROM user WHERE id = :id")
    fun getByIdLiveData(id: Int): LiveData<User>

    @Query("DELETE FROM user")
    suspend fun clear()

    @Query("DELETE FROM user WHERE id = :id")
    suspend fun deleteById(id: Int)
}
