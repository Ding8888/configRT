package entity;

/**
 * 官方xml配置文档解析所用的类*/

public class XmlEntity {
    private final String confName;
    private final String value;
    private final String description;
    private final String component;

    public XmlEntity(String confName, String value, String description, String component) {
        this.confName = confName;
        this.value = value;
        this.description = description;
        this.component = component;
    }

    public String getConfName(){
        return confName;
    }

    public String getValue(){
        return value;
    }

    public String getDescription(){
        return description;
    }

    public String getComponent(){
        return component;
    }
}
