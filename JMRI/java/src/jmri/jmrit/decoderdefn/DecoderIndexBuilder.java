package jmri.jmrit.decoderdefn;

/**
 * Update the decoder index and store as a command-line action
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2011, 2014
 * @author Randall Wood Copyright (C) 2013
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

        // print the location where the result is stored
        System.out.println(jmri.util.FileUtil.getUserFilesPath() + "decoderIndex.xml");

        // recreate the index
        DecoderIndexCreateAction da = new DecoderIndexCreateAction(null);
        da.setIncrement(true);
        da.actionPerformed(null);
    }
}
