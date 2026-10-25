package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.net.*;
import java.io.*;
import java.security.cert.*;
import javax.net.ssl.*;
import java.util.*;

/** Consent-driven network checks. No interception, packet capture or port scanning. */
public class NetworkDefenseActivity extends Activity {
 private LinearLayout root;private TextView output;private EditText host;
 private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private void action(String label,Runnable fn){Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setTextSize(15);b.setTextColor(0xffF7FBFF);android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable(android.graphics.drawable.GradientDrawable.Orientation.TL_BR,new int[]{0xff164B72,0xff10273E});d.setCornerRadius(dp(17));d.setStroke(dp(1),0xff2789BD);b.setBackground(d);root.addView(b);View space=new View(this);root.addView(space,new LinearLayout.LayoutParams(1,dp(10)));b.setOnClickListener(v->fn.run());}
 private void run(Check check){output.setText("جارٍ الفحص...");new Thread(()->{String s;try{s=check.call();}catch(Exception e){s="تعذر الفحص: "+e.getClass().getSimpleName()+" — "+e.getMessage();}final String v=s;runOnUiThread(()->output.setText(v));}).start();}
 private interface Check{String call()throws Exception;}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(0xff080D18);
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(20),dp(18),dp(20));root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setBackgroundColor(0xffF2F8FF);sc.addView(root);setContentView(sc);
  root.addView(new TitanCyberSceneView(this,0xff00D9FF),new LinearLayout.LayoutParams(-1,dp(170)));
  TextView title=new TextView(this);title.setText("TITAN NETWORK DEFENSE X");title.setTextSize(25);title.setTypeface(android.graphics.Typeface.create("sans-serif-black",1));title.setTextColor(0xff00D9FF);root.addView(title);
  host=new EditText(this);host.setSingleLine(true);host.setText("example.com");host.setHint("اسم نطاق لفحص HTTPS");host.setTextColor(0xffFFFFFF);host.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);root.addView(host);
  action("تحليل مؤشرات رابط مشبوه",()->output.setText(urlRisk(host.getText().toString().trim())));
  action("فحص شهادة HTTPS والكمون",()->{String h=host.getText().toString().trim();run(()->tls(h));});
  action("حفظ خط أساس للشبكة",()->output.setText(baseline(true)));
  action("كشف تغيّر DNS والبوابة",()->output.setText(baseline(false)));
  action("قياس زمن استجابة HTTPS (5 محاولات)",()->run(this::latency));
  action("مراجعة إعدادات حماية DNS",()->{try{startActivity(new Intent("android.settings.PRIVATE_DNS_SETTINGS"));}catch(Exception e){startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));}});
  action("اختبار سرعة تنزيل ورفع حقيقي",()->startActivity(new Intent(this,NetworkSpeedActivity.class)));
  action("فحص خصوصية DNS ومحلل HTTPS",()->startActivity(new Intent(this,DnsDiagnosticsActivity.class)));
  action("TITAN DNS FIREWALL — حجب النطاقات عبر VPN",()->startActivity(new Intent(this,DnsFirewallActivity.class)));
  action("تفعيل مراقبة الشبكة الدورية",()->{NetworkWatchJob.schedule(this);output.setText("تمت جدولة المراقبة، وقد يؤخرها Android لتوفير الطاقة.");});
  action("إيقاف مراقبة الشبكة",()->{NetworkWatchJob.stop(this);output.setText("تم إيقاف المراقبة.");});
  action("عرض نتيجة آخر مراقبة",()->{android.content.SharedPreferences p=getSharedPreferences("titan_network_watch",MODE_PRIVATE);output.setText("آخر فحص: "+new Date(p.getLong("last_checked",0))+"\\nتغيّر الإعدادات: "+p.getBoolean("changed",false)+"\\n"+p.getString("last","لا توجد بيانات"));});
  action("اكتشاف الأجهزة ومراقبة هوية الشبكة",()->startActivity(new Intent(this,NetworkDiscoveryActivity.class)));
  action("فتح فحص Wi-Fi التفصيلي",()->startActivity(new Intent(this,WifiAuditActivity.class)));
  output=new TextView(this);output.setTextColor(0xffF0F6FF);output.setTextSize(15);output.setPadding(0,dp(15),0,dp(20));root.addView(output);
  output.setText("فحوصات تشخيصية عند الطلب. لا يتم اعتراض اتصالات التطبيقات أو منع المواقع أو مسح أجهزة الشبكة.");
 }
 private String urlRisk(String input){
  try{
   String raw=input.startsWith("http://")||input.startsWith("https://")?input:"https://"+input;
   URI uri=new URI(raw);String h=uri.getHost();if(h==null)return "رابط غير صالح";
   h=java.net.IDN.toASCII(h).toLowerCase(Locale.US);
   ArrayList<String> flags=new ArrayList<>();
   if(!"https".equalsIgnoreCase(uri.getScheme()))flags.add("الرابط لا يستخدم HTTPS");
   if(uri.getUserInfo()!=null)flags.add("الرابط يحتوي بيانات مستخدم قبل اسم المضيف");
   if(h.startsWith("xn--")||h.contains(".xn--"))flags.add("اسم نطاق دولي Punycode؛ راجع الاسم المرئي");
   if(h.matches("[0-9.]+"))flags.add("الرابط يستخدم عنوان IP بدل نطاق");
   if(h.split("\\.").length>5)flags.add("عدد كبير من النطاقات الفرعية");
   if(raw.contains("@"))flags.add("وجود الرمز @ قد يربك قراءة الرابط");
   StringBuilder s=new StringBuilder("النطاق الفعلي: "+h+"\\n");
   if(flags.isEmpty())s.append("لم تُرصد مؤشرات الشكل المحددة. لا يعني ذلك أن الموقع آمن.");
   else for(String flag:flags)s.append("• ").append(flag).append("\\n");
   s.append("\\nفحص شكلي محلي فقط، دون قاعدة تصيد حية أو تصفح للموقع.");
   return s.toString();
  }catch(Exception e){return "تعذر تحليل الرابط: "+e.getMessage();}
 }
 private String cleanHost(String s)throws Exception{
  if(s.length()>253||!s.matches("(?i)[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?")||s.contains("..")||!s.contains("."))throw new IOException("أدخل اسم نطاق صالحًا دون مسار أو بروتوكول");
  return s;
 }
 private String tls(String input)throws Exception{
  String name=cleanHost(input);long start=System.nanoTime();
  URL url=new URL("https://"+name+"/");HttpsURLConnection c=(HttpsURLConnection)url.openConnection();
  c.setConnectTimeout(6000);c.setReadTimeout(6000);c.setInstanceFollowRedirects(false);
  try{
   c.connect();long ms=(System.nanoTime()-start)/1000000;
   java.security.cert.Certificate[] certs=c.getServerCertificates();
   StringBuilder s=new StringBuilder("HTTPS: اتصال موثق من النظام\nالكمون التقريبي لإنشاء الاتصال: "+ms+" ms\n");
   if(certs.length>0&&certs[0] instanceof X509Certificate){
    X509Certificate x=(X509Certificate)certs[0];x.checkValidity();
    s.append("الجهة: ").append(x.getSubjectX500Principal().getName()).append("\n");
    s.append("الجهة المصدرة: ").append(x.getIssuerX500Principal().getName()).append("\n");
    s.append("انتهاء الشهادة: ").append(x.getNotAfter()).append("\n");
   }
   s.append("رمز HTTP: ").append(c.getResponseCode()).append("\nنجاح HTTPS لا يعني أن الموقع غير احتيالي.");
   return s.toString();
  }finally{c.disconnect();}
 }
 private String latency(){
  long[] results=new long[5];int ok=0;StringBuilder s=new StringBuilder("زمن استجابة اتصال HTTPS إلى example.com\n");
  for(int i=0;i<5;i++){long start=System.nanoTime();HttpURLConnection c=null;try{
   c=(HttpURLConnection)new URL("https://example.com/").openConnection();c.setConnectTimeout(4500);c.setReadTimeout(4500);c.setInstanceFollowRedirects(false);c.getResponseCode();
   long ms=(System.nanoTime()-start)/1000000;results[ok++]=ms;s.append("محاولة ").append(i+1).append(": ").append(ms).append(" ms\n");
  }catch(Exception e){s.append("محاولة ").append(i+1).append(": فشلت\n");}finally{if(c!=null)c.disconnect();}}
  if(ok>0){long min=Long.MAX_VALUE,max=0,sum=0;for(int i=0;i<ok;i++){min=Math.min(min,results[i]);max=Math.max(max,results[i]);sum+=results[i];}
   s.append("المتوسط: ").append(sum/ok).append(" ms | التفاوت (max-min): ").append(max-min).append(" ms\n");
  }
  s.append("نجاح ").append(ok).append("/5. هذا زمن اتصال HTTP وليس اختبار ping أو قياس سرعة تنزيل/رفع أو فقد حزم ICMP.");
  return s.toString();
 }
 private String fingerprint(){
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);Network n=cm.getActiveNetwork();if(n==null)return "لا يوجد اتصال";
  LinkProperties p=cm.getLinkProperties(n);if(p==null)return "بيانات الشبكة غير متاحة";
  List<String> dns=new ArrayList<>();for(InetAddress a:p.getDnsServers())dns.add(a.getHostAddress());Collections.sort(dns);
  List<String> gateways=new ArrayList<>();for(RouteInfo route:p.getRoutes())if(route.isDefaultRoute()&&route.getGateway()!=null)gateways.add(route.getGateway().getHostAddress());Collections.sort(gateways);
  return "DNS: "+dns+"\nGateway: "+gateways+"\nPrivate DNS: "+p.isPrivateDnsActive()+"\nPrivate DNS host: "+p.getPrivateDnsServerName();
 }
 private String baseline(boolean save){
  String now=fingerprint();android.content.SharedPreferences p=getSharedPreferences("titan_network_baseline",MODE_PRIVATE);
  if(save){p.edit().putString("baseline",now).putLong("timestamp",System.currentTimeMillis()).apply();return "تم حفظ إعدادات الاتصال الحالية كخط أساس:\n"+now;}
  String previous=p.getString("baseline",null);
  if(previous==null)return "لم يتم حفظ خط أساس بعد. استخدم زر الحفظ أولًا.";
  return previous.equals(now)?"لم تتغير إعدادات DNS والبوابة المسجلة. هذا لا يثبت أمان الشبكة.":"تم رصد اختلاف في إعدادات الاتصال. قد يكون طبيعيًا بعد تغيير الشبكة أو استخدام VPN.\n\nالسابق:\n"+previous+"\n\nالحالي:\n"+now;
 }
}
