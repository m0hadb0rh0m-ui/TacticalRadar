package com.titancorex.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class TrendsActivity extends Activity {
  private final int BG=0xffF1F8FF,FG=0xff173557,CYAN=0xff087BEA,MUTED=0xff526B88;
  private LinearLayout root;
  private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
  private TextView t(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(5),dp(8),dp(5),dp(8));return v;}
  private String snapshot(){
    Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    int level=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_LEVEL,-1);
    int temp=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);
    ActivityManager.MemoryInfo m=new ActivityManager.MemoryInfo();((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(m);
    android.os.StatFs f=new android.os.StatFs(android.os.Environment.getDataDirectory().getPath());
    return System.currentTimeMillis()+","+level+","+temp+","+m.availMem+","+f.getAvailableBytes();
  }
  private ArrayList<String> load(){
    String raw=getSharedPreferences("titan_trends",0).getString("history","");
    ArrayList<String> rows=new ArrayList<>();for(String s:raw.split("\n"))if(!s.isEmpty())rows.add(s);return rows;
  }
  private void save(ArrayList<String> rows){
    while(rows.size()>120)rows.remove(0);
    StringBuilder b=new StringBuilder();for(String s:rows)b.append(s).append("\n");
    getSharedPreferences("titan_trends",0).edit().putString("history",b.toString()).apply();
  }
  @Override public void onCreate(Bundle b){
    super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
    ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(18),dp(24),dp(18),dp(20));root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
    root.addView(t("◈ TITAN TRENDS LAB",25,CYAN));
    root.addView(t("سجل حالة البطارية والرام والتخزين",16,FG));
    root.addView(t("يتم حفظ القياسات فقط عند الضغط على تسجيل قراءة. لا توجد مراقبة خفية في الخلفية، وتُحفظ آخر 120 قراءة محليًا.",13,MUTED));
    Button capture=new Button(this);capture.setText("＋ تسجيل قراءة الآن");root.addView(capture);
    capture.setOnClickListener(v->{ArrayList<String> rows=load();rows.add(snapshot());save(rows);render();});
    Button share=new Button(this);share.setText("مشاركة السجل");root.addView(share);
    share.setOnClickListener(v->{StringBuilder sb=new StringBuilder("TITAN CORE X - Local trends\n");for(String row:load())sb.append(row).append("\n");Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,sb.toString());startActivity(Intent.createChooser(i,"مشاركة التقرير"));});
    Button clear=new Button(this);clear.setText("حذف السجل المحلي");root.addView(clear);
    clear.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("حذف جميع القراءات المسجلة؟").setNegativeButton("إلغاء",null).setPositiveButton("حذف",(d,w)->{save(new ArrayList<>());render();}).show());
    render();
  }
  private void render(){
    if(root.getChildCount()>6)root.removeViews(6,root.getChildCount()-6);
    ArrayList<String> rows=load();
    root.addView(t("عدد القراءات: "+rows.size(),15,CYAN));
    if(rows.size()>1){
      root.addView(new TrendGraph(this,rows));
    }
    Collections.reverse(rows);
    int count=0;
    for(String row:rows){
      if(count++>=25)break;
      try{
        String[] a=row.split(",");
        String time=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date(Long.parseLong(a[0])));
        double temp=Double.parseDouble(a[2])/10.0;
        String message=time+"   |   بطارية "+a[1]+"%   |   "+String.format(Locale.US,"%.1f °C",temp)+"   |   RAM "+String.format(Locale.US,"%.1f GB",Long.parseLong(a[3])/1073741824.0);
        TextView item=t(message,12,FG);item.setBackgroundColor(0xff142136);root.addView(item);
      }catch(Exception ignored){}
    }
  }
  static class TrendGraph extends View{
    private final ArrayList<Float> points=new ArrayList<>();
    private final Paint p=new Paint(3);
    TrendGraph(Context c,ArrayList<String> rows){
      super(c);for(String row:rows)try{points.add(Float.parseFloat(row.split(",")[2])/10f);}catch(Exception ignored){}
      setMinimumHeight((int)(170*getResources().getDisplayMetrics().density));
    }
    @Override protected void onMeasure(int w,int h){setMeasuredDimension(MeasureSpec.getSize(w),(int)(180*getResources().getDisplayMetrics().density));}
    @Override protected void onDraw(Canvas c){
      super.onDraw(c);float w=getWidth(),h=getHeight();
      p.setColor(0xff142136);c.drawRoundRect(0,0,w,h,20,20,p);
      p.setColor(0xff9AB5C9);p.setTextSize(28);c.drawText("Battery temperature (°C) • saved readings",18,38,p);
      if(points.size()<2)return;
      float min=Float.MAX_VALUE,max=-Float.MAX_VALUE;for(float v:points){min=Math.min(min,v);max=Math.max(max,v);}
      if(max-min<2){min-=1;max+=1;}
      p.setColor(0xff42E8F5);p.setStrokeWidth(5);p.setStyle(Paint.Style.STROKE);
      Path path=new Path();for(int i=0;i<points.size();i++){
        float x=18+(w-36)*i/(points.size()-1f);
        float y=65+(h-90)*(1-(points.get(i)-min)/(max-min));
        if(i==0)path.moveTo(x,y);else path.lineTo(x,y);
      }
      c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
    }
  }
}
