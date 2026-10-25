package com.titancorex.app;

import android.content.*;
import android.content.pm.*;
import android.os.Build;
import java.security.*;
import java.security.cert.Certificate;
import java.util.*;

/** Self-check signal; not a replacement for server-side Play Integrity attestation. */
public final class TitanAppIntegrity {
 private TitanAppIntegrity(){}
 public static final class Result{
  public final boolean packageOk,signatureConfigured,signatureOk,debuggerAttached;
  public final String fingerprint;
  Result(boolean p,boolean c,boolean s,boolean d,String f){packageOk=p;signatureConfigured=c;signatureOk=s;debuggerAttached=d;fingerprint=f;}
 }
 public static Result inspect(Context context)throws Exception{
  String pkg=context.getPackageName();
  PackageManager pm=context.getPackageManager();
  PackageInfo info;
  if(Build.VERSION.SDK_INT>=28)info=pm.getPackageInfo(pkg,PackageManager.GET_SIGNING_CERTIFICATES);
  else info=pm.getPackageInfo(pkg,PackageManager.GET_SIGNATURES);
  android.content.pm.Signature[] signatures;
  if(Build.VERSION.SDK_INT>=28)signatures=info.signingInfo.hasMultipleSigners()?
   info.signingInfo.getApkContentsSigners():info.signingInfo.getSigningCertificateHistory();
  else signatures=info.signatures;
  if(signatures==null||signatures.length==0)throw new SecurityException("No signing certificates");
  MessageDigest digest=MessageDigest.getInstance("SHA-256");
  Set<String> observed=new HashSet<>();
  for(android.content.pm.Signature signature:signatures){
   byte[] bytes=digest.digest(signature.toByteArray());StringBuilder hex=new StringBuilder();
   for(byte v:bytes)hex.append(String.format(Locale.US,"%02x",v&255));observed.add(hex.toString());
  }
  String expected=BuildConfig.EXPECTED_SIGNING_SHA256.trim().toLowerCase(Locale.US).replace(":","");
  boolean configured=expected.matches("[0-9a-f]{64}");
  return new Result("com.titancorex.app".equals(pkg),configured,configured&&observed.contains(expected),
   android.os.Debug.isDebuggerConnected()||android.os.Debug.waitingForDebugger(),observed.iterator().next());
 }
}
