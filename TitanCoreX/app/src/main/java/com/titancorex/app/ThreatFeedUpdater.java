package com.titancorex.app;

import android.app.job.*;
import android.content.*;
import android.os.*;
import java.io.*;
import java.net.*;
import java.util.*;

/** HTTPS transport for a pinned, signed threat intelligence feed. */
public final class ThreatFeedUpdater {
 // Provision both values from a vetted threat-intelligence service before production.
 public static final String FEED_URL=""; // Disabled: no verified signed feed.json and .sig have been provisioned.
 private static final int MAX_FEED_BYTES=12*1024*1024;
 private ThreatFeedUpdater(){}
 public static boolean configured(){return FEED_URL.startsWith("https://")&&ThreatEngine.isSignatureConfigured();}
 private static byte[] download(String url,int max)throws Exception{
  URL u=new URL(url);if(!"https".equalsIgnoreCase(u.getProtocol()))throw new SecurityException("HTTPS required");
  HttpURLConnection c=(HttpURLConnection)u.openConnection();c.setConnectTimeout(12000);c.setReadTimeout(20000);c.setInstanceFollowRedirects(false);
  try{if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());if(c.getContentLengthLong()>max)throw new IOException("Feed too large");
   try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new IOException("Feed too large");out.write(b,0,n);}return out.toByteArray();}
  }finally{c.disconnect();}
 }
 public static void update(Context context)throws Exception{
  if(!configured())throw new IllegalStateException("Trusted feed endpoint and verification key not configured");
  byte[] json=download(FEED_URL,MAX_FEED_BYTES);
  byte[] sig=download(FEED_URL+".sig",2048);
  // The signature is over the exact downloaded JSON bytes.
  ThreatEngine.installSignedFeed(context,json,sig);
 }
 public static void schedule(Context c){
  if(!configured())throw new IllegalStateException("Feed source not configured");
  JobScheduler jobs=(JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
  JobInfo info=new JobInfo.Builder(771205,new ComponentName(c,ThreatUpdateJob.class))
   .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(12L*60*60*1000).setPersisted(true).build();
  jobs.schedule(info);
 }
 public static void cancel(Context c){((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(771205);}
 public static boolean scheduled(Context c){return ((JobScheduler)c.getSystemService(Context.JOB_SCHEDULER_SERVICE)).getPendingJob(771205)!=null;}
}
