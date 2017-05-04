package de.yadrone.android;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.yadrone.base.IARDrone;

/**
 * Sample code that invokes the speech recognition intent API.
 */
public class VoiceActivity extends Activity implements OnClickListener {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private ListView mList;

    private TextView action;

    private IARDrone drone;
    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.activity_voice);

        // Get display items for later interaction
        Button speakButton = (Button) findViewById(R.id.v_recognize);

        mList = (ListView) findViewById(R.id.v_result);
        action = (TextView) findViewById(R.id.v_action);

        YADroneApplication app = (YADroneApplication) getApplication();
        drone = app.getARDrone();

        Button emergency = (Button) findViewById(R.id.v_emergency);
        emergency.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drone.reset();
            }
        });

        Button landing = (Button) findViewById(R.id.v_land);
        landing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                drone.landing();
            }
        });

        // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            speakButton.setOnClickListener(this);
        } else {
            speakButton.setEnabled(false);
            speakButton.setText("Recognizer not present");
        }
    }

    /**
     * Handle the click on the start recognition button.
     */
    public void onClick(View v) {
        if (v.getId() == R.id.v_recognize) {
            startVoiceRecognitionActivity();
        }
    }

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    matches));
            String result = matches.toString();
            action.setText(result);
            if (result.contains("起飞")){
                action.setText("起飞");
                drone.takeOff();
            } else if (result.contains("降落")){
                action.setText("降落");
                drone.landing();
            } else if (result.contains("前进")){
                action.setText("前进");
                drone.getCommandManager().forward(20);
            } else if (result.contains("后退")){
                action.setText("后退");
                drone.getCommandManager().backward(20);
            } else if (result.contains("上升")){
                action.setText("上升");
                drone.getCommandManager().up(40);
            } else if (result.contains("下降")){
                action.setText("下降");
                drone.getCommandManager().down(40);
            } else if (result.contains("左旋")){
                action.setText("左旋");
                drone.getCommandManager().spinLeft(20);
            } else if (result.contains("右旋")){
                action.setText("右旋");
                drone.getCommandManager().spinRight(40);
            } else if (result.contains("左")){
                action.setText("向左");
                drone.getCommandManager().goLeft(20);
            } else if (result.contains("右")){
                action.setText("向右");
                drone.getCommandManager().goRight(20);
            } else if (result.contains("悬停") || result.contains("停")){
                action.setText("悬停");
                drone.hover();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            drone.hover();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        switch (item.getItemId())
        {
            case R.id.menuitem_main:
                i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_navdata:
                i = new Intent(this, NavDataActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_control:
                i = new Intent(this, ControlActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_video:
                i = new Intent(this, de.yadrone.android.videodeprecated.VideoActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_gravity:
                i = new Intent(this, GravityActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_gesture:
                i = new Intent(this, GestureActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}