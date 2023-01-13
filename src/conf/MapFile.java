package conf;

import utils.ReadFileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapFile {
    List<String> confNames;
    HashMap<String, List<String>> map;

    public MapFile(){
        confNames = new ArrayList<>();
        map = new HashMap<>();
    }

    public void getConfMap(String filePath) throws IOException {
        File mapFile = new File(filePath);
        int allLine = ReadFileUtils.getTotalLines(mapFile);
        String confTemp = "";
        for(int i = 1; i <= allLine; i++){
            String s = readByLine(mapFile, i);
            if(s.contains("--------")){
                System.out.println(s);
                confNames.add(s);
                confTemp = s;
            }else if(s.contains("LOG.error") || s.contains("LOG.warn")){
                System.out.println(s);
                if(map.containsKey(confTemp)){
                    map.get(confTemp).add(s);
                }else{
                    List<String> list = new ArrayList<>();
                    list.add(s);
                    map.put(confTemp, list);
                }
            }else{
                continue;
            }
        }
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
        MapFile mapFile = new MapFile();
        mapFile.getConfMap("./outputs/logMap/commonLogMap.txt");

    }
}
