package com.example.ali.newlifeplanner;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import com.example.ali.R;

public class ViewTasks extends Activity {

    //SQL related variables.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "entries.db";
    Database dbAdapter = new Database(this, DATABASE_NAME, null, DATABASE_VERSION);
    SQLiteDatabase myDB;

    //The arrays to keep track of the tasks and their positions.
    ArrayList<String> trackList = new ArrayList<String>();
    ArrayList<Integer> trackListID = new ArrayList<Integer>();

    //Variables to ensure that only the tasks for today are listed.
    int day;
    int month;
    int year;

    //The various widgets of this Activity.
    TextView dateTitle;
    ListView taskList;
    LinearLayout linearLayout;

    Handler handler = new Handler();

    //Runnable method that will call todayTaskList(); for displaying the main list of tracked tasks.
    Runnable dbTodayTask = new Runnable() {

        @Override
        public void run() {
            handler.post(new Runnable() {

                @Override
                public void run() {
                    displayList(day, month, year);
                }
            });
        }
    };

    //This method creates the UI from the XML files and assigns the day, month and year variables via the intent extras.
    //It also creates an underlined string that tells the user which day he/she have selected to track.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_tasks);
        dateTitle = (TextView) findViewById(R.id.viewTaskButton);
        taskList = (ListView) findViewById(R.id.viewTaskList);
        linearLayout = (LinearLayout) findViewById(R.id.viewTaskLayout);
        day = getIntent().getExtras().getInt("day");
        month = getIntent().getExtras().getInt("month");
        year = getIntent().getExtras().getInt("year");

        setActivityBackgroundColor();

        String titleString = new String("Scheduled tasks for " + day + "/" + (month + 1) + "/" + year);
        SpannableString content = new SpannableString(titleString);
        content.setSpan(new UnderlineSpan(), 0, titleString.length(), 0);
        dateTitle.setText(content);

        displayList(day, month, year);
        registerForContextMenu(taskList);
        Toast.makeText(this, "Refresh the main page when finished", Toast.LENGTH_SHORT).show();
    }

    //Inflates the context menu upon pressing on a task, specifically, the View/Update and Delete options.
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tasks_menu, menu);
        menu.setHeaderTitle("Select an option");
    }

    //Provides the items to be inflated by the onCreateContextMenu method. Checks which item the user selected
    //and does the appropriate operation by passing taskPosition to the methods.
    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int taskPosition = info.position;

        switch (item.getItemId()) {
            case R.id.updateTask: {
                updateTask(taskPosition);
                break;
            }
            case R.id.deleteTask: {
                deleteTask(taskPosition);
                break;
            }
        }
        return super.onContextItemSelected(item);
    }

    //This method is responsible for when the user clicks Delete on an item. A dialog will appear to ask the users
    //if they are sure, if they are the onClick dialog will run and remove that specific task from the database.
    private void deleteTask(final int itemId) {
        AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
        deleteAlert.setTitle("Delete Task");
        deleteAlert.setMessage("Are you sure you want to delete this task?");
        deleteAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            //I couldn't show a TOAST in this onClick method so I created a showToastDeleted(); method to display it.
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myDB = dbAdapter.getReadableDatabase();
                int id = trackListID.get(itemId);
                myDB.delete("TASKS", "_id = " + id, null);
                myDB.close();
                showToastDeleted();
                displayList(day, month, year);
            }
        });
        deleteAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        deleteAlert.create().show();
    }

    //The method used for when I can't use the built-in TOAST methods.
    public void showToastDeleted(){
        Toast.makeText(this, "Task Deleted!", Toast.LENGTH_SHORT).show();
    }

    //This method creates an intent for AddTaskActivity with two pieces of information: the ID of the task to be updated
    //and the "update" string. This lets me know to keep the previous information (task name, details, date and time).
    private void updateTask(int itemID) {
        int taskID = trackListID.get(itemID);
        Intent intent = new Intent(this, CreateTask.class);
        intent.putExtra("id", taskID);
        intent.putExtra("update", true);
        startActivityForResult(intent, 0);
    }

    //This method is responsible for going through the database via Cursor and displaying the tasks' information
    //in the appropriate format. This method also converts the 24-hour system  to 12-hour system.
    private void displayList(int day, int month, int year) {
        Database dbAdapter = new Database(this, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase myDB;
        trackList.clear();
        trackListID.clear();
        myDB = dbAdapter.getReadableDatabase();
        Cursor cursor = myDB.rawQuery("select _id, NAME, DETAILS, MINUTE, HOUR, DAY, MONTH, YEAR from TASKS", null);
        cursor.moveToFirst();
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(5) == day && cursor.getInt(6) == month && cursor.getInt(7) == year) {
                    if (cursor.getInt(4) == 0){
                        if(cursor.getInt(3) < 10)
                            trackList.add("12" + ":0" + cursor.getInt(3) + " AM"  +"\n" + cursor.getString(1));
                        else
                            trackList.add("12" + ":" + cursor.getInt(3) + " AM"  +"\n" + cursor.getString(1));
                    }
                    else if (cursor.getInt(4) < 12 && cursor.getInt(4) > 0){
                        if(cursor.getInt(4) < 10 & cursor.getInt(3) < 10)
                            trackList.add("0" + cursor.getInt(4) + ":0" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                        else if(cursor.getInt(4) < 10 & cursor.getInt(3) >= 10)
                            trackList.add("0" + cursor.getInt(4) + ":" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                        else if(cursor.getInt(4) >= 10 & cursor.getInt(3) < 10)
                            trackList.add(cursor.getInt(4) + ":0" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                        else
                            trackList.add(cursor.getInt(4) + ":" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                    }
                    else if (cursor.getInt(4) == 12){
                        if(cursor.getInt(3) < 10)
                            trackList.add("12" + ":0" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                        else
                            trackList.add("12" + ":" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                    }
                    else{
                        if(cursor.getInt(4) < 2 & cursor.getInt(3) < 10)
                            trackList.add("0" + (cursor.getInt(4)-12) + ":0" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                        else if(cursor.getInt(4) < 22 & cursor.getInt(3) >= 10)
                            trackList.add("0" + (cursor.getInt(4)-12) + ":" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                        else if(cursor.getInt(4) >= 22 & cursor.getInt(3) < 10)
                            trackList.add((cursor.getInt(4)-12) + ":0" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                        else
                            trackList.add((cursor.getInt(4)-12) + ":" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                    }
                    trackListID.add(cursor.getInt(0));
                }

            } while (cursor.moveToNext());
        }
        myDB.close();
        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, trackList);
        taskList.setAdapter(listAdapter);
        if (trackList.isEmpty()) {
            TextView noTasksView = new TextView(this);
            linearLayout.addView(noTasksView);
            noTasksView.setTextSize((float) 12);
            noTasksView.setText("There are no scheduled tasks for this day.");
        }
    }

    //This method is called when the current Activity exits, and is responsible for creating and starting a new runnable thread.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                boolean result = data.getBooleanExtra("check", false);
                if (result)
                    new Thread(dbTodayTask).start();
            }
        }
    }

    //This method sets the background colour of the Activity.
    public void setActivityBackgroundColor() {
        View view = this.getWindow().getDecorView();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String colourValue = settings.getString("mainBackground", "#ffffff");
        view.setBackgroundColor(Color.parseColor(colourValue));
    }

}
