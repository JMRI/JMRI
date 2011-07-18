// InstallDecoderFileAction.java

package jmri.jmrit.decoderdefn;

import java.io.*;

import javax.swing.*;
import java.net.URL;


/**
 * Install decoder definition from local file.
 *
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision$
 * @see jmri.jmrit.XmlFile
 */
public class InstallDecoderFileAction extends InstallDecoderURLAction {
    
    public InstallDecoderFileAction(String s) {
        super(s);
    }
    
    public InstallDecoderFileAction(String s, JPanel who) {
        super(s);
    }
    
    JFileChooser fci;
    
    URL pickURL(JPanel who) {
        if (fci==null) {
            fci = jmri.jmrit.XmlFile.userFileChooser("XML files", "xml");
        }
        // request the filename from an open dialog
        fci.rescanCurrentDirectory();
        int retVal = fci.showOpenDialog(who);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            if (log.isDebugEnabled()) log.debug("located file "+file+" for XML processing");
            try {
                return new URL("file:"+file.getCanonicalPath());
            } catch (Exception e) {
                log.error("Unexpected exception in new URL: "+e);
                return null;
            }
        } else {
            log.debug("cancelled in open dialog");
            return null;
        }
    }
    
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstallDecoderFileAction.class.getName());
    
}
