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

        setText(Bundle.getMessage("MenuDebug"));

        add(new jmri.jmrit.MemoryFrameAction(Bundle.getMessage("MenuItemMemoryUsageMonitor")));
        add(new JSeparator());
        add(new jmri.jmrit.decoderdefn.InstallDecoderFileAction(Bundle.getMessage("MenuItemImportDecoderFile"), panel));
        add(new jmri.jmrit.decoderdefn.InstallDecoderURLAction(Bundle.getMessage("MenuItemImportDecoderURL"), panel));
        add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction(Bundle.getMessage("MenuItemRecreateDecoderIndex")));
        add(new jmri.jmrit.roster.RecreateRosterAction(Bundle.getMessage("MenuItemRecreateRoster")));
        add(new JSeparator());
        add(new jmri.jmrit.XmlFileCheckAction(Bundle.getMessage("MenuItemCheckXMLFile"), panel));
        add(new jmri.jmrit.XmlFileValidateAction(Bundle.getMessage("MenuItemValidateXMLFile"), panel));
        add(new jmri.jmrit.decoderdefn.NameCheckAction(Bundle.getMessage("MenuItemCheckDecoderNames"), panel));
        add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction(Bundle.getMessage("MenuItemCheckProgrammerNames"), panel));
        add(new JSeparator());
		add(new jmri.jmrit.LogixLoadAction(Bundle.getMessage("MenuItemLogixDisabled"), panel));
        add(new jmri.jmrit.log.LogAction(Bundle.getMessage("MenuItemLogAction")));
        add(new jmri.jmrit.log.LogOutputWindowAction(Bundle.getMessage("MenuItemLogOutputWindowAction")));
        add(new jmri.util.swing.JmriNamedPaneAction(Bundle.getMessage("MenuItemLogTreeAction"), 
            new jmri.util.swing.sdi.JmriJFrameInterface(),
            "jmri.jmrit.log.Log4JTreePane"));
        add(new JSeparator());
	JMenu vsdMenu = new JMenu(Bundle.getMessage("VSDMenuItem"));
	vsdMenu.add(new jmri.jmrit.vsdecoder.VSDecoderCreationAction(Bundle.getMessage("VSDecoderManagerAction"), true));
	vsdMenu.add(new jmri.jmrit.vsdecoder.swing.ManageLocationsAction(Bundle.getMessage("VSDecoderLocationManager"), null));
	JMenu oldVsdMenu = new JMenu(Bundle.getMessage("OldVSDInterfaceMenuItem"));
	oldVsdMenu.add(new jmri.jmrit.vsdecoder.VSDecoderCreationAction(Bundle.getMessage("OldVSDecoderWindow"), false));
	oldVsdMenu.add(new jmri.jmrit.beantable.SetPhysicalLocationAction(Bundle.getMessage("OldSetReporterLocationsAction"), null));
	oldVsdMenu.setEnabled(false);
	vsdMenu.add(oldVsdMenu);
	add(vsdMenu);


    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DebugMenu.class.getName());
}


