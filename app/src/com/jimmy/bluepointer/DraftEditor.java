package com.jimmy.bluepointer;
/** Caret operations use Unicode codepoints, never splitting surrogate pairs. */
public final class DraftEditor {
 public String text="";public int cursor=0;
 public void set(String value,int offset){text=value;cursor=Math.max(0,Math.min(offset,text.length()));if(cursor>0&&cursor<text.length()&&Character.isLowSurrogate(text.charAt(cursor))&&Character.isHighSurrogate(text.charAt(cursor-1)))cursor--;}
 public void insert(String value){text=text.substring(0,cursor)+value+text.substring(cursor);cursor+=value.length();}
 public void backspace(){if(cursor==0)return;int start=text.offsetByCodePoints(cursor,-1);text=text.substring(0,start)+text.substring(cursor);cursor=start;}
 public void move(int dx,int dy){while(dx<0&&cursor>0){cursor=text.offsetByCodePoints(cursor,-1);dx++;}while(dx>0&&cursor<text.length()){cursor=text.offsetByCodePoints(cursor,1);dx--;}
 if(dy==0)return;int start=text.lastIndexOf('\n',Math.max(-1,cursor-1))+1;int col=text.codePointCount(start,cursor);
 if(dy<0){if(start==0){cursor=0;return;}int prevEnd=start-1;int prevStart=text.lastIndexOf('\n',prevEnd-1)+1;cursor=text.offsetByCodePoints(prevStart,Math.min(col,text.codePointCount(prevStart,prevEnd)));}
 else{int end=text.indexOf('\n',cursor);if(end<0){cursor=text.length();return;}int nextEnd=text.indexOf('\n',end+1);if(nextEnd<0)nextEnd=text.length();cursor=text.offsetByCodePoints(end+1,Math.min(col,text.codePointCount(end+1,nextEnd)));}}
}
