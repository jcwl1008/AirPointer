package com.jimmy.bluepointer.tests;
import android.app.*;
import android.content.*;
import android.os.*;
import com.jimmy.bluepointer.LinkService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
public class LinkSmoke extends Instrumentation {
 public void onCreate(Bundle args){super.onCreate(args);start();}
 public void onStart(){final Bundle result=new Bundle();final AtomicReference<LinkService> service=new AtomicReference<>();final CountDownLatch bound=new CountDownLatch(1);Context c=getTargetContext();
 ServiceConnection connection=new ServiceConnection(){public void onServiceConnected(ComponentName n,IBinder b){service.set(((LinkService.LocalBinder)b).get());bound.countDown();}public void onServiceDisconnected(ComponentName n){}};
 boolean binding=false;try{
  final Activity activity=startActivitySync(new Intent().setClassName("com.jimmy.bluepointer","com.jimmy.bluepointer.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  runOnMainSync(()->c.startForegroundService(new Intent(c,LinkService.class)));
  binding=c.bindService(new Intent(c,LinkService.class),connection,Context.BIND_AUTO_CREATE);
  if(!binding||!bound.await(15,TimeUnit.SECONDS))throw new AssertionError("Service binder unavailable");
  Thread.sleep(1000);final AtomicReference<String> info=new AtomicReference<>();runOnMainSync(()->info.set(service.get().diagnostics()));
  if(!info.get().contains("前景服務：true"))throw new AssertionError(info.get());
  runOnMainSync(()->activity.moveTaskToBack(true));Thread.sleep(1500);runOnMainSync(()->info.set(service.get().diagnostics()));
  if(!info.get().contains("前景服務：true"))throw new AssertionError("Service lost foreground status after Activity stopped");
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  if(nm.getActiveNotifications().length==0)throw new AssertionError("Foreground notification absent");
  runOnMainSync(()->service.get().close());c.stopService(new Intent(c,LinkService.class));Thread.sleep(500);
  if(nm.getActiveNotifications().length!=0)throw new AssertionError("Notification remains after explicit stop");
  result.putString("result","PASS: foreground connectedDevice service starts; Activity background retains service; ongoing notification exists; explicit stop removes notification.");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){result.putString("result","FAIL: "+e.toString());finish(Activity.RESULT_CANCELED,result);}finally{if(binding)c.unbindService(connection);}}
}
