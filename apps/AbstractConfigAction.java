// AbstractConfigAction.java

package apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Abstract base action to create a ConfigFrame
 *
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision: 1.7 $
 */
abstract public class AbstractConfigAction 			extends AbstractAction {

    abstract protected AbstractConfigFile readFile(String name)  throws org.jdom.JDOMException, java.io.FileNotFoundException;
    abstract protected AbstractConfigFrame newFrame(String name);

    public AbstractConfigAction(String actionName) {
        super(actionName);
        configure(null);
    }

    public AbstractConfigAction(String actionName, String fileName) {
        super(actionName);
        configure(fileName);
    }

    protected void configure(String fileName) {
        // see if the file can be read
        try {
            jmri.jmrit.XmlFile.ensurePrefsPresent(jmri.jmrit.XmlFile.prefsDir());
            AbstractConfigFile file = readFile(fileName);
            log.debug("configuration file located and read");
            frame = newFrame("Preferences");
            log.debug("start configuration from file: "+file.defaultConfigFilename());

            // try to configure
            configOK = false;
            try { configOK = frame.configure(file); }
            catch (java.lang.UnsatisfiedLinkError e) {
                // failed badly - tell about it
                log.error("unexpected error configuring application. Quit application, delete prefs/*Config.xml and try again\n"+e);
                // make sure config frame is presented
                configOK = false;
            }
            if ( !configOK ) {
                // config failed, handle that
                configFailed();
            }
            log.debug("configuration complete");
        } catch (Exception e) {
            readFailed(e);
        }
    }
    public void actionPerformed(ActionEvent e) {
        frame.show();
    }

    public boolean configOK;

    /**
     * What action should be taken if there's
     * a failure while doing the configure?
     * Default is to show the frame.
     */
    protected void configFailed() {
        configOK = false;
        frame.show();
    }

    /**
     * What action should be taken if there's
     * a failure while doing the file read?
     * Default is to show the frame.
     */
    protected void readFailed(Exception e) {
        // did not succeed, have to pop now
        configOK = false;
        log.info("configuration file could not be located and read");
        log.debug("error was "+e);
        // create an unconfigured Frame
        frame = newFrame("Preferences");
        frame.show();
    }

    public String getCurrentProtocolName() { return (frame != null ? frame.getCommPane().getCurrentProtocolName():null); }
    public String getCurrentPortName() { return (frame != null ? frame.getCommPane().getCurrentPortName():null); }

    AbstractConfigFrame frame = null;

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractConfigAction.class.getName());

}

