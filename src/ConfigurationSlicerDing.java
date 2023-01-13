import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.modref.ModRef;
import com.ibm.wala.ipa.slicer.*;
import com.ibm.wala.ipa.slicer.thin.CISlicer;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.io.FileProvider;
import diagSlicer.*;
import entity.ConfigEntity;
import entity.ORPEntity;
import utils.ConfUtils;
import utils.WALAUtils;

import java.io.IOException;
import java.util.*;

public class ConfigurationSlicerDing {

    public final String classPath;
    private String exclusionFile = CallGraphTestUtil.REGRESSION_EXCLUSIONS;
    private ConfigurationSlicer.CG type = ConfigurationSlicer.CG.ZeroCFA;
    private AnalysisScope scope = null;
    private ClassHierarchy cha = null;
    private Iterable<Entrypoint> entryPoints = null;
    private AnalysisOptions options = null;
    private PointerAnalysis<InstanceKey> pa = null;
    private Slicer.DataDependenceOptions dataOption = Slicer.DataDependenceOptions.NO_BASE_NO_HEAP_NO_EXCEPTIONS;//数据依赖
    private Slicer.ControlDependenceOptions controlOption = Slicer.ControlDependenceOptions.FULL;//控制依赖
    private CallGraphBuilder builder = null;
    private CallGraph cg = null;
    private SDG<?> sdg = null;
    private CISlicer slicer = null;
    private boolean isThinSlice = true;

    public ConfigurationSlicerDing(String classPath) {
        this.classPath = classPath;
    }

    public void setExclusiveFile(String fileName) {
        this.exclusionFile = fileName;
    }

    public ClassHierarchy getClassHierarchy() {
        return this.cha;
    }

    public CallGraph getCallGraph() {
        return this.cg;
    }

    public SDG<?> getSdg(){return this.sdg;}

    /**--------------------------------------------构建CG图------------------------------------------*/

    public void buildAnalysis() {
        try {
            System.out.println("Using exclusion file: " + this.exclusionFile);
            System.out.println("CG type: " + this.type);
            //构建scope
            long beginScopeTime = System.currentTimeMillis();
            this.buildScope();
            long endScopeTime = System.currentTimeMillis();
            System.out.println("build Scope Time:" + (endScopeTime - beginScopeTime));
            //make cha(ClassHierarchy)
            long beginchaTime = System.currentTimeMillis();
            this.buildClassHierarchy();
            long endchaTime = System.currentTimeMillis();
            System.out.println("buildClassHierarchy Time:" + (endchaTime - beginchaTime));
            //make entrypoint
            long beginMakeEntrypointTime = System.currentTimeMillis();
            if (this.entryPoints == null) {
                this.entryPoints = new AllApplicationEntrypoints(scope, cha);
                System.out.println("entryPoints is build!");
            } else {
                System.err.println("Note, use customized entry points: " + this.entryPoints);
                System.err.println("Total num: " + ConfUtils.countIterable(this.entryPoints));
            }
            System.out.println("Number of entryPoints: " + ConfUtils.countIterable(this.entryPoints));
            //build options
            this.options = CallGraphTestUtil.makeAnalysisOptions(this.scope, this.entryPoints);
            long endMakeEntrypointTime = System.currentTimeMillis();
            System.out.println("make Entrypoint Time:" + (endMakeEntrypointTime - beginMakeEntrypointTime));
            //make builder
            this.builder = chooseCallGraphBuilder(options, new AnalysisCacheImpl(), cha, scope);
            //build cg
            long beginCgTime = System.currentTimeMillis();
            System.out.println("Building call graph...");
            this.cg = this.builder.makeCallGraph(this.options, null);
            long endCgTime = System.currentTimeMillis();
            System.out.println("buildCg Time:" + (endCgTime - beginCgTime));
            long beginSDGTime = System.currentTimeMillis();
            ModRef<InstanceKey> modRef = ModRef.make();
            sdg = new SDG<>(cg, pa, modRef, Slicer.DataDependenceOptions.REFLECTION, Slicer.ControlDependenceOptions.NO_EXCEPTIONAL_EDGES, null);
            System.out.println("sdg is build!");
            long endSDGTime = System.currentTimeMillis();
            System.out.println("buildSDG Time:" + (endSDGTime - beginSDGTime));
            //print cg state
            System.out.println(CallGraphStats.getStats(this.cg));
        } catch (Throwable e) {
            System.err.println("error in buildAnalysis");
            throw new Error(e);
        }
    }
    /*
    * build scope*/
    public void buildScope() {
        try {
            this.scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(this.classPath, (new FileProvider()).getFile(this.exclusionFile));
            System.out.println("scope is build!");
        } catch (IOException e) {
            System.err.println("error in buildScope");
            throw new Error(e);
        }
    }

    /*
    * build cha*/
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

    /*
    * choose builder type*/
    private CallGraphBuilder chooseCallGraphBuilder(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) {
        CallGraphBuilder builder = null;
        if (this.type == ConfigurationSlicer.CG.OneCFA) {
            System.out.println("Using 1-CFA call graph");
            //builder = WALAUtils.makeOneCFABuilder(options, cache, cha, scope);
        }
        if (this.type == ConfigurationSlicer.CG.ZeroCFA) {
            System.out.println("Using 0-CFA call graph");
            builder = Util.makeZeroCFABuilder(Language.JAVA,options, cache, cha, scope);
        } else if (this.type == ConfigurationSlicer.CG.ZeroOneCFA) {
            System.out.println("Using 0-1-CFA call graph");
            builder = Util.makeVanillaZeroOneCFABuilder(Language.JAVA,options, cache, cha, scope);
        } else if (this.type == ConfigurationSlicer.CG.ZeroContainerCFA) {
            System.out.println("Using 0-container-CFA call graph");
            builder = Util.makeVanillaZeroOneContainerCFABuilder(options, cache, cha, scope);
        } else if (this.type == ConfigurationSlicer.CG.RTA) {
            System.out.println("Using RTA call graph");
            builder = Util.makeRTABuilder(options, cache, cha, scope);
        }
        return builder;
    }

    /**------------------------------------通过entity获得statement-------------------------------------------------*/

    public Statement extractStatementORP(ORPEntity entity){
        this.CheckCG();
        String className = entity.getClassName();
        String methodName = entity.getMethodName();
        String confName = entity.getConfName();
        int lineNumber = entity.getLineNumber();
        System.out.println("target className:"+className);
        System.out.println("target methodName:"+methodName);
        for(CGNode node:cg){
            if(node.toString().contains(className)){
                Statement statement = findCallTo(node, methodName);
                if(statement == null){
                    continue;
                }else{
                    return statement;
                }
            }
        }
        return null;
    }

    //获得调用methodName的方法
    public static Statement findCallTo(CGNode n, String methodName) {
        //The IR consists of a control-flow graph of basic blocks, along with a set of instructions.(IR由基本块的控制流图和一组指令组成).
        IR ir = n.getIR();
        //ir.iterateAllInstructions() return all the instructions, in an undefined order. Useful for flow-insensitive analysis.
        //ir.iterateAllInstructions() 以未定义的顺序返回所有指令，适用于流量不敏感分析
        for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext(); ) {
            SSAInstruction s = it.next();
            if (s instanceof SSAAbstractInvokeInstruction) {
                SSAAbstractInvokeInstruction call = (SSAAbstractInvokeInstruction) s;
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {
                    IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
                    Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
                    return new NormalStatement(n, indices.intIterator().next());
                }
            }
        }
        //如果没有调用该方法的statement则返回空
        return null;
    }

    public Statement extractStatementFromConfigEntity(ConfigEntity entity){
        this.CheckCG();
        String className = entity.getClassName();
        String classNameofInst = entity.getClassNameofInst();
        String confVariable = entity.getConfVariable();
        String confName = entity.getConfName();
        System.out.println("target className:"+className);
        System.out.println("target classNameofInst:"+classNameofInst);
        System.out.println("target confVariable:"+confVariable);
        //通过inst找到entity所指的statement
        for(CGNode node : cg){
            String fullMethodName = WALAUtils.getFullMethodName(node.getMethod());
            if(fullMethodName.contains(className)){
                IR ir = node.getIR();
                if(ir != null) {
                    SSAInstruction[] instructions = ir.getInstructions();
                    for (SSAInstruction inst : instructions) {
                        if (inst != null) {
                            if (inst.toString().contains(confVariable)) {
                                Statement s = new NormalStatement(node, WALAUtils.getInstructionIndex(node, inst));
                                System.out.println("!!success find seed!");
                                return s;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public Statement extractConfStatementFromOrpEntity(ORPEntity entity) {
        this.CheckCG();
        String className = entity.getClassName();
        String methodName = entity.getMethodName();
        //通过inst找到entity所指的statement
        for(CGNode node : cg){
            IR ir = node.getIR();
            if(ir != null) {
                SSAInstruction[] instructions = ir.getInstructions();
                for (SSAInstruction inst : instructions) {
                    if (inst != null) {
                        if (inst.toString().contains(className) && inst.toString().contains(methodName)) {
                            Statement s = new NormalStatement(node, WALAUtils.getInstructionIndex(node, inst));
                            System.out.println("!!success find seed!");
                            System.out.println("seed : "+s);
                            return s;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**-------------------------------找到seed后进行切片---------------------------------*/

    //使用sdg进行前向切片
    public Collection<Statement> performForwardSlicingBySdg(Statement seed) {
        this.CheckCG();
        //ConfUtils.checkNotNull(seed);
        try {
            if(sdg != null) {
                //Selected as needed!
                return computeForwardSliceBySdg(sdg, seed);
            }else{
                System.err.println("sdg is null!");
                return null;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    //使用sdg进行前向切片
    public Collection<Statement> performBackwardSlicingBySdg(Statement seed) {
        this.CheckCG();
        //ConfUtils.checkNotNull(seed);
        try {
            if(sdg != null) {
                //Selected as needed!
                return computeBackwardSliceBySdg(sdg, seed);
            }else{
                System.err.println("sdg is null!");
                return null;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    //通过sdg和seed来前向切片performSlice
    public Collection<Statement> computeForwardSliceBySdg(SDG<?> sdg, Statement seed) throws IllegalArgumentException, CancelException {
        Collection<Statement> slice = null;
        slice = Slicer.computeForwardSlice(sdg,seed);
        return slice;
    }
    public Collection<Statement> computeBackwardSliceBySdg(SDG<?> sdg, Statement seed) throws IllegalArgumentException, CancelException {
        Collection<Statement> slice = null;
        slice = Slicer.computeBackwardSlice(sdg, seed);
        return slice;
    }

    //使用cg切片
    public Collection<Statement> performSlicingByCg(Statement seed) {
        this.CheckCG();
        try {
            //Selected as needed!
            return computeSliceByCg(seed, false, this.dataOption, this.controlOption);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /*使用cg切片*/
    public Collection<Statement> computeSliceByCg(Statement seed, boolean goBackward, Slicer.DataDependenceOptions dOptions, Slicer.ControlDependenceOptions cOptions) throws IllegalArgumentException, CancelException {
        this.CheckCG();
        System.out.println("Seed statement in context-sensitive slicing: " + seed);
        Collection<Statement> slice = null;
        slice = Slicer.computeForwardSlice(seed, cg, builder.getPointerAnalysis(), dOptions, cOptions);
        return slice;
    }

    //进行薄切片
    public Collection<Statement> performThinSlicing(Statement seed) {
        this.CheckCG();
        Collection<Statement> slice = null;
        try {
            if(sdg != null) {
                //Selected as needed!
                slice = slicer.computeForwardThinSlice(seed);
            }else{
                System.err.println("sdg is null!");
                return null;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return slice;
    }

    //上下文敏感切片（全）
    public Collection<Statement> computeContextSensitiveForwardSlice(Statement seed) throws IllegalArgumentException, CancelException {
        return computeContextSensitiveSlice(seed, false, this.dataOption, this.controlOption);
    }

    /*上下文敏感全切片(confDiagnoser用)*/
    public Collection<Statement> computeContextSensitiveSlice(Statement seed, boolean goBackward, Slicer.DataDependenceOptions dOptions, Slicer.ControlDependenceOptions cOptions) throws IllegalArgumentException, CancelException {
        this.CheckCG();
        System.out.println("Seed statement in context-sensitive slicing: " + seed);

        Collection<Statement> slice = null;
        if (goBackward) {
            slice = Slicer.computeBackwardSlice(seed, cg, builder.getPointerAnalysis(), dOptions, cOptions);
        } else {
            //seed = getReturnStatementForCall(seed);
            slice = Slicer.computeForwardSlice(seed, cg, builder.getPointerAnalysis(), dOptions, cOptions);
        }
        return slice;
    }

    //上下文不敏感切片(薄)
    public Collection<Statement> computeContextInsensitiveForwardThinSlice(Statement seed) throws IllegalArgumentException, CancelException {
        return computeContextInsensitiveThinSlice(seed, false, this.dataOption, this.controlOption);
    }

    /*
    * 上下文不敏感薄切片*/
    public Collection<Statement> computeContextInsensitiveThinSlice(Statement seed, boolean goBackward,
                                                                    Slicer.DataDependenceOptions dOptions, Slicer.ControlDependenceOptions cOptions) throws IllegalArgumentException, CancelException {
        this.CheckCG();
        System.out.println("Seed statement in context-insensitive slicing: " + seed);
        if (slicer == null) {
            slicer = new CISlicer(cg, builder.getPointerAnalysis(), dOptions, cOptions);
        }
        Collection<Statement> slice = null;
        if (goBackward) {
            slice = slicer.computeBackwardThinSlice(seed);
        } else {
            //seed = getReturnStatementForCall(seed);
            slice = slicer.computeForwardThinSlice(seed);
        }
        return slice;
    }

    //获得某个statement的返回值
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
                System.out.println("Use returni value as slicng seed: " + s);
                return new NormalReturnCaller(s.getNode(), n.getInstructionIndex());

            } else {
                return s;
            }
        } else {
            return s;
        }
    }


    /**-----------------------------------内部方法--------------------------------------------*/
    //检查cg是否已经建立
    private void CheckCG() {
        if (this.cg == null) {
            throw new RuntimeException("Please call buildAnalysis() first.");
        }
    }

    //statement转IRStatement
    public Collection<IRStatement> convert(Collection<Statement> stmts) {
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
