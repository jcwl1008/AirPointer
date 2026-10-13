package com.jimmy.bluepointer;

/** Calculator-inspired tokens; never bundle proprietary Apple fonts. */
public final class AppTheme {
 public static final int CANVAS=0xFF000000, SURFACE=0xFF333333,
  SURFACE_ALT=0xFF272729, UTILITY=0xFFA5A5A5, ACCENT=0xFFFF9F0A,
  INK=0xFFFFFFFF, MUTED=0xFFCCCCCC, HAIRLINE=0xFF555555;
 private AppTheme() {}
 public static int buttonInk(int fill){return fill==UTILITY||fill==ACCENT?CANVAS:INK;}
}
