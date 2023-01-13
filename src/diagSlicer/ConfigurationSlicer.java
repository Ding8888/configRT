package diagSlicer;

import com.ibm.wala.classLoader.Language;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.*;
import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import com.ibm.wala.ipa.slicer.thin.CISlicer;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import entity.ConfEntity;
import utils.ConfUtils;
import utils.WALAUtils;

import java.io.IOException;
import java.util.*;

public class ConfigurationSlicer {
    public enum CG {
        RTA, ZeroCFA, ZeroContainerCFA, VanillaZeroOneCFA, ZeroOneCFA, ZeroOneContainerCFA,
        OneCFA,
        //TwoCFA, CFA, TempZeroCFA
    }

    public final String classPath;
    public final String mainClass;

    private CG type = CG.ZeroCFA;
    private String exclusionFile = CallGraphTestUtil.REGRESSION_EXCLUSIONS;
    private DataDependenceOptions dataOption = DataDependenceOptions.NO_BASE_NO_HEAP_NO_EXCEPTIONS;//数据依赖
    private ControlDependenceOptions controlOption = ControlDependenceOptions.NONE;//控制依赖
    private boolean contextSensitive = false;
    private AnalysisScope scope = null;
    private ClassHierarchy cha = null;
    private Iterable<Entrypoint> entryPoints = null;
    private AnalysisOptions options = null;
    private CallGraphBuilder builder = null;
    private CallGraph cg = null;
    private SDG<?> sdg = null;
    private String byPassFile = null;
    private boolean backward = false;
    private CISlicer slicer = null;
    private boolean extractAllGets = false;
    private boolean addSliceSeedFromGet = false;
    private boolean addStatementDistance = false;
    private String targetPackageName = null;
    private boolean useReturnSeed = false;

    public ConfigurationSlicer(String classPath, String mainClass) {
        this.classPath = classPath;
        this.mainClass = mainClass;
    }

    public void setCGType(CG type) {
        this.type = type;
    }

    public void setExclusiveFile(String fileName) {
        this.exclusionFile = fileName;
    }

    public void setDataDependenceOptions(DataDependenceOptions op) {
        this.dataOption = op;
    }

    public void setControlDependenceOptions(ControlDependenceOptions op) {
        this.controlOption = op;
    }

    public void setContextSensitive(boolean b) {
        this.contextSensitive = b;
    }

    public void buildAnalysis() {
        try {
            System.out.println("Using exclusion file: " + this.exclusionFile);
            System.out.println("CG type: " + this.type);
            this.buildScope();
            this.buildClassHierarchy();
            if (this.entryPoints == null) {
                //this.entryPoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(this.scope, this.cha, this.mainClass);
                this.entryPoints = new AllApplicationEntrypoints(scope, cha);
                System.out.println("entryPoints is build!");

            } else {
                System.err.println("Note, use customized entry points: " + this.entryPoints);
                System.err.println("Total num: " + ConfUtils.countIterable(this.entryPoints));
            }
            ////what is options?
            this.options = CallGraphTestUtil.makeAnalysisOptions(this.scope, this.entryPoints);
            this.builder = chooseCallGraphBuilder(options, new AnalysisCacheImpl(), cha, scope);
            System.out.println("Number of entry points: " + ConfUtils.countIterable(this.entryPoints));
            //build cg
            System.out.println("Building call graph...");
            this.cg = this.builder.makeCallGraph(this.options, null);
            //build sdg
            PointerAnalysis pa = this.builder.getPointerAnalysis();
            ModRef<InstanceKey> modRef = ModRef.make();
            this.sdg = new SDG<>(cg, pa, modRef, Slicer.DataDependenceOptions.REFLECTION, Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES, null);
            System.out.println("sdg is build!");
            //print cg
            System.out.println(CallGraphStats.getStats(this.cg));

            for(CGNode node:cg){
                //node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息。
                if(node.getMethod() instanceof ShrikeBTMethod) {
                    //一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                    ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                    //System.out.println("node.getMethod():"+node.getMethod().toString());
                    //System.out.println("method.getName():" + method.getName());
                }
            }

        } catch (Throwable e) {
            System.err.println("error in buildAnalysis");
            throw new Error(e);
        }
    }

    public void buildScope() {
        try {
            this.scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(this.classPath, (new FileProvider()).getFile(this.exclusionFile));
            System.out.println("scope is build!");
        } catch (IOException e) {
            System.err.println("error in buildScope");
            throw new Error(e);
        }
    }

    public void buildClassHierarchy() {
        if (this.scope == null) {
            this.buildScope();
        }
        try {
            this.cha = ClassHierarchyFactory.make(this.scope);
            System.out.println("cha is build!");
        } catch (ClassHierarchyException e) {
            System.err.println("error in buildClassHierarchy");
            throw new Error(e);
        }
    }

    private CallGraphBuilder chooseCallGraphBuilder(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) {
        CallGraphBuilder builder = null;
        if (this.type == CG.OneCFA) {
            System.out.println("Using 1-CFA call graph");
            //builder = WALAUtils.makeOneCFABuilder(options, cache, cha, scope);
        }
        if (this.type == CG.ZeroCFA) {
            System.out.println("Using 0-CFA call graph");
            builder = Util.makeZeroCFABuilder(Language.JAVA,options, cache, cha, scope);
        } else if (this.type == CG.ZeroOneCFA) {
            System.out.println("Using 0-1-CFA call graph");
            builder = Util.makeVanillaZeroOneCFABuilder(Language.JAVA,options, cache, cha, scope);
        } else if (this.type == CG.ZeroContainerCFA) {
            System.out.println("Using 0-container-CFA call graph");
            builder = Util.makeVanillaZeroOneContainerCFABuilder(options, cache, cha, scope);
        } else if (this.type == CG.RTA) {
            System.out.println("Using RTA call graph");
            builder = Util.makeRTABuilder(options, cache, cha, scope);
        }
        assert builder != null;
        if (this.byPassFile != null) {
            System.err.println("Use bypass file: " + this.byPassFile);
            Util.addBypassLogic(options, scope, ConfUtils.class.getClassLoader(), this.byPassFile, cha);
        }
        return builder;
    }

    public ConfPropOutput outputSliceConfOption(ConfEntity entity) {
        long startT = System.currentTimeMillis();
        Collection<Statement> stmts = sliceConfOption(entity);
        System.out.println("ConfPropOutput:get Collection<Statement> stmts!");
        //default false
        if (this.addSliceSeedFromGet) {
            Collection<Statement> moreStmts = sliceConfOptionFromGetter(entity);
            stmts.addAll(moreStmts);
        }
        System.out.println("Time cost: " + (System.currentTimeMillis() - startT) / 1000 + " s");
        Collection<IRStatement> irs = convert(stmts);
        ConfPropOutput output = new ConfPropOutput(entity, irs, this.targetPackageName);
        //default false
        if (this.addStatementDistance) {
            System.out.println("get seed statement of entity!!");
            //(important!)
            Statement seed = this.extractConfStatement(entity);
            for (IRStatement target : irs) {
                int distance = this.computeDistanceInThinSlicing(seed, target.getStatement());
                output.setSlicingDistance(target, distance);
            }
        }
        return output;
    }

    public int computeDistanceInThinSlicing(Statement seed, Statement target) {
        ConfUtils.checkNotNull(slicer);
        int distance = slicer.computeBFSDistanceInForwardSlice(seed, target);
        return distance;
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

    public Collection<Statement> sliceConfOptionFromGetter(ConfEntity entity) {
        this.CheckCG();
        Statement s = this.extractConfStatementFromGetter(entity);
        if (s == null) {
            return Collections.EMPTY_LIST;
        }
        System.out.println("Add additional seed: " + s + " to: " + entity.getConfName());
        return this.performSlicing(s);
    }

    public Statement extractConfStatementFromGetter(ConfEntity entity) {
        String confClassName = entity.getClassName();
        String confName = entity.getConfName();
        boolean isStatic = entity.getIsStatic();
        Set<String> skippedMethods = new HashSet<String>();
        skippedMethods.add("equals");
        skippedMethods.add("hashCode");
        skippedMethods.add("toString");
        skippedMethods.add("<init>");
        skippedMethods.add("<clinit>");

        for (CGNode node : cg) {
            String fullClassName = WALAUtils.getJavaFullClassName(node.getMethod().getDeclaringClass());
            if (fullClassName.equals(confClassName)) {
                String methodName = node.getMethod().getName().toString();
                if (skippedMethods.contains(methodName)) {
                    continue;
                }
                if (methodName.startsWith("set")) { //a heuristic
                    continue;
                }
                for (SSAInstruction ssa : WALAUtils.getAllIRs(node)) {
                    if (ssa instanceof SSAGetInstruction) {
                        SSAGetInstruction ssaGet = (SSAGetInstruction) ssa;
                        if (ssaGet.isStatic() == isStatic && ssaGet.toString().indexOf(confName) != -1) {
                            int index = WALAUtils.getInstructionIndex(node, ssa);
                            Statement s = new NormalStatement(node, index);
                            return s;
                        }
                    }
                }
            }
        }
        return null;
    }

    //获得entity相关的Statement
    public Collection<Statement> sliceConfOption(ConfEntity entity) {
        CheckCG();
        Statement s = this.extractConfStatement(entity);
        //s为空
        if (s == null) {
            IClass clz = WALAUtils.lookupClass(this.getClassHierarchy(), entity.getClassName());
            //clz不为空
            if (clz != null) {
                System.out.println("ConfigurationSlicer:sliceConfOption:clz is not null!");
                String signature = entity.getClassName() + "." +
                        (entity.getAssignMethod() == null
                                ? (entity.getIsStatic() ? "<clinit>" : "<init>")
                                : entity.getAssignMethod());
                Collection<CGNode> nodes = WALAUtils.lookupCGNode(this.getCallGraph(), signature);
                for (CGNode node : nodes) {
                    WALAUtils.printAllIRs(node);
                }
                if (nodes.isEmpty()) {
                    System.err.println(" no such nodes in CG: " + signature);
                    return new LinkedList<Statement>();
                }
            }
            //clz为空
//            Utils.checkTrue(clz == null, "Class: " + entity.getClassName()
//                    + ",  here is the entity: " + entity);
        }
        //s不为空
        ConfUtils.checkNotNull(s, "statement is null? " + entity);
        Collection<Statement> slice = this.performSlicing(s);
        if(slice == null){
            System.err.println("note:performSlicing:Collection<Statement> slice is null!");
        }
        if (this.extractAllGets) {
            Collection<Statement> stmtsFromGetters = this.sliceConfOptionFromEveryGetter(entity);
            slice.addAll(stmtsFromGetters);
        }
        return slice;

    }

    public Collection<Statement> extractAllGetStatements(ConfEntity entity) {
        return diagSlicer.ConfUtils.getextractAllGetStatements(entity, this.getCallGraph());
    }

    Collection<Statement> sliceConfOptionFromEveryGetter(ConfEntity entity) {
        this.CheckCG();
        Collection<Statement> allStmts = new LinkedHashSet<Statement>();

        Collection<Statement> allGetStmts = this.extractAllGetStatements(entity);
        System.err.println("   all get stmts: " + allGetStmts.size());
        for (Statement getStmt : allGetStmts) {
            Collection<Statement> slicedStmts = this.performSlicing(getStmt);
            allStmts.addAll(slicedStmts);
        }

        return allStmts;
    }

    public Collection<Statement> performSlicing(Statement s) {
        this.CheckCG();
        ConfUtils.checkNotNull(s);
        try {
            //default false
//            if (this.backward) {
//                System.err.println("!!! Now backward slicing -- experimental!, context: " + this.contextSensitive);
//                if (this.contextSensitive) {
//                    return this.computeContextSensitiveSlice(s, true, this.dataOption, this.controlOption);
//                } else {
//                    return this.computeContextInsensitiveThinSlice(s, true, this.dataOption, this.controlOption);
//                }
//            }
            //default false
            if (this.contextSensitive) {
                return computeContextSensitiveForwardSlice(s);
            } else {
                return computeContextInsensitiveForwardThinSlice(s);
            }
            //return computeSliceBySdg(this.sdg,s);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<Statement> computeContextSensitiveForwardSlice(Statement seed) throws IllegalArgumentException, CancelException {
        return computeContextSensitiveSlice(seed, false, this.dataOption, this.controlOption);
    }

    public Collection<Statement> computeContextInsensitiveForwardThinSlice(Statement seed) throws IllegalArgumentException, CancelException {
        return computeContextInsensitiveThinSlice(seed, false, this.dataOption, this.controlOption);
    }

    public Collection<Statement> computeContextInsensitiveThinSlice(Statement seed, boolean goBackward,
                                                                    DataDependenceOptions dOptions, ControlDependenceOptions cOptions) throws IllegalArgumentException, CancelException {
        this.CheckCG();
        System.err.println("Seed statement in context-insensitive slicing: " + seed);
        System.err.println("Data dependence option: " + dOptions);
        System.err.println("Control dependence option: " + cOptions);
        if (slicer == null) {
            slicer = new CISlicer(cg, builder.getPointerAnalysis(), dOptions, cOptions);
        }
        Collection<Statement> slice = null;
        if (goBackward) {
            slice = slicer.computeBackwardThinSlice(seed);
        } else {
            seed = getReturnStatementForCall(seed);
            slice = slicer.computeForwardThinSlice(seed);
        }
        return slice;
    }

    public Collection<Statement> computeContextSensitiveSlice(Statement seed, boolean goBackward,
                                                              DataDependenceOptions dOptions, ControlDependenceOptions cOptions) throws IllegalArgumentException, CancelException {
        this.CheckCG();
        System.err.println("Seed statement in context-sensitive slicing: " + seed);
        System.err.println("Data dependence option: " + dOptions);
        System.err.println("Control dependence option: " + cOptions);

        Collection<Statement> slice = null;
        if (goBackward) {
            slice = Slicer.computeBackwardSlice(seed, cg, builder.getPointerAnalysis(), dOptions, cOptions);
        } else {
            seed = getReturnStatementForCall(seed);
            slice = Slicer.computeForwardSlice(seed, cg, builder.getPointerAnalysis(), dOptions, cOptions);
        }
        return slice;
    }

    //通过sdg和seed来performSlice
    public Collection<Statement> computeSliceBySdg(SDG<?> sdg, Statement seed) throws IllegalArgumentException, CancelException {
        this.CheckCG();
        System.err.println("Seed statement in context-sensitive slicing: " + seed);
        Collection<Statement> slice = null;
        slice = Slicer.computeForwardSlice(sdg,seed);
        return slice;
    }

    public static Statement getReturnStatementForCall(Statement s) {
        if (s.getKind() == Statement.Kind.NORMAL) {
            NormalStatement n = (NormalStatement) s;
            SSAInstruction st = n.getInstruction();
            if (st instanceof SSAInvokeInstruction) {
                SSAAbstractInvokeInstruction call = (SSAAbstractInvokeInstruction) st;
                if (call.getCallSite().getDeclaredTarget().getReturnType().equals(TypeReference.Void)) {
                    throw new IllegalArgumentException("this driver computes forward slices from the return value of calls.\n" + ""
                            + "Method " + call.getCallSite().getDeclaredTarget().getSignature() + " returns void.");
                }
                System.err.println("Use return value as slicing seed: " + s);
                return new NormalReturnCaller(s.getNode(), n.getInstructionIndex());

            } else {
                return s;
            }
        } else {
            return s;
        }
    }

    public ClassHierarchy getClassHierarchy() {
        return this.cha;
    }

    public CallGraph getCallGraph() {
        return this.cg;
    }

    private void CheckCG() {
        if (this.cg == null) {
            throw new RuntimeException("Please call buildAnalysis() first.");
        }
    }

    //找到配置的seed对应的statement
    public Statement extractConfStatement(ConfEntity entity) {
        String className = entity.getClassName();
        String confName = entity.getConfName();
        String assignMethod = entity.getAssignMethod(); //FIXME we may need more specific method signature   //null
        boolean isStatic = entity.getIsStatic();
        String targetMethod = assignMethod != null
                ? assignMethod
                : (isStatic ? "<clinit>" : "<init>");
        System.out.println("target method name: " + targetMethod);
        System.out.println("------------node of "+className+"---------------------------");
        //打印看看node图中所有和本class相关的method
        for (CGNode node : cg) {
            String fullClassName = WALAUtils.getJavaFullClassName(node.getMethod().getDeclaringClass());
            if(node.getMethod().getClass().toString().contains(className)){
                String fullMethodName = WALAUtils.getFullMethodName(node.getMethod());
                System.out.println("CGNode:fullMethodName in "+fullClassName+"="+fullMethodName);
            }
        }
        for(CGNode node : cg){
            SSAInstruction[] instructions = node.getIR().getInstructions();
            for(SSAInstruction inst : instructions){
                if(inst != null) {
                    System.out.println("ConfigurationSlicer:extractConfStatement(ConfEntity entity):inst=" + inst.toString());
                    if (inst.toString().contains(confName)) {
                        Statement s = new NormalStatement(node, WALAUtils.getInstructionIndex(node, inst));
                        System.out.println("!!success find seed!");
                        return s;
                    }
                }
            }
        }
//        for (CGNode node : cg) {
//            String fullMethodName = WALAUtils.getFullMethodName(node.getMethod());
//            String fullClassName = WALAUtils.getJavaFullClassName(node.getMethod().getDeclaringClass());
//            if(fullClassName.equals(className)){
//                System.out.println("CGNode:fullMethodName in "+fullClassName+"="+fullMethodName);
//            }
//            if (fullMethodName.equals(className + "." + targetMethod)) {
////                //default false
////                if (this.useReturnSeed) {
////                    List<SSAInstruction> irList = WALAUtils.getAllIRs(node);
////                    Collections.reverse(irList);
////                    for (SSAInstruction ssa : irList) {
////                        if (ssa instanceof SSAGetInstruction) {
////                            SSAGetInstruction ssaGet = (SSAGetInstruction) ssa;
////                            if (ssaGet.toString().indexOf(confName) != -1) {
////                                Statement s = new NormalStatement(node, WALAUtils.getInstructionIndex(node, ssaGet));
////                                return s;
////                            }
////                        }
////                    }
////                }
//                Iterator<SSAInstruction> ssaIt = node.getIR().iterateAllInstructions();
//                while (ssaIt.hasNext()) {
//                    SSAInstruction inst = ssaIt.next();
//                    System.out.println("ConfigurationSlicer:extractConfStatement(ConfEntity entity):inst="+inst.toString());
//
//                    if (inst instanceof SSAPutInstruction) {
//                        SSAPutInstruction putInst = (SSAPutInstruction) inst;
//                        System.out.println("ConfigurationSlicer:extractConfStatement(ConfEntity entity):putInst="+putInst.toString());
//                        //System.out.println("ConfigurationSlicer:extractConfStatement(ConfEntity entity):putInst.isStatic="+inst.isStatic());
//
//                        //if (putInst.isStatic() == isStatic) {
//                            if (inst.toString().contains(confName)) {
//                                Statement s = new NormalStatement(node, WALAUtils.getInstructionIndex(node, inst));
//                                System.out.println("!!success find seed!");
//                                return s;
//                            }
//                        //}
//                    }
//                }
//            }
//        }
        return null;
    }
}
