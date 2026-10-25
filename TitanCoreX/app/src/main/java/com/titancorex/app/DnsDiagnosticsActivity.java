package com.titancorex.app;

import android.app.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

/** DNS route inspection + independent HTTPS resolver comparison; not a complete leak test. */
public class DnsDiagnosticsActivity extends Activity {
 private TextView output;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(24,24,24,24);root.setBackgroundColor(0xffF1F8FF);setContentView(root);
  TextView title=new TextView(this);title.setText("TITAN DNS PRIVACY LAB");title.setTextSize(23);title.setTextColor(0xff087BEA);root.addView(title);
  Button check=new Button(this);check.setText("فحص DNS ومقارنة محلل HTTPS");root.addView(check);
  output=new TextView(this);output.setTextColor(0xff173557);output.setTextSize(15);root.addView(output);
  check.setOnClickListener(v->runCheck());
  output.setText("يعرض خوادم DNS التي أعلنها Android ويختبر الوصول إلى محلل DNS عبر HTTPS. لا يثبت غياب تسرب DNS؛ يتطلب الاختبار الكامل نقطة قياس خارجية تتحكم بها.");
 }
 private void runCheck(){output.setText("جارٍ الفحص...");
  new Thread(()->{String result;
   try{
    ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);Network n=cm.getActiveNetwork();LinkProperties p=n==null?null:cm.getLinkProperties(n);
    StringBuilder s=new StringBuilder("خوادم DNS المعلنة:\n");
    if(p!=null){for(InetAddress a:p.getDnsServers())s.append("• ").append(a.getHostAddress()).append("\n");
     s.append("Private DNS: ").append(p.isPrivateDnsActive()).append("\n");
     s.append("Private DNS hostname: ").append(p.getPrivateDnsServerName()).append("\n");
    }else s.append("غير متاحة\n");
    URL url=new URL("https://cloudflare-dns.com/dns-query?name=example.com&type=A");
    HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setRequestProperty("Accept","application/dns-json");c.setConnectTimeout(6000);c.setReadTimeout(6000);
    try{s.append("استجابة DNS-over-HTTPS: HTTP ").append(c.getResponseCode()).append("\n");}finally{c.disconnect();}
    s.append("\nلا يمكن اعتبار هذا اختبار تسريب DNS قاطعًا؛ التطبيقات قد تستخدم محللات مختلفة.");
    result=s.toString();
   }catch(Exception e){result="تعذر الفحص: "+e.getMessage();}
   final String text=result;runOnUiThread(()->output.setText(text));
  }).start();
 }
}
