package com.titancorex.app;

import android.content.pm.PackageInfo;
import java.util.*;

/** Explainable static APK risk indicators. Not a malware verdict. */
public final class ApkRiskAnalyzer {
 private ApkRiskAnalyzer(){}
 public static final class Result {
  public final int score;
  public final String band,details;
  Result(int n,String b,String d){score=n;band=b;details=d;}
 }
 public static Result evaluate(PackageInfo pkg,boolean manifest,int duplicateNames,int dexCount,int nativeCount,int suspiciousZipEntries){
  int score=0;StringBuilder details=new StringBuilder();
  if(!manifest){score+=35;details.append("• ملف AndroidManifest.xml مفقود.\n");}
  if(duplicateNames>0){score+=20;details.append("• أسماء ملفات متكررة داخل ZIP: ").append(duplicateNames).append("\n");}
  if(suspiciousZipEntries>0){score+=25;details.append("• مكونات ZIP ذات نسب ضغط غير معتادة: ").append(suspiciousZipEntries).append("\n");}
  if(dexCount==0){score+=20;details.append("• لم يتم العثور على classes.dex؛ قد لا يكون APK صالحًا.\n");}
  if(dexCount>8){score+=5;details.append("• عدد كبير من ملفات DEX؛ قد يكون طبيعيًا في التطبيقات الكبيرة.\n");}
  if(nativeCount>40){score+=5;details.append("• عدد كبير من المكتبات الأصلية؛ راجع المصدر.\n");}
  Set<String> p=new HashSet<>();
  if(pkg!=null&&pkg.requestedPermissions!=null)Collections.addAll(p,pkg.requestedPermissions);
  boolean sms=p.contains("android.permission.READ_SMS")||p.contains("android.permission.RECEIVE_SMS");
  boolean contacts=p.contains("android.permission.READ_CONTACTS");
  boolean overlay=p.contains("android.permission.SYSTEM_ALERT_WINDOW");
  boolean accessibility=p.contains("android.permission.BIND_ACCESSIBILITY_SERVICE");
  boolean install=p.contains("android.permission.REQUEST_INSTALL_PACKAGES");
  boolean allFiles=p.contains("android.permission.MANAGE_EXTERNAL_STORAGE");
  boolean boot=p.contains("android.permission.RECEIVE_BOOT_COMPLETED");
  if(sms&&contacts){score+=15;details.append("• الجمع بين قراءة الرسائل وجهات الاتصال يستحق مراجعة وظيفة التطبيق.\n");}
  if(overlay&&sms){score+=15;details.append("• الجمع بين النوافذ العائمة والوصول للرسائل قد يزيد مخاطر التصيد.\n");}
  if(install&&overlay){score+=15;details.append("• الجمع بين طلب تثبيت حزم وظهور نوافذ فوق التطبيقات.\n");}
  if(allFiles){score+=10;details.append("• يطلب الوصول الواسع إلى الملفات.\n");}
  if(accessibility){score+=10;details.append("• يعلن صلاحية مرتبطة بخدمات إمكانية الوصول؛ راجع استخدامه الفعلي.\n");}
  if(boot&&(sms||overlay)){score+=5;details.append("• بدء التشغيل مع أذونات حساسة أخرى.\n");}
  score=Math.min(100,score);
  String band=score>=60?"مؤشرات مرتفعة":score>=30?"مؤشرات متوسطة":score>0?"مؤشرات محدودة":"لم تُرصد المؤشرات المحددة";
  if(details.length()==0)details.append("• لم يُكتشف أي مؤشر من القواعد الحالية، لكن ذلك لا يثبت سلامة الملف.\n");
  details.append("هذه درجة استرشادية غير معايرة إحصائيًا. الأذونات وحدها لا تثبت وجود برمجية خبيثة، وقد تتطلب وظائف مشروعة هذه الصلاحيات.");
  return new Result(score,band,details.toString());
 }
}
