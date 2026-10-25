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
 private static final int BG=0xff0B0F19,SURFACE=0xff151D2B,PANEL=0xff1C293A,CYAN=0xff06B6D4,WHITE=0xffEDF9FF,MUTED=0xff96AFC1,GREEN=0xff10B981,AMBER=0xffF59E0B,RED=0xffFB7185;
 private LinearLayout body,nav;private TextView topSubtitle,liveBattery,liveTemp,liveRam,liveDisk;private TitanVitalsView vitals;private int page=0;
 private final Handler liveHandler=new Handler(Looper.getMainLooper());
 private final Runnable liveRefresh=new Runnable(){@Override public void run(){refreshVitals();liveHandler.postDelayed(this,5000);}};
 private int dp(float x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private GradientDrawable gradient(int a,int b,int r,int stroke){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(r));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
 private TextView txt(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
 private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 private void gap(LinearLayout p,int h){View v=new View(this);p.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
 private void add(LinearLayout p,View v){p.addView(v,new LinearLayout.LayoutParams(-1,-2));}
 private void title(String s){
  LinearLayout section=row();section.setPadding(dp(2),dp(18),dp(2),dp(14));
  View marker=new View(this);marker.setBackground(gradient(CYAN,0xff3677FF,3,0));
  section.addView(marker,new LinearLayout.LayoutParams(dp(4),dp(26)));
  TextView t=txt(s,21,WHITE,true);t.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
  t.setPadding(dp(12),0,0,0);section.addView(t);add(body,section);
 }
 private LinearLayout card(String label,String description){
  LinearLayout c=col();c.setPadding(dp(17),dp(17),dp(17),dp(17));c.setBackground(gradient(0xff172333,0xff111C29,24,0xff294357));
  TextView heading=txt(label,19,WHITE,true);heading.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));add(c,heading);if(description!=null){gap(c,5);add(c,txt(description,12,MUTED,false));}gap(c,10);
  add(body,c);gap(body,13);return c;
 }
 private int featureColor(String name){
  if(name.contains("شبك")||name.contains("NETWORK")||name.contains("Wi-Fi")||name.contains("DNS"))return 0xff00D9FF;
  if(name.contains("حما")||name.contains("Security")||name.contains("Scan")||name.contains("أمان"))return 0xffff506e;
  if(name.contains("Power")||name.contains("أداء")||name.contains("شحن")||name.contains("بطار"))return 0xffffc83d;
  if(name.contains("خصوص")||name.contains("أذون"))return 0xffba75ff;
  if(name.contains("APK")||name.contains("تطبيق"))return 0xff35ee9c;
  return 0xff5ebaff;
 }
 private String featureIcon(String name){
  if(name.contains("شبك")||name.contains("NETWORK")||name.contains("Wi-Fi")||name.contains("DNS"))return "◎";
  if(name.contains("حما")||name.contains("Security")||name.contains("Scan")||name.contains("أمان"))return "⬡";
  if(name.contains("Power")||name.contains("أداء")||name.contains("شحن")||name.contains("بطار"))return "ϟ";
  if(name.contains("خصوص")||name.contains("أذون"))return "♧";
  if(name.contains("APK")||name.contains("تطبيق"))return "▣";
  return "✦";
 }
 private void link(LinearLayout c,String name,String detail,Runnable action){
  LinearLayout line=row();line.setPadding(dp(12),dp(13),dp(12),dp(13));line.setBackground(gradient(0xff1C2B3D,0xff172337,17,0xff315068));
  int accent=featureColor(name);
  TextView art=txt(featureIcon(name),26,accent,true);art.setGravity(Gravity.CENTER);
  art.setBackground(gradient(0xff142536,0xff1B3543,13,accent));
  LinearLayout.LayoutParams artParams=new LinearLayout.LayoutParams(dp(50),dp(50));artParams.setMargins(0,0,dp(12),0);line.addView(art,artParams);
  LinearLayout labels=col();add(labels,txt(name,15,WHITE,true));if(detail!=null){gap(labels,4);add(labels,txt(detail,11,MUTED,false));}
  line.addView(labels,new LinearLayout.LayoutParams(0,-2,1));line.addView(txt("‹",27,accent,true));line.setContentDescription(name+" - "+(detail==null?"":detail));line.setOnClickListener(v->action.run());add(c,line);gap(c,9);
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
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);getWindow().getDecorView().setSystemUiVisibility(0);shell();show(0);}
 private void shell(){
  LinearLayout root=col();root.setBackgroundColor(BG);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);setContentView(root);
  LinearLayout header=row();header.setPadding(dp(18),dp(17),dp(18),dp(17));header.setBackground(gradient(0xff162132,0xff111A29,0,0));
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.titan_logo);header.addView(logo,new LinearLayout.LayoutParams(dp(53),dp(53)));
  LinearLayout heading=col();heading.setPadding(dp(12),0,0,0);TextView brand=txt("TITAN CORE X",25,WHITE,true);brand.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));add(heading,brand);gap(heading,4);
  topSubtitle=txt("SECURITY • PERFORMANCE • INTELLIGENCE",10,CYAN,true);add(heading,topSubtitle);header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
  TextView refresh=txt("⟳",27,CYAN,true);refresh.setPadding(dp(10),dp(5),dp(10),dp(5));header.addView(refresh);refresh.setOnClickListener(v->show(page));add(root,header);
  ScrollView sc=new ScrollView(this);sc.setFillViewport(false);sc.setVerticalScrollBarEnabled(false);
  body=col();body.setPadding(dp(16),dp(19),dp(16),dp(22));sc.addView(body);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
  nav=row();nav.setPadding(dp(7),dp(9),dp(7),dp(10));nav.setBackground(gradient(0xff121E2F,0xff0E1727,0,0));add(root,nav);
 }
 private void navigation(){
  nav.removeAllViews();String[] names={"الرئيسية","الحماية","الشبكة","الأداء","الخصوصية"},icons={"⌂","⬡","◎","⚡","◈"};
  for(int i=0;i<5;i++){final int index=i;LinearLayout item=col();item.setGravity(Gravity.CENTER);item.setPadding(0,dp(8),0,dp(8));if(i==page)item.setBackground(gradient(0xff164B4B,0xff123C51,13,0xff237C7C));
   TextView icon=txt(icons[i],25,i==page?new int[]{CYAN,RED,CYAN,AMBER,0xff8051D1}[i]:MUTED,true);icon.setGravity(Gravity.CENTER);add(item,icon);gap(item,4);
   TextView label=txt(names[i],11,i==page?WHITE:MUTED,i==page);label.setGravity(Gravity.CENTER);add(item,label);
   nav.addView(item,new LinearLayout.LayoutParams(0,-2,1));item.setContentDescription("فتح قسم "+names[i]);item.setOnClickListener(v->show(index));
  }
 }
 private void show(int index){page=index;vitals=null;liveBattery=null;liveTemp=null;liveRam=null;liveDisk=null;body.removeAllViews();navigation();topSubtitle.setText(new String[]{"DEVICE COMMAND CENTER","SECURITY CONTROL","NETWORK LAB","PERFORMANCE LAB","PRIVACY INTELLIGENCE"}[index]);
  if(index!=0)sectionHero(index);
  if(index==0){home();refreshVitals();}else if(index==1)security();else if(index==2)network();else if(index==3)performance();else privacy();
 }
 @Override protected void onResume(){super.onResume();if(body!=null)show(page);liveHandler.removeCallbacks(liveRefresh);liveHandler.post(liveRefresh);}
 @Override protected void onPause(){liveHandler.removeCallbacks(liveRefresh);super.onPause();}
 private void refreshVitals(){if(page!=0||vitals==null)return;int battery=batteryPct();float temp=temperature();
  ActivityManager.MemoryInfo mem=ram();StatFs storage=disk();
  if(liveBattery!=null)liveBattery.setText(battery<0?"غير متاح":battery+"%");
  if(liveTemp!=null)liveTemp.setText(temp<0?"غير متاحة":String.format(Locale.US,"%.1f °C",temp));
  if(liveRam!=null)liveRam.setText(gb(mem.availMem));
  if(liveDisk!=null)liveDisk.setText(gb(storage.getAvailableBytes()));
  vitals.addSample(battery,temp,mem.availMem,mem.totalMem);
 }
 private void sectionHero(int index){
  String[] titles={"","TITAN SECURITY SHIELD","TITAN NETWORK LAB","TITAN POWER ENGINE","TITAN PRIVACY VAULT"};
  String[] descriptions={"","فحوصات إعدادات الأمان في مركز واحد","اختبارات الاتصال وDNS وWi-Fi وسرعة الإنترنت","مراقبة الموارد والحرارة والبطارية","أذونات التطبيقات وبياناتك تحت سيطرتك"};
  String[] symbols={"","⬡","◎","ϟ","◈"};
  int[] accents={CYAN,RED,CYAN,AMBER,0xff8051D1};
  LinearLayout hero=col();hero.setPadding(dp(22),dp(24),dp(22),dp(23));
  hero.setBackground(gradient(0xff1C2B3D,0xff101B29,26,accents[index]));
  TitanCyberSceneView scene=new TitanCyberSceneView(this,accents[index]);hero.addView(scene,new LinearLayout.LayoutParams(-1,dp(168)));
  gap(hero,7);TextView heading=txt(titles[index],21,WHITE,true);
  heading.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));heading.setGravity(Gravity.CENTER);add(hero,heading);
  gap(hero,10);TextView description=txt(descriptions[index],13,MUTED,false);description.setGravity(Gravity.CENTER);add(hero,description);
  add(body,hero);gap(body,8);
 }
 private void stat(LinearLayout c,String name,String value,int color){
  LinearLayout l=row();l.setPadding(0,dp(9),0,dp(9));TextView n=txt(name,13,MUTED,false);l.addView(n,new LinearLayout.LayoutParams(0,-2,1));TextView val=txt(value,14,color,true);l.addView(val);if(page==0){if(name.equals("مستوى البطارية"))liveBattery=val;else if(name.equals("حرارة البطارية"))liveTemp=val;else if(name.equals("الذاكرة المتاحة"))liveRam=val;else if(name.equals("التخزين المتاح"))liveDisk=val;}add(c,l);
  View rule=new View(this);rule.setBackgroundColor(0xff2B3F50);c.addView(rule,new LinearLayout.LayoutParams(-1,dp(1)));
 }
 private void hero(){
  LinearLayout h=col();h.setPadding(dp(20),dp(18),dp(20),dp(22));
  h.setBackground(gradient(0xff172C3A,0xff101B2B,28,0xff255D64));
  add(h,txt("TITAN CORE X   •   CYBER DEFENSE V3",12,CYAN,true));
  gap(h,5);add(h,txt("مركز التحكم الأمني",24,WHITE,true));
  gap(h,8);
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
  Network n=cm==null?null:cm.getActiveNetwork();
  NetworkCapabilities caps=n==null?null:cm.getNetworkCapabilities(n);
  boolean connected=caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
  LinearLayout ribbon=row();ribbon.setPadding(dp(10),dp(10),dp(10),dp(10));
  ribbon.setBackground(gradient(0xff203348,0xff16283B,15,0xff315363));
  TextView net=txt(connected?"● الإنترنت متصل":"● الاتصال غير مؤكد",13,connected?GREEN:AMBER,true);
  ribbon.addView(net,new LinearLayout.LayoutParams(0,-2,1));
  TextView protection=txt("DNS: إعدادات قابلة للتحكم",11,CYAN,true);ribbon.addView(protection);
  add(h,ribbon);
  TitanV3OrbView orb=new TitanV3OrbView(this);
  h.addView(orb,new LinearLayout.LayoutParams(-1,dp(265)));
  orb.setOnClickListener(v->launch(V3SecurityScanActivity.class));
  TextView state=txt("اضغط على الرادار لبدء تدقيق الحماية",15,WHITE,true);
  state.setGravity(Gravity.CENTER);add(h,state);gap(h,3);
  TextView accuracy=txt("المؤشر يمثل تقدم الفحص الفعلي، وليس نسبة حماية مزعومة",12,MUTED,false);
  accuracy.setGravity(Gravity.CENTER);add(h,accuracy);gap(h,13);
  Button scan=new Button(this);scan.setText("⬡  ابدأ الفحص الأمني V3");
  scan.setTextSize(17);scan.setAllCaps(false);scan.setTextColor(BG);
  scan.setBackground(gradient(GREEN,0xff06B6D4,17,0));add(h,scan);
  scan.setOnClickListener(v->launch(V3SecurityScanActivity.class));
  add(body,h);gap(body,16);
 }
 private LinearLayout tile(String symbol,String name,String detail,int tint,Runnable action){
  LinearLayout t=col();t.setPadding(dp(14),dp(17),dp(14),dp(15));
  t.setBackground(gradient(0xff192A3B,0xff111E2C,22,tint));
  TextView art=txt(symbol,27,tint,true);add(t,art);gap(t,9);
  add(t,txt(name,16,WHITE,true));gap(t,5);add(t,txt(detail,12,MUTED,false));
  t.setMinimumHeight(dp(145));t.setContentDescription(name+"؛ "+detail);
  t.setOnClickListener(v->action.run());return t;
 }
 private void bento(){
  title("المهام الأساسية");
  LinearLayout first=row(),second=row();
  LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,-2,1);
  left.setMargins(dp(4),0,dp(4),0);
  first.addView(tile("⬡","فحص الأمان","تدقيق إعدادات Android",GREEN,()->launch(V3SecurityScanActivity.class)),left);
  LinearLayout.LayoutParams right=new LinearLayout.LayoutParams(0,-2,1);
  right.setMargins(dp(4),0,dp(4),0);
  first.addView(tile("◎","درع الشبكة","DNS وWi-Fi والاتصال",CYAN,()->show(2)),right);
  add(body,first);gap(body,9);
  LinearLayout.LayoutParams third=new LinearLayout.LayoutParams(0,-2,1);
  third.setMargins(dp(4),0,dp(4),0);
  second.addView(tile("◈","الخصوصية","مراجعة أذونات التطبيقات",0xffC084FC,()->launch(PrivacyAuditActivity.class)),third);
  LinearLayout.LayoutParams fourth=new LinearLayout.LayoutParams(0,-2,1);
  fourth.setMargins(dp(4),0,dp(4),0);
  second.addView(tile("ϟ","أداء الهاتف","بطارية وحرارة وموارد",AMBER,()->show(3)),fourth);
  add(body,second);gap(body,12);
 }
 private void home(){
  hero();bento();
  title("القياسات المباشرة");
  LinearLayout state=card("نظرة على الجهاز","تتجدد القراءات كل 5 ثوانٍ أثناء عرض التطبيق");
  int charge=batteryPct();float t=temperature();ActivityManager.MemoryInfo mem=ram();
  stat(state,"مستوى البطارية",charge<0?"غير متاح":charge+"%",GREEN);
  stat(state,"حرارة البطارية",t<0?"غير متاحة":String.format(Locale.US,"%.1f °C",t),t>=42?RED:CYAN);
  stat(state,"الذاكرة المتاحة",gb(mem.availMem),WHITE);
  stat(state,"التخزين المتاح",gb(disk().getAvailableBytes()),WHITE);
  vitals=new TitanVitalsView(this);
  state.addView(vitals,new LinearLayout.LayoutParams(-1,dp(207)));
  gap(state,4);add(state,txt("الرسم مبني على عينات هذا العرض فقط، وليس تسجيلًا دائمًا.",11,MUTED,false));
  title("التحليلات والخصوصية");
  LinearLayout records=card("سجل الهاتف","تحكم أنت في التسجيل والمشاركة");
  link(records,"سجل الأداء والحرارة","احفظ قراءات Android وقارنها",()->launch(TrendsActivity.class));
  link(records,"كبسولة الزمن","مقارنة التغيرات الأمنية بين لقطات",()->launch(TimeCapsuleActivity.class));
  link(records,"مشاركة ملخص الجهاز","تقرير عن موارد الهاتف المتاحة",this::share);
  note("V3 تجريبي: مؤشر الموارد ليس فحصًا مضادًا للفيروسات، والمخططات لا تتنبأ بأداء الألعاب.");
 }
 private void security(){
  title("SECURITY COMMAND");
  LinearLayout s=card("مركز الحماية الشامل","تدقيق شفاف دون ادعاء وجود محرك فيروسات");
  link(s,"المراقبة الدورية","تنبيهات التغييرات وإعدادات الحماية",()->launch(ProtectionActivity.class));
  link(s,"فحص ملفات APK الحقيقي","بصمة SHA-256 والأذونات وبنية الحزمة",()->launch(ApkInspectorActivity.class));
  link(s,"⬡  Smart Security Scan","قفل الجهاز، تصحيح USB، التحديثات والحرارة",()->launch(V3SecurityScanActivity.class));
  link(s,"تقرير الحالة الأمنية","مؤشرات إعدادات الحماية",()->launch(SecurityCenterActivity.class));
  link(s,"فحص أذونات التطبيقات","مراجعة الأذونات الحساسة",()->launch(PrivacyAuditActivity.class));
  LinearLayout system=card("حماية Android الرسمية","اختصارات إلى إعدادات النظام");
  link(system,"إعدادات الأمان","قفل الشاشة وحماية الجهاز",()->settings(Settings.ACTION_SECURITY_SETTINGS));
  link(system,"تحديثات النظام","مراجعة التحديثات المتاحة",()->settings(Settings.ACTION_DEVICE_INFO_SETTINGS));
  link(system,"تثبيت التطبيقات الخارجية","مراجعة مصادر التثبيت",()->settings(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
  note("لا يوجد في هذا الإصدار محرك مضاد فيروسات، ولا يمكن إعلان الهاتف خاليًا من البرمجيات الضارة.");
 }
 private void network(){
  title("NETWORK LAB");
  LinearLayout c=card("حالة الاتصال","بيانات الشبكة من Android");
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
  Network net=cm.getActiveNetwork();NetworkCapabilities caps=net==null?null:cm.getNetworkCapabilities(net);
  String type=caps==null?"غير متصل":caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"بيانات الهاتف":"اتصال آخر";
  stat(c,"نوع الاتصال",type,CYAN);
  stat(c,"تحقق Android من الإنترنت",caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"متصل":"غير مؤكد",GREEN);
  LinearLayout tests=card("اختبارات الشبكة","نتائج فعلية تعتمد على الاتصال الحالي");
  link(tests,"المراقب الحي للشبكة V3","مخطط تنزيل ورفع من عدّادات Android",()->launch(V3NetworkMonitorActivity.class));
  link(tests,"اختبار سرعة الإنترنت","قياس التنزيل والرفع وسجل الاختبارات",()->launch(NetworkSpeedActivity.class));
  link(tests,"V3  |  تحليلات الشبكة","اختبار HTTPS والتذبذب ومراقبة البيانات",()->launch(NetworkInsightActivity.class));
  link(tests,"تدقيق Wi-Fi","التشفير والإشارة ومعلومات الشبكة",()->launch(WifiAuditActivity.class));
  link(tests,"تشخيص DNS","الخوادم وحالة التهيئة",()->launch(DnsDiagnosticsActivity.class));
  link(tests,"Network Defense","فحص HTTPS وجودة الاتصال",()->launch(NetworkDefenseActivity.class));
  link(tests,"جدار حماية DNS","إعدادات الحجب بإذن المستخدم",()->launch(DnsFirewallActivity.class));
  note("قد يستهلك اختبار السرعة بيانات الهاتف. لا تُفسر نتيجة اتصال Android على أنها فحص أمني شامل.");
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
  link(c,"مركز الحماية الدورية","مراقبة التطبيقات والتنبيهات وقاعدة التهديدات",()->launch(ProtectionActivity.class));
  link(c,"فحص APK","تحليل ملف تثبيت تختاره من هاتفك",()->launch(ApkInspectorActivity.class));
  link(c,"⬡  Security Scan","تدقيق أمان الجهاز",()->launch(V3SecurityScanActivity.class));
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
  link(n,"TITAN NETWORK DEFENSE X","مختبر أمان HTTPS وDNS وجودة الاتصال",()->launch(NetworkDefenseActivity.class));
  link(n,"تشخيص الشبكة التفصيلي","عرض DNS ومعلومات الاتصال",()->launch(IntelligenceHubActivity.class));
 }
 private void note(String s){LinearLayout c=col();c.setPadding(dp(14),dp(14),dp(14),dp(14));c.setBackground(gradient(0xff2A261B,0xff1C201E,16,0xff7A5B2D));add(c,txt("ⓘ  "+s,12,AMBER,false));add(body,c);gap(body,15);}
 private void share(){
  String report="TITAN CORE X — Device Report\nDevice: "+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid: "+Build.VERSION.RELEASE+"\nEstimated resource score: "+score()+"/100\nBattery: "+batteryPct()+"%\nBattery temperature: "+temperature()+" C\nAvailable RAM: "+gb(ram().availMem)+"\nFree storage: "+gb(disk().getAvailableBytes())+"\nNo malware scan was performed.";
  Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,report);startActivity(Intent.createChooser(i,"مشاركة تقرير TITAN"));
 }
}
