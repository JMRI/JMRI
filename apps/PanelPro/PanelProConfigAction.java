// PanelProConfigAction.java

package apps.PanelPro;

import apps.*;

/**
 * Swing action to create PanelProConfigFrame
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version	$Revision: 1.1 $
 */
public class PanelProConfigAction extends AbstractConfigAction {

    protected AbstractConfigFile readFile(String name)
        throws org.jdom.JDOMException, java.io.FileNotFoundException {
        PanelProConfigFile file = new PanelProConfigFile();
        if (name!=null) {
            log.debug("using file "+name);
            file.readFile(name);
        } else {
            log.debug("for default file, use "+file.defaultConfigFilename());
            file.readFile(file.defaultConfigFilename());
        }
        return file;
    }
    protected AbstractConfigFrame newFrame(String name){
        return new PanelProConfigFrame(name);
    }

    /**
     * Create an action object, reading configuration with the
     * default filename.
     */
    public PanelProConfigAction(String actionName) {
        super(actionName);
    }

    /**
     * Create an action object, using a specific filename for
     * configuration information.
     */
    public PanelProConfigAction(String actionName, String fileName) {
        super(actionName, fileName);
    }

    /** not finding a file or having a config fail isn't
     *  really an error; record it for later
     */
    protected void configFailed() {
        super.configFailed();
    }

    protected void readFailed(Exception e) {
        super.readFailed(e);
    }

    public String getCurrentProtocol2Name() { return (frame != null ? ((PanelProConfigFrame)frame).getCommPane2().getCurrentProtocolName():null); }
    public String getCurrentPort2Name() { return (frame != null ? ((PanelProConfigFrame)frame).getCommPane2().getCurrentPortName():null); }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelProConfigAction.class.getName());
}

