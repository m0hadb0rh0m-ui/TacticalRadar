package com.titancorex.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashActivity extends Activity {
  private final Handler handler=new Handler(Looper.getMainLooper());
  private final Runnable next=()->{if(!isFinishing()){startActivity(new Intent(this,AuthActivity.class));finish();}};
  private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  @Override public void onCreate(Bundle state){
    super.onCreate(state);
    getWindow().setStatusBarColor(0xff080D19);getWindow().setNavigationBarColor(0xff080D19);
    LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setGravity(Gravity.CENTER);root.setPadding(dp(24),dp(24),dp(24),dp(24));
    GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff080D19,0xff0F2035,0xff080D19});
    root.setBackground(bg);setContentView(root);
    ImageView icon=new ImageView(this);icon.setImageResource(R.drawable.titan_logo);
    root.addView(icon,new LinearLayout.LayoutParams(dp(236),dp(236)));
    TextView name=new TextView(this);name.setText("TITAN CORE X");name.setTextColor(Color.WHITE);name.setTextSize(32);
    name.setGravity(Gravity.CENTER);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);name.setLetterSpacing(.11f);
    LinearLayout.LayoutParams titleParams=new LinearLayout.LayoutParams(-1,-2);titleParams.topMargin=dp(10);root.addView(name,titleParams);
    TextView subtitle=new TextView(this);subtitle.setText("DEVICE INTELLIGENCE  •  SECURITY  •  PERFORMANCE");
    subtitle.setTextColor(0xff42E8F5);subtitle.setTextSize(10);subtitle.setLetterSpacing(.07f);subtitle.setGravity(Gravity.CENTER);
    subtitle.setPadding(0,dp(10),0,0);root.addView(subtitle);
    TextView tag=new TextView(this);tag.setText("POWERED BY REAL ANDROID DIAGNOSTICS");
    tag.setTextColor(0xff9BAEC5);tag.setTextSize(10);tag.setGravity(Gravity.CENTER);tag.setPadding(0,dp(34),0,0);root.addView(tag);
    root.setOnClickListener(v->{handler.removeCallbacks(next);next.run();});
    if(!BuildConfig.DEBUG){
      try{
        TitanAppIntegrity.Result integrity=TitanAppIntegrity.inspect(this);
        if(!integrity.packageOk||!integrity.signatureConfigured||!integrity.signatureOk||integrity.debuggerAttached){
          handler.removeCallbacks(next);
          root.setOnClickListener(null);
          new android.app.AlertDialog.Builder(this).setTitle("تعذر التحقق من سلامة التطبيق")
            .setMessage("تعذر تأكيد توقيع نسخة TITAN CORE X أو بيئة تشغيلها. استخدم النسخة الرسمية.")
            .setCancelable(false).setPositiveButton("إغلاق", (dialog,which)->finish()).show();
          return;
        }
      }catch(Exception error){
        handler.removeCallbacks(next);root.setOnClickListener(null);
        new android.app.AlertDialog.Builder(this).setTitle("فشل التحقق الأمني")
          .setMessage("لم يتم التحقق من سلامة النسخة.").setCancelable(false)
          .setPositiveButton("إغلاق",(dialog,which)->finish()).show();
        return;
      }
    }
    handler.postDelayed(next,1400);
  }
  @Override protected void onDestroy(){handler.removeCallbacks(next);super.onDestroy();}
}
