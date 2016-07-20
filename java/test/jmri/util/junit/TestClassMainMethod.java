package jmri.util.junit;

import java.lang.reflect.*;

/**
 * Main method to launch a JUnit test class
 *
 * @author	Bob Jacobsen Copyright 2016
 */
public class TestClassMainMethod {

    // Main entry point
    static public void main(String[] args) {
        String className = args[0];        
        try {
            // first try to find a main in the class
            Class<?> cl = Class.forName(className);
            Method method = cl.getMethod("main", String[].class);
            method.invoke(null, new Object[] {new String[] { /* put args here */ }});
        } catch (InvocationTargetException e) {
            // main threw an exception, report
            System.err.println(e);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            // failed, now invoke manually
            org.junit.runner.JUnitCore.main(className);
        }
    }
}
