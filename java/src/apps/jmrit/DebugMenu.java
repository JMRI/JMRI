package apps.jmrit;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ResourceBundle;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import jmri.jmrit.jython.RunJythonScript;
import jmri.util.FileUtil;

/**
 * Create a "Debug" menu containing the JMRI system-independent debugging tools.
 *
 * @author Bob Jacobsen Copyright 2003
 */
public class DebugMenu extends JMenu {

    static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");

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
        add(new apps.jmrit.log.LogAction(Bundle.getMessage("MenuItemLogAction")));
        add(new jmri.util.swing.JmriNamedPaneAction(Bundle.getMessage("MenuItemLogTreeAction"),
                new jmri.util.swing.sdi.JmriJFrameInterface(),
                "jmri.jmrit.log.Log4JTreePane"));

        add(new JSeparator());
        add(new jmri.jmrit.LogixLoadAction(Bundle.getMessage("MenuItemLogixDisabled"), panel));

        // also add some tentative items from jmrix
        add(new JSeparator());
        add(new jmri.jmrix.pricom.PricomMenu());

        add(new JSeparator());
        add(new jmri.jmrix.jinput.treecontrol.TreeAction());
        add(new jmri.jmrix.libusb.UsbViewAction());

        add(new JSeparator());
        try {
            add(new RunJythonScript(rb.getString("MenuRailDriverThrottle"), new File(FileUtil.findURL("jython/RailDriver.py").toURI())));  // NOI18N
        } catch (URISyntaxException | NullPointerException ex) {
            log.error("Unable to load RailDriver Throttle", ex);  // NOI18N
            JMenuItem i = new JMenuItem(rb.getString("MenuRailDriverThrottle"));  // NOI18N
            i.setEnabled(false);
            add(i);
        }

        add(new JSeparator());
        add(new apps.TrainCrew.InstallFromURL());
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DebugMenu.class);
}
