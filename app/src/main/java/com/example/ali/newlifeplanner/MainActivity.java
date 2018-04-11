package com.example.ali.newlifeplanner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.ContextMenu.ContextMenuInfo;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.example.ali.R;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity {

    //SQL related components.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "entries.db";
    Database dbAdapter = new Database(this, DATABASE_NAME, null, DATABASE_VERSION);
    SQLiteDatabase myDB;

    //The arrays to keep track of the tasks and their positions.
    ArrayList<String> mainList = new ArrayList<String>();
    ArrayList<Integer> mainListID  = new ArrayList<Integer>();

    //The various widgets of this Activity.
    Button createTask;
    ListView taskList;
    RelativeLayout relativeLayout;
    ArrayAdapter<String> listAdapter;

    Handler handler = new Handler();

    //Runnable method that will call displayList(); for displaying the main list
    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            updateColours();
            handler.post(new Runnable() {

                @Override
                public void run() {
                    displayList();
                }
            });
        }
    };

    //The onCreate method is similar to a constructor class. I initialize my variables and inflate the activity's UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        createTask = (Button) findViewById(R.id.btnFixedTimeTask);
        taskList = (ListView) findViewById(R.id.listView1);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativelayout);

        updateColours();

        new Thread(runnable).start();
        createTask.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(getBaseContext(), CreateTask.class);
                intent.putExtra("update", false);
                startActivityForResult(intent, 0);
            }
        });
        registerForContextMenu(taskList);
    }

    //The listener for the date dialog when viewing tasks.
    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            updateColours();

            //Create a new intent for the ViewTasks class and pass in the values of the date.
            Intent intent = new Intent("com.example.ali.newlifeplanner.ViewTasks");
            intent.putExtra("day", day);
            intent.putExtra("month", month);
            intent.putExtra("year", year);
            startActivity(intent);
        }
    };

    //Inflates the context menu upon pressing on a task, specifically, the View/Update and Delete options.
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        updateColours();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tasks_menu, menu);
        menu.setHeaderTitle("Select an option");
    }

    //Provides the items to be inflated by the onCreateContextMenu method. Checks which item the user selected
    //and does the appropriate operation by passing taskPosition to the methods.
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        updateColours();
        AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
        int taskPosition = menuInfo.position;

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

    //Checks which item from the options menu the user has selected, and does the appropriate action.
    //These options include About, Refresh, View Tasks, and Settings.
    @SuppressWarnings("deprecation")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        updateColours();

        switch (item.getItemId()) {
            case R.id.settingsItem: {
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                Toast.makeText(this, "Refresh the main page after selecting a colour", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.aboutItem: {
                Intent intent = new Intent(this, About.class);
                startActivity(intent);
                break;
            }
            case R.id.viewTasksItem: {
                showDialog(1);
                break;
            }
            case R.id.refreshItem: {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //This method is called whenever the option menu is closed, either by pressing the Back button
    //or by selecting an item from the menu.
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        updateColours();
        super.onOptionsMenuClosed(menu);
    }

    //This method is responsible for going through the database via Cursor and displaying the tasks' information
    //in the appropriate format. This method also converts the 24-hour system  to 12-hour system.
    public void displayList() {
        updateColours();
        myDB = dbAdapter.getReadableDatabase();
        Calendar c = Calendar.getInstance();
        mainList.clear();
        mainListID.clear();
        Cursor cursor = myDB.query("TASKS", new String[] { "_id", "NAME", "HOUR", "MINUTE", "DAY", "YEAR", "MONTH",
                "DETAILS" }, null, null, null, null, null);

        //This while loop makes sure that only the tasks that have a date equal to today will be displayed.
        while (cursor.moveToNext()) {
            if (cursor.getInt(4) == c.get(Calendar.DAY_OF_MONTH) && cursor.getInt(6) == (c.get(Calendar.MONTH))
                    && cursor.getInt(5) == c.get(Calendar.YEAR)) {
                if (cursor.getInt(2) == 0){
                    if (cursor.getInt(3) < 10)
                        mainList.add("12" + ":0" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                    else
                        mainList.add("12" + ":" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                }
                else if (cursor.getInt(2) < 12 && cursor.getInt(2) > 0){
                    if(cursor.getInt(2) < 10 & cursor.getInt(3) < 10)
                        mainList.add("0" + cursor.getInt(2) + ":0" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                    else if(cursor.getInt(2) < 10 & cursor.getInt(3) >= 10)
                        mainList.add("0" + cursor.getInt(2) + ":" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                    else if(cursor.getInt(2) >= 10 & cursor.getInt(3) < 10)
                        mainList.add(cursor.getInt(2) + ":0" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                    else
                        mainList.add(cursor.getInt(2) + ":" + cursor.getInt(3) + " AM" + "\n" + cursor.getString(1));
                }
                else if (cursor.getInt(2) == 12){
                    if (cursor.getInt(3) < 10)
                        mainList.add("12" + ":0" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                    else
                        mainList.add("12" + ":" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                }
                else{
                    if(cursor.getInt(2) < 22 & cursor.getInt(3) < 10)
                        mainList.add("0" + (cursor.getInt(2)-12) + ":0" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                    else if(cursor.getInt(2) < 22 & cursor.getInt(3) >= 10)
                        mainList.add("0" + (cursor.getInt(2)-12) + ":" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                    else if(cursor.getInt(2) >= 2 & cursor.getInt(3) < 10)
                        mainList.add((cursor.getInt(2)-12) + ":0" + cursor.getInt(3) + " PM" + "\n" + cursor.getString(1));
                    else
                        mainList.add((cursor.getInt(2)-12) + ":" + cursor.getInt(3) + " PM" +"\n" + cursor.getString(1));
                }
                //Adds the _id attribute from the database to the array list mainListID.
                mainListID.add(cursor.getInt(0));
            }
        }
        myDB.close();
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mainList);
        taskList.setAdapter(listAdapter);
    }

    //This method is responsible for when the user clicks Delete on an item. A dialog will appear to ask the users
    //if they are sure, if they are the onClick dialog will run and remove that specific task from the database.
    private void deleteTask(final int itemID) {
        updateColours();

        AlertDialog.Builder deleteAlert = new AlertDialog.Builder(this);
        deleteAlert.setTitle("Delete Task");
        deleteAlert.setMessage("Are you sure you want to delete this task?");
        deleteAlert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            //I couldn't show a TOAST in this onClick method so I created a showToastDeleted(); method to display it.
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myDB = dbAdapter.getReadableDatabase();
                int id = mainListID.get(itemID);
                myDB.delete("TASKS", "_id = " + id, null);
                myDB.close();
                showToastDeleted();
                displayList();
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

    //This method creates an intent for CreateTask activity with two pieces of information: the ID of the task to be updated
    //and the "update" string. This lets me know to keep the previous information (task name, details, date and time).
    private void updateTask(int itemID) {
        int taskID = mainListID.get(itemID);
        Intent intent = new Intent(this, CreateTask.class);
        intent.putExtra("id", taskID);
        intent.putExtra("update", true);
        startActivityForResult(intent, 0);
    }

    //Initializes the contents of the option menu from an XML file. These items are Refresh, About, View Tasks and Settings.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        updateColours();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    //This method is used for when the user selects View Tasks. A Date Picker is displayed, with the default values
    //as today's values.
    @Override
    @Deprecated
    protected Dialog onCreateDialog(int id) {
        updateColours();
        Calendar c = Calendar.getInstance();

        switch (id) {
            case 1:
                return new DatePickerDialog(this, dateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
                        c.get(Calendar.DAY_OF_MONTH));

        }
        return super.onCreateDialog(id);
    }

    //This method changes the background's colour by reading the mainBackground attribute in the settings XML file.
    private void colourBackground(RelativeLayout relativeLayoutBG) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String colourValue = settings.getString("mainBackground", "#ffffff");
        relativeLayoutBG.setBackgroundColor(Color.parseColor(colourValue));
    }

    //This method changes the  list's background colour by reading the listBackground attribute in the settings XML file.
    public void colourList(View listView) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String colourValue = settings.getString("listBackground", "#ffffff");
        listView.setBackgroundColor(Color.parseColor(colourValue));
    }

    //This method calls both colour updating methods. Helps keep the code clean.
    public void updateColours(){
        colourList(taskList);
        colourBackground(relativeLayout);
    }

    //This method is called when the current Activity exits, and is responsible for creating and starting a new runnable thread.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateColours();

        if (requestCode == 0)
            if (resultCode == RESULT_OK) {
                boolean result = data.getBooleanExtra("check", false);
                if (result)
                    new Thread(runnable).start();
            }
    }

    //The method used for when I can't use the built-in TOAST methods.
    public void showToastDeleted(){
        Toast.makeText(this, "Task Deleted!", Toast.LENGTH_SHORT).show();
    }

}
