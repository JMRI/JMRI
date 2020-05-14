package jmri.util.junit;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.lang.reflect.*;

import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;

/**
 * Main method to launch a JUnit test class
 *
 * @author Bob Jacobsen Copyright 2016
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
                // will directly invoke Maim in the class
                Method method = cl.getMethod("main", String[].class);
                method.invoke(null, new Object[] {new String[] { /* put args here */ }});
            } catch (InvocationTargetException e) {
                // main threw an exception, report
                System.err.println(e);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // failed, now invoke as JUnit tests
                run(className);
            }
        } catch (ClassNotFoundException e) {
            // try for a package pattern that handles all tests in a package
            try {
                run(className+".*");
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
        // This shouldn't be necessary, but....
        System.exit(0);
    }

    /**
     * Run tests with a compile-selected RunListener.
     * 
     * @param testClass the class containing tests to run
     */
    public static void run(String testClass) {
        SummaryGeneratingListener listener = new jmri.util.junit.PrintingTestListener(System.out); // test-by-test output if enabled
        run(listener, testClass);
        TestExecutionSummary summary = listener.getSummary();
        PrintWriter p = new PrintWriter(System.out);
        summary.printTo(p);
        summary.printFailuresTo(p);
    }

    /**
     * Run tests with a specified RunListener.
     * 
     * @param listener the listener for the tests
     * @param pattern the filter pattern used for test selection
     */
    public static void run(TestExecutionListener listener, String pattern) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("jmri"))
                .selectors(selectPackage("apps"))
                .filters(includeClassNamePatterns(pattern))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(testPlan);
    }
}
