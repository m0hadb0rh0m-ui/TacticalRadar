package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/** Consent-based local discovery and network identity baseline; no intrusive port scanning. */
public class NetworkDiscoveryActivity extends Activity {
 private TextView report;private LinearLayout layout;
 private void button(String label,Runnable fn){Button b=new Button(this);b.setText(label);b.setAllCaps(false);layout.addView(b);b.setOnClickListener(v->fn.run());}
 @Override public void onCreate(Bundle state){super.onCreate(state);
  ScrollView sc=new ScrollView(this);layout=new LinearLayout(this);layout.setOrientation(1);layout.setPadding(24,24,24,24);layout.setBackgroundColor(0xff080D18);sc.addView(layout);setContentView(sc);
  report=new TextView(this);report.setTextColor(-1);report.setTextSize(15);layout.addView(report);
  button("اكتشاف أجهزة الشبكة المحلية (بموافقتك)",()->scan());
  button("حفظ بصمة شبكة Wi-Fi الحالية",()->baseline(true));
  button("مقارنة بصمة Wi-Fi الحالية",()->baseline(false));
  report.setText("الاكتشاف محدود بالشبكة المحلية وبما يسمح به Android. قد لا تستجيب الأجهزة؛ عدم ظهور جهاز لا يعني غيابه.");
 }
 private String networkId(){
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);Network n=cm.getActiveNetwork();if(n==null)return "غير متصل";
  NetworkCapabilities cap=cm.getNetworkCapabilities(n);if(cap==null||!cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))return "الاتصال الحالي ليس Wi-Fi";
  LinkProperties p=cm.getLinkProperties(n);if(p==null)return "بيانات الشبكة غير متاحة";
  ArrayList<String> gateways=new ArrayList<>(),dns=new ArrayList<>();
  for(RouteInfo route:p.getRoutes())if(route.isDefaultRoute()&&route.getGateway()!=null)gateways.add(route.getGateway().getHostAddress());
  for(InetAddress a:p.getDnsServers())dns.add(a.getHostAddress());
  Collections.sort(gateways);Collections.sort(dns);
  return "Gateways="+gateways+"; DNS="+dns+"; PrivateDNS="+p.isPrivateDnsActive()+"; Domain="+p.getPrivateDnsServerName();
 }
 private void baseline(boolean save){
  String id=networkId();android.content.SharedPreferences prefs=getSharedPreferences("titan_wifi_guardian",MODE_PRIVATE);
  if(save){prefs.edit().putString("network",id).apply();report.setText("تم حفظ بصمة الشبكة.\n"+id);return;}
  String old=prefs.getString("network",null);report.setText(old==null?"احفظ بصمة الشبكة أولًا":old.equals(id)?"لا تغيّر في بوابة الشبكة وDNS المسجلين. لا يمكن إثبات عدم وجود نقطة وصول مزيفة.":"تحذير: تغيّرت خصائص الاتصال. قد يكون تغييرًا طبيعيًا.\nالسابق: "+old+"\nالحالي: "+id);
 }
 private void scan(){
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);Network n=cm.getActiveNetwork();NetworkCapabilities cap=n==null?null:cm.getNetworkCapabilities(n);LinkProperties lp=n==null?null:cm.getLinkProperties(n);
  if(cap==null||!cap.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)||lp==null){report.setText("اتصل بشبكة Wi-Fi أولًا");return;}
  Inet4Address local=null;int prefix=0;
  for(LinkAddress la:lp.getLinkAddresses())if(la.getAddress() instanceof Inet4Address){local=(Inet4Address)la.getAddress();prefix=la.getPrefixLength();break;}
  if(local==null||prefix<24){report.setText("الاكتشاف متاح فقط لشبكة IPv4 محلية /24 أو أصغر؛ لا مسح لشبكات واسعة.");return;}
  byte[] ip=local.getAddress();final String stem=(ip[0]&255)+"."+(ip[1]&255)+"."+(ip[2]&255)+".";final String own=local.getHostAddress();
  report.setText("يجري اختبار الوصول للأجهزة على الشبكة المحلية...");new Thread(()->{
   ExecutorService pool=Executors.newFixedThreadPool(12);List<Future<String>> futures=new ArrayList<>();
   for(int i=1;i<255;i++){final String address=stem+i;if(address.equals(own))continue;
    futures.add(pool.submit(()->{try{InetAddress a=InetAddress.getByName(address);return a.isReachable(450)?address:null;}catch(Exception e){return null;}}));}
   pool.shutdown();ArrayList<String> found=new ArrayList<>();
   for(Future<String> f:futures)try{String v=f.get(12,TimeUnit.SECONDS);if(v!=null)found.add(v);}catch(Exception ignored){}
   Collections.sort(found);StringBuilder s=new StringBuilder("الأجهزة التي استجابت لاختبار الوصول: "+found.size()+"\n");
   for(String x:found)s.append("• ").append(x).append("\n");
   s.append("\nلا يمكن اعتبار القائمة حصرًا لكل الأجهزة. قد يحجب الراوتر أو جدار الحماية الاستجابة.");
   runOnUiThread(()->report.setText(s.toString()));
  }).start();
 }
}
