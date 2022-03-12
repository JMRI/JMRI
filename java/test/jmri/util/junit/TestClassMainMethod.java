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
 * Main method to run JMRI code.
 *
 * It takes an rather-general format argument and then attempts to
 * <ul>
 *   <li>If it reduces to a fully-qualified class name, try running a main() method in the Class
 *   <li>Otherwise, create a JUnit5 runner and ask that to run the request.
 * </ul>
 *
 * To make the input string more general by allowing filenames and directory paths to be used,
 * some reductions are done:
 * <ul>
 *   <li>"//" is replaced by "/"
 *   <li>Any trailing ".java" is removed
 *   <li>Any trailing "/" is removed
 *   <li>Any preceding "java/src/" or "java/test" is removed
 *   <li>Any preceding "/" is removed
 *   <li>"/" is replaced by "."
 *   <li>".." is replaced by "."
 * </ul>
 *
 * Typical uses (as embedded in the JMRI development environment):
 * <ul>
 *  <li>./runtest.csh java/test/jmri/beans
 *  <li>./runtest.csh java/test/jmri/VersionTest
 * </ul>
 *
 * @author Bob Jacobsen Copyright 2016, 2020
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
        if (className.endsWith("/")) className = className.substring(0, className.length()-1);

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
                // if main returns, we return from here; threads may still be running
                // i.e. if this is a JMRI app.
                return;
            } catch (InvocationTargetException e) {
                // main threw an exception, report
                System.err.println(e);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                // failed, now invoke as JUnit tests
                System.exit(run(className));
            }
        } catch (ClassNotFoundException e) {
            // try for a package pattern that handles all tests in a package
            try {
                System.exit(run(className+".*"));
            } catch (Exception ex) {
                System.err.println(ex);
            }
        }
        // This shouldn't be necessary, but....
        System.err.println("Fall-through exit is a fail");
        System.exit(1);
    }

    /**
     * Run tests with a compile-selected RunListener.
     *
     * @param pattern the class or package containing tests to run
     */
    public static int run(String pattern) {
        SummaryGeneratingListener listener = new jmri.util.junit.PrintingTestListener(System.out); // test-by-test output if enabled

        run(listener, pattern);

        TestExecutionSummary summary = listener.getSummary();
        PrintWriter p = new PrintWriter(System.out);
        summary.printTo(p);
        summary.printFailuresTo(p);
        return (int)(summary.getContainersFailedCount()+summary.getTestsFailedCount());
    }

    /**
     * Run tests with a specified RunListener.
     *
     * @param listener the listener for the tests
     * @param pattern the filter pattern used for test selection
     */
    public static void run(TestExecutionListener listener, String pattern) {
        LauncherDiscoveryRequest request;

        if (pattern.endsWith(".*")) {  // package form
            String packagePattern = pattern.replace(".*","");
            request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage(packagePattern))
                .build();
        } else { // single-class form
            request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(pattern))
                .build();
        }

        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(testPlan);
    }
}
