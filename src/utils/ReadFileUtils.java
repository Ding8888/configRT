package utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * 读源码文件 */
public class ReadFileUtils {

    public static String readByLineNumber(File sourceFile, int lineNumber)
            throws IOException {
        FileReader in = new FileReader(sourceFile);
        LineNumberReader reader = new LineNumberReader(in);
        String s = "";
        if (lineNumber <= 0 || lineNumber > getTotalLines(sourceFile)) {
            System.out.println("不在文件的行数范围(1至总行数)之内。");
            return null;
        }
        int lines = 0;
        StringBuilder source = new StringBuilder();
        while (s != null) {
            lines++;
            s = reader.readLine();
            if(s != null && (!s.contains("*")) && (!s.contains("/"))) {//除去注释的消息。
                if (lines >= lineNumber) {
                    if (s != null && s.length() >= 1 && s.charAt(s.length() - 1) == ';') {
                        String ss = s.strip();
                        source.append(ss);
                        break;
                    }
                    if(s != null && s.length() >= 1 && ((s.charAt(s.length()-1) == '{') || (s.charAt(s.length()-1) == '}') || s.contains("@"))){
                        String ss = s.strip();
                        source.append(ss);
                        break;
                    }
                    if (s != null) {
                        String ss = s.strip();
                        source.append(" "+ss);
                    }
                }
            }
        }
        reader.close();
        in.close();
        return source.toString();
    }

    //获取文件总行数
    public static int getTotalLines(File file) throws IOException {
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String s = reader.readLine();
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
        }
        reader.close();
        in.close();
        return lines;
    }

}
