package com.titancorex.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.*;
import android.os.PowerManager;
import android.os.SystemClock;
import android.view.View;

/** V3 decorative animated security orb; only shows genuine audit progress, never a fake protection percentage. */
public final class TitanV3OrbView extends View {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private int completed=0,total=0;
 private boolean attention=false,animating=false;
 private long started;
 private final float density;
 private final Runnable tick=new Runnable(){@Override public void run(){
  if(!animating)return;invalidate();postDelayed(this,16);
 }};
 public TitanV3OrbView(Context c){super(c);density=getResources().getDisplayMetrics().density;
  setContentDescription("بدء الفحص الأمني");setClickable(true);
 }
 public void setAuditProgress(int done,int count,boolean hasAttention){
  completed=Math.max(0,done);total=Math.max(0,count);attention=hasAttention;
  setContentDescription(total==0?"اضغط لبدء فحص إعدادات الأمان":
   "فحص "+completed+" من "+total+" إعدادات"+(attention?"؛ توجد تنبيهات":""));
  invalidate();
 }
 private boolean allowMotion(){
  PowerManager pm=(PowerManager)getContext().getSystemService(Context.POWER_SERVICE);
  return ValueAnimator.areAnimatorsEnabled()&&(pm==null||!pm.isPowerSaveMode());
 }
 private void beginMotion(){if(!isShown()||!allowMotion())return;
  animating=true;started=SystemClock.uptimeMillis();removeCallbacks(tick);post(tick);
 }
 private void stopMotion(){animating=false;removeCallbacks(tick);}
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();beginMotion();}
 @Override protected void onDetachedFromWindow(){stopMotion();super.onDetachedFromWindow();}
 @Override protected void onVisibilityChanged(View changed,int visibility){
  super.onVisibilityChanged(changed,visibility);
  if(visibility==VISIBLE&&isAttachedToWindow())beginMotion();else stopMotion();
 }
 @Override protected void onDraw(Canvas c){
  super.onDraw(c);
  float w=getWidth(),h=getHeight(),cx=w*.5f,cy=h*.49f,r=Math.min(w,h)*.33f;
  if(r<=0)return;
  float phase=animating?(SystemClock.uptimeMillis()-started)/1000f:0;
  float pulse=1f+.035f*(float)Math.sin(phase*1.8);
  int accent=attention?0xffF59E0B:0xff10B981;
  p.setStyle(Paint.Style.FILL);
  p.setShader(new RadialGradient(cx,cy,r*1.52f,
    new int[]{attention?0x305A2B09:0x3048FFC9,0x1450E8F4,0x000B0F19},
    new float[]{0,.62f,1},Shader.TileMode.CLAMP));
  c.drawCircle(cx,cy,r*1.52f,p);p.setShader(null);
  p.setColor(0xff12283A);c.drawCircle(cx,cy,r*pulse,p);
  p.setShader(new LinearGradient(cx-r,cy-r,cx+r,cy+r,
    new int[]{0xff174F62,0xff0B192A,0xff182D42},null,Shader.TileMode.CLAMP));
  c.drawCircle(cx,cy,r*.89f*pulse,p);p.setShader(null);
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);
  p.setColor(0x334DD9E9);p.setStrokeWidth(Math.max(1.5f,2*density));
  for(int i=0;i<3;i++)c.drawCircle(cx,cy,r*(.94f+i*.12f),p);
  p.setColor(accent);p.setStrokeWidth(Math.max(3f,5*density));
  float rotate=phase*27;
  RectF arc=new RectF(cx-r*.98f,cy-r*.98f,cx+r*.98f,cy+r*.98f);
  c.drawArc(arc,rotate,75,false,p);
  c.drawArc(arc,rotate+170,46,false,p);
  if(total>0){
   p.setColor(0xff0A6C79);p.setStrokeWidth(6*density);
   c.drawArc(new RectF(cx-r*.73f,cy-r*.73f,cx+r*.73f,cy+r*.73f),-90,360,false,p);
   p.setColor(accent);
   c.drawArc(new RectF(cx-r*.73f,cy-r*.73f,cx+r*.73f,cy+r*.73f),
    -90,360f*Math.min(completed,total)/total,false,p);
  }
  p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.CENTER);
  p.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));
  p.setColor(0xffE7FCFF);p.setTextSize(Math.max(21*density,r*.33f));
  c.drawText(total>0?completed+" / "+total:"⬡",cx,cy+5*density,p);
  p.setTextSize(Math.max(10*density,r*.12f));p.setColor(accent);
  c.drawText(total>0?"CHECKS REVIEWED":"START SECURITY SCAN",cx,cy+r*.28f,p);
  p.setColor(0xff5FAFC7);p.setTextSize(10*density);
  c.drawText("TITAN CORE X • V3",cx,cy+r*.47f,p);
 }
}
