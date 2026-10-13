package com.jimmy.bluepointer;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import java.util.*;

/** Bluetooth session is owned by a connectedDevice foreground service, never by a screen. */
public final class LinkService extends Service {
 public interface Listener {void changed();}
 public Listener listener;
 public final class LocalBinder extends Binder {public LinkService get(){return LinkService.this;}}
 final Handler handler=new Handler(Looper.getMainLooper()); final LocalBinder binder=new LocalBinder();
 BluetoothAdapter adapter; BluetoothHidDevice hid; BluetoothDevice host,target;
 boolean active,registered,registering,proxyPending,connecting; int epoch,registerRetries,registrationSequence; Runnable connectionTimeout;
 final RetryPolicy retry=new RetryPolicy(3);
 String state="未連接";byte protocol=BluetoothHidDevice.PROTOCOL_REPORT_MODE,leds;
 byte[] keyboard=new byte[8],mouse=new byte[3];
 final ArrayDeque<String> events=new ArrayDeque<>();
 final Runnable reconnect=()->connectNow();
 final BroadcastReceiver receiver=new BroadcastReceiver(){public void onReceive(Context c,Intent i){
  int s=i.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1);
  if(s==BluetoothAdapter.STATE_ON&&active){retry.reset();openProfile();}
  else if(s==BluetoothAdapter.STATE_OFF){epoch++;registered=registering=proxyPending=connecting=false;host=null;hid=null;handler.removeCallbacks(reconnect);update("Bluetooth 已關閉");}
 }};
 @Override public void onCreate(){super.onCreate();BluetoothManager m=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);adapter=m==null?null:m.getAdapter();
  NotificationChannel channel=new NotificationChannel("link","簡報遙控連線",NotificationManager.IMPORTANCE_LOW);channel.setDescription("保持 Bluetooth 滑鼠與簡報鍵盤連線");((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
  IntentFilter filter=new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);if(Build.VERSION.SDK_INT>=33)registerReceiver(receiver,filter,Context.RECEIVER_EXPORTED);else registerReceiver(receiver,filter);
 }
 @Override public int onStartCommand(Intent i,int flags,int id){if(i!=null&&"STOP".equals(i.getAction())){close();stopSelf();return START_NOT_STICKY;}
  if(!active){active=true;epoch++;retry.reset();registerRetries=0;log("Session started");}
  Notification n=notification();try{if(Build.VERSION.SDK_INT>=29)startForeground(7,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);else startForeground(7,n);}catch(SecurityException e){active=false;state="前景服務權限不足";if(listener!=null)listener.changed();stopSelf();return START_NOT_STICKY;}
  openProfile();return START_NOT_STICKY;
 }
 Notification notification(){PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  PendingIntent stop=PendingIntent.getService(this,1,new Intent(this,LinkService.class).setAction("STOP"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  return new Notification.Builder(this,"link").setSmallIcon(android.R.drawable.stat_sys_data_bluetooth).setContentTitle("AirPointer 1.6").setContentText(state).setContentIntent(open).setOngoing(true).addAction(new Notification.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,"停止連線",stop).build()).build();
 }
 public boolean ready(){return active&&registered&&hid!=null;}
 public boolean connected(){return ready()&&host!=null;}
 public String hostName(){try{return host!=null?host.getName():target!=null?target.getName():"請選擇已配對電腦";}catch(SecurityException e){return "需要附近裝置權限";}}
 void update(String s){state=s;log(s);if(active)((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(7,notification());if(listener!=null)listener.changed();}
 void log(String s){events.add(String.format(Locale.US,"%tT  %s",new Date(),s));while(events.size()>60)events.remove();}
 public String diagnostics(){StringBuilder b=new StringBuilder("AirPointer 1.6\n手機："+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid："+Build.VERSION.RELEASE+" / API "+Build.VERSION.SDK_INT+"\n狀態："+state+"\nHID 已註冊："+registered+"\n前景服務："+active+"\n嘗試次數："+retry.attempts()+" / 3\n通訊協定："+(protocol==BluetoothHidDevice.PROTOCOL_BOOT_MODE?"Boot":"Report")+"\n\n");for(String s:events)b.append(s).append('\n');return b.toString();}
 void openProfile(){if(!active)return;if(adapter==null){update("手機沒有 Bluetooth");return;}try{if(!adapter.isEnabled()){update("請開啟 Bluetooth");return;}if(hid!=null){registerHid();return;}if(proxyPending)return;proxyPending=true;final int expected=epoch;
  boolean ok=adapter.getProfileProxy(getApplicationContext(),new BluetoothProfile.ServiceListener(){public void onServiceConnected(int profile,BluetoothProfile proxy){if(!active||expected!=epoch){adapter.closeProfileProxy(profile,proxy);return;}proxyPending=false;hid=(BluetoothHidDevice)proxy;log("HID profile available");registerHid();}public void onServiceDisconnected(int profile){if(expected!=epoch)return;proxyPending=false;hid=null;registered=registering=false;host=null;update("HID 服務中斷 · 請重試");}},BluetoothProfile.HID_DEVICE);
  if(!ok){proxyPending=false;update("手機未提供 HID 服務 · 點選狀態查看診斷");}else handler.postDelayed(()->{if(expected==epoch&&proxyPending){proxyPending=false;update("HID 服務未回應 · 請重開 Bluetooth");}},10000);
 }catch(SecurityException e){proxyPending=false;update("請允許附近裝置權限");}}
 void registerHid(){if(!active||hid==null||registered||registering)return;registering=true;final int expected=epoch, registration=++registrationSequence;
  try{boolean accepted=hid.registerApp(new BluetoothHidDeviceAppSdpSettings("Blue Pointer","Presentation keyboard and mouse","Jimmy",BluetoothHidDevice.SUBCLASS1_COMBO,HidReports.DESCRIPTOR),null,null,getMainExecutor(),new BluetoothHidDevice.Callback(){
   boolean valid(){return active&&expected==epoch&&registration==registrationSequence&&hid!=null;}
   public void onAppStatusChanged(BluetoothDevice plugged,boolean ok){if(!valid())return;registering=false;registered=ok;log("HID registration="+ok);
    if(!ok){host=null;connecting=false;update("HID 註冊已中斷");if(registerRetries++<2)handler.postDelayed(()->{if(valid())registerHid();},1200);return;}
    update("遙控已就緒 · 選擇電腦");try{List<BluetoothDevice> live=hid.getConnectedDevices();if(!live.isEmpty()){connectedTo(live.get(0));return;}if(target==null){String saved=getSharedPreferences("settings",0).getString("computer",null);if(saved!=null)target=adapter.getRemoteDevice(saved);}if(target!=null&&target.getBondState()==BluetoothDevice.BOND_BONDED)scheduleConnect(400);}catch(SecurityException|IllegalArgumentException ex){log("Restore failed: "+ex.getClass().getSimpleName());}
   }
   public void onConnectionStateChanged(BluetoothDevice d,int s){if(!valid())return;log("Host state="+s);if(s==BluetoothProfile.STATE_CONNECTED){connectedTo(d);return;}if(target!=null&&!target.equals(d))return;
    if(s==BluetoothProfile.STATE_CONNECTING){connecting=true;watchConnection(d);update("正在建立 HID 連線…");}
    else if(s==BluetoothProfile.STATE_DISCONNECTED){cancelWatch();host=null;connecting=false;keyboard=new byte[8];mouse=new byte[3];if(retry.canAttempt()){update("HID 已斷線 · 自動重試中");scheduleConnect(retry.delayMillis());}else update("已配對，但 HID 連線失敗 · 點選狀態查看診斷");}
   }
   public void onGetReport(BluetoothDevice d,byte type,byte id,int max){if(!valid())return;log("GET_REPORT type="+type+" id="+id);
    byte[] bytes=null;if(type==BluetoothHidDevice.REPORT_TYPE_INPUT){if(id==1)bytes=mouse;else if(id==2||protocol==BluetoothHidDevice.PROTOCOL_BOOT_MODE&&id==0)bytes=keyboard;}else if(type==BluetoothHidDevice.REPORT_TYPE_OUTPUT&&id==2)bytes=new byte[]{leds};
    if(bytes==null){hid.reportError(d,BluetoothHidDevice.ERROR_RSP_INVALID_RPT_ID);return;}hid.replyReport(d,type,id,max>0&&max<bytes.length?Arrays.copyOf(bytes,max):bytes);
   }
   public void onSetReport(BluetoothDevice d,byte type,byte id,byte[] data){if(!valid())return;log("SET_REPORT type="+type+" id="+id+" length="+(data==null?0:data.length));
    if(type==BluetoothHidDevice.REPORT_TYPE_OUTPUT&&(id==2||protocol==BluetoothHidDevice.PROTOCOL_BOOT_MODE&&id==0)&&data!=null&&data.length==1){leds=data[0];hid.reportError(d,BluetoothHidDevice.ERROR_RSP_SUCCESS);}else hid.reportError(d,BluetoothHidDevice.ERROR_RSP_INVALID_PARAM);
   }
   public void onInterruptData(BluetoothDevice d,byte id,byte[] data){if(valid()&&id==2&&data!=null&&data.length>0){leds=data[0];log("Keyboard LED output received");}}
   public void onSetProtocol(BluetoothDevice d,byte p){if(!valid())return;protocol=p;log("Protocol="+p);}
   public void onVirtualCableUnplug(BluetoothDevice d){if(!valid())return;target=host=null;connecting=false;handler.removeCallbacks(reconnect);getSharedPreferences("settings",0).edit().remove("computer").apply();update("電腦移除了 HID 連線 · 請重新配對");}
  });log("registerApp request="+accepted);if(!accepted){registering=false;update("HID 註冊未獲接受 · 關閉其他遙控 App 後重試");}
  handler.postDelayed(()->{if(active&&expected==epoch&&registration==registrationSequence&&registering){registering=false;update("HID 註冊逾時 · 點選狀態查看診斷");}},10000);
 }catch(SecurityException e){registering=false;update("附近裝置權限不足");}}
 void connectedTo(BluetoothDevice d){cancelWatch();host=target=d;connecting=false;protocol=BluetoothHidDevice.PROTOCOL_REPORT_MODE;handler.removeCallbacks(reconnect);getSharedPreferences("settings",0).edit().putString("computer",d.getAddress()).apply();update("已連接 · HID 滑鼠／鍵盤");send(1,new byte[3]);send(2,new byte[8]);}
 public void select(BluetoothDevice d){cancelWatch();connecting=false;retry.reset();registerRetries=0;handler.removeCallbacks(reconnect);if(host!=null&&!host.equals(d)){try{hid.disconnect(host);}catch(SecurityException ignored){}host=null;}target=d;getSharedPreferences("settings",0).edit().putString("computer",d.getAddress()).apply();if(!registered){update("等待 HID 註冊完成…");openProfile();return;}scheduleConnect(0);}
 void scheduleConnect(long delay){handler.removeCallbacks(reconnect);if(active&&target!=null&&!connecting&&retry.canAttempt())handler.postDelayed(reconnect,delay);}
 void connectNow(){if(!ready()||target==null||connecting)return;try{if(target.getBondState()!=BluetoothDevice.BOND_BONDED){update("電腦尚未配對");return;}int s=hid.getConnectionState(target);if(s==BluetoothProfile.STATE_CONNECTED){connectedTo(target);return;}if(s==BluetoothProfile.STATE_CONNECTING){connecting=true;watchConnection(target);return;}if(!retry.takeAttempt())return;connecting=true;final BluetoothDevice selected=target;boolean accepted=hid.connect(target);log("connect request="+accepted+", attempt="+retry.attempts());update("正在連接 HID · "+retry.attempts()+"/3");if(!accepted){connecting=false;if(retry.canAttempt())scheduleConnect(retry.delayMillis());else update("HID 連線未獲接受 · 點選狀態查看診斷");return;}
  watchConnection(selected);
 }catch(SecurityException e){connecting=false;update("附近裝置權限不足");}}
 void cancelWatch(){if(connectionTimeout!=null)handler.removeCallbacks(connectionTimeout);connectionTimeout=null;}
 void watchConnection(final BluetoothDevice selected){cancelWatch();final int expected=epoch;connectionTimeout=()->{if(active&&epoch==expected&&selected.equals(target)&&host==null&&connecting){connecting=false;log("Connection timeout");try{hid.disconnect(selected);}catch(SecurityException ignored){}if(retry.canAttempt())scheduleConnect(retry.delayMillis());else update("HID 連線逾時 · 點選狀態查看診斷");}};handler.postDelayed(connectionTimeout,12000);}
 public boolean send(int id,byte[] data){if(!connected())return false;try{if(id==2)keyboard=data.clone();else if(id==1)mouse=data.clone();return hid.sendReport(host,protocol==BluetoothHidDevice.PROTOCOL_BOOT_MODE?0:id,data);}catch(SecurityException|IllegalStateException e){log("sendReport failed: "+e.getClass().getSimpleName());return false;}}
 public void release(){send(1,new byte[3]);send(2,new byte[8]);}
 public void close(){release();active=false;epoch++;handler.removeCallbacksAndMessages(null);if(hid!=null){try{if(registered)hid.unregisterApp();adapter.closeProfileProxy(BluetoothProfile.HID_DEVICE,hid);}catch(SecurityException ignored){}}hid=null;host=null;registered=registering=proxyPending=connecting=false;state="連線服務已停止";stopForeground(true);if(listener!=null)listener.changed();listener=null;}
 @Override public IBinder onBind(Intent i){return binder;}
 @Override public void onDestroy(){close();unregisterReceiver(receiver);super.onDestroy();}
}
