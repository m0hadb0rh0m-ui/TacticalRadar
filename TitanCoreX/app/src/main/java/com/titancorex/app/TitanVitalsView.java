package com.titancorex.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** V3 mini-dashboard chart using only actual foreground Android telemetry samples. */
public final class TitanVitalsView extends View {
 private static final int BLUE=0xff087BEA,TEAL=0xff009F9A,ORANGE=0xffE49A32,INK=0xff1A3B60;
 private static final int LIMIT=48;
 private final ArrayList<Float> temps=new ArrayList<>();
 private final ArrayList<Float> available=new ArrayList<>();
 private final ArrayList<Float> battery=new ArrayList<>();
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final float density;
 public TitanVitalsView(Context c){super(c);density=getResources().getDisplayMetrics().density;
  setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 public void addSample(int charge,float temperature,long free,long total){
  if(temps.size()==LIMIT){temps.remove(0);available.remove(0);battery.remove(0);}
  temps.add(temperature>=0?temperature:Float.NaN);
  available.add(total>0?Math.max(0,Math.min(100,(float)free/total*100)):Float.NaN);
  battery.add(charge>=0?charge:Float.NaN);
  invalidate();
 }
 private void trace(Canvas c,ArrayList<Float> items,float l,float r,float top,float bottom,
  float min,float max,int color){
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.8f*density);p.setStrokeJoin(Paint.Join.ROUND);
  p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);
  Path line=new Path();boolean begun=false;
  for(int i=0;i<items.size();i++){
   float v=items.get(i);if(Float.isNaN(v)){begun=false;continue;}
   float x=l+(r-l)*i/(LIMIT-1f);
   float y=bottom-(bottom-top)*Math.max(0,Math.min(1,(v-min)/(max-min)));
   if(!begun){line.moveTo(x,y);begun=true;}else line.lineTo(x,y);
  }
  c.drawPath(line,p);p.setStyle(Paint.Style.FILL);
  for(int i=items.size()-1;i>=0;i--)if(!Float.isNaN(items.get(i))){
   float x=l+(r-l)*i/(LIMIT-1f);
   float y=bottom-(bottom-top)*Math.max(0,Math.min(1,(items.get(i)-min)/(max-min)));
   c.drawCircle(x,y,3.7f*density,p);break;
  }
 }
 @Override protected void onDraw(Canvas c){
  super.onDraw(c);float w=getWidth(),h=getHeight(),pad=14*density;
  p.setStyle(Paint.Style.FILL);p.setColor(0xffffffff);
  c.drawRoundRect(0,0,w,h,20*density,20*density,p);
  p.setColor(INK);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));
  p.setTextSize(13*density);p.setTextAlign(Paint.Align.LEFT);
  c.drawText("LIVE DEVICE TRENDS",pad,27*density,p);
  p.setTextSize(10*density);p.setTypeface(Typeface.DEFAULT);p.setColor(0xff65809D);
  c.drawText("عينات حقيقية أثناء فتح التطبيق",pad,46*density,p);
  float chartTop=62*density,chartBottom=h-32*density;
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(density);p.setColor(0xffDCE9F7);
  for(int i=0;i<4;i++){float y=chartTop+(chartBottom-chartTop)*i/3;c.drawLine(pad,y,w-pad,y,p);}
  if(temps.size()>0){
   trace(c,temps,pad,w-pad,chartTop,chartBottom,20,50,ORANGE);
   trace(c,available,pad,w-pad,chartTop,chartBottom,0,100,TEAL);
   trace(c,battery,pad,w-pad,chartTop,chartBottom,0,100,BLUE);
  }
  p.setStyle(Paint.Style.FILL);p.setTextSize(10*density);p.setTypeface(Typeface.DEFAULT);
  float x=pad,y=h-11*density;
  p.setColor(BLUE);c.drawText("● بطارية %",x,y,p);
  p.setColor(TEAL);c.drawText("● RAM متاحة %",x+91*density,y,p);
  p.setColor(ORANGE);c.drawText("● حرارة °C",x+196*density,y,p);
 }
}
