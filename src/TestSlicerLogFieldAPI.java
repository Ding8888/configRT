import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.slicer.Statement;
import diagSlicer.IRStatement;
import entity.ConfigEntity;
import utils.ConfigUtils;
import utils.ReadFileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestSlicerLogFieldAPI {
    //static String path = "./tests/hadoop-common-jar/hadoop-common-2.10.1.jar;./tests/hadoop-common-jar/hadoop-nfs-2.10.1.jar";
    //static String path = "./tests/hadoop-hdfs-jar/hadoop-hdfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-client-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-native-client-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-nfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-rbf-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-nfs-2.10.1.jar";
    //static String path = "./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-app-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-common-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-core-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-hs-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-hs-plugins-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-jobclient-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-shuffle-2.10.1.jar";
    //static String path = "./tests/hadoop-yarn-jar/hadoop-yarn-api-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-applications-distributedshell-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-applications-unmanaged-am-launcher-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-client-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-common-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-registry-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-applicationhistoryservice-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-common-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-nodemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-resourcemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-router-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-sharedcachemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-timeline-pluginstorage-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-web-proxy-2.10.1.jar";
    static String path = "./tests/hadoop-common-jar/hadoop-common-2.10.1.jar;./tests/hadoop-common-jar/hadoop-nfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-client-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-native-client-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-nfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-rbf-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-nfs-2.10.1.jar;" +
            "./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-app-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-common-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-core-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-hs-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-hs-plugins-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-jobclient-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-shuffle-2.10.1.jar;" +
            "./tests/hadoop-yarn-jar/hadoop-yarn-api-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-applications-distributedshell-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-applications-unmanaged-am-launcher-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-client-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-common-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-registry-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-applicationhistoryservice-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-common-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-nodemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-resourcemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-router-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-sharedcachemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-timeline-pluginstorage-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-web-proxy-2.10.1.jar";

    static String exclusionFile = "./exclusions/Exclusions.txt";
    static String sourcePath;
//    public static List<String> sourcePaths = new ArrayList<String>(){
//        {
//            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-annotations/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth-examples/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-kms/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-minikdc/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-nfs/src/main/java/");
//        }
//    };

//    public static List<String> sourcePaths = new ArrayList<String>(){
//        {
//            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-native-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-nfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-rbf/src/main/java/");
//        }
//    };

//    public static List<String> sourcePaths = new ArrayList<String>(){
//        {
//            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-app/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs-plugins/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-jobclient/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-shuffle/src/main/java/");
//        }
//    };

//    public static List<String> sourcePaths = new ArrayList<String>(){
//        {
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-api/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-unmanaged-am-launcher/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-registry/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-applicationhistoryservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-resourcemanager/src/main/java/");
//            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-router/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-sharedcachemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timeline-pluginstorage/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-web-proxy/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-ui/src/main/java/");
//        }
//    };

    public static List<String> sourcePaths = new ArrayList<String>(){
        {
            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-annotations/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth-examples/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-kms/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-minikdc/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-nfs/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-native-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-nfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-rbf/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-app/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs-plugins/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-jobclient/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-shuffle/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-api/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-unmanaged-am-launcher/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-registry/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-applicationhistoryservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-resourcemanager/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-router/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-sharedcachemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timeline-pluginstorage/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-web-proxy/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-ui/src/main/java/");
        }
    };

    public static void main(String[] args) throws IOException {
        ConfigurationSlicerDing helper = new ConfigurationSlicerDing(path);
        helper.setExclusiveFile(exclusionFile);
        //构建cg
        helper.buildAnalysis();

        //获得所有的confEntity
        List<ConfigEntity> configEntityList = ConfigUtils.getConfigEntityList();

        //获得每个confEntity相关的seed(Statement)，并对statement进行切片
        int hasLOG = 0;
        int numberOfLog = 0;
        for(ConfigEntity entity : configEntityList){
            System.out.println("--------------confName:"+entity.getConfName()+"--------target Method:"+entity.getConfVariable()+"-----------------");
            Statement seed = helper.extractStatementFromConfigEntity(entity);
            if(seed == null){
                System.out.println("seed is null!");
            }
            else{
                System.out.println("seed:" + seed);
                //找到seed后进行切片
                Collection<Statement> sliceResults = helper.performForwardSlicingBySdg(seed);
                Collection<IRStatement> irSliceResults = helper.convert(sliceResults);
                int findFile = 0;
                //处理seed的所有切片结果
                for(IRStatement irsliceResult : irSliceResults){
                    //除去exclusion之外的java包中的类
                    if(!irsliceResult.getStatement().getNode().getMethod().toString().contains("Lorg/apache/hadoop")){
                        continue;
                    }
                    //System.out.println("irsliceResult:"+irsliceResult);
                    IMethod method = irsliceResult.getStatement().getNode().getMethod();
                    //System.out.println("Method:"+method);
                    //获得method中的类
                    String methodClass = getClassString(method.toString());
                    //System.out.println("methodClass:"+methodClass);
                    //打开对应的源码文件
                    for(int j = 0; j < sourcePaths.size(); j++){
                        String s = sourcePaths.get(j) + methodClass + ".java";
                        File f = new File(s);
                        if(f.exists()){
                            sourcePath = sourcePaths.get(j);
                            findFile = 1;
                            break;
                        }
                    }
                    if(findFile == 0){
                        continue;
                    }
                    String filePath = sourcePath+methodClass+".java";
                    File sourceFile = new File(filePath);
                    //通过切片的行号获得源代码
                    int lineNumber = irsliceResult.getLineNumber();
                    if(lineNumber > 0) {
                        String source = ReadFileUtils.readByLineNumber(sourceFile, lineNumber);
                        if (source != null) {
                            //删除空行
                            String sourceafterTrip = source.strip();
                            if(sourceafterTrip.contains("LOG") || sourceafterTrip.contains("Exception")) {
                                System.out.println("source:" + sourceafterTrip);
                                hasLOG = 1;
                            }
                        } else {
                            System.out.println("source of " + lineNumber + " is null!");
                        }
                    }
                    findFile = 0;
                }
            }
            System.out.println();
            if(hasLOG == 1){
                numberOfLog++;
            }
        }
        System.out.println("number of containing LOG："+numberOfLog);
    }

    //分解method, 获得其中的类
    public static String getClassString(String method){
        String[] m = method.split(",");
        String mm = m[1];
        String methodClass = "";
        if(mm.contains("$")){
            methodClass = mm.substring(2, mm.indexOf("$"));
            return methodClass;
        }else {
            methodClass = mm.substring(2, mm.length());
            return methodClass;
        }
    }

}
