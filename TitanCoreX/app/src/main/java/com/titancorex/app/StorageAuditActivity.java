package com.titancorex.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

public class StorageAuditActivity extends Activity {
    private static final int PICK=100;
    private LinearLayout root, results;
    private TextView status;
    private final ArrayList<Item> items=new ArrayList<>();
    private static class Item {Uri uri;String name;long size;String hash;Item(Uri u,String n,long s){uri=u;name=n;size=s;}}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView text(String value,int size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setPadding(dp(4),dp(10),dp(4),dp(10));return t;}
    private void line(String s){results.addView(text(s,14,0xff395676));}
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(0xffF1F8FF);
        ScrollView scroll=new ScrollView(this);
        root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(22),dp(20),dp(20));root.setBackgroundColor(0xffF1F8FF);
        scroll.addView(root);setContentView(scroll);
        root.addView(text("TITAN STORAGE LAB",25,0xff42E8F5));
        root.addView(text("فحص الملفات المكررة — بإذن المستخدم فقط",16,Color.WHITE));
        root.addView(text("اختر مجلدًا؛ سيُحسب بصمة SHA-256 للملفات المتساوية بالحجم لتحديد التطابق الحقيقي. لا يحذف التطبيق أي ملف تلقائيًا.",13,0xffB1C3D6));
        Button select=new Button(this);select.setText("اختيار مجلد للفحص");root.addView(select);
        select.setOnClickListener(v->{
            Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent,PICK);
        });
        status=text("لم يبدأ الفحص بعد",14,0xffFFCE77);root.addView(status);
        results=new LinearLayout(this);results.setOrientation(1);root.addView(results);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=PICK||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri tree=data.getData();
        try{getContentResolver().takePersistableUriPermission(tree,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
        status.setText("جارٍ فحص المجلد المحدد...");
        results.removeAllViews();
        new Thread(()->scan(tree)).start();
    }
    private void scan(Uri tree){
        ArrayList<Item> found=new ArrayList<>();
        String error=null;
        try{
            Uri parent=DocumentsContract.buildDocumentUriUsingTree(tree,DocumentsContract.getTreeDocumentId(tree));
            walk(tree,parent,found,0);
            Map<Long,List<Item>> bySize=new HashMap<>();
            for(Item i:found)if(i.size>0){if(!bySize.containsKey(i.size))bySize.put(i.size,new ArrayList<>());bySize.get(i.size).add(i);}
            Map<String,List<Item>> dup=new LinkedHashMap<>();
            for(List<Item> group:bySize.values())if(group.size()>1){
                for(Item i:group){
                    try{MessageDigest digest=MessageDigest.getInstance("SHA-256");
                        try(InputStream in=getContentResolver().openInputStream(i.uri)){
                            if(in==null)continue;
                            byte[] buffer=new byte[32768];int n;
                            while((n=in.read(buffer))!=-1)digest.update(buffer,0,n);
                        }
                        StringBuilder key=new StringBuilder();
                        for(byte x:digest.digest())key.append(String.format(java.util.Locale.US,"%02x",x&255));
                        i.hash=key.toString();
                        if(!dup.containsKey(i.hash))dup.put(i.hash,new ArrayList<>());
                        dup.get(i.hash).add(i);
                    }catch(Exception ignored){}
                }
            }
            int sets=0;long excess=0;
            ArrayList<String> report=new ArrayList<>();
            for(List<Item> group:dup.values())if(group.size()>1){
                sets++;excess+=(group.size()-1)*group.get(0).size;
                report.add("مجموعة "+sets+" — "+group.size()+" ملفات متطابقة");
                for(Item i:group)report.add("• "+i.name+" ("+(i.size/1024)+" KB)");
            }
            final int total=found.size(),count=sets;final long bytes=excess;
            runOnUiThread(()->{
                status.setText("اكتمل الفحص: "+total+" ملف | "+count+" مجموعة مكررة");
                line("مساحة يمكن مراجعتها: "+String.format(java.util.Locale.US,"%.2f MB",bytes/1048576.0));
                if(report.isEmpty())line("لم يُعثر على ملفات مكررة ضمن نطاق الفحص.");
                for(String s:report)line(s);
                line("لا يتم الحذف تلقائيًا. راجع الملفات من مدير الملفات قبل اتخاذ القرار.");
            });
        }catch(Exception e){error=e.getMessage();}
        if(error!=null){final String msg=error;runOnUiThread(()->status.setText("تعذر الفحص: "+msg));}
    }
    private void walk(Uri tree,Uri folder,List<Item> out,int depth) throws Exception{
        if(depth>15||out.size()>3000)return;
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(tree,DocumentsContract.getDocumentId(folder));
        try(android.database.Cursor c=getContentResolver().query(children,
            new String[]{DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_SIZE},null,null,null)){
            if(c==null)return;
            while(c.moveToNext()&&out.size()<3000){
                String id=c.getString(0),name=c.getString(1),mime=c.getString(2);
                long size=c.isNull(3)?0:c.getLong(3);
                Uri doc=DocumentsContract.buildDocumentUriUsingTree(tree,id);
                if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime))walk(tree,doc,out,depth+1);
                else out.add(new Item(doc,name,size));
            }
        }
    }
}
