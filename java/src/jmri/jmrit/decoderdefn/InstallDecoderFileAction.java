package jmri.jmrit.decoderdefn;

import java.io.File;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Install decoder definition from local file.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 * @see jmri.jmrit.XmlFile
 */
public class InstallDecoderFileAction extends InstallDecoderURLAction {

    public InstallDecoderFileAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public InstallDecoderFileAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public InstallDecoderFileAction(String s) {
        super(s);
    }

    public InstallDecoderFileAction(String s, JPanel who) {
        super(s);
    }

    JFileChooser fci;

    @Override
    URL pickURL(JPanel who) {
        if (fci == null) {
            fci = jmri.jmrit.XmlFile.userFileChooser("XML files", "xml");
        }
        // request the filename from an open dialog
        fci.rescanCurrentDirectory();
        int retVal = fci.showOpenDialog(who);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("located file " + file + " for XML processing");
            }
            try {
                return new URL("file:" + file.getCanonicalPath());
            } catch (Exception e) {
                log.error("Unexpected exception in new URL: " + e);
                return null;
            }
        } else {
            log.debug("cancelled in open dialog");
            return null;
        }
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(InstallDecoderFileAction.class);

}
