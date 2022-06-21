package com.example.courseworklive.dialogFragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Objects;

public class DatePickerAdvanced extends DialogFragment {

    /**
     * The Date Picker Advanced is a custom implementation of the DialogFragment class.
     * This is done as it allows for a custom implementation of the date picker
     * functionality that makes it easier for the user to select date
     * it also allows the app to restrict the min date that the user can choose
     * */
    Calendar mCalender = Calendar.getInstance();
    boolean limiter = true;
    Calendar min = Calendar.getInstance();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        int year = mCalender.get(Calendar.YEAR);
        int month = mCalender.get(Calendar.MONTH);
        int dayOfMonth = mCalender.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(Objects.requireNonNull(getActivity()), (DatePickerDialog.OnDateSetListener)
                getActivity(), year, month, dayOfMonth);
        if (limiter) datePickerDialog.getDatePicker().setMinDate(min.getTimeInMillis());
        return datePickerDialog;
    }

    public void setmCalender(Calendar mCalender) {
        this.mCalender = mCalender;
    }

    public void setMin(Calendar min) {
        this.min = min;
    }

    public void setLimiter(boolean limiter) {
        this.limiter = limiter;
    }

}
