package com.cteam.writedown;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

import android.os.Bundle;
import android.util.Log;
import android.view.View; // Added for button visibility
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections; // For sorting
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText editTextNote;
    private Button buttonSave;
    private Button buttonNewNote; // For the optional New Note button
    private ArrayList<NoteItem> noteItems;
    private ArrayAdapter<NoteItem> adapter;
    private File notesSavedDirectory;

    private String currentEditingFilename = null;

    private static final String TAG = "MainActivity";

    public static class NoteItem implements Comparable<NoteItem> { // Implement Comparable for sorting
        private final String content;
        private final String filename;
        private final long lastModified; // For sorting by date

        public NoteItem(String content, String filename, long lastModified) {
            this.content = content;
            this.filename = filename;
            this.lastModified = lastModified;
        }

        public String getContent() { return content; }
        public String getFilename() { return filename; }
        public long getLastModified() { return lastModified; }

        @NonNull
        @Override
        public String toString() {
            return filename.replace(".txt", "");
        }

        // Sort by last modified date, newest first
        @Override
        public int compareTo(NoteItem other) {
            return Long.compare(other.lastModified, this.lastModified);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        editTextNote = findViewById(R.id.edit_text_note);
        buttonSave = findViewById(R.id.button_save);
        buttonNewNote = findViewById(R.id.button_new_note); // Find the new note button
        ListView listViewNotes = findViewById(R.id.list_view_notes);

        setupDirectories();

        noteItems = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, noteItems);
        listViewNotes.setAdapter(adapter);

        loadNotes();

        buttonSave.setOnClickListener(view -> {
            String noteText = editTextNote.getText().toString().trim();
            if (!noteText.isEmpty()) {
                saveNoteToFile(noteText);
            } else {
                Toast.makeText(MainActivity.this, "Note content cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Optional: New Note Button Functionality
        buttonNewNote.setVisibility(View.VISIBLE); // Make it visible
        buttonNewNote.setOnClickListener(v -> {
            clearEditorAndResetSaveButton();
            Toast.makeText(this, "Ready for new note", Toast.LENGTH_SHORT).show();
        });


        listViewNotes.setOnItemClickListener((parent, view, position, id) -> {
            NoteItem selectedNote = noteItems.get(position);
            editTextNote.setText(selectedNote.getContent());
            editTextNote.setSelection(editTextNote.getText().length());
            currentEditingFilename = selectedNote.getFilename();
            buttonSave.setText("Update Note");
            Log.d(TAG, "Editing note: " + currentEditingFilename);
        });

        listViewNotes.setOnItemLongClickListener((parent, view, position, id) -> {
            NoteItem noteToDelete = noteItems.get(position);
            File noteFile = new File(notesSavedDirectory, noteToDelete.getFilename());

            // Use the custom theme for the AlertDialog
            new AlertDialog.Builder(MainActivity.this, R.style.CustomDeleteDialogTheme) // APPLYING THEME
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete '" + noteToDelete.getFilename().replace(".txt", "") + "'?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        if (noteFile.exists() && noteFile.delete()){
                            if (noteToDelete.getFilename().equals(currentEditingFilename)) {
                                clearEditorAndResetSaveButton();
                            }
                            loadNotes();
                            Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Failed to delete note file: " + noteFile.getAbsolutePath());
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        });
    }

    private void setupDirectories() {
        File appSpecificDir = getApplicationContext().getFilesDir();
        File notesSubDir = new File(appSpecificDir, "notes");
        if (!notesSubDir.exists() && !notesSubDir.mkdirs()) {
            Log.e(TAG, "Failed to create 'notes' directory");
            Toast.makeText(this, "Error: Could not create notes directory", Toast.LENGTH_LONG).show();
            finish(); // Critical error, close app or handle gracefully
            return;
        }
        notesSavedDirectory = new File(notesSubDir, "saved");
        if (!notesSavedDirectory.exists() && !notesSavedDirectory.mkdirs()) {
            Log.e(TAG, "Failed to create 'saved' directory");
            Toast.makeText(this, "Error: Could not create saved notes directory", Toast.LENGTH_LONG).show();
            finish(); // Critical error
            return;
        }
        Log.d(TAG, "Notes will be saved in: " + notesSavedDirectory.getAbsolutePath());
    }

    private void clearEditorAndResetSaveButton() {
        editTextNote.setText("");
        currentEditingFilename = null;
        buttonSave.setText("Save Note");
        editTextNote.setHint("Enter your new note here");
        editTextNote.requestFocus(); // Set focus to the editor for a new note
    }

    private void saveNoteToFile(String noteContent) {
        if (notesSavedDirectory == null || !notesSavedDirectory.exists()) {
            Toast.makeText(this, "Error: Notes directory not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String filenameToSave;
        boolean isUpdating = false;

        if (currentEditingFilename != null) {
            filenameToSave = currentEditingFilename;
            isUpdating = true;
        } else {
            String[] lines = noteContent.split("\n", 2);
            String potentialTitle = lines[0].trim();
            if (potentialTitle.isEmpty() || potentialTitle.length() > 50) { // Keep title reasonable or use timestamp
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                filenameToSave = "Note_" + timeStamp + ".txt";
            } else {
                filenameToSave = potentialTitle.replaceAll("[^a-zA-Z0-9_\\-\\.\\s]", "_").trim().replaceAll("\\s+", "_") + ".txt";
                if (filenameToSave.length() > 60) { // Limit filename length
                    filenameToSave = filenameToSave.substring(0, 55) + ".txt";
                }
            }

            File tempFile = new File(notesSavedDirectory, filenameToSave);
            int counter = 1;
            String originalFilenameBase = filenameToSave.replace(".txt", "");
            while (tempFile.exists()) { // Handle filename conflicts for new notes
                String timeStampSuffix = new SimpleDateFormat("HHmmss", Locale.getDefault()).format(new Date());
                filenameToSave = originalFilenameBase + "_" + timeStampSuffix + "_" + counter + ".txt";
                if (filenameToSave.length() > 60) { // Re-check length after adding suffix
                    filenameToSave = (originalFilenameBase.length() > 20 ? originalFilenameBase.substring(0,20) : originalFilenameBase)
                            + "_" + timeStampSuffix + "_" + counter + ".txt";
                }
                tempFile = new File(notesSavedDirectory, filenameToSave);
                counter++;
            }
        }

        File noteFile = new File(notesSavedDirectory, filenameToSave);

        try (FileOutputStream fos = new FileOutputStream(noteFile)) {
            fos.write(noteContent.getBytes());
            Toast.makeText(this, isUpdating ? "Note updated!" : "Note saved!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, (isUpdating ? "Note updated: " : "Note saved to: ") + noteFile.getAbsolutePath());
            clearEditorAndResetSaveButton();
            loadNotes();
        } catch (IOException e) {
            Log.e(TAG, "Error saving note: " + filenameToSave, e);
            Toast.makeText(this, "Failed to save note: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadNotes() {
        if (notesSavedDirectory == null || !notesSavedDirectory.exists()) {
            Log.e(TAG, "loadNotes: notesSavedDirectory is null or does not exist.");
            return;
        }

        noteItems.clear();
        File[] files = notesSavedDirectory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    StringBuilder content = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        String noteContent = content.toString();
                        if (noteContent.endsWith("\n")) {
                            noteContent = noteContent.substring(0, noteContent.length() - 1);
                        }
                        noteItems.add(new NoteItem(noteContent, file.getName(), file.lastModified()));
                        Log.d(TAG, "Loaded note: " + file.getName() + " lastMod: " + file.lastModified());
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading note file: " + file.getName(), e);
                    }
                }
            }
            Collections.sort(noteItems); // Sort notes by last modified date (newest first)
        } else {
            Log.d(TAG, "No files found in notes directory or directory is not readable.");
        }
        adapter.notifyDataSetChanged();
    }
}