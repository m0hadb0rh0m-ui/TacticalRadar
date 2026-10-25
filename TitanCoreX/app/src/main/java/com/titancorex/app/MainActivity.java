package com.titancorex.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
  private final int BG=0xff080C16, CARD=0xff121B2B, PANEL=0xff19263A, CYAN=0xff42E8F5, WHITE=0xffF4F8FF, MUTED=0xff9CAFC7, GREEN=0xff4EF0A3, GOLD=0xffFFCE77, RED=0xffFF7288;
  private LinearLayout content, navigation;
  private TextView subtitle;
  private int page=0;
  private BatteryManager battery;
  private android.app.ActivityManager memory;
  private final String[] sections={"HOME","DEVICE","SECURITY","BOOST","TOOLS"};
  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
    battery=(BatteryManager)getSystemService(BATTERY_SERVICE);
    memory=(android.app.ActivityManager)getSystemService(ACTIVITY_SERVICE);
    buildShell();show(0);
  }
  private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}
  private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  private GradientDrawable border(int color,int stroke,int radius){GradientDrawable d=shape(color,radius);d.setStroke(dp(1),stroke);return d;}
  private TextView label(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setGravity(Gravity.CENTER_VERTICAL);if(bold)t.setTypeface(android.graphics.Typeface.DEFAULT,1);return t;}
  private LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
  private LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
  private void pad(View v,int a,int b,int c,int d){v.setPadding(dp(a),dp(b),dp(c),dp(d));}
  private void space(LinearLayout p,int height){View v=new View(this);p.addView(v,new LinearLayout.LayoutParams(1,dp(height)));}
  private void add(LinearLayout p,View v){p.addView(v,new LinearLayout.LayoutParams(-1,-2));}
  private void buildShell(){
    LinearLayout root=vertical();root.setBackgroundColor(BG);setContentView(root);
    LinearLayout top=vertical();pad(top,22,18,22,14);top.setBackgroundColor(BG);
    TextView brand=label("◈  TITAN CORE X",25,WHITE,true);add(top,brand);
    TextView strap=label("SYSTEM INTELLIGENCE  /  ANDROID",10,CYAN,true);strap.setLetterSpacing(.17f);add(top,strap);
    subtitle=label("COMMAND CENTER",11,MUTED,true);pad(subtitle,0,10,0,0);add(top,subtitle);
    add(root,top);
    ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);
    content=vertical();pad(content,18,12,18,18);scroll.addView(content);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    navigation=horizontal();pad(navigation,7,8,7,10);navigation.setBackgroundColor(0xff101929);
    add(root,navigation);
  }
  private LinearLayout card(String title,String desc){
    LinearLayout c=vertical();pad(c,18,17,18,18);c.setBackground(border(CARD,0xff233B53,19));
    TextView t=label(title,17,WHITE,true);add(c,t);
    if(desc!=null){space(c,5);add(c,label(desc,12,MUTED,false));}
    return c;
  }
  private void block(LinearLayout c){add(content,c);space(content,14);}
  private void section(String title){TextView t=label(title,17,WHITE,true);pad(t,3,9,0,12);add(content,t);}
  private void metric(LinearLayout p,String name,String value,int color){
    LinearLayout line=horizontal();pad(line,0,9,0,9);
    TextView n=label(name,13,MUTED,false);line.addView(n,new LinearLayout.LayoutParams(0,-2,1));
    TextView v=label(value,14,color,true);line.addView(v);add(p,line);
    View sep=new View(this);sep.setBackgroundColor(0xff223248);p.addView(sep,new LinearLayout.LayoutParams(-1,dp(1)));
  }
  private void action(LinearLayout p,String title,String info,Runnable run){
    LinearLayout row=horizontal();pad(row,14,14,12,14);row.setBackground(shape(PANEL,13));
    LinearLayout texts=vertical();texts.addView(label(title,14,WHITE,true));
    if(info!=null){space(texts,4);texts.addView(label(info,11,MUTED,false));}
    row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
    row.addView(label("›",27,CYAN,true));
    row.setOnClickListener(v->run.run());add(p,row);space(p,9);
  }
  private void nav(){
    navigation.removeAllViews();
    for(int i=0;i<sections.length;i++){
      final int index=i;
      LinearLayout cell=vertical();cell.setGravity(Gravity.CENTER);pad(cell,1,8,1,8);
      if(i==page)cell.setBackground(shape(0xff173448,12));
      String[] symbols={"◈","▣","⬡","⚡","☷"};
      TextView ico=label(symbols[i],21,i==page?CYAN:MUTED,true);ico.setGravity(Gravity.CENTER);add(cell,ico);
      TextView txt=label(sections[i],9,i==page?CYAN:MUTED,true);txt.setGravity(Gravity.CENTER);add(cell,txt);
      navigation.addView(cell,new LinearLayout.LayoutParams(0,-2,1));
      cell.setOnClickListener(v->show(index));
    }
  }
  private void show(int index){
    page=index;content.removeAllViews();nav();subtitle.setText(new String[]{"COMMAND CENTER","DEVICE DIAGNOSTICS","SECURITY AUDIT","GAME PERFORMANCE","SYSTEM UTILITIES"}[page]);
    if(page==0)home();else if(page==1)device();else if(page==2)security();else if(page==3)boost();else tools();
  }
  private Intent batteryIntent(){return registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));}
  private int level(){Intent i=batteryIntent();if(i==null)return -1;int l=i.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),s=i.getIntExtra(BatteryManager.EXTRA_SCALE,100);return l<0||s<=0?-1:l*100/s;}
  private float temp(){Intent i=batteryIntent();return i==null?-1:i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1)/10f;}
  private String format(long bytes){if(bytes<0)return "Unavailable";return String.format(Locale.US,"%.1f GB",bytes/1073741824.0);}
  private android.app.ActivityManager.MemoryInfo ram(){android.app.ActivityManager.MemoryInfo m=new android.app.ActivityManager.MemoryInfo();memory.getMemoryInfo(m);return m;}
  private android.os.StatFs disk(){return new android.os.StatFs(android.os.Environment.getDataDirectory().getPath());}
  private int score(){
    int result=100;
    android.app.ActivityManager.MemoryInfo m=ram();
    double used=1.0-(double)m.availMem/m.totalMem;
    if(used>.85)result-=18;else if(used>.7)result-=8;
    float t=temp();if(t>=43)result-=25;else if(t>=39)result-=12;
    android.os.StatFs f=disk();double free=(double)f.getAvailableBytes()/f.getTotalBytes();
    if(free<.1)result-=22;else if(free<.2)result-=10;
    return Math.max(0,result);
  }
  private void home(){
    LinearLayout hero=vertical();pad(hero,22,23,22,23);hero.setBackground(border(0xff102B39,0xff22667A,24));
    add(hero,label("LIVE DEVICE HEALTH",12,CYAN,true));space(hero,10);
    add(hero,label(score()+" / 100",47,WHITE,true));
    add(hero,label("Estimated from RAM, battery temperature and storage headroom",12,MUTED,false));
    space(hero,14);
    ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(100);bar.setProgress(score());bar.setProgressTintList(android.content.res.ColorStateList.valueOf(CYAN));add(hero,bar);
    block(hero);
    section("LIVE TELEMETRY");
    LinearLayout c=card("Device snapshot","Values read from Android system APIs");
    metric(c,"Battery level",level()<0?"Unavailable":level()+"%",GREEN);
    metric(c,"Battery temperature",temp()<0?"Unavailable":String.format(Locale.US,"%.1f °C",temp()),CYAN);
    android.app.ActivityManager.MemoryInfo m=ram();
    metric(c,"RAM available",format(m.availMem)+" / "+format(m.totalMem),WHITE);
    metric(c,"Storage available",format(disk().getAvailableBytes()),GOLD);
    block(c);
    LinearLayout actions=card("QUICK ACCESS","Launch diagnostics and safe system utilities");
    action(actions,"Run device inspection","Battery, RAM, storage and network",()->show(1));
    action(actions,"Review security posture","Installed apps and permissions",()->show(2));
    action(actions,"Gaming performance hub","Prepare a game session",()->show(3));
    block(actions);
    note("Privacy first: measurements stay on your device. No cloud upload or background scanning in this release.");
  }
  private void device(){
    section("HARDWARE & SYSTEM");
    LinearLayout c=card("DEVICE PROFILE","Live Android properties");
    metric(c,"Manufacturer",android.os.Build.MANUFACTURER,WHITE);
    metric(c,"Model",android.os.Build.MODEL,WHITE);
    metric(c,"Android",android.os.Build.VERSION.RELEASE,WHITE);
    metric(c,"API level",String.valueOf(android.os.Build.VERSION.SDK_INT),WHITE);
    metric(c,"CPU cores",String.valueOf(Runtime.getRuntime().availableProcessors()),CYAN);
    metric(c,"Available RAM",format(ram().availMem),GREEN);
    metric(c,"Total RAM",format(ram().totalMem),WHITE);
    metric(c,"Storage free",format(disk().getAvailableBytes()),GREEN);
    metric(c,"Storage total",format(disk().getTotalBytes()),WHITE);
    block(c);
    section("BATTERY LAB");
    LinearLayout b=card("BATTERY ANALYZER","Readings depend on manufacturer support");
    Intent i=batteryIntent();
    int status=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
    metric(b,"Charging",status==BatteryManager.BATTERY_STATUS_CHARGING?"Yes":status==BatteryManager.BATTERY_STATUS_FULL?"Full":"No / unknown",GREEN);
    metric(b,"Temperature",temp()<0?"Unavailable":String.format(Locale.US,"%.1f °C",temp()),CYAN);
    int voltage=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1);
    metric(b,"Voltage",voltage<0?"Unavailable":voltage+" mV",WHITE);
    long current=battery.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
    metric(b,"Current (device dependent)",current==Long.MIN_VALUE?"Unavailable":String.format(Locale.US,"%.0f mA",current/1000.0),WHITE);
    block(b);
    note("Battery temperature is not the CPU temperature. Battery health percentage is not estimated without reliable cycle/capacity data.");
  }
  private List<android.content.pm.ResolveInfo> launchers(){
    Intent intent=new Intent(Intent.ACTION_MAIN);intent.addCategory(Intent.CATEGORY_LAUNCHER);
    return getPackageManager().queryIntentActivities(intent,0);
  }
  private void security(){
    section("SECURITY CONTROL");
    LinearLayout c=card("APP VISIBILITY AUDIT","Review launchable applications visible to Android");
    List<android.content.pm.ResolveInfo> apps=launchers();
    HashSet<String> names=new HashSet<>();for(android.content.pm.ResolveInfo r:apps)if(r.activityInfo!=null)names.add(r.activityInfo.packageName);
    metric(c,"Visible launchable apps",String.valueOf(names.size()),CYAN);
    metric(c,"Malware engine","Not included",GOLD);
    metric(c,"Antivirus status","Not certified",GOLD);
    block(c);
    LinearLayout a=card("SECURITY SHORTCUTS","Use official Android security controls");
    action(a,"App permissions","Review camera, microphone and location access",()->open(Settings.ACTION_APPLICATION_SETTINGS));
    action(a,"Security settings","Device security and protection controls",()->open(Settings.ACTION_SECURITY_SETTINGS));
    action(a,"Install unknown apps","Review risky sideloading access",()->open(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
    action(a,"Privacy settings","Review Android privacy controls",()->open(Settings.ACTION_PRIVACY_SETTINGS));
    block(a);
    note("This is a security audit dashboard, NOT an antivirus scanner. A genuine malware detector requires an updated detection engine and permitted package visibility.");
  }
  private void boost(){
    section("GAME PERFORMANCE");
    LinearLayout c=card("GAME SESSION READINESS","Measured device state — no fake FPS or RAM boost");
    metric(c,"RAM free",format(ram().availMem),CYAN);
    metric(c,"Battery temperature",temp()<0?"Unavailable":String.format(Locale.US,"%.1f °C",temp()),GREEN);
    metric(c,"Battery",level()<0?"Unavailable":level()+"%",WHITE);
    block(c);
    LinearLayout a=card("PREPARE TO PLAY","Android-supported optimizations");
    action(a,"Manage background apps","Inspect apps manually in Android settings",()->open(Settings.ACTION_APPLICATION_SETTINGS));
    action(a,"Do Not Disturb settings","Reduce interruptions during games",()->open(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
    action(a,"Battery optimization settings","Review background battery restrictions",()->open(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
    action(a,"Network settings","Check connectivity before multiplayer games",()->open(Settings.ACTION_WIRELESS_SETTINGS));
    block(a);
    note("Android prevents ordinary apps from force-closing all other apps or guaranteeing higher FPS. This release provides diagnostics and safe shortcuts.");
  }
  private void tools(){
    section("NETWORK LAB");
    LinearLayout n=card("CONNECTION DIAGNOSTICS","Live connectivity state");
    ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
    Network network=cm.getActiveNetwork();
    NetworkCapabilities cap=network==null?null:cm.getNetworkCapabilities(network);
    String type=cap==null?"Disconnected":cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Cellular":cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"Ethernet":"Other";
    metric(n,"Connection",type,CYAN);
    metric(n,"Internet capability",cap!=null&&cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)?"Advertised":"Unavailable",WHITE);
    metric(n,"Validated by Android",cap!=null&&cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"Yes":"No / unknown",GREEN);
    block(n);
    section("STORAGE CONTROL");
    LinearLayout s=card("STORAGE INSPECTOR","Safe cleanup via Android tools");
    metric(s,"Total internal storage",format(disk().getTotalBytes()),WHITE);
    metric(s,"Free internal storage",format(disk().getAvailableBytes()),GREEN);
    action(s,"Open storage management","Inspect and remove files with Android controls",()->open(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
    action(s,"Open file picker","Browse documents without automatic deletion",()->{
      try{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,4);}catch(Exception e){toast("No file picker available");}
    });
    block(s);
    section("SYSTEM UTILITIES");
    LinearLayout u=card("CONTROL PANEL","Device-native system tools");
    action(u,"Network settings","Wi-Fi, mobile data and connection settings",()->open(Settings.ACTION_WIRELESS_SETTINGS));
    action(u,"Battery settings","Battery usage and charging options",()->open(Settings.ACTION_BATTERY_SAVER_SETTINGS));
    action(u,"Refresh measurements","Read current values again",()->show(page));
    block(u);
    note("Deep app caches and protected system files cannot be erased silently on modern Android. Cleanup always requires user-approved access.");
  }
  private void note(String s){LinearLayout c=vertical();pad(c,14,14,14,14);c.setBackground(border(0xff18212B,0xff37475A,12));add(c,label("ⓘ  "+s,12,GOLD,false));block(c);}
  private void open(String action){try{startActivity(new Intent(action));}catch(Exception e){toast("This settings page is unavailable on your device");}}
  private void toast(String msg){Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();}
}
