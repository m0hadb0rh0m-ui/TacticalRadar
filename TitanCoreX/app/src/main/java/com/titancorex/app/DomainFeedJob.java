package com.titancorex.app;

import android.app.job.*;
import android.content.*;
import android.os.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/** Downloads public hosts indicators over TLS. Failure retains last known good list. */
public class DomainFeedJob extends JobService {
 public static final int ID=771211;
 public static final String SOURCE="https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts";
 public static void schedule(Context c){
  JobScheduler jobs=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  jobs.schedule(new JobInfo.Builder(ID,new ComponentName(c,DomainFeedJob.class))
   .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED).setPeriodic(24*60*60*1000L).setPersisted(true).build());
 }
 public static String update(Context context)throws Exception{
  HttpURLConnection conn=(HttpURLConnection)new URL(SOURCE).openConnection();
  conn.setConnectTimeout(12000);conn.setReadTimeout(25000);conn.setInstanceFollowRedirects(false);
  if(conn.getResponseCode()!=200)throw new IOException("HTTP "+conn.getResponseCode());
  Set<String> names=new HashSet<>();int bytes=0;
  try(BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream(),java.nio.charset.StandardCharsets.UTF_8))){
   String line;while((line=reader.readLine())!=null){
    bytes+=line.length()+1;if(bytes>12*1024*1024)throw new IOException("Feed exceeds 12 MB");
    line=line.trim();if(line.startsWith("#")||line.isEmpty())continue;
    String[] fields=line.split("\\s+");if(fields.length<2||!("0.0.0.0".equals(fields[0])||"127.0.0.1".equals(fields[0])))continue;
    for(int i=1;i<fields.length;i++){String d=fields[i].toLowerCase(Locale.US);
     if(d.length()<4||d.length()>253||d.equals("localhost")||!d.contains(".")||!d.matches("[a-z0-9][a-z0-9.-]*[a-z0-9]")||d.contains(".."))continue;
     names.add(d);if(names.size()>180000)throw new IOException("Too many domains");
    }
   }
  }finally{conn.disconnect();}
  if(names.size()<1000)throw new IOException("Feed suspiciously small");
  StringBuilder output=new StringBuilder();for(String name:names)output.append(name).append('\n');
  context.getSharedPreferences("titan_dns_feed",MODE_PRIVATE).edit().putString("domains",output.toString())
   .putInt("count",names.size()).putLong("updated",System.currentTimeMillis()).apply();
  return "Updated "+names.size()+" domains";
 }
 @Override public boolean onStartJob(JobParameters params){
  new Thread(()->{boolean retry=false;try{update(this);}catch(Exception e){
   retry=true;getSharedPreferences("titan_dns_feed",MODE_PRIVATE).edit().putString("last_error",e.toString()).apply();
  }finally{jobFinished(params,retry);}}).start();return true;
 }
 @Override public boolean onStopJob(JobParameters params){return true;}
}
