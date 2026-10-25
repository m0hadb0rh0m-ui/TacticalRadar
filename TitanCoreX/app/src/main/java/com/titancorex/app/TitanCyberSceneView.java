package com.titancorex.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.os.PowerManager;
import android.view.View;

/** V3 light-mode animated radar. Decorative artwork only: no simulated telemetry. */
public final class TitanCyberSceneView extends View {
 private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final int accent;
 private long startMs;
 private boolean animating;
 private final Runnable animator=new Runnable(){@Override public void run(){
  if(!animating)return;
  invalidate();
  postDelayed(this,50);
 }};
 public TitanCyberSceneView(Context context,int color){
  super(context);accent=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
  setLayerType(View.LAYER_TYPE_SOFTWARE,null);
 }
 private boolean canAnimate(){
  PowerManager pm=(PowerManager)getContext().getSystemService(Context.POWER_SERVICE);
  return ValueAnimator.areAnimatorsEnabled()&&(pm==null||!pm.isPowerSaveMode());
 }
 private void start(){
  if(!isShown()||!canAnimate())return;
  startMs=android.os.SystemClock.uptimeMillis();
  animating=true;removeCallbacks(animator);post(animator);
 }
 private void stop(){animating=false;removeCallbacks(animator);}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();start();}
 @Override protected void onDetachedFromWindow(){stop();super.onDetachedFromWindow();}
 @Override protected void onVisibilityChanged(View changedView,int visibility){
  super.onVisibilityChanged(changedView,visibility);
  if(visibility==VISIBLE&&isAttachedToWindow())start();else stop();
 }
 @Override protected void onDraw(Canvas canvas){
  super.onDraw(canvas);
  float w=getWidth(),h=getHeight(),cx=w*.5f,cy=h*.51f;
  float r=Math.min(w*.35f,h*.41f);
  if(r<=0)return;
  float phase=animating?((android.os.SystemClock.uptimeMillis()-startMs)%8000L)/8000f:0f;
  paint.setShader(new RadialGradient(cx,cy,r*1.42f,
    new int[]{(accent&0x00ffffff)|0x24000000,0x10B4DFFF,0x00FFFFFF},null,Shader.TileMode.CLAMP));
  paint.setStyle(Paint.Style.FILL);
  canvas.drawCircle(cx,cy,r*1.42f,paint);paint.setShader(null);
  paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);
  for(int i=0;i<4;i++){
   paint.setStrokeWidth(Math.max(1.2f,r*.013f));
   paint.setColor((accent&0x00ffffff)|((45+i*21)<<24));
   canvas.drawCircle(cx,cy,r*(.44f+.185f*i),paint);
  }
  paint.setStrokeWidth(Math.max(4f,r*.045f));paint.setColor(accent);
  RectF outer=new RectF(cx-r,cy-r,cx+r,cy+r);
  canvas.save();canvas.rotate(phase*360f,cx,cy);
  canvas.drawArc(outer,-85,78,false,paint);
  canvas.drawArc(outer,45,43,false,paint);
  canvas.drawArc(outer,158,62,false,paint);
  canvas.restore();
  paint.setStrokeWidth(Math.max(2f,r*.017f));paint.setColor(0xff77C6F2);
  canvas.drawLine(cx-r*1.12f,cy,cx+r*1.12f,cy,paint);
  canvas.drawLine(cx,cy-r*1.12f,cx,cy+r*1.12f,paint);
  paint.setStyle(Paint.Style.FILL);paint.setColor((accent&0x00ffffff)|0x25000000);
  canvas.save();canvas.rotate(phase*360f,cx,cy);
  canvas.drawArc(new RectF(cx-r*.74f,cy-r*.74f,cx+r*.74f,cy+r*.74f),-18,48,true,paint);
  canvas.restore();
  for(int i=0;i<12;i++){
   double a=i*Math.PI/6+phase*Math.PI*2*.4;
   paint.setColor(i%3==0?accent:0xff83C5ED);
   canvas.drawCircle(cx+(float)Math.cos(a)*r*1.12f,cy+(float)Math.sin(a)*r*1.12f,
    i%3==0?Math.max(3f,r*.032f):Math.max(2f,r*.018f),paint);
  }
  paint.setColor(0xff17385D);paint.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
  paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(r*.37f);
  canvas.drawText("T",cx,cy+r*.125f,paint);
  paint.setTextSize(Math.max(10f,r*.11f));paint.setColor(accent);
  canvas.drawText("TITAN V3",cx,cy+r*.36f,paint);
 }
}
