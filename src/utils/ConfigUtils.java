package utils;

import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.slicer.Statement;
import conf.InitialSeed;
import entity.ConfigEntity;
import entity.InitialSeedEntity;
import entity.ORPEntity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *该类通过ORP的结果，获得切片用的entity列表。 （只包含含有this的）*/

public class ConfigUtils {

    public static String sourcePath;

    //common
//    public static List<String> sourcePaths = new ArrayList<String>(){
//        {
//            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-annotations/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth-examples/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-common/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-kms/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-minikdc/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-nfs/src/main/java/");
//        }
//    };
    //hdfs
//    public static List<String> sourcePaths = new ArrayList<String>(){
//        {
//            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-native-client/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-nfs/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-rbf/src/main/java/");
//        }
//    };
    //mapreduce
//    public static List<String> sourcePaths = new ArrayList<String>(){
//        {
//            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-app/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-common/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs-plugins/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-jobclient/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-shuffle/src/main/java/");
//        }
//    };

    //yarn
//    public static List<String> sourcePaths = new ArrayList<String>(){
//        {
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-api/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-unmanaged-am-launcher/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-client/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-registry/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-applicationhistoryservice/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-common/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-resourcemanager/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-router/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-sharedcachemanager/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-tests/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timeline-pluginstorage/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase-tests/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-web-proxy/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-ui/src/main/java/");
//        }
//    };

    //all
    public static List<String> sourcePaths = new ArrayList<String>(){
        {
            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-annotations/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth-examples/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-kms/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-minikdc/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-nfs/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-native-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-nfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-rbf/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-app/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs-plugins/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-jobclient/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-shuffle/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-api/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-unmanaged-am-launcher/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-registry/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-applicationhistoryservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-resourcemanager/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-router/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-sharedcachemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timeline-pluginstorage/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-web-proxy/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-ui/src/main/java/");
        }
    };

    public static List<ConfigEntity> getConfigEntityList() throws IOException {
        List<ConfigEntity> configEntityList = new LinkedList<ConfigEntity>();
        List<ORPEntity> entities = ORPUtils.getOrpEntityList();
        int i = 1;
        int count = 0;
        for(ORPEntity entity:entities){
            String className = entity.getClassName();
            String classNameofinst= entity.getClassNameofinst();
            String methodName = entity.getMethodName();
            String confName = entity.getConfName();
            int lineNumber = entity.getLineNumber();
            for(int j = 0; j < sourcePaths.size(); j++){
                String s = sourcePaths.get(j) + classNameofinst + ".java";
                File f = new File(s);
                if(f.exists()){
                    sourcePath = sourcePaths.get(j);
                    break;
                }
            }
            String filePath = sourcePath+classNameofinst+".java";
            File sourceFile = new File(filePath);
            String source;
            try {
                source = ReadFileUtils.readByLineNumber(sourceFile, lineNumber);
                if (source != null) {
                    //删除空行
                    String sourceAfterTrip = source.strip();
                    //System.out.println("source:" + sourceAfterTrip);
                    if(sourceAfterTrip.contains("this")){
                        count++;
                        String confVariable = getConfVariable(sourceAfterTrip);
                        //System.out.println("confVariable"+confVariable);
                        ConfigEntity configEntity = new ConfigEntity(className, classNameofinst, confVariable, confName);
                        configEntityList.add(configEntity);
                    }
                } else {
                    System.out.println("source of " + lineNumber + " is null!");
                }
            }catch (Exception e){
                continue;
            }
            i++;
        }
        System.out.println("------------------------------------");
        System.out.println("number of entities："+entities.size());
        System.out.println("--------------------------------------");
        return configEntityList;
    }

    public static String getConfVariable(String source){
        return source.substring(source.indexOf(".")+1,source.indexOf("=")-1);
    }



    public static void main(String[] args) throws IOException {
        List<ConfigEntity> list = getConfigEntityList();
        System.out.println("Number of containing this in ORPResult:"+list.size());
        int h = 1;
        for(ConfigEntity entity : list){
            String className = entity.getClassName();
            String classNameofInst = entity.getClassNameofInst();
            String confVariable = entity.getConfVariable();
            String confName = entity.getConfName();
            System.out.println("["+h+"]");
            System.out.println("className:"+className);
            System.out.println("classNameofInst:"+classNameofInst);
            System.out.println("confVariable:"+confVariable);
            System.out.println("confName:"+confName);
            System.out.println();
            h++;
        }
    }
}
