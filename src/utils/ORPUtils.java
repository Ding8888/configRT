package utils;

import conf.ConfFile;
import entity.ORPEntity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.LinkedList;
import java.util.List;

/**
*该类获得所有ORPResult的结果，包括类名，方法名（get-），行号。 */

public class ORPUtils {

    public static List<ORPEntity> getOrpEntityList() throws IOException {
        //common
//        String orpResultPath = "./orpResult/hadoop-common-orps-2.10.1.txt";
//        String confFilePath = "./confFile/core-default.txt";
        //hdfs
//        String orpResultPath = "./orpResult/hadoop-hdfs-orps-2.10.1.txt";
//        String confFilePath = "./confFile/hdfs-default.txt";
        //mapreduce
//        String orpResultPath = "./orpResult/hadoop-mapreduce-orps-2.10.1.txt";
//        String confFilePath = "./confFile/mapered-default.txt";
        //yarn
//        String orpResultPath = "./orpResult/hadoop-yarn-orps-2.10.1.txt";
//        String confFilePath = "./confFile/yarn-default.txt";
        //all
        String orpResultPath = "./orpResult/hadoop-all-orps-2.10.1.txt";
        File file = new File(orpResultPath);
        List<String> confListInFile = ConfFile.getconfListInFile();
        System.out.println("All conf in File:"+confListInFile.size());
        //读取文件获得entity
        List<ORPEntity> list = new LinkedList<>();
        int allLineNumber = ReadFileUtils.getTotalLines(file);
        int numberOfOrp = 0;
        int numberOfOrpInFile = 0;
        int methodLine = 1;
        int confMethod = 2;
        int i = 1;
        int confCount = 0;
        while(i < allLineNumber){
            String method = readByLineNumber(file,methodLine);
            String conf = readByLineNumber(file,confMethod);
            String className = getClassName(method);
            String methodName = getMethodName(method);
            int lineNumber = Integer.parseInt(getLineNumber(method));
            String confName = getConfName(conf);

            if(methodName!=null && confName!=null) {
                numberOfOrp++;
                //配置项在官方默认配置文件中出现
                if(confListInFile.contains(confName)){
                    numberOfOrpInFile++;
                    String classNameofinst = classNametoInst(className);
                    ORPEntity entity = new ORPEntity(className, classNameofinst, methodName, confName, lineNumber);
                    int has = 0;
                    for(ORPEntity orpEntity : list){
                        if(orpEntity.getConfName().equals(confName)){
                            has = 1;
                        }
                    }
                    if(has == 0){
                        confCount++;
                    }
                    list.add(entity);
                }
            }
            methodLine +=3;
            confMethod +=3;
            i+=3;
        }
        System.out.println("number of ORP:"+numberOfOrp);//orp数量
        System.out.println("number of ORP in file:"+numberOfOrpInFile);//在配置文件中出现的
        System.out.println("all confCount:"+confCount);//不含重复的
        return list;
    }

    private static String readByLineNumber(File file, int lineNum) throws IOException {
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String s = "";
        if (lineNum <= 0 || lineNum > ReadFileUtils.getTotalLines(file)) {
            System.out.println("不在文件的行数范围(1至总行数)之内。");
            return null;
        }
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
            if (lines >= lineNum) {
                return s;
            }
        }
        reader.close();
        in.close();
        return null;
    }

    public static String classNametoInst(String className){
        StringBuilder classNameofinst = new StringBuilder();
        //if(className != null){
            String[] classNames = className.split("\\.");
            for(int i = 0; i < classNames.length; i++){
                if(i != classNames.length-1){
                    classNameofinst.append(classNames[i]).append("/");
                }else{
                    classNameofinst.append(classNames[i]);
                }
            }
            return classNameofinst.toString();
        //}
        //System.err.println("className is null, can't get classNameofInst");
        //return null;
    }

    //分解methodString获得className
    static String getClassName(String s){
        String[] list = s.split(":");
        String ss = list[1];
        String sss = ss.strip();
        String className = sss.substring(0,sss.length()-13);
        //System.out.println("getClassName:"+className);
        return className;
    }

    //分解methodString获得lineNumber
    static String getLineNumber(String s){
        String[] list = s.split(":");
        String ss = list[2];
        String sss  = ss.strip();
        String lineNumber = sss.substring(0,sss.length()-12);
        return lineNumber;
    }

    //分解methodString获得methodName
    static String getMethodName(String s){
        String[] list = s.split(":");
        String ss = list[3];
        String sss = ss.strip();
        if(sss.contains(".")&&sss.contains("(")) {
            String ssss = sss.substring(0, sss.indexOf("("));
            String methodName = ssss.substring(ssss.indexOf(".")+1,ssss.length());
            //System.out.println("getMethodName:" + methodName.strip());
            return methodName.strip();
        }
        return null;
    }

    //分解conf获得confName
    static String getConfName(String s){
        String confName = "";
        String[] list = s.split(":");
        if(!list[1].equals(" ")){
            if(!list[1].contains(".")){
                return null;
            }
            String ss = list[1];
            String confName1 = ss.strip();
            if(confName1.contains(",")){
                confName = confName1.substring(0, confName1.indexOf(","));
            }
            else{
                confName = confName1;
            }
            //System.out.println("confName:"+confName);
            return confName;
        }
        //System.out.println("no confName!");
        return null;
    }

    //test
    public static void main(String[] arg) throws IOException {
        List<ORPEntity> list = getOrpEntityList();
        int h = 1;
        for(ORPEntity entity: list){
            System.out.println("["+h+"]");
            System.out.println("confName:"+entity.getConfName());
            System.out.println("className:"+entity.getClassName());
            System.out.println("classNameofinst:"+entity.getClassNameofinst());
            System.out.println("MethodName:"+entity.getMethodName());
            System.out.println("lineNumber:"+entity.getLineNumber());
            System.out.println();
            h++;
        }
    }

}

