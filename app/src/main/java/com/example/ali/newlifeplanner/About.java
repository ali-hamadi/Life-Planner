package com.example.ali.newlifeplanner;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import com.example.ali.R;

public class About extends Activity {

    TextView aboutTextView;

    //Displays the About activity by reading the about XML file.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.about);
        aboutTextView = (TextView)findViewById(R.id.tvAbout);
    }

}
