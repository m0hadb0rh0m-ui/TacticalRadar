package com.titancorex.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import org.json.*;

/** Local, signed intelligence feed. A non-match is never a clean verdict. */
public final class ThreatEngine {
 private static final String PREF="titan_threat_feed";
 private static final String PUBLIC_KEY_B64="MCowBQYDK2VwAyEA8NVhpy5wGC403mzMDXwDSNvtlagxsxDLu9Y15M2e9pY="; // Set an audited Ed25519 public key before enabling remote feeds.
 public static boolean isSignatureConfigured(){return !PUBLIC_KEY_B64.isEmpty();}
 private ThreatEngine(){}
 public static final class Verdict {
  public final String status,reason,sha256;
  public Verdict(String s,String r,String h){status=s;reason=r;sha256=h;}
 }
 public static String hash(InputStream input)throws Exception{
  MessageDigest d=MessageDigest.getInstance("SHA-256");byte[] b=new byte[32768];int n;
  while((n=input.read(b))!=-1)d.update(b,0,n);
  StringBuilder s=new StringBuilder();for(byte x:d.digest())s.append(String.format(Locale.US,"%02x",x&255));return s.toString();
 }
 public static Verdict scan(Context c,InputStream input)throws Exception{
  String hash=hash(input);SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);
  String feed=p.getString("feed",null);
  if(feed==null)return new Verdict("UNKNOWN","لا توجد قاعدة تهديدات موثقة مثبتة بعد؛ لا يمكن الحكم بسلامة الملف.",hash);
  JSONObject db=new JSONObject(feed);JSONArray hashes=db.optJSONArray("sha256");
  if(hashes!=null)for(int i=0;i<hashes.length();i++){JSONObject entry=hashes.optJSONObject(i);if(entry!=null&&hash.equalsIgnoreCase(entry.optString("hash"))){return new Verdict("KNOWN_THREAT",entry.optString("description","مطابقة بصمة ملف مع قاعدة تهديدات موثقة"),hash);}}
  return new Verdict("NO_KNOWN_MATCH","لم تظهر مطابقة في القاعدة المحلية، لكن هذا لا يثبت سلامة الملف.",hash);
 }
 public static boolean installSignedFeed(Context c,byte[] data,byte[] signature)throws Exception{
  if(PUBLIC_KEY_B64.isEmpty())throw new SecurityException("Remote intelligence is disabled until a verified public key is configured");
  byte[] encoded=Base64.decode(PUBLIC_KEY_B64,Base64.DEFAULT);
  KeyFactory kf=KeyFactory.getInstance("Ed25519");
  PublicKey key=kf.generatePublic(new java.security.spec.X509EncodedKeySpec(encoded));
  Signature verifier=Signature.getInstance("Ed25519");verifier.initVerify(key);verifier.update(data);
  if(!verifier.verify(signature))throw new SecurityException("Invalid intelligence signature");
  JSONObject obj=new JSONObject(new String(data,StandardCharsets.UTF_8));
  if(obj.optInt("schema")!=1||!obj.has("sha256"))throw new IOException("Invalid feed schema");
  JSONArray entries=obj.getJSONArray("sha256");if(entries.length()>200000)throw new IOException("Feed too large");
  for(int i=0;i<entries.length();i++){JSONObject e=entries.getJSONObject(i);if(!e.optString("hash").matches("[0-9a-fA-F]{64}"))throw new IOException("Invalid SHA-256 entry");}
  c.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString("feed",obj.toString()).putLong("updated",System.currentTimeMillis()).apply();
  return true;
 }
 public static String feedStatus(Context c){
  SharedPreferences p=c.getSharedPreferences(PREF,Context.MODE_PRIVATE);long updated=p.getLong("updated",0);
  return updated==0?"قاعدة التهديدات غير مفعلة":"آخر تحديث موثق: "+new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(new Date(updated));
 }
}
