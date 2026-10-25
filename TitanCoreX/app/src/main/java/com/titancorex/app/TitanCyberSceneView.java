package com.titancorex.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Decorative cyber radar art. Not a security measurement. */
public final class TitanCyberSceneView extends View {
 private final Paint p=new Paint(3);
 private final int accent;
 public TitanCyberSceneView(Context context,int color){super(context);accent=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
 @Override protected void onDraw(Canvas canvas){
  super.onDraw(canvas);
  float w=getWidth(),h=getHeight(),cx=w/2f,cy=h/2f;
  float r=Math.min(w,h)*.38f;
  p.setStyle(Paint.Style.FILL);p.setShader(new RadialGradient(cx,cy,r*1.45f,
   new int[]{(accent&0x00ffffff)|0x33000000,0x10121e35,0x00121e35},null,Shader.TileMode.CLAMP));
  canvas.drawCircle(cx,cy,r*1.45f,p);p.setShader(null);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.5f);
  for(int i=0;i<4;i++){
   p.setColor((accent&0x00ffffff)|((75+i*30)<<24));
   canvas.drawCircle(cx,cy,r*(.55f+i*.18f),p);
  }
  p.setStrokeWidth(5f);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(accent);
  RectF ring=new RectF(cx-r,cy-r,cx+r,cy+r);
  canvas.drawArc(ring,-85,92,false,p);canvas.drawArc(ring,30,70,false,p);
  canvas.drawArc(ring,135,95,false,p);
  p.setStrokeWidth(1f);p.setColor((accent&0x00ffffff)|0x77000000);
  canvas.drawLine(cx-r*1.1f,cy,cx+r*1.1f,cy,p);canvas.drawLine(cx,cy-r*1.1f,cx,cy+r*1.1f,p);
  p.setStyle(Paint.Style.FILL);p.setColor(accent);
  for(int i=0;i<12;i++){double a=i*Math.PI/6;canvas.drawCircle(cx+(float)Math.cos(a)*r*1.12f,cy+(float)Math.sin(a)*r*1.12f,2.3f,p);}
  p.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));p.setTextAlign(Paint.Align.CENTER);
  p.setTextSize(Math.min(w,h)*.28f);p.setColor(Color.WHITE);canvas.drawText("⬡",cx,cy+Math.min(w,h)*.095f,p);
  p.setStyle(Paint.Style.FILL);
 }
}
