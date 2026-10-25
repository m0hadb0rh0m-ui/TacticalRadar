package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/**
 * Real device-wide network counters sampled in foreground.
 * No per-app attribution, packet interception, or geolocation is claimed.
 */
public final class V3NetworkMonitorActivity extends Activity {
 private static final int BG=0xff0B0F19,PANEL=0xff152334,INK=0xffECFCFF,
 CYAN=0xff06B6D4,GREEN=0xff10B981,MUTED=0xff9AB2C2,AMBER=0xffF59E0B;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private long previousRx=-1,previousTx=-1,previousTime=-1;
 private TextView traffic,connection;
 private TrafficChart chart;
 private boolean observing=false;
 private int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int size,int color){
  TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);
  t.setPadding(dp(5),dp(8),dp(5),dp(8));return t;
 }
 private GradientDrawable surface(){
  GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
   new int[]{0xff182C3F,0xff111B2B});d.setCornerRadius(dp(24));d.setStroke(dp(1),0xff265066);return d;
 }
 private void button(LinearLayout parent,String label,Class<?> target){
  Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextColor(INK);
  GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
   new int[]{0xff15687B,0xff184C60});d.setCornerRadius(dp(16));b.setBackground(d);
  LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52));p.topMargin=dp(8);
  parent.addView(b,p);b.setOnClickListener(v->startActivity(new Intent(this,target)));
 }
 private final Runnable ticker=new Runnable(){@Override public void run(){
  if(!observing)return;refreshSample();handler.postDelayed(this,1000);
 }};
 @Override public void onCreate(Bundle state){super.onCreate(state);
  getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView scroll=new ScrollView(this);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);
  root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  root.setPadding(dp(18),dp(22),dp(18),dp(26));root.setBackgroundColor(BG);
  scroll.addView(root);setContentView(scroll);
  root.addView(text("◎ TITAN LIVE NETWORK",24,INK));
  root.addView(text("مراقبة حركة الشبكة الفعلية — على مستوى الجهاز",13,CYAN));
  LinearLayout panel=new LinearLayout(this);panel.setOrientation(1);
  panel.setPadding(dp(17),dp(17),dp(17),dp(17));panel.setBackground(surface());
  LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,-2);pp.topMargin=dp(14);
  root.addView(panel,pp);
  traffic=text("تنزيل: بانتظار العينة التالية\nرفع: بانتظار العينة التالية",20,GREEN);
  panel.addView(traffic);
  chart=new TrafficChart();panel.addView(chart,new LinearLayout.LayoutParams(-1,dp(230)));
  panel.addView(text("■ السماوي: تنزيل    ■ الأخضر: رفع    |    آخر 60 ثانية تقريبًا",12,MUTED));
  LinearLayout network=new LinearLayout(this);network.setOrientation(1);
  network.setPadding(dp(15),dp(15),dp(15),dp(15));network.setBackground(surface());
  LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.topMargin=dp(13);
  root.addView(network,np);
  network.addView(text("حالة الاتصال الحالي",18,INK));
  connection=text("",14,MUTED);network.addView(connection);
  root.addView(text("مختبر أدوات الشبكة",20,INK));
  button(root,"درع DNS والتحكم في الحجب",DnsFirewallActivity.class);
  button(root,"قياس سرعة الإنترنت",NetworkSpeedActivity.class);
  button(root,"الكمون وزمن استجابة HTTPS",NetworkInsightActivity.class);
  button(root,"فحص إعدادات Wi-Fi",WifiAuditActivity.class);
  root.addView(text("السرعات مشتقة من عدّادات Android الإجمالية عند إتاحتها. لا يحدد هذا الإصدار استهلاك كل تطبيق أو موقع خوادمه، ولا يمنع اتصالات التطبيقات من هذه الشاشة.",12,AMBER));
 }
 private static String speed(double bytesPerSec){
  if(bytesPerSec<0||Double.isNaN(bytesPerSec))return "غير متاح";
  return String.format(Locale.US,"%.2f Mbps",bytesPerSec*8/1000000.0);
 }
 private void refreshSample(){
  ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
  Network n=cm==null?null:cm.getActiveNetwork();
  NetworkCapabilities caps=n==null?null:cm.getNetworkCapabilities(n);
  String transport=caps==null?"غير متصل":caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"Wi-Fi":
    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)?"بيانات الهاتف":"شبكة أخرى";
  boolean validated=caps!=null&&caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
  boolean metered=cm!=null&&cm.isActiveNetworkMetered();
  connection.setText("الشبكة: "+transport+"\nوصول الإنترنت: "+(validated?"مؤكد عبر Android":"غير مؤكد")+
    "\nاتصال محدود البيانات: "+(metered?"نعم":"لا"));
  long rx=TrafficStats.getTotalRxBytes(),tx=TrafficStats.getTotalTxBytes();
  long now=SystemClock.elapsedRealtime();
  if(previousTime>0&&rx>=0&&tx>=0&&rx>=previousRx&&tx>=previousTx&&now>previousTime){
   double seconds=(now-previousTime)/1000.0;
   double down=(rx-previousRx)/seconds,up=(tx-previousTx)/seconds;
   traffic.setText("↓ تنزيل: "+speed(down)+"\n↑ رفع: "+speed(up));
   chart.add(down*8/1000000.0,up*8/1000000.0);
  }else traffic.setText("جارٍ جمع عينات نقل بيانات الهاتف...");
  previousRx=rx;previousTx=tx;previousTime=now;
 }
 @Override protected void onResume(){super.onResume();previousTime=-1;observing=true;handler.removeCallbacks(ticker);handler.post(ticker);}
 @Override protected void onPause(){observing=false;handler.removeCallbacks(ticker);super.onPause();}
 private final class TrafficChart extends View {
  private final ArrayList<Double> rx=new ArrayList<>(),tx=new ArrayList<>();
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  TrafficChart(){super(V3NetworkMonitorActivity.this);setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);}
  void add(double a,double b){if(rx.size()==60){rx.remove(0);tx.remove(0);}
   rx.add(Math.max(0,a));tx.add(Math.max(0,b));invalidate();}
  private void trace(Canvas c,ArrayList<Double> samples,float top,float bottom,float left,float right,double max,int color){
   if(samples.size()<2)return;
   Path path=new Path();
   for(int i=0;i<samples.size();i++){
    float x=left+(right-left)*i/59f;
    float y=bottom-(bottom-top)*(float)(samples.get(i)/max);
    if(i==0)path.moveTo(x,y);else path.lineTo(x,y);
   }
   p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2.6f));
   p.setStrokeJoin(Paint.Join.ROUND);c.drawPath(path,p);
  }
  @Override protected void onDraw(Canvas c){super.onDraw(c);
   float w=getWidth(),h=getHeight(),l=dp(12),r=w-dp(12),top=dp(22),bottom=h-dp(28);
   p.setStyle(Paint.Style.FILL);p.setColor(0xff101D2C);c.drawRoundRect(0,0,w,h,dp(15),dp(15),p);
   p.setStyle(Paint.Style.STROKE);p.setColor(0xff294153);p.setStrokeWidth(dp(1));
   for(int i=1;i<=3;i++){float y=top+(bottom-top)*i/4;c.drawLine(l,y,r,y,p);}
   double max=.5;for(double v:rx)max=Math.max(max,v);for(double v:tx)max=Math.max(max,v);
   trace(c,rx,top,bottom,l,r,max,CYAN);trace(c,tx,top,bottom,l,r,max,GREEN);
   p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.LEFT);p.setTextSize(dp(11));p.setColor(MUTED);
   c.drawText("الذروة "+String.format(Locale.US,"%.2f Mbps",max),l,dp(15),p);
  }
 }
}
