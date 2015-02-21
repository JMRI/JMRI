// GetJavaProperty.java
package jmri.util;

/**
 * Print a Java system variable from the command line
 * <p>
 * Intented to be invoked like:<br>
 * <code><pre>
 * java jmri.util.GetJavaProperty java.home
 * </pre><code><br>
 * to print the value of a Java system property in the current JVM, e.g. for
 * including in a script
 *
 * @author	Bob Jacobsen, Copyright (C) 2008
 * @version $Revision$
 */
public class GetJavaProperty {

    // Main entry point
    static public void main(String[] args) {
        if (args.length < 1 || args[0].equals("-h")) {
            System.out.println("Provide the name of a system property as an argument");
            return;
        }
        System.out.println(System.getProperty(args[0]));
    }
}
