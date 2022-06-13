package com.smartphonecodes.thenotes.activities1.fragments

import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.smartphonecodes.thenotes.R
import com.smartphonecodes.thenotes.data.adapters.NotesPreviewAdapter
import com.smartphonecodes.thenotes.data.database.NotesDatabase
import com.smartphonecodes.thenotes.data.entities.NotesEntity
import com.smartphonecodes.thenotes.databinding.FragmentNotesEditorBinding
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class NotesEditorFragment : Fragment() {

    private var _binding: FragmentNotesEditorBinding? = null
    private val binding get() = _binding!!
    private val fragmentEditorLogs = "FRAGMENT_LOG"
    private var isEditingMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotesEditorBinding.inflate(inflater, container, false)
        val bottomBar = activity?.findViewById<BottomNavigationView>(R.id.mianBottomBar)
        bottomBar?.visibility = View.GONE

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.actionBar?.hide()

        if (this.arguments != null) {
            switchEditingMode(false)
            binding.notesTitleText.setText(arguments?.getString(NotesPreviewAdapter.KEY_SAVED_NOTE_TITTLE))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                binding.notesContentText.setText(
                    Html.fromHtml(
                        arguments?.getString(
                            NotesPreviewAdapter.KEY_SAVED_NOTE_CONTENT
                        ), Html.FROM_HTML_MODE_LEGACY )
                )
            } else {
                binding.notesContentText.setText(
                    Html.fromHtml(
                        arguments?.getString(
                            NotesPreviewAdapter.KEY_SAVED_NOTE_CONTENT
                        ) )
                )
            }

            binding.dateCreated.text = arguments?.getString(NotesPreviewAdapter.KEY_SAVED_NOTE_DATE)
        } else {
            binding.dateCreated.text = getTimeOfSavingNote()
        }

        binding.saveNoteButton.setOnClickListener { saveNote() }
        binding.cancelNoteButton.setOnClickListener { cancelNote() }

        binding.optionBold.setOnClickListener { formatBold() }
        binding.optionItalic.setOnClickListener { formatItalic() }
        binding.optionUnderline.setOnClickListener { formatUnderline() }
        binding.optionStrikeThrough.setOnClickListener { formatStrikeThrough() }

        binding.notesContentText.setOnClickListener {
            uncheckAllOptions()
            if (!binding.notesContentText.text.isNullOrEmpty()) {
                onCursorChanged(
                    binding.notesContentText.selectionStart - 1,
                    binding.notesContentText.selectionEnd
                )
            }
        }

        binding.notesContentText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                switchEditingMode(true)
            }
        }

        binding.notesTitleText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                switchEditingMode(true)
            }
        }


    }

    //text formatting & checkbox functionality :

    private fun uncheckAllOptions() {

        binding.optionBold.isChecked = false
        binding.optionItalic.isChecked = false
        binding.optionUnderline.isChecked = false
        binding.optionStrikeThrough.isChecked = false

    }


    private fun onCursorChanged(
        selectionStart: Int,
        selectionEnd: Int,
        boldSpan: Boolean = false,
        italicSpan: Boolean = false,
        underlineSpan: Boolean = false,
        strikethroughSpan: Boolean = false
    ) {
        val start = if (selectionStart < 0) { selectionStart + 1  } else { selectionStart  }
        val end = if (selectionStart < 0) { selectionEnd + 1 } else { selectionEnd  }
        Log.d("SPAN", "CHECKING IN : $start - $end")
        val spanText = SpannableString(binding.notesContentText.text?.subSequence(start, end))
        Log.d("SPAN", "CHECKING IN :$spanText $start - $end")

        var boldSpanChecked = boldSpan
        var italicSpanChecked = italicSpan
        var underlineSpanChecked = underlineSpan
        var strikethroughSpanChecked = strikethroughSpan

        val styleSpans = spanText.getSpans(0, spanText.length, StyleSpan::class.java)


        if (!spanText.getSpans(0, spanText.length, StyleSpan::class.java)
                .isNullOrEmpty() && !(italicSpanChecked || boldSpanChecked)
        ) {
            if (styleSpans[0].style == Typeface.BOLD_ITALIC) {
                binding.optionBold.isChecked = true
                binding.optionItalic.isChecked = true
                italicSpanChecked = true
                boldSpanChecked = true
                Log.d("SPAN", "Bold & Italic FOUND")
            } else if (styleSpans[0].style == Typeface.ITALIC) {
                binding.optionItalic.isChecked = true
                Log.d("SPAN", "Italic FOUND")
                italicSpanChecked = true
            } else if (styleSpans[0].style == Typeface.BOLD) {
                binding.optionBold.isChecked = true
                Log.d("SPAN", "Bold FOUND")
                boldSpanChecked = true
            }
            onCursorChanged(
                start,
                end,
                boldSpanChecked,
                italicSpanChecked,
                underlineSpanChecked,
                strikethroughSpanChecked
            )
        } else if (!spanText.getSpans(0, spanText.length, UnderlineSpan::class.java)
                .isNullOrEmpty() && !underlineSpanChecked
        ) {
            binding.optionUnderline.isChecked = true
            Log.d("SPAN", "Underline FOUND")
            underlineSpanChecked = true
            onCursorChanged(
                start,
                end,
                boldSpanChecked,
                italicSpanChecked,
                underlineSpanChecked,
                strikethroughSpanChecked
            )
        } else if (!spanText.getSpans(0, spanText.length, StrikethroughSpan::class.java)
                .isNullOrEmpty() && !strikethroughSpanChecked
        ) {
            binding.optionStrikeThrough.isChecked = true
            Log.d("SPAN", "Strike FOUND")
            strikethroughSpanChecked = true
            onCursorChanged(
                start,
                end,
                boldSpanChecked,
                italicSpanChecked,
                underlineSpanChecked,
                strikethroughSpanChecked
            )
        } else if (!underlineSpanChecked || !boldSpanChecked || !italicSpanChecked || !strikethroughSpanChecked) {
            //Do Nothing i.e. all options are set now our fuction will terminate this time
            Log.d("SPAN", "All Options Updated FOUND")
        } else {
            uncheckAllOptions()
            Log.d("SPAN", "No Span FOUND")
        }
    }

    private fun <T> applyFormattingOnSelectedText(formatType: T, start: Int, end: Int) {

        Log.d("SPAN", "Appliying on selected : $start - $end")

        when (formatType) {
            StyleSpan(Typeface.BOLD).style -> {
                Log.d("SPAN", "Process BOLD : $start - $end")
                val spanText =
                    SpannableString(binding.notesContentText.text?.subSequence(start, end))
                val styleSpans = spanText.getSpans(0, spanText.length, StyleSpan::class.java)
                if (!spanText.getSpans(0, spanText.length, StyleSpan::class.java).isNullOrEmpty()) {
                    if (styleSpans[0].style == Typeface.ITALIC) {
                        Log.d("SPAN", "Appliying BOLD - Italic : $start - $end")
                        removeSpansFromText(
                            SpannableString(binding.notesContentText.text), start, end,
                            StyleSpan(Typeface.ITALIC)::class.java
                        )
                        binding.notesContentText.text?.setSpan(
                            StyleSpan(Typeface.BOLD_ITALIC), start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        Log.d("SPAN", "Appliying BOLD : $start - $end")
                        binding.notesContentText.text?.setSpan(
                            StyleSpan(Typeface.BOLD), start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                } else {
                    Log.d("SPAN", "Appliying BOLD : $start - $end")
                    binding.notesContentText.text?.setSpan(
                        StyleSpan(Typeface.BOLD),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            StyleSpan(Typeface.ITALIC).style -> {
                Log.d("SPAN", "Appliying ITALIC : $start - $end")
                val spanText =
                    SpannableString(binding.notesContentText.text?.subSequence(start, end))
                val styleSpans = spanText.getSpans(0, spanText.length, StyleSpan::class.java)
                if (!spanText.getSpans(0, spanText.length, StyleSpan::class.java).isNullOrEmpty()) {
                    if (styleSpans[0].style == Typeface.BOLD) {
                        Log.d("SPAN", "Appliying BOLD - Italic : $start - $end")
                        removeSpansFromText(
                            SpannableString(binding.notesContentText.text), start, end,
                            StyleSpan(Typeface.BOLD)::class.java
                        )
                        binding.notesContentText.text?.setSpan(
                            StyleSpan(Typeface.BOLD_ITALIC), start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        binding.notesContentText.text?.setSpan(
                            StyleSpan(Typeface.ITALIC), start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                } else {
                    binding.notesContentText.text?.setSpan(
                        StyleSpan(Typeface.ITALIC), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            StrikethroughSpan().spanTypeId -> {
                Log.d("SPAN", "Appliying STRIKE : $start - $end")
                binding.notesContentText.text?.setSpan(
                    StrikethroughSpan(),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            UnderlineSpan().spanTypeId -> {
                Log.d("SPAN", "Appliying Underline : $start - $end")
                binding.notesContentText.text?.setSpan(
                    UnderlineSpan(),
                    start,
                    end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun formatBold() {

        Log.d(
            "SPAN",
            "BOLD : ${binding.optionBold.isChecked} , ${binding.notesContentText.selectionStart - binding.notesContentText.selectionEnd} "
        )
        if (binding.optionBold.isChecked) {
            if (binding.notesContentText.selectionEnd - binding.notesContentText.selectionStart > 2) {
                applyFormattingOnSelectedText(
                    StyleSpan(Typeface.BOLD).style,
                    binding.notesContentText.selectionStart,
                    binding.notesContentText.selectionEnd
                )
            }
        } else if (!binding.optionBold.isChecked && binding.optionItalic.isChecked) {
            removeSpansFromText(
                SpannableString(binding.notesContentText.text),
                binding.notesContentText.selectionStart,
                binding.notesContentText.selectionEnd,
                StyleSpan(Typeface.BOLD_ITALIC)::class.java
            )
            binding.notesContentText.text?.setSpan(
                StyleSpan(Typeface.ITALIC),
                binding.notesContentText.selectionStart,
                binding.notesContentText.selectionEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (!binding.optionBold.isChecked) {
            if (binding.notesContentText.selectionEnd - binding.notesContentText.selectionStart > 2) {
                formatNormalise(
                    SpannableString(binding.notesContentText.text),
                    binding.notesContentText.selectionStart,
                    binding.notesContentText.selectionEnd,
                    StyleSpan(Typeface.BOLD)::class.java
                )
            }
        }
    }

    private fun formatItalic() {
        val start = binding.notesContentText.selectionStart
        val end = binding.notesContentText.selectionEnd
        Log.d("SPAN", "ITALIC : ${binding.optionItalic.isChecked}")
        Log.d("SPAN", "ITALIC : Selection $start - $end")
        if (binding.optionItalic.isChecked) {
            if ((end - start) > 1) {
                applyFormattingOnSelectedText(StyleSpan(Typeface.ITALIC).style, start, end)
            } else if ((binding.notesContentText.selectionEnd - binding.notesContentText.selectionStart) <= 1) {
                Log.d("SPAN", "No Selection")
                if (binding.notesContentText.text?.get(end - start)?.equals(" ") == true) {
                    Log.d("SPAN", "Yes Space")
                    binding.notesContentText.text!!.setSpan(
                        StyleSpan(Typeface.ITALIC), end, end + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
                    binding.notesContentText.text?.insert(end, " ")
                    Log.d("SPAN", "No Space : adding at ${binding.notesContentText.text?.get(end)}")
                    binding.notesContentText.text!!.setSpan(
                        StyleSpan(Typeface.ITALIC), end, end + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.notesContentText.append(" ")
                    binding.notesContentText.setSelection(end + 1, end + 1)
                    Log.d("SPAN", "Itallic Apply on : $end - ${end + 1}")

                }
            }
        } else if (!binding.optionItalic.isChecked && binding.optionBold.isChecked) {
            removeSpansFromText(
                SpannableString(binding.notesContentText.text), start, end,
                StyleSpan(Typeface.BOLD_ITALIC)::class.java
            )
            binding.notesContentText.text?.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else if (!binding.optionItalic.isChecked) {
            if ((end - start) > 1) {
                formatNormalise(
                    SpannableString(binding.notesContentText.text), start, end,
                    StyleSpan(Typeface.ITALIC)::class.java
                )
            } else if ((binding.notesContentText.selectionEnd - binding.notesContentText.selectionStart) <= 1) {
                Log.d("SPAN", "No Selection")
                if (binding.notesContentText.text?.get(end - start)?.equals(" ") == true) {
                    Log.d("SPAN", "Yes Space")
                    binding.notesContentText.text!!.setSpan(
                        StyleSpan(Typeface.ITALIC), end, end + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
//                    Log.d("SPAN","No Space : adding at ${binding.notesContentText.text?.get(end)}")
//                    binding.notesContentText.text!!.setSpan(StyleSpan(Typeface.ITALIC),end-1,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    Log.d("SPAN", "Removing Itallic Apply on : ${end} - ${end + 1}")
                    binding.notesContentText.text?.insert(end, " ")
                    removeSpansFromText(
                        SpannableString(binding.notesContentText.text), end, end + 1,
                        StyleSpan::class.java
                    )
                    binding.notesContentText.setSelection(end + 1, end + 1)
                }
            }
        }
    }

    private fun formatUnderline() {

        Log.d("SPAN", "Underline : ${binding.optionBold.isChecked}")

        if (binding.optionUnderline.isChecked) {
            if (binding.notesContentText.hasSelection()) {
                onCursorChanged(
                    binding.notesContentText.selectionStart - 1,
                    binding.notesContentText.selectionEnd
                )
                if (binding.optionUnderline.isChecked) {
                    applyFormattingOnSelectedText(
                        UnderlineSpan().spanTypeId,
                        binding.notesContentText.selectionStart,
                        binding.notesContentText.selectionEnd
                    )
                }
            }
        } else if (!binding.optionUnderline.isChecked) {
            if (binding.notesContentText.hasSelection()) {
                formatNormalise(
                    SpannableString(binding.notesContentText.text),
                    binding.notesContentText.selectionStart,
                    binding.notesContentText.selectionEnd,
                    UnderlineSpan::class.java
                )
            }
        }
    }

    private fun formatStrikeThrough() {
        Log.d("SPAN", "StrikeThrough : ${binding.optionBold.isChecked}")

        if (binding.optionStrikeThrough.isChecked) {
            if (binding.notesContentText.hasSelection()) {
                applyFormattingOnSelectedText(
                    StrikethroughSpan().spanTypeId,
                    binding.notesContentText.selectionStart,
                    binding.notesContentText.selectionEnd
                )
            }
        } else if (!binding.optionStrikeThrough.isChecked) {
            if (binding.notesContentText.hasSelection()) {
                formatNormalise(
                    SpannableString(binding.notesContentText.text),
                    binding.notesContentText.selectionStart,
                    binding.notesContentText.selectionEnd,
                    StrikethroughSpan::class.java
                )
            }
        }
    }


    private fun <T> formatNormalise(text: Spannable, start: Int, end: Int, spanType: Class<T>) {
        Log.d("SPAN", "NORMAL : $start to $end")
        if (end - start >= 2) {
            val prefix = text.subSequence(0, start)
            val suffix = text.subSequence(end, text.length)
            val textToEdit = SpannableString(text.subSequence(start, end))
            val spanToRemove = textToEdit.getSpans(0, textToEdit.length, spanType)
            textToEdit.removeSpan(spanToRemove[0])
            binding.notesContentText.setText(prefix)
            binding.notesContentText.append(textToEdit)
            binding.notesContentText.append(suffix)
            binding.notesContentText.setSelection(start, end)
        }
    }
    fun <T> removeSpansFromText(fullText: Spannable, start: Int, end: Int, type: Class<T>) {
        val prefixText = fullText.subSequence(0, start)
        val textToEdit = SpannableString(fullText.subSequence(start, end))
        val suffixText = fullText.subSequence(end, fullText.length)
        val spanToRemove = textToEdit.getSpans(0, textToEdit.length, type)
        textToEdit.removeSpan(spanToRemove[0])

        Log.d("SPAN REMOVED", "from - $textToEdit")
        binding.notesContentText.setText(prefixText)
        binding.notesContentText.append(textToEdit)
        binding.notesContentText.append(suffixText)
        binding.notesContentText.setSelection(start, end)
    }


// other important functions like saving , cancel , etc :-

    override fun onStop() {
        super.onStop()
        Log.d(fragmentEditorLogs, "I am Stop")
        val bottomBar = activity?.findViewById<BottomNavigationView>(R.id.mianBottomBar)
        bottomBar?.visibility = View.GONE
    }

    override fun onPause() {
        Log.d(fragmentEditorLogs, "I am Pause")
        super.onPause()
    }

    override fun onResume() {
        Log.d(fragmentEditorLogs, "I am Resume")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d(fragmentEditorLogs, "I am Destroyed")
        val bottomBar = activity?.findViewById<BottomNavigationView>(R.id.mianBottomBar)
        bottomBar?.visibility = View.VISIBLE
        super.onDestroy()
    }

    private fun switchEditingMode(switchOn: Boolean) {
        isEditingMode = switchOn
        if (isEditingMode) {
            binding.cancelNoteButton.visibility = View.VISIBLE
            binding.saveNoteButton.visibility = View.VISIBLE
            binding.textFormattingOptionsCard.visibility = View.VISIBLE
        } else {
            binding.cancelNoteButton.visibility = View.INVISIBLE
            binding.saveNoteButton.visibility = View.INVISIBLE
            binding.textFormattingOptionsCard.visibility = View.INVISIBLE
        }
    }

    private fun noteValidToSave(): Boolean {
        return !(binding.notesTitleText.text.isNullOrEmpty() && binding.notesContentText.text.isNullOrEmpty())
    }

    private fun getTimeOfSavingNote(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("EEE MMM d yyyy", Locale.getDefault())
        val date = formatter.format(calendar.time)
        return date

    }

    private fun processDataBaseSaving(): NotesEntity {
        return if (this.arguments != null) {

            val noteID = arguments?.getLong(NotesPreviewAdapter.KEY_SAVED_NOTE_ID)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotesEntity(
                    noteID!!, getTimeOfSavingNote(), binding.notesTitleText.text.toString(),
                    Html.toHtml(binding.notesContentText.text, Html.FROM_HTML_MODE_LEGACY)
                )
            } else {
                NotesEntity(
                    noteID!!, getTimeOfSavingNote(), binding.notesTitleText.text.toString(),
                    Html.toHtml(binding.notesContentText.text)
                )
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotesEntity(
                    0, getTimeOfSavingNote(), binding.notesTitleText.text.toString(),
                    Html.toHtml(binding.notesContentText.text, Html.FROM_HTML_MODE_LEGACY)
                )
            } else {
                NotesEntity(
                    0, getTimeOfSavingNote(), binding.notesTitleText.text.toString(),
                    Html.toHtml(binding.notesContentText.text)
                )
            }
        }
    }


    private fun saveNote() {
        val notesDb = NotesDatabase.getNotesDbInstance(requireContext())
        val notesDao = notesDb.notesDao
        if (noteValidToSave()) {
            runBlocking {
                notesDao.addNote(processDataBaseSaving())
            }

            activity?.findViewById<BottomNavigationView>(R.id.mianBottomBar)?.selectedItemId =
                R.id.homeOption

        } else {
            Toast.makeText(requireContext(), "Note Invalid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelNote() {
        activity?.findViewById<BottomNavigationView>(R.id.mianBottomBar)?.selectedItemId =
            R.id.homeOption
    }

}