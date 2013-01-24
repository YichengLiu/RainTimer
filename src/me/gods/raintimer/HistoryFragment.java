package me.gods.raintimer;

import java.sql.Date;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

public class HistoryFragment extends Fragment {
    private static Date startDate;
    private static Date endDate;
    private static TextView startDateView;
    private static TextView endDateView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        startDateView = (TextView)v.findViewById(R.id.start_date_picker);
        startDateView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                showDatePickerDialog("Start");
            }
        });

        endDateView = (TextView)v.findViewById(R.id.end_date_picker);
        endDateView.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                showDatePickerDialog("End");
            }
        });

        return v;
    }

    public void showDatePickerDialog(String tag) {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getActivity().getFragmentManager(), tag);
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (this.getTag() == "Start") {
                startDate = new Date(year, monthOfYear, dayOfMonth);
                startDateView.setText(String.format("%s-%02d-%04d", new DateFormatSymbols().getMonths()[monthOfYear].substring(0,3), dayOfMonth, year));
            } else {
                endDate = new Date(year, monthOfYear, dayOfMonth);
                endDateView.setText(String.format("%s-%02d-%04d", new DateFormatSymbols().getMonths()[monthOfYear].substring(0,3), dayOfMonth, year));
            }
        }
    }
}
