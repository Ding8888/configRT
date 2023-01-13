package entity;

import java.sql.Statement;

/**
 * Ding 定义的用于切片的类*/

public class ConfigEntity {
    private final String className;
    private final String classNameofInst;
    private String confVariable = "";
    private final String confName;
    private Statement confseed = null;

    public ConfigEntity(String className, String classNameofInst, String confVariable, String confName){
        this.className = className;
        this.classNameofInst = classNameofInst;
        this.confVariable = confVariable;
        this.confName = confName;
    }

    public ConfigEntity(String className, String classNameofInst, String confName, Statement confseed){
        this.className = className;
        this.classNameofInst = classNameofInst;
        this.confVariable = confVariable;
        this.confName = confName;
        this.confseed = confseed;
    }

    public String getClassName(){
        return this.className;
    }

    public String getClassNameofInst(){return this.classNameofInst;}

    public String getConfVariable(){
        return this.confVariable;
    }

    public String getConfName(){
        return this.confName;
    }

    public Statement getConfseed(){return this.confseed;}

}
