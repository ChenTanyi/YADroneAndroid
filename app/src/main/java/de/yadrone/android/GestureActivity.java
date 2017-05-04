package de.yadrone.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import de.yadrone.base.IARDrone;

public class GestureActivity extends Activity implements OnDoubleTapListener,OnGestureListener{

    GestureDetector detector;
    TextView action;
    TextView help;
    boolean isFlying = false;
    IARDrone drone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesture);
        action = (TextView) findViewById(R.id.ges_action);
        help = (TextView) findViewById(R.id.ges_help);
        help.setText("手势控制指南：\n上下左右对应飞机前后左右\n斜左上、斜右上：左旋、右旋\n斜左下、斜右下：下降\n长按：上升\n单击或轻触：悬停\n双击：起飞与降落");
        detector = new GestureDetector(this);
        YADroneApplication app = (YADroneApplication)getApplication();
        drone = app.getARDrone();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //unbindService(connection);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.detector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
        drone.getCommandManager().up(40);
        action.setText("上升");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        drone.hover();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float x1 = e1.getX(), x2 = e2.getX(), y1 = e1.getY(), y2 = e2.getY();
        if (y1 - y2 > 120) {
            if (x1 - x2 > 120) {
                drone.getCommandManager().spinLeft(20);
                action.setText("左旋");
            } else if (x2 - x1 > 120) {
                drone.getCommandManager().spinRight(40);
                action.setText("右旋");
            } else {
                drone.getCommandManager().forward(20);
                action.setText("前进");
            }
        } else if (y2 - y1 > 120) {
            if (Math.abs(x1 - x2) > 120) {
                drone.getCommandManager().down(40);
                action.setText("下降");
            } else {
                drone.getCommandManager().backward(20);
                action.setText("后退");
            }
        } else {
            if (x1 - x2 > 120) {
                drone.getCommandManager().goLeft(20);
                action.setText("向左");
            } else if (x2 - x1 > 120) {
                drone.getCommandManager().goRight(20);
                action.setText("向右");
            } else {
                drone.hover();
                action.setText("悬停");
            }
        }
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // TODO Auto-generated method stub
        drone.hover();
        action.setText("悬停");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        // TODO Auto-generated method stub
        if (isFlying){
            drone.landing();
            action.setText("降落");
        } else {
            drone.takeOff();
            action.setText("起飞");
        }
        isFlying = !isFlying;
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }


    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_gravity, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
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
            case R.id.menuitem_gravity:
                i = new Intent(this, GravityActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
