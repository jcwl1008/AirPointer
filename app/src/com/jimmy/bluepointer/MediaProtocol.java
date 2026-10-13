package com.jimmy.bluepointer;
import java.util.*;
/** Media commands transported through the unchanged keyboard HID descriptor. */
public final class MediaProtocol {
 public static final int PLAY_PAUSE=1,STOP=2,MUTE=3,VOLUME_UP=4,VOLUME_DOWN=5;
 public static String frame(int command){if(command<1||command>5)throw new IllegalArgumentException("Media command");return String.format(Locale.US,"a2%02x%02x",command,command^0xC3);}
 public static List<int[]> events(int command){List<int[]> r=new ArrayList<>();r.add(new int[]{7,0x45});for(char c:frame(command).toCharArray())r.add(KeyboardProtocol.ascii(c));r.add(new int[]{0,0x28});return r;}
}
