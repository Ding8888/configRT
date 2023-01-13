package conf;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import entity.InitialSeedEntity;
import utils.ClassHierUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InitialSeed {
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

    public static List<InitialSeedEntity> getInitialSeedList() throws ClassHierarchyException, IOException {
        List<InitialSeedEntity> initialSeedList = new ArrayList<>();
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(jarPath,new FileProvider().getFile("./exclusions/Exclusions.txt"));
        ClassHierarchy cha = ClassHierarchyFactory.make(scope);
        Collection<IClass> confClasses = ClassHierUtil.getConfClass(cha);
        for(IClass confclass : confClasses){
            String className = iclassToClassName(confclass.toString());
            Collection<? extends IMethod> methods = confclass.getAllMethods();
            for(IMethod method : methods){
                if(method.toString().toString().contains("get")){
                    String classNameofInst = classNametoInst(className);
                    String fulllmethodName = className+"."+method.getName().toString();
                    String methodName = method.getName().toString();
                    InitialSeedEntity entity = new InitialSeedEntity(className, classNameofInst, fulllmethodName, methodName);
                    initialSeedList.add(entity);
                }
            }
        }
        return initialSeedList;
    }

    public static String classNametoInst(String className){
        StringBuilder classNameofinst = new StringBuilder();
        String[] classNames = className.split("\\.");
        for(int i = 0; i < classNames.length; i++){
            if(i != classNames.length-1){
                classNameofinst.append(classNames[i]).append("/");
            }else{
                classNameofinst.append(classNames[i]);
            }
        }
        return classNameofinst.toString();
    }

    public static String iclassToClassName(String iclass){
        StringBuilder className = new StringBuilder();
        String ic = iclass.substring(iclass.indexOf("L")+1,iclass.lastIndexOf(">"));
        String[] classNames = ic.split("/");
        for(int i = 0; i < classNames.length; i++){
            if(i != classNames.length-1){
                className.append(classNames[i]).append(".");
            }else{
                className.append(classNames[i]);
            }
        }
        return className.toString();
    }

    public static void main(String[] args) throws ClassHierarchyException, IOException {
        List<InitialSeedEntity> list  = getInitialSeedList();
        System.out.println("list.size:"+list.size());
        for(InitialSeedEntity initialSeedEntity : list){
            System.out.println(initialSeedEntity.getClassName()+";"+initialSeedEntity.getClassNameofInst()+";"+initialSeedEntity.getFullmethodName()+";"+initialSeedEntity.getMethodName());
        }
    }

}
