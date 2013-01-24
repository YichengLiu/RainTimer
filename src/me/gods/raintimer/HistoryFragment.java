package me.gods.raintimer;

import java.sql.Date;
import java.text.DateFormatSymbols;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class HistoryFragment extends Fragment {
    private Spinner eventSpinner;
    private ArrayAdapter<String> adapter;
    private SharedPreferences settings;

    private static Date startDate;
    private static Date endDate;
    private static TextView startDateView;
    private static TextView endDateView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_history, container, false);

        settings = this.getActivity().getPreferences(Activity.MODE_PRIVATE);
        String eventList = settings.getString(PreferenceFragment.PREFERENCE_KEY, "[]");

        JSONArray eventArray = null;
        try {
            eventArray = new JSONArray(eventList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int eventLength = eventArray.length();
        String[] events;

        if (eventLength == 0) {
            events = new String[] {"default"};
        } else {
            events = new String[eventLength];

            for (int i = 0; i < eventLength; i++) {
                try {
                    events[i] = eventArray.getString(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        eventSpinner = (Spinner)v.findViewById(R.id.event_spinner_history);
        adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, events);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(adapter);

        startDate = null;
        endDate = null;

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

        GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {
                new GraphViewData(1, 2.0d)
                , new GraphViewData(2, 1.5d)
                , new GraphViewData(3, 2.5d)
                , new GraphViewData(4, 1.0d)
        });

        GraphView graphView = new LineGraphView(this.getActivity(), "");
        graphView.addSeries(exampleSeries);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(30, 30, 30, 20);
        graphView.setLayoutParams(lp);
        LinearLayout layout = (LinearLayout)v.findViewById(R.id.history_container);
        layout.addView(graphView);
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
            int year, month, day;

            if (this.getTag().equals("Start") && startDate == null || this.getTag().equals("End") && endDate == null) {
                final Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            } else {
                Date dateToShow = this.getTag().equals("Start") ? startDate : endDate;

                year = dateToShow.getYear();
                month = dateToShow.getMonth();
                day = dateToShow.getDate();
            }

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if (this.getTag().equals("Start")) {
                startDate = new Date(year, monthOfYear, dayOfMonth);
                startDateView.setText(String.format("%s-%02d-%04d", new DateFormatSymbols().getMonths()[monthOfYear].substring(0,3), dayOfMonth, year));
            } else {
                endDate = new Date(year, monthOfYear, dayOfMonth);
                endDateView.setText(String.format("%s-%02d-%04d", new DateFormatSymbols().getMonths()[monthOfYear].substring(0,3), dayOfMonth, year));
            }
        }
    }
}
