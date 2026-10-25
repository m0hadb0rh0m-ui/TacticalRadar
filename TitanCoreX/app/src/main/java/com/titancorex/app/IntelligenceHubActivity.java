package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.net.*;
import java.util.*;

public class IntelligenceHubActivity extends Activity {
 private final int BG=0xffF1F8FF,CARD=0xffFFFFFF,CYAN=0xff087BEA,WHITE=0xff193657,MUTED=0xff526B88,GOLD=0xffA66D00,GREEN=0xff087C58;
 private LinearLayout root;private EditText urlInput;private TextView result;
 private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int z,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(color);t.setPadding(dp(9),dp(8),dp(9),dp(8));return t;}
 private GradientDrawable shape(){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xffFFFFFF,0xffE8F3FF});d.setCornerRadius(dp(20));d.setStroke(dp(1),0xff315B70);return d;}
 private LinearLayout card(String title,String subtitle){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(12),dp(13),dp(12),dp(13));c.setBackground(shape());c.addView(text(title,19,CYAN));c.addView(text(subtitle,12,MUTED));root.addView(c);View gap=new View(this);root.addView(gap,new LinearLayout.LayoutParams(1,dp(13)));return c;}
 private void button(LinearLayout c,String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);c.addView(b);b.setOnClickListener(v->action.run());}
 private void launch(Class<?> c){startActivity(new Intent(this,c));}
 @Override public void onCreate(Bundle b){
  super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(17),dp(22),dp(17),dp(25));root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
  root.addView(text("◈  TITAN INTELLIGENCE X",25,CYAN));
  root.addView(text("10 أنظمة مترابطة لفحص الجهاز والخصوصية والأداء",14,WHITE));
  root.addView(text("التحليل محلي دون رفع بياناتك. لا يوجد محرك مضاد فيروسات أو خدمة ذكاء اصطناعي سحابية في هذا الإصدار.",12,MUTED));
  LinearLayout scam=card("01  SCAM SHIELD","فحص أولي للروابط المشبوهة؛ ليس قاعدة تهديدات ولا ضمان سلامة");
  urlInput=new EditText(this);urlInput.setSingleLine(true);urlInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);urlInput.setHint("https://example.com");scam.addView(urlInput);
  result=text("ألصق رابطًا لفحص مؤشرات الاشتباه دون فتحه.",13,MUTED);scam.addView(result);
  button(scam,"تحليل الرابط محليًا",()->scanUrl(urlInput.getText().toString()));
  LinearLayout privacy=card("02  PRIVACY INTELLIGENCE","مراجعة أذونات التطبيقات ومقارنة إعدادات الحماية");
  button(privacy,"تدقيق أذونات التطبيقات",()->launch(PrivacyAuditActivity.class));
  button(privacy,"كبسولة التغيرات الأمنية",()->launch(TimeCapsuleActivity.class));
  LinearLayout battery=card("03  BATTERY INTELLIGENCE","تيار البطارية وحرارتها وسجل جلسات الشحن");
  button(battery,"فتح مختبر الطاقة",()->launch(PowerStationActivity.class));
  LinearLayout security=card("04  SECURITY HEALTH","قفل الشاشة وتصحيح USB وخيارات المطور");
  button(security,"فحص إعدادات الحماية",()->launch(SecurityCenterActivity.class));
  LinearLayout storage=card("05  STORAGE RECOVERY","اكتشاف الملفات المكررة داخل مجلد يحدده المستخدم");
  button(storage,"فحص الملفات المكررة",()->launch(StorageAuditActivity.class));
  LinearLayout gaming=card("06  GAME PERFORMANCE","قياس ظروف الهاتف قبل اللعب وبعده");
  button(gaming,"مختبر الموارد والحرارة",()->launch(ResourceLabActivity.class));
  LinearLayout network=card("07  NETWORK SECURITY LAB","قراءة نوع الاتصال والتحقق من حالة الشبكة وDNS المتاح");
  button(network,"تشخيص الشبكة الآن",()->networkReport(network));
  LinearLayout capsule=card("08  DIGITAL TIME CAPSULE","كشف تغيّر إعدادات الهاتف بين لقطتين");
  button(capsule,"مقارنة حالة الجهاز",()->launch(TimeCapsuleActivity.class));
  LinearLayout advisor=card("09  DEVICE ADVISOR","توصيات قواعدية مستندة إلى قراءات فعلية، وليست ذكاءً اصطناعيًا توليديًا");
  button(advisor,"إنشاء توصيات الجهاز",()->advice(advisor));
  LinearLayout reports=card("10  REPORT STUDIO","سجل الأداء وتقارير قابلة للمشاركة");
  button(reports,"مخطط الأداء",()->launch(TrendsActivity.class));
  button(reports,"مشاركة تقرير التشخيص",this::share);
 }
 private void scanUrl(String raw){
  String input=raw.trim();if(input.length()>2048){result.setText("الرابط طويل جدًا");return;}
  try{
   URI u=new URI(input);String scheme=u.getScheme(),host=u.getHost();
   if(scheme==null||host==null||!(scheme.equalsIgnoreCase("https")||scheme.equalsIgnoreCase("http"))){result.setText("أدخل رابط HTTP أو HTTPS كاملًا وصحيحًا.");return;}
   String h=java.net.IDN.toASCII(host).toLowerCase(Locale.US);
   ArrayList<String> flags=new ArrayList<>();
   if(scheme.equalsIgnoreCase("http"))flags.add("الرابط لا يستخدم HTTPS");
   if(u.getRawUserInfo()!=null)flags.add("الرابط يتضمن بيانات مستخدم قبل اسم المضيف");
   if(h.contains("xn--"))flags.add("اسم نطاق دولي مُرمّز يستحق التحقق");
   if(h.split("\\.").length>5)flags.add("عدد كبير من النطاقات الفرعية");
   if(h.matches(".*[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}.*"))flags.add("عنوان IP بدل اسم نطاق");
   if(h.length()>55)flags.add("اسم نطاق طويل بصورة غير معتادة");
   String path=u.getRawPath()==null?"":u.getRawPath().toLowerCase(Locale.US);
   if(path.matches(".*(verify.account|confirm.password|reset.password|claim.prize).*"))flags.add("عبارات شائعة في روابط التصيّد");
   String message=flags.isEmpty()?"لم تُكتشف مؤشرات الاشتباه المحددة. هذا لا يعني أن الرابط آمن.":"مؤشرات تحتاج مراجعة ("+flags.size()+"):\n• "+TextUtils.join("\n• ",flags);
   result.setText("النطاق: "+h+"\n"+message+"\nلم يتم فتح الرابط أو الاتصال بالخادم.");
  }catch(Exception e){result.setText("تعذّر تحليل صيغة الرابط. تحقق من كتابته.");}
 }
 private void networkReport(LinearLayout c){
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
  Network n=cm.getActiveNetwork();NetworkCapabilities caps=n==null?null:cm.getNetworkCapabilities(n);
  LinkProperties props=n==null?null:cm.getLinkProperties(n);
  String type=caps==null?"غير متصل":caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"شبكة خلوية":caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)?"Ethernet":"اتصال آخر";
  String dns="غير متاح";if(props!=null){ArrayList<String> list=new ArrayList<>();for(java.net.InetAddress a:props.getDnsServers())list.add(a.getHostAddress());dns=TextUtils.join("، ",list);}
  String status=caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"اتصال تحقّق منه Android":"غير مؤكد";
  c.addView(text("نوع الاتصال: "+type+"\nالتحقق من الإنترنت: "+status+"\nخوادم DNS المعلنة: "+dns+"\nهذه معلومات تشخيصية وليست اختبارًا لاعتراض الاتصال.",13,WHITE));
 }
 private void advice(LinearLayout c){
  android.app.ActivityManager am=(android.app.ActivityManager)getSystemService(ACTIVITY_SERVICE);android.app.ActivityManager.MemoryInfo m=new android.app.ActivityManager.MemoryInfo();am.getMemoryInfo(m);
  android.os.StatFs fs=new android.os.StatFs(android.os.Environment.getDataDirectory().getAbsolutePath());
  Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));int t=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);
  ArrayList<String> tips=new ArrayList<>();
  if(m.lowMemory||m.availMem<m.totalMem/6)tips.add("ضغط الذاكرة مرتفع: راجع التطبيقات الثقيلة من إعدادات النظام.");
  else tips.add("الذاكرة المتاحة ليست في مستوى الضغط المرتفع حسب الفحص الحالي.");
  if(fs.getAvailableBytes()<fs.getTotalBytes()/8)tips.add("مساحة التخزين منخفضة: راجع الملفات الكبيرة والمكررة.");
  else tips.add("مساحة التخزين المتاحة جيدة وفق عتبة هذا الفحص.");
  if(t>=420)tips.add("حرارة البطارية مرتفعة: خفف الاستخدام الثقيل واترك الجهاز يبرد طبيعيًا.");
  else if(t>=0)tips.add("حرارة البطارية الحالية: "+(t/10f)+"°C.");
  c.addView(text(TextUtils.join("\n\n",tips),13,WHITE));
 }
 private void share(){
  android.app.ActivityManager am=(android.app.ActivityManager)getSystemService(ACTIVITY_SERVICE);android.app.ActivityManager.MemoryInfo m=new android.app.ActivityManager.MemoryInfo();am.getMemoryInfo(m);
  android.os.StatFs fs=new android.os.StatFs(android.os.Environment.getDataDirectory().getAbsolutePath());
  Intent i=registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));int t=i==null?-1:i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,-1);
  String report="TITAN INTELLIGENCE X\nDevice: "+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid: "+Build.VERSION.RELEASE+"\nSecurity patch: "+Build.VERSION.SECURITY_PATCH+"\nRAM free: "+(m.availMem/1048576)+" MB\nStorage free: "+(fs.getAvailableBytes()/1048576)+" MB\nBattery temp: "+(t<0?"Unavailable":(t/10f)+" C")+"\nLocal diagnostic; not an antivirus verdict.";
  Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,report);startActivity(Intent.createChooser(send,"مشاركة تقرير TITAN"));
 }
}
