package com.titancorex.app;

import android.app.*;
import android.content.*;
import android.net.VpnService;
import android.os.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Opt-in DNS-only VPN. Routes ONLY the virtual resolver /32, never a default route.
 * DNS queries not matching the blocklist are forwarded over protected UDP sockets.
 * Does not inspect HTTPS, IPv6 DNS, DoH, or arbitrary app traffic.
 */
public class TitanDnsVpnService extends VpnService implements Runnable {
 public static final String STOP="com.titancorex.app.STOP_DNS";
 private static final String DNS_IP="10.77.0.1";
 private volatile boolean running;
 private volatile Set<String> feedDomains=Collections.emptySet();
 private long feedUpdated=-1;
 private ParcelFileDescriptor tun;private Thread worker;
 private static int u16(byte[] b,int i){return ((b[i]&255)<<8)|(b[i+1]&255);}
 private static void put16(byte[] b,int i,int x){b[i]=(byte)(x>>>8);b[i+1]=(byte)x;}
 private static int checksum(byte[] b,int start,int length){
  long sum=0;for(int i=0;i<length;i+=2){sum+=((b[start+i]&255)<<8)|(i+1<length?(b[start+i+1]&255):0);sum=(sum&65535)+(sum>>>16);}
  return (int)(~sum)&65535;
 }
 @Override public int onStartCommand(Intent intent,int flags,int id){
  if(intent!=null&&STOP.equals(intent.getAction())){stopSelf();return START_NOT_STICKY;}
  if(running)return START_STICKY;
  if(VpnService.prepare(this)!=null){stopSelf();return START_NOT_STICKY;}
  try{
   if(Build.VERSION.SDK_INT>=26){
    NotificationChannel channel=new NotificationChannel("titan_dns_shield","TITAN DNS Shield",NotificationManager.IMPORTANCE_LOW);
    ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
   }
   Notification.Builder notification=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"titan_dns_shield"):new Notification.Builder(this);
   notification.setSmallIcon(android.R.drawable.ic_lock_lock).setContentTitle("TITAN DNS Shield").setContentText("حماية DNS قيد التشغيل").setOngoing(true);
   startForeground(771210,notification.build());
   tun=new Builder().setSession("TITAN DNS Shield").addAddress("10.77.0.2",32)
    .addDnsServer(DNS_IP).addRoute(DNS_IP,32).setMtu(1500).establish();
   if(tun==null)throw new IOException("VPN permission or TUN unavailable");
   running=true;worker=new Thread(this,"TitanDnsTunnel");worker.start();
   getSharedPreferences("titan_dns_vpn",MODE_PRIVATE).edit().putBoolean("active",true).apply();
  }catch(Exception e){getSharedPreferences("titan_dns_vpn",MODE_PRIVATE).edit().putString("error",e.toString()).apply();stopSelf();}
  return START_STICKY;
 }
 @Override public void run(){
  try(FileInputStream input=new FileInputStream(tun.getFileDescriptor());FileOutputStream output=new FileOutputStream(tun.getFileDescriptor())){
   byte[] buffer=new byte[1600];int n;
   while(running&&(n=input.read(buffer))>0){
    byte[] request=Arrays.copyOf(buffer,n);
    if(n<29||(request[0]>>>4&15)!=4)continue;
    int ihl=(request[0]&15)*4;if(ihl<20||n<ihl+8||(request[9]&255)!=17)continue;
    int frag=u16(request,6);if((frag&0x3fff)!=0)continue;
    int total=u16(request,2);if(total>n||total<ihl+8)continue;
    int udp=ihl;int sourcePort=u16(request,udp),destinationPort=u16(request,udp+2);
    int len=u16(request,udp+4);if(destinationPort!=53||len<20||udp+len>total)continue;
    int dnsStart=udp+8,dnsLength=len-8;
    byte[] query=Arrays.copyOfRange(request,dnsStart,dnsStart+dnsLength);
    String domain=qname(query);
    byte[] answer;
    if(domain!=null&&blocked(domain)){answer=nxdomain(query);}
    else{answer=forward(query);}
    if(answer==null||answer.length>1400)continue;
    byte[] response=packet(request,ihl,sourcePort,answer);
    synchronized(output){output.write(response);}
   }
  }catch(Exception e){getSharedPreferences("titan_dns_vpn",MODE_PRIVATE).edit().putString("error",e.toString()).apply();}
  finally{running=false;stopSelf();}
 }
 private String qname(byte[] q){
  if(q.length<17||u16(q,4)!=1)return null;
  StringBuilder s=new StringBuilder();int pos=12;int labels=0;
  while(pos<q.length&&labels++<60){
   int l=q[pos++]&255;if(l==0)return pos+4<=q.length?s.toString().toLowerCase(Locale.US):null;
   if(l>63||pos+l>q.length)return null;
   if(s.length()>0)s.append('.');
   for(int i=0;i<l;i++){int ch=q[pos++]&255;if(ch<33||ch>126)return null;s.append((char)ch);}
  }
  return null;
 }
 private boolean blocked(String domain){
  android.content.SharedPreferences feed=getSharedPreferences("titan_dns_feed",MODE_PRIVATE);
  long updated=feed.getLong("updated",0);
  if(updated!=feedUpdated){HashSet<String> loaded=new HashSet<>();for(String item:feed.getString("domains","").split("\\n"))if(!item.isEmpty())loaded.add(item);feedDomains=loaded;feedUpdated=updated;}
  String candidate=domain;
  while(true){if(feedDomains.contains(candidate))return true;int dot=candidate.indexOf('.');if(dot<0)break;candidate=candidate.substring(dot+1);}

  String raw=getSharedPreferences("titan_dns_vpn",MODE_PRIVATE).getString("domains","");
  for(String line:raw.split("\\n")){
   String d=line.trim().toLowerCase(Locale.US);
   if(d.length()>3&&(domain.equals(d)||domain.endsWith("."+d)))return true;
  }
  return false;
 }
 private byte[] nxdomain(byte[] q){
  byte[] a=Arrays.copyOf(q,q.length);a[2]=(byte)0x81;a[3]=(byte)0x83;
  for(int i=6;i<12;i++)a[i]=0;return a;
 }
 private byte[] forward(byte[] q){
  DatagramSocket socket=null;
  try{
   socket=new DatagramSocket();if(!protect(socket))return null;
   socket.setSoTimeout(2500);
   InetAddress upstream=InetAddress.getByName("1.1.1.1");
   socket.send(new DatagramPacket(q,q.length,upstream,53));
   byte[] response=new byte[1500];DatagramPacket p=new DatagramPacket(response,response.length);socket.receive(p);
   if(p.getLength()<12||response[0]!=q[0]||response[1]!=q[1])return null;
   return Arrays.copyOf(response,p.getLength());
  }catch(Exception ignored){return null;}finally{if(socket!=null)socket.close();}
 }
 private byte[] packet(byte[] req,int ihl,int port,byte[] dns){
  int udpLen=8+dns.length;byte[] out=new byte[ihl+udpLen];
  System.arraycopy(req,0,out,0,ihl);out[2]=0;out[3]=0;put16(out,2,out.length);
  out[8]=64;out[10]=0;out[11]=0;
  System.arraycopy(req,16,out,12,4);System.arraycopy(req,12,out,16,4);
  put16(out,10,checksum(out,0,ihl));
  put16(out,ihl,53);put16(out,ihl+2,port);put16(out,ihl+4,udpLen);put16(out,ihl+6,0);
  System.arraycopy(dns,0,out,ihl+8,dns.length);
  return out;
 }
 @Override public void onDestroy(){running=false;if(tun!=null)try{tun.close();}catch(IOException ignored){}getSharedPreferences("titan_dns_vpn",MODE_PRIVATE).edit().putBoolean("active",false).apply();super.onDestroy();}
 @Override public void onRevoke(){stopSelf();}
}
