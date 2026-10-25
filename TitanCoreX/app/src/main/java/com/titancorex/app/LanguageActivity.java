package com.titancorex.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class LanguageActivity extends Activity {
  private final String[] codes={"ar","en","fr","es","de","pt","it","tr","ru","zh","ja","ko","hi","id","ur","fa","bn","nl","sv","pl"};
  private final String[] names={"العربية","English","Français","Español","Deutsch","Português","Italiano","Türkçe","Русский","简体中文","日本語","한국어","हिन्दी","Bahasa Indonesia","اردو","فارسی","বাংলা","Nederlands","Svenska","Polski"};
  private final String[] titles={"اللغة والمظهر","Language & appearance","Langue et apparence","Idioma y apariencia","Sprache und Design","Idioma e aparência","Lingua e aspetto","Dil ve görünüm","Язык и оформление","语言与外观","言語と外観","언어 및 디자인","भाषा और रूप","Bahasa dan tampilan","زبان اور ظاہری شکل","زبان و ظاهر","ভাষা ও চেহারা","Taal en uiterlijk","Språk och utseende","Język i wygląd"};
  private LinearLayout list;private TextView current;
  private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
  private TextView text(String s,int size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setPadding(dp(10),dp(10),dp(10),dp(10));return v;}
  private GradientDrawable card(){GradientDrawable d=new GradientDrawable();d.setColor(0xff15243A);d.setCornerRadius(dp(14));d.setStroke(dp(1),0xff30445C);return d;}
  public static String tr(Context c,String key){
    String lang=c.getSharedPreferences("titan_settings",0).getString("language","ar");
    String[] keys={"home","device","security","games","tools","language","resource","health","battery","storage","ram"};
    String[][] words={
      {"الرئيسية","الجهاز","الأمان","الألعاب","الأدوات","اللغة والمظهر","مختبر الموارد","صحة الجهاز","البطارية","التخزين","الرام"},
      {"Home","Device","Security","Gaming","Tools","Language & appearance","Resource Lab","Device health","Battery","Storage","RAM"},
      {"Accueil","Appareil","Sécurité","Jeux","Outils","Langue et apparence","Laboratoire des ressources","État de l'appareil","Batterie","Stockage","RAM"},
      {"Inicio","Dispositivo","Seguridad","Juegos","Herramientas","Idioma y apariencia","Laboratorio de recursos","Estado del dispositivo","Batería","Almacenamiento","RAM"},
      {"Start","Gerät","Sicherheit","Spiele","Werkzeuge","Sprache und Design","Ressourcenlabor","Gerätestatus","Akku","Speicher","RAM"},
      {"Início","Dispositivo","Segurança","Jogos","Ferramentas","Idioma e aparência","Laboratório de recursos","Saúde do dispositivo","Bateria","Armazenamento","RAM"},
      {"Home","Dispositivo","Sicurezza","Giochi","Strumenti","Lingua e aspetto","Laboratorio risorse","Stato dispositivo","Batteria","Archiviazione","RAM"},
      {"Ana Sayfa","Cihaz","Güvenlik","Oyunlar","Araçlar","Dil ve görünüm","Kaynak laboratuvarı","Cihaz sağlığı","Pil","Depolama","RAM"},
      {"Главная","Устройство","Безопасность","Игры","Инструменты","Язык и оформление","Ресурсы","Состояние устройства","Батарея","Память","ОЗУ"},
      {"首页","设备","安全","游戏","工具","语言与外观","资源实验室","设备健康","电池","存储","内存"},
      {"ホーム","端末","セキュリティ","ゲーム","ツール","言語と外観","リソース分析","端末の状態","バッテリー","ストレージ","RAM"},
      {"홈","기기","보안","게임","도구","언어 및 디자인","리소스 연구소","기기 상태","배터리","저장 공간","RAM"},
      {"होम","डिवाइस","सुरक्षा","गेमिंग","टूल्स","भाषा और रूप","संसाधन लैब","डिवाइस स्वास्थ्य","बैटरी","स्टोरेज","रैम"},
      {"Beranda","Perangkat","Keamanan","Game","Alat","Bahasa dan tampilan","Lab sumber daya","Kesehatan perangkat","Baterai","Penyimpanan","RAM"},
      {"ہوم","ڈیوائس","سیکیورٹی","گیمنگ","ٹولز","زبان اور ظاہری شکل","وسائل لیب","ڈیوائس کی حالت","بیٹری","اسٹوریج","ریم"},
      {"خانه","دستگاه","امنیت","بازی","ابزارها","زبان و ظاهر","آزمایشگاه منابع","سلامت دستگاه","باتری","فضای ذخیره","رم"},
      {"হোম","ডিভাইস","নিরাপত্তা","গেম","টুলস","ভাষা ও চেহারা","রিসোর্স ল্যাব","ডিভাইস স্বাস্থ্য","ব্যাটারি","স্টোরেজ","র‍্যাম"},
      {"Start","Apparaat","Beveiliging","Games","Tools","Taal en uiterlijk","Resource-lab","Apparaatstatus","Batterij","Opslag","RAM"},
      {"Hem","Enhet","Säkerhet","Spel","Verktyg","Språk och utseende","Resurslabb","Enhetshälsa","Batteri","Lagring","RAM"},
      {"Start","Urządzenie","Bezpieczeństwo","Gry","Narzędzia","Język i wygląd","Laboratorium zasobów","Stan urządzenia","Bateria","Pamięć","RAM"}
    };
    int row=Arrays.asList(new String[]{"ar","en","fr","es","de","pt","it","tr","ru","zh","ja","ko","hi","id","ur","fa","bn","nl","sv","pl"}).indexOf(lang);
    int col=Arrays.asList(keys).indexOf(key);
    return row>=0&&col>=0?words[row][col]:key;
  }
  @Override public void onCreate(Bundle b){
    super.onCreate(b);getWindow().setStatusBarColor(0xff080C16);getWindow().setNavigationBarColor(0xff080C16);
    ScrollView scroll=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(1);
    root.setPadding(dp(17),dp(22),dp(17),dp(22));root.setBackgroundColor(0xff080C16);scroll.addView(root);setContentView(scroll);
    root.addView(text("◈ TITAN CORE X",25,0xff42E8F5));
    root.addView(text("LANGUAGE & APPEARANCE",17,0xffF4F8FF));
    root.addView(text("اختر لغة الواجهة الأساسية. الترجمة الحالية تشمل التنقل والعناوين الرئيسية؛ بعض الشاشات التقنية ما زالت بالعربية وسيتم توسيع الترجمة لاحقًا.",13,0xffB6C5D6));
    current=text("",15,0xffFFCE77);root.addView(current);
    list=new LinearLayout(this);list.setOrientation(1);root.addView(list);render();
  }
  private void render(){
    list.removeAllViews();String selected=getSharedPreferences("titan_settings",0).getString("language","ar");
    current.setText("Selected: "+selected.toUpperCase(Locale.ROOT)+"  •  "+tr(this,"language"));
    for(int i=0;i<codes.length;i++){
      final int idx=i;String code=codes[i];
      TextView row=text((selected.equals(code)?"✓  ":"◯  ")+names[i]+"  —  "+titles[i],15,selected.equals(code)?0xff42E8F5:0xffF4F8FF);
      row.setBackground(card());list.addView(row);
      View gap=new View(this);list.addView(gap,new LinearLayout.LayoutParams(1,dp(7)));
      row.setOnClickListener(v->{
        getSharedPreferences("titan_settings",0).edit().putString("language",codes[idx]).apply();
        render();Toast.makeText(this,"Language: "+names[idx],Toast.LENGTH_SHORT).show();
      });
    }
  }
}
