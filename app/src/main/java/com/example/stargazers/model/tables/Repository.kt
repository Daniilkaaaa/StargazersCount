package com.example.githubstars.model.tables

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "repo",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["id_owner"]
        )
    ]
)
data class Repo(
    val id_owner: Int,
    val url: String,
    val name: String,
    val description: String,
    @PrimaryKey(autoGenerate = true) val id:Int? = null
)

@Dao
interface RepositoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(repository: Repo): Long

    @Query("SELECT * FROM Repo WHERE url = :url LIMIT 1")
    fun getByUrl(url: String): Repo?

    @Query("SELECT * FROM repo")
    fun getAllLiveData(): LiveData<List<Repo>>

    @Query("SELECT * FROM repo")
    fun getAllFlow(): Flow<List<Repo>>

    @Query("SELECT * FROM repo")
    fun getAllForWork(): Flow<List<Repo>>

    @Query("SELECT * FROM repo WHERE id = :id")
    fun getById(id: Int): LiveData<Repo>

    @Query("SELECT * FROM repo WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Repo>

    @Query("DELETE FROM repo")
    suspend fun clear()

    @Query("DELETE FROM repo WHERE id = :id")
    suspend fun deleteById(id: Int)
}



