package com.jimmy.bluepointer;
/** Pure, independently testable gyro integration. Phone held upright; yaw=horizontal, pitch=vertical. */
public final class MotionEngine {
 private long last; private double fx,fy,sx,sy;
 public void reset(){last=0;fx=fy=sx=sy=0;}
 public int[] step(long timestamp,float pitch,float yaw,float gain,boolean invert){
  if(last==0){last=timestamp;return new int[]{0,0};}
  double dt=(timestamp-last)/1e9;last=timestamp;
  if(dt<=0||dt>0.1){fx=fy=sx=sy=0;return new int[]{0,0};}
  // Dead zone is applied BEFORE filtering to suppress stationary sensor noise.
  double x=Math.abs(yaw)<0.018?0:-yaw, y=Math.abs(pitch)<0.018?0:-pitch;
  double alpha=1-Math.exp(-dt/0.035);sx+=alpha*(x-sx);sy+=alpha*(y-sy);
  fx+=sx*dt*gain;fy+=sy*dt*gain*(invert?-1:1);
  int dx=(int)fx,dy=(int)fy;fx-=dx;fy-=dy;
  return new int[]{Math.max(-127,Math.min(127,dx)),Math.max(-127,Math.min(127,dy))};
 }
}
