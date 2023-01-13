package diagSlicer;

import entity.ConfEntity;
import utils.ConfUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class TestSliceEntity {
    static String path = "./tests/hadoop-common-2.10.1.jar";
    //static String mainClass = "Lweka/classifiers/trees/J48";

    public static void main(String[] args){
        //获取配置选项列表
        List<ConfEntity> confList = ConfUtils.getConfList();
        //获得切片集合scope,cha,build,cg..
        Collection<ConfPropOutput> slices = CommonUtils.getConfPropOutputs(path,null, confList, false);
        //切片结果
        for(ConfPropOutput slice:slices){
            Set<IRStatement> irStatements= slice.statements;
            System.out.println("-----------------------------slice result-----------------------------------");
            for(IRStatement statement : irStatements){
                if(statement.getLineNumber()!=-1) {
                    System.out.println("statement.getStatement():" + statement.getStatement());
                    System.out.println("LineNumber:" + statement.getLineNumber());
                }
            }
        }
    }

}
