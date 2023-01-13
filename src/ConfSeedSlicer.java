import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import conf.InitialSeed;
import diagSlicer.IRStatement;
import entity.InitialSeedEntity;
import entity.ORPEntity;
import utils.ORPUtils;
import utils.ReadFileUtils;
import utils.WALAUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ConfSeedSlicer {

    public final static String jarPath =
            "./tests/hadoop-common-jar/hadoop-common-2.10.1.jar;./tests/hadoop-common-jar/hadoop-nfs-2.10.1.jar;"+
            "./tests/hadoop-hdfs-jar/hadoop-hdfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-client-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-native-client-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-nfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-rbf-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-nfs-2.10.1.jar;"+
            "./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-app-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-common-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-core-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-hs-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-hs-plugins-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-jobclient-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-shuffle-2.10.1.jar;"+
            "./tests/hadoop-yarn-jar/hadoop-yarn-api-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-applications-distributedshell-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-applications-unmanaged-am-launcher-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-client-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-common-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-registry-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-applicationhistoryservice-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-common-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-nodemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-resourcemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-router-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-sharedcachemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-timeline-pluginstorage-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-web-proxy-2.10.1.jar";

    static String exclusionFile = "./exclusions/Exclusions.txt";

    static String sourcePath;

    public static List<String> sourcePaths = new ArrayList<String>(){
        {
            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-annotations/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth-examples/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-kms/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-minikdc/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-nfs/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-native-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-nfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-rbf/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-app/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs-plugins/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-jobclient/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-shuffle/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-api/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-unmanaged-am-launcher/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-registry/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-applicationhistoryservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-resourcemanager/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-router/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-sharedcachemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timeline-pluginstorage/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-web-proxy/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-ui/src/main/java/");
        }
    };

    public static Map<String, List<Statement>> getConfLogMap() throws ClassHierarchyException, IOException {
        Map<String, List<Statement>> confSeedMap = new HashMap<>();

        //切片准备
        ConfigurationSlicerDing helper = new ConfigurationSlicerDing(jarPath);
        helper.setExclusiveFile(exclusionFile);
        helper.buildAnalysis();

        //获得initial seed和orp entities
        List<InitialSeedEntity> initialSeedEntities = InitialSeed.getInitialSeedList();
        Collection<ORPEntity> orpEntities = ORPUtils.getOrpEntityList();

        int hasLogCount = 0;
        int confnum = 1;
        //循环node
        for(CGNode node:helper.getCallGraph()){
            if((node.getMethod() instanceof ShrikeBTMethod)) {
                List<SSAInstruction> ssaInstructionList = WALAUtils.getAllIRs(node);
                if (ssaInstructionList != null) {
                    for (SSAInstruction ssaInstruction : ssaInstructionList) {
                        if (ssaInstruction != null) {
                            if(ssaInstruction instanceof SSAInvokeInstruction) {
                                SSAInvokeInstruction call = (SSAInvokeInstruction) ssaInstruction;
                                //如果instruction调用了get-方法
                                for(InitialSeedEntity initialSeedEntity : initialSeedEntities){
                                    if (call.toString().contains(initialSeedEntity.getClassNameofInst()) && call.getCallSite().getDeclaredTarget().getName().toString().equals(initialSeedEntity.getMethodName())) {
                                        try {
                                            for (ORPEntity entity : orpEntities) {
                                                //如果包含ORP中的类和方法
                                                if (node.toString().contains(entity.getClassNameofinst()) && call.getCallSite().getDeclaredTarget().getName().toString().equals(entity.getMethodName())) {
                                                    int index = WALAUtils.getInstructionIndex(node, ssaInstruction);
                                                    try {
                                                        Statement statement = new NormalStatement(node, index);
                                                        IRStatement ir = new IRStatement((StatementWithInstructionIndex) statement);
                                                        //行号对应的上
                                                        if (ir.getLineNumber() == entity.getLineNumber()) {
                                                            Set<String> keys = confSeedMap.keySet();
                                                            int has = 0;
                                                            for(String k : keys){
                                                                if(k.equals(entity.getConfName())){
                                                                    has = 1;
                                                                    break;
                                                                }
                                                            }
                                                            if(has == 0) {
                                                                List<Statement> newStatementList = new ArrayList<>();
                                                                newStatementList.add(statement);
                                                                confSeedMap.put(entity.getConfName(), newStatementList);
                                                            }
                                                            if(has == 1) {
                                                                confSeedMap.get(entity.getConfName()).add(statement);
                                                            }
                                                            System.out.println("--------target confName:"+confnum+":"+entity.getConfName()+"----------------");
                                                            confnum++;
                                                            //System.out.println(statement);
                                                            Collection<Statement> sliceResults = helper.performBackwardSlicingBySdg(statement);//上下文敏感全切片
                                                            //处理切片结果
                                                            Collection<IRStatement> irSliceResults = helper.convert(sliceResults);
                                                            if(parseResult(irSliceResults) == 1){
                                                                hasLogCount++;
                                                            }
                                                            break;
                                                        }
                                                    } catch (ArrayIndexOutOfBoundsException ignored) {
                                                        System.err.println("ArrayIndexOutOfBoundsException");
                                                    }
                                                }
                                            }
                                        } catch (Throwable ignored) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("hasLogCount:"+hasLogCount);
        return confSeedMap;
    }

    public static void printConfSeed() throws ClassHierarchyException, IOException {
        Map<String, List<Statement>> confSeedMap = new HashMap<>();

        //切片准备
        ConfigurationSlicerDing helper = new ConfigurationSlicerDing(jarPath);
        helper.setExclusiveFile(exclusionFile);
        helper.buildAnalysis();

        //获得initial seed和orp entities
        List<InitialSeedEntity> initialSeedEntities = InitialSeed.getInitialSeedList();
        Collection<ORPEntity> orpEntities = ORPUtils.getOrpEntityList();

        //循环node
        for(CGNode node:helper.getCallGraph()){
            if((node.getMethod() instanceof ShrikeBTMethod)) {
                List<SSAInstruction> ssaInstructionList = WALAUtils.getAllIRs(node);
                if (ssaInstructionList != null) {
                    for (SSAInstruction ssaInstruction : ssaInstructionList) {
                        if (ssaInstruction != null) {
                            if(ssaInstruction instanceof SSAInvokeInstruction) {
                                SSAInvokeInstruction call = (SSAInvokeInstruction) ssaInstruction;
                                //如果instruction调用了get-方法
                                for(InitialSeedEntity initialSeedEntity : initialSeedEntities){
                                    if (call.toString().contains(initialSeedEntity.getClassNameofInst()) && call.getCallSite().getDeclaredTarget().getName().toString().equals(initialSeedEntity.getMethodName())) {
                                        try {
                                            for (ORPEntity entity : orpEntities) {
                                                //如果包含ORP中的类和方法
                                                if (node.toString().contains(entity.getClassNameofinst()) && call.getCallSite().getDeclaredTarget().getName().toString().equals(entity.getMethodName())) {
                                                    int index = WALAUtils.getInstructionIndex(node, ssaInstruction);
                                                    try {
                                                        Statement statement = new NormalStatement(node, index);
                                                        IRStatement ir = new IRStatement((StatementWithInstructionIndex) statement);
                                                        //行号对应的上
                                                        if (ir.getLineNumber() == entity.getLineNumber()) {
                                                            System.out.println("confName:"+entity.getConfName());
                                                            System.out.println("statement:"+statement);
                                                            break;
                                                        }
                                                    } catch (ArrayIndexOutOfBoundsException ignored) {
                                                        System.err.println("ArrayIndexOutOfBoundsException");
                                                    }
                                                }
                                            }
                                        } catch (Throwable ignored) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static int parseResult(Collection<IRStatement> irSliceResults) throws IOException {
        int hasLog = 0;
        int findFile = 0;
        //处理seed的所有切片结果
        String tempSource = "";
        for(IRStatement irsliceResult : irSliceResults){
            //除去exclusion之外的java包中的类
            if(!irsliceResult.getStatement().getNode().getMethod().toString().contains("Lorg/apache/hadoop")){
                continue;
            }
            IMethod method = irsliceResult.getStatement().getNode().getMethod();
            //获得method中的类
            String methodClass = getClassString(method.toString());
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
            if(findFile == 0){//没有找到对应的源码
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
                    if(sourceafterTrip.contains("LOG.warn") || sourceafterTrip.contains("LOG.error") || sourceafterTrip.contains("Exception")) {
                        //除去重复的
                        if(sourceafterTrip.equals(tempSource)){
                            continue;
                        }
                        System.out.println("source:" + sourceafterTrip);
                        tempSource = sourceafterTrip;
                        hasLog = 1;
                    }
                } else {
                    System.out.println("source of " + lineNumber + " is null!");
                }
            }
            findFile = 0;
        }
        return hasLog;
    }

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

    public static void main(String[] args) throws ClassHierarchyException, IOException {
//        long startTime = fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
//        Map<String, List<Statement>> map = getConfLogMap();
//        System.out.println("Map size:"+map.keySet().size());
//        long stopTime = fromDateStringToLong(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date()));
//        long timeSpan = stopTime - startTime;
//        System.out.println("timeSpan="+timeSpan);
        printConfSeed();
    }

    public static long fromDateStringToLong(String inVal) {
        Date date = null;
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        try {
            date = inputFormat.parse(inVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date.getTime();
    }
}


