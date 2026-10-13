package com.jimmy.bluepointer;
import java.io.*;import java.util.*;
/** Fixed candidate order from the distributed Unicode data; no adaptive reordering. */
public final class QuickEngine {
 final Map<String,String> trad=new HashMap<>();
 final Map<String,List<String>> table=new LinkedHashMap<>();
 public void load(InputStream in)throws IOException{try(BufferedReader b=new BufferedReader(new InputStreamReader(in,"UTF-8"))){String s;while((s=b.readLine())!=null){String[] p=s.split("\t");if(p.length<2)continue;table.computeIfAbsent(p[0],k->new ArrayList<>()).add(p[1]);}}}
 public void loadTraditional(InputStream in)throws IOException{try(BufferedReader b=new BufferedReader(new InputStreamReader(in,"UTF-8"))){String l;while((l=b.readLine())!=null){String[] p=l.split("\t");if(p.length==2)trad.put(p[0],p[1]);}}}
 public String traditional(String text){StringBuilder s=new StringBuilder();for(int i=0;i<text.length();){int cp=text.codePointAt(i);String c=new String(Character.toChars(cp));s.append(trad.containsKey(c)?trad.get(c):c);i+=Character.charCount(cp);}return s.toString();}
 public List<String> find(String code){if(code.isEmpty())return Collections.emptyList();List<String> r=table.get(code);return r==null?Collections.emptyList():Collections.unmodifiableList(r);}
 public static String roots(String code){String out="";String r="日月金木水火土竹戈十大中一弓人心手口尸廿山女田難卜重";for(char c:code.toCharArray())if(c>='a'&&c<='z')out+=r.charAt(c-'a');return out;}
 public List<String> related(String prefix){String[] phrases={"你好","你好嗎","你們","我們","我的","我哋","香港","香港人","香港島","中國","中文","中文輸入","中學","三星","三星手機","三星電子","手機","手機號碼","電腦","電腦鍵盤","電郵","電話","電子郵件","鍵盤","鍵盤輸入","輸入","輸入法","速成","速成輸入法","簡報","簡報內容","簡報模式","工作","工作坊","會議","會議記錄","培訓","培訓課程","謝謝","謝謝你","多謝","多謝晒","可以","可以嗎","時間","今日","今天","明天","明日","資料","資料夾","下載","上載","連接","連接電腦","設定","設定完成","完成","大家","大家好","早晨","早上","晚上","上午","下午","朋友","同事","學生","老師","產品","產品介紹","功能","功能比較","銀河","生日","生日快樂"};LinkedHashSet<String> out=new LinkedHashSet<>();for(String s:phrases)for(int n=Math.min(prefix.length(),6);n>0;n--){String tail=prefix.substring(prefix.length()-n);if(s.startsWith(tail)&&s.length()>tail.length()){out.add(s.substring(tail.length()));break;}}return new ArrayList<>(out);}
}
