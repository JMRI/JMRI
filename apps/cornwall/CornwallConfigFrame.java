// CornwallConfigFrame.java

package apps.cornwall;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jdom.Element;
import org.jdom.Attribute;
import apps.*;
import jmri.*;
import jmri.configurexml.*;

/**
 * CornwallConfigFrame provides startup configuration, a GUI for setting
 * config/preferences, and read/write support.  Its specific to CornwallRR
 * but should eventually be generalized.  Note that routine GUI config,
 * menu building, etc is done in other code.
 *<P>For now, we're implicitly assuming that configuration of these
 * things is _only_ done here, so that we don't have to track anything
 * else.  When asked to write the config, we just write the values
 * stored in local variables.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.4 $
 */
public class CornwallConfigFrame extends AbstractConfigFrame {

    public CornwallConfigFrame(String name) {
        super(name);
    }

    protected void addCommPane() {
        commPane = new DefaultCommConfigPane(availableProtocols());
        getContentPane().add(commPane);
        getContentPane().add(new JSeparator());
        commPane2 = new DefaultCommConfigPane(availableProtocols());
        getContentPane().add(commPane2);
        getContentPane().add(new JSeparator());
    }

    protected DefaultCommConfigPane commPane2;
    public DefaultCommConfigPane getCommPane2() {
        return commPane2;
    }

    /**
     * CornwallRR only supports the C/MRI and LocoNet protocols, so the parent member is overloaded
     */
    public String[] availableProtocols() {
        return  new String[] {"(None selected)",
                              "CMRI serial",
                              "LocoNet LocoBuffer","LocoNet MS100",
                              "LocoNet Server", "LocoNet HexFile"
        };
    }

    /**
     * Command reading the configuration, and setting it into the application.
     * Returns true if
     * a configuration file was found and loaded OK.
     * @param file Input configuration file
     * @throws jmri.JmriException from internal code
     * @return true if successful
     */
    public boolean configure(AbstractConfigFile file) throws jmri.JmriException {
        boolean connected = commPane.configureConnection(file.getConnectionElement());
        boolean connected2 = commPane2.configureConnection(((CornwallConfigFile)file).getConnectionElement2());
        boolean gui = configureGUI(file.getGuiElement());
        boolean programmer = configureProgrammer(file.getProgrammerElement());

        // ensure that an XML config manager exists
        if (InstanceManager.configureManagerInstance()==null)
            InstanceManager.setConfigureManager(new ConfigXmlManager());

        // install custom TurnoutManager
        InstanceManager.setTurnoutManager(new DoubleTurnoutManager());

        return connected&&connected2&&gui&&programmer;
    }

    /**
     * Abstract method to save the data
     */
    public void saveContents() {
        CornwallConfigFile f = new CornwallConfigFile();
        f.makeBackupFile(f.defaultConfigFilename());
        f.writeFile(f.defaultConfigFilename(), this);
    }


    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CornwallConfigFrame.class.getName());

}
