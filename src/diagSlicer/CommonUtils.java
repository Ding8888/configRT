package diagSlicer;

import com.ibm.wala.ipa.slicer.Slicer.ControlDependenceOptions;
import com.ibm.wala.ipa.slicer.Slicer.DataDependenceOptions;
import diagSlicer.ConfigurationSlicer.CG;
import entity.ConfEntity;
import utils.ConfUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class CommonUtils {
    public static Collection<ConfPropOutput> getConfPropOutputs(String path, String mainClass, List<ConfEntity> confList, boolean doPruning) {
        return getConfPropOutputs(path, mainClass, confList, "Java60RegressionExclusions.txt", doPruning);
        //  return getConfPropOutputs(path,mainClass,confList,"ChordExclusions.txt",doPruning);
    }

    public static Collection<ConfPropOutput> getConfPropOutputs(String path, String mainClass, List<ConfEntity> confList, String exclusionFile, boolean doPruning) {
        return getConfPropOutputs(path, mainClass, confList, exclusionFile, CG.ZeroCFA, doPruning);
    }

    ///// 数据流 控制流无 ??????????????????
    public static Collection<ConfPropOutput> getConfPropOutputs(String path, String mainClass, List<ConfEntity> confList, String exclusionFile, CG type, boolean doPruning) {
        return getConfPropOutputs(path, mainClass, confList, exclusionFile, type, doPruning, DataDependenceOptions.NO_BASE_NO_HEAP_NO_EXCEPTIONS, ControlDependenceOptions.NONE);
    }

    public static Collection<ConfPropOutput> getConfPropOutputs(String path, String mainClass, List<ConfEntity> confList, String exclusionFile, CG type, boolean doPruning,
                                                                DataDependenceOptions dataDep, ControlDependenceOptions controlDep) {
        ConfigurationSlicer helper = new ConfigurationSlicer(path, mainClass);
        helper.setCGType(type);
        helper.setExclusiveFile(exclusionFile);
        helper.setDataDependenceOptions(dataDep);
        helper.setControlDependenceOptions(controlDep);
        helper.setContextSensitive(false);
        //构建cg
        helper.buildAnalysis();

        //初始化entities
        //ConfEntityRepository repo = new ConfEntityRepository(confList);
        //repo.initializeTypesInConfEntities(path);

        Collection<ConfPropOutput> outputs = new LinkedList<ConfPropOutput>();
        for (ConfEntity entity : confList) {
            System.out.println("CommonUtil:");
            System.out.println("entity:"+entity);
            //important!!
            ConfPropOutput output = helper.outputSliceConfOption(entity);

            //针对配置项的所有的切片结果output
            outputs.add(output);
            System.out.println("statement in slice: " + output.statements.size());

            //去掉依赖包的
            Set<IRStatement> filtered = ConfPropOutput.excludeIgnorableStatements(output.statements);
            System.out.println("statements after filtering: " + filtered.size());

            //???
            Set<IRStatement> sameStmts = diagSlicer.ConfUtils.removeSameStmtsInDiffContexts(filtered);
            System.out.println("filtered statements: " + sameStmts.size());
//            for (diagSlicer.IRStatement s : sameStmts) {
//                System.out.println("       statement: " + s);
//            }

            //???
            Set<IRStatement> branchStmts = ConfPropOutput.extractBranchStatements(sameStmts);
            System.out.println("branching statements: " + branchStmts.size());
            dumpStatements(branchStmts);

        }
        ConfUtils.checkTrue(confList.size() == outputs.size());
        if (doPruning) {
            //fixed!
        }
        for (ConfPropOutput output : outputs) {
            output.setConfigurationSlicer(helper);
        }
        return outputs;

    }

    public static void dumpStatements(Collection<IRStatement> stmts) {
        for (IRStatement stmt : stmts) {
            //Log.logln("     >> " + stmt.toString());
        }
    }
}
