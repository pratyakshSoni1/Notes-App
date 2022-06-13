package com.smartphonecodes.thenotes.data.entities

import android.text.Spannable
import android.text.SpannableString
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NotesEntity(
    @PrimaryKey(autoGenerate = true)
    val noteId:Long,
    val noteDateTime:String,
    val noteTitle:String,
    val noteContent: String,
)