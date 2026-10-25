package com.titancorex.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;

public class PrivacyAuditActivity extends Activity {
    private LinearLayout results;
    private EditText search;
    private final ArrayList<AppEntry> entries=new ArrayList<>();
    private final int BG=0xffF8F4FF, CARD=0xffFFFFFF, CYAN=0xff8048D5, WHITE=0xff243250, MUTED=0xff5E6B86, GOLD=0xffA66B00;
    private static class AppEntry {String name, pkg;ArrayList<String> permissions=new ArrayList<>();int risk;boolean system;}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(3),dp(6),dp(3),dp(6));return v;}
    private GradientDrawable background(){GradientDrawable d=new GradientDrawable();d.setColor(CARD);d.setCornerRadius(dp(20));d.setStroke(dp(1),0xff7250A0);return d;}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        ScrollView scroll=new ScrollView(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setPadding(dp(18),dp(22),dp(18),dp(22));root.setBackgroundColor(BG);scroll.addView(root);setContentView(scroll);
        root.addView(new TitanCyberSceneView(this,CYAN),new LinearLayout.LayoutParams(-1,dp(170)));
        TextView heading=text("TITAN PRIVACY LAB",25,CYAN);heading.setTypeface(android.graphics.Typeface.create("sans-serif-black",1));root.addView(heading);
        root.addView(text("تدقيق الأذونات للتطبيقات الظاهرة",17,WHITE));
        root.addView(text("نراجع الأذونات الحساسة المطلوبة في ملفات التطبيقات، ونوضح ما إذا كانت ممنوحة. وجود إذن لا يعني أن التطبيق خبيث. نتائج الفحص مقتصرة على التطبيقات التي يسمح Android برؤيتها.",13,MUTED));
        search=new EditText(this);search.setSingleLine(true);search.setHint("ابحث باسم التطبيق أو الحزمة");search.setHintTextColor(MUTED);search.setTextColor(WHITE);root.addView(search);
        Button scan=new Button(this);scan.setText("فحص الأذونات الآن");root.addView(scan);
        TextView status=text("جاهز للفحص",13,GOLD);root.addView(status);
        results=new LinearLayout(this);results.setOrientation(1);root.addView(results);
        scan.setOnClickListener(v->{scanApps();status.setText("التطبيقات الظاهرة: "+entries.size()+" — التقييم إرشادي فقط");render();});
        search.addTextChangedListener(new android.text.TextWatcher(){
            public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            public void onTextChanged(CharSequence s,int start,int before,int count){render();}
            public void afterTextChanged(android.text.Editable e){}
        });
        scanApps();status.setText("التطبيقات الظاهرة: "+entries.size()+" — فحص محلي دون رفع بيانات");render();
    }
    private String nice(String p){
        if(p.endsWith("CAMERA"))return "الكاميرا";
        if(p.endsWith("RECORD_AUDIO"))return "الميكروفون";
        if(p.contains("LOCATION"))return "الموقع";
        if(p.contains("CONTACTS"))return "جهات الاتصال";
        if(p.contains("SMS"))return "الرسائل";
        if(p.contains("CALL_LOG"))return "سجل المكالمات";
        if(p.contains("PHONE"))return "الهاتف";
        if(p.contains("CALENDAR"))return "التقويم";
        if(p.contains("STORAGE")||p.contains("MEDIA")||p.contains("IMAGES")||p.contains("VIDEO")||p.contains("AUDIO"))return "الملفات والوسائط";
        if(p.contains("BODY_SENSORS"))return "حساسات الجسم";
        return null;
    }
    private void scanApps(){
        entries.clear();PackageManager pm=getPackageManager();
        Intent intent=new Intent(Intent.ACTION_MAIN);intent.addCategory(Intent.CATEGORY_LAUNCHER);
        HashSet<String> seen=new HashSet<>();
        for(ResolveInfo info:pm.queryIntentActivities(intent,0)){
            if(info.activityInfo==null)continue;
            String pkg=info.activityInfo.packageName;
            if(!seen.add(pkg))continue;
            try{
                PackageInfo pi=pm.getPackageInfo(pkg,PackageManager.GET_PERMISSIONS);
                AppEntry e=new AppEntry();e.pkg=pkg;e.name=String.valueOf(pm.getApplicationLabel(pi.applicationInfo));
                e.system=(pi.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM)!=0;
                if(pi.requestedPermissions!=null)for(int i=0;i<pi.requestedPermissions.length;i++){
                    String p=pi.requestedPermissions[i],friendly=nice(p);
                    if(friendly==null)continue;
                    boolean granted=pi.requestedPermissionsFlags!=null && i<pi.requestedPermissionsFlags.length &&
                        (pi.requestedPermissionsFlags[i]&PackageInfo.REQUESTED_PERMISSION_GRANTED)!=0;
                    e.permissions.add(friendly+(granted?" ✓ ممنوح":" ○ غير ممنوح"));
                    if(granted)e.risk++;
                }
                entries.add(e);
            }catch(Exception ignored){}
        }
        Collections.sort(entries,(a,b)->Integer.compare(b.risk,a.risk));
    }
    private void render(){
        if(results==null||search==null)return;
        results.removeAllViews();String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);
        int shown=0;
        for(AppEntry e:entries){
            if(!e.name.toLowerCase(Locale.ROOT).contains(q)&&!e.pkg.toLowerCase(Locale.ROOT).contains(q))continue;
            if(shown++>=100){results.addView(text("تم عرض أول 100 تطبيق فقط. استخدم البحث لتضييق النتائج.",12,GOLD));break;}
            LinearLayout c=new LinearLayout(this);c.setOrientation(1);c.setPadding(dp(14),dp(12),dp(14),dp(12));c.setBackground(background());
            c.addView(text(e.name,16,WHITE));c.addView(text(e.pkg,11,MUTED));
            c.addView(text("أذونات حساسة ممنوحة: "+e.risk+" | "+(e.system?"تطبيق نظام":"تطبيق مستخدم"),12,e.risk>3?GOLD:CYAN));
            if(e.permissions.isEmpty())c.addView(text("لا تظهر أذونات حساسة في البيانات المتاحة",12,MUTED));
            else for(String p:e.permissions)c.addView(text("• "+p,12,MUTED));
            c.setOnClickListener(v->{
                try{Intent i=new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.setData(android.net.Uri.parse("package:"+e.pkg));startActivity(i);
                }catch(Exception ignored){}
            });
            results.addView(c);View gap=new View(this);results.addView(gap,new LinearLayout.LayoutParams(1,dp(11)));
        }
    }
}
