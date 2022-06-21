package com.example.courseworklive.dialogFragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public class CheckForDeletion extends DialogFragment {

    /**
     * The Check for deletion is a custom implementation of the DialogFragment class.
     * This is done as it allows the use of a listener that can pass the input to the
     * activity that uses the application
     * It also allows for an extensive array od modifications
     * */
    String title;
    String message;
    String positiveButton;
    String negativeButton;

    /**
     * Setting up the title, message and buttons of the dialog fragment
     * */
    public CheckForDeletion(String title, String message, String positiveButton, String negativeButton){
        this.title = title;
        this.message = message;
        this.positiveButton = positiveButton;
        this.negativeButton = negativeButton;
    }

    /**
     * Creating the dialog
     * */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message).setTitle(title).
                setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onPositiveClick(CheckForDeletion.this);
                    }
                }).setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                listener.onNegativeClick(CheckForDeletion.this);
            }
        });
        return builder.create();
    }

    /**
     * Attaching the listener to the fragment
     * */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (CheckForDeletionListener) context;
        } catch (ClassCastException ignored){

        }
    }

    /**
     * or possible use in the future, in case the dialog needs to b modified after a
     * certain function
     * */
    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPositiveButton(String positiveButton) {
        this.positiveButton = positiveButton;
    }

    public void setNegativeButton(String negativeButton) {
        this.negativeButton = negativeButton;
    }

    /**
     * Interface and listener used to pass the click off each button
     * to the activity that implements it
     * */
    CheckForDeletionListener listener;

    public interface CheckForDeletionListener{
        void onPositiveClick(DialogFragment dialogFragment);
        void onNegativeClick(DialogFragment dialogFragment);
    }

}
