import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.*;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import conf.ConfKey;
import diagSlicer.IRStatement;
import entity.ORPEntity;
import utils.ClassHierUtil;
import utils.ORPUtils;
import utils.ReadFileUtils;
import utils.WALAUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class TestClassHierarchy {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date();

    public static final String DOT_EXE = "D:\\software\\Graphviz2.38\\bin\\dot";
    public final static String DOT_FILE = "E:\\data\\dotfiles\\temp.dt";
    public final static String PDF_FILE = "E:\\data\\dotfiles\\temp.pdf";
    public final static String jarPath = "./tests/hadoop-common-jar/hadoop-common-2.10.1.jar;./tests/hadoop-common-jar/hadoop-nfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-client-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-native-client-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-nfs-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-hdfs-rbf-2.10.1.jar;./tests/hadoop-hdfs-jar/hadoop-nfs-2.10.1.jar;" +
            "./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-app-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-common-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-core-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-hs-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-hs-plugins-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-jobclient-2.10.1.jar;./tests/hadoop-mapreduce-jar/hadoop-mapreduce-client-shuffle-2.10.1.jar;" +
            "./tests/hadoop-yarn-jar/hadoop-yarn-api-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-applications-distributedshell-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-applications-unmanaged-am-launcher-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-client-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-common-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-registry-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-applicationhistoryservice-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-common-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-nodemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-resourcemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-router-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-sharedcachemanager-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-timeline-pluginstorage-2.10.1.jar;./tests/hadoop-yarn-jar/hadoop-yarn-server-web-proxy-2.10.1.jar";

    public static List<String> sourcePaths = new ArrayList<String>(){
        {
            add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-annotations/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-auth-examples/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-kms/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-minikdc/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-common-project/hadoop-nfs/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-native-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-nfs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-hdfs-project/hadoop-hdfs-rbf/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-app/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-hs-plugins/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-jobclient/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-shuffle/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-api/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/hadoop-yarn-applications-unmanaged-am-launcher/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-client/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-registry/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-applicationhistoryservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-common/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-nodemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-resourcemanager/src/main/java/");
            add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-router/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-sharedcachemanager/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timeline-pluginstorage/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase-tests/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/hadoop-yarn-server-web-proxy/src/main/java/");add("./tests/hadoop-2.10.1-src/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-ui/src/main/java/");
        }
    };

    static String sourcePath;

    public static void main(String[] args) throws WalaException, IOException, CancelException, InvalidClassFileException {

        //ConfigurationSlicerLiu configurationSlicer = new ConfigurationSlicerLiu("./exclusions/ExclusionsDemo1.txt");
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarPath,new FileProvider().getFile("./exclusions/Exclusions.txt"));
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);

        //查看所有类层次结构的类和方法
        IClassLoader[] classLoaders = cha.getLoaders();
        for(IClassLoader classLoader:classLoaders) {
            Iterator<IClass> iterateAllClasses= classLoader.iterateAllClasses();
            while(iterateAllClasses.hasNext()) {
                IClass iClass = iterateAllClasses.next();
                if(iClass.toString().contains("org/apache/hadoop")) {
                    System.out.println("---------------------" + iClass.toString() + "-------------------");
                    Collection<? extends IMethod> allMethods = iClass.getAllMethods();
                    for (IMethod allMethod : allMethods) {
                        System.out.println(allMethod);
                    }
                }
            }
        }

        //get confClass
        Collection<IClass> confClasses = ClassHierUtil.getConfClass(cha);
        System.out.println("-------------------confClass---------------");
        for(IClass confClass:confClasses){
            System.out.println(confClass);
        }

        //get classStructuredByConfClass
        int countofAllVarible = 0;
        int countofthis = 0;
        int countofthisInConf = 0;
        Collection<IClass> classesStructuredByConfClass = ClassHierUtil.getClassStructuredByConfClass(cha, confClasses);
        Map<String,String> map = ConfKey.getConfKey();

        //遍历每个备选class的IField,IField中包含预定义变量
        for(IClass classStructuredByConfClass : classesStructuredByConfClass){
            System.out.println("--------------"+classStructuredByConfClass+"---------------");
            Collection<IField> fields = classStructuredByConfClass.getAllInstanceFields();

            //遍历每个变量，查看是否有赋值的地方
            for(IField field:fields){
                countofAllVarible++;
                //System.out.println(field);
                String fieldString  = field.toString();
                String[] fieldStrings = fieldString.split(",");
                String fieldClassName = fieldStrings[1].strip().substring(1);
                //System.out.println("filedClassName:"+fieldClassName);
                String fieldVariableName = fieldStrings[2].strip();
                //System.out.println("fieldVariableName"+fieldVariableName);
                //打开对应的源码文件
                int findFile = 0;
                for(int j = 0; j < sourcePaths.size(); j++){
                    String s = sourcePaths.get(j) + fieldClassName + ".java";
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
                String filePath = sourcePath+fieldClassName+".java";
                File sourceFile = new File(filePath);
                int totalLines = ReadFileUtils.getTotalLines(sourceFile);
                for(int i = 1 ; i <= totalLines ; i++){
                    String source = ReadFileUtils.readByLineNumber(sourceFile,i);
                    assert source != null;
                    //判断是否通过this.赋值
                    if(source.contains("this."+fieldVariableName+" =") && source.contains("get") && source.contains("(")){
                        System.out.println(source);
                        //判断是否get了获得的map中的配置项。
                        for(String k : map.keySet()){
                            if(source.contains(k)){
                                System.out.println("containing conf----"+map.get(k));
                                countofthisInConf++;
                                break;
                            }
                        }
                        countofthis++;
                    }
                }
            }
        }
        System.out.println("所有变量数："+countofAllVarible);
        System.out.println("通过this.赋值的所有变量总数:"+countofthis);
        System.out.println("通过this.赋值的变量中含有提取的配置项的变量总数："+countofthisInConf);

    }
}
