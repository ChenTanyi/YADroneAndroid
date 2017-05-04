package de.yadrone.android;

import java.util.Calendar;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;

public class GravityActivity extends Activity implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private int mX, mY, mZ;
    private EditText mEditText;
    private TextView action;
    private int method = 0;

    private long lasttimestamp = 0;
    Calendar mCalendar;

    boolean isFlying = false;
    private IARDrone drone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gravity);
        mEditText = (EditText) findViewById(R.id.g_editText);
        mEditText.setEnabled(false);
        mEditText.setText("悬停");
        action = (TextView) findViewById(R.id.g_action);

        YADroneApplication app = (YADroneApplication) getApplication();
        drone = app.getARDrone();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (null == mSensorManager) {
            Log.d(TAG, "deveice not support SensorManager");
        }
        // 参数三，检测的精准度
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);// SENSOR_DELAY_GAME

        Button horizontal = (Button) findViewById(R.id.g_horizontal);
        horizontal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("水平");
                method = 1;
            }
        });

        Button vertical = (Button) findViewById(R.id.g_vertical);
        vertical.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("垂直");
                method = 2;
            }
        });

        Button hover = (Button) findViewById(R.id.g_hover);
        hover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("悬停");
                method = 0;
            }
        });


        final Button landing = (Button) findViewById(R.id.g_land);
        landing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFlying){
                    drone.landing();
                    landing.setText("Take Off");
                }
                else{
                    drone.takeOff();
                    landing.setText("Land");
                }
                isFlying = !isFlying;
            }
        });
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        mSensorManager.registerListener(this, mSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        super.onResume();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null) {
            return;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            int x = (int) event.values[0];
            int y = (int) event.values[1];
            int z = (int) event.values[2];
            mCalendar = Calendar.getInstance();
            long stamp = mCalendar.getTimeInMillis() / 1000l;// 1393844912

            //textviewX.setText(String.valueOf(x));
            //textviewY.setText(String.valueOf(y));
            //textviewZ.setText(String.valueOf(z));

            int second = mCalendar.get(Calendar.SECOND);// 53

            int px = Math.abs(mX - x);
            int py = Math.abs(mY - y);
            int pz = Math.abs(mZ - z);
            Log.d(TAG, "pX:" + px + "  pY:" + py + "  pZ:" + pz + "    stamp:"
                    + stamp + "  second:" + second);
            int maxvalue = getMaxValue(px, py, pz);
            if (maxvalue > 2 && (stamp - lasttimestamp) > 30) {
                lasttimestamp = stamp;
                Log.d(TAG, " sensor isMoveorchanged....");
                //textviewF.setText("检测手机在移动..");
            }

            mX = x;
            mY = y;
            mZ = z;

            if (method == 1) {
                if (y < -1) {
                    drone.getCommandManager().forward(20);
                    action.setText("前进");
                }
                else if (y > 1) {
                    drone.getCommandManager().backward(20);
                    action.setText("后退");
                }
                else if (x < -3) {
                    drone.getCommandManager().goRight(20);
                    action.setText("向右");
                }
                else if (x > 3) {
                    drone.getCommandManager().goLeft(20);
                    action.setText("向左");
                }
                else {
                    drone.hover();
                    action.setText("悬停");
                }
            }
            else if (method == 2) {
                if (y < -1) {
                    drone.getCommandManager().up(40);
                    action.setText("上升");
                }
                else if (y > 1) {
                    drone.getCommandManager().down(40);
                    action.setText("下降");
                }
                else if (x < -3) {
                    drone.getCommandManager().spinRight(40);
                    action.setText("右旋");
                }
                else if (x > 3) {
                    drone.getCommandManager().spinLeft(20);
                    action.setText("左旋");
                }
                else {
                    drone.hover();
                    action.setText("悬停");
                }
            }
            else {
                drone.hover();
                action.setText("悬停");
            }

        }

    }

    /**
     * 获取一个最大值
     *
     * @param px
     * @param py
     * @param pz
     * @return
     */
    public int getMaxValue(int px, int py, int pz) {
        int max = 0;
        if (px > py && px > pz) {
            max = px;
        } else if (py > px && py > pz) {
            max = py;
        } else if (pz > px && pz > py) {
            max = pz;
        }

        return max;
    }

    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gravity, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent i;
        switch (item.getItemId())
        {
            case R.id.menuitem_navdata:
                i = new Intent(this, NavDataActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_main:
                i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_video:
                i = new Intent(this, de.yadrone.android.videodeprecated.VideoActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_voice:
                i = new Intent(this, VoiceActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            case R.id.menuitem_control:
                i = new Intent(this, ControlActivity.class);
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
