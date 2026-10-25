package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public class FeatureGuideActivity extends Activity {
 private static final int BG=0xff080D18,WHITE=0xffF5F9FF,CYAN=0xff59E4EC,MUTED=0xffB2C2D5;
 private LinearLayout root;private String feature;
 private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
 private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
 private void space(int h){View v=new View(this);root.addView(v,new LinearLayout.LayoutParams(1,dp(h)));}
 private GradientDrawable bg(){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff1A3049,0xff111D30});d.setCornerRadius(dp(18));d.setStroke(dp(1),0xff34566C);return d;}
 private void section(String title,String content){LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(17),dp(16),dp(17),dp(16));c.setBackground(bg());c.addView(text(title,17,WHITE,true));TextView t=text(content,14,MUTED,false);t.setLineSpacing(dp(5),1f);t.setPadding(0,dp(9),0,0);c.addView(t);root.addView(c);space(13);}
 private void action(String label,Class<?> cls){Button b=new Button(this);b.setAllCaps(false);b.setText(label);b.setTextColor(BG);GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,new int[]{0xff58E2EA,0xff9DF7F1});d.setCornerRadius(dp(15));b.setBackground(d);root.addView(b,new LinearLayout.LayoutParams(-1,dp(52)));b.setOnClickListener(v->startActivity(new Intent(this,cls)));space(10);}
 private void settings(String label,String action){Button b=new Button(this);b.setAllCaps(false);b.setText(label);root.addView(b);b.setOnClickListener(v->{try{startActivity(new Intent(action));}catch(Exception e){Toast.makeText(this,"الإعداد غير متاح",Toast.LENGTH_SHORT).show();}});space(10);}
 @Override public void onCreate(Bundle b){super.onCreate(b);feature=getIntent().getStringExtra("feature");if(feature==null)feature="overview";
  getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);root.setPadding(dp(19),dp(24),dp(19),dp(25));root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
  TextView back=text("‹  الرجوع",15,CYAN,true);root.addView(back);back.setOnClickListener(v->finish());space(20);
  switch(feature){
   case "security":
    header("مركز الحماية","SECURITY INTELLIGENCE");
    section("ما الذي يفحصه التطبيق؟","قفل الشاشة الآمن، تصحيح USB، تاريخ تصحيح Android الأمني، مساحة التخزين، وحرارة البطارية. هذه فحوصات إعدادات وليست فحصًا للبرمجيات الضارة.");
    section("كيف تفهم النتيجة؟","التنبيه يعني أن إعدادًا يستحق المراجعة، ولا يعني وجود اختراق. عدم وجود تنبيه لا يثبت خلو الهاتف من الفيروسات.");
    section("خطوات مقترحة","1. شغّل الفحص.\n2. افتح الإعدادات المقترحة لكل تنبيه.\n3. أعد الفحص للتحقق من التغييرات.");
    action("بدء الفحص الأمني",SmartScanActivity.class);action("تفاصيل صحة الأمان",SecurityCenterActivity.class);break;
   case "apps":
    header("تدقيق التطبيقات","APP PERMISSION INTELLIGENCE");
    section("ما الذي نراجعه؟","الأذونات الحساسة التي يمكن للنظام إظهارها للتطبيقات المرئية، مثل الكاميرا والميكروفون والموقع وجهات الاتصال، بحسب القيود المطبقة على إصدار Android.");
    section("كيف تفسّر النتائج؟","وجود إذن لا يعني أن التطبيق خبيث؛ افحص ملاءمة الإذن لوظيفة التطبيق. قد لا تظهر كل التطبيقات بسبب قيود رؤية الحزم.");
    section("طريقة الاستخدام","1. افتح تقرير الأذونات.\n2. راجع التطبيقات ذات الصلاحيات الحساسة.\n3. غيّر الأذونات غير الضرورية من إعدادات Android.");
    action("فتح تدقيق الأذونات",PrivacyAuditActivity.class);settings("فتح إعدادات التطبيقات",Settings.ACTION_APPLICATION_SETTINGS);break;
   case "scam":
    header("حماية الروابط","SCAM SHIELD");
    section("كيف يعمل التحليل؟","يحلّل عنوان الرابط محليًا بحثًا عن مؤشرات مثل HTTP غير المشفر، عنوان IP بدل اسم نطاق، أو نطاقات وصيغ URL مريبة.");
    section("حدود الفحص","لا يتم الاتصال بموقع الرابط ولا تتم مقارنته حاليًا بقاعدة بيانات تهديدات مباشرة؛ النتيجة استرشادية وليست ضمانًا للأمان.");
    section("قبل فتح أي رابط","تحقق من النطاق الحقيقي، وتجنّب إدخال كلمات المرور في صفحات وصلتك عبر رسائل مجهولة، واستخدم الموقع الرسمي مباشرة.");
    action("فتح محلل الروابط",IntelligenceHubActivity.class);break;
   case "network":
    header("أمان الشبكة","NETWORK SECURITY LAB");
    section("البيانات التي نقرأها","نوع الشبكة النشطة، وحالة تحقق Android من اتصال الإنترنت، وعناوين DNS عندما يوفرها النظام.");
    section("ما الذي لا نستطيع إثباته؟","حالة اتصال الإنترنت أو عنوان DNS لا يكفيان لإثبات أمان شبكة Wi-Fi. فحص الأجهزة الأخرى أو اعتراض المرور يحتاج آليات وصلاحيات إضافية.");
    section("نصائح عملية","استخدم WPA2 أو WPA3 متى أمكن، وحدّث الراوتر، ولا تثق بصفحات تسجيل دخول غريبة.");
    action("فتح تشخيص الشبكة",IntelligenceHubActivity.class);settings("إعدادات Wi-Fi",Settings.ACTION_WIFI_SETTINGS);break;
   case "battery":
    header("مختبر البطارية","POWER STATION");
    section("القياسات","مستوى الشحن، حرارة البطارية، الجهد، والتيار إذا أتاح الجهاز قراءته. حساب القدرة تقديري من قيم البطارية ولا يمثل بالضرورة قدرة الشاحن.");
    section("سجل الجلسات","يمكنك بدء جلسة شحن وإيقافها ومراجعة الجلسات المحفوظة محليًا. تختلف دقة التيار حسب الهاتف.");
    section("متى تنتبه؟","إذا ارتفعت الحرارة كثيرًا، أوقف الاستخدام المكثف أثناء الشحن وراجع الشاحن والكابل. التطبيق لا يغيّر سرعة الشحن أو تبريد الجهاز.");
    action("فتح محلل الشحن",PowerStationActivity.class);break;
   case "gaming":
    header("مختبر الألعاب","GAME PERFORMANCE LAB");
    section("المراقبة الممكنة","حرارة البطارية، ضغط الذاكرة، وتغيرات الموارد. قياس FPS الحقيقي لكل الألعاب غير متاح لتطبيق عادي دون تكامل أو صلاحيات مناسبة.");
    section("تحسين آمن","خفف التطبيقات الثقيلة، وراجع وضع الألعاب الرسمي، وراقب الحرارة قبل جلسة اللعب وبعدها.");
    section("لا وعود وهمية","لا يستطيع التطبيق رفع تردد المعالج أو فرض زيادة الإطارات أو إغلاق تطبيقات أخرى قسرًا.");
    action("فتح مختبر الموارد",ResourceLabActivity.class);action("سجل القياسات",TrendsActivity.class);break;
   case "storage":
    header("استعادة التخزين","STORAGE RECOVERY");
    section("اكتشاف التكرار","اختر مجلدًا بنفسك؛ يحسب التطبيق SHA-256 للملفات المتاحة للعثور على المحتوى المتطابق.");
    section("حماية بياناتك","لا يحذف التطبيق الملفات تلقائيًا. راجع كل نتيجة قبل اتخاذ أي قرار، واحتفظ بنسخة احتياطية من الملفات المهمة.");
    section("قيود الوصول","لا يمكنه فحص المجلدات المحمية لتطبيقات أخرى أو تنظيف بياناتها الداخلية دون صلاحية مناسبة.");
    action("فتح فاحص التكرار",StorageAuditActivity.class);settings("إدارة التخزين",Settings.ACTION_INTERNAL_STORAGE_SETTINGS);break;
   case "timeline":
    header("كبسولة الزمن","DIGITAL TIME CAPSULE");
    section("لقطة الجهاز","يحفظ خط أساس محليًا لبعض إعدادات الأمان وإصدار Android ومساحة التخزين والتطبيقات الظاهرة.");
    section("مقارنة التغييرات","يعرض الفروقات بين اللقطة الحالية والسابقة؛ تغيّر الإعداد ليس دليلًا تلقائيًا على اختراق.");
    section("الخصوصية","المقارنة محلية على الهاتف. مشاركة التقرير تتم فقط بطلبك.");
    action("فتح كبسولة الزمن",TimeCapsuleActivity.class);break;
   case "reports":
    header("مركز التقارير","REPORT STUDIO");
    section("المعلومات المتاحة","اتجاهات البطارية والحرارة والذاكرة والتخزين التي يسجلها التطبيق أثناء استخدام وظائف القياس.");
    section("قراءة الرسوم","الرسوم تعتمد على العينات المحلية المتوفرة، ولا تمثل مراقبة متواصلة لكل ثانية في الخلفية.");
    section("مشاركة النتائج","شارك التقرير النصي أو احتفظ به للمقارنة؛ راجع المعلومات قبل إرسالها إلى أي جهة.");
    action("فتح الرسوم والتقارير",TrendsActivity.class);break;
   default:
    header("مركز TITAN","FEATURE INTELLIGENCE");
    section("معلومات الوظيفة","اختر أداة من القائمة الرئيسية لعرض تفاصيلها وطريقة استخدامها وحدود عملها.");
    action("فتح مركز الذكاء",IntelligenceHubActivity.class);
  }
 }
 private void header(String name,String subtitle){root.addView(text(subtitle,12,CYAN,true));space(9);root.addView(text(name,29,WHITE,true));space(12);root.addView(text("شرح الوظيفة • طريقة الاستخدام • القيود • الإجراء",13,MUTED,false));space(22);}
}
