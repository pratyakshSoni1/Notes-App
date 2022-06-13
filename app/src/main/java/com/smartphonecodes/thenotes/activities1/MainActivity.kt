package com.smartphonecodes.thenotes.activities1

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.smartphonecodes.thenotes.R
import com.smartphonecodes.thenotes.activities1.fragments.NotesEditorFragment
import com.smartphonecodes.thenotes.activities1.fragments.SetingsFragment
import com.smartphonecodes.thenotes.data.adapters.NotesPreviewAdapter
import com.smartphonecodes.thenotes.databinding.ActivityMainBinding
import com.smartphonecodes.thenotes.fragments.AllNotesHomeFragment

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TheNotes)

        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.mianBottomBar.background = null
        supportActionBar?.hide()
        binding.mianBottomBar.selectedItemId = R.id.homeOption
        supportFragmentManager.beginTransaction().replace(
            R.id.fragmentContainerFrameLayout,
            AllNotesHomeFragment()
        ).commit()

        binding.mianBottomBar.setOnItemSelectedListener { item ->
            bottomNavMenuListeners(item)
        }

    }

    companion object {
        var countBackPressed: Int = 0
    }

    override fun onBackPressed() {

        when {
            NotesPreviewAdapter.isSelectionMode -> {
                NotesPreviewAdapter.isSelectionMode = false
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerFrameLayout, AllNotesHomeFragment()).commit()
            }
            countBackPressed >= 1 -> {
                countBackPressed = 0
//                Toast.makeText(this, "finishing", Toast.LENGTH_SHORT).show()
                finish()
            }
            else -> {
                Log.d("Fragment", "I'm replaced : Home $countBackPressed")
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerFrameLayout, AllNotesHomeFragment()).commit()
                binding.mianBottomBar.selectedItemId = R.id.homeOption
                countBackPressed++
            }
        }

    }

    private fun bottomNavMenuListeners(item: MenuItem): Boolean {
        var used = true
        when (item.itemId) {
            R.id.homeOption -> {
                countBackPressed = 0
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerFrameLayout, AllNotesHomeFragment()).commit()
            }
            R.id.addNewNoteOption -> {
                countBackPressed = 0
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerFrameLayout, NotesEditorFragment()).commit()
            }
            R.id.settingsOption -> {
                countBackPressed = 0
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainerFrameLayout, SetingsFragment()).commit()
            }
            else -> {
                used = false
            }
        }

        return used
    }


    fun showDeleteDialog(
        dialogTitle: String,
        dialogMessage: String,
        positiveBtnText: String,
        positiveBtnListener: () -> Boolean,
        negativeBtnListener: () -> Boolean
    ) {

        val deleteDialog = Dialog(this)
        deleteDialog.setContentView(R.layout.delete_dialog_fragment)
        deleteDialog.findViewById<TextView>(R.id.deleDialogTitle).text = dialogTitle
        deleteDialog.findViewById<TextView>(R.id.deleteDialogMessage).text = dialogMessage
        deleteDialog.findViewById<Button>(R.id.dialogButtonDelete).text = positiveBtnText

        deleteDialog.findViewById<Button>(R.id.dialogButtonDelete).setOnClickListener {
            positiveBtnListener()
            deleteDialog.dismiss()

        }

        deleteDialog.findViewById<Button>(R.id.dialogButtonCancel).setOnClickListener {
            //do nothing as cancel is cancel
            negativeBtnListener()
            deleteDialog.dismiss()
        }

        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        deleteDialog.setCancelable(true)

        deleteDialog.show()

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}