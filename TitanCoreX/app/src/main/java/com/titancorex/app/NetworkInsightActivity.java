package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/** V3 network diagnostics: Android traffic counters + real HTTPS round-trip sampling. */
public final class NetworkInsightActivity extends Activity {
 private static final int BG=0xff0B0F19,INK=0xffEDF9FF,BLUE=0xff06B6D4,MUTED=0xffA0B3C5,TEAL=0xff10B981;
 private LinearLayout root;private TextView summary,result,history;private Button start,stop;
 private LatencyGraph graph;
 private volatile boolean cancel=false,testing=false;
 private volatile HttpURLConnection active;
 private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int sz,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(sz);t.setTextColor(color);t.setPadding(dp(3),dp(5),dp(3),dp(5));return t;}
 private GradientDrawable bg(int a,int b,int stroke){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});d.setCornerRadius(dp(22));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
 private LinearLayout box(String header){
  LinearLayout l=new LinearLayout(this);l.setOrientation(1);l.setPadding(dp(16),dp(15),dp(16),dp(15));
  l.setBackground(bg(0xff18283A,0xff111D2D,0xff315165));l.addView(text(header,19,INK));
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(13),0,dp(2));root.addView(l,p);return l;
 }
 private String bytes(long n){if(n==TrafficStats.UNSUPPORTED||n<0)return "غير متاح";
  if(n<1048576)return String.format(Locale.US,"%.1f KB",n/1024.0);
  if(n<1073741824L)return String.format(Locale.US,"%.2f MB",n/1048576.0);
  return String.format(Locale.US,"%.2f GB",n/1073741824.0);
 }
 @Override public void onCreate(Bundle saved){
  super.onCreate(saved);
  getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  getWindow().getDecorView().setSystemUiVisibility(0);
  ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);
  root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(17),dp(19),dp(17),dp(25));
  root.setBackgroundColor(BG);scroll.addView(root);setContentView(scroll);
  root.addView(text("◎  TITAN NETWORK INSIGHTS",24,BLUE));
  root.addView(text("V3 • قياسات الشبكة واستهلاك البيانات",15,INK));
  LinearLayout traffic=box("حالة الشبكة واستهلاك البيانات");
  summary=text("",14,MUTED);traffic.addView(summary);
  Button refresh=new Button(this);refresh.setText("تحديث عدّادات Android");refresh.setAllCaps(false);
  refresh.setTextColor(0xffffffff);refresh.setBackground(bg(0xff129DE0,0xff3764F5,0));
  traffic.addView(refresh);refresh.setOnClickListener(v->loadNetwork());
  LinearLayout latency=box("HTTPS LATENCY LAB");
  latency.addView(text("خمس محاولات اتصال HTTPS فعلية. القياس يتضمن زمن إنشاء الاتصال والخادم، وليس ICMP Ping.",12,MUTED));
  graph=new LatencyGraph();latency.addView(graph,new LinearLayout.LayoutParams(-1,dp(180)));
  result=text("جاهز لاختبار زمن الاستجابة",15,INK);latency.addView(result);
  start=new Button(this);start.setText("بدء اختبار الاستجابة");start.setAllCaps(false);
  start.setTextColor(-1);start.setBackground(bg(0xff098DF6,0xff5C60EC,0));latency.addView(start);
  start.setOnClickListener(v->begin());
  stop=new Button(this);stop.setText("إيقاف الاختبار");stop.setAllCaps(false);stop.setEnabled(false);
  stop.setTextColor(-1);stop.setBackground(bg(0xffFF9363,0xffDA4C83,0));latency.addView(stop);
  stop.setOnClickListener(v->halt());
  LinearLayout saved=box("سجل النتائج المحلي");
  history=text("",13,MUTED);saved.addView(history);
  Button clear=new Button(this);clear.setText("مسح السجل");clear.setAllCaps(false);saved.addView(clear);
  clear.setOnClickListener(v->new AlertDialog.Builder(this).setMessage("هل تريد حذف سجل الاستجابة المحلي؟")
   .setNegativeButton("إلغاء",null).setPositiveButton("حذف",(d,w)->{
    getSharedPreferences("titan_network_insights_v3",0).edit().clear().apply();loadHistory();
   }).show());
  root.addView(text("العدادات منذ آخر إعادة تشغيل للهاتف، وقد لا تكون متاحة على بعض الأجهزة. لا تمثل فاتورة مشغل الشبكة ولا بيانات التطبيقات الفردية.",12,MUTED));
  loadNetwork();loadHistory();
 }
 private void loadNetwork(){
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
  Network network=cm==null?null:cm.getActiveNetwork();
  NetworkCapabilities caps=network==null?null:cm.getNetworkCapabilities(network);
  String transport=caps==null?"غير متصل":caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":
    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"بيانات خلوية":"اتصال آخر";
  boolean validated=caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
  long rx=TrafficStats.getTotalRxBytes(),tx=TrafficStats.getTotalTxBytes();
  long mobileRx=TrafficStats.getMobileRxBytes(),mobileTx=TrafficStats.getMobileTxBytes();
  summary.setText("نوع الاتصال: "+transport+"\nتأكيد الإنترنت من Android: "+(validated?"مؤكد":"غير مؤكد")
    +"\nالبيانات المستقبلة من الجهاز: "+bytes(rx)+"\nالبيانات المرسلة من الجهاز: "+bytes(tx)
    +"\nالبيانات الخلوية المستقبلة: "+bytes(mobileRx)+"\nالبيانات الخلوية المرسلة: "+bytes(mobileTx));
 }
 private void setBusy(boolean value){testing=value;start.setEnabled(!value);stop.setEnabled(value);}
 private void halt(){cancel=true;HttpURLConnection c=active;if(c!=null)c.disconnect();result.setText("تم طلب إيقاف الاختبار");}
 private long sample() throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL("https://speed.cloudflare.com/cdn-cgi/trace").openConnection();
  active=c;c.setRequestMethod("GET");c.setConnectTimeout(4500);c.setReadTimeout(4500);
  c.setUseCaches(false);c.setRequestProperty("Cache-Control","no-cache");
  long begin=SystemClock.elapsedRealtimeNanos();
  try{
   int status=c.getResponseCode();if(status<200||status>=400)throw new IOException("HTTP "+status);
   try(InputStream in=c.getInputStream()){byte[] buf=new byte[128];in.read(buf);}
   return Math.max(1,(SystemClock.elapsedRealtimeNanos()-begin)/1000000L);
  }finally{c.disconnect();if(active==c)active=null;}
 }
 private void begin(){
  if(testing)return;cancel=false;setBusy(true);graph.setValues(new ArrayList<Long>());
  result.setText("جارٍ قياس استجابة HTTPS ...");
  new Thread(()->{
   ArrayList<Long> vals=new ArrayList<>();String error=null;
   for(int i=0;i<5&&!cancel;i++){
    try{vals.add(sample());}catch(Exception e){if(!cancel)error=e.getMessage();break;}
    final ArrayList<Long> copy=new ArrayList<>(vals);
    runOnUiThread(()->{if(!isFinishing()&&!isDestroyed()){
     graph.setValues(copy);result.setText("تم قياس "+copy.size()+" من 5 استجابات");
    }});
   }
   final String failure=error;final ArrayList<Long> finalVals=vals;
   runOnUiThread(()->{
    if(isFinishing()||isDestroyed())return;
    setBusy(false);
    if(cancel){result.setText("أُلغي الاختبار؛ لا تُحفظ نتيجة جزئية");return;}
    if(failure!=null){result.setText("تعذر الاتصال: "+failure);return;}
    if(finalVals.size()!=5){result.setText("لم يكتمل الاختبار");return;}
    long sum=0;long min=Long.MAX_VALUE,max=0;double change=0;
    for(int i=0;i<finalVals.size();i++){long n=finalVals.get(i);sum+=n;min=Math.min(min,n);max=Math.max(max,n);
     if(i>0)change+=Math.abs(n-finalVals.get(i-1));}
    double avg=sum/5.0,jitter=change/4.0;
    String report=String.format(Locale.US,"متوسط HTTPS: %.0f ms | التذبذب: %.0f ms | الأقل: %d ms | الأعلى: %d ms",
     avg,jitter,min,max);
    result.setText(report+"\nالتذبذب هنا متوسط الفروق المطلقة، وليس Jitter حزم UDP.");
    saveHistory(report);
   });
  },"TitanHttpsLatency").start();
 }
 private void saveHistory(String report){
  String time=new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.getDefault()).format(new Date());
  String old=getSharedPreferences("titan_network_insights_v3",0).getString("rows","");
  String[] rows=old.split("\n");StringBuilder next=new StringBuilder(time+" — "+report);
  for(int i=0;i<Math.min(11,rows.length);i++)if(!rows[i].isEmpty())next.append('\n').append(rows[i]);
  getSharedPreferences("titan_network_insights_v3",0).edit().putString("rows",next.toString()).apply();loadHistory();
 }
 private void loadHistory(){String h=getSharedPreferences("titan_network_insights_v3",0).getString("rows","");
  history.setText(h.isEmpty()?"لم تسجّل نتائج بعد":h);
 }
 @Override protected void onDestroy(){halt();super.onDestroy();}
 private final class LatencyGraph extends View{
  private final Paint p=new Paint(3);private ArrayList<Long> values=new ArrayList<>();
  LatencyGraph(){super(NetworkInsightActivity.this);}
  void setValues(ArrayList<Long> v){values=v;invalidate();}
  @Override protected void onDraw(Canvas c){
   super.onDraw(c);float w=getWidth(),h=getHeight(),left=dp(18),top=dp(18),right=w-dp(18),bottom=h-dp(20);
   p.setStyle(Paint.Style.FILL);p.setColor(0xff142536);c.drawRoundRect(0,0,w,h,dp(18),dp(18),p);
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(0xff30475A);
   for(int k=1;k<4;k++){float y=top+(bottom-top)*k/4;c.drawLine(left,y,right,y,p);}
   if(values.size()<1){p.setStyle(Paint.Style.FILL);p.setColor(MUTED);p.setTextSize(dp(14));p.setTextAlign(Paint.Align.CENTER);
    c.drawText("تظهر القراءات الحقيقية هنا",w/2,h/2,p);return;}
   long mn=Collections.min(values),mx=Collections.max(values);if(mx==mn)mx=mn+10;
   Path line=new Path();
   for(int i=0;i<values.size();i++){
    float x=left+(right-left)*i/4f;
    float y=bottom-(bottom-top)*(values.get(i)-mn)/(float)(mx-mn);
    if(i==0)line.moveTo(x,y);else line.lineTo(x,y);
   }
   p.setStrokeWidth(dp(3));p.setColor(BLUE);p.setStyle(Paint.Style.STROKE);c.drawPath(line,p);
   p.setStyle(Paint.Style.FILL);p.setColor(TEAL);
   for(int i=0;i<values.size();i++){
    float x=left+(right-left)*i/4f;
    float y=bottom-(bottom-top)*(values.get(i)-mn)/(float)(mx-mn);
    c.drawCircle(x,y,dp(4),p);
   }
  }
 }
}
