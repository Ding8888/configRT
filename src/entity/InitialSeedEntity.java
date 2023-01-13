package entity;

public class InitialSeedEntity {
    private final String className;
    private final String classNameofInst;
    private final String fullmethodName;
    private final String methodName;

    public InitialSeedEntity(String className, String classNameofInst, String fullmethodName, String methodName){
        this.className = className;
        this.classNameofInst = classNameofInst;
        this.fullmethodName = fullmethodName;
        this.methodName = methodName;
    }

    public String getClassName(){
        return className;
    }

    public String getClassNameofInst(){ return classNameofInst;}

    public String getFullmethodName(){ return fullmethodName;}

    public String getMethodName(){
        return methodName;
    }

}
