package com.titancorex.app;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

/** User-initiated transfer test; real bytes and monotonic clock, not estimated link rate. */
public class NetworkSpeedActivity extends Activity {
 private TextView result;private volatile boolean cancelled=false;
 private static final String BASE="https://speed.cloudflare.com";
 @Override public void onCreate(Bundle state){super.onCreate(state);
  ScrollView scroll=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(24,24,24,24);root.setBackgroundColor(0xff080D18);scroll.addView(root);setContentView(scroll);
  TextView title=new TextView(this);title.setText("TITAN SPEED & RELIABILITY LAB");title.setTextColor(0xff59E6EF);title.setTextSize(23);root.addView(title);
  result=new TextView(this);result.setTextColor(-1);result.setTextSize(16);root.addView(result);
  Button start=new Button(this);start.setText("قياس سرعة التنزيل والرفع");root.addView(start);start.setOnClickListener(v->runTest());
  Button stop=new Button(this);stop.setText("إيقاف القياس");root.addView(stop);stop.setOnClickListener(v->{cancelled=true;result.setText("تم طلب إيقاف الاختبار");});
  result.setText("اختبار نقل بيانات فعلي إلى Cloudflare. يستهلك قرابة 6 MB في كل تشغيل وقد يستهلك بيانات الهاتف. لا يختبر فقدان حزم ICMP.");
 }
 private void display(String message){runOnUiThread(()->result.setText(message));}
 private double download(int bytes)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL(BASE+"/__down?bytes="+bytes).openConnection();
  c.setConnectTimeout(7000);c.setReadTimeout(12000);c.setUseCaches(false);
  long start=System.nanoTime(),total=0;
  try{if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());
   try(InputStream in=c.getInputStream()){byte[] buffer=new byte[32768];int n;while(!cancelled&&(n=in.read(buffer))!=-1){total+=n;if(total>bytes+4096)throw new IOException("Unexpected response size");}}
  }finally{c.disconnect();}
  if(cancelled||total<bytes)throw new IOException("Incomplete download");
  return total*8.0/((System.nanoTime()-start)/1e9)/1e6;
 }
 private double upload(int bytes)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL(BASE+"/__up").openConnection();
  c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(7000);c.setReadTimeout(12000);
  c.setRequestProperty("Content-Type","application/octet-stream");c.setFixedLengthStreamingMode(bytes);
  byte[] buffer=new byte[16384];new java.security.SecureRandom().nextBytes(buffer);
  long start=System.nanoTime();
  try{try(OutputStream out=c.getOutputStream()){int sent=0;while(sent<bytes&&!cancelled){int n=Math.min(buffer.length,bytes-sent);out.write(buffer,0,n);sent+=n;}if(cancelled)throw new IOException("Cancelled");}
   int code=c.getResponseCode();if(code<200||code>=300)throw new IOException("Upload endpoint HTTP "+code);
   return bytes*8.0/((System.nanoTime()-start)/1e9)/1e6;
  }finally{c.disconnect();}
 }
 private void runTest(){cancelled=false;display("جارٍ نقل بيانات الاختبار...");
  new Thread(()->{try{
   double down=download(4*1024*1024);if(cancelled)return;display(String.format(Locale.US,"تنزيل: %.2f Mbps\nجارٍ اختبار الرفع...",down));
   double up=upload(2*1024*1024);if(cancelled)return;
   display(String.format(Locale.US,"تنزيل: %.2f Mbps\nرفع: %.2f Mbps\n\nالنتائج مرتبطة بخادم Cloudflare وحالة الشبكة وقت الاختبار، ولا تمثل السرعة القصوى المضمونة.",down,up));
  }catch(Exception e){display("فشل القياس أو الخادم غير متاح: "+e.getMessage());}}).start();
 }
}
