package com.example.ali.newlifeplanner;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.Calendar;
import android.content.SharedPreferences;
import android.app.Notification;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import com.example.ali.R;

public class TaskNotification extends Service {

    Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //This method is called every time the client calls its service.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        notificationCheck();
                    }
                });
            }
        }).start();
        return START_STICKY;
    }

    //This method checks every attribute for time and date to see if it is equal to current time and date.
    //If a match is found, it will call showNotification().
    public void notificationCheck() {
        final int DATABASE_VERSION = 1;
        final String DATABASE_NAME = "entries.db";

        Calendar c = Calendar.getInstance();
        Database dbAdapter = new Database(this, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase myDB;
        myDB = dbAdapter.getWritableDatabase();
        int minute = c.get(Calendar.MINUTE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH);
        int year = c.get(Calendar.YEAR);

        Cursor cursor = myDB.rawQuery("SELECT _id, NAME, DETAILS, MINUTE, HOUR, DAY, MONTH, YEAR FROM TASKS", null);
        cursor.moveToFirst();

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(3) == minute && cursor.getInt(4) == hour && cursor.getInt(5) == day && cursor.getInt(6) == month
                        && cursor.getInt(7) == year) {
                    displayNotification(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
                }
            } while (cursor.moveToNext());
        }
        myDB.close();
        stopSelf();
    }

    //This method takes the task name and task details, along with the ID of the task. Only
    //the name and details will be displayed in the notification message.
    public void displayNotification(int taskID, String Name, String details) {
        Intent intent = new Intent(this, MainActivity.class);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = settings.getString("userName", "User");
        PendingIntent pendIntent = PendingIntent.getActivity(this, taskID, intent, 0);
        Notification alarmNotification = new NotificationCompat.Builder(this)
                .setContentTitle("Hey " + userName + "! Time For " + Name + ".")
                .setContentText(details).setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendIntent).build();
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        alarmNotification.flags |= Notification.FLAG_AUTO_CANCEL;
        alarmNotification.defaults |= Notification.DEFAULT_ALL;
        mgr.notify(taskID, alarmNotification);
    }

}
