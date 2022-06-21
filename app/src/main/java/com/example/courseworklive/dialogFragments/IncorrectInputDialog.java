package com.example.courseworklive.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class IncorrectInputDialog extends DialogFragment {

    /**
     * The Incorrect Input Dialog is a custom implementation of the DialogFragment class.
     * */
    String title;
    String message;

    /**
     * Setting up the title, message and buttons of the dialog fragment
     * */
    public IncorrectInputDialog(String title, String message){
        this.title = title;
        this.message = message;
    }

    /**
     * Creating the dialog
     * */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setTitle(title).
                setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        return builder.create();
    }
}
