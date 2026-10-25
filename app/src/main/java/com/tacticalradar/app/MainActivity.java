package com.tacticalradar.app;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.content.*;
import android.hardware.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager sensors;
    private Sensor rotation;
    private RadarView radar;
    private TextView heading, status;
    private final ArrayList<Target> targets = new ArrayList<>();
    private float bearing = 0f;
    private boolean active = true;
    private final int green = Color.rgb(20, 255, 137);
    static class Target {
        String name; float angle, range;
        Target(String n, float a, float r){name=n;angle=a;range=r;}
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(3, 11, 15));
        getWindow().setNavigationBarColor(Color.rgb(3, 11, 15));
        sensors=(SensorManager)getSystemService(SENSOR_SERVICE);
        rotation=sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(22),dp(18),dp(14));
        root.setBackgroundColor(Color.rgb(4,12,17));
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);scroll.addView(root);setContentView(scroll);
        TextView title=text("TACTICAL RADAR",26,green);title.setTypeface(null,1);root.addView(title);
        root.addView(text("Radar dashboard and device compass",14,0xffa1bcb9));
        radar=new RadarView(this);
        root.addView(radar,new LinearLayout.LayoutParams(-1,dp(340)));
        heading=text("Compass: unavailable",17,green);root.addView(heading);
        status=text(rotation==null?"Compass sensor unavailable":"Compass sensor ready",13,0xffb0c8c5);root.addView(status);
        TextView disclaimer=text("Targets are added manually. This app does not locate Wi-Fi or Bluetooth devices using signal strength.",13,0xffe5bd74);
        disclaimer.setPadding(0,dp(12),0,dp(12));root.addView(disclaimer);
        EditText name=input("Target name");root.addView(name);
        EditText angle=input("Angle (0 to 359 degrees)");angle.setInputType(2);root.addView(angle);
        EditText range=input("Relative distance (0 to 100)");range.setInputType(2);root.addView(range);
        Button add=button("Add target");root.addView(add);
        add.setOnClickListener(v->{
            try{
                float a=Float.parseFloat(angle.getText().toString());
                float r=Float.parseFloat(range.getText().toString());
                if(a<0||a>=360||r<0||r>100)throw new Exception();
                targets.add(new Target(name.getText().toString().trim().isEmpty()?"Target "+(targets.size()+1):name.getText().toString().trim(),a,r));
                radar.invalidate();name.setText("");angle.setText("");range.setText("");
                status.setText("Manual targets: "+targets.size());
            }catch(Exception ex){Toast.makeText(this,"Enter angle 0-359 and range 0-100",Toast.LENGTH_LONG).show();}
        });
        Button clear=button("Clear targets");root.addView(clear);
        clear.setOnClickListener(v->{targets.clear();radar.invalidate();status.setText("Targets cleared");});
        Button toggle=button("Pause / Resume radar");root.addView(toggle);
        toggle.setOnClickListener(v->{active=!active;radar.invalidate();status.setText(active?"Radar animation active":"Radar animation paused");});
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(0,dp(7),0,dp(7));return t;}
    private EditText input(String hint){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(0xff849e9e);e.setTextColor(Color.WHITE);e.setSingleLine(true);return e;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(0xffc7ffe2);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xff126346));return b;}
    @Override protected void onResume(){super.onResume();if(rotation!=null)sensors.registerListener(this,rotation,SensorManager.SENSOR_DELAY_UI);}
    @Override protected void onPause(){super.onPause();sensors.unregisterListener(this);}
    @Override public void onSensorChanged(SensorEvent e){
        if(e.sensor.getType()!=Sensor.TYPE_ROTATION_VECTOR)return;
        float[] matrix=new float[9],values=new float[3];
        SensorManager.getRotationMatrixFromVector(matrix,e.values);
        SensorManager.getOrientation(matrix,values);
        bearing=(float)((Math.toDegrees(values[0])+360)%360);
        heading.setText(String.format(java.util.Locale.US,"Compass: %.0f degrees (approx. magnetic north)",bearing));
        radar.invalidate();
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    class RadarView extends View {
        Paint p=new Paint(3);long start=System.currentTimeMillis();
        RadarView(Context c){super(c);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float cx=getWidth()/2f,cy=getHeight()/2f;
            float radius=Math.min(getWidth(),getHeight())*.45f;
            p.setColor(0xff08251d);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy,radius,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));
            p.setColor(0xff216047);
            for(int i=1;i<=4;i++)c.drawCircle(cx,cy,radius*i/4,p);
            c.drawLine(cx-radius,cy,cx+radius,cy,p);c.drawLine(cx,cy-radius,cx,cy+radius,p);
            p.setStyle(Paint.Style.FILL);
            if(active){
                float sweep=(System.currentTimeMillis()-start)%4000*360f/4000;
                p.setColor(0x5543ff92);
                c.drawArc(cx-radius,cy-radius,cx+radius,cy+radius,sweep-28,28,true,p);
                postInvalidateDelayed(40);
            }
            p.setColor(green);c.drawCircle(cx,cy,dp(5),p);
            p.setTextSize(dp(12));p.setColor(0xffa4ffcc);c.drawText("N",cx-dp(5),cy-radius-dp(5),p);
            for(Target t:targets){
                double a=Math.toRadians(t.angle);
                float x=cx+(float)Math.sin(a)*radius*t.range/100;
                float y=cy-(float)Math.cos(a)*radius*t.range/100;
                p.setColor(0xffffcc66);c.drawCircle(x,y,dp(5),p);
                p.setTextSize(dp(11));c.drawText(t.name,x+dp(7),y-dp(7),p);
            }
        }
    }
}
