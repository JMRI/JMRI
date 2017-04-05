package jmri.jmrit;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Create a "Debug" menu containing the JMRI system-independent debugging tools.
 *
 * @author Bob Jacobsen Copyright 2003
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
        add(new jmri.jmrit.roster.UpdateDecoderDefinitionAction(Bundle.getMessage("MenuItemUpdateDecoderDefinition")));
        add(new JSeparator());
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
        JMenu vsdMenu = new JMenu(Bundle.getMessage("MenuItemVSDecoder"));
        vsdMenu.add(new jmri.jmrit.vsdecoder.VSDecoderCreationAction(Bundle.getMessage("MenuItemVSDecoderManager"), true));
        vsdMenu.add(new jmri.jmrit.vsdecoder.swing.ManageLocationsAction(Bundle.getMessage("MenuItemVSDecoderLocationManager"), null));
        vsdMenu.add(new jmri.jmrit.vsdecoder.swing.VSDPreferencesAction(Bundle.getMessage("MenuItemVSDecoderPreferences")));
        add(vsdMenu);

    }
}
