package conf;

import entity.ConfigEntity;
import entity.ORPEntity;
import utils.ReadFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfKey {

    public static List<String> sourcePaths = new ArrayList<String>(){
        {
            add("./tests/confKey/CommonConfigurationKeys.java");add("./tests/confKey/CommonConfigurationKeysPublic.java");
            add("./tests/confKey/DFSConfigKeys.java");add("./tests/confKey/HdfsClientConfigKeys.java");add("./tests/confKey/RBFConfigKeys.java");
            add("./tests/confKey/MRJobConfig.java");add("./tests/confKey/YarnConfiguration.java");
        }
    };

//    public static List<ConfigEntity> getConfigEntityList() throws IOException {
//        List<ConfigEntity> configEntityList = new ArrayList<>();
//        Map<String, String> map = getConfKey();
//        for(String k : map.keySet()){
//            String
//            String confName =
//        }
//
//        return configEntityList;
//    }
    public static List<ConfigEntity> getConfigEntityList() throws IOException {
        List<ConfigEntity> configEntityList = new ArrayList<>();
        Map <String, String> map = new HashMap<>();
        for(String sourcePath : sourcePaths){
            File file = new File(sourcePath);
            //获得配置文件中的配置项名称
            List<String> conffileList = ConfFile.getconfListInFile();
            //获得java文件中的键值对对应
            map = getMap(file);
            int countInFile = 0;
            String className = "";
            String classNameofInst = "";
            String confVariable = "";
            String confName = "";
            for (String k : map.keySet()) {
                if(conffileList.contains(map.get(k))){
                    if(sourcePath.equals("./tests/confKey/CommonConfigurationKeys.java")){className = "org.apace.hadoop.fs.CommonConfigurationKeys"; classNameofInst = "org/apace/hadoop/fs/CommonConfigurationKeys"; }
                    else if(sourcePath.equals("./tests/confKey/CommonConfigurationKeysPublic.java")){className = "org.apace.hadoop.fs.CommonConfigurationKeysPublic"; classNameofInst = "org/apace/hadoop/fs/CommonConfigurationKeysPublic"; }
                    else if(sourcePath.equals("./tests/confKey/DFSConfigKeys.java")){className = "org.apache.hadoop.hdfs.DFSConfigKeys"; classNameofInst = "org/apache/hadoop/hdfs/DFSConfigKeys";}
                    else if(sourcePath.equals("./tests/confKey/HdfsClientConfigKeys.java")){className = "org.apache.hadoop.hdfs.client.HdfsClientConfigKeys"; classNameofInst = "org/apache/hadoop/hdfs/client/HdfsClientConfigKeys";}
                    else if(sourcePath.equals("./tests/confKey/RBFConfigKeys.java")){className = "org.apache.hadoop.hdfs.server.federation.router.RBFConfigKeys"; classNameofInst = "org/apache/hadoop/hdfs/server/federation/router/RBFConfigKeys";}
                    else if(sourcePath.equals("./tests/confKey/MRJobConfig.java")){className = "org.apache.hadoop.mapreduce.MRJobConfig"; classNameofInst = "org/apache/hadoop/mapreduce/MRJobConfig";}
                    else{className = "org.apache.hadoop.yarn.conf.YarnConfiguration"; classNameofInst = "org/apache/hadoop/yarn/conf/YarnConfiguration";}
                    confVariable = k;
                    confName = map.get(k);
                    ConfigEntity entity = new ConfigEntity(className,classNameofInst,confVariable,confName);
                    configEntityList.add(entity);
                    countInFile++;
                }
            }
            System.out.println("number of confInFile "+sourcePath+" : "+countInFile);
        }
        return configEntityList;
    }

    public static Map<String, String> getConfKey() throws IOException {
        Map <String, String> map = new HashMap<>();
        Map<String,String> resultMap = new HashMap<>();
        for(String sourcePath : sourcePaths){
            File file = new File(sourcePath);
            //获得配置文件中的配置项名称
            List<String> conffileList = ConfFile.getconfListInFile();
            //获得java文件中的键值对对应
            map = getMap(file);
            int countInFile = 0;
            for (String k : map.keySet()) {
                if(conffileList.contains(map.get(k))){
                    countInFile++;
                    resultMap.put(k,map.get(k));
                    System.out.println(k+"="+map.get(k));
                }
            }
            System.out.println("number of confInFile "+sourcePath+" : "+countInFile);
        }
        return resultMap;
    }

    //判断字符串中是否含有大写
    public static boolean containDaxie(String str){
        char[] c=str.toCharArray();
        for(int i=0;i<str.length();i++){
            if(c[i]>='A'&&c[i]<='Z'){
                return true;
            }
        }
        return false;
    }

    //分离java文件中的key和confName
    public static Map<String,String> getMap(File file) throws IOException {
        Map<String,String> map = new HashMap<>();
        int totallinNum = ReadFileUtils.getTotalLines(file);
        String tempKey = "";
        //再构建map
        for(int i = 1; i <= totallinNum; i++){
            String sourceline = ReadFileUtils.readByLineNumber(file,i);
            if(sourceline.contains("(") || sourceline.contains(")") || sourceline.contains("{") || sourceline.contains("}")){
                continue;
            }
            if(containDaxie(sourceline) && sourceline.contains("=")){
                String s = sourceline.strip();
                String[] strings = s.split("=");
                if(strings.length == 2){
                    String keySource = strings[0];
                    String[] keySources = keySource.split(" ");
                    if(keySources.length >= 2){
                        String key = keySources[keySources.length-1];//"+"左边的最后一个字符串
                        String confNameSource = strings[1];
                        String confName;
                        //如果不包含“+”,confName为去掉引号的值
                        if((!confNameSource.contains("+")) && confNameSource.contains("\"")) {
                            confName = confNameSource.substring(confNameSource.indexOf("\"")+1, confNameSource.lastIndexOf("\""));
                        }
                        else{
                            String c = "";
                            if(confNameSource.contains("+") && ConfKey.containDaxie(confNameSource)){
                                String[] confNameStrings = confNameSource.split("\\+");
                                if(confNameStrings.length >=2){
                                    //System.out.println("confNameStrings[0]:"+confNameStrings[0]);
                                    //查看已有的tempmap中的值
                                    for (String k : map.keySet()) {
                                        if (confNameStrings[0].strip().equals(k)) {
                                            //System.out.println("k:"+k);
                                            if(confNameStrings[1].contains("\"")){
                                                c = map.get(k) + confNameStrings[1].substring(confNameStrings[1].indexOf("\"")+1,confNameStrings[1].lastIndexOf("\""));
                                                //System.out.println("c:"+c);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            confName = c;
                        }
                        if(!key.equals(tempKey)){
                            map.put(key,confName);
                            tempKey = key;
                            //System.out.println(key + " : " + confName);
                        }
                    }
                }
            }
        }
        return map;
    }

    //test
    public static void main(String[] args) throws IOException {
        List<ConfigEntity> configEntityList = ConfKey.getConfigEntityList();
        System.out.println("Size if List:"+configEntityList.size());
        int h = 1;
        for(ConfigEntity entity: configEntityList){
            System.out.println("["+h+"]");
            System.out.println("className:"+entity.getClassName());
            System.out.println("confName:"+entity.getConfName());
            System.out.println("confVariable:"+entity.getConfVariable());
            System.out.println();
            h++;
        }
    }


}
