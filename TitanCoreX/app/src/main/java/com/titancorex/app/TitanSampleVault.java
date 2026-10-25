package com.titancorex.app;

import android.content.Context;
import android.net.Uri;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import java.io.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Opt-in encrypted APK sample vault. Stores copies of user-selected files only.
 * This cannot quarantine installed apps or remove/block the original file.
 */
public final class TitanSampleVault {
 private TitanSampleVault(){}
 private static final String KEY="titan_v3_sample_vault_aes";
 private static final long MAX_BYTES=250L*1024*1024;
 public static final class Receipt {
  public final String id,sha256;public final long bytes;
  Receipt(String id,String hash,long length){this.id=id;this.sha256=hash;this.bytes=length;}
 }
 private static synchronized SecretKey getKey()throws Exception{
  KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);
  KeyStore.Entry e=store.getEntry(KEY,null);
  if(e instanceof KeyStore.SecretKeyEntry)return ((KeyStore.SecretKeyEntry)e).getSecretKey();
  KeyGenerator gen=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
  gen.init(new KeyGenParameterSpec.Builder(KEY,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
    .setKeySize(256).setRandomizedEncryptionRequired(true).build());
  return gen.generateKey();
 }
 private static String hex(byte[] digest){
  StringBuilder s=new StringBuilder();
  for(byte b:digest)s.append(String.format(Locale.US,"%02x",b&255));return s.toString();
 }
 public static Receipt importSelected(Context context,Uri uri)throws Exception{
  if(uri==null)throw new IllegalArgumentException("Missing file");
  String id=UUID.randomUUID().toString();
  File directory=context.getDir("titan_v3_vault",Context.MODE_PRIVATE);
  File target=new File(directory,id+".tq3");
  long bytes=0;
  boolean success=false;
  try{
   Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
   cipher.init(Cipher.ENCRYPT_MODE,getKey());
   byte[] iv=cipher.getIV();
   if(iv.length!=12)throw new SecurityException("Invalid cipher IV");
   MessageDigest digest=MessageDigest.getInstance("SHA-256");
   try(InputStream in=context.getContentResolver().openInputStream(uri);
       FileOutputStream raw=new FileOutputStream(target)){
    if(in==null)throw new IOException("Cannot open selected document");
    raw.write(new byte[]{'T','Q','X','3'});raw.write(iv);
    try(CipherOutputStream encrypted=new CipherOutputStream(raw,cipher)){
     byte[] buffer=new byte[32768];int count;
     while((count=in.read(buffer))!=-1){
      bytes+=count;if(bytes>MAX_BYTES)throw new IOException("Sample is too large");
      digest.update(buffer,0,count);encrypted.write(buffer,0,count);
     }
    }
   }
   if(bytes==0)throw new IOException("Selected document is empty");
   Receipt result=new Receipt(id,hex(digest.digest()),bytes);
   boolean recorded=context.getSharedPreferences("titan_v3_vault",0).edit()
    .putString(id,result.sha256+"|"+bytes).commit();
   if(!recorded)throw new IOException("Could not save vault index");
   success=true;return result;
  }finally{if(!success)target.delete();}
 }
 public static boolean remove(Context context,String id){
  if(id==null||!id.matches("[0-9a-f-]{36}"))return false;
  File target=new File(context.getDir("titan_v3_vault",Context.MODE_PRIVATE),id+".tq3");
  if(target.exists()&&!target.delete())return false;
  context.getSharedPreferences("titan_v3_vault",0).edit().remove(id).apply();return true;
 }
 public static int clearAll(Context context){
  Set<String> ids=new HashSet<>(context.getSharedPreferences("titan_v3_vault",0).getAll().keySet());
  int removed=0;for(String id:ids)if(remove(context,id))removed++;
  return removed;
 }
 public static int count(Context context){
  return context.getSharedPreferences("titan_v3_vault",0).getAll().size();
 }
}
