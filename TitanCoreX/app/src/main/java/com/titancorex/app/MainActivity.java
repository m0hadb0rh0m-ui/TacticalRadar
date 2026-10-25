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
  private final int BG=0xff070B14, CARD=0xff121F33, PANEL=0xff192A43, CYAN=0xff42E8F5, WHITE=0xffF4F8FF, MUTED=0xff9CAFC7, GREEN=0xff4EF0A3, GOLD=0xffFFCE77, RED=0xffFF7288;
  private LinearLayout content, navigation;
  private TextView subtitle;
  private int page=0;
  private BatteryManager battery;
  private android.app.ActivityManager memory;
  private final String[] sectionKeys={"home","device","security","games","tools"};
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
    LinearLayout root=vertical();root.setBackgroundColor(BG);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);setContentView(root);
    LinearLayout top=vertical();pad(top,22,20,22,18);top.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff112B42,0xff090F1C}));
    LinearLayout brandRow=horizontal();android.widget.ImageView emblem=new android.widget.ImageView(this);emblem.setImageResource(R.drawable.titan_logo);brandRow.addView(emblem,new LinearLayout.LayoutParams(dp(54),dp(54)));TextView brand=label("TITAN CORE X",24,WHITE,true);brandRow.addView(brand,new LinearLayout.LayoutParams(0,-2,1));add(top,brandRow);
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
    LinearLayout c=vertical();pad(c,18,17,18,18);GradientDrawable premium=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff172A43,0xff101B2D});premium.setCornerRadius(dp(21));premium.setStroke(dp(1),0xff31536B);c.setBackground(premium);c.setElevation(dp(3));
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
    LinearLayout row=horizontal();pad(row,14,14,12,14);row.setBackground(border(PANEL,0xff2B4B65,15));row.setElevation(dp(1));
    LinearLayout texts=vertical();texts.addView(label(title,14,WHITE,true));
    if(info!=null){space(texts,4);texts.addView(label(info,11,MUTED,false));}
    row.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
    row.addView(label("›",27,CYAN,true));
    row.setOnClickListener(v->run.run());add(p,row);space(p,9);
  }
  private void nav(){
    navigation.removeAllViews();
    for(int i=0;i<sectionKeys.length;i++){
      final int index=i;
      LinearLayout cell=vertical();cell.setGravity(Gravity.CENTER);pad(cell,1,8,1,8);
      if(i==page)cell.setBackground(shape(0xff173448,12));
      String[] symbols={"◈","▣","⬡","⚡","☷"};
      TextView ico=label(symbols[i],21,i==page?CYAN:MUTED,true);ico.setGravity(Gravity.CENTER);add(cell,ico);
      TextView txt=label(LanguageActivity.tr(this,sectionKeys[i]),9,i==page?CYAN:MUTED,true);txt.setGravity(Gravity.CENTER);add(cell,txt);
      navigation.addView(cell,new LinearLayout.LayoutParams(0,-2,1));
      cell.setOnClickListener(v->show(index));
    }
  }
  @Override protected void onResume(){super.onResume();if(content!=null)show(page);}
  private void show(int index){
    page=index;content.removeAllViews();nav();subtitle.setText(new String[]{"مركز التحكم","تشخيص الجهاز","تدقيق الأمان","أداء الألعاب","أدوات النظام"}[page]);
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
    LinearLayout hero=vertical();pad(hero,22,23,22,23);GradientDrawable heroBg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff144A5D,0xff11243D,0xff12162B});heroBg.setCornerRadius(dp(26));heroBg.setStroke(dp(2),0xff39A5B5);hero.setBackground(heroBg);hero.setElevation(dp(6));
    add(hero,label("صحة الجهاز المباشرة",12,CYAN,true));space(hero,10);
    add(hero,label(score()+" / 100",47,WHITE,true));
    add(hero,label("مؤشر تقديري يعتمد على الذاكرة وحرارة البطارية والتخزين",12,MUTED,false));
    space(hero,14);
    ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);bar.setMax(100);bar.setProgress(score());bar.setProgressTintList(android.content.res.ColorStateList.valueOf(CYAN));add(hero,bar);
    block(hero);
    section("القياسات المباشرة");
    LinearLayout c=card("ملخص الجهاز","قياسات حقيقية من نظام Android");
    metric(c,"مستوى البطارية",level()<0?"Unavailable":level()+"%",GREEN);
    metric(c,"حرارة البطارية",temp()<0?"Unavailable":String.format(Locale.US,"%.1f °C",temp()),CYAN);
    android.app.ActivityManager.MemoryInfo m=ram();
    metric(c,"الذاكرة المتاحة",format(m.availMem)+" / "+format(m.totalMem),WHITE);
    metric(c,"التخزين المتاح",format(disk().getAvailableBytes()),GOLD);
    block(c);
    LinearLayout actions=card("الوصول السريع","تشخيص الجهاز وأدوات النظام الآمنة");
    action(actions,"⬡  مركز القيادة الأمنية","فحص قفل الشاشة وتصحيح USB وإعدادات الحماية",()->startActivity(new Intent(this,SecurityCenterActivity.class)));
    action(actions,"✦  TITAN CORE X PRO","استكشف خطة المزايا المتقدمة بسعر مستهدف 5 دولارات",()->startActivity(new Intent(this,PremiumActivity.class)));
    action(actions,"📈  سجل الأداء والحرارة","تسجيل قياسات ومخطط حرارة البطارية",()->startActivity(new Intent(this,TrendsActivity.class)));
    action(actions,"🌐  "+LanguageActivity.tr(this,"language"),"اختيار لغة الواجهة من 20 لغة",()->startActivity(new Intent(this,LanguageActivity.class)));
    action(actions,"مختبر التبريد والرام والتخزين","مراقبة الحرارة والذاكرة والتنظيف الآمن",()->startActivity(new Intent(this,ResourceLabActivity.class)));
    action(actions,"فحص الهاتف","البطارية والذاكرة والتخزين والشبكة",()->show(1));
    action(actions,"مراجعة الحماية","التطبيقات والأذونات",()->show(2));
    action(actions,"مركز أداء الألعاب","تهيئة جلسة لعب",()->show(3));
    action(actions,"مشاركة تقرير الفحص","تصدير قياسات الجهاز إلى نص",()->shareReport());
    block(actions);
    note("Privacy first: measurements stay on your device. No cloud upload or background scanning in this release.");
  }
  private void device(){
    section("الأجهزة والنظام");
    LinearLayout c=card("معلومات الهاتف","مواصفات النظام المباشرة");
    metric(c,"الشركة المصنعة",android.os.Build.MANUFACTURER,WHITE);
    metric(c,"الطراز",android.os.Build.MODEL,WHITE);
    metric(c,"إصدار أندرويد",android.os.Build.VERSION.RELEASE,WHITE);
    metric(c,"مستوى API",String.valueOf(android.os.Build.VERSION.SDK_INT),WHITE);
    metric(c,"أنوية المعالج",String.valueOf(Runtime.getRuntime().availableProcessors()),CYAN);
    metric(c,"الرام المتاحة",format(ram().availMem),GREEN);
    metric(c,"إجمالي الرام",format(ram().totalMem),WHITE);
    metric(c,"التخزين الحر",format(disk().getAvailableBytes()),GREEN);
    metric(c,"إجمالي التخزين",format(disk().getTotalBytes()),WHITE);
    block(c);
    section("مختبر البطارية");
    LinearLayout b=card("تحليل البطارية","تعتمد القياسات على دعم الشركة المصنعة");
    Intent i=batteryIntent();
    int status=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_STATUS,-1);
    metric(b,"حالة الشحن",status==BatteryManager.BATTERY_STATUS_CHARGING?"Yes":status==BatteryManager.BATTERY_STATUS_FULL?"Full":"No / unknown",GREEN);
    metric(b,"درجة الحرارة",temp()<0?"Unavailable":String.format(Locale.US,"%.1f °C",temp()),CYAN);
    int voltage=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_VOLTAGE,-1);
    metric(b,"الجهد",voltage<0?"Unavailable":voltage+" mV",WHITE);
    long current=battery.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
    metric(b,"التيار (حسب الجهاز)",current==Long.MIN_VALUE?"Unavailable":String.format(Locale.US,"%.0f mA",current/1000.0),WHITE);
    block(b);
    note("Battery temperature is not the CPU temperature. Battery health percentage is not estimated without reliable cycle/capacity data.");
  }
  private List<android.content.pm.ResolveInfo> launchers(){
    Intent intent=new Intent(Intent.ACTION_MAIN);intent.addCategory(Intent.CATEGORY_LAUNCHER);
    return getPackageManager().queryIntentActivities(intent,0);
  }
  private void security(){
    section("مركز الحماية");
    LinearLayout c=card("تدقيق التطبيقات","التطبيقات المرئية المسموح بفحصها");
    List<android.content.pm.ResolveInfo> apps=launchers();
    HashSet<String> names=new HashSet<>();for(android.content.pm.ResolveInfo r:apps)if(r.activityInfo!=null)names.add(r.activityInfo.packageName);
    metric(c,"التطبيقات الظاهرة",String.valueOf(names.size()),CYAN);
    metric(c,"محرك كشف البرمجيات الضارة","غير متوفر حاليًا",GOLD);
    metric(c,"حالة مضاد الفيروسات","غير معتمد",GOLD);
    block(c);
    LinearLayout shield=card("⬡  TITAN SECURITY COMMAND","مراجعة مؤشرات الأمان الحقيقية");
    action(shield,"بدء فحص إعدادات الحماية","قفل الشاشة وADB وخيارات المطور",()->startActivity(new Intent(this,SecurityCenterActivity.class)));
    block(shield);
    LinearLayout a=card("اختصارات الأمان","أدوات الحماية الرسمية في Android");
    action(a,"مختبر الخصوصية المتقدم","فحص الأذونات الحساسة الممنوحة لكل تطبيق ظاهر",()->startActivity(new Intent(this,PrivacyAuditActivity.class)));
    action(a,"أذونات التطبيقات","مراجعة الكاميرا والميكروفون والموقع",()->open(Settings.ACTION_APPLICATION_SETTINGS));
    action(a,"إعدادات الأمان","خيارات حماية الهاتف",()->open(Settings.ACTION_SECURITY_SETTINGS));
    action(a,"تثبيت التطبيقات غير المعروفة","مراجعة صلاحيات التثبيت الخارجية",()->open(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
    action(a,"إعدادات الخصوصية","مراجعة إعدادات الخصوصية",()->open(Settings.ACTION_PRIVACY_SETTINGS));
    block(a);
    note("This is a security audit dashboard, NOT an antivirus scanner. A genuine malware detector requires an updated detection engine and permitted package visibility.");
  }
  private void boost(){
    section("أداء الألعاب");
    LinearLayout c=card("جاهزية اللعب","قياسات فعلية دون ادعاءات تسريع وهمية");
    metric(c,"الرام الحرة",format(ram().availMem),CYAN);
    metric(c,"حرارة البطارية",temp()<0?"Unavailable":String.format(Locale.US,"%.1f °C",temp()),GREEN);
    metric(c,"البطارية",level()<0?"Unavailable":level()+"%",WHITE);
    block(c);
    LinearLayout a=card("استعد للعب","إعدادات تحسين يدعمها Android");
    action(a,"إدارة التطبيقات الخلفية","مراجعة التطبيقات من إعدادات النظام",()->open(Settings.ACTION_APPLICATION_SETTINGS));
    action(a,"إعدادات عدم الإزعاج","تقليل الإشعارات أثناء اللعب",()->open(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
    action(a,"إعدادات تحسين البطارية","مراجعة قيود البطارية",()->open(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
    action(a,"إعدادات الشبكة","فحص الاتصال قبل اللعب الجماعي",()->open(Settings.ACTION_WIRELESS_SETTINGS));
    action(a,"بدء جلسة لعب","تسجيل وقت البداية وقياسات الجهاز",()->startGameSession());
    action(a,"إنهاء جلسة لعب","حفظ المدة وحرارة البطارية",()->endGameSession());
    String last=getPreferences(MODE_PRIVATE).getString("last_game","لا توجد جلسة محفوظة");
    a.addView(label("آخر جلسة: "+last,12,MUTED,false));
    block(a);
    note("Android prevents ordinary apps from force-closing all other apps or guaranteeing higher FPS. This release provides diagnostics and safe shortcuts.");
  }
  private void tools(){
    section("مختبر الشبكة");
    LinearLayout n=card("تشخيص الاتصال","حالة الاتصال الفعلية");
    ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
    Network network=cm.getActiveNetwork();
    NetworkCapabilities cap=network==null?null:cm.getNetworkCapabilities(network);
    String type=cap==null?"Disconnected":cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":cap.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"Cellular":cap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"Ethernet":"Other";
    metric(n,"نوع الاتصال",type,CYAN);
    metric(n,"إمكانية الاتصال بالإنترنت",cap!=null&&cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)?"Advertised":"Unavailable",WHITE);
    metric(n,"اتصال مؤكد من Android",cap!=null&&cap.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"Yes":"No / unknown",GREEN);
    block(n);
    section("إدارة التخزين");
    LinearLayout s=card("فحص التخزين","تنظيف آمن بأدوات Android");
    metric(s,"إجمالي التخزين الداخلي",format(disk().getTotalBytes()),WHITE);
    metric(s,"التخزين الداخلي المتاح",format(disk().getAvailableBytes()),GREEN);
    action(s,"فتح إدارة التخزين","مراجعة الملفات وحذفها من النظام",()->open(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
    action(s,"فتح مستعرض الملفات","استعراض الملفات دون حذف تلقائي",()->{
      try{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,4);}catch(Exception e){toast("No file picker available");}
    });
    action(s,"فحص الملفات المكررة بدقة","مقارنة SHA-256 داخل مجلد تختاره",()->startActivity(new Intent(this,StorageAuditActivity.class)));
    block(s);
    section("أدوات النظام");
    LinearLayout u=card("لوحة الأدوات","أدوات نظام الهاتف");
    action(u,"✦  باقة TITAN PRO","الميزات المستقبلية وخطة الدفع",()->startActivity(new Intent(this,PremiumActivity.class)));
    action(u,"📈  سجل أداء الهاتف","متابعة تاريخ البطارية والذاكرة",()->startActivity(new Intent(this,TrendsActivity.class)));
    action(u,"🌐  "+LanguageActivity.tr(this,"language"),"20 لغة للاختيار — ترجمة الأقسام الأساسية حاليًا",()->startActivity(new Intent(this,LanguageActivity.class)));
    action(u,"إعدادات الشبكة","إعدادات Wi-Fi وبيانات الهاتف",()->open(Settings.ACTION_WIRELESS_SETTINGS));
    action(u,"إعدادات البطارية","استهلاك البطارية وخيارات الشحن",()->open(Settings.ACTION_BATTERY_SAVER_SETTINGS));
    action(u,"مركز استعادة الموارد","الحرارة والرام والتخزين في لوحة واحدة",()->startActivity(new Intent(this,ResourceLabActivity.class)));
    action(u,"تحديث القياسات","إعادة قراءة بيانات الهاتف",()->show(page));
    block(u);
    note("Deep app caches and protected system files cannot be erased silently on modern Android. Cleanup always requires user-approved access.");
  }
  private void note(String s){LinearLayout c=vertical();pad(c,14,14,14,14);c.setBackground(border(0xff18212B,0xff37475A,12));add(c,label("ⓘ  "+s,12,GOLD,false));block(c);}
  private void startGameSession(){
    getPreferences(MODE_PRIVATE).edit().putLong("game_start",System.currentTimeMillis()).putFloat("game_temp",temp()).apply();
    toast("بدأ تسجيل جلسة اللعب");show(3);
  }
  private void endGameSession(){
    android.content.SharedPreferences p=getPreferences(MODE_PRIVATE);
    long start=p.getLong("game_start",0);
    if(start==0){toast("ابدأ جلسة أولًا");return;}
    long mins=Math.max(0,(System.currentTimeMillis()-start)/60000);
    float startTemp=p.getFloat("game_temp",-1);
    String report=mins+" دقيقة | حرارة البداية "+startTemp+"° | النهاية "+temp()+"°";
    p.edit().remove("game_start").putString("last_game",report).apply();
    toast("تم حفظ الجلسة");show(3);
  }
  private void shareReport(){
    android.app.ActivityManager.MemoryInfo m=ram();
    String report="TITAN CORE X - Device Report\\n"
      +"Model: "+android.os.Build.MANUFACTURER+" "+android.os.Build.MODEL+"\\n"
      +"Android: "+android.os.Build.VERSION.RELEASE+"\\n"
      +"Health estimate: "+score()+"/100\\n"
      +"Battery: "+level()+"%\\n"
      +"Battery temperature: "+temp()+" C\\n"
      +"RAM free: "+format(m.availMem)+"\\n"
      +"Storage free: "+format(disk().getAvailableBytes())+"\\n"
      +"Generated on device. No malware scan was performed.";
    Intent send=new Intent(Intent.ACTION_SEND);
    send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,report);
    startActivity(Intent.createChooser(send,"مشاركة تقرير TITAN CORE X"));
  }
  private void open(String action){try{startActivity(new Intent(action));}catch(Exception e){toast("This settings page is unavailable on your device");}}
  private void toast(String msg){Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();}
}
