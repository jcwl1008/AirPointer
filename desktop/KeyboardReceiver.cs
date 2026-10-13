using System;
using System.Text;
using System.Globalization;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using System.Diagnostics;

public static class TextFrame {
 public static string Decode(string frame) {
  if(frame==null||frame.Length<12||!frame.StartsWith("a1",StringComparison.OrdinalIgnoreCase))throw new FormatException("Bad header");
  int n=int.Parse(frame.Substring(2,2),NumberStyles.HexNumber,CultureInfo.InvariantCulture);
  int sum=int.Parse(frame.Substring(4,4),NumberStyles.HexNumber,CultureInfo.InvariantCulture);
  if(n<1||n>32||frame.Length!=8+n*4)throw new FormatException("Bad length");
  StringBuilder s=new StringBuilder();int total=0;
  for(int i=0;i<n;i++){int x=int.Parse(frame.Substring(8+i*4,4),NumberStyles.HexNumber,CultureInfo.InvariantCulture);total=(total+x)&65535;s.Append((char)x);}
  if(sum!=total)throw new FormatException("Bad checksum");
  string value=s.ToString();for(int i=0;i<value.Length;i++){if(char.IsHighSurrogate(value[i])){if(i+1>=value.Length||!char.IsLowSurrogate(value[++i]))throw new FormatException("Bad surrogate");}else if(char.IsLowSurrogate(value[i]))throw new FormatException("Bad surrogate");}
  return value;
 }
}
public static class MediaFrame {
 public static ushort Decode(string frame){if(frame==null||frame.Length!=6||!frame.StartsWith("a2",StringComparison.OrdinalIgnoreCase))throw new FormatException("Bad media frame");int command=int.Parse(frame.Substring(2,2),NumberStyles.HexNumber,CultureInfo.InvariantCulture);int check=int.Parse(frame.Substring(4,2),NumberStyles.HexNumber,CultureInfo.InvariantCulture);if(command<1||command>5||check!=(command^0xC3))throw new FormatException("Bad media checksum");ushort[] keys={0,0xB3,0xB2,0xAD,0xAF,0xAE};return keys[command];}
}
public sealed class AirPointerReceiver : Form {
 delegate IntPtr HookProc(int code,IntPtr msg,IntPtr data);
 [DllImport("user32.dll")] static extern IntPtr SetWindowsHookEx(int type,HookProc cb,IntPtr module,uint thread);
 [DllImport("user32.dll")] static extern bool UnhookWindowsHookEx(IntPtr hook);
 [DllImport("user32.dll")] static extern IntPtr CallNextHookEx(IntPtr h,int code,IntPtr msg,IntPtr data);
 [DllImport("kernel32.dll",CharSet=CharSet.Auto)] static extern IntPtr GetModuleHandle(string name);
 [DllImport("user32.dll")] static extern short GetAsyncKeyState(int key);
 [DllImport("user32.dll")] static extern IntPtr GetForegroundWindow();
 [DllImport("user32.dll",SetLastError=true)] static extern uint SendInput(uint n,INPUT[] input,int size);
 [StructLayout(LayoutKind.Sequential)] struct KEYDATA {public uint vk,scan,flags,time;public UIntPtr extra;}
 [StructLayout(LayoutKind.Sequential)] struct KEYINPUT {public ushort vk,scan;public uint flags,time;public UIntPtr extra;}
 [StructLayout(LayoutKind.Sequential)] struct MOUSEINPUT {public int dx,dy;public uint data,flags,time;public UIntPtr extra;}
 [StructLayout(LayoutKind.Explicit)] struct INPUTUNION {[FieldOffset(0)]public KEYINPUT key;[FieldOffset(0)]public MOUSEINPUT mouse;}
 [StructLayout(LayoutKind.Sequential)] struct INPUT {public uint type;public INPUTUNION data;}
 IntPtr hook,target;HookProc callback;NotifyIcon tray;Timer timer;StringBuilder buffer=new StringBuilder();bool capture=false;DateTime last;bool swallowEnter=false;
 public AirPointerReceiver(){ShowInTaskbar=false;WindowState=FormWindowState.Minimized;Opacity=0;FormBorderStyle=FormBorderStyle.FixedToolWindow;Text="AirPointer 1.6 文字／媒體接收器";callback=OnKey;hook=SetWindowsHookEx(13,callback,GetModuleHandle(Process.GetCurrentProcess().MainModule.ModuleName),0);if(hook==IntPtr.Zero)throw new InvalidOperationException("Unable to start keyboard receiver.");tray=new NotifyIcon();tray.Icon=System.Drawing.SystemIcons.Information;tray.Text="AirPointer 1.6 文字／媒體接收器 · 已啟動";ContextMenuStrip m=new ContextMenuStrip();m.Items.Add("取消目前傳送",null,delegate{Cancel();});m.Items.Add("退出",null,delegate{Close();});tray.ContextMenuStrip=m;tray.Visible=true;timer=new Timer();timer.Interval=500;timer.Tick+=delegate{if(capture&&(DateTime.UtcNow-last).TotalSeconds>5)Cancel();};timer.Start();tray.ShowBalloonTip(3500,"AirPointer 1.6 文字／媒體接收器","已啟動。返回手機確認接收器，再點選電腦輸入視窗。",ToolTipIcon.Info);}
 protected override bool ShowWithoutActivation {get{return true;}}
 bool Down(int k){return (GetAsyncKeyState(k)&0x8000)!=0;}
 void Cancel(){capture=false;buffer.Clear();target=IntPtr.Zero;}
 IntPtr OnKey(int code,IntPtr msg,IntPtr ptr){if(code<0)return CallNextHookEx(hook,code,msg,ptr);KEYDATA d=(KEYDATA)Marshal.PtrToStructure(ptr,typeof(KEYDATA));if((d.flags&0x10)!=0)return CallNextHookEx(hook,code,msg,ptr);int vk=(int)d.vk;bool down=msg==(IntPtr)0x100||msg==(IntPtr)0x104;bool up=msg==(IntPtr)0x101||msg==(IntPtr)0x105;
 if(down&&vk==0x7B&&Down(0x10)&&Down(0x11)&&Down(0x12)){buffer.Clear();capture=true;target=GetForegroundWindow();last=DateTime.UtcNow;return (IntPtr)1;}
 if(vk==0x7B&&up)return CallNextHookEx(hook,code,msg,ptr);
 if(swallowEnter&&vk==13&&up){swallowEnter=false;return (IntPtr)1;}
 if(!capture)return CallNextHookEx(hook,code,msg,ptr);
 if(vk==27){Cancel();return (IntPtr)1;}
 if(vk==13){if(down){swallowEnter=true;string frame=buffer.ToString();IntPtr expected=target;Cancel();BeginInvoke((Action)delegate{if(GetForegroundWindow()!=expected)return;try{if(frame.StartsWith("a2",StringComparison.OrdinalIgnoreCase))MediaKey(MediaFrame.Decode(frame));else Write(TextFrame.Decode(frame));}catch(FormatException){tray.ShowBalloonTip(2000,"AirPointer","文字封包未完成或有錯誤，已取消；請重新傳送。",ToolTipIcon.Warning);}});}return (IntPtr)1;}
 if((vk>=48&&vk<=57)||(vk>=65&&vk<=70)){if(down){last=DateTime.UtcNow;buffer.Append((char)(vk>=65?vk+32:vk));if(buffer.Length>136)Cancel();}return (IntPtr)1;}
 // Modifier releases belong to the frame opener. Other keys abort safely.
 if(vk!=16&&vk!=17&&vk!=18&&vk!=160&&vk!=161&&vk!=162&&vk!=163&&vk!=164&&vk!=165&&down)Cancel();
 return CallNextHookEx(hook,code,msg,ptr);
 }
 void MediaKey(ushort key){INPUT[] input=new INPUT[2];input[0].type=1;input[0].data.key.vk=key;input[1].type=1;input[1].data.key.vk=key;input[1].data.key.flags=2;SendInput(2,input,Marshal.SizeOf(typeof(INPUT)));}
 void Write(string s){INPUT[] input=new INPUT[s.Length*2];for(int i=0;i<s.Length;i++){input[i*2].type=1;input[i*2].data.key.scan=s[i];input[i*2].data.key.flags=4;input[i*2+1].type=1;input[i*2+1].data.key.scan=s[i];input[i*2+1].data.key.flags=6;}uint n=SendInput((uint)input.Length,input,Marshal.SizeOf(typeof(INPUT)));if(n!=(uint)input.Length)tray.ShowBalloonTip(2000,"AirPointer","目標程式未接受完整輸入。請使用一般權限程式，並檢查文字後再傳送。",ToolTipIcon.Warning);}
 protected override void Dispose(bool disposing){if(hook!=IntPtr.Zero){UnhookWindowsHookEx(hook);hook=IntPtr.Zero;}if(disposing){if(timer!=null)timer.Dispose();if(tray!=null){tray.Visible=false;tray.Dispose();}}base.Dispose(disposing);}
}

public static class KeyboardProgram {
 [STAThread] public static void Main(){
  using(var mutex=new System.Threading.Mutex(false,"Local\\AirPointerKeyboardReceiverV1")){
   if(!mutex.WaitOne(0,false)){MessageBox.Show("請先從系統匣退出舊版接收器，再啟動 AirPointer 1.6。","AirPointer 1.6");return;}
   try {Application.EnableVisualStyles();Application.Run(new AirPointerReceiver());}
   catch(Exception e){MessageBox.Show(e.Message,"AirPointer 1.6 文字／媒體接收器");}
   finally {mutex.ReleaseMutex();}
  }
 }
}
