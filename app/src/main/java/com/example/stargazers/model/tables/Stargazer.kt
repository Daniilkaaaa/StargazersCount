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
    tableName = "stargazer",
    foreignKeys = [
        ForeignKey(
            entity = Repo::class,
            parentColumns = ["id"],
            childColumns = ["id_repo"]
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["id_user"]
        )
    ],

)
data class Stargazer(
    val id_repo: Int,
    val id_user: Long,
    val starred_at: String,
    @PrimaryKey(autoGenerate = true) val id:Int? = null
)

@Dao
interface StargazerDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(stargazery: Stargazer)

    @Query("SELECT * FROM stargazer")
    fun getAll(): LiveData<List<Stargazer>>

    @Query("SELECT * FROM stargazer")
    fun getAllFlow(): Flow<List<Stargazer>>

    @Query("SELECT * FROM stargazer WHERE id_repo = :id_repository")
    fun getByIdRepository(id_repository: Int): Flow<List<Stargazer>>

    @Query("SELECT * FROM stargazer WHERE id_repo = :id_repository")
    fun getByIdRepositoryLiveData(id_repository: Int): LiveData<List<Stargazer>>

    @Query("SELECT * FROM stargazer")
    fun getAllForWork(): Flow<List<Stargazer>>

    @Query("DELETE FROM stargazer")
    suspend fun clear()

    @Query("DELETE FROM stargazer WHERE id_repo = :id_repository")
    suspend fun deleteByIdRepository(id_repository: Int)
}

