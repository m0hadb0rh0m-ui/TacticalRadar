package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class TimeCapsuleActivity extends Activity {
 private final int BG=0xffF2F8FF,CYAN=0xff087BEA,WHITE=0xff142D50,MUTED=0xff4D6686,GOLD=0xffA96C00,GREEN=0xff087C57;
 private LinearLayout root,results;
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(8),dp(8),dp(8),dp(8));return t;}
 private GradientDrawable card(){GradientDrawable d=new GradientDrawable();d.setColor(0xffFFFFFF);d.setCornerRadius(dp(20));d.setStroke(dp(1),0xffC4DCF5);return d;}
 private void styleButton(Button button,int first,int second){
   button.setAllCaps(false);button.setTextSize(16);button.setTextColor(0xffF7FAFF);
   button.setTypeface(android.graphics.Typeface.create("sans-serif-medium",1));
   android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable(
    android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{first,second});
   bg.setCornerRadius(dp(18));bg.setStroke(dp(1),0xff417FA1);button.setBackground(bg);
   LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-1,dp(60));
   params.bottomMargin=dp(12);root.addView(button,params);
 }
 private void animateIn(View view,int delay){
   view.setAlpha(0f);view.setTranslationY(dp(22));
   view.animate().alpha(1f).translationY(0f).setStartDelay(delay).setDuration(560).start();
 }
 private LinkedHashMap<String,String> snapshot(){
   LinkedHashMap<String,String> m=new LinkedHashMap<>();
   KeyguardManager km=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
   m.put("قفل الشاشة الآمن",km!=null&&km.isDeviceSecure()?"مفعّل":"غير مفعّل");
   m.put("تصحيح USB",Settings.Global.getInt(getContentResolver(),Settings.Global.ADB_ENABLED,0)==1?"مفعّل":"غير مفعّل");
   m.put("خيارات المطور",Settings.Global.getInt(getContentResolver(),Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,0)==1?"مفعّلة":"غير مفعّلة");
   Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(Intent.CATEGORY_LAUNCHER);
   HashSet<String> apps=new HashSet<>();
   for(ResolveInfo r:getPackageManager().queryIntentActivities(i,0))if(r.activityInfo!=null)apps.add(r.activityInfo.packageName);
   m.put("عدد التطبيقات الظاهرة",String.valueOf(apps.size()));
   android.os.StatFs fs=new android.os.StatFs(android.os.Environment.getDataDirectory().getAbsolutePath());
   m.put("مساحة التخزين المتاحة (MB)",String.valueOf(fs.getAvailableBytes()/1048576L));
   m.put("إصدار Android",Build.VERSION.RELEASE);
   m.put("مستوى تحديث الأمان",Build.VERSION.SECURITY_PATCH);
   return m;
 }
 private String encode(LinkedHashMap<String,String> map){
   StringBuilder b=new StringBuilder();
   for(Map.Entry<String,String> e:map.entrySet())b.append(e.getKey()).append("=").append(e.getValue()).append("\n");
   return b.toString();
 }
 private LinkedHashMap<String,String> decode(String s){
   LinkedHashMap<String,String> m=new LinkedHashMap<>();
   for(String line:s.split("\n")){int k=line.indexOf('=');if(k>0)m.put(line.substring(0,k),line.substring(k+1));}
   return m;
 }
 @Override public void onCreate(Bundle state){
   super.onCreate(state);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
   ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
   root.setPadding(dp(18),dp(23),dp(18),dp(24));root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
   TitanCyberSceneView scene=new TitanCyberSceneView(this,CYAN);
   root.addView(scene,new LinearLayout.LayoutParams(-1,dp(205)));
   android.animation.ObjectAnimator pulse=android.animation.ObjectAnimator.ofFloat(scene,"alpha",0.68f,1f);
   pulse.setDuration(1700);pulse.setRepeatCount(android.animation.ValueAnimator.INFINITE);
   pulse.setRepeatMode(android.animation.ValueAnimator.REVERSE);pulse.start();
   TextView heading=text("◈  TITAN TIME CAPSULE",25,CYAN);
   heading.setTypeface(android.graphics.Typeface.create("sans-serif-black",1));root.addView(heading);
   animateIn(heading,100);
   root.addView(text("ذاكرة التغييرات الأمنية للجهاز",17,WHITE));
   root.addView(text("قارن حالة الهاتف اليوم بلقطة سابقة. كل البيانات تبقى محليًا، ولا نراقب الهاتف في الخلفية.",13,MUTED));
   Button save=new Button(this);save.setText("◈   حفظ بصمة الحالة الحالية");styleButton(save,0xff0089F7,0xff3B63F4);animateIn(save,180);
   save.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("حفظ لقطة مرجعية جديدة")
    .setMessage("سيتم استبدال اللقطة السابقة. هل تريد المتابعة؟")
    .setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{
      getSharedPreferences("titan_capsule",0).edit().putString("baseline",encode(snapshot())).putLong("date",System.currentTimeMillis()).apply();render();
    }).show());
   Button compare=new Button(this);compare.setText("◎   مقارنة الجهاز مع اللقطة المحفوظة");styleButton(compare,0xff00BDB4,0xff008CBE);animateIn(compare,260);compare.setOnClickListener(v->render());
   Button export=new Button(this);export.setText("↗   مشاركة تقرير المقارنة");styleButton(export,0xffF4A62A,0xffEE7C32);animateIn(export,340);
   export.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,report());startActivity(Intent.createChooser(i,"مشاركة تقرير التغييرات"));});
   results=new LinearLayout(this);results.setOrientation(1);root.addView(results);render();
 }
 private String report(){
   android.content.SharedPreferences p=getSharedPreferences("titan_capsule",0);
   String saved=p.getString("baseline","");
   if(saved.isEmpty())return "TITAN TIME CAPSULE: لا توجد لقطة مرجعية محفوظة";
   LinkedHashMap<String,String> before=decode(saved),now=snapshot();
   StringBuilder out=new StringBuilder("TITAN TIME CAPSULE — Device changes\n");
   for(String key:now.keySet()){
     String old=before.get(key),fresh=now.get(key);
     out.append(key).append(": ").append(old==null?"غير متاح سابقًا":old).append(" → ").append(fresh);
     if(!Objects.equals(old,fresh))out.append(" [تغير]");
     out.append("\n");
   }
   out.append("ملاحظة: التغيّر لا يثبت اختراقًا، والفحص محدود بصلاحيات Android.");
   return out.toString();
 }
 private void render(){
   if(results==null)return;results.removeAllViews();
   android.content.SharedPreferences p=getSharedPreferences("titan_capsule",0);
   String saved=p.getString("baseline","");
   if(saved.isEmpty()){results.addView(text("لا توجد لقطة محفوظة. اضغط «حفظ بصمة الحالة الحالية» لبدء المقارنة مستقبلًا.",14,GOLD));return;}
   String date=new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date(p.getLong("date",0)));
   results.addView(text("تاريخ اللقطة المرجعية: "+date,14,CYAN));
   LinkedHashMap<String,String> old=decode(saved),now=snapshot();
   int changed=0;
   for(Map.Entry<String,String> e:now.entrySet()){
     String before=old.get(e.getKey()),current=e.getValue();
     boolean different=!Objects.equals(before,current);
     if(different)changed++;
     LinearLayout item=new LinearLayout(this);item.setOrientation(1);item.setPadding(dp(10),dp(9),dp(10),dp(9));item.setBackground(card());
     item.addView(text((different?"◆  تغيّر: ":"✓  ثابت: ")+e.getKey(),15,different?GOLD:GREEN));
     item.addView(text("سابقًا: "+(before==null?"غير معروف":before)+"\nحاليًا: "+current,12,MUTED));
     results.addView(item);animateIn(item,Math.min(600,changed*60));View gap=new View(this);results.addView(gap,new LinearLayout.LayoutParams(1,dp(9)));
   }
   results.addView(text("عدد المؤشرات التي تغيرت: "+changed,17,changed>0?GOLD:GREEN));
   results.addView(text("قد تتغير مساحة التخزين وعدد التطبيقات طبيعيًا. عدد التطبيقات يشمل التطبيقات الظاهرة فقط. لا يُعد هذا فحصًا للبرمجيات الخبيثة.",12,MUTED));
 }
}
