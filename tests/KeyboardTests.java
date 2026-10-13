import com.jimmy.bluepointer.*;import java.io.*;import java.util.*;
public class KeyboardTests {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 static String decode(String f){check(f.startsWith("a1"),"Header");int n=Integer.parseInt(f.substring(2,4),16),sum=Integer.parseInt(f.substring(4,8),16);check(f.length()==8+n*4,"Length");int actual=0;StringBuilder s=new StringBuilder();for(int i=0;i<n;i++){char c=(char)Integer.parseInt(f.substring(8+i*4,12+i*4),16);s.append(c);actual=(actual+c)&65535;}check(actual==sum,"Checksum");return s.toString();}
 public static void main(String[] args)throws Exception{
  for(String s:new String[]{"香港繁體中文","你好\n世界","𠮷😀測試","abc 123"})check(decode(KeyboardProtocol.frame(s)).equals(s),"Roundtrip");
  check(KeyboardProtocol.ascii('a')[1]==4&&KeyboardProtocol.ascii('Z')[0]==2,"Letters");check(KeyboardProtocol.ascii('0')[1]==0x27&&KeyboardProtocol.ascii('!')[0]==2,"Numbers symbols");
  List<int[]> events=KeyboardProtocol.unicode("香港");check(events.get(0)[0]==7&&events.get(0)[1]==0x45,"Frame shortcut");check(events.get(events.size()-1)[1]==0x28,"Frame terminator");
  String longText=new String(new char[31]).replace('\0','中')+"😀"+"文";events=KeyboardProtocol.unicode(longText);int starts=0;for(int[] k:events)if(k[0]==7&&k[1]==0x45)starts++;check(starts==2,"Surrogate-safe batching");
  QuickEngine q=new QuickEngine();q.load(new FileInputStream(args[0]));check(q.find("on").contains("今"),"Cangjie ON 今");check(q.find("or").contains("個"),"Cangjie OR 個");check(q.find("m").contains("一"),"Single root 一");check(q.find("zz").isEmpty(),"Invalid code");check(q.related("香").contains("港"),"Related word");check(q.related("香港").contains("人"),"Related phrase");check(QuickEngine.roots("on").equals("人弓"),"Root labels");
  List<String> fixed=new ArrayList<>(q.find("or"));check(fixed.equals(q.find("or")),"Stable order");
  System.out.println("PASS: Chinese/supplementary Unicode, checksum, framing, ASCII, surrogate chunking, dictionary lookups, related words, stable order.");
 }
}
