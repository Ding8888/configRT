package entity;

/**
 * confDiagnoser定义的切片类*/

public class ConfEntity {
    private final String className;
    private final String confName;
    private String assignMethod = null;
    private final boolean isStatic;
    private String type = null;

    public ConfEntity(String className, String confName, String assignMethod, boolean isStatic) {
        this.className = className;
        this.confName = confName;
        this.assignMethod = assignMethod;
        this.isStatic = isStatic;
    }
    public ConfEntity(String className, String confName, boolean isStatic){
        this.className = className;
        this.confName = confName;
        this.isStatic = isStatic;
    }

    public String getClassName(){
        return this.className;
    }
    public String getConfName(){
        return this.confName;
    }

    public String getAssignMethod(){
        return this.assignMethod;
    }
    public boolean getIsStatic(){
        return this.isStatic;
    }

    public String getType(){
        return this.type;
    }

    public void  setType(String type){
        this.type = type;
    }

    public String allConf(){
        return this.className + ":"+this.confName+"@"+this.assignMethod+","+this.type+",static:"+this.isStatic;
    }

    @Override
    public String toString() {
        return className + " : " + confName + " @ " + assignMethod
                + ", " + type + ", static: " + isStatic;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ConfEntity) {
            ConfEntity e = (ConfEntity)obj;
            return e.className.equals(this.className)
                    && e.confName.equals(this.confName)
			        //&& e.type.equals(this.type)
                    && e.isStatic == this.isStatic;
        }
        return false;
    }

}
