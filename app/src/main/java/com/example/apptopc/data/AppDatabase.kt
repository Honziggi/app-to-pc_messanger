package com.example.apptopc.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatMessage:: class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nitro_chat_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun insert(context: Context, text: String) {
            getDatabase(context).chatDao().insertMessage(ChatMessage(text = text))
        }
    }
}