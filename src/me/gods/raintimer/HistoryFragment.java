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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryFragment extends Fragment {
    private Spinner eventSpinner;
    private ArrayAdapter<String> adapter;
    private SharedPreferences settings;
    private String currentEvent;

    private static Date startDate;
    private static Date endDate;
    private static TextView startDateView;
    private static TextView endDateView;

    private Switch modeSwithcer;
    private GraphViewSeries dataSeries;
    private GraphView graphView;

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

        int eventLength = eventArray.length() + 1;
        final String[] events = new String[eventLength];

        events[0] = getString(R.string.default_event);

        for (int i = 1; i < eventLength; i++) {
            try {
                events[i] = eventArray.getString(i - 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        eventSpinner = (Spinner)v.findViewById(R.id.event_spinner_history);
        adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, events);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(adapter);

        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                currentEvent = events[arg2];
                updateChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

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

        modeSwithcer = (Switch)v.findViewById(R.id.mode_switcher);
        modeSwithcer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
                updateChart();
            }
        });

        dataSeries = new GraphViewSeries(new GraphViewData[] {
                new GraphViewData(1, 2.0d)
                , new GraphViewData(2, 1.5d)
                , new GraphViewData(3, 2.5d)
                , new GraphViewData(4, 1.0d)});
        graphView = new LineGraphView(this.getActivity(), "");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(30, 30, 30, 20);
        graphView.setLayoutParams(lp);
        LinearLayout layout = (LinearLayout)v.findViewById(R.id.history_container);
        layout.addView(graphView);

        graphView.addSeries(dataSeries);
        graphView.redrawAll();

        return v;
    }

    public void showDatePickerDialog(String tag) {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getActivity().getFragmentManager(), tag);
    }

    public void updateChart() {
        if (graphView.getSeriesCount() > 0) {
            graphView.removeSeries(0);
        }

        if (currentEvent == null
            || currentEvent.equals(getString(R.string.default_event))
            || startDate == null
            || endDate == null) {
            
        } else if (startDate.compareTo(endDate) > 0 ) {
            Toast.makeText(getActivity().getApplicationContext(), "Invalid Date!", Toast.LENGTH_LONG).show();
        } else {
            dataSeries = new GraphViewSeries(new GraphViewData[] {
                    new GraphViewData(1, 1d)
                    , new GraphViewData(2, 2d)
                    , new GraphViewData(3, 3d)
                    , new GraphViewData(4, 4d)
            });
        }

        graphView.addSeries(dataSeries);
        graphView.redrawAll();
    }

    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
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
        public void onDateSet(DatePicker arg0, int year, int monthOfYear, int dayOfMonth) {
            if (this.getTag().equals("Start")) {
                startDate = new Date(year, monthOfYear, dayOfMonth);
                startDateView.setText(String.format("%s-%02d-%04d", new DateFormatSymbols().getMonths()[monthOfYear].substring(0,3), dayOfMonth, year));
            } else {
                endDate = new Date(year, monthOfYear, dayOfMonth);
                endDateView.setText(String.format("%s-%02d-%04d", new DateFormatSymbols().getMonths()[monthOfYear].substring(0,3), dayOfMonth, year));
            }
            updateChart();
        }
    }
}
