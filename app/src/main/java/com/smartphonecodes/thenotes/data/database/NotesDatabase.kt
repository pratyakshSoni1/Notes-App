package com.smartphonecodes.thenotes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.smartphonecodes.thenotes.data.daos.NotesDao
import com.smartphonecodes.thenotes.data.entities.NotesEntity

@Database( entities = [NotesEntity::class] , version = 2)
abstract class NotesDatabase: RoomDatabase() {

    abstract val notesDao:NotesDao

    companion object{
        @Volatile
        private var NOTESDB_INSTANCE : NotesDatabase?= null

        fun getNotesDbInstance(context:Context):NotesDatabase{
            synchronized(this){
                return NOTESDB_INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NotesDatabase::class.java,
                   "notes_db"
                ).build().also {
                    NOTESDB_INSTANCE = it
                }
            } }
    }

}