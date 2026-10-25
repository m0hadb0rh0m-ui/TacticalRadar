package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.net.*;
import java.util.*;

/** Read-only network diagnostics for the current Wi-Fi connection. */
public class WifiAuditActivity extends Activity {
 private LinearLayout root;private TextView result;
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private void button(String name,Runnable action){Button b=new Button(this);b.setText(name);b.setAllCaps(false);root.addView(b);b.setOnClickListener(v->action.run());}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setStatusBarColor(0xff080D18);
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(22),dp(18),dp(22));root.setBackgroundColor(0xff080D18);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);sc.addView(root);setContentView(sc);
  TextView title=new TextView(this);title.setText("TITAN WIFI SECURITY LAB");title.setTextColor(0xff58e4ef);title.setTextSize(23);root.addView(title);
  result=new TextView(this);result.setTextColor(0xfff2f7ff);result.setTextSize(15);result.setPadding(0,dp(18),0,dp(18));root.addView(result);
  button("فحص اتصال Wi-Fi الحالي",this::refresh);
  button("إعدادات Wi-Fi",()->startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
  button("إعدادات DNS الخاص",()->{try{startActivity(new Intent("android.settings.PRIVATE_DNS_SETTINGS"));}catch(Exception e){startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));}});
  refresh();
 }
 private void refresh(){
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
  Network network=cm.getActiveNetwork();NetworkCapabilities caps=network==null?null:cm.getNetworkCapabilities(network);
  LinkProperties link=network==null?null:cm.getLinkProperties(network);
  StringBuilder s=new StringBuilder();
  boolean wifi=caps!=null&&caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
  s.append("نوع الاتصال: ").append(wifi?"Wi-Fi":caps==null?"غير متصل":"ليس Wi-Fi").append("\n");
  if(caps!=null){
   s.append("وصول متحقق للإنترنت: ").append(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)?"نعم":"غير متحقق").append("\n");
   s.append("الشبكة تُصنّف غير مدفوعة: ").append(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)?"نعم":"لا/غير معروف").append("\n");
   s.append("اتصال VPN نشط على المسار: ").append(caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"نعم":"لا يظهر في الاتصال النشط").append("\n");
   if(Build.VERSION.SDK_INT>=29&&caps.getTransportInfo() instanceof WifiInfo){
    WifiInfo info=(WifiInfo)caps.getTransportInfo();
    s.append("اسم الشبكة: ").append(info.getSSID()).append("\n");
    s.append("قوة الإشارة RSSI: ").append(info.getRssi()).append(" dBm\n");
    s.append("سرعة الرابط: ").append(info.getLinkSpeed()).append(" Mbps (ليست سرعة الإنترنت)\n");
    s.append("التردد: ").append(info.getFrequency()).append(" MHz\n");
    if(Build.VERSION.SDK_INT>=31)s.append("نوع حماية Wi-Fi: ").append(security(info.getCurrentSecurityType())).append("\n");
   }
  }
  if(link!=null){
   s.append("\nDNS المعلن من الشبكة:\n");for(InetAddress dns:link.getDnsServers())s.append("• ").append(dns.getHostAddress()).append("\n");
   s.append("Private DNS: ").append(link.isPrivateDnsActive()?"نشط":"غير نشط/غير معروف").append("\n");
   if(link.getPrivateDnsServerName()!=null)s.append("اسم خادم Private DNS: ").append(link.getPrivateDnsServerName()).append("\n");
   s.append("بوابة الشبكة: ");
   boolean found=false;for(RouteInfo r:link.getRoutes())if(r.isDefaultRoute()&&r.getGateway()!=null){s.append(r.getGateway().getHostAddress()).append(" ");found=true;}
   if(!found)s.append("غير متاحة");
   s.append("\n");
  }
  s.append("\nتقييم الحماية:\n");
  if(!wifi)s.append("• اتصل بشبكة Wi-Fi لإظهار تفاصيلها.\n");
  else s.append("• افحص نوع تشفير الشبكة. WPA3 أو WPA2 أقوى عادةً من الشبكات المفتوحة أو WEP.\n");
  if(link!=null&&!link.isPrivateDnsActive())s.append("• Private DNS غير نشط على هذا المسار؛ يمكن مراجعة الإعدادات.\n");
  s.append("\nتنبيه: لا يمكن لهذا الفحص إثبات خلو الراوتر من الاختراق أو كشف جميع الأجهزة المتصلة. لم تُجرَ محاولات اختراق أو مسح للأجهزة الأخرى.");
  result.setText(s.toString());
 }
 private String security(int type){
  switch(type){
   case WifiInfo.SECURITY_TYPE_OPEN:return "OPEN — شبكة مفتوحة";
   case WifiInfo.SECURITY_TYPE_WEP:return "WEP — ضعيف";
   case WifiInfo.SECURITY_TYPE_PSK:return "WPA/WPA2-Personal";
   case WifiInfo.SECURITY_TYPE_SAE:return "WPA3-Personal";
   case WifiInfo.SECURITY_TYPE_EAP:return "Enterprise (EAP)";
   case WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE:return "WPA3-Enterprise";
   case WifiInfo.SECURITY_TYPE_OWE:return "Enhanced Open (OWE)";
   default:return "نوع آخر/غير معروف ("+type+")";
  }
 }
}
