package com.smartphonecodes.thenotes.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.addTextChangedListener
import androidx.datastore.preferences.core.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.smartphonecodes.thenotes.activities1.MainActivity
import com.smartphonecodes.thenotes.activities1.fragments.SetingsFragment.Companion.KEY_THEME_DATASTORE
import com.smartphonecodes.thenotes.activities1.fragments.SetingsFragment.Companion.theme_mode_datastore
import com.smartphonecodes.thenotes.data.adapters.NotesPreviewAdapter
import com.smartphonecodes.thenotes.data.daos.NotesDao
import com.smartphonecodes.thenotes.data.database.NotesDatabase
import com.smartphonecodes.thenotes.data.entities.NotesEntity
import com.smartphonecodes.thenotes.databinding.FragmentAllNotesHomeBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class AllNotesHomeFragment : Fragment() {

    private var _binding: FragmentAllNotesHomeBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAllNotesHomeBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runBlocking {
            when {
                requireActivity().theme_mode_datastore.data.first()[KEY_THEME_DATASTORE] == null -> {
                    setThemeForFirst()
                }
                requireActivity().theme_mode_datastore.data.first()[KEY_THEME_DATASTORE] == "DARK" -> {
                    setCurrentAppsTheme(true)
                }
                requireActivity().theme_mode_datastore.data.first()[KEY_THEME_DATASTORE] == "LIGHT" -> {
                    setCurrentAppsTheme(false)
                    Log.d("PROCESS_APPTHEME", "DATASTORE READ LIGHT")
                }
                else -> {
                    Toast.makeText(
                        requireContext(),
                        "Error While setting Theme :(",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("PROCESS_APPTHEME", "DataStore error cannot set theme in onCreateView")
                }
            }
        }

        val notesDb = NotesDatabase.getNotesDbInstance(requireContext())
        val notesDao = notesDb.notesDao
        var adapterNew: NotesPreviewAdapter = getUpdatedAdapterData(notesDao)
        adapterNew.cancelSelectionMode()
        checkNotesAvailability(adapterNew)

        fun resetRecyclerView(notesDaoInstance: NotesDao) {
            adapterNew = getUpdatedAdapterData(notesDaoInstance)
            binding.publicNotesRecyclerView.adapter = adapterNew
            checkNotesAvailability(adapterNew)
        }

        binding.publicNotesSearch.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                adapterNew.cancelSelectionMode()
                binding.cancelSearchBtn.visibility = View.VISIBLE
            } else {
                binding.cancelSearchBtn.visibility = View.INVISIBLE
            }
        }

        binding.publicNotesSearch.addTextChangedListener {
            val searchData = filterMyListOfNotes(it!!, notesDao)
            if (!it.isNullOrEmpty()) {
                Log.d("SEARCH : $it", searchData.toString())
                adapterNew = NotesPreviewAdapter(searchData, activity, this@AllNotesHomeFragment)
                Log.d("SEARCH", "${adapterNew.notes.size}")

                binding.publicNotesRecyclerView.adapter = adapterNew
                if (adapterNew.notes.isEmpty()) {
                    binding.nothingFoundIllustrateLayout.visibility = View.VISIBLE
                } else {
                    binding.nothingFoundIllustrateLayout.visibility = View.INVISIBLE
                }
            } else {
//                resetRecyclerView(notesDao)
            }
        }

        binding.cancelSearchBtn.setOnClickListener {
            binding.nothingFoundIllustrateLayout.visibility = View.INVISIBLE
            binding.publicNotesSearch.setText("")
            binding.publicNotesSearch.clearFocus()

            (activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                getView()?.windowToken,
                0
            )

            resetRecyclerView(notesDao)
            adapterNew.cancelSelectionMode()
            setSelectionModeConfiguration()

        }

        binding.shareNoteBtn.setOnClickListener {
            val noteTittle: String = NotesPreviewAdapter.selectedNotesIdList[0].noteTitle
            val noteContent: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                (Html.fromHtml(
                    NotesPreviewAdapter.selectedNotesIdList[0].noteContent,
                    Html.FROM_HTML_MODE_LEGACY
                )
                        ).toString()
            } else {
                (Html.fromHtml(
                    NotesPreviewAdapter.selectedNotesIdList[0].noteContent
                )).toString()
            }

            val noteToshare = "$noteTittle .\n\n$noteContent"

            val shareIntent = Intent(Intent.ACTION_SEND)
                .setType("text/html")
                .putExtra(Intent.EXTRA_TEXT, noteToshare)

            if (activity?.packageManager?.resolveActivity(shareIntent, 0) != null) {
                startActivity(shareIntent)
            }

        }


        binding.deleteNoteBtn.setOnClickListener {

            Log.d("DELETE", "Button clicked!")

            val positiveBtnLamda: () -> Boolean = {
                Log.d("DELETE", "deleting enter")
                runBlocking {
                    adapterNew.deleteSelectedItems()
                }
                NotesPreviewAdapter.isSelectionMode = false
                adapterNew = getUpdatedAdapterData(notesDao)
                adapterNew.notifyDataSetChanged()
                setSelectionModeConfiguration()
                Log.d("DELETE", "deleting done")
                true
            }

            val negativeBtnLambda: () -> Boolean = {
                adapterNew.cancelSelectionMode()
                true
            }

            (activity as MainActivity?)!!.showDeleteDialog(
                "Delete Notes !",
                "Delete all notes you have selected ?",
                "Delete",
                positiveBtnLamda, negativeBtnLambda
            )

        }


        binding.cancelDeleteNoteBtn.setOnClickListener {
            adapterNew.cancelSelectionMode()
        }
    }


    private fun getUpdatedAdapterData(notesDao: NotesDao): NotesPreviewAdapter {
        var adapterNew: NotesPreviewAdapter
        runBlocking {

            adapterNew =
                NotesPreviewAdapter(notesDao.getAllNotes(), activity, this@AllNotesHomeFragment)
            binding.publicNotesRecyclerView.layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            binding.publicNotesRecyclerView.adapter = adapterNew
            checkNotesAvailability(adapterNew)

        }
        return adapterNew
    }


    private fun checkNotesAvailability(adapter: NotesPreviewAdapter) {
        if (adapter.itemCount == 0) {
            binding.nothingFoundIllustrateLayout.visibility = View.VISIBLE
            binding.publicNotesRecyclerView.visibility = View.INVISIBLE
        } else {
            binding.nothingFoundIllustrateLayout.visibility = View.INVISIBLE
            binding.publicNotesRecyclerView.visibility = View.VISIBLE
        }
    }

    private suspend fun setThemeForFirst() {

        activity?.theme_mode_datastore?.edit { dataPreferncesForTheme ->
            dataPreferncesForTheme[KEY_THEME_DATASTORE] = "DARK"
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

    }

    private fun setCurrentAppsTheme(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    fun setSelectionModeConfiguration() {
        if (NotesPreviewAdapter.isSelectionMode) {
            binding.deleteNoteBtn.visibility = View.VISIBLE
            binding.cancelDeleteNoteBtn.visibility = View.VISIBLE
        } else {
            binding.deleteNoteBtn.visibility = View.INVISIBLE
            binding.cancelDeleteNoteBtn.visibility = View.INVISIBLE
        }
    }

    fun toggleShareButtonVisibility(shouldBeVisible: Boolean) {
        if (shouldBeVisible) {
            binding.shareNoteBtn.visibility = View.VISIBLE
        } else {
            binding.shareNoteBtn.visibility = View.INVISIBLE
        }
    }


    private fun filterMyListOfNotes(
        typedChars: CharSequence,
        notesDaoInst: NotesDao
    ): List<NotesEntity> {
        val filteredList = mutableListOf<NotesEntity>()

        runBlocking {
            filteredList.addAll(notesDaoInst.searchByName(typedChars.toString()))
        }
        return filteredList
    }


}