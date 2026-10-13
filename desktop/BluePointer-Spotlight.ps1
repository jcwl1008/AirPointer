# Blue Pointer local Windows spotlight. No network access or administrator rights.
Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing
Add-Type -ReferencedAssemblies System.Windows.Forms,System.Drawing -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Runtime.InteropServices;
using System.Windows.Forms;

public sealed class BlueSpotlight : Form {
 [DllImport("user32.dll")] static extern bool RegisterHotKey(IntPtr h,int id,uint mods,uint key);
 [DllImport("user32.dll")] static extern bool UnregisterHotKey(IntPtr h,int id);
 [DllImport("user32.dll")] static extern bool SetProcessDPIAware();
 [DllImport("user32.dll")] static extern bool SetWindowPos(IntPtr h,IntPtr after,int x,int y,int cx,int cy,uint flags);
 readonly Timer follow = new Timer();
 NotifyIcon tray; bool active; int radius=110;
 protected override bool ShowWithoutActivation {get{return true;}}
 protected override CreateParams CreateParams {get{CreateParams p=base.CreateParams;p.ExStyle |= 0x20|0x80000|0x8000000|0x80;return p;}}
 public BlueSpotlight(){
  FormBorderStyle=FormBorderStyle.None;ShowInTaskbar=false;TopMost=true;BackColor=Color.Black;Opacity=0.62;StartPosition=FormStartPosition.Manual;
  Bounds=SystemInformation.VirtualScreen;
  IntPtr h=Handle;
  if(!RegisterHotKey(h,1,0x4003,0x53))throw new InvalidOperationException("Ctrl+Alt+S is in use. Close the other spotlight helper and try again.");
  RegisterHotKey(h,2,0x4003,0x51);
  RegisterHotKey(h,3,0x4003,0xBB);
  RegisterHotKey(h,4,0x4003,0xBD);
  follow.Interval=16;follow.Tick+=delegate{UpdateHole();};
  ContextMenuStrip menu=new ContextMenuStrip();
  menu.Items.Add("Toggle spotlight (Ctrl+Alt+S)",null,delegate{Toggle();});
  menu.Items.Add("Larger circle",null,delegate{radius=Math.Min(400,radius+20);UpdateHole();});
  menu.Items.Add("Smaller circle",null,delegate{radius=Math.Max(40,radius-20);UpdateHole();});
  menu.Items.Add("Exit (Ctrl+Alt+Q)",null,delegate{Application.ExitThread();});
  tray=new NotifyIcon();tray.Icon=SystemIcons.Information;tray.Text="Blue Pointer Spotlight";tray.ContextMenuStrip=menu;tray.Visible=true;tray.DoubleClick+=delegate{Toggle();};
 }
 void UpdateHole(){if(!active)return;Rectangle area=SystemInformation.VirtualScreen;if(Bounds!=area)Bounds=area;Point p=Cursor.Position;using(GraphicsPath circle=new GraphicsPath()){
  circle.AddEllipse(p.X-area.X-radius,p.Y-area.Y-radius,radius*2,radius*2);
  Region updated=new Region(new Rectangle(0,0,area.Width,area.Height));updated.Exclude(circle);Region old=Region;Region=updated;if(old!=null)old.Dispose();
 }}
 void Toggle(){active=!active;if(active){UpdateHole();Show();SetWindowPos(Handle,new IntPtr(-1),0,0,0,0,0x13);follow.Start();}else{follow.Stop();Hide();}}
 protected override void WndProc(ref Message m){if(m.Msg==0x312){int id=m.WParam.ToInt32();if(id==1)Toggle();else if(id==2)Application.ExitThread();else if(id==3){radius=Math.Min(400,radius+20);UpdateHole();}else if(id==4){radius=Math.Max(40,radius-20);UpdateHole();}return;}base.WndProc(ref m);}
 protected override void Dispose(bool disposing){if(disposing){follow.Dispose();UnregisterHotKey(Handle,1);UnregisterHotKey(Handle,2);UnregisterHotKey(Handle,3);UnregisterHotKey(Handle,4);if(tray!=null){tray.Visible=false;tray.Dispose();}}base.Dispose(disposing);}
 [STAThread] public static void Main(){RunHelper();}
 public static void RunHelper(){SetProcessDPIAware();Application.EnableVisualStyles();using(BlueSpotlight helper=new BlueSpotlight()){Application.Run(new ApplicationContext());}}
}
'@
Write-Host 'Blue Pointer Spotlight is ready.'
Write-Host 'Phone Spotlight button / Ctrl+Alt+S: toggle. Ctrl+Alt+Q: exit.'
Write-Host 'Ctrl+Alt+Plus / Minus: resize. Tray icon: more options.'
[BlueSpotlight]::RunHelper()
