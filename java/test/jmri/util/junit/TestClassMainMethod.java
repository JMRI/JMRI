package jmri.util.junit;

import java.lang.reflect.*;

import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

/**
 * Main method to launch a JUnit test class
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class TestClassMainMethod {

    // Main entry point
    static public void main(String[] args) {
        String className = args[0];
        
        // as a convenience, allow file names and paths 
        className = className.replace("//","/");    
        if (className.endsWith(".java")) className = className.replace(".java","");
        if (className.startsWith("java/test/")) className = className.replace("java/test/","");
        if (className.startsWith("java/src/")) className = className.replace("java/src/","");
        if (className.startsWith("/")) className = className.substring(1, className.length());

        // as a convenience, allow e.g. jmri/BundleTest in addition to jmri.BundleTest
        className = className.replace('/','.');    
        className = className.replace("..",".");    
        
        try {
            Class<?> cl = Class.forName(className);
            // first try to find a main in the class
            try {
                Method method = cl.getMethod("main", String[].class);
                method.invoke(null, new Object[] {new String[] { /* put args here */ }});
            } catch (InvocationTargetException e) {
                // main threw an exception, report
                System.err.println(e);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // failed, now invoke manually
                run(cl);
            }
        } catch (ClassNotFoundException e) {
            // log error
            System.err.println("Unable to locate class " + className);
        }
    }

    /**
     * Run tests with a default RunListener.
     * 
     * @param testClass the class containing tests to run
     */
    public static void run(Class<?> testClass) {
        run(new TextListener(System.out), testClass);
    } 

    /**
     * Run tests with a specified RunListener.
     * 
     * @param listener the listener for the tests
     * @param testClass the class containing tests to run
     */
    public static void run(RunListener listener, Class<?> testClass) {
        JUnitCore runner = new JUnitCore();
        runner.addListener(listener);
        Result result = runner.run(testClass);
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
