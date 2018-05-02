package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.tools.PrintSwitchListAction;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Yardmaster Frame. Shows work at one location.
 *
 * @author Dan Boudreau Copyright (C) 2013
 * 
 */
public class YardmasterFrame extends OperationsFrame {

    public YardmasterFrame(Location location) {
        super(Bundle.getMessage("Yardmaster"), new YardmasterPanel(location));
        this.initComponents(location);
    }

    private void initComponents(Location location) {
        super.initComponents();

        if (location != null) {

            // build menu
            JMenuBar menuBar = new JMenuBar();
            JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
            toolMenu.add(new YardmasterByTrackAction(location));
            JMenuItem print = toolMenu.add(new PrintSwitchListAction(Bundle.getMessage("MenuItemPrint"), location,
                    false));
            JMenuItem preview = toolMenu.add(new PrintSwitchListAction(Bundle.getMessage("MenuItemPreview"), location,
                    true));
            menuBar.add(toolMenu);
            setJMenuBar(menuBar);

            // add tool tip if in consolidation mode: "Disabled when switch list is in consolidation mode"
            if (!Setup.isSwitchListRealTime()) {
                print.setToolTipText(Bundle.getMessage("TipDisabled"));
                preview.setToolTipText(Bundle.getMessage("TipDisabled"));
            }
            setTitle(Bundle.getMessage("Yardmaster") + " (" + location.getName() + ")");
        }

        addHelpMenu("package.jmri.jmrit.operations.Operations_Locations", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }

//    private static final Logger log = LoggerFactory.getLogger(YardmasterFrame.class);
}
