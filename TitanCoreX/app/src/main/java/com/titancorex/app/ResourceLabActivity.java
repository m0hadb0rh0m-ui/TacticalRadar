package com.titancorex.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.io.File;
import java.util.*;

public class ResourceLabActivity extends Activity {
  private static final int BG=0xff080C16,CARD=0xff142136,CYAN=0xff42E8F5,WHITE=0xffF4F8FF,MUTED=0xffA9BBD1,GREEN=0xff52F3B0,GOLD=0xffFFD07C,RED=0xffFF788C;
  private LinearLayout root,readouts;
  private TextView status;
  private Handler handler=new Handler(Looper.getMainLooper());
  private final ArrayDeque<Integer> thermalHistory=new ArrayDeque<>();
  private final ArrayDeque<Long> ramHistory=new ArrayDeque<>();
  private final Runnable updater=new Runnable(){public void run(){refresh();handler.postDelayed(this,4000);}};
  private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
  private TextView t(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(2),dp(7),dp(2),dp(7));return v;}
  private GradientDrawable bg(){GradientDrawable d=new GradientDrawable();d.setColor(CARD);d.setCornerRadius(dp(16));d.setStroke(dp(1),0xff2A435B);return d;}
  private LinearLayout card(String title){
    LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(15),dp(15),dp(15),dp(15));l.setBackground(bg());l.addView(t(title,18,CYAN));
    root.addView(l);View gap=new View(this);root.addView(gap,new LinearLayout.LayoutParams(1,dp(15)));return l;
  }
  private void button(LinearLayout parent,String title,String desc,Runnable callback){
    Button b=new Button(this);b.setAllCaps(false);b.setText(title);parent.addView(b);
    if(desc!=null)parent.addView(t(desc,12,MUTED));b.setOnClickListener(v->callback.run());
  }
  private void line(LinearLayout p,String label,String value,int color){p.addView(t(label+"   "+value,14,color));}
  private String gb(long bytes){return String.format(Locale.US,"%.2f GB",bytes/1073741824.0);}
  private void open(String action){try{startActivity(new Intent(action));}catch(Exception e){Toast.makeText(this,"هذا الخيار غير متاح على جهازك",Toast.LENGTH_SHORT).show();}}
  private String thermalName(int n){
    switch(n){
      case 0:return "طبيعي — لا توجد قيود حرارية";
      case 1:return "خفيف";
      case 2:return "متوسط";
      case 3:return "مرتفع";
      case 4:return "حرج";
      case 5:return "حالة طوارئ";
      case 6:return "إيقاف وشيك";
      default:return "غير معروف";
    }
  }
  @Override public void onCreate(Bundle b){
    super.onCreate(b);
    getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
    ScrollView scroll=new ScrollView(this);
    root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(18),dp(22),dp(18),dp(22));root.setBackgroundColor(BG);
    scroll.addView(root);setContentView(scroll);
    root.addView(t("◈  TITAN RESOURCE LAB",24,CYAN));
    root.addView(t("مركز التحكم بالحرارة والرام والتخزين",17,WHITE));
    root.addView(t("قياسات من نظام Android — بدون أرقام وهمية أو تنظيف إجباري لتطبيقات أخرى",12,MUTED));
    readouts=card("القياسات المباشرة");
    status=t("جارٍ قراءة بيانات الجهاز...",12,GOLD);root.addView(status);
    LinearLayout cool=card("❄  THERMAL GUARDIAN | الحماية الحرارية");
    cool.addView(t("إذا ارتفعت الحرارة: أوقف اللعب مؤقتًا، افصل الشاحن عند الحاجة، أزل الغطاء إن كان يعيق التهوية، وانتقل إلى مكان معتدل الحرارة. لا تستخدم الثلاجة أو الماء.",13,MUTED));
    button(cool,"فتح إعدادات توفير الطاقة","تقليل الاستهلاك قد يخفف توليد الحرارة",()->open(Settings.ACTION_BATTERY_SAVER_SETTINGS));
    button(cool,"مراجعة التطبيقات المستهلكة","افحص استهلاك التطبيقات يدويًا",()->open(Settings.ACTION_BATTERY_SAVER_SETTINGS));
    LinearLayout ram=card("◈  RAM PRESSURE LAB | ضغط الذاكرة");
    ram.addView(t("راقب مقدار RAM المتاحة وحالة lowMemory الفعلية. لا يُنصح بإغلاق جميع التطبيقات عشوائيًا؛ فقد يزيد ذلك الاستهلاك عند إعادة تشغيلها.",13,MUTED));
    button(ram,"مراجعة التطبيقات المثبتة","افتح إعدادات التطبيقات لتحديد المستهلكة",()->open(Settings.ACTION_APPLICATION_SETTINGS));
    button(ram,"تحرير موارد التطبيق نفسه","يطلب من Java تنظيف الكائنات غير المستخدمة في هذا التطبيق فقط",()->{
      System.gc();Toast.makeText(this,"تم طلب جمع الذاكرة غير المستخدمة داخل TITAN CORE X فقط",Toast.LENGTH_LONG).show();refresh();
    });
    LinearLayout storage=card("▣  STORAGE RECOVERY | استعادة مساحة التخزين");
    storage.addView(t("تحليل الملفات المكررة، وإدارة مساحة النظام، وتنظيف الملفات المؤقتة الخاصة بهذا التطبيق فقط. لا حذف لملفاتك دون اختيارك.",13,MUTED));
    button(storage,"البحث عن الملفات المكررة","فحص مجلد تختاره باستخدام بصمة SHA-256",()->startActivity(new Intent(this,StorageAuditActivity.class)));
    button(storage,"مدير مساحة التخزين","أدوات Android الرسمية لتنظيف الملفات",()->open(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
    button(storage,"حساب مساحة ذاكرة TITAN المؤقتة","احسب حجم الملفات المؤقتة الخاصة بهذا التطبيق",()->{
      long size=bytes(getCacheDir(),0);new AlertDialog.Builder(this).setTitle("ذاكرة TITAN المؤقتة")
        .setMessage("الحجم التقريبي: "+String.format(Locale.US,"%.2f MB",size/1048576.0)+"\nهل تريد حذف الملفات المؤقتة الخاصة بهذا التطبيق فقط؟")
        .setNegativeButton("إلغاء",null)
        .setPositiveButton("تنظيف آمن",(d,w)->{
          long before=bytes(getCacheDir(),0);clear(getCacheDir(),false,0);long after=bytes(getCacheDir(),0);
          Toast.makeText(this,"تم تحرير "+String.format(Locale.US,"%.2f MB",Math.max(0,before-after)/1048576.0),Toast.LENGTH_LONG).show();refresh();
        }).show();
    });
    root.addView(t("تنبيه: حالة الحرارة من Android قد لا تكون متاحة على كل هاتف، وحرارة البطارية ليست حرارة المعالج. لا توجد صلاحيات Root في هذا التطبيق.",12,GOLD));
    refresh();
  }
  private long bytes(File f,int depth){
    if(f==null||!f.exists()||depth>20)return 0;
    if(f.isFile())return f.length();
    long sum=0;File[] children=f.listFiles();if(children!=null)for(File c:children)sum+=bytes(c,depth+1);return sum;
  }
  private void clear(File f,boolean removeRoot,int depth){
    if(f==null||!f.exists()||depth>20)return;
    if(f.isDirectory()){File[] children=f.listFiles();if(children!=null)for(File c:children)clear(c,true,depth+1);}
    if(removeRoot)f.delete();
  }
  private void refresh(){
    if(readouts==null)return;
    readouts.removeViews(1,Math.max(0,readouts.getChildCount()-1));
    android.app.ActivityManager am=(android.app.ActivityManager)getSystemService(ACTIVITY_SERVICE);
    android.app.ActivityManager.MemoryInfo mem=new android.app.ActivityManager.MemoryInfo();am.getMemoryInfo(mem);
    android.os.StatFs disk=new android.os.StatFs(android.os.Environment.getDataDirectory().getAbsolutePath());
    Intent bat=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    float temp=bat==null?-1:bat.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1)/10f;
    PowerManager pm=(PowerManager)getSystemService(POWER_SERVICE);
    int thermal=android.os.Build.VERSION.SDK_INT>=29?pm.getCurrentThermalStatus():-1;
    thermalHistory.addLast(thermal);if(thermalHistory.size()>20)thermalHistory.removeFirst();
    ramHistory.addLast(mem.availMem);if(ramHistory.size()>20)ramHistory.removeFirst();
    line(readouts,"حرارة البطارية:",temp<0?"غير متاحة":String.format(Locale.US,"%.1f °C",temp),temp>=42?RED:CYAN);
    line(readouts,"ضغط الحرارة في النظام:",thermal<0?"غير متاح":thermalName(thermal),thermal>=3?RED:GREEN);
    line(readouts,"RAM المتاحة:",gb(mem.availMem)+" / "+gb(mem.totalMem),GREEN);
    line(readouts,"تحذير انخفاض RAM:",mem.lowMemory?"نعم — ضغط مرتفع":"لا",mem.lowMemory?RED:GREEN);
    line(readouts,"حد انخفاض RAM:",gb(mem.threshold),MUTED);
    line(readouts,"التخزين المتاح:",gb(disk.getAvailableBytes())+" / "+gb(disk.getTotalBytes()),CYAN);
    line(readouts,"ذاكرة التطبيق المؤقتة:",String.format(Locale.US,"%.2f MB",bytes(getCacheDir(),0)/1048576.0),GOLD);
    line(readouts,"توفير الطاقة:",pm.isPowerSaveMode()?"مفعل":"غير مفعل",MUTED);
    long first=ramHistory.getFirst(),last=ramHistory.getLast();
    line(readouts,"اتجاه RAM خلال آخر القياسات:",last>first?"المتاح يزداد":last<first?"المتاح ينخفض":"مستقر",MUTED);
    String advice=(thermal>=3||temp>=43)?"حرارة مرتفعة: أوقف الألعاب والشحن غير الضروري واترك الهاتف يبرد طبيعيًا":
      mem.lowMemory?"ضغط RAM مرتفع: راجع التطبيقات الثقيلة ولا تغلق خدمات النظام":
      (double)disk.getAvailableBytes()/disk.getTotalBytes()<.12?"التخزين شبه ممتلئ: راجع الملفات الكبيرة والمكررة":
      "حالة الموارد مستقرة حسب القياسات المتاحة";
    status.setText("المستشار الذكي: "+advice);
  }
  @Override protected void onResume(){super.onResume();handler.removeCallbacks(updater);handler.post(updater);}
  @Override protected void onPause(){handler.removeCallbacks(updater);super.onPause();}
}
