package com.titancorex.app;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.os.StatFs;
import android.os.Environment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Non-invasive Android security posture audit.
 * Does not claim to detect malware or access packages hidden by Android visibility limits.
 */
public final class TitanSecurityPosture {
 private TitanSecurityPosture(){}
 public interface Progress { void onProgress(int checked,int total,String label); }
 public static final class Finding {
  public final String label,detail;
  public final boolean attention;
  public Finding(String label,String detail,boolean attention){
   this.label=label;this.detail=detail;this.attention=attention;
  }
 }
 public static final class Report {
  public final List<Finding> findings;
  public final int passed,reviewed,visibleApps,sensitiveGrants;
  Report(List<Finding> f,int p,int apps,int grants){
   findings=f;passed=p;reviewed=f.size();visibleApps=apps;sensitiveGrants=grants;
  }
 }
 private static boolean dangerous(String p){
  return p.equals("android.permission.CAMERA")
   ||p.equals("android.permission.RECORD_AUDIO")
   ||p.equals("android.permission.ACCESS_FINE_LOCATION")
   ||p.equals("android.permission.ACCESS_COARSE_LOCATION")
   ||p.equals("android.permission.ACCESS_BACKGROUND_LOCATION")
   ||p.equals("android.permission.READ_SMS")
   ||p.equals("android.permission.READ_CONTACTS")
   ||p.equals("android.permission.READ_CALL_LOG");
 }
 private static void add(List<Finding> out,Progress progress,String title,String detail,boolean attention){
  out.add(new Finding(title,detail,attention));
  if(progress!=null)progress.onProgress(out.size(),6,title);
 }
 public static Report audit(Context context,Progress progress){
  ArrayList<Finding> out=new ArrayList<>();
  KeyguardManager km=(KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
  boolean locked=km!=null&&km.isDeviceSecure();
  add(out,progress,"قفل الشاشة",locked?"قفل شاشة آمن مفعل":"فعّل PIN أو كلمة مرور من إعدادات Android",!locked);
  boolean adb=Settings.Global.getInt(context.getContentResolver(),Settings.Global.ADB_ENABLED,0)!=0;
  add(out,progress,"تصحيح USB",adb?"تصحيح USB مفعل؛ راجع استخدامه":"تصحيح USB غير مفعل",adb);
  boolean developer=Settings.Global.getInt(context.getContentResolver(),Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,0)!=0;
  add(out,progress,"خيارات المطور",developer?"راجع سبب تشغيل خيارات المطور":"خيارات المطور غير مفعلة",developer);
  String patch=Build.VERSION.SECURITY_PATCH;
  long days=-1;
  try{
   SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd",Locale.US);
   format.setLenient(false);format.setTimeZone(TimeZone.getTimeZone("UTC"));
   Date parsed=format.parse(patch);if(parsed!=null)days=Math.max(0,(System.currentTimeMillis()-parsed.getTime())/86400000L);
  }catch(Exception ignored){}
  add(out,progress,"تصحيح أمان Android",days<0?"تاريخ التصحيح غير متاح":("آخر تصحيح معلن: "+patch+" (منذ "+days+" يوم)"),days<0||days>180);
  StatFs storage=new StatFs(Environment.getDataDirectory().getAbsolutePath());
  long total=storage.getTotalBytes(),available=storage.getAvailableBytes();
  boolean space=total>0&&available*10>=total;
  add(out,progress,"المساحة المتاحة",total<=0?"القياس غير متاح":String.format(Locale.US,"المتاح %.1f GB",available/1073741824.0),!space);

  int visible=0,grants=0;
  try{
   PackageManager pm=context.getPackageManager();
   Intent launcher=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
   HashSet<String> unique=new HashSet<>();
   for(ResolveInfo info:pm.queryIntentActivities(launcher,0)){
    if(info.activityInfo==null||!unique.add(info.activityInfo.packageName))continue;
    visible++;
    try{
     PackageInfo pkg=pm.getPackageInfo(info.activityInfo.packageName,PackageManager.GET_PERMISSIONS);
     if(pkg.requestedPermissions==null)continue;
     for(int i=0;i<pkg.requestedPermissions.length;i++){
      int flag=pkg.requestedPermissionsFlags==null||i>=pkg.requestedPermissionsFlags.length?0:pkg.requestedPermissionsFlags[i];
      if(dangerous(pkg.requestedPermissions[i])&&(flag&PackageInfo.REQUESTED_PERMISSION_GRANTED)!=0)grants++;
     }
    }catch(Exception ignored){}
   }
  }catch(Exception ignored){}
  add(out,progress,"أذونات التطبيقات الظاهرة","تمت مراجعة "+visible+" تطبيقًا ظاهرًا؛ عدد الأذونات الحساسة الممنوحة: "+grants+". منح الإذن لا يعني وجود تهديد.",false);
  int passed=0;for(Finding f:out)if(!f.attention)passed++;
  return new Report(out,passed,visible,grants);
 }
}
