package jmri.util;

import java.io.File;

/**
 * Creates a classpath for JMRI from directories
 *
 * @author	Bob Jacobsen, Copyright (C) 2008
 */
public class GetClassPath {

    // static provide the class path
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SBSC_USE_STRINGBUFFER_CONCATENATION") // not a performance issue
    static public String getClassPath() {
        File programdir = new File(".");
        File libdir = new File("lib");

        String classpath = "";

        // add the jar files from the base directory
        String[] programfiles = programdir.list();
        if (programfiles == null) {
             return ". wasn't a directory; failure";
        }

        for (int i = 0; i < programfiles.length; i++) {
            String entry = programfiles[i];
            // check that this file should go on the class path
            if (entry.length() < 5) {
                continue;
            }
            if (entry.lastIndexOf(".jar") != entry.length() - 4) {
                continue;
            }
            if ((new File(entry)).isDirectory()) {
                continue;
            }
            if (entry.equals("jmri.jar")) {
                continue;
            }
            // OK, it should
            classpath = classpath + entry + ":";
        }
        // add jmri.jar explicitly
        classpath = classpath + "jmri.jar";

        // add entries from lib/
        String[] libfiles = libdir.list();
        if (libfiles == null) {
             return "lib wasn't a directory; failure";
        }

        for (int i = 0; i < libfiles.length; i++) {
            String entry = libfiles[i];
            // check that this file should go on the class path
            if (entry.length() < 5) {
                continue;
            }
            if (entry.lastIndexOf(".jar") != entry.length() - 4) {
                continue;
            }
            if ((new File(entry)).isDirectory()) {
                continue;
            }
            if (entry.equals("jmri.jar")) {
                continue;
            }
            // OK, it should
            classpath = classpath + ":lib/" + entry;
        }

        // return the result
        return classpath;
    }

    // Main entry point
    static public void main(String[] args) {
        // display result
        System.out.println(getClassPath());
    }

}
