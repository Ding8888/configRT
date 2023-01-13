package utils;

import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.examples.drivers.PDFSlice;
import com.ibm.wala.examples.properties.WalaExamplesProperties;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.pruned.ApplicationLoaderPolicy;
import com.ibm.wala.ipa.slicer.NormalStatement;
import com.ibm.wala.ipa.slicer.SDG;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.slicer.Statement;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphIntegrity;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.strings.Atom;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.PDFViewUtil;

import java.io.File;
import java.util.*;

/**
 * Liu */
public class StmtUtils {

    public static void printSlice(CallGraphBuilder<InstanceKey> cgb, CallGraph cg, Collection<Statement> slice) {
        SDG<InstanceKey> sdg = new SDG(cg, cgb.getPointerAnalysis(), Slicer.DataDependenceOptions.NO_BASE_PTRS, Slicer.ControlDependenceOptions.NONE);
        Graph<Statement> g = PDFSlice.pruneSDG(sdg, slice);
        try {
            GraphIntegrity.check(g);
        } catch (GraphIntegrity.UnsoundGraphException var3) {
            var3.printStackTrace();
            Assertions.UNREACHABLE();
        }
        Assertions.productionAssertion(g.getNumberOfNodes() == slice.size(), "panic " + g.getNumberOfNodes() + " " + slice.size());
        Properties p = null;
        try {
            p = WalaExamplesProperties.loadProperties();
            p.putAll(WalaProperties.loadProperties());
        } catch (WalaException e) {
            e.printStackTrace();
        }
        String psFile = p.getProperty("output_dir").substring(46) + File.separatorChar + "slice.pdf";
        String dotExe = p.getProperty("dot_exe");
        //System.err.println("dir:"+p.getProperty("output_dir").substring(46));
        try {
            DotUtil.dotify(g, PDFSlice.makeNodeDecorator(), "temp.dt", psFile, dotExe);
            String gvExe = p.getProperty("pdfview_exe");
            PDFViewUtil.launchPDFView(psFile, gvExe);
        } catch (WalaException e) {
            e.printStackTrace();
        }
    }

    public static CGNode findMainMethod(CallGraph cg) {
        Descriptor d = Descriptor.findOrCreateUTF8("([Ljava/lang/String;)V");
        //Atom name = Atom.findOrCreateUnicodeAtom("main");
        /**
         * user defined
         */
        Atom name = Atom.findOrCreateUnicodeAtom("main");
        for (Iterator<? extends CGNode> it = cg.getSuccNodes(cg.getFakeRootNode()); it.hasNext(); ) {
            CGNode n = it.next();
            if (n.getMethod().getName().equals(name) && n.getMethod().getDescriptor().equals(d)) {
                return n;
            }
        }
        Assertions.UNREACHABLE("failed to find main() method");
        return null;
    }

    public static Statement findCallTo(CGNode n, String methodName) {
        IR ir = n.getIR();
        System.out.println("IR:"+ir.toString());
        /**
         * The IR consists of a control-flow graph of basic blocks, along with a set of instructions.IR 由基本块的控制流图和一组指令组成。
         * ir.iterateAllInstructions() return all the instructions, in an undefined order. Useful for flow-insensitive analysis.
         * ir.iterateAllInstructions() 以未定义的顺序返回所有指令。 适用于流量不敏感分析。
         */
        for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext(); ) {
            SSAInstruction s = it.next();
            System.out.println("SSAInstruction::"+s.toString());
            if (s instanceof SSAAbstractInvokeInstruction) {
                SSAAbstractInvokeInstruction call = (SSAAbstractInvokeInstruction) s;
                System.out.println("call::"+call.toString());
                if (call.getCallSite().getDeclaredTarget().getName().toString().equals(methodName)) {

                    IntSet indices = ir.getCallInstructionIndices(call.getCallSite());
                    Assertions.productionAssertion(indices.size() == 1, "expected 1 but got " + indices.size());
                    return new NormalStatement(n, indices.intIterator().next());
                }
            }
            else{
                System.err.println("SSAInstruction::"+s.toString()+" is not a SSAAbstractInvokeInstruction");
            }
        }
        System.err.println("failed to find call to " + methodName + " in " + n.toString());
        return null;
    }

    //找到method
    public static CGNode findMethod(CallGraph cg, String Name, String methodCLass){
        if(Name.equals(null) && methodCLass.equals(null))
            return null;
        //构建 atom（wala中），用来比较CGNode的名字和类
        Atom name = Atom.findOrCreateUnicodeAtom(Name);
        //迭代cg图
        for (Iterator<? extends CGNode> it = cg.iterator(); it.hasNext();) {
            CGNode n = it.next();
            if (n.getMethod().getName().equals(name) &&
                    n.getMethod().getDeclaringClass().getName().toString().equals(methodCLass)){
                //System.err.println("Found node IR of "+Name+":"+n.getIR());
                return n;
            }
        }
        return null;
    }

    public static Statement extractStatement(String dstClassName, String srcMethodName, String dstMethodName, CallGraph cg) {
        for (CGNode node : cg) {
            TypeName typeName = node.getMethod().getDeclaringClass().getName();
            String methodName = node.getMethod().toString();
            String packageName = (typeName.getPackage() == null ? "" : typeName.getPackage() + ".");
            String clazzName = typeName.getClassName().toString();
            String fullMethodName = packageName.replace('/', '.') + clazzName;
            if (!methodName.contains(srcMethodName)) {
                continue;
            }
            System.err.println("methodName:" + methodName);
            System.err.println("srcMethodName:" + srcMethodName);

            if (fullMethodName.equals(dstClassName)) {
                SSAInstruction[] instructions = node.getIR().getInstructions();
                for (SSAInstruction ssa : instructions) {
                    if (ssa == null) continue;
                    if (ssa.toString().contains(dstMethodName)) {
                        int index = ssa.iIndex();
                        Statement s = new NormalStatement(node, index);
                        return s;
                    }
                }
                Iterator<SSAInstruction> ssaIt = node.getIR().iterateAllInstructions();
                while (ssaIt.hasNext()) {
                    SSAInstruction inst = ssaIt.next();
                    if (inst.toString().contains(dstMethodName)) {
                        int index = inst.iIndex();
                        Statement s = new NormalStatement(node, index);
                        return s;
                    }

                }
            }
        }
        return null;
    }

    //获取字节码指令所在行号
    public static void printSourcLine(Collection<Statement> collection ){
        Iterator<Statement> pruneIterator = collection.iterator();
        while (pruneIterator.hasNext()) {
            Statement sliceStatement = pruneIterator.next();
            CGNode node = sliceStatement.getNode();
            // node中包含了很多信息，包括类加载器、方法信息等，这里只筛选出需要的信息
            if(node.getMethod() instanceof ShrikeBTMethod) {
                // node.getMethod()返回一个比较泛化的IMethod实例，不能获取到我们想要的信息
                // 一般地，本项目中所有和业务逻辑相关的方法都是ShrikeBTMethod对象
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                // 使用Primordial类加载器加载的类都属于Java原生类，我们一般不关心。
                if("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    // 获取声明该方法的类的内部表示
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    // 获取方法签名
                    String signature = method.getSignature();
                    System.out.println(classInnerName + ":" + signature);
                    //获取字节码中指令所在行号
                    if (sliceStatement.getKind() == Statement.Kind.NORMAL) { // ignore special kinds of statements
                        int bcIndex, instructionIndex = ((NormalStatement) sliceStatement).getInstructionIndex();
                        try {
                            bcIndex = method.getBytecodeIndex(instructionIndex);
                            try {
                                int src_line_number = method.getLineNumber(bcIndex);
                                System.out.println ( "Source line number = " + src_line_number );
                                //out.write("Source line number = " + src_line_number);
                            } catch (Exception e) {
                                System.err.println("Bytecode index no good");
                                System.err.println(e.getMessage());
                            }
                        } catch (Exception e ) {
                            System.err.println("it's probably not a BT method (e.g. it's a fakeroot method)");
                            System.err.println(e.getMessage());
                        }
                    }
                }
            } else {
                System.out.println(String.format("'%s'不是一个ShrikeBTMethod：%s",
                        node.getMethod(), node.getMethod().getClass()));
            }
        }
    }

    public static void dumpSlice(Collection<Statement> slice) {
        System.err.println("Slicing statements: " + slice.size());
//        for (Statement s : slice) {
//            System.err.println(s);
//        }

//        /**
//         * pruned slice results by application (no primordial and Synthetic)
//         */
//        Statement sliceStatement = null;
//        ApplicationLoaderPolicy applicationLoaderPolicy = ApplicationLoaderPolicy.INSTANCE;
//
//        Iterator<Statement> pruneIterator = slice.iterator();
//        while (pruneIterator.hasNext()) {
//            sliceStatement = pruneIterator.next();
//            CGNode node = sliceStatement.getNode();
//            if (applicationLoaderPolicy.check(node)) {
//                System.err.println("Application:" + sliceStatement);
//            } else {
//                System.out.println(sliceStatement);
//            }
//        }
    }
}
