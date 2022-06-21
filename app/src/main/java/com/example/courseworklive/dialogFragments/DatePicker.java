package com.example.courseworklive.dialogFragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;

public class DatePicker extends DialogFragment {

    /**
     * The Date Picker is a custom implementation of the DialogFragment class.
     * This is done as it allows for a custom implementation of the date picker
     * functionality that makes it easier for the user to select date
     * it also allows the app to restrict the max date that the user can choose.
     * */
    Calendar mCalender = Calendar.getInstance();
    boolean limiter = true;

    /**
     * Setting up the Dialog*/
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int year = mCalender.get(Calendar.YEAR);
        int month = mCalender.get(Calendar.MONTH);
        int dayOfMonth = mCalender.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()), (DatePickerDialog.OnDateSetListener)
                getActivity(), year, month, dayOfMonth);
        if (limiter) datePickerDialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
        return datePickerDialog;
    }

    /**
     * Setting up the restrictions
     * */
    public void setmCalender(Calendar mCalender) {
        this.mCalender = mCalender;
    }

    public void setLimiter(boolean limiter) {
        this.limiter = limiter;
    }
}
