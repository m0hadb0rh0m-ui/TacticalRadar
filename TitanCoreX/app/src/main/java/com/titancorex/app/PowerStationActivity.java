package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class PowerStationActivity extends Activity {
 private final int BG=0xff080C16,CYAN=0xff42E8F5,WHITE=0xffF4F8FF,MUTED=0xffABC1D6,GOLD=0xffFFD07D,GREEN=0xff4EE5A4;
 private LinearLayout root,live,history;
 private Handler handler=new Handler(Looper.getMainLooper());
 private Runnable ticker=new Runnable(){public void run(){update();handler.postDelayed(this,3000);}};
 private long startTime=0;private int startPct=-1;private float maxTemp=-1;
 private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private TextView txt(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(8),dp(7),dp(8),dp(7));return v;}
 private GradientDrawable cardBg(){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff1A304A,0xff101D30});d.setCornerRadius(dp(19));d.setStroke(dp(1),0xff31546B);return d;}
 private LinearLayout card(String title){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(13),dp(13),dp(13),dp(13));c.setBackground(cardBg());c.addView(txt(title,18,CYAN));root.addView(c);View gap=new View(this);root.addView(gap,new LinearLayout.LayoutParams(1,dp(14)));return c;}
 private Intent battery(){return registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));}
 private int pct(Intent i){if(i==null)return -1;int n=i.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),s=i.getIntExtra(BatteryManager.EXTRA_SCALE,-1);return n<0||s<=0?-1:n*100/s;}
 private float temp(Intent i){if(i==null)return -1;int n=i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);return n<0?-1:n/10f;}
 private String fmt(double n){return String.format(Locale.US,"%.2f",n);}
 private String state(int n){if(n==BatteryManager.BATTERY_STATUS_CHARGING)return "يشحن";if(n==BatteryManager.BATTERY_STATUS_FULL)return "ممتلئ";if(n==BatteryManager.BATTERY_STATUS_DISCHARGING)return "يفرغ";return "غير معروف";}
 @Override public void onCreate(Bundle b){
  super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(18),dp(22),dp(18),dp(24));root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
  root.addView(txt("⚡  TITAN POWER STATION",25,CYAN));root.addView(txt("مختبر الشحن والطاقة الاحترافي",16,WHITE));
  root.addView(txt("القراءات من واجهات Android المتاحة؛ التيار والقدرة قد لا يتوفران أو قد يعكسان استهلاك البطارية الصافي، لا قدرة الشاحن الفعلية.",12,MUTED));
  live=card("القياسات المباشرة");
  LinearLayout session=card("جلسة مراقبة الشحن");
  Button start=new Button(this);start.setText("بدء تسجيل جلسة");session.addView(start);
  start.setOnClickListener(v->{Intent i=battery();startTime=System.currentTimeMillis();startPct=pct(i);maxTemp=temp(i);update();Toast.makeText(this,"بدأت جلسة القياس",Toast.LENGTH_SHORT).show();});
  Button stop=new Button(this);stop.setText("إنهاء الجلسة وحفظ تقريرها");session.addView(stop);
  stop.setOnClickListener(v->{if(startTime==0){Toast.makeText(this,"ابدأ جلسة أولًا",Toast.LENGTH_SHORT).show();return;}
   long mins=Math.max(0,(System.currentTimeMillis()-startTime)/60000);int end=pct(battery());
   String row=System.currentTimeMillis()+"|"+mins+"|"+startPct+"|"+end+"|"+fmt(maxTemp);
   android.content.SharedPreferences p=getSharedPreferences("power_sessions",0);
   String previous=p.getString("rows","");String[] parts=(previous+row+"\n").split("\n");
   StringBuilder out=new StringBuilder();for(int j=Math.max(0,parts.length-30);j<parts.length;j++)if(!parts[j].isEmpty())out.append(parts[j]).append("\n");
   p.edit().putString("rows",out.toString()).apply();startTime=0;showHistory();update();
  });
  history=card("سجل جلسات الشحن");
  Button share=new Button(this);share.setText("مشاركة سجل الجلسات");root.addView(share);
  share.setOnClickListener(v->{Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,"TITAN POWER STATION\n"+getSharedPreferences("power_sessions",0).getString("rows",""));startActivity(Intent.createChooser(send,"مشاركة سجل الطاقة"));});
  Button clear=new Button(this);clear.setText("مسح سجل الجلسات");root.addView(clear);
  clear.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("حذف سجل جلسات الشحن المحلي؟").setNegativeButton("إلغاء",null).setPositiveButton("حذف",(d,w)->{getSharedPreferences("power_sessions",0).edit().remove("rows").apply();showHistory();}).show());
  showHistory();update();
 }
 private void update(){
  if(live==null)return;
  while(live.getChildCount()>1)live.removeViewAt(1);
  Intent i=battery();int level=pct(i),status=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
  int voltage=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1);
  float temperature=temp(i);if(startTime>0)maxTemp=Math.max(maxTemp,temperature);
  BatteryManager bm=(BatteryManager)getSystemService(BATTERY_SERVICE);
  long current=bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
  long charge=bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
  live.addView(txt("البطارية: "+(level<0?"غير متاح":level+"%")+"   |   "+state(status),17,WHITE));
  live.addView(txt("درجة حرارة البطارية: "+(temperature<0?"غير متاحة":fmt(temperature)+" °C"),15,temperature>=42?GOLD:GREEN));
  live.addView(txt("الجهد: "+(voltage<=0?"غير متاح":voltage+" mV"),15,WHITE));
  boolean currentOk=current!=Long.MIN_VALUE&&current!=Integer.MIN_VALUE&&Math.abs(current)<100000000;
  live.addView(txt("تيار البطارية الصافي: "+(currentOk?fmt(current/1000.0)+" mA":"غير متاح"),15,WHITE));
  live.addView(txt("قدرة البطارية الصافية التقديرية: "+(currentOk&&voltage>0?fmt(Math.abs(current/1000000.0)*voltage/1000.0)+" W":"غير متاحة"),15,CYAN));
  live.addView(txt("الشحنة المتبقية: "+(charge>0&&charge<1000000000?fmt(charge/1000.0)+" mAh":"غير متاحة"),15,WHITE));
  if(startTime>0)live.addView(txt("جلسة نشطة منذ "+((System.currentTimeMillis()-startTime)/60000)+" دقيقة | البداية "+startPct+"% | الذروة "+fmt(maxTemp)+"°C",13,GOLD));
  live.addView(txt("هذه القراءات ليست اختبارًا معتمدًا لصحة البطارية أو قدرة الشاحن.",12,MUTED));
 }
 private void showHistory(){
  if(history==null)return;while(history.getChildCount()>1)history.removeViewAt(1);
  String raw=getSharedPreferences("power_sessions",0).getString("rows","");
  if(raw.isEmpty()){history.addView(txt("لا توجد جلسات محفوظة",13,MUTED));return;}
  String[] rows=raw.trim().split("\n");
  for(int j=rows.length-1;j>=0;j--){
   String[] a=rows[j].split("\\|");if(a.length<5)continue;
   try{String date=new SimpleDateFormat("MM-dd HH:mm",Locale.getDefault()).format(new Date(Long.parseLong(a[0])));
    history.addView(txt(date+" | "+a[1]+" دقيقة | "+a[2]+"% → "+a[3]+"% | أعلى حرارة "+a[4]+"°C",12,WHITE));
   }catch(Exception ignored){}
  }
 }
 @Override protected void onResume(){super.onResume();handler.removeCallbacks(ticker);handler.post(ticker);}
 @Override protected void onPause(){handler.removeCallbacks(ticker);super.onPause();}
}
