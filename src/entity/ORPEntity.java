package entity;

/**
 * 分析ORPLocator的类（解析orp结果文件得到）*/

public class ORPEntity {
    private final String className;
    private final String classNameofinst;
    private final String methodName;
    private final String confName;
    private final int lineNumber;

    public ORPEntity(String className, String classNameofinst, String methodName, String confName, int lineNumber) {
        this.className = className;
        this.methodName = methodName;
        this.confName = confName;
        this.lineNumber = lineNumber;
        this.classNameofinst = classNameofinst;
    }

    public String getClassName(){
        return className;
    }

    public String getMethodName(){
        return methodName;
    }

    public String getConfName(){
        return confName;
    }

    public int getLineNumber(){
        return lineNumber;
    }

    public String getClassNameofinst(){
        return classNameofinst;
    }

    public String toString(){
        String linenumber = String.valueOf(lineNumber);
        String s = className+":"+methodName+ ":<"+confName+"> " + "lineNumber:" + linenumber;
        return s;
    }
}
