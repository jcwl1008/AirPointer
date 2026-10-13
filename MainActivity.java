package com.jimmy.bluepointer;
import android.Manifest;
import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.Color;
import android.graphics.drawable.*;
import android.hardware.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity implements SensorEventListener {
 final int BLUE=Color.rgb(0,122,255), INK=Color.rgb(28,28,30), MUTED=Color.rgb(99,99,102), LIGHT=Color.rgb(242,242,247);
 BluetoothAdapter adapter; LinkService link; boolean bound=false,binding=false;
 boolean holding=false,laser=false,spotlight=false,keyBusy=false;
 final ServiceConnection serviceConnection=new ServiceConnection(){
 public void onServiceConnected(ComponentName n,IBinder b){binding=false;bound=true;link=((LinkService.LocalBinder)b).get();link.listener=()->refreshLink();refreshLink();}
 public void onServiceDisconnected(ComponentName n){binding=false;bound=false;link=null;stopMotion();showStatus("連線服務中斷 · 請重新開啟 App");}};
 void refreshLink(){if(link==null)return;showStatus(link.state);device.setText(link.hostName());if(!link.connected()){stopMotion();keys.clear();keyBusy=false;}}
 SensorManager sensors; Sensor gyro; MotionEngine motion=new MotionEngine();
 Handler handler=new Handler(Looper.getMainLooper()); ArrayDeque<int[]> keys=new ArrayDeque<>();
 TextView status,device,hold,clock; LinearLayout root; Typeface bold,regular; android.content.SharedPreferences prefs;
 float gain; boolean invert; long started; boolean timing; int mouseButtons=0; float tx,ty,travel;long touchStart;
 final Runnable ticker=new Runnable(){public void run(){long secs=timing?(SystemClock.elapsedRealtime()-started)/1000:0;clock.setText(String.format(Locale.US,"%02d:%02d",secs/60,secs%60));handler.postDelayed(this,1000);}};
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("settings",0);gain=prefs.getFloat("gain",1050);invert=prefs.getBoolean("invert",false);
 bold=Typeface.create("sans-serif",Typeface.BOLD);regular=Typeface.create("sans-serif",Typeface.NORMAL);
 getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().setStatusBarColor(LIGHT);getWindow().setNavigationBarColor(LIGHT);
 getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
 sensors=(SensorManager)getSystemService(SENSOR_SERVICE);gyro=sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
 BluetoothManager manager=(BluetoothManager)getSystemService(BLUETOOTH_SERVICE);adapter=manager==null?null:manager.getAdapter();buildUi();
 handler.post(ticker);
 }
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 TextView text(String value,int size,int color,boolean strong){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);t.setTypeface(strong?bold:regular);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
 TextView button(String value,int color,boolean primary,int height,Runnable action){TextView t=text(value,17,primary?Color.WHITE:INK,true);t.setGravity(Gravity.CENTER);t.setPadding(dp(10),dp(8),dp(10),dp(8));t.setMinHeight(dp(height));t.setBackground(new RippleDrawable(android.content.res.ColorStateList.valueOf(0x223377EE),bg(color,22),null));t.setClickable(true);t.setFocusable(true);t.setOnClickListener(v->{v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);action.run();});return t;}
 void add(View v,int h,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,h<0?h:dp(h));p.topMargin=dp(top);root.addView(v,p);}
 void row(TextView a,TextView b,int h,int top){LinearLayout r=new LinearLayout(this);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(h),1);p.rightMargin=dp(6);r.addView(a,p);p=new LinearLayout.LayoutParams(0,dp(h),1);p.leftMargin=dp(6);r.addView(b,p);add(r,h,top);}
 void buildUi(){
 LinearLayout screen=new LinearLayout(this);screen.setOrientation(LinearLayout.VERTICAL);screen.setBackgroundColor(LIGHT);setContentView(screen);
 screen.setOnApplyWindowInsetsListener((v,i)->{if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=i.getInsets(WindowInsets.Type.systemBars());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}else v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());return i;});
 LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(22),dp(8),dp(16),dp(8));
 LinearLayout name=new LinearLayout(this);name.setOrientation(1);name.addView(text("AirPointer",30,INK,true));name.addView(text("你的隨身簡報遙控器",13,MUTED,false));header.addView(name,new LinearLayout.LayoutParams(0,-2,1));header.addView(button("設定",Color.WHITE,false,52,()->settings()),new LinearLayout.LayoutParams(dp(64),dp(52)));screen.addView(header,new LinearLayout.LayoutParams(-1,-2));
 ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(20),dp(6),dp(20),dp(14));scroll.addView(root);screen.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
 LinearLayout card=new LinearLayout(this);card.setOrientation(1);card.setPadding(dp(16),dp(10),dp(16),dp(10));card.setBackground(bg(Color.WHITE,24));
 status=text("●  未連接",16,BLUE,true);device=text("點一下選擇已配對電腦",13,MUTED,false);device.setSingleLine(true);device.setEllipsize(android.text.TextUtils.TruncateAt.END);
 LinearLayout info=new LinearLayout(this);info.setOrientation(1);info.addView(status);info.addView(device);LinearLayout line=new LinearLayout(this);line.setGravity(Gravity.CENTER_VERTICAL);line.addView(info,new LinearLayout.LayoutParams(0,-2,1));line.addView(button("連接  ›",LIGHT,false,52,()->chooseHost()),new LinearLayout.LayoutParams(dp(86),dp(52)));card.addView(line);info.setPadding(0,dp(6),dp(6),dp(6));info.setMinimumHeight(dp(52));info.setOnClickListener(v->diagnostics());info.setContentDescription("電腦連線狀態，點一下查看診斷");add(card,-2,4);
 LinearLayout session=new LinearLayout(this);session.setGravity(Gravity.CENTER_VERTICAL);session.setPadding(dp(4),dp(8),0,dp(2));session.addView(text("簡報控制",14,MUTED,true),new LinearLayout.LayoutParams(0,-2,1));clock=text("00:00",16,MUTED,true);clock.setGravity(Gravity.CENTER);session.addView(clock,new LinearLayout.LayoutParams(dp(68),dp(44)));session.addView(button("說明",LIGHT,false,48,()->help()),new LinearLayout.LayoutParams(dp(64),dp(48)));add(session,54,0);
 hold=button(gyro==null?"使用下方觸控板":"◎  按住移動游標",0xFFE1EEFF,false,112,()->{});hold.setTextSize(22);hold.setContentDescription("按住並轉動手機控制游標，放開即停");
 hold.setOnTouchListener((v,e)->{if(e.getActionMasked()==MotionEvent.ACTION_DOWN){if(!connected())return true;if(gyro==null){toast("可使用下方觸控板");return true;}v.getParent().requestDisallowInterceptTouchEvent(true);holding=true;motion.reset();sensors.registerListener(this,gyro,16667);hold.setText("◎  控制中 · 放開即停");hold.setTextColor(Color.WHITE);hold.setBackground(bg(BLUE,26));v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);return true;}if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){v.getParent().requestDisallowInterceptTouchEvent(false);stopMotion();return true;}return true;});add(hold,112,0);
 TextView tip=text("直向握住 · 左右轉動／上下傾斜",12,MUTED,false);tip.setGravity(Gravity.CENTER);add(tip,-2,6);
 TextView pad=text("觸控板\n滑動移動游標 · 輕點一下點擊",15,MUTED,false);pad.setGravity(Gravity.CENTER);pad.setBackground(bg(Color.WHITE,26));pad.setContentDescription("觸控板，滑动移動游標，輕點一下點擊");
 pad.setOnTouchListener((v,e)->{switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:if(!connected())return true;v.getParent().requestDisallowInterceptTouchEvent(true);tx=e.getX();ty=e.getY();travel=0;touchStart=SystemClock.uptimeMillis();v.setAlpha(0.78f);return true;case MotionEvent.ACTION_MOVE:float dx=e.getX()-tx,dy=e.getY()-ty;travel+=Math.abs(dx)+Math.abs(dy);move((int)(dx*1.3),(int)(dy*1.3));tx=e.getX();ty=e.getY();return true;case MotionEvent.ACTION_UP:v.setAlpha(1f);v.getParent().requestDisallowInterceptTouchEvent(false);if(travel<dp(8)&&SystemClock.uptimeMillis()-touchStart<300){if(connected()){send(1,HidReports.mouse(1,0,0));send(1,HidReports.mouse(0,0,0));}v.performClick();v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);}return true;case MotionEvent.ACTION_CANCEL:v.setAlpha(1f);v.getParent().requestDisallowInterceptTouchEvent(false);return true;}return true;});pad.setOnClickListener(v->{});pad.setFocusable(true);add(pad,112,10);
 row(button("雷射游標",Color.WHITE,false,58,()->{if(connected()){key(1,laser?0x04:0x0F);laser=!laser;toast(laser?"PowerPoint 雷射模式":"已切回箭頭");}}),button("聚光燈",Color.WHITE,false,58,()->{if(connected()){key(5,0x16);spotlight=!spotlight;toast("需先啟動 Windows 聚光燈工具");}}),58,10);
 row(button("開始簡報",Color.WHITE,false,58,()->{if(connected()){timing=true;started=SystemClock.elapsedRealtime();key(2,0x3E);}}),button("結束簡報",Color.WHITE,false,58,()->{key(0,0x29);if(spotlight){key(5,0x16);spotlight=false;}laser=false;timing=false;}),58,10);
 // A fixed thumb dock keeps page navigation reachable, even when content scrolls.
 LinearLayout dock=new LinearLayout(this);dock.setOrientation(1);dock.setPadding(dp(20),dp(10),dp(20),dp(12));dock.setBackgroundColor(LIGHT);
 LinearLayout pages=new LinearLayout(this);TextView prev=button("‹\n上一頁",Color.WHITE,false,94,()->key(0,0x4B));TextView next=button("›\n下一頁",BLUE,true,94,()->key(0,0x4E));prev.setTextSize(21);next.setTextSize(21);prev.setContentDescription("上一張投影片");next.setContentDescription("下一張投影片");LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(0,dp(94),1);pp.rightMargin=dp(6);pages.addView(prev,pp);pp=new LinearLayout.LayoutParams(0,dp(94),1);pp.leftMargin=dp(6);pages.addView(next,pp);dock.addView(pages);screen.addView(dock,new LinearLayout.LayoutParams(-1,-2));
 }
 boolean allowed(){return Build.VERSION.SDK_INT<31||(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE)==PackageManager.PERMISSION_GRANTED);}
 void startBluetooth(){if(adapter==null){showStatus("手機不支援 Bluetooth");return;}if(!allowed()){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT,Manifest.permission.BLUETOOTH_ADVERTISE},10);return;}if(!adapter.isEnabled()){showStatus("請開啟 Bluetooth");return;}
 if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED&&!prefs.getBoolean("askedNotifications",false)){prefs.edit().putBoolean("askedNotifications",true).apply();requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},13);return;}
 try{startForegroundService(new Intent(this,LinkService.class));if(!bound&&!binding){binding=bindService(new Intent(this,LinkService.class),serviceConnection,BIND_AUTO_CREATE);if(!binding)showStatus("未能繫結連線服務");}else if(link!=null){link.listener=()->refreshLink();refreshLink();}}catch(RuntimeException e){binding=false;showStatus("無法啟動連線服務 · "+e.getClass().getSimpleName());}}
 void diagnostics(){String info=link==null?"AirPointer 1.2\n連線服務未啟動\n手機："+Build.MODEL+"\nAndroid："+Build.VERSION.RELEASE:link.diagnostics();TextView t=text(info,13,INK,false);t.setTextIsSelectable(true);t.setPadding(dp(20),dp(12),dp(20),dp(12));ScrollView scroll=new ScrollView(this);scroll.addView(t);new AlertDialog.Builder(this).setTitle("Bluetooth 連線診斷").setView(scroll).setPositiveButton("複製診斷",(d,w)->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("AirPointer diagnostics",info));toast("已複製，可貼上診斷文字");}).setNegativeButton("關閉",null).show();}
 void showStatus(String s){status.setText("●  "+s);}
 void chooseHost(){if(adapter==null){toast("手機沒有 Bluetooth");return;}if(!allowed()){startBluetooth();return;}if(!adapter.isEnabled()){startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),11);return;}startBluetooth();ArrayList<BluetoothDevice> devices=new ArrayList<>(adapter.getBondedDevices());Collections.sort(devices,(a,b)->String.valueOf(a.getName()).compareTo(String.valueOf(b.getName())));String[] names=new String[devices.size()];for(int i=0;i<names.length;i++){BluetoothDevice d=devices.get(i);names[i]=(d.getName()==null?"Bluetooth 裝置":d.getName())+"\n"+d.getAddress();}
 new AlertDialog.Builder(this).setTitle("選擇已配對電腦").setItems(names,(dlg,index)->{if(link==null){toast("連線服務正在啟動，請稍後再選一次");return;}link.select(devices.get(index));}).setPositiveButton("配對新電腦",(d,w)->{Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120);startActivityForResult(intent,12);}).setNeutralButton("連線診斷",(d,w)->diagnostics()).setNegativeButton("取消",null).show();}
 boolean connected(){if(link!=null&&link.connected())return true;toast("請先連接電腦；點選頂部狀態可查看診斷");return false;}
 boolean send(int id,byte[] data){return link!=null&&link.send(id,data);}
 void move(int dx,int dy){if(dx==0&&dy==0)return;send(1,HidReports.mouse(mouseButtons,dx,dy));}
 void key(int mod,int usage){if(!connected())return;if(keys.size()>12)return;keys.add(new int[]{mod,usage});drainKeys();}
 void drainKeys(){if(keyBusy||keys.isEmpty())return;int[] k=keys.remove();if(!send(2,HidReports.key(k[0],k[1])))return;keyBusy=true;final LinkService session=link;handler.postDelayed(()->{if(session!=null&&session==link)send(2,new byte[8]);keyBusy=false;drainKeys();},45);}
 void stopMotion(){holding=false;motion.reset();sensors.unregisterListener(this);if(hold!=null){hold.setText(gyro==null?"使用下方觸控板":"◎  按住移動游標");hold.setTextColor(INK);hold.setBackground(bg(0xFFE1EEFF,26));}}
 @Override public void onSensorChanged(SensorEvent e){if(!holding||link==null||!link.connected())return;int[] delta=motion.step(e.timestamp,e.values[0],e.values[1],gain,invert);move(delta[0],delta[1]);}
 @Override public void onAccuracyChanged(Sensor s,int accuracy){}
 void settings(){LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(24),dp(14),dp(24),dp(8));TextView label=text("陀螺儀靈敏度",15,INK,true);box.addView(label);SeekBar seek=new SeekBar(this);seek.setMax(1800);seek.setProgress((int)gain-300);seek.setMinimumHeight(dp(56));seek.setProgressTintList(android.content.res.ColorStateList.valueOf(BLUE));box.addView(seek);seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean user){gain=300+p;prefs.edit().putFloat("gain",gain).apply();}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});Switch flip=new Switch(this);flip.setText("反轉上下方向");flip.setChecked(invert);flip.setOnCheckedChangeListener((b,c)->{invert=c;prefs.edit().putBoolean("invert",c).apply();});flip.setMinHeight(dp(56));box.addView(flip);TextView reset=button("重設為預設值",LIGHT,false,48,()->{gain=1050;invert=false;seek.setProgress(750);flip.setChecked(false);prefs.edit().putFloat("gain",gain).putBoolean("invert",false).apply();});box.addView(reset);box.addView(button("停止 Bluetooth 連線",LIGHT,false,48,()->{if(link!=null)link.close();stopService(new Intent(this,LinkService.class));showStatus("連線服務已停止");device.setText("按連接電腦可重新啟動");}));new AlertDialog.Builder(this).setTitle("控制設定").setView(box).setPositiveButton("完成",null).show();}
 void help(){new AlertDialog.Builder(this).setTitle("3 步開始簡報").setMessage("1  按「連接電腦」→「配對新電腦」，再到電腦 Bluetooth 設定新增此手機。\n\n2  返回 App，選擇已配對電腦。新版使用前景連線服務。若已配對但 HID 仍無法連接，請點選頂部狀態，複製連線診斷。\n\n3  電腦開啟 PowerPoint，點一下投影片令視窗取得焦點，再按「開始簡報」。按住「移動游標」區域，移動手機控制游標；放開即停。\n\n雷射：Windows PowerPoint 放映模式使用 Ctrl+L／Ctrl+A。\n聚光燈：先在 Windows 啟動附送工具，按鍵以 Ctrl+Alt+S 切換。\n\n陀螺儀為相對移動；放開、調整握姿、再按住即可。介面使用系統字型，支援繁體中文及放大文字。\n\nAirPointer 1.2：支援 HID 前景服務、有限次數自動重連及 Windows 鍵盤狀態報告。Android 9+，需手機支援 Bluetooth HID。無網絡、無帳戶、無廣告。\n\n個人製作，並非 Samsung 或 Logitech 官方產品。").setPositiveButton("明白",null).show();}
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
 @Override protected void onResume(){super.onResume();startBluetooth();}
 @Override protected void onPause(){stopMotion();if(link!=null)link.release();super.onPause();}
 @Override protected void onStop(){keys.clear();keyBusy=false;if(link!=null)link.listener=null;super.onStop();}
 @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(link!=null)link.listener=null;if(bound||binding){unbindService(serviceConnection);bound=binding=false;}super.onDestroy();}
 @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(allowed())startBluetooth();else showStatus("請在設定允許附近裝置權限");}
}
