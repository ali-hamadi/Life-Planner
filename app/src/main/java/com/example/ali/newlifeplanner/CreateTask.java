package com.example.ali.newlifeplanner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.ali.R;

public class CreateTask extends Activity {

    //SQL related variables.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "entries.db";
    Database dbAdapter = new Database(this, DATABASE_NAME, null, DATABASE_VERSION);
    SQLiteDatabase myDB;

    //Used to differentiate between updating a task and adding a task.
    boolean checkUpdate = false;

    //Variables to store the date and time.
    int minute = -1;
    int hour = 1;
    int day = -1;
    int month = -1;
    int year = -1;

    //The various widgets of this Activity.
    Button timePicker, datePicker, saveTask, cancelTask;
    EditText taskName, taskDetails;

    Handler handler = new Handler();

    //This method does everything the onCreate() method in MainActivity does, but also checks if the
    //the user has selected the Update/View button from the main page. If it is selected, the text fields and date/time
    //will be set already.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_task);
        timePicker = (Button) findViewById(R.id.timeButton);
        datePicker = (Button) findViewById(R.id.dateButton);
        saveTask = (Button) findViewById(R.id.saveButton);
        cancelTask = (Button) findViewById(R.id.cancelButton);

        setActivityBackgroundColor();
        timePicker.setEnabled(false);

        taskName = (EditText) findViewById(R.id.nameEdit);
        taskDetails = (EditText) findViewById(R.id.detailsEdit);
        checkUpdate = getIntent().getExtras().getBoolean("update");

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (checkUpdate) {
            int taskID = getIntent().getExtras().getInt("id");
            myDB = dbAdapter.getReadableDatabase();
            Cursor cursor = myDB.query("TASKS", new String[] { "_id", "NAME", "DETAILS", "HOUR", "MINUTE", "DAY",
                    "MONTH", "YEAR" }, null, null, null, null, null);
            while (cursor.moveToNext()) {
                if (cursor.getInt(0) == taskID) {
                    taskName.setText(cursor.getString(1));
                    taskDetails.setText(cursor.getString(2));
                    minute = cursor.getInt(4);
                    hour = cursor.getInt(3);
                    day = cursor.getInt(5);
                    month = cursor.getInt(6);
                    year = cursor.getInt(7);
                    convertTime(hour, minute);
                    convertDate(year, month, day);
                }
            }
            myDB.close();

        }

        saveTask.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                //This block of code is run when the user clicks saveTask button after selecting "View/Update Task" from the main page.
                if (checkUpdate) {
                    boolean inputValidation = userInputValidation();
                    if (inputValidation) {
                        updateData(getIntent().getExtras().getInt("id"));
                        Intent intent = new Intent();
                        intent.putExtra("check", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), "Please fill in all information",
                                Toast.LENGTH_LONG).show();
                    }
                    //This block of code is run when the user clicks the saveTask button after selecting "Add Task" from the main page.
                } else {
                    boolean inputValidation = userInputValidation();
                    if (inputValidation) {
                        new Thread(new Runnable() {

                            @Override
                            public void run() {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        saveData();
                                    }
                                });
                            }
                        });
                        saveData();
                        Intent intent = new Intent();
                        intent.putExtra("check", true);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), "Please fill in all information",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

        //This method redirects the user to the main page by exiting the Activity.
        cancelTask.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        //Displays the Date Picker dialog with the default values supplied in the onCreateDialog method below.
        datePicker.setOnClickListener(new View.OnClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                showDialog(1);
            }
        });

        //Displays the Time Picker dialog with the default values supplied in the onCreateDialog method below.
        timePicker.setOnClickListener(new View.OnClickListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onClick(View v) {
                showDialog(2);
            }
        });
    }

    //This is where the functionality of the Save Task button happens. All the information entered by the user
    //is entered into the database here. I store the values in the ContentValues and insert them into the DB table.
    private void saveData() {
        try {
            myDB = dbAdapter.getWritableDatabase();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        ContentValues values = new ContentValues();
        values.put("NAME", taskName.getText().toString());
        values.put("DETAILS", taskDetails.getText().toString());
        values.put("MINUTE", minute);
        values.put("HOUR", hour);
        values.put("DAY", day);
        values.put("MONTH", month);
        values.put("YEAR", year);
        addAlarmNotification(minute, hour, day, month, year);

        try {
            myDB.insert("TASKS", null, values);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        myDB.close();
        Toast.makeText(this, "Task Added", Toast.LENGTH_SHORT).show();
    }

    //This method is for when the user selects the Save Task button from the View/Update page
    private void updateData(int id) {
        myDB = dbAdapter.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("NAME", taskName.getText().toString());
        values.put("DETAILS", taskDetails.getText().toString());
        values.put("MINUTE", minute);
        values.put("HOUR", hour);
        values.put("DAY", day);
        values.put("MONTH", month);
        values.put("YEAR", year);
        addAlarmNotification(minute, hour, day, month, year);

        try {
            myDB.update("TASKS", values, "_id = " + id, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        myDB.close();
        Toast.makeText(this, "Task Updated!", Toast.LENGTH_SHORT).show();
    }

    //This method creates a standard alarm with the date and time values that are passed in the parameter.
    private void addAlarmNotification(int theMinute, int theHour, int theDay, int theMonth, int theYear) {
        Calendar c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
        c.set(theYear, theMonth, theDay, theHour, theMinute, 1);
        int requestCode = theYear + theMonth + theDay + theHour + theMinute;

        AlarmManager taskAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TaskNotification.class);
        PendingIntent pendIntent = PendingIntent.getService(this, requestCode, intent, 0);
        taskAlarmManager.set(AlarmManager.RTC, c.getTimeInMillis(), pendIntent);
    }

    //This method checks if the user has filled in all the information for creating or updating a task.
    private boolean userInputValidation() {

        if (taskName.getText().toString().equals("") || taskDetails.getText().toString().equals("") || day == -1 || hour == -1 || minute == -1)
            return false;
        return true;
    }

    //This method passes the date values to the convertDate() method.
    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            convertDate(arg1, arg2, arg3);
        }
    };

    //This method checks if the user has selected a valid date. If valid, the date is displayed
    //on the date button. If invalid, a TOAST message is shown telling the user he/she cannot plan for the past
    //and the values of the date are set to -1. It also checks to see if the Time button should be enabled.
    public void convertDate(int arg1, int arg2, int arg3){
        Calendar c = Calendar.getInstance();
        year = arg1;
        month = arg2;
        day = arg3;
        if( checkUpdate == false && (year < c.get(Calendar.YEAR))
                || (month < c.get(Calendar.MONTH) && year <= c.get(Calendar.YEAR))
                || (day < c.get(Calendar.DAY_OF_MONTH) && month <= c.get(Calendar.MONTH) && year <= c.get(Calendar.YEAR))){
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            datePicker.setText("You cannot select a past date");
            Toast.makeText(getBaseContext(), "You can't plan for the past!", Toast.LENGTH_SHORT).show();
            day = -1;
            month = -1;
            year = -1;
            timePicker.setEnabled(false);
        }
        else{
            timePicker.setEnabled(true);
            datePicker.setText("Selected Date - " + day + "/" + (month + 1) + "/" + year);}
    }

    //This method passes the time values to the convertTime() method.
    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker arg0, int arg1, int arg2) {
            convertTime(arg1, arg2);
        }
    };

    //This method checks if the time passed in the arguments is valid. If it is not, a message will appear telling the user,
    //and the minute and hour will be set to -1. If it is valid, this method converts the time to a 12-hour system with
    //AM/PM. It also displays the time on the time button.
    public void convertTime(int theHour, int theMinute){
        Calendar c = Calendar.getInstance();

        hour = theHour;
        minute = theMinute;
        if (checkUpdate == false && day == c.get(Calendar.DAY_OF_MONTH) && month == c.get(Calendar.MONTH) && year == c.get(Calendar.YEAR)
                && (hour < c.get(Calendar.HOUR_OF_DAY) || (hour <= c.get(Calendar.HOUR_OF_DAY) && minute < c.get(Calendar.MINUTE)))  ){
            timePicker.setText("You cannot select a past time");
            Toast.makeText(getBaseContext(), "You can't plan for the past!", Toast.LENGTH_SHORT).show();
            hour = -1;
            minute = -1;
        }

        else if(theHour == 0){
            if(theMinute < 10)
                timePicker.setText("Selected Time - 12" + ":0" + minute + " AM");
            else
                timePicker.setText("Selected Time - 12" + ":" + minute + " AM");
        }
        else if(theHour < 12 && theHour > 0) {
            if (theHour < 10 & theMinute < 10)
                timePicker.setText("Selected Time- 0" + hour + ":0" + minute + " AM");
            else if (theHour < 10 & theMinute >= 10)
                timePicker.setText("Selected Time - 0" + hour + ":" + minute + " AM");
            else if (theHour >= 10 & theMinute < 10)
                timePicker.setText("Selected Time - " + hour + ":0" + minute + " AM");
            else
                timePicker.setText("Selected Time - " + hour + ":" + minute + " AM");}
        else if (theHour == 12){
            if(theMinute < 10)
                timePicker.setText("Selected Time - 12" + ":0" + minute + " PM");
            else
                timePicker.setText("Selected Time - 12" + ":" + minute + " PM");
        }
        else {
            if (theHour < 22 & theMinute < 10)
                timePicker.setText("Selected Time - 0" + (hour-12) + ":0" + minute + " PM");
            else if (theHour < 22 & theMinute >= 10)
                timePicker.setText("Selected Time - 0" + (hour-12) + ":" + minute + " PM");
            else if (theHour >= 22 & theMinute < 10)
                timePicker.setText("Selected Time - " + (hour-12) + ":0" + minute + " PM");
            else
                timePicker.setText("Selected Time - " + (hour-12) + ":" + minute + " PM");
        }
    };

    //This method checks the argument passed in the parameter to know if to display the time or date dialog.
    //It also sets a default value for the dialogs, which is currently set for current time/date.
    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar c = Calendar.getInstance();
        switch (id) {
            case 1:
                DatePickerDialog dateDialog = new DatePickerDialog(this, dateSetListener, c.get(Calendar.YEAR),
                        c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                return dateDialog;

            case 2:
                TimePickerDialog timeDialog = new TimePickerDialog(this, timeSetListener, c.get(Calendar.HOUR),
                        c.get(Calendar.MINUTE), false);
                return timeDialog;
        }
        return null;
    }

    //This method sets the background colour of the Activity.
    public void setActivityBackgroundColor() {
        View view = this.getWindow().getDecorView();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String colourValue = settings.getString("mainBackground", "#ffffff");
        view.setBackgroundColor(Color.parseColor(colourValue));
    }

}