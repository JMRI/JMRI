package jmri.util;

import java.io.File;

/**
 * Creates a classpath for JMRI from directories
 *
 * @author	Bob Jacobsen, Copyright (C) 2008
 */
public class GetClassPath {

    // static provide the class path
    static public String getClassPath() {
        File programdir = new File(".");
        File libdir = new File("lib");

        StringBuilder classpath = new StringBuilder();

        // add the jar files from the base directory
        String[] programfiles = programdir.list();
        if (programfiles == null) {
             return ". wasn't a directory; failure";
        }

        for (String entry : programfiles) {
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
            classpath.append(entry).append(":");
        }
        // add jmri.jar explicitly
        classpath.append("jmri.jar");

        // add entries from lib/
        String[] libfiles = libdir.list();
        if (libfiles == null) {
             return "lib wasn't a directory; failure";
        }

        for (String entry : libfiles) {
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
            classpath.append(":lib/").append(entry);
        }

        // return the result
        return classpath.toString();
    }

    // Main entry point
    static public void main(String[] args) {
        // display result
        System.out.println(getClassPath());
    }

}
