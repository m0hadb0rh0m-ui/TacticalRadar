package com.titancorex.app;

import android.app.*;
import android.app.KeyguardManager;
import android.content.*;
import android.graphics.drawable.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class SmartScanActivity extends Activity {
 private static final int BG=0xff080C16,CYAN=0xff42E8F5,WHITE=0xffF4F8FF,MUTED=0xffA7BCD2,GOLD=0xffFFD27D,GREEN=0xff54E5AD;
 private LinearLayout root,results;private TextView headline;
 private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private TextView label(String s,int z,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(color);t.setPadding(dp(10),dp(8),dp(10),dp(8));return t;}
 private GradientDrawable bg(){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff192F49,0xff101C30});d.setCornerRadius(dp(18));d.setStroke(dp(1),0xff35566D);return d;}
 private void addResult(String name,boolean ok,String detail,Runnable action){
  LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(12),dp(10),dp(12),dp(10));c.setBackground(bg());
  c.addView(label((ok?"✓  ":"!  ")+name,17,ok?GREEN:GOLD));c.addView(label(detail,13,MUTED));
  if(action!=null){Button b=new Button(this);b.setText("مراجعة الإعداد");c.addView(b);b.setOnClickListener(v->action.run());}
  results.addView(c);View gap=new View(this);results.addView(gap,new LinearLayout.LayoutParams(1,dp(12)));
 }
 private void settings(String action){try{startActivity(new Intent(action));}catch(Exception e){Toast.makeText(this,"الإعداد غير متاح على هذا الجهاز",Toast.LENGTH_SHORT).show();}}
 @Override public void onCreate(Bundle b){
  super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(17),dp(22),dp(17),dp(24));root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
  root.addView(label("⬡  TITAN SMART SECURITY SCAN",23,CYAN));
  root.addView(label("فحص إعدادات الأمان والموارد الأساسية",14,WHITE));
  root.addView(label("الفحص لا يكشف الفيروسات أو الاختراقات. يعرض مؤشرات محلية ويقترح مراجعة إعدادات Android.",12,MUTED));
  headline=label("",18,WHITE);root.addView(headline);
  Button scan=new Button(this);scan.setText("إعادة الفحص الآن");root.addView(scan);scan.setOnClickListener(v->scan());
  results=new LinearLayout(this);results.setOrientation(1);root.addView(results);scan();
 }
 @Override protected void onResume(){super.onResume();if(results!=null)scan();}
 private void scan(){
  results.removeAllViews();int issues=0;
  KeyguardManager km=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
  boolean locked=km!=null&&km.isDeviceSecure();if(!locked)issues++;
  addResult("قفل الشاشة",locked,locked?"تم تفعيل قفل آمن":"يُنصح بتفعيل PIN أو كلمة مرور أو بصمة",locked?null:()->settings(Settings.ACTION_SECURITY_SETTINGS));
  boolean adb=Settings.Global.getInt(getContentResolver(),Settings.Global.ADB_ENABLED,0)==1;if(adb)issues++;
  addResult("تصحيح USB",!adb,adb?"تصحيح USB مفعّل؛ راجع حاجتك إليه":"تصحيح USB غير مفعّل",adb?()->settings(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS):null);
  String patch=Build.VERSION.SECURITY_PATCH;boolean patchKnown=patch!=null&&!patch.isEmpty();
  addResult("تحديث الأمان",patchKnown,"تاريخ آخر تصحيح معلن: "+(patchKnown?patch:"غير متاح")+" — لا نتحقق من وجود تحديث أحدث",()->settings(Settings.ACTION_DEVICE_INFO_SETTINGS));
  android.os.StatFs fs=new android.os.StatFs(android.os.Environment.getDataDirectory().getAbsolutePath());
  long total=fs.getTotalBytes(),free=fs.getAvailableBytes();boolean space=total>0&&free*10>=total;if(!space)issues++;
  addResult("مساحة التخزين",space,"المساحة المتاحة: "+(free/1048576)+" MB؛ أقل من 10% قد يؤثر على الاستخدام",space?null:()->settings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
  Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));int raw=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);
  boolean tempOk=raw<0||raw<420;if(!tempOk)issues++;
  addResult("حرارة البطارية",tempOk,raw<0?"غير متاحة":String.format(Locale.US,"%.1f °C",raw/10f)+(tempOk?"":" — الحرارة مرتفعة؛ خفف الاستخدام الثقيل"),null);
  headline.setText(issues==0?"لا توجد تنبيهات ضمن الفحوصات المحدودة":issues+" مؤشرات تستحق المراجعة");
  headline.setTextColor(issues==0?GREEN:GOLD);
  addResult("فحص البرمجيات الضارة",false,"غير متوفر: يلزم محرك كشف وقواعد تهديدات محدثة. لا يمكن استنتاج خلو الهاتف من الفيروسات.",null);
 }
}
