package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** V3 security scan UI. Audits accessible Android posture; not a malware scanner. */
public final class V3SecurityScanActivity extends Activity {
 private static final int BG=0xff0B0F19,CARD=0xff151D2B,INK=0xffE7F7FF,MUTED=0xff8DA5B8,
  GREEN=0xff10B981,CYAN=0xff06B6D4,AMBER=0xffF59E0B,RED=0xffFB7185;
 private LinearLayout root,findings;
 private TextView status,summary;
 private TitanV3OrbView orb;
 private Button scan;
 private volatile boolean running=false;
 private volatile boolean closed=false;
 private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private GradientDrawable panel(int a,int b,int stroke){
  GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});
  g.setCornerRadius(dp(20));if(stroke!=0)g.setStroke(dp(1),stroke);return g;
 }
 private TextView text(String s,int size,int color,boolean bold){
  TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
  if(bold)t.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
  t.setPadding(dp(4),dp(5),dp(4),dp(5));return t;
 }
 private void action(String name,int accent,Runnable r){
  Button button=new Button(this);button.setText(name);button.setAllCaps(false);
  button.setTextColor(INK);button.setTextSize(15);
  button.setBackground(panel(accent,accent==GREEN?0xff0F766E:0xff164E63,0));
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(55));p.topMargin=dp(9);
  root.addView(button,p);button.setOnClickListener(v->r.run());
 }
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
  root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  root.setPadding(dp(18),dp(26),dp(18),dp(32));root.setBackgroundColor(BG);
  scroll.addView(root);setContentView(scroll);
  root.addView(text("⬡  TITAN SECURITY RADAR",23,INK,true));
  root.addView(text("V3  /  LOCAL POSTURE INSPECTION",12,CYAN,true));
  root.addView(text("فحص واقعي لإعدادات Android وأذونات التطبيقات الظاهرة. لا يدّعي اكتشاف كل الفيروسات ولا يقرأ ملفات التطبيقات المحمية.",13,MUTED,false));
  orb=new TitanV3OrbView(this);root.addView(orb,new LinearLayout.LayoutParams(-1,dp(285)));
  orb.setOnClickListener(v->startScan());
  summary=text("اضغط على الرادار لبدء التدقيق",19,INK,true);summary.setGravity(Gravity.CENTER);root.addView(summary);
  status=text("يظهر تقدم حقيقي خلال مراجعة الفحوصات.",13,MUTED,false);status.setGravity(Gravity.CENTER);root.addView(status);
  scan=new Button(this);scan.setAllCaps(false);scan.setText("ابدأ فحص إعدادات الأمان");
  scan.setTextSize(16);scan.setTextColor(BG);scan.setBackground(panel(GREEN,0xff06B6D4,0));
  LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(58));sp.topMargin=dp(16);
  root.addView(scan,sp);scan.setOnClickListener(v->startScan());
  TextView heading=text("نتائج التدقيق",19,INK,true);heading.setPadding(0,dp(18),0,dp(10));root.addView(heading);
  findings=new LinearLayout(this);findings.setOrientation(1);root.addView(findings);
  action("تحليل APK تختاره أنت",CYAN,()->startActivity(new Intent(this,ApkInspectorActivity.class)));
  action("مراجعة خريطة أذونات التطبيقات",CYAN,()->startActivity(new Intent(this,PrivacyAuditActivity.class)));
  root.addView(text("دلالة النتائج: التنبيه يعني حاجة إلى مراجعة الإعداد، وليس إثبات إصابة. الفحص لا يزيل التطبيقات أو يغيّر الأذونات.",12,MUTED,false));
 }
 private void addFinding(TitanSecurityPosture.Finding item){
  LinearLayout card=new LinearLayout(this);card.setOrientation(1);
  card.setPadding(dp(15),dp(13),dp(15),dp(13));
  card.setBackground(panel(CARD,0xff1A2739,item.attention?0xff76511E:0xff245B59));
  card.addView(text((item.attention?"◆  ":"✓  ")+item.label,16,item.attention?AMBER:GREEN,true));
  card.addView(text(item.detail,13,MUTED,false));
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(10);
  findings.addView(card,p);
 }
 private void startScan(){
  if(running)return;running=true;scan.setEnabled(false);findings.removeAllViews();
  orb.setAuditProgress(0,6,false);
  status.setText("بدء قراءة إعدادات النظام...");summary.setText("جاري الفحص");
  new Thread(()->{
   try{
    TitanSecurityPosture.Report report=TitanSecurityPosture.audit(this,(done,total,label)->{
     runOnUiThread(()->{if(closed||isFinishing()||isDestroyed())return;
      orb.setAuditProgress(done,total,false);
      status.setText("فحص "+done+" من "+total+": "+label);
     });
    });
    runOnUiThread(()->{if(closed||isFinishing()||isDestroyed())return;
     int needs=report.reviewed-report.passed;
     for(TitanSecurityPosture.Finding item:report.findings)addFinding(item);
     orb.setAuditProgress(report.reviewed,report.reviewed,needs>0);
     summary.setText(needs==0?"لا توجد تنبيهات في هذه الفحوصات":"عدد التنبيهات التي تحتاج مراجعة: "+needs);
     summary.setTextColor(needs==0?GREEN:AMBER);
     status.setText("تم فحص "+report.reviewed+" إعدادات، ورؤية "+report.visibleApps+" تطبيقًا ضمن حدود Android.");
     running=false;scan.setEnabled(true);scan.setText("إعادة فحص إعدادات الأمان");
    });
   }catch(Exception e){
    runOnUiThread(()->{if(closed||isFinishing()||isDestroyed())return;
     status.setText("تعذر إكمال الفحص: "+e.getClass().getSimpleName());
     running=false;scan.setEnabled(true);
    });
   }
  },"TitanPostureScan").start();
 }
 @Override protected void onDestroy(){closed=true;super.onDestroy();}
}
