package com.smartphonecodes.thenotes.data.daos

import androidx.room.*
import com.smartphonecodes.thenotes.data.entities.NotesEntity

@Dao
interface NotesDao {

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    suspend fun addNote(note:NotesEntity)

    @Transaction
    @Query("SELECT * FROM NotesEntity WHERE noteId =:queryNoteId")
    suspend fun getNoteFromId(queryNoteId:Long):NotesEntity

    @Transaction
    @Query("SELECT * FROM NotesEntity")
    suspend fun getAllNotes():List<NotesEntity>

    @Delete()
    suspend fun deleteGivenNotes(notesToDelete:List<NotesEntity>)

    @Transaction
    @Query("SELECT * FROM NotesEntity WHERE noteTitle LIKE '%'|| :nameToSearch ||'%' ")
    suspend fun searchByName(nameToSearch:String):List<NotesEntity>


}