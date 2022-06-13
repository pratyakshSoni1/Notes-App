package com.smartphonecodes.thenotes.activities1.fragments

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smartphonecodes.thenotes.R
import com.smartphonecodes.thenotes.activities1.MainActivity
import com.smartphonecodes.thenotes.data.database.NotesDatabase
import com.smartphonecodes.thenotes.databinding.FragmentSetingsBinding
import com.smartphonecodes.thenotes.fragments.AllNotesHomeFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class SetingsFragment : Fragment() {

    private var _binding: FragmentSetingsBinding? = null
    private val binding get() = _binding!!

    companion object {
        val Context.theme_mode_datastore by preferencesDataStore(name = "notes_current_theme_data")
        val KEY_THEME_DATASTORE = stringPreferencesKey(name = "NOTES_CURRENTTHEME_KEY")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSetingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runBlocking {
            when {
                requireActivity().theme_mode_datastore.data.first()[KEY_THEME_DATASTORE] == "DARK" -> {
                    toggleThemeSwitch(true)
                }
                requireActivity().theme_mode_datastore.data.first()[KEY_THEME_DATASTORE] == "LIGHT" -> {
                    toggleThemeSwitch(false)
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


        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->

            CoroutineScope(Dispatchers.Main).launch {

                delay(450)

                if (isChecked) {
                    requireActivity().theme_mode_datastore.edit { dataFlow ->
                        dataFlow[KEY_THEME_DATASTORE] = "DARK"
                    }
                    onSwitchStateChanged(true)
                } else if (!isChecked) {

                    requireActivity().theme_mode_datastore.edit { dataFlow ->
                        dataFlow[KEY_THEME_DATASTORE] = "LIGHT"
                    }
                    onSwitchStateChanged(false)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error while changing theme !",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }

        binding.deleteAllNotes.setOnClickListener {

            val positiveBtnLambda: () -> Boolean = {
                val notesDbInterface = NotesDatabase.getNotesDbInstance(requireContext())
                CoroutineScope(Dispatchers.IO).launch {
                    notesDbInterface.clearAllTables()
                }
                true
            }

            val negativeBtnLambda: () -> Boolean = {
                //do nothing
                true
            }

            (activity as MainActivity).showDeleteDialog(
                "Delete All !",
                "Delete all notes available",
                "Delete",
                positiveBtnLambda, negativeBtnLambda
            )


        }

        binding.resetApp.setOnClickListener {
            val positiveBtnLambda: () -> Boolean = {
                (activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).clearApplicationUserData()
                requireActivity().supportFragmentManager.beginTransaction().replace(
                    R.id.fragmentContainerFrameLayout,
                    AllNotesHomeFragment()
                ).commit()
                true
            }
            val negativeBtnLambda: () -> Boolean = {
                //do nothing :)
                true
            }

            (activity as MainActivity).showDeleteDialog(
                "Reset App",
                "This will delete all data of app and \nwill reset app as New one.\nYou will have to restart the App",
                "Reset",
                positiveBtnLambda, negativeBtnLambda
            )
        }

    }

    private fun toggleThemeSwitch(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            binding.themeSwitch.isChecked = true
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            binding.themeSwitch.isChecked = false
        }
    }

    private fun onSwitchStateChanged(isDark: Boolean) {
        if (isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        toggleThemeSwitch(isDark)
        activity?.findViewById<BottomNavigationView>(R.id.mianBottomBar)?.selectedItemId = R.id.homeOption

    }

}


