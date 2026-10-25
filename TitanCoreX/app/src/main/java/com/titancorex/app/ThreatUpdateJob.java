package com.titancorex.app;

import android.app.job.*;
import android.os.*;

public class ThreatUpdateJob extends JobService {
 @Override public boolean onStartJob(JobParameters params){
  if(!ThreatFeedUpdater.configured()){ThreatFeedUpdater.cancel(this);return false;}
  new Thread(()->{
   boolean retry=false;
   try{ThreatFeedUpdater.update(this);getSharedPreferences("titan_feed_update",MODE_PRIVATE).edit().putString("last_error","").putLong("last_success",System.currentTimeMillis()).apply();}
   catch(Exception e){retry=true;getSharedPreferences("titan_feed_update",MODE_PRIVATE).edit().putString("last_error",e.getClass().getSimpleName()+": "+e.getMessage()).apply();}
   jobFinished(params,retry);
  }).start();
  return true;
 }
 @Override public boolean onStopJob(JobParameters params){return true;}
}
