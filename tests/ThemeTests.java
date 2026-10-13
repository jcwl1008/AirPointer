import com.jimmy.bluepointer.AppTheme;
public class ThemeTests {
 static double channel(int v){double s=v/255.0;return s<=.04045?s/12.92:Math.pow((s+.055)/1.055,2.4);}
 static double luminance(int c){return .2126*channel((c>>16)&255)+.7152*channel((c>>8)&255)+.0722*channel(c&255);}
 static void check(int fill,int text){double a=luminance(fill),b=luminance(text);double ratio=(Math.max(a,b)+.05)/(Math.min(a,b)+.05);if(ratio<4.5)throw new AssertionError("Text contrast "+ratio);}
 public static void main(String[] args){for(int color:new int[]{AppTheme.SURFACE,AppTheme.SURFACE_ALT,AppTheme.UTILITY,AppTheme.ACCENT})check(color,AppTheme.buttonInk(color));check(AppTheme.CANVAS,AppTheme.INK);check(AppTheme.SURFACE,AppTheme.MUTED);System.out.println("PASS: calculator palette and text contrast >= 4.5:1");}
}
