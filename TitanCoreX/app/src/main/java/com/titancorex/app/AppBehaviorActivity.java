package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Permission-based application posture signals, not runtime behavior interception. */
public class AppBehaviorActivity extends Activity {
 private LinearLayout root;private TextView status;
 private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private TextView label(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(6),dp(9),dp(6),dp(9));return t;}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff080D18);ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(17),dp(20),dp(17),dp(20));root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(0xff080D18);scroll.addView(root);setContentView(scroll);
  root.addView(label("TITAN APP RISK INTELLIGENCE",23,0xff58E4EF));
  root.addView(label("تحليل استرشادي لأذونات التطبيقات التي يسمح Android برؤيتها. لا نراقب تنفيذ الكود داخل التطبيقات الأخرى.",14,0xffB1C3D4));
  Button run=new Button(this);run.setText("تحليل التطبيقات المرئية");root.addView(run);
  status=label("لم يبدأ التحليل بعد.",14,0xffF4F8FF);root.addView(status);
  run.setOnClickListener(v->{run.setEnabled(false);status.setText("جارٍ تحليل الأذونات...");new Thread(()->{String r;try{r=analyze();}catch(Exception e){r="تعذر التحليل: "+e.getMessage();}final String out=r;runOnUiThread(()->{run.setEnabled(true);status.setText(out);});}).start();});
 }
 private String analyze(){
  PackageManager pm=getPackageManager();Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(Intent.CATEGORY_LAUNCHER);
  List<ResolveInfo> visible=pm.queryIntentActivities(i,0);Set<String> packages=new TreeSet<>();
  for(ResolveInfo r:visible)if(r.activityInfo!=null)packages.add(r.activityInfo.packageName);
  StringBuilder result=new StringBuilder("عدد التطبيقات المرئية: "+packages.size()+"\n\n");int flagged=0;
  for(String name:packages){
   try{
    PackageInfo p=pm.getPackageInfo(name,PackageManager.GET_PERMISSIONS);Set<String> perms=new HashSet<>();
    if(p.requestedPermissions!=null)Collections.addAll(perms,p.requestedPermissions);
    boolean sms=perms.contains("android.permission.READ_SMS")||perms.contains("android.permission.RECEIVE_SMS");
    boolean overlay=perms.contains("android.permission.SYSTEM_ALERT_WINDOW");
    boolean contacts=perms.contains("android.permission.READ_CONTACTS");
    boolean install=perms.contains("android.permission.REQUEST_INSTALL_PACKAGES");
    boolean files=perms.contains("android.permission.MANAGE_EXTERNAL_STORAGE");
    ArrayList<String> flags=new ArrayList<>();
    if(sms&&overlay)flags.add("الرسائل + الظهور فوق التطبيقات");
    if(sms&&contacts)flags.add("الرسائل + جهات الاتصال");
    if(overlay&&install)flags.add("النوافذ العائمة + طلب تثبيت حزم");
    if(files&&install)flags.add("وصول واسع للملفات + تثبيت حزم");
    if(!flags.isEmpty()){flagged++;result.append("• ").append(name).append("\n");for(String f:flags)result.append("  - ").append(f).append("\n");}
   }catch(Exception ignored){}
  }
  if(flagged==0)result.append("لم تظهر مجموعات الأذونات المحددة في التطبيقات المرئية. هذا لا يعني خلو الجهاز من البرمجيات الخبيثة.");
  result.append("\n\nالتطبيقات التي تستوفي هذه المؤشرات ليست بالضرورة خبيثة. لا تتوفر مراقبة حقيقية لاستدعاءات النظام أو حركة التطبيقات الداخلية.");
  return result.toString();
 }
}
