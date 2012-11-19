// DebugMenu.java

package jmri.jmrit;

import javax.swing.*;
import java.util.*;

/**
 * Create a "Debug" menu containing the JMRI system-independent 
 * debugging tools.
 *
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 */
public class DebugMenu extends JMenu {
    public DebugMenu(String name, JPanel panel) {
        this(panel);
        setText(name);
    }

    public DebugMenu(JPanel panel) {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.JmritDebugBundle");

        setText(rb.getString("MenuDebug"));

        add(new jmri.jmrit.MemoryFrameAction(rb.getString("MenuItemMemoryUsageMonitor")));
        add(new JSeparator());
        add(new jmri.jmrit.decoderdefn.InstallDecoderFileAction(rb.getString("MenuItemImportDecoderFile"), panel));
        add(new jmri.jmrit.decoderdefn.InstallDecoderURLAction(rb.getString("MenuItemImportDecoderURL"), panel));
        add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction(rb.getString("MenuItemRecreateDecoderIndex")));
        add(new jmri.jmrit.roster.RecreateRosterAction(rb.getString("MenuItemRecreateRoster")));
        add(new JSeparator());
        add(new jmri.jmrit.XmlFileCheckAction(rb.getString("MenuItemCheckXMLFile"), panel));
        add(new jmri.jmrit.XmlFileValidateAction(rb.getString("MenuItemValidateXMLFile"), panel));
        add(new jmri.jmrit.decoderdefn.NameCheckAction(rb.getString("MenuItemCheckDecoderNames"), panel));
        add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction(rb.getString("MenuItemCheckProgrammerNames"), panel));
        add(new JSeparator());
		add(new jmri.jmrit.LogixLoadAction(rb.getString("MenuItemLogixDisabled"), panel));
        add(new jmri.jmrit.log.LogAction(rb.getString("MenuItemLogAction")));
        add(new jmri.jmrit.log.LogOutputWindowAction(rb.getString("MenuItemLogOutputWindowAction")));
        add(new jmri.util.swing.JmriNamedPaneAction(rb.getString("MenuItemLogTreeAction"), 
            new jmri.util.swing.sdi.JmriJFrameInterface(),
            "jmri.jmrit.log.Log4JTreePane"));
        add(new JSeparator());
	JMenu vsdMenu = new JMenu("Virtual Sound Decoder");
	vsdMenu.add(new jmri.jmrit.vsdecoder.VSDecoderCreationAction("New GUI", true));
	vsdMenu.add(new jmri.jmrit.vsdecoder.VSDecoderCreationAction("Old GUI", false));
	vsdMenu.add(new jmri.jmrit.beantable.SetPhysicalLocationAction("Set Reporter Locations", null));
	vsdMenu.add(new jmri.jmrit.vsdecoder.swing.ManageLocationsAction("Manage VSD Locations", null));
	add(vsdMenu);


    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DebugMenu.class.getName());
}


