package utils;

import diagSlicer.Sep;
import entity.ConfEntity;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

/**
 * confDiagnoser获得用于切片的entity列表*/
public class ConfUtils {
    //获得配置选项列表
    public static List<ConfEntity> getConfList() {
        //test------------------
        ConfEntity entity1 = new ConfEntity("org.apache.hadoop.hdfs.server.namenode.NameNode", "stopRequested" , true);
        ConfEntity entity2 = new ConfEntity("org.apache.hadoop.hdfs.server.namenode.NameNode", "startRequested" , true);
        ConfEntity entity3 = new ConfEntity("org.apache.hadoop.hdfs.server.namenode.NameNode", "hellonamenode" , true);
        ConfEntity entity4 = new ConfEntity("org.apache.hadoop.TestConf", "getInts" , false);

        /*-----------------hadoop-common--------------*/
        ConfEntity entity11 = new ConfEntity("org.apache.hadoop.ipc.Server", "HTTP_GET_BYTES" , true);
        ConfEntity entity22 = new ConfEntity("org.apache.hadoop.ipc.Server", "INITIAL_RESP_BUF_SIZE" , true);
        ConfEntity entity33 = new ConfEntity("org.apache.hadoop.ipc.Server", "idleScanThreshold", false);
        ConfEntity entity44 = new ConfEntity("org.apache.hadoop.ipc.Server", "maxIdleTime", false);

        /*------------hadoop-hdfs-------*/
        ConfEntity entity111 = new ConfEntity("org.apache.hadoop.hdfs.tools.NNHAServiceTarget", "numCheckpointsToRetain", false);
        ConfEntity entity222 = new ConfEntity("org.apache.hadoop.hdfs.tools.NNHAServiceTarget", "numExtraEditsToRetain", false);
        ConfEntity entity333 = new ConfEntity("org.apache.hadoop.hdfs.tools.NNHAServiceTarget", "maxExtraEditsSegmentsToRetain", false);
        ConfEntity entity444 = new ConfEntity("org.apache.hadoop.hdfs.tools.DFSHAAdmin", "nameNodePrincipal", false);
        ConfEntity entity555 = new ConfEntity("org.apache.hadoop.hdfs.server.namenode.FSDirectory", "maxDirItems", false);

        List<ConfEntity> list =new LinkedList<ConfEntity>();
//        list.add(entity1);
//        list.add(entity2);
//        list.add(entity3);
        list.add(entity4);

//        list.add(entity11);
//        list.add(entity22);
//        list.add(entity33);
//        list.add(entity44);

//        list.add(entity111);
//        list.add(entity222);


        return list;
    }

    //check
    public static void checkNotNull(Object o) {
        checkNotNull(o, null);
    }

    public static void checkTrue(boolean b) {
        checkTrue(b, "");
    }

    public static void checkTrue(boolean b, String s) {
        if (!b) {
            System.err.println("error:"+s);
            //throw new RuntimeException(s);
        }
    }

    public static void checkNotNull(Object o, String msg) {
        if (o == null) {
            System.err.println(msg);
            throw new RuntimeException(msg);
        }
    }

    //format
    public static String translateSlashToDot(String str) {
        assert str != null;
        return str.replace('/', '.');
    }

    //entryPoint  计数
    public static <T> int countIterable(Iterable<T> c) {
        int count = 0;
        for (T t : c) {
            count++;
        }
        return count;
    }

    //loadclass
    public static Class<?> loadClass(String classPath, String className) {
        String[] paths = classPath.split(Sep.pathSep);
        File[] files = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = new File(paths[i]);
        }
        try {
            URL[] urls = new URL[files.length];
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURL();
            }
            ClassLoader cl = new URLClassLoader(urls);
            Class<?> cls = cl.loadClass(className);
            return cls;
        } catch (MalformedURLException | ClassNotFoundException ignored) {
        }
        return null;
    }

    public static Field lookUpField(Class<?> clz, String confName) {
        try {
            Field[] fields = clz.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(confName)) {
                    return f;
                }
            }
            //throw new Error("Can not find confName: " + confName + " in " + clz.toString());
        } catch (Throwable e) {
            throw new Error(e);
        }
        return null;
    }

    public static List<String> getConfNameList(List<ConfEntity> confList) {
        List<String> name = new LinkedList<String>();
        int size = confList.size();
        for (int i = 0; i < size; i++) {
            name.add(confList.get(i).getConfName());
        }
        return name;
    }


}
