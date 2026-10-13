package com.jimmy.bluepointer;
import java.util.*;
/** ASCII HID transport for Unicode, consumed only by the Windows helper. */
public final class KeyboardProtocol {
 public static int[] ascii(char c){
  if(c>='a'&&c<='z')return new int[]{0,4+c-'a'};
  if(c>='A'&&c<='Z')return new int[]{2,4+c-'A'};
  if(c>='1'&&c<='9')return new int[]{0,0x1E+c-'1'};
  if(c=='0')return new int[]{0,0x27};
  String plain=" -=[]\\;'`,./", shifted=" _+{}|:\"~<>?";
  int[] ids={0x2C,0x2D,0x2E,0x2F,0x30,0x31,0x33,0x34,0x35,0x36,0x37,0x38};
  int p=plain.indexOf(c);if(p>=0)return new int[]{0,ids[p]};p=shifted.indexOf(c);if(p>=0)return new int[]{2,ids[p]};
  String symbols="!@#$%^&*()";p=symbols.indexOf(c);if(p>=0)return new int[]{2,p==9?0x27:0x1E+p};
  if(c=='\n')return new int[]{0,0x28};if(c=='\t')return new int[]{0,0x2B};throw new IllegalArgumentException("Unsupported ASCII");
 }
 public static String frame(String text){if(text.length()<1||text.length()>32)throw new IllegalArgumentException("1–32 UTF-16 units required");StringBuilder b=new StringBuilder();int sum=0;for(int i=0;i<text.length();i++){int n=text.charAt(i);sum=(sum+n)&65535;b.append(String.format(Locale.US,"%04x",n));}return String.format(Locale.US,"a1%02x%04x",text.length(),sum)+b;}
 public static List<int[]> unicode(String text){ArrayList<int[]> r=new ArrayList<>();for(int pos=0;pos<text.length();){int end=Math.min(pos+32,text.length());if(end<text.length()&&Character.isHighSurrogate(text.charAt(end-1)))end--;String f=frame(text.substring(pos,end));r.add(new int[]{7,0x45});for(char c:f.toCharArray())r.add(ascii(c));r.add(new int[]{0,0x28});pos=end;}return r;}
}
