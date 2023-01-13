package diagSlicer;

import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.LinkedList;

public class Test {
    public static void main(String[] args){
        String path = "./tests/hadoop-hdfs-2.10.1.jar";
        String className = "org.apache.hadoop.hdfs.server.datanode.DNConf";
        String confName = "socketTimeout";
        String exclusionFile = "./exclusions/ExclusionsDemo1.txt";
        Slicer.DataDependenceOptions dataOption = Slicer.DataDependenceOptions.NO_BASE_NO_HEAP_NO_EXCEPTIONS;
        Slicer.ControlDependenceOptions controlOption = Slicer.ControlDependenceOptions.NONE;
        //ClassName
        String[] paths = path.split(Sep.pathSep);
        System.out.println("Test:paths:"+paths.length);
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        try {
            URL[] urls = new URL[files.length];
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURL();
            }
            ClassLoader cl = new URLClassLoader(urls);
            System.out.println("Test:cl:"+cl.toString());

            Class<?> cls = cl.loadClass(className);
            System.out.println("Test:cls:"+cls.toString());


            //confName
            try {
                //getFields()：获得某个类的所有的公共（public）的字段，包括父类中的字段。
                //getDeclaredFields()：获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
                //getConstructors()和getDeclaredConstructors()、getMethods()和getDeclaredMethods()，这两者分别表示获取某个类的方法、构造函数
                //获得该类的声明字段
                Field[] fields = cls.getDeclaredFields();
                //Field[] fields = cls.getFields();
                for (Field f : fields) {
                    System.out.println("Test:all-f.name:"+f.getName());
                    if (f.getName().equals(confName)) {
                        System.out.println("------");
                        System.out.println("Test:f:"+f.toString());
                        System.out.println("isStatic:"+Modifier.isStatic(f.getModifiers()));
                        System.out.println("Test:f.type:"+f.getType().getName());
                        System.out.println("------");
                    }
                    else{
                        throw new Error("Can not find confName: " + confName + " in " + cls.toString());
                    }
                }
            } catch (Throwable e) {
                throw new Error(e);
            }

        } catch (MalformedURLException | ClassNotFoundException ignored) {
        }

        //test IClass
        //build scope
//        try {
//            AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(path, (new FileProvider()).getFile(exclusionFile));
//            System.out.println("scope is build!");
//            //build cha
//            try {
//                ClassHierarchy cha = ClassHierarchyFactory.make(scope);
//                System.out.println("cha is build!");
//                for (IClass c : cha) {
//                    String fullName = WALAUtils.getJavaFullClassName(c);
//                    System.out.println("WALAUtils:IClass in cha:"+fullName);
//                    if (fullName.equals(className)) {
//                        System.out.println("success find className in cha!");
//                    }
//                }
//                Iterable<Entrypoint> entryPoints = new AllApplicationEntrypoints(scope, cha);
//                System.out.println("entryPoints is build!");
//                AnalysisOptions options = CallGraphTestUtil.makeAnalysisOptions(scope, entryPoints);
//                CallGraphBuilder<InstanceKey> builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha, scope);
//                System.out.println("Building call graph...");
//                System.out.println("Number of entry points: " + Utils.countIterable(entryPoints));
//                CallGraph cg = builder.makeCallGraph(options,null);
//                //找node中的seed
//                for(CGNode node:cg) {
//                    if (node.getIR() != null) {
//                        Iterator<SSAInstruction> ssaIt = node.getIR().iterateAllInstructions();
//                        while (ssaIt.hasNext()) {
//                            SSAInstruction inst = ssaIt.next();
//                            System.out.println("Test:ConfigurationSlicer:extractConfStatement(ConfEntity entity):" + inst.toString());
//                            if (inst instanceof SSAPutInstruction) {
//                                SSAPutInstruction putInst = (SSAPutInstruction) inst;
//                                System.out.println("Test:ConfigurationSlicer:extractConfStatement(ConfEntity entity):putInst=" + putInst.toString());
//                                System.out.println("Test:ConfigurationSlicer:extractConfStatement(ConfEntity entity):putInst.isStatic=" + putInst.isStatic());
//                                Statement s = new NormalStatement(node, WALAUtils.getInstructionIndex(node, inst));
//                                System.out.println("-------Test:Statement s:" + s.toString());
//                                CISlicer slicer = new CISlicer(cg, builder.getPointerAnalysis(), dataOption, controlOption);
//                                Collection<Statement> slices = slicer.computeForwardThinSlice(s);
//                                Collection<diagSlicer.IRStatement> irs = convert(slices);
//                                //切片结果
//                                System.out.println("-----------------------------slice result-----------------------------------");
//                                for(diagSlicer.IRStatement statement : irs){
//                                    if(statement.getLineNumber()!=-1) {
//                                        System.out.println("statement.getStatement():" + statement.getStatement());
//                                        System.out.println("LineNumber:" + statement.getLineNumber());
//                                    }
//                                }
//
//                            }
//                        }
//                    }
//                }
//            } catch (ClassHierarchyException e) {
//                System.err.println("error in buildClassHierarchy");
//                throw new Error(e);
//            } catch (CallGraphBuilderCancelException e) {
//                e.printStackTrace();
//            }
//        } catch (IOException e) {
//            System.err.println("error in buildScope");
//            throw new Error(e);
//        }

    }

    static Collection<IRStatement> convert(Collection<Statement> stmts) {
        Collection<IRStatement> irs = new LinkedList<IRStatement>();

        for (Statement s : stmts) {
            if (s instanceof StatementWithInstructionIndex) {
                if (s.getNode().getMethod() instanceof ShrikeBTMethod) {
                    try {
                        IRStatement ir = new IRStatement((StatementWithInstructionIndex) s);
                        irs.add(ir);
                    } catch (Throwable e) {
                        continue;
                    }
                }

            }
        }
        return irs;
    }

}
