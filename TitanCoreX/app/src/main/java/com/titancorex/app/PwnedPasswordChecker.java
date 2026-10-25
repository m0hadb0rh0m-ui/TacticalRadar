package com.titancorex.app;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;

/**
 * Optional HIBP Pwned Passwords k-anonymity lookup.
 * Sends only the FIRST FIVE hexadecimal SHA-1 hash characters over HTTPS.
 * It never transmits plaintext, the full hash, an email address, or the suffix.
 */
public final class PwnedPasswordChecker {
 private PwnedPasswordChecker(){}
 public static final class Result{
  public final long occurrences;
  public Result(long n){occurrences=n;}
 }
 public static Result check(char[] password)throws Exception{
  if(password==null||password.length==0||password.length>1024)throw new IllegalArgumentException("Invalid password length");
  ByteBuffer utf8=null;
  String hash;
  try{
   utf8=StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
   MessageDigest md=MessageDigest.getInstance("SHA-1");
   md.update(utf8.asReadOnlyBuffer());
   byte[] digest=md.digest();
   StringBuilder sb=new StringBuilder(40);
   for(byte part:digest)sb.append(String.format(Locale.US,"%02X",part&255));
   hash=sb.toString();
   Arrays.fill(digest,(byte)0);
  }finally{
   Arrays.fill(password,'\u0000');
   if(utf8!=null)for(int i=0;i<utf8.limit();i++)utf8.put(i,(byte)0);
  }
  String prefix=hash.substring(0,5),suffix=hash.substring(5);
  URL endpoint=new URL("https://api.pwnedpasswords.com/range/"+prefix);
  HttpsURLConnection conn=(HttpsURLConnection)endpoint.openConnection();
  conn.setRequestMethod("GET");
  conn.setConnectTimeout(7000);conn.setReadTimeout(11000);
  conn.setUseCaches(false);conn.setInstanceFollowRedirects(false);
  conn.setRequestProperty("User-Agent","TitanCoreX/3.0 (privacy-preserving breach check)");
  conn.setRequestProperty("Add-Padding","true");
  try{
   int code=conn.getResponseCode();
   if(code!=200)throw new java.io.IOException("HIBP HTTP "+code);
   int lines=0,chars=0;long occurrences=0;
   try(BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream(),StandardCharsets.US_ASCII))){
    String line;
    while((line=reader.readLine())!=null){
     chars+=line.length();if(chars>180000||++lines>2500)throw new java.io.IOException("Unexpected range response size");
     int sep=line.indexOf(':');
     if(sep!=35)continue;
     if(line.regionMatches(true,0,suffix,0,35)){
      long count=Long.parseLong(line.substring(sep+1).trim());
      occurrences=Math.max(0,count);
      break;
     }
    }
   }
   return new Result(occurrences);
  }finally{conn.disconnect();}
 }
}
