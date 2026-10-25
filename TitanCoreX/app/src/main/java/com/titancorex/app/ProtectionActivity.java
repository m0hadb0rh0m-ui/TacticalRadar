package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class ProtectionActivity extends Activity {
 private LinearLayout root;private TextView status;
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(0xff173557);t.setPadding(dp(8),dp(12),dp(8),dp(12));return t;}
 private void button(String title,Runnable run){Button b=new Button(this);b.setAllCaps(false);b.setText(title);root.addView(b);b.setOnClickListener(v->run.run());}
 private void refresh(){
  android.content.SharedPreferences p=getSharedPreferences("titan_monitor",MODE_PRIVATE);
  long last=p.getLong("last_run",0);
  status.setText("مراقبة التطبيقات المرئية: "+(SecurityMonitorJob.enabled(this)?"مفعلة":"متوقفة")+
   "\nآخر فحص: "+(last==0?"لم يُنفذ بعد":new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date(last)))+
   "\nعدد التطبيقات المرئية: "+p.getInt("count",0)+
   "\nتطبيقات ظهرت حديثًا: "+p.getString("new_apps","لا توجد بيانات")+
   "\nتنبيهات تثبيت التطبيقات: "+(p.getBoolean("instant_alerts",false)?"مفعلة":"متوقفة")+
   "\nآخر تطبيق تم تثبيته: "+p.getString("last_installed","لا يوجد سجل")+
   "\nمراجعة أذونات آخر تثبيت: "+(p.getString("last_install_attention","").isEmpty()?"لا يظهر تنبيه ضمن القواعد المحدودة":p.getString("last_install_attention",""))+
   "\nتحديثات قاعدة التهديدات: "+(ThreatFeedUpdater.configured()?(ThreatFeedUpdater.scheduled(this)?"مجدولة":"غير مجدولة"):"بانتظار مصدر موثوق ومفتاح توقيع")+
   "\nقاعدة البصمات: "+ThreatEngine.feedStatus(this)+
   "\n\nهذا رصد لتغيّر قائمة التطبيقات التي يسمح Android برؤيتها، وليس كشفًا لحظيًا للفيروسات. قد يؤخر النظام الفحص لتوفير الطاقة.");
 }
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(0xff080D18);
  ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(20),dp(20),dp(20));root.setBackgroundColor(0xffFFF4F6);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);scroll.addView(root);setContentView(scroll);
  root.addView(label("TITAN PROTECTION CENTER",23));
  root.addView(label("مراقبة دورية للتغييرات — بفواصل لا تقل عن 15 دقيقة بحسب جدولة Android.",14));
  status=label("",15);root.addView(status);
  button("تفعيل تحديث قاعدة التهديدات تلقائيًا",()->{try{ThreatFeedUpdater.schedule(this);Toast.makeText(this,"تمت جدولة التحديثات",Toast.LENGTH_SHORT).show();}catch(Exception e){new AlertDialog.Builder(this).setTitle("التحديث غير جاهز").setMessage("يلزم إعداد رابط مصدر تهديدات موثوق ومفتاح تحقق رقمي قبل تفعيل التنزيل.").setPositiveButton("حسنًا",null).show();}refresh();});
  button("إيقاف تحديثات القاعدة",()->{ThreatFeedUpdater.cancel(this);refresh();});
  button("تفعيل تنبيهات تثبيت التطبيقات",()->{getSharedPreferences("titan_monitor",MODE_PRIVATE).edit().putBoolean("instant_alerts",true).apply();refresh();});
  button("إيقاف تنبيهات التثبيت",()->{getSharedPreferences("titan_monitor",MODE_PRIVATE).edit().putBoolean("instant_alerts",false).apply();refresh();});
  button("تفعيل مراقبة التطبيقات الدورية",()->{SecurityMonitorJob.schedule(this);refresh();});
  button("إيقاف المراقبة",()->{SecurityMonitorJob.stop(this);refresh();});
  button("تحليل مؤشرات مخاطر التطبيقات",()->startActivity(new Intent(this,AppBehaviorActivity.class)));
  button("فحص ملف APK",()->startActivity(new Intent(this,ApkInspectorActivity.class)));
  button("فحص إعدادات الحماية",()->startActivity(new Intent(this,SmartScanActivity.class)));
  button("تحديث الحالة",this::refresh);
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED){button("السماح بإشعارات التغييرات",()->requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},113));}
  refresh();
 }
}
