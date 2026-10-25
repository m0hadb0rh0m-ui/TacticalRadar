package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.net.VpnService;
import android.os.*;
import android.view.*;
import android.widget.*;

public class DnsFirewallActivity extends Activity {
 private static final int VPN_REQUEST=401;
 private EditText domains;private TextView status;
 @Override public void onCreate(Bundle b){super.onCreate(b);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(24,28,24,24);root.setBackgroundColor(0xff080D18);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  ScrollView sc=new ScrollView(this);sc.addView(root);setContentView(sc);
  TextView title=new TextView(this);title.setText("TITAN DNS FIREWALL");title.setTextColor(0xff5CE5EF);title.setTextSize(24);root.addView(title);
  TextView description=new TextView(this);description.setText("حماية DNS تجريبية: تمرير طلبات DNS العادية عبر DNS-over-HTTPS مشفر إلى Cloudflare، وإرجاع NXDOMAIN للنطاقات المدرجة أدناه. لا تفك تشفير HTTPS ولا تعترض DoH الخاص بالتطبيقات أو DNS عبر IPv6. يتطلب التشغيل موافقتك على VPN.");description.setTextColor(-1);description.setTextSize(15);root.addView(description);
  domains=new EditText(this);domains.setTextColor(-1);domains.setHint("نطاق واحد في كل سطر");domains.setMinLines(6);
  domains.setText(getSharedPreferences("titan_dns_vpn",MODE_PRIVATE).getString("domains",""));root.addView(domains);
  Button save=new Button(this);save.setText("حفظ قائمة الحجب");root.addView(save);
  save.setOnClickListener(v->{String input=domains.getText().toString().toLowerCase(java.util.Locale.US);StringBuilder valid=new StringBuilder();
   for(String line:input.split("\\n")){String d=line.trim();if(d.isEmpty())continue;
    if(d.length()>253||!d.matches("[a-z0-9](?:[a-z0-9.-]*[a-z0-9])?")||d.contains("..")||!d.contains(".")){Toast.makeText(this,"نطاق غير صالح: "+d,Toast.LENGTH_LONG).show();return;}
    valid.append(d).append("\n");
   }
   getSharedPreferences("titan_dns_vpn",MODE_PRIVATE).edit().putString("domains",valid.toString()).apply();Toast.makeText(this,"تم حفظ قائمة الحجب",Toast.LENGTH_SHORT).show();refresh();
  });
  Button update=new Button(this);update.setText("تحديث قاعدة النطاقات الآن");root.addView(update);
  update.setOnClickListener(v->{status.setText("جارٍ تنزيل قاعدة الحجب...");new Thread(()->{
   String result;try{result=DomainFeedJob.update(this);DomainFeedJob.schedule(this);}catch(Exception e){result="تعذر التحديث: "+e.getMessage();}
   final String message=result;runOnUiThread(()->{refresh();status.append("\\n"+message);});
  }).start();});
  Button automatic=new Button(this);automatic.setText("جدولة التحديث اليومي عبر Wi-Fi");root.addView(automatic);
  automatic.setOnClickListener(v->{DomainFeedJob.schedule(this);Toast.makeText(this,"تمت جدولة التحديث",Toast.LENGTH_SHORT).show();});
  Button start=new Button(this);start.setText("تشغيل حماية DNS — طلب إذن VPN");root.addView(start);start.setOnClickListener(v->{
   Intent approval=VpnService.prepare(this);
   if(approval!=null)startActivityForResult(approval,VPN_REQUEST);else startShield();
  });
  Button stop=new Button(this);stop.setText("إيقاف VPN");root.addView(stop);stop.setOnClickListener(v->{Intent i=new Intent(this,TitanDnsVpnService.class);i.setAction(TitanDnsVpnService.STOP);startService(i);refresh();});
  status=new TextView(this);status.setTextColor(-1);status.setTextSize(15);root.addView(status);refresh();
 }
 private void startShield(){Intent i=new Intent(this,TitanDnsVpnService.class);if(Build.VERSION.SDK_INT>=26)startForegroundService(i);else startService(i);refresh();}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==VPN_REQUEST&&result==RESULT_OK)startShield();}
 private void refresh(){android.content.SharedPreferences p=getSharedPreferences("titan_dns_vpn",MODE_PRIVATE);android.content.SharedPreferences feed=getSharedPreferences("titan_dns_feed",MODE_PRIVATE);status.setText("قاعدة النطاقات: "+feed.getInt("count",0)+" نطاق | آخر تحديث: "+new java.util.Date(feed.getLong("updated",0))+"\\nحالة الخدمة المسجلة: "+(p.getBoolean("active",false)?"نشطة":"متوقفة")+"\nآخر خطأ: "+p.getString("error","لا يوجد")+"\nتنبيه: قد لا تستخدم بعض التطبيقات DNS النظام، وقد لا تعمل هذه النسخة على جميع الأجهزة.");}
 @Override protected void onResume(){super.onResume();if(status!=null)refresh();}
}
