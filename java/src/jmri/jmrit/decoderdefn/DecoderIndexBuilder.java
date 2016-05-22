// DecoderIndexCreateAction.java
package jmri.jmrit.decoderdefn;

/**
 * Update the decoder index and store as a command-line action
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2011
 * @author      Randall Wood Copyright (C) 2013
 * @version	$Revision$
 * @see jmri.jmrit.XmlFile
 */
public class DecoderIndexBuilder {

    // main entry point to run standalone
    static public void main(String[] args) {
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure("default.lcf");
            } else {
                org.apache.log4j.BasicConfigurator.configure();
            }
        } catch (java.lang.NoSuchMethodError e) {
            System.out.println("Exception starting logging: " + e);
        }

        (new DecoderIndexCreateAction(null)).actionPerformed(null);
    }
}