package com.titancorex.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class PremiumActivity extends Activity {
  private final int BG=0xff080C16, PANEL=0xff15243A, CYAN=0xff42E8F5, WHITE=0xffF2F7FF, MUTED=0xffB3C3D5, GOLD=0xffFFCC70;
  private LinearLayout root;
  private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
  private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(8),dp(8),dp(8),dp(8));return t;}
  private GradientDrawable bg(){GradientDrawable d=new GradientDrawable();d.setColor(PANEL);d.setCornerRadius(dp(16));d.setStroke(dp(1),0xff335A6D);return d;}
  private void feature(String title,String detail,boolean available,Runnable action){
    LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(13),dp(11),dp(13),dp(11));card.setBackground(bg());
    card.addView(text((available?"✓  ":"◆  ")+title,17,available?CYAN:GOLD));card.addView(text(detail,12,MUTED));
    root.addView(card);View gap=new View(this);root.addView(gap,new LinearLayout.LayoutParams(1,dp(11)));
    card.setOnClickListener(v->{
      if(available&&action!=null)action.run();
      else new AlertDialog.Builder(this).setTitle("TITAN CORE X PRO").setMessage("هذه الميزة ضمن خطة PRO المستقبلية. لن يتم خصم أي مبلغ. التفعيل يتطلب نظام دفع مرخص والتحقق من الشراء أولًا.").setPositiveButton("حسنًا",null).show();
    });
  }
  @Override public void onCreate(Bundle b){
    super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
    ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
    root.setPadding(dp(18),dp(24),dp(18),dp(24));root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
    root.addView(text("✦  TITAN CORE X PRO",28,GOLD));
    root.addView(text("Premium Device Intelligence",16,CYAN));
    root.addView(text("$5.00 USD  •  سعر مستهدف لشراء لمرة واحدة",18,WHITE));
    root.addView(text("هذا عرض تجريبي لباقة PRO وليس صفحة دفع فعّالة. لا تُدخل بيانات بطاقتك هنا. قبل البيع يلزم إعداد حساب التاجر، شروط الخدمة، سياسة الخصوصية، والتحقق الآمن من المشتريات.",13,MUTED));
    TextView heading=text("الميزات المتاحة الآن — مجانًا",19,WHITE);root.addView(heading);
    feature("Thermal Guardian","متابعة الضغط الحراري وحرارة البطارية مع توصيات واقعية",true,()->startActivity(new Intent(this,ResourceLabActivity.class)));
    feature("RAM Pressure Lab","تحليل ضغط الذاكرة وحد انخفاض RAM",true,()->startActivity(new Intent(this,ResourceLabActivity.class)));
    feature("Storage Recovery","فحص الملفات المكررة وتنظيف ذاكرة التطبيق المؤقتة بموافقتك",true,()->startActivity(new Intent(this,ResourceLabActivity.class)));
    feature("Privacy Audit","مراجعة أذونات التطبيقات الظاهرة",true,()->startActivity(new Intent(this,PrivacyAuditActivity.class)));
    feature("Performance Trends","مخطط حرارة البطارية وسجل القياسات المحلي",true,()->startActivity(new Intent(this,TrendsActivity.class)));
    root.addView(text("خطة PRO المستقبلية — لم تُنفّذ بعد",19,GOLD));
    feature("Advanced Thermal Timeline","سجل حراري موسع وتنبيهات قابلة للتخصيص",false,null);
    feature("Storage Deep Insights","تقارير حجم الملفات داخل المجلدات التي تختارها، مع تحليل تكرار متقدم",false,null);
    feature("Gaming Session Analytics","مقارنة جلسات اللعب والحرارة والذاكرة عبر الزمن",false,null);
    feature("Privacy Change Monitor","مقارنة أذونات التطبيقات الظاهرة بين فحوصات يبدؤها المستخدم",false,null);
    feature("Export Studio","تصدير تقارير متقدمة بصيغ قابلة للمشاركة",false,null);
    Button payment=new Button(this);payment.setText("الدفع بالبطاقة البنكية — غير متاح حاليًا");payment.setEnabled(false);root.addView(payment);
    root.addView(text("طريقة الدفع المستهدفة: بطاقة بنكية عبر Google Play Billing عند التوزيع عبر Google Play، وفق متطلبات المتجر. لا يجمع التطبيق أرقام البطاقات أو رموز CVV.",12,MUTED));
  }
}
