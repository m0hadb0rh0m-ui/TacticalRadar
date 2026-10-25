package com.titancorex.app;

import android.app.*;
import android.content.*;
import java.text.*;
import android.os.*;
import android.view.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

/** User-initiated transfer test; real bytes and monotonic clock, not estimated link rate. */
public class NetworkSpeedActivity extends Activity {
 private TextView result;private volatile boolean cancelled=false;
 private volatile boolean running=false;
 private SpeedVisual visual;private TextView visualLabel,history;private Button startButton,stopButton;private String lastReport="";
 private static final String BASE="https://speed.cloudflare.com";
 @Override public void onCreate(Bundle state){super.onCreate(state);
  ScrollView scroll=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(24,24,24,24);root.setBackgroundColor(0xffF1F8FF);scroll.addView(root);setContentView(scroll);
  TextView title=new TextView(this);title.setText("TITAN SPEED & RELIABILITY LAB");title.setTextColor(0xff087BEA);title.setTextSize(23);root.addView(title);
  visual=new SpeedVisual();root.addView(visual,new LinearLayout.LayoutParams(-1,260));
  visualLabel=new TextView(this);visualLabel.setText("جاهز للفحص");visualLabel.setTextColor(0xff087BEA);visualLabel.setTextSize(18);visualLabel.setGravity(Gravity.CENTER);root.addView(visualLabel);
  result=new TextView(this);result.setTextColor(0xff183453);result.setTextSize(16);root.addView(result);
  Button start=new Button(this);start.setText("ϟ  بدء قياس سرعة التنزيل والرفع");start.setAllCaps(false);start.setTextColor(0xffffffff);start.setBackground(buttonBackground(0xff078DFF,0xff535FF0));root.addView(start);startButton=start;start.setOnClickListener(v->runTest());
  Button stop=new Button(this);stop.setText("■  إيقاف القياس");stop.setAllCaps(false);stop.setTextColor(0xffffffff);stop.setBackground(buttonBackground(0xffFF9860,0xffE45480));root.addView(stop);stopButton=stop;stop.setEnabled(false);stop.setOnClickListener(v->{cancelled=true;result.setText("تم طلب إيقاف الاختبار");visual.stop();});
  Button share=new Button(this);share.setText("مشاركة آخر تقرير");share.setAllCaps(false);share.setTextColor(0xffffffff);share.setBackground(buttonBackground(0xff0BAA9B,0xff0576BA));root.addView(share);share.setOnClickListener(v->shareReport());
  TextView historyTitle=new TextView(this);historyTitle.setText("سجل آخر الاختبارات");historyTitle.setTextColor(0xff173B67);historyTitle.setTextSize(19);historyTitle.setPadding(0,24,0,8);root.addView(historyTitle);
  history=new TextView(this);history.setTextColor(0xff385C80);history.setTextSize(14);history.setPadding(16,16,16,16);history.setBackground(buttonBackground(0xffffffff,0xffE3F2FF));root.addView(history);refreshHistory();
  result.setText("اختبار نقل بيانات فعلي إلى Cloudflare. يستهلك قرابة 6 MB في كل تشغيل وقد يستهلك بيانات الهاتف. لا يختبر فقدان حزم ICMP.");
 }
 private void saveReport(double down,double up){
  String stamp=new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date());
  lastReport=String.format(Locale.US,"%s | Download %.2f Mbps | Upload %.2f Mbps",stamp,down,up);
  android.content.SharedPreferences prefs=getSharedPreferences("titan_speed_history",MODE_PRIVATE);
  String old=prefs.getString("entries","");String[] rows=old.split("\\n");StringBuilder sb=new StringBuilder(lastReport);
  for(int i=0;i<Math.min(9,rows.length);i++)if(!rows[i].isEmpty())sb.append("\\n").append(rows[i]);
  prefs.edit().putString("entries",sb.toString()).apply();runOnUiThread(this::refreshHistory);
 }
 private void refreshHistory(){if(history!=null){String saved=getSharedPreferences("titan_speed_history",MODE_PRIVATE).getString("entries","");history.setText(saved.isEmpty()?"لا توجد اختبارات محفوظة بعد":saved);}}
 private void shareReport(){
  String text=lastReport.isEmpty()?getSharedPreferences("titan_speed_history",MODE_PRIVATE).getString("entries","").split("\\n")[0]:lastReport;
  if(text.isEmpty()){Toast.makeText(this,"أجرِ اختبارًا أولًا",Toast.LENGTH_SHORT).show();return;}
  Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,"TITAN CORE X — Network speed\\n"+text);
  startActivity(Intent.createChooser(send,"مشاركة تقرير السرعة"));
 }
 private GradientDrawable buttonBackground(int a,int b){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(22);return d;}
 private void display(String message){runOnUiThread(()->{result.setText(message);if(visualLabel!=null)visualLabel.setText(message.split("\\n")[0]);});}
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
 private void runTest(){if(running)return;running=true;cancelled=false;startButton.setEnabled(false);stopButton.setEnabled(true);visual.start();display("جارٍ نقل بيانات الاختبار...");
  new Thread(()->{try{
   double down=download(4*1024*1024);if(cancelled)return;display(String.format(Locale.US,"تنزيل: %.2f Mbps\nجارٍ اختبار الرفع...",down));
   double up=upload(2*1024*1024);if(cancelled)return;
   saveReport(down,up);display(String.format(Locale.US,"تنزيل: %.2f Mbps\nرفع: %.2f Mbps\n\nالنتائج مرتبطة بخادم Cloudflare وحالة الشبكة وقت الاختبار، ولا تمثل السرعة القصوى المضمونة.",down,up));
  }catch(Exception e){display("فشل القياس أو الخادم غير متاح: "+e.getMessage());}finally{runOnUiThread(()->{visual.stop();running=false;startButton.setEnabled(true);stopButton.setEnabled(false);});}}).start();
 }
 private final class SpeedVisual extends View {
  private final Paint paint=new Paint(3);
  private float angle=0;private boolean active=false;
  private final Runnable frame=new Runnable(){public void run(){if(!active)return;angle=(angle+3)%360;invalidate();postDelayed(this,35);}};
  SpeedVisual(){super(NetworkSpeedActivity.this);}
  void start(){active=true;removeCallbacks(frame);post(frame);}
  void stop(){active=false;removeCallbacks(frame);invalidate();}
  @Override protected void onDraw(Canvas canvas){
   super.onDraw(canvas);float cx=getWidth()/2f,cy=getHeight()/2f;
   float radius=Math.min(getWidth(),getHeight())*.36f;
   paint.setStyle(Paint.Style.FILL);paint.setColor(0xffE0F3FF);canvas.drawCircle(cx,cy,radius*1.17f,paint);
   paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(15);paint.setStrokeCap(Paint.Cap.ROUND);
   paint.setColor(0xffC4DCF7);canvas.drawArc(cx-radius,cy-radius,cx+radius,cy+radius,135,270,false,paint);
   paint.setColor(0xff138BF4);canvas.drawArc(cx-radius,cy-radius,cx+radius,cy+radius,135,active?angle*.75f:175,false,paint);
   paint.setStrokeWidth(5);paint.setColor(0xff02BBD4);
   canvas.drawArc(cx-radius*.73f,cy-radius*.73f,cx+radius*.73f,cy+radius*.73f,active?angle:30,65,false,paint);
   paint.setStyle(Paint.Style.FILL);paint.setColor(0xff193B66);
   paint.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));paint.setTextSize(43);
   paint.setTextAlign(Paint.Align.CENTER);canvas.drawText("ϟ",cx,cy+10,paint);
   paint.setTextSize(15);canvas.drawText("TITAN SPEED",cx,cy+38,paint);
  }
 }
 @Override protected void onDestroy(){if(visual!=null)visual.stop();cancelled=true;super.onDestroy();}
}

