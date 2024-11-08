package com.example.githubstars.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.githubstars.model.tables.Repo
import com.example.githubstars.model.tables.RepositoryDao
import com.example.githubstars.model.tables.Stargazer
import com.example.githubstars.model.tables.StargazerDao
import com.example.githubstars.model.tables.User
import com.example.githubstars.model.tables.UserDao

@Database(entities = [User::class, Repo::class, Stargazer::class],  version = 3, exportSchema = false)
abstract class DB : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun repositoryDao(): RepositoryDao
    abstract fun stargazerDao(): StargazerDao

    companion object {
        @Volatile
        private var INSTANCE: DB? = null

        fun getDatabase(context: Context): DB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DB::class.java,
                    "app-database"
                ).addMigrations(MIGRATION_2_3).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE user")
        database.execSQL("""CREATE TABLE user (
            name TEXT NOT NULL,
            url TEXT NOT NULL,
            avatar TEXT NOT NULL,
            id INTEGER PRIMARY KEY AUTOINCREMENT
        )"""
        )
        database.execSQL("DROP TABLE stargazer")
        database.execSQL("DROP TABLE repository")
        database.execSQL("""
            CREATE TABLE repo (
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_owner INTEGER NOT NULL,
                url TEXT NOT NULL,
                FOREIGN KEY (id_owner) REFERENCES user(id) ON DELETE NO ACTION ON UPDATE NO ACTION
)
        """)
        //FOREIGN KEY (id_owner) REFERENCES user(id) ON UPDATE CASCADE ON DELETE CASCADE
        database.execSQL("""
            CREATE TABLE stargazer (
                id_repo INTEGER NOT NULL,
                id_user INTEGER NOT NULL,
                starred_at TEXT NOT NULL,
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                FOREIGN KEY (id_user) REFERENCES user(id) ON DELETE NO ACTION ON UPDATE NO ACTION,
                FOREIGN KEY (id_repo) REFERENCES repo(id) ON DELETE NO ACTION ON UPDATE NO ACTION
            )
        """)
//        FOREIGN KEY (id_repository) REFERENCES repository(id) ON UPDATE CASCADE ON DELETE CASCADE,
//        FOREIGN KEY (id_user) REFERENCES user(id) ON UPDATE CASCADE ON DELETE CASCADE
    }
}
