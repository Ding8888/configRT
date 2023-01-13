package entity;

import java.io.Serializable;

/**
 * Liu 定义的用于后向切片的类*/
public class SeedEntity implements Serializable {
    private String srcMethodName;
    private String dstMethodName;
    private String dstClassName;

    public String getSrcMethodName() {
        return srcMethodName;
    }

    public void setSrcMethodName(String srcMethodName) {
        this.srcMethodName = srcMethodName;
    }

    public String getDstMethodName() {
        return dstMethodName;
    }

    public void setDstMethodName(String dstMethodName) {
        this.dstMethodName = dstMethodName;
    }

    public String getDstClassName() {
        return dstClassName;
    }

    public void setDstClassName(String dstClassName) {
        this.dstClassName = dstClassName;
    }

    @Override
    public String toString() {
        return "entity.SeedEntity{" +
                "srcMethodName='" + srcMethodName + '\'' +
                ", dstMethodName='" + dstMethodName + '\'' +
                ", dstClassName='" + dstClassName + '\'' +
                '}';
    }
}
