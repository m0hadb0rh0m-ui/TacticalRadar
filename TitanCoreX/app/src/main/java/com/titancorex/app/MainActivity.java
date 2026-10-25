package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
 private static final int BG=0xff070A12,SURFACE=0xff111A2A,PANEL=0xff19283E,CYAN=0xff57E5EF,WHITE=0xffF4F8FF,MUTED=0xff9CAFC7,GREEN=0xff4BE0A3,AMBER=0xffFFCE7B,RED=0xffFF788B;
 private LinearLayout body,nav;private TextView topSubtitle;private int page=0;
 private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private GradientDrawable gradient(int a,int b,int r,int stroke){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(r));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
 private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
 private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 private void gap(LinearLayout p,int h){View v=new View(this);p.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
 private void add(LinearLayout p,View v){p.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 private void title(String s){TextView t=txt(s,19,WHITE,true);t.setPadding(dp(2),dp(14),0,dp(13));add(body,t);}
 private LinearLayout card(String label,String description){
  LinearLayout c=col();c.setPadding(dp(17),dp(17),dp(17),dp(17));c.setBackground(gradient(0xff19273B,0xff101A2A,22,0xff2B4059));
  add(c,txt(label,17,WHITE,true));if(description!=null){gap(c,5);add(c,txt(description,12,MUTED,false));}gap(c,10);
  add(body,c);gap(body,13);return c;
 }
 private void link(LinearLayout c,String name,String detail,Runnable action){
  LinearLayout line=row();line.setPadding(dp(12),dp(13),dp(12),dp(13));line.setBackground(gradient(0xff20344C,0xff16273C,14,0xff314B63));
  LinearLayout labels=col();add(labels,txt(name,14,WHITE,true));if(detail!=null){gap(labels,4);add(labels,txt(detail,11,MUTED,false));}
  line.addView(labels,new LinearLayout.LayoutParams(0,-2,1));line.addView(txt("‹",27,CYAN,true));line.setOnClickListener(v->action.run());add(c,line);gap(c,9);
 }
 private void guide(String key){Intent i=new Intent(this,FeatureGuideActivity.class);i.putExtra("feature",key);startActivity(i);}
 private void launch(Class<?> activity){startActivity(new Intent(this,activity));}
 private void settings(String action){try{startActivity(new Intent(action));}catch(Exception e){Toast.makeText(this,"هذا الإعداد غير متاح على الجهاز",Toast.LENGTH_SHORT).show();}}
 private Intent battery(){return registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));}
 private int batteryPct(){Intent i=battery();if(i==null)return -1;int n=i.getIntExtra(BatteryManager.EXTRA_LEVEL,-1),scale=i.getIntExtra(BatteryManager.EXTRA_SCALE,-1);return n<0||scale<=0?-1:n*100/scale;}
 private float temperature(){Intent i=battery();int n=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);return n<0?-1:n/10f;}
 private android.app.ActivityManager.MemoryInfo ram(){android.app.ActivityManager.MemoryInfo m=new android.app.ActivityManager.MemoryInfo();((android.app.ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(m);return m;}
 private StatFs disk(){return new StatFs(Environment.getDataDirectory().getAbsolutePath());}
 private String gb(long n){return String.format(Locale.US,"%.1f GB",n/1073741824.0);}
 private int score(){
  android.app.ActivityManager.MemoryInfo m=ram();StatFs s=disk();int result=100;
  if(m.totalMem>0){double free=(double)m.availMem/m.totalMem;if(free<.13)result-=16;else if(free<.25)result-=8;}
  if(s.getTotalBytes()>0){double free=(double)s.getAvailableBytes()/s.getTotalBytes();if(free<.1)result-=22;else if(free<.2)result-=10;}
  float t=temperature();if(t>=43)result-=25;else if(t>=39)result-=12;
  return Math.max(0,result);
 }
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);shell();show(0);}
 private void shell(){
  LinearLayout root=col();root.setBackgroundColor(BG);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);setContentView(root);
  LinearLayout header=row();header.setPadding(dp(18),dp(17),dp(18),dp(17));header.setBackground(gradient(0xff152B42,0xff0A1423,0,0));
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.titan_logo);header.addView(logo,new LinearLayout.LayoutParams(dp(53),dp(53)));
  LinearLayout heading=col();heading.setPadding(dp(12),0,0,0);add(heading,txt("TITAN CORE X",23,WHITE,true));gap(heading,4);
  topSubtitle=txt("SECURITY • PERFORMANCE • INTELLIGENCE",10,CYAN,true);add(heading,topSubtitle);header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
  TextView refresh=txt("⟳",27,CYAN,true);refresh.setPadding(dp(10),dp(5),dp(10),dp(5));header.addView(refresh);refresh.setOnClickListener(v->show(page));add(root,header);
  ScrollView sc=new ScrollView(this);sc.setFillViewport(false);sc.setVerticalScrollBarEnabled(false);
  body=col();body.setPadding(dp(16),dp(19),dp(16),dp(22));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
  nav=row();nav.setPadding(dp(7),dp(9),dp(7),dp(10));nav.setBackgroundColor(0xff0F1828);add(root,nav);
 }
 private void navigation(){
  nav.removeAllViews();String[] names={"الرئيسية","الحماية","الأداء","الخصوصية","المزيد"},icons={"⌂","⬡","⚡","◈","☷"};
  for(int i=0;i<5;i++){final int index=i;LinearLayout item=col();item.setGravity(Gravity.CENTER);item.setPadding(0,dp(8),0,dp(8));if(i==page)item.setBackground(gradient(0xff1D3D52,0xff15273C,13,0));
   TextView icon=txt(icons[i],23,i==page?CYAN:MUTED,true);icon.setGravity(Gravity.CENTER);add(item,icon);gap(item,4);
   TextView label=txt(names[i],10,i==page?WHITE:MUTED,i==page);label.setGravity(Gravity.CENTER);add(item,label);
   nav.addView(item,new LinearLayout.LayoutParams(0,-2,1));item.setOnClickListener(v->show(index));
  }
 }
 private void show(int index){page=index;body.removeAllViews();navigation();topSubtitle.setText(new String[]{"DEVICE COMMAND CENTER","SECURITY CONTROL","PERFORMANCE LAB","PRIVACY INTELLIGENCE","TITAN TOOLKIT"}[index]);
  if(index==0)home();else if(index==1)security();else if(index==2)performance();else if(index==3)privacy();else tools();
 }
 @Override protected void onResume(){super.onResume();if(body!=null)show(page);}
 private void stat(LinearLayout c,String name,String value,int color){
  LinearLayout l=row();l.setPadding(0,dp(9),0,dp(9));TextView n=txt(name,13,MUTED,false);l.addView(n,new LinearLayout.LayoutParams(0,-2,1));l.addView(txt(value,14,color,true));add(c,l);
  View rule=new View(this);rule.setBackgroundColor(0xff263B52);c.addView(rule,new LinearLayout.LayoutParams(-1,dp(1)));
 }
 private void hero(){
  LinearLayout h=col();h.setPadding(dp(22),dp(23),dp(22),dp(23));h.setBackground(gradient(0xff145366,0xff162943,26,0xff3D9FAE));
  add(h,txt("LIVE DEVICE STATUS",11,CYAN,true));gap(h,9);
  add(h,txt(score()+" / 100",46,WHITE,true));gap(h,4);add(h,txt("مؤشر تقديري للموارد، وليس نتيجة فحص فيروسات",12,0xffD0E2EF,false));gap(h,18);
  ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);p.setProgress(score());p.setProgressTintList(ColorStateList.valueOf(CYAN));add(h,p);gap(h,19);
  Button scan=new Button(this);scan.setText("⬡  بدء الفحص الأمني");scan.setAllCaps(false);scan.setTextColor(BG);scan.setBackground(gradient(0xff62E9EF,0xff9FF6F5,15,0));add(h,scan);scan.setOnClickListener(v->launch(SmartScanActivity.class));
  add(body,h);gap(body,17);
 }
 private void home(){
  hero();title("نظرة مباشرة على الهاتف");
  LinearLayout c=card("حالة الجهاز","بيانات من نظام Android دون قياسات وهمية");
  int battery=batteryPct();float t=temperature();
  stat(c,"مستوى البطارية",battery<0?"غير متاح":battery+"%",GREEN);
  stat(c,"حرارة البطارية",t<0?"غير متاحة":String.format(Locale.US,"%.1f°C",t),t>=42?AMBER:CYAN);
  stat(c,"الذاكرة المتاحة",gb(ram().availMem),WHITE);
  stat(c,"التخزين المتاح",gb(disk().getAvailableBytes()),WHITE);
  title("المراكز الذكية");
  LinearLayout guides=card("دليل الأنظمة","شرح تفصيلي لكل خيار قبل استخدامه");
  link(guides,"الحماية والأذونات","تفاصيل الفحص والخصوصية",()->guide("security"));
  link(guides,"البطارية والألعاب","شرح القياسات وحدود التحسين",()->guide("battery"));
  link(guides,"الروابط والشبكات","الاحتيال وDNS والاتصال",()->guide("scam"));
  link(guides,"التخزين والتقارير","الملفات المكررة والرسوم",()->guide("storage"));
  LinearLayout quick=card("TITAN INTELLIGENCE X","الأنظمة العشرة في مركز واحد");
  link(quick,"✦  افتح مركز الذكاء والتحليل","روابط، أمان، بطارية، شبكة، ألعاب وتقارير",()->launch(IntelligenceHubActivity.class));
  link(quick,"⬡  Security Smart Scan","فحص إعدادات الأمان والموارد",()->launch(SmartScanActivity.class));
  link(quick,"⚡  Power Station","مراقبة الشحن وحفظ الجلسات",()->launch(PowerStationActivity.class));
  link(quick,"◈  Time Capsule","تغيّرات الجهاز منذ آخر لقطة",()->launch(TimeCapsuleActivity.class));
  title("تقارير وتحليلات");
  LinearLayout r=card("المتابعة الذكية","معلومات قابلة للتحقق من الجهاز");
  link(r,"سجل الأداء والحرارة","رسم بياني وقياسات تاريخية",()->launch(TrendsActivity.class));
  link(r,"مشاركة تقرير الهاتف","تقرير القياسات الحالية",this::share);
 }
 private void security(){
  title("SECURITY COMMAND");
  LinearLayout s=card("مركز الحماية الشامل","تدقيق شفاف دون ادعاء وجود محرك فيروسات");
  link(s,"⬡  Smart Security Scan","قفل الجهاز، تصحيح USB، التحديثات والحرارة",()->launch(SmartScanActivity.class));
  link(s,"Security Health Center","مؤشرات إعدادات الحماية",()->launch(SecurityCenterActivity.class));
  link(s,"Scam Shield","تحليل محلي لمؤشرات روابط التصيّد",()->launch(IntelligenceHubActivity.class));
  link(s,"فحص أذونات التطبيقات","مراجعة الأذونات الحساسة",()->launch(PrivacyAuditActivity.class));
  LinearLayout system=card("حماية Android الرسمية","اختصارات إلى إعدادات النظام");
  link(system,"إعدادات الأمان","قفل الشاشة وحماية الجهاز",()->settings(Settings.ACTION_SECURITY_SETTINGS));
  link(system,"تحديثات النظام","مراجعة التحديثات المتاحة",()->settings(Settings.ACTION_DEVICE_INFO_SETTINGS));
  link(system,"تثبيت التطبيقات الخارجية","مراجعة مصادر التثبيت",()->settings(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
  note("لا يوجد في هذا الإصدار محرك مضاد فيروسات، ولا يمكن إعلان الهاتف خاليًا من البرمجيات الضارة.");
 }
 private void performance(){
  title("PERFORMANCE LAB");
  LinearLayout c=card("مراقبة الموارد","حالة فعلية لحرارة البطارية والذاكرة والتخزين");
  float t=temperature();stat(c,"حرارة البطارية",t<0?"غير متاحة":String.format(Locale.US,"%.1f°C",t),t>=42?AMBER:CYAN);
  stat(c,"الرام المتاحة",gb(ram().availMem),GREEN);stat(c,"مساحة التخزين",gb(disk().getAvailableBytes()),WHITE);
  LinearLayout tools=card("مختبرات TITAN","تحليل الأداء بدل وعود التسريع الوهمية");
  link(tools,"⚡  Power Station","التيار والجهد وجلسات الشحن",()->launch(PowerStationActivity.class));
  link(tools,"مراقب الحرارة والذاكرة","ضغط الموارد والتوصيات",()->launch(ResourceLabActivity.class));
  link(tools,"مخططات الأداء","تاريخ القياسات المحلية",()->launch(TrendsActivity.class));
  link(tools,"تحليل بيئة الألعاب","إعدادات اللعب ومؤشرات الموارد",()->launch(IntelligenceHubActivity.class));
  link(tools,"إعدادات البطارية","خيارات النظام الرسمية",()->settings(Settings.ACTION_BATTERY_SAVER_SETTINGS));
  note("لا يستطيع تطبيق عادي رفع تردد المعالج أو تبريد الجهاز فعليًا أو ضمان زيادة FPS.");
 }
 private void privacy(){
  title("PRIVACY INTELLIGENCE");
  LinearLayout c=card("الخصوصية تحت سيطرتك","فحص محلي، دون رفع قائمة تطبيقاتك إلى خادم");
  link(c,"تدقيق أذونات التطبيقات","فحص الأذونات التي يمكن للتطبيق رؤيتها",()->launch(PrivacyAuditActivity.class));
  link(c,"كبسولة الزمن الأمنية","مقارنة إعدادات الجهاز بلقطة سابقة",()->launch(TimeCapsuleActivity.class));
  link(c,"إعدادات الخصوصية","مراجعة أذونات الكاميرا والميكروفون والموقع",()->settings("android.settings.PRIVACY_SETTINGS"));
  LinearLayout files=card("STORAGE RECOVERY","إدارة الملفات بموافقتك");
  link(files,"فحص الملفات المكررة","مقارنة SHA-256 في مجلد تختاره",()->launch(StorageAuditActivity.class));
  link(files,"إدارة التخزين","مراجعة ملفات الجهاز",()->settings(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
  note("لا يتم حذف الملفات دون اختيار المستخدم، ولا تُقرأ الملفات الخاصة بالتطبيقات الأخرى دون إذن.");
 }
 private void tools(){
  title("TITAN TOOLKIT");
  LinearLayout c=card("جميع الأنظمة","انتقل إلى الوظيفة المطلوبة");
  link(c,"✦  Intelligence X","مركز الأنظمة العشرة",()->launch(IntelligenceHubActivity.class));
  link(c,"⬡  Security Scan","تدقيق أمان الجهاز",()->launch(SmartScanActivity.class));
  link(c,"⚡  Power Station","مختبر الشحن والبطارية",()->launch(PowerStationActivity.class));
  link(c,"◈  Time Capsule","تتبّع التغيرات الأمنية",()->launch(TimeCapsuleActivity.class));
  link(c,"فحص التخزين","اكتشاف الملفات المكررة",()->launch(StorageAuditActivity.class));
  link(c,"تقرير الأداء","سجل القياسات",()->launch(TrendsActivity.class));
  link(c,"اللغات","إعدادات لغة الواجهة",()->launch(LanguageActivity.class));
  link(c,"TITAN CORE X PRO","الميزات المخططة وخيارات الترقية",()->launch(PremiumActivity.class));
  LinearLayout n=card("NETWORK LAB","حالة الاتصال المتاحة عبر Android");
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);Network net=cm.getActiveNetwork();NetworkCapabilities caps=net==null?null:cm.getNetworkCapabilities(net);
  String type=caps==null?"غير متصل":caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"شبكة خلوية":"اتصال آخر";
  stat(n,"نوع الاتصال",type,CYAN);
  stat(n,"اتصال تحقق منه Android",caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"نعم":"غير مؤكد",WHITE);
  link(n,"تشخيص الشبكة التفصيلي","عرض DNS ومعلومات الاتصال",()->launch(IntelligenceHubActivity.class));
 }
 private void note(String s){LinearLayout c=col();c.setPadding(dp(14),dp(14),dp(14),dp(14));c.setBackground(gradient(0xff302B28,0xff201E26,16,0xff66533C));add(c,txt("ⓘ  "+s,12,AMBER,false));add(body,c);gap(body,15);}
 private void share(){
  String report="TITAN CORE X — Device Report\nDevice: "+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid: "+Build.VERSION.RELEASE+"\nEstimated resource score: "+score()+"/100\nBattery: "+batteryPct()+"%\nBattery temperature: "+temperature()+" C\nAvailable RAM: "+gb(ram().availMem)+"\nFree storage: "+gb(disk().getAvailableBytes())+"\nNo malware scan was performed.";
  Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,report);startActivity(Intent.createChooser(i,"مشاركة تقرير TITAN"));
 }
}
