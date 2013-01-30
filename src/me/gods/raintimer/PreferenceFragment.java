package me.gods.raintimer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class PreferenceFragment extends Fragment {
    public static final String FAVORITE_EVENT_LIST = "FavoriteEventList";
    public static final String EVENT_LIST = "EventList";

    private ListView eventListView;
    private ArrayAdapter<String> adapter;
    private Button addEventButton;
    private Button backupRestoreButton;


    private SharedPreferences settings;
    private ArrayList<String> eventList;
    JSONArray eventArray = null;
    private ArrayList<String> favoriteList;
    JSONArray favoriteArray = null;

    private SQLiteDatabase db;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_preference, container, false);

        db = getActivity().openOrCreateDatabase("raintimer.db", Context.MODE_PRIVATE, null);

        settings = this.getActivity().getPreferences(Activity.MODE_PRIVATE);
        eventList = new ArrayList<String>();
        String events = settings.getString(EVENT_LIST, "[]");
        try {
            eventArray = new JSONArray(events);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int eventLength = eventArray.length();
        for (int i = 0; i < eventLength; i++) {
            try {
                eventList.add(eventArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        favoriteList = new ArrayList<String>();
        String favorites = settings.getString(FAVORITE_EVENT_LIST, "[]");
        try {
            favoriteArray = new JSONArray(favorites);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        int favoriteLength = favoriteArray.length();
        for (int i = 0; i < favoriteLength; i++) {
            try {
                favoriteList.add(favoriteArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        eventListView = (ListView)v.findViewById(R.id.event_list);
        adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_expandable_list_item_1,eventList);
        eventListView.setAdapter(adapter);

        eventListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
                menu.setHeaderIcon(android.R.drawable.ic_delete);
                menu.setHeaderTitle("This operation will delete all data about this event. Continue?");
                menu.add(0, 0, 0, "Delete");
                menu.add(0, 1, 0, "Cancel");  
            }
        });

        addEventButton = (Button)v.findViewById(R.id.add_event_button);
        addEventButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Please Input Event Name")
                    .setIcon(android.R.drawable.ic_dialog_info);

                final EditText input = new EditText(getActivity());

                dialog.setView(input)
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String added = input.getText().toString();

                            if (eventList.contains(added)) {
                                return;
                            }

                            eventList.add(added);
                            updateStorage();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();

                
            }
        });

        backupRestoreButton = (Button)v.findViewById(R.id.backup_restore_button);
        backupRestoreButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new AlertDialog.Builder(getActivity())
                    .setTitle("Backup/Restore")
                    .setPositiveButton("Backup", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            backupToExternalStorage();
                        }

                    })
                    .setNegativeButton("Restore", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            restoreFromExternalStorage();
                        }

                    })
                    .setNeutralButton("Cancel", null)
                    .create().show();
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterContextMenuInfo)item.getMenuInfo()).position;

        if (item.getItemId() == 0) {
            db.delete("history", "event_name = ?", new String[]{eventList.get(position)});
            favoriteList.remove(eventList.get(position));
            eventList.remove(position);
            updateStorage();
        }

        return super.onContextItemSelected(item);
    }

    private void backupToExternalStorage() {
        File path = new File(Environment.getExternalStorageDirectory().toString() + "/data/me.gods.raintimer/");
        if(!path.exists()) {
            path.mkdirs();
        }
        File backupFile = new File(path, "data.restore");
        JSONObject obj = new JSONObject();

        String rawSQL = "SELECT event_name, commit_date, total_time FROM history";
        Cursor c = db.rawQuery(rawSQL, null);
        JSONArray historyArray = new JSONArray();

        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndex("event_name"));
            int time = c.getInt(c.getColumnIndex("total_time"));
            String date = c.getString(c.getColumnIndex("commit_date"));
            Log.i("backup", "name=>" + name + ", time=>" + time + ", date=>" + date);

            JSONObject historyObj = new JSONObject();
            try {
                historyObj.put("event_name", name);
                historyObj.put("total_time", time);
                historyObj.put("commit_date", date);

                historyArray.put(historyObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        try {
            obj.put("events", eventArray);
            obj.put("favorites", favoriteArray);
            obj.put("history", historyArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            BufferedWriter output = new BufferedWriter(new FileWriter(backupFile));
            output.write(obj.toString());
            output.flush();
            output.close();
        } catch (IOException e) {
            Log.e("RainTimer", "Error in write file");
        }
    }

    private void restoreFromExternalStorage() {
        File path = new File(Environment.getExternalStorageDirectory().toString() + "/data/me.gods.raintimer/");

        if(!path.exists()) {
            Toast.makeText(getActivity(), "You have not backup yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.i("raintimer", "start restore!");
        try {
            File backupFile = new File(path, "data.restore");
            FileInputStream input = new FileInputStream(backupFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            Log.i("raintimer", "load backup file = " + sb.toString());
            JSONObject obj = new JSONObject(sb.toString());
            JSONArray tmpEventArray = obj.getJSONArray("events");
            eventList.clear();
            for (int i = 0; i < tmpEventArray.length(); i++) {
                eventList.add(tmpEventArray.getString(i));
            }

            JSONArray tmpFavoriteArray = obj.getJSONArray("favorites");
            favoriteList.clear();
            for (int i = 0; i < tmpFavoriteArray.length(); i++) {
                favoriteList.add(tmpFavoriteArray.getString(i));
            }

            db.execSQL("delete from history");
            JSONArray history = obj.getJSONArray("history");
            for (int i = 0; i < history.length(); i++) {
                JSONObject historyObj = history.getJSONObject(i);
                String sql = "insert into history values(null, ?, ?, ?)";
                db.execSQL(sql, new Object[] { historyObj.getString("event_name"), historyObj.getString("total_time"), historyObj.getString("commit_date") });
            }

            updateStorage();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void updateStorage() {
        eventArray = new JSONArray(eventList);
        favoriteArray = new JSONArray(favoriteList);

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(EVENT_LIST, eventArray.toString());
        editor.putString(FAVORITE_EVENT_LIST, favoriteArray.toString());
        editor.commit();

        adapter.notifyDataSetChanged();
    }
}
