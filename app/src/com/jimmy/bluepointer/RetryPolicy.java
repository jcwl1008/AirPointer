package com.jimmy.bluepointer;
/** Finite retry budget per user session/selection; successful connections do not reset it. */
public final class RetryPolicy {
 private final int max;private int attempts;
 public RetryPolicy(int max){if(max<1)throw new IllegalArgumentException();this.max=max;}
 public void reset(){attempts=0;}
 public int attempts(){return attempts;}
 public boolean canAttempt(){return attempts<max;}
 public boolean takeAttempt(){if(!canAttempt())return false;attempts++;return true;}
 public long delayMillis(){return Math.min(8000,1000L<<attempts);}
}
