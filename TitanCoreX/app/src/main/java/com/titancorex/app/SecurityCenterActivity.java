package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class SecurityCenterActivity extends Activity {
 private final int BG=0xff070B14, PANEL=0xff142139, CYAN=0xff48E9F5, WHITE=0xffF2F7FF, MUTED=0xffA9BED5, GOLD=0xffFFCB78, GREEN=0xff52E8A8;
 private LinearLayout root;
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
 private TextView txt(String s,int z,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(color);v.setPadding(dp(9),dp(8),dp(9),dp(8));return v;}
 private GradientDrawable shape(){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff192B45,0xff101B30});d.setCornerRadius(dp(20));d.setStroke(dp(1),0xff31576D);return d;}
 private LinearLayout card(String title){
   LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(14),dp(13),dp(14),dp(13));c.setBackground(shape());
   c.addView(txt(title,19,CYAN));root.addView(c);View gap=new View(this);root.addView(gap,new LinearLayout.LayoutParams(1,dp(13)));return c;
 }
 private void check(LinearLayout c,String title,String value,boolean ok){
   c.addView(txt((ok?"●  ":"◆  ")+title,15,ok?GREEN:GOLD));c.addView(txt(value,12,MUTED));
 }
 private void link(LinearLayout c,String label,String action){
   Button b=new Button(this);b.setAllCaps(false);b.setText(label);c.addView(b);b.setOnClickListener(v->{
     try{startActivity(new Intent(action));}catch(Exception e){Toast.makeText(this,"الإعداد غير متاح على جهازك",Toast.LENGTH_SHORT).show();}
   });
 }
 @Override public void onCreate(Bundle b){
   super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
   ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
   root.setPadding(dp(18),dp(24),dp(18),dp(24));root.setBackgroundColor(BG);scroll.addView(root);setContentView(scroll);
   root.addView(txt("⬡  TITAN SECURITY COMMAND",24,CYAN));
   root.addView(txt("مركز تقييم إعدادات حماية الهاتف",16,WHITE));
   root.addView(txt("فحوصات حقيقية متاحة لتطبيق عادي دون Root. لا ندعي اكتشاف الفيروسات أو ضمان خلو الجهاز من البرمجيات الضارة.",12,MUTED));
   android.app.KeyguardManager km=(android.app.KeyguardManager)getSystemService(KEYGUARD_SERVICE);
   boolean secure=km!=null&&km.isDeviceSecure();
   boolean debug=Settings.Global.getInt(getContentResolver(),Settings.Global.ADB_ENABLED,0)==1;
   boolean dev=Settings.Global.getInt(getContentResolver(),Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,0)==1;
   boolean encrypted=android.os.Build.VERSION.SDK_INT>=26; // Modern Android encryption capability, not a verified encryption state.
   int passed=(secure?1:0)+(!debug?1:0)+(!dev?1:0);
   LinearLayout summary=card("حالة الإعدادات الأمنية");
   summary.addView(txt(passed+" / 3",36,passed==3?GREEN:GOLD));
   summary.addView(txt("هذا عدد الإعدادات التي اجتازت الفحص، وليس نسبة الحماية أو نتيجة فحص فيروسات.",12,MUTED));
   LinearLayout checks=card("Security Posture Audit");
   check(checks,"قفل الشاشة",secure?"قفل آمن مفعّل":"لا يظهر قفل شاشة آمن",secure);
   check(checks,"تصحيح USB (ADB)",debug?"مفعّل — راجعه إذا لم تكن بحاجة إليه":"غير مفعّل",!debug);
   check(checks,"خيارات المطوّر",dev?"مفعّلة — راجع الإعدادات":"غير مفعّلة",!dev);
   check(checks,"التشفير","حالة تشفير البيانات غير قابلة للتحقق بدقة عبر هذا الفحص؛ لم تُحتسب في النتيجة.",false);
   LinearLayout tools=card("إجراءات حماية موثوقة");
   link(tools,"إعدادات الأمان وقفل الشاشة",Settings.ACTION_SECURITY_SETTINGS);
   link(tools,"مراجعة أذونات التطبيقات",Settings.ACTION_APPLICATION_SETTINGS);
   link(tools,"إعدادات الخصوصية",Settings.ACTION_PRIVACY_SETTINGS);
   Button audit=new Button(this);audit.setText("فحص أذونات التطبيقات");tools.addView(audit);
   audit.setOnClickListener(v->startActivity(new Intent(this,PrivacyAuditActivity.class)));
   Button refresh=new Button(this);refresh.setText("إعادة فحص حالة الأمان");tools.addView(refresh);refresh.setOnClickListener(v->recreate());
   root.addView(txt("مهم: Google Play Protect ومحركات كشف البرمجيات الخبيثة أدوات مستقلة. هذا التطبيق لا يفحص ملفات APK باستخدام قاعدة توقيعات للفيروسات.",12,GOLD));
 }
}
