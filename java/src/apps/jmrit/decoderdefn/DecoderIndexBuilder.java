package apps.jmrit.decoderdefn;

import jmri.jmrit.decoderdefn.DecoderIndexCreateAction;

/**
 * Update the decoder index and store as a command-line action.
 * <P>
 * Not intended to be referenced from within JMRI itself, as this
 * reconfigures logging for standalone operation.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2011, 2014
 * @author Randall Wood Copyright (C) 2013
 * @see jmri.jmrit.XmlFile
 */
public class DecoderIndexBuilder {

    // main entry point to run standalone
    static public void main(String[] args) {
    
        // logging needed for code invoked from here
        String configFile = "default_lcf.xml";
        apps.util.Log4JUtil.initLogging(configFile);
        Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());

        // log the location where the result is stored
        System.out.println(jmri.util.FileUtil.getUserFilesPath() + "decoderIndex.xml"); // command line

        // recreate the index
        DecoderIndexCreateAction da = new DecoderIndexCreateAction(null);
        da.setIncrement(true);
        da.actionPerformed(null);
    }
}
