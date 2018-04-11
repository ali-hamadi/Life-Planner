package com.example.ali.newlifeplanner;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import com.example.ali.R;

public class Settings extends PreferenceActivity  {

    //Reads the preferences in the settings XML file to fill up the screen.
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setActivityBackgroundColor();
    }

    //This method sets the background colour of the Settings page.
    public void setActivityBackgroundColor() {
        View view = this.getWindow().getDecorView();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String colourValue = settings.getString("mainBackground", "#ffffff");
        view.setBackgroundColor(Color.parseColor(colourValue));
    }

}
