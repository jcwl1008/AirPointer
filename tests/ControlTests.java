import com.jimmy.bluepointer.*;
import java.util.*;
public class ControlTests {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static int[] run(MotionEngine m,float p,float y,boolean invert){int x=0,z=0;for(int i=0;i<=60;i++){int[] d=m.step(1_000_000_000L+i*16_666_667L,p,y,1050,invert);x+=d[0];z+=d[1];}return new int[]{x,z};}
 public static void main(String[] args){
 MotionEngine m=new MotionEngine();check(Arrays.equals(run(m,0.01f,-0.01f,false),new int[]{0,0}),"Stationary noise must not drift");
 m.reset();int[] horizontal=run(m,0,-1,false);check(horizontal[0]>900&&horizontal[0]<1100&&horizontal[1]==0,"One radian yaw should produce predictable positive movement");
 m.reset();int[] vertical=run(m,1,0,false);check(vertical[1]<-900&&vertical[0]==0,"Pitch sign and independent axis");
 m.reset();int[] inverted=run(m,1,0,true);check(inverted[1]==-vertical[1],"Invert Y");
 m.reset();int[] first=m.step(1_000_000_000,1,1,1050,false);check(Arrays.equals(first,new int[]{0,0}),"Press must start without jump");
 int[] gap=m.step(3_000_000_000L,1,1,1050,false);check(Arrays.equals(gap,new int[]{0,0}),"Sensor resume after gap must not jump");
 int[] large=m.step(3_016_666_667L,1000,-1000,2100,false);check(Math.abs(large[0])<=127&&Math.abs(large[1])<=127,"HID signed range");
 check(Arrays.equals(HidReports.key(5,0x16),new byte[]{5,0,0x16,0,0,0,0,0}),"Ctrl+Alt+S report");
 check(Arrays.equals(HidReports.mouse(1,200,-200),new byte[]{1,127,-127}),"Mouse buttons and range");
 // Parse HID short items and verify host-visible input lengths match report payloads.
 int report=0,size=0,count=0;Map<Integer,Integer> bits=new HashMap<>(),outputs=new HashMap<>();byte[] d=HidReports.DESCRIPTOR;
 for(int i=0;i<d.length;){int prefix=d[i++]&255;int n=prefix&3;if(n==3)n=4;int value=0;for(int j=0;j<n;j++)value|=(d[i++]&255)<<(j*8);int tag=prefix&0xFC;if(tag==0x84)report=value;if(tag==0x74)size=value;if(tag==0x94)count=value;if(tag==0x80)bits.put(report,bits.getOrDefault(report,0)+size*count);if(tag==0x90)outputs.put(report,outputs.getOrDefault(report,0)+size*count);}
 check(bits.get(1)==24&&bits.get(2)==64,"Descriptor matches three-byte mouse and eight-byte keyboard");
 check(outputs.get(2)==8,"Windows keyboard LED output must be exactly one byte");
 RetryPolicy r=new RetryPolicy(3);check(r.takeAttempt()&&r.takeAttempt()&&r.takeAttempt(),"Three retry attempts allowed");check(!r.canAttempt()&&!r.takeAttempt(),"Disconnected host cannot create endless retries");check(r.delayMillis()<=8000,"Backoff bounded");r.reset();check(r.attempts()==0&&r.takeAttempt(),"New user selection restores retry budget");
 System.out.println("PASS: LED output, bounded retries, selection reset, noise, integration, axis directions, inversion, reset, gap, limits, shortcuts, descriptor payloads.");
 }
}
