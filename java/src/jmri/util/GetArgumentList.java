// GetArgumentList.java
package jmri.util;

/**
 * Creates the classpath and library java arguments for JMRI
 * <p>
 * Ensures all local jar files are on the class path, and that the program
 * directory and lib subdirectory are on the paths for loading libraries
 * <p>
 * Intended primarily for Windows; other systems use startup scripts so they
 * don't need to start the JVM twice.
 *
 * @author	Bob Jacobsen, Copyright (C) 2008
 * @version $Revision$
 */
public class GetArgumentList {

    // static provide the class path
    static public String getArgumentList() {

        String classpath = "-cp " + GetClassPath.getClassPath();

        String javaextdirs = "-Djava.ext.dirs="
                + System.getProperty("java.ext.dirs")
                + ":.:lib";

        String javalibrarypath = "-Djava.library.path="
                + System.getProperty("java.library.path")
                + ":.:lib";

        // return the result
        return classpath + " " + javaextdirs + " " + javalibrarypath;
    }

    // Main entry point
    static public void main(String[] args) {
        // display result
        System.out.println(getArgumentList());
    }

}
