package jmri.util.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import jmri.InstanceManager;
import jmri.Plugin;
import jmri.PluginManager;

/**
 * Loads a plugin JAR file.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class PluginLoader {

    private static void loadClassesInJarFile(String filename, List<String> classNameList)
            throws IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {

        File file = new File(filename);

        URL url = file.toURI().toURL();
        URL[] urls = new URL[]{url};

        try (URLClassLoader cl = new URLClassLoader(urls)) {
            for (String className : classNameList) {
                Class cls = cl.loadClass(className);
                if (Plugin.class.isAssignableFrom(cls)) {
                    Object o = cls.getDeclaredConstructor().newInstance();
                    if (o instanceof Plugin) {
                        Plugin p = (Plugin)o;
                        p.init();
                        InstanceManager.getDefault(PluginManager.class).addPlugin(p);
                    } else {
                        throw new IllegalArgumentException("Class is not a jmri.Plugin");
                    }
                }
            }
            cl.close();
        }
    }

    public static void loadJarFile(String filename)
            throws FileNotFoundException, IOException, ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {

        List<String> classNameList = new ArrayList<>();

        try (JarInputStream jarFile = new JarInputStream(new FileInputStream(filename))) {
            JarEntry jarEntry;

            while (true) {
                jarEntry = jarFile.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                if ((jarEntry.getName().endsWith(".class"))) {
                    String className = jarEntry.getName().replaceAll("/", "\\.");
                    String myClass = className.substring(0, className.lastIndexOf('.'));
                    classNameList.add(myClass);
                }
            }
        }

        loadClassesInJarFile(filename, classNameList);
    }

}
