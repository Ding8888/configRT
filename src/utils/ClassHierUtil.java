package utils;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 解析类层次结构。。*/

public class ClassHierUtil {

    public static Collection<IClass> getallClass(ClassHierarchy cha){
        IClassLoader[] classLoaders = cha.getLoaders();
        Collection<IClass> allclasses = null;
        for(IClassLoader classLoader:classLoaders){
            Iterator<IClass> iterateAllClasses= classLoader.iterateAllClasses();
            while(iterateAllClasses.hasNext()){
                IClass iClass = iterateAllClasses.next();
                allclasses.add(iClass);
            }
        }
        return allclasses;
    }

    public static Collection<IClass> getConfClass(ClassHierarchy cha){
        IClassLoader[] classLoaders = cha.getLoaders();
        Collection<IClass> confclasses = null;
        for(IClassLoader classLoader:classLoaders){
            //System.out.println(classLoader);
            Iterator<IClass> iterateAllClasses= classLoader.iterateAllClasses();
            while(iterateAllClasses.hasNext()){
                IClass iClass = iterateAllClasses.next();
                if(iClass.toString().contains("org/apache/hadoop/conf/Configuration>")){
                    System.out.println("Find root class:"+iClass);
                    Collection<IClass> subclasses = cha.getImmediateSubclasses(iClass);
                    subclasses.add(iClass);
                    confclasses = subclasses;
                }
            }
        }
        return confclasses;
    }

    public static Collection<IClass> getClassStructuredByConfClass(ClassHierarchy cha, Collection<IClass> confClasses){
        Collection<IClass> classStructuredByConfClass = new ArrayList<>();
        List<String> confClassNames = new ArrayList<>();
        for(IClass confClass:confClasses){
            String s = confClass.toString();
            String confClassName = s.substring(s.indexOf("L"),s.indexOf(">"));
            confClassNames.add(confClassName);
        }
        int i = 0;
        IClassLoader[] classLoaders = cha.getLoaders();
        IClass tempIClass = null;
        for(IClassLoader classLoader:classLoaders){
            Iterator<IClass> iterateAllClasses= classLoader.iterateAllClasses();
            while(iterateAllClasses.hasNext()){
                IClass iClass = iterateAllClasses.next();
                Collection<? extends IMethod> iMethods = iClass.getAllMethods();
                for (IMethod iMethod : iMethods) {
                    for (String confClassName : confClassNames) {
                        if (iMethod.toString().contains("<init>") && iMethod.toString().contains(confClassName)) {
                            if(iClass != tempIClass) {
                                classStructuredByConfClass.add(iClass);
                                tempIClass = iClass;
                                i++;
                            }
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("number of ClassStructuredByConfClass:"+i);
        return classStructuredByConfClass;
    }
}
