package diagSlicer;

import entity.ConfEntity;
import utils.ConfUtils;
import utils.WALAUtils;

import java.io.Serializable;
import java.util.*;

public class ConfPropOutput implements Serializable {

    //序列化
    private static final long serialVersionUID = -7129055336865285652L;
    public final ConfEntity conf;
    public final Set<IRStatement> statements;
    public List<AnalysisCount> AnalysisCount;
    public List<OutputCount> outputCounts;

    //public int AllCount=0;
    private ConfigurationSlicer slicer = null;
    public final Map<IRStatement, Integer> stmtDistances = new LinkedHashMap<IRStatement, Integer>();


    public ConfPropOutput(ConfEntity conf, Collection<IRStatement> stmts) {
        this(conf, stmts, null);
    }

    public ConfPropOutput(ConfEntity conf, Collection<IRStatement> stmts, String targetPackage) {
        ConfUtils.checkNotNull(conf);

        this.conf = conf;
        this.statements = new LinkedHashSet<IRStatement>();
        this.AnalysisCount = new LinkedList<AnalysisCount>();
        this.outputCounts = new LinkedList<OutputCount>();

        if (targetPackage != null) {
            //把符合 a.b.c.d.MethodName 的加入statement
            for (IRStatement s : stmts) {
                String fullMethod = WALAUtils.getFullMethodName(s.getStatement().getNode().getMethod());
                if (fullMethod.startsWith(targetPackage)) {
                    this.statements.add(s);
                }
            }
        } else {
            this.statements.addAll(stmts);
        }
    }

    public ConfEntity getConfEntity() {
        return this.conf;
    }

    public static Set<IRStatement> excludeIgnorableStatements(Collection<IRStatement> set) {
        Set<IRStatement> filterSet = new LinkedHashSet<IRStatement>();
        for (IRStatement s : set) {
            if (!s.shouldIgnore()) {
                filterSet.add(s);
            }
        }
        return filterSet;
    }

    public static Set<IRStatement> extractBranchStatements(Collection<IRStatement> set) {
        Set<IRStatement> branchSet = new LinkedHashSet<IRStatement>();
        for (IRStatement s : set) {
            if (s.isBranch()) {
                branchSet.add(s);
            }
        }
        return branchSet;
    }

    public void setConfigurationSlicer(ConfigurationSlicer slicer) {
        ConfUtils.checkNotNull(slicer);
        this.slicer = slicer;
    }

    public void setSlicingDistance(IRStatement irs, int distance) {
        ConfUtils.checkTrue(statements.contains(irs));
        ConfUtils.checkTrue(distance > 0);
        this.stmtDistances.put(irs, distance);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Seed:");
        sb.append(Sep.lineSep);
        sb.append(conf.toString());
        sb.append(Sep.lineSep);
        sb.append("Slicing results, stmt no: " + this.statements.size());
        sb.append(Sep.lineSep);
        for (IRStatement s : statements) {
            sb.append(s.toString());
            sb.append(Sep.lineSep);
        }

        return sb.toString();
    }

    public String headString() {
        StringBuilder sb = new StringBuilder();
        for (IRStatement s : statements) {
            sb.append(s.IRHeadString());
            sb.append(Sep.lineSep);
        }
        return sb.toString();
    }

    public List<Integer> getLineNumber() {
        List<Integer> LineNumberList = new LinkedList<Integer>();
        for (IRStatement s : statements)
            LineNumberList.add(s.getLineNumber());
        return LineNumberList;
    }


    public void Analysis(List<ConfEntity> confList) {
        int size = confList.size();
        Iterator<IRStatement> it = statements.iterator();
        //初始化 analysis A B C D count 0
        for (int i = 0; i < size; i++) {
            AnalysisCount temp = new AnalysisCount(confList.get(i));
            this.AnalysisCount.add(temp);
        }
        while (it.hasNext()) {
            IRStatement s = it.next();
            Iterator<AnalysisCount> ACit = s.AnalysisCount.iterator();
            while (ACit.hasNext()) {
                AnalysisCount ac = ACit.next();
                ////比较两个conf一致 就把 ac.count 作为参数set进去
                for (int j = 0; j < size; j++) {
                    if (ac.conf.equals(this.AnalysisCount.get(j).conf)) {
                        this.AnalysisCount.get(j).setCount(ac.count);
                    }
                }
            }
        }
    }

    public void OutputAnalysis(List<ConfEntity> confList) {
        int size = confList.size();
        Iterator<IRStatement> it = statements.iterator();
        //初始化 analysis A B C D count 0
        for (int i = 0; i < size; i++) {
            OutputCount temp = new OutputCount(confList.get(i));
            this.outputCounts.add(temp);
        }
        while (it.hasNext()) {
            IRStatement s = it.next();
            Iterator<AnalysisCount> ACit = s.AnalysisCount.iterator();

            while (ACit.hasNext()) {
                AnalysisCount ac = ACit.next();
                //比较两个conf一致 就把 ac.count 作为参数set进去
                for (int j = 0; j < size; j++) {
                    if (ac.conf.equals(this.outputCounts.get(j).conf)) {
                        this.outputCounts.get(j).setCount(ac.count);
                    }
                }
            }
        }
    }


    /*
    根据 count 降序排列
    */
    public void rank() {
        Collections.sort(this.AnalysisCount);
        System.out.println(this.AnalysisCount);
    }

    public void RatioRank() {
        Iterator<OutputCount> it = this.outputCounts.iterator();
        while (it.hasNext()) {
            OutputCount o = it.next();
            double temp = Double.valueOf(o.count) / Double.valueOf(this.statements.size());
            o.setRatio(temp);
        }
        Collections.sort(this.outputCounts);
        System.out.println(this.outputCounts);
    }

}
