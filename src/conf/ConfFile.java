package conf;

import utils.ReadFileUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/*
* 本类主要用于读取xml配置文件中的配置项*/

public class ConfFile {
    //获得配置文件手册中的配置项列表
    public static List<String> getconfListInFile() throws IOException {
        String confFilePath = "./confFile/all-default.txt";
        File confFile = new File(confFilePath);
        List<String> confListInFile = new LinkedList<>();
        int allLine = ReadFileUtils.getTotalLines(confFile);
        for(int i = 1; i < allLine; i++){
            String s  = readByLine(confFile,i);
            String ss = s.strip();
            if(ss.length()>7){
                String start = ss.substring(0,6);
                if(start.equals("<name>")&&ss.contains("</")){
                    String confinFile = ss.substring(ss.indexOf(">")+1,ss.lastIndexOf("<"));
                    confListInFile.add(confinFile);
                }
            }
        }
        return confListInFile;
    }

    private static String readByLine(File file, int lineNum) throws IOException {
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String s = "";
        if (lineNum <= 0 || lineNum > ReadFileUtils.getTotalLines(file)) {
            System.out.println("不在文件的行数范围(1至总行数)之内。");
            return null;
        }
        int lines = 0;
        StringBuilder source = new StringBuilder();
        while (s != null) {
            lines++;
            s = reader.readLine();
            if (lines >= lineNum) {
                return s;
            }
        }
        reader.close();
        in.close();
        return source.toString();
    }

    public static void main(String[] args) throws IOException {
        ConfFile.getconfListInFile();
    }



}
