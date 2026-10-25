package com.titancorex.app;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Device-bound authenticated encryption. Never export the key or reuse GCM IVs. */
public final class TitanSecureStore {
 private static final String ALIAS="titan_core_x_local_v1";
 private static final String PREFS="titan_secure_store";
 private TitanSecureStore(){}
 private static synchronized SecretKey key()throws Exception{
  KeyStore store=KeyStore.getInstance("AndroidKeyStore");store.load(null);
  KeyStore.Entry entry=store.getEntry(ALIAS,null);
  if(entry instanceof KeyStore.SecretKeyEntry)return ((KeyStore.SecretKeyEntry)entry).getSecretKey();
  KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
  generator.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
   .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
   .setKeySize(256).setRandomizedEncryptionRequired(true).build());
  return generator.generateKey();
 }
 public static void put(Context context,String name,String value)throws Exception{
  if(!name.matches("[a-zA-Z0-9_.-]{1,80}"))throw new IllegalArgumentException("Invalid key name");
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());
  cipher.updateAAD(name.getBytes(StandardCharsets.UTF_8));
  byte[] iv=cipher.getIV(),encrypted=cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
  byte[] data=new byte[1+iv.length+encrypted.length];data[0]=(byte)iv.length;
  System.arraycopy(iv,0,data,1,iv.length);System.arraycopy(encrypted,0,data,1+iv.length,encrypted.length);
  if(!context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit()
    .putString(name,Base64.encodeToString(data,Base64.NO_WRAP)).commit())throw new IllegalStateException("Secure store write failed");
 }
 public static String get(Context context,String name)throws Exception{
  String encoded=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(name,null);
  if(encoded==null)return null;
  byte[] data=Base64.decode(encoded,Base64.NO_WRAP);
  if(data.length<30)throw new SecurityException("Invalid encrypted value");
  int n=data[0]&255;if(n!=12||data.length<1+n+16)throw new SecurityException("Invalid IV");
  Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
  cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,data,1,n));
  cipher.updateAAD(name.getBytes(StandardCharsets.UTF_8));
  return new String(cipher.doFinal(data,1+n,data.length-1-n),StandardCharsets.UTF_8);
 }
 public static void remove(Context context,String name){
  context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().remove(name).apply();
 }
}
