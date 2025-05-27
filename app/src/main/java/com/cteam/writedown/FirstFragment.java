package com.cteam.writedown; // Make sure this matches your project's package name

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
// For navigation, if you're using Navigation Component:
// import androidx.navigation.fragment.NavHostFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView; // If you want to interact with the placeholder
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FirstFragment extends Fragment {

    // Declare views if you need to access them outside onCreateView, though often not necessary
    // private ImageView menuIcon;
    // private TextView titleWriteDown;
    // private FloatingActionButton fabAddNote;
    // private TextView placeholderContentText;

    public FirstFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FirstFragment.
     */
    public static FirstFragment newInstance() {
        return new FirstFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // You can get arguments here if you pass any via newInstance(args)
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        // Initialize views
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView menuIcon = view.findViewById(R.id.menu_icon);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView titleWriteDown = view.findViewById(R.id.title_writedown); // If you need to interact
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) FloatingActionButton fabAddNote = view.findViewById(R.id.fab_add_note);
        @SuppressLint("MissingInflatedId") TextView placeholderContentText = view.findViewById(R.id.text_view_placeholder_content); // Example

        // Set up listeners
        menuIcon.setOnClickListener(v -> {
            // Handle menu icon click
            // For example, if you have a DrawerLayout in your MainActivity:
            // ((MainActivity) requireActivity()).openDrawer();
            Toast.makeText(getContext(), "Menu icon clicked!", Toast.LENGTH_SHORT).show();
        });

        fabAddNote.setOnClickListener(v -> {
            // Handle FloatingActionButton click
            // For example, navigate to a screen to create a new note
            Toast.makeText(getContext(), "Add new note clicked!", Toast.LENGTH_SHORT).show();

            // Example using Navigation Component (uncomment if you have it set up):
            // NavHostFragment.findNavController(FirstFragment.this)
            //         .navigate(R.id.action_FirstFragment_to_yourCreateNoteFragment);
            // Replace R.id.action_FirstFragment_to_yourCreateNoteFragment with your actual action ID
        });

        // You can update the placeholder text or load your main content here
        // placeholderContentText.setText("Loading notes...");

        return view;
    }

    // You might add other lifecycle methods or helper functions here as needed
}