package com.titancorex.app;

import android.app.job.*;
import android.content.*;
import android.net.*;
import android.os.*;
import java.net.*;
import java.util.*;

/** Best-effort periodic monitoring of network DNS/gateway changes. */
public class NetworkWatchJob extends JobService {
 public static final int ID=771209;
 public static void schedule(Context c){
  JobScheduler js=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  JobInfo job=new JobInfo.Builder(ID,new ComponentName(c,NetworkWatchJob.class))
   .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(15*60*1000L).setPersisted(true).build();
  js.schedule(job);
 }
 public static void stop(Context c){((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(ID);}
 @Override public boolean onStartJob(JobParameters p){
  new Thread(()->{try{
   ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
   Network n=cm.getActiveNetwork();LinkProperties lp=n==null?null:cm.getLinkProperties(n);
   if(lp!=null){List<String> data=new ArrayList<>();
    for(InetAddress a:lp.getDnsServers())data.add("dns:"+a.getHostAddress());
    for(RouteInfo route:lp.getRoutes())if(route.isDefaultRoute()&&route.getGateway()!=null)data.add("gateway:"+route.getGateway().getHostAddress());
    data.add("privateDns:"+lp.isPrivateDnsActive());Collections.sort(data);
    String current=data.toString();android.content.SharedPreferences prefs=getSharedPreferences("titan_network_watch",MODE_PRIVATE);
    String old=prefs.getString("last",null);
    prefs.edit().putString("last",current).putLong("last_checked",System.currentTimeMillis()).putBoolean("changed",old!=null&&!old.equals(current)).apply();
   }
  }finally{jobFinished(p,false);}}).start();return true;
 }
 @Override public boolean onStopJob(JobParameters p){return true;}
}
