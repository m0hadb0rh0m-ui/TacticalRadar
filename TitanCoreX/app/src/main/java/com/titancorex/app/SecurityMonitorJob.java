package com.titancorex.app;

import android.app.*;
import android.app.job.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import java.util.*;

/** Periodic app inventory change monitor. Not real-time antivirus. */
public class SecurityMonitorJob extends JobService {
 public static final int ID=771204;
 public static void schedule(Context c){
  JobScheduler js=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  JobInfo job=new JobInfo.Builder(ID,new ComponentName(c,SecurityMonitorJob.class))
   .setPeriodic(15*60*1000L).setPersisted(true).build();
  js.schedule(job);
 }
 public static void stop(Context c){((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(ID);}
 public static boolean enabled(Context c){return ((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).getPendingJob(ID)!=null;}
 @Override public boolean onStartJob(JobParameters params){
  new Thread(()->{
   try{
    PackageManager pm=getPackageManager();
    Intent intent=new Intent(Intent.ACTION_MAIN);intent.addCategory(Intent.CATEGORY_LAUNCHER);
    List<ResolveInfo> apps=pm.queryIntentActivities(intent,0);
    Set<String> names=new TreeSet<>();
    for(ResolveInfo info:apps)if(info.activityInfo!=null)names.add(info.activityInfo.packageName);
    SharedPreferences pref=getSharedPreferences("titan_monitor",MODE_PRIVATE);
    Set<String> previous=pref.getStringSet("inventory",null);
    Set<String> added=new TreeSet<>(names);
    if(previous!=null)added.removeAll(previous);
    pref.edit().putStringSet("inventory",new HashSet<>(names)).putLong("last_run",System.currentTimeMillis()).putInt("count",names.size()).putString("new_apps",String.join(", ",added)).apply();
    if(previous!=null&&!added.isEmpty())notifyChange(added.size());
   }catch(Exception e){getSharedPreferences("titan_monitor",MODE_PRIVATE).edit().putString("error",e.toString()).apply();}
   jobFinished(params,false);
  }).start();
  return true;
 }
 private void notifyChange(int count){
  NotificationManager nm=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
  if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel("titan_changes","TITAN App Changes",NotificationManager.IMPORTANCE_DEFAULT));
  if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=PackageManager.PERMISSION_GRANTED)return;
  Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"titan_changes"):new Notification.Builder(this);
  b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("TITAN CORE X — تطبيقات جديدة").setContentText("تم رصد "+count+" تطبيقات جديدة مرئية منذ آخر فحص. ليست بالضرورة ضارة.").setAutoCancel(true);
  nm.notify(7712,b.build());
 }
 @Override public boolean onStopJob(JobParameters params){return true;}
}
