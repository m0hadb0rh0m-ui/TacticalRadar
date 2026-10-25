package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

public class ApkInspectorActivity extends Activity {
 private static final int PICK=7041,BG=0xff080D18,WHITE=0xffF4F8FF,CYAN=0xff58E4EF,MUTED=0xffB0C1D3,AMBER=0xffFFCE7A;
 private LinearLayout root;private TextView status;private Button pick;
 private int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setTextSize(size);t.setPadding(dp(5),dp(8),dp(5),dp(8));return t;}
 private void result(String label,String value){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(13),dp(10),dp(13),dp(10));GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff1B3049,0xff101D30});d.setCornerRadius(dp(16));c.setBackground(d);c.addView(text(label,13,CYAN));c.addView(text(value,14,WHITE));root.addView(c);View gap=new View(this);root.addView(gap,new LinearLayout.LayoutParams(1,dp(9)));}
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(18),dp(22),dp(18),dp(22));root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
  root.addView(text("TITAN APK INSPECTOR",25,CYAN));root.addView(text("تحليل ملف APK تختاره أنت. لا يرفع الملف إلى الإنترنت ولا يثبت التطبيق.",14,MUTED));
  pick=new Button(this);pick.setText("اختيار ملف APK للفحص");root.addView(pick);pick.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,PICK);});
  status=text("النتائج تحليل بنيوي وبصمة SHA-256، وليست حكمًا بأن الملف آمن أو خبيث.",13,AMBER);root.addView(status);
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=PICK||result!=RESULT_OK||data==null||data.getData()==null)return;
  Uri uri=data.getData();pick.setEnabled(false);status.setText("جارٍ قراءة الملف وتحليله...");
  new Thread(()->{String report;try{report=inspect(uri);}catch(Exception e){report="تعذر الفحص: "+e.getClass().getSimpleName()+" — "+e.getMessage();}final String r=report;runOnUiThread(()->{pick.setEnabled(true);status.setText(r);});}).start();
 }
 private String inspect(Uri uri)throws Exception{
  File temp=File.createTempFile("titan-inspect-", ".apk",getCacheDir());long bytes=0;MessageDigest digest=MessageDigest.getInstance("SHA-256");
  try{
   try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(temp)){if(in==null)throw new IOException("تعذر فتح الملف");byte[] buf=new byte[32768];int n;while((n=in.read(buf))!=-1){bytes+=n;if(bytes>250L*1024*1024)throw new IOException("حجم الملف يتجاوز حد الفحص 250MB");digest.update(buf,0,n);out.write(buf,0,n);}}
   StringBuilder hex=new StringBuilder();for(byte b:digest.digest())hex.append(String.format(Locale.US,"%02x",b&255));
   int entries=0,dex=0,nativeFiles=0,duplicates=0,zipAnomalies=0;Set<String> names=new HashSet<>();boolean manifest=false,signature=false;
   try(ZipFile zip=new ZipFile(temp)){Enumeration<? extends ZipEntry> e=zip.entries();while(e.hasMoreElements()){ZipEntry z=e.nextElement();entries++;if(entries>40000)throw new IOException("عدد مكونات الأرشيف كبير جدًا");String name=z.getName();if(!names.add(name))duplicates++;if(z.getSize()>100L*1024*1024&&z.getCompressedSize()>0&&z.getSize()/Math.max(1,z.getCompressedSize())>200)zipAnomalies++;if(name.matches("classes[0-9]*\\.dex"))dex++;if(name.startsWith("lib/")&&name.endsWith(".so"))nativeFiles++;if(name.equals("AndroidManifest.xml"))manifest=true;if(name.startsWith("META-INF/")&&(name.endsWith(".RSA")||name.endsWith(".DSA")||name.endsWith(".EC")))signature=true;}}
   PackageInfo pkg=getPackageManager().getPackageArchiveInfo(temp.getAbsolutePath(),PackageManager.GET_PERMISSIONS);
   ThreatEngine.Verdict verdict;
   try(InputStream check=new FileInputStream(temp)){verdict=ThreatEngine.scan(this,check);}
   ApkRiskAnalyzer.Result risk=ApkRiskAnalyzer.evaluate(pkg,manifest,duplicates,dex,nativeFiles,zipAnomalies);
   StringBuilder report=new StringBuilder();
   report.append("تحليل المخاطر الاسترشادي: ").append(risk.score).append("/100 — ").append(risk.band).append("\n").append(risk.details).append("\n\n");
   report.append("Threat Engine: ").append(verdict.status).append("\n").append(verdict.reason).append("\n").append(ThreatEngine.feedStatus(this)).append("\n");

   report.append("SHA-256: ").append(hex).append("\n");
   report.append("الحجم: ").append(String.format(Locale.US,"%.2f MB",bytes/1048576.0)).append("\n");
   report.append("عدد الملفات: ").append(entries).append("\n");
   report.append("ملفات DEX: ").append(dex).append(" | مكتبات Native: ").append(nativeFiles).append("\n");
   report.append("AndroidManifest.xml: ").append(manifest?"موجود":"غير موجود").append("\n");
   report.append("توقيع META-INF تقليدي: ").append(signature?"موجود":"غير ظاهر (قد يستخدم APK Signature Scheme أحدث)").append("\n");
   report.append("أسماء ZIP مكررة: ").append(duplicates).append("\n");
   if(pkg!=null){report.append("معرّف الحزمة: ").append(pkg.packageName).append("\n");if(pkg.requestedPermissions!=null){report.append("الأذونات المعلنة (").append(pkg.requestedPermissions.length).append("):\n");for(String p:pkg.requestedPermissions)report.append("• ").append(p).append("\n");}}
   else report.append("تعذر قراءة بيانات الحزمة؛ ربما الملف غير صالح أو غير مدعوم.\n");
   report.append("\nملاحظات: ").append(duplicates>0?"وجود أسماء مكررة في ZIP يستحق الفحص. ":"").append(!manifest?"ملف Manifest غير موجود. ":"");
   report.append("لا توجد قاعدة بيانات تهديدات متصلة، ولا تحليل سلوكي أو تحقق من سلامة التوقيع. عدم وجود مؤشرات لا يعني سلامة APK.");
   return report.toString();
  }finally{temp.delete();}
 }
}
