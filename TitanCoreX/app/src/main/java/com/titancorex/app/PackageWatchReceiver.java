package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import java.util.*;
import android.os.*;

/** Event-driven alerts for newly installed packages, opt-in only. */
public final class PackageWatchReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){
  if(intent==null||!Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())||intent.getData()==null)return;
  if(intent.getBooleanExtra(Intent.EXTRA_REPLACING,false))return;
  if(!context.getSharedPreferences("titan_monitor",Context.MODE_PRIVATE).getBoolean("instant_alerts",false))return;
  String pkg=intent.getData().getSchemeSpecificPart();if(pkg==null||pkg.isEmpty())return;
  android.content.SharedPreferences p=context.getSharedPreferences("titan_monitor",Context.MODE_PRIVATE);
  String concern=permissionConcern(context,pkg);
  p.edit().putString("last_install_attention",concern).apply();
  p.edit().putString("last_installed",pkg).putLong("last_install_time",System.currentTimeMillis()).apply();
  NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel("titan_changes","TITAN App Changes",NotificationManager.IMPORTANCE_DEFAULT));
  if(Build.VERSION.SDK_INT>=33&&context.checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)return;
  Intent launch=new Intent(context,ProtectionActivity.class);launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
  PendingIntent pending=PendingIntent.getActivity(context,0,launch,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,"titan_changes"):new Notification.Builder(context);
  b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(concern.isEmpty()?"TITAN: تطبيق جديد مثبت":"TITAN: راجع أذونات التطبيق الجديد").setContentText(concern.isEmpty()?"تم تثبيت "+pkg+" — راجع أذوناته. لا يعني ذلك أنه ضار.":concern).setContentIntent(pending).setAutoCancel(true);
  nm.notify(7713,b.build());
 } 
 private static String permissionConcern(Context context,String pkg){
  try{
   PackageInfo info=context.getPackageManager().getPackageInfo(pkg,PackageManager.GET_PERMISSIONS);
   Set<String> p=new HashSet<>();
   if(info.requestedPermissions!=null)Collections.addAll(p,info.requestedPermissions);
   boolean sms=p.contains("android.permission.READ_SMS")||p.contains("android.permission.RECEIVE_SMS");
   boolean contacts=p.contains("android.permission.READ_CONTACTS");
   boolean overlay=p.contains("android.permission.SYSTEM_ALERT_WINDOW");
   boolean installer=p.contains("android.permission.REQUEST_INSTALL_PACKAGES");
   if(sms&&contacts)return "يطلب الوصول للرسائل وجهات الاتصال؛ راجع ضرورته.";
   if(overlay&&installer)return "يطلب النوافذ العائمة وتثبيت تطبيقات؛ راجع المصدر.";
  }catch(Exception ignored){}
  return "";
 }
}
