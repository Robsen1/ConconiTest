package at.fhooe.mc.conconii;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private ArrayList<ActualData> mDataList = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
