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

        setText(Bundle.getString("MenuDebug"));

        add(new jmri.jmrit.MemoryFrameAction(Bundle.getString("MenuItemMemoryUsageMonitor")));
        add(new JSeparator());
        add(new jmri.jmrit.decoderdefn.InstallDecoderFileAction(Bundle.getString("MenuItemImportDecoderFile"), panel));
        add(new jmri.jmrit.decoderdefn.InstallDecoderURLAction(Bundle.getString("MenuItemImportDecoderURL"), panel));
        add(new jmri.jmrit.decoderdefn.DecoderIndexCreateAction(Bundle.getString("MenuItemRecreateDecoderIndex")));
        add(new jmri.jmrit.roster.RecreateRosterAction(Bundle.getString("MenuItemRecreateRoster")));
        add(new JSeparator());
        add(new jmri.jmrit.XmlFileCheckAction(Bundle.getString("MenuItemCheckXMLFile"), panel));
        add(new jmri.jmrit.XmlFileValidateAction(Bundle.getString("MenuItemValidateXMLFile"), panel));
        add(new jmri.jmrit.decoderdefn.NameCheckAction(Bundle.getString("MenuItemCheckDecoderNames"), panel));
        add(new jmri.jmrit.symbolicprog.tabbedframe.ProgCheckAction(Bundle.getString("MenuItemCheckProgrammerNames"), panel));
        add(new JSeparator());
		add(new jmri.jmrit.LogixLoadAction(Bundle.getString("MenuItemLogixDisabled"), panel));
        add(new jmri.jmrit.log.LogAction(Bundle.getString("MenuItemLogAction")));
        add(new jmri.jmrit.log.LogOutputWindowAction(Bundle.getString("MenuItemLogOutputWindowAction")));
        add(new jmri.util.swing.JmriNamedPaneAction(Bundle.getString("MenuItemLogTreeAction"), 
            new jmri.util.swing.sdi.JmriJFrameInterface(),
            "jmri.jmrit.log.Log4JTreePane"));
        add(new JSeparator());
	JMenu vsdMenu = new JMenu(Bundle.getString("VSDMenuItem"));
	vsdMenu.add(new jmri.jmrit.vsdecoder.VSDecoderCreationAction(Bundle.getString("VSDecoderManagerAction"), true));
	vsdMenu.add(new jmri.jmrit.vsdecoder.swing.ManageLocationsAction(Bundle.getString("VSDecoderLocationManager"), null));
	JMenu oldVsdMenu = new JMenu(Bundle.getString("OldVSDInterfaceMenuItem"));
	oldVsdMenu.add(new jmri.jmrit.vsdecoder.VSDecoderCreationAction(Bundle.getString("OldVSDecoderWindow"), false));
	oldVsdMenu.add(new jmri.jmrit.beantable.SetPhysicalLocationAction(Bundle.getString("OldSetReporterLocationsAction"), null));
	oldVsdMenu.setEnabled(false);
	vsdMenu.add(oldVsdMenu);
	add(vsdMenu);


    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DebugMenu.class.getName());
}


