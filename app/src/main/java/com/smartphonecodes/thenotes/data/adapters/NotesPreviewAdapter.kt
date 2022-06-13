package com.smartphonecodes.thenotes.data.adapters

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.smartphonecodes.thenotes.R
import com.smartphonecodes.thenotes.activities1.MainActivity
import com.smartphonecodes.thenotes.activities1.fragments.NotesEditorFragment
import com.smartphonecodes.thenotes.data.database.NotesDatabase
import com.smartphonecodes.thenotes.data.entities.NotesEntity
import com.smartphonecodes.thenotes.fragments.AllNotesHomeFragment

class NotesPreviewAdapter(val notes:List<NotesEntity>, val context:FragmentActivity?, private val fragmentHome:AllNotesHomeFragment):RecyclerView.Adapter<NotesPreviewAdapter.NotesPreviewViewHolder>() {

    companion object {
        const val KEY_SAVED_NOTE_CONTENT = "savedNoteContentKey"
        const val KEY_SAVED_NOTE_TITTLE = "savedNoteTittleKey"
        const val KEY_SAVED_NOTE_DATE = "savedNoteDateKey"
        const val KEY_SAVED_NOTE_ID = "savedNoteIdKey"
        var isSelectionMode = false
        val selectedNotesIdList = mutableListOf<NotesEntity>()

    }

    inner class NotesPreviewViewHolder(view:View):RecyclerView.ViewHolder(view){
        val notesTitle:TextView=view.findViewById(R.id.notesTitlePreview)
        val notesTime:TextView=view.findViewById(R.id.notesTimeDatePreview)
        val notesContent:TextView=view.findViewById(R.id.notesContentPreview)
        val theNote:ConstraintLayout=view.findViewById(R.id.theNotePreview)
        val selectionLayer:ImageView=view.findViewById(R.id.selectionLayer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesPreviewViewHolder {
        return NotesPreviewViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.notes_main_preview,parent,false)
        )
    }

    override fun onBindViewHolder(holder: NotesPreviewViewHolder, position: Int) {

        Log.d("ADAPTER","data update again bind")
        holder.notesTitle.text = notes[position].noteTitle
        holder.notesTime.text = notes[position].noteDateTime
        holder.selectionLayer.visibility = View.INVISIBLE
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N) {
            holder.notesContent.setText(Html.fromHtml(notes[position].noteContent,Html.FROM_HTML_MODE_LEGACY))
        }else{
            holder.notesContent.setText(Html.fromHtml(notes[position].noteContent))
        }

        holder.theNote.setOnClickListener {

            if(isSelectionMode){

                if(selectedNotesIdList.contains(notes[position])) {
                    holder.selectionLayer.visibility = View.INVISIBLE
                    selectedNotesIdList.remove(notes[position])
                    Log.d("Selection_Mode","$selectedNotesIdList")
                }else{
                    holder.selectionLayer.visibility = View.VISIBLE
                    selectedNotesIdList.add(notes[position])
                    Log.d("Selection_Mode","$selectedNotesIdList")
                }
                afterSelectionUiFormalities()
            }else {

                val noteDisplayDataBundle = Bundle()
                val editorFragment = NotesEditorFragment()
                noteDisplayDataBundle.putString(KEY_SAVED_NOTE_CONTENT, notes[position].noteContent)
                noteDisplayDataBundle.putString(KEY_SAVED_NOTE_TITTLE, notes[position].noteTitle)
                noteDisplayDataBundle.putString(KEY_SAVED_NOTE_DATE, notes[position].noteDateTime)
                noteDisplayDataBundle.putLong(KEY_SAVED_NOTE_ID, notes[position].noteId)
                editorFragment.arguments = noteDisplayDataBundle

                MainActivity.countBackPressed = 0
                context?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.fragmentContainerFrameLayout, editorFragment)?.commit()
            }
        }

        holder.theNote.setOnLongClickListener {

            if(selectedNotesIdList.contains(notes[position])) {
                holder.selectionLayer.visibility = View.INVISIBLE
                selectedNotesIdList.remove(notes[position])
                Log.d("Selection_Mode","$selectedNotesIdList")
            }else{
                holder.selectionLayer.visibility = View.VISIBLE
                selectedNotesIdList.add(notes[position])
                Log.d("Selection_Mode","$selectedNotesIdList")
            }
            afterSelectionUiFormalities()

            true
        }

    }

    private fun afterSelectionUiFormalities(){
        isSelectionMode = !selectedNotesIdList.isNullOrEmpty()
        fragmentHome.setSelectionModeConfiguration()
        fragmentHome.toggleShareButtonVisibility(selectedNotesIdList.size==1)
    }

    suspend fun deleteSelectedItems(){
        val notesDbInterface = NotesDatabase.getNotesDbInstance(context!!)
        val notesDao = notesDbInterface.notesDao

        notesDao.deleteGivenNotes(selectedNotesIdList)
        Log.d("DELETENOTES","NOTES DELETED !")

    }

    fun cancelSelectionMode(){
        isSelectionMode = false
        selectedNotesIdList.clear()
        notifyDataSetChanged()
        fragmentHome.setSelectionModeConfiguration()

    }

    override fun getItemCount(): Int {
        return notes.size
    }





}