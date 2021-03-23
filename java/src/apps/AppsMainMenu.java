package apps;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URISyntaxException;
import java.util.EventObject;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import apps.jmrit.DebugMenu;
import apps.plaf.macosx.Application;

import jmri.jmrit.ToolsMenu;
import jmri.jmrit.decoderdefn.PrintDecoderListAction;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.jython.RunJythonScript;
import jmri.jmrit.operations.OperationsMenu;
import jmri.jmrit.roster.swing.RosterMenu;
import jmri.jmrit.withrottle.WiThrottleCreationAction;
import jmri.jmrix.ActiveSystemsMenu;
import jmri.util.FileUtil;
import jmri.util.HelpUtil;
import jmri.util.JmriJFrame;
import jmri.util.SystemType;
import jmri.util.WindowMenu;
import jmri.util.swing.WindowInterface;
import jmri.web.server.WebServerAction;

/**
  * Create the main menu for PanelPro and related apps.  Includes opening PanelPro from
  * DecoderPro3.
  * <p>
  * Redundant menu code was removed from {@link apps.Apps} and {@link apps.AppsLaunchFrame}.
  *
  * @author Dave Sand Copyright (C) 2021
  */
public class AppsMainMenu {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.jython.JythonBundle");      // Link for script menu items  // NOI18N
    static Action prefsAction;

    public AppsMainMenu() {
    }

    /**
    * Add menus to a menu bar.
    * <p>
    * This does not include the development menu.
    *
    * @param menuBar The existing menu bar
    * @param wi      The WindowInterface to associate actions in menus with
    * @param pane    The JPanel to associate actions in menus with
    * @param windowHelpID The the help id to be assigned to Help / Window Help...
    */
    static protected void createMenus(JMenuBar menuBar, WindowInterface wi, JPanel pane, String windowHelpID) {
        fileMenu(menuBar, wi);
        editMenu(menuBar, wi);
        toolsMenu(menuBar, wi);
        rosterMenu(menuBar, wi, pane);
        panelMenu(menuBar, wi);
        scriptMenu(menuBar, wi);
        // check to see if operations should be in the main menu
        if (jmri.jmrit.operations.setup.Setup.isMainMenuEnabled()) {
            operationsMenu(menuBar, wi);
        }
        systemsMenu(menuBar, wi);
        debugMenu(menuBar, wi, pane);
        menuBar.add(new WindowMenu(wi));
        helpMenu(menuBar, wi, pane, windowHelpID);
    }

    static private void fileMenu(JMenuBar menuBar, WindowInterface wi) {
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));  // NOI18N
        menuBar.add(fileMenu);

        fileMenu.add(new jmri.configurexml.LoadXmlUserAction(Bundle.getMessage("FileMenuItemLoad")));  // NOI18N
        fileMenu.add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("FileMenuItemStore")));  // NOI18N
        fileMenu.add(new jmri.jmrit.revhistory.swing.FileHistoryAction(Bundle.getMessage("FileMenuItemHistory")));  // NOI18N

        fileMenu.add(new JSeparator());

        fileMenu.add(new PrintDecoderListAction(Bundle.getMessage("MenuPrintDecoderDefinitions"), wi.getFrame(), false));  // NOI18N
        fileMenu.add(new PrintDecoderListAction(Bundle.getMessage("MenuPrintPreviewDecoderDefinitions"), wi.getFrame(), true));  // NOI18N

        // Use Mac OS X native Quit if using Aqua look and feel
        if (!(SystemType.isMacOSX() && UIManager.getLookAndFeel().isNativeLookAndFeel())) {
            fileMenu.add(new JSeparator());
            fileMenu.add(new AbstractAction(Bundle.getMessage("MenuItemQuit")) {  // NOI18N
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleQuit();
                }
            });
        }
    }

    static private void editMenu(JMenuBar menuBar, WindowInterface wi) {

        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));  // NOI18N
        menuBar.add(editMenu);

        // cut, copy, paste
        AbstractAction a;
        a = new DefaultEditorKit.CutAction();
        a.putValue(Action.NAME, Bundle.getMessage("MenuItemCut"));  // NOI18N
        editMenu.add(a);
        a = new DefaultEditorKit.CopyAction();
        a.putValue(Action.NAME, Bundle.getMessage("MenuItemCopy"));  // NOI18N
        editMenu.add(a);
        a = new DefaultEditorKit.PasteAction();
        a.putValue(Action.NAME, Bundle.getMessage("MenuItemPaste"));  // NOI18N
        editMenu.add(a);

        // prefs
        prefsAction = new apps.gui3.tabbedpreferences.TabbedPreferencesAction(Bundle.getMessage("MenuItemPreferences"));  // NOI18N

        // Put prefs in Apple's prefered area on Mac OS X
        if (SystemType.isMacOSX()) {
            Application.getApplication().setPreferencesHandler((EventObject eo) -> {
                prefsAction.actionPerformed(null);
            });
        }
        // Include prefs in Edit menu if not on Mac OS X or not using Aqua Look and Feel
        if (!SystemType.isMacOSX() || !UIManager.getLookAndFeel().isNativeLookAndFeel()) {
            editMenu.addSeparator();
            editMenu.add(prefsAction);
        }

    }

    static private void toolsMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(new ToolsMenu(Bundle.getMessage("MenuTools")));  // NOI18N
    }

    /**
     * Add a script menu to the main menu bar.
     *
     * @param menuBar the menu bar to add the script menu to
     * @param wi      the window interface containing menuBar
     */
    static private void scriptMenu(JMenuBar menuBar, WindowInterface wi) {
        JMenu scriptMenu = new JMenu(rb.getString("MenuScripting"));  // NOI18N
        scriptMenu.add(new jmri.jmrit.jython.RunJythonScript(rb.getString("MenuItemScript")));  // NOI18N
        scriptMenu.add(new jmri.jmrit.automat.monitor.AutomatTableAction(rb.getString("MenuItemMonitor")));  // NOI18N
        scriptMenu.add(new jmri.jmrit.jython.JythonWindow(rb.getString("MenuItemScriptLog")));  // NOI18N
        scriptMenu.add(new jmri.jmrit.jython.InputWindowAction(rb.getString("MenuItemScriptInput")));  // NOI18N
        menuBar.add(scriptMenu);
    }

    static private void operationsMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(new OperationsMenu());
    }

    static private void rosterMenu(JMenuBar menuBar, WindowInterface wi, JPanel pane) {
        menuBar.add(new RosterMenu(Bundle.getMessage("MenuRoster"), RosterMenu.MAINMENU, pane));  // NOI18N
    }

    static private void panelMenu(JMenuBar menuBar, WindowInterface wi) {
        menuBar.add(new PanelMenu());
    }

    /**
     * Show only active systems in the menu bar.
     *
     * @param menuBar the menu to attach systems menus to
     * @param wi      ignored, but available for overriding methods to use if
     *                needed
     */
    static private void systemsMenu(JMenuBar menuBar, WindowInterface wi) {
        ActiveSystemsMenu.addItems(menuBar);
    }

    static private void debugMenu(JMenuBar menuBar, WindowInterface wi, JPanel pane) {
        JMenu d = new DebugMenu(pane);

        // also add some tentative items from jmrix
        d.add(new JSeparator());
        d.add(new jmri.jmrix.pricom.PricomMenu());
        d.add(new JSeparator());

        d.add(new jmri.jmrix.jinput.treecontrol.TreeAction());
        d.add(new jmri.jmrix.libusb.UsbViewAction());

        d.add(new JSeparator());
        try {
            d.add(new RunJythonScript(Bundle.getMessage("MenuRailDriverThrottle"), new File(FileUtil.findURL("jython/RailDriver.py").toURI())));  // NOI18N
        } catch (URISyntaxException | NullPointerException ex) {
            log.error("Unable to load RailDriver Throttle", ex);  // NOI18N
            JMenuItem i = new JMenuItem(Bundle.getMessage("MenuRailDriverThrottle"));  // NOI18N
            i.setEnabled(false);
            d.add(i);
        }

        // also add some tentative items from webserver
        d.add(new JSeparator());
        d.add(new WebServerAction());

        d.add(new JSeparator());
        d.add(new WiThrottleCreationAction());

        d.add(new JSeparator());
        d.add(new apps.TrainCrew.InstallFromURL());

        // add final to menu bar
        menuBar.add(d);
    }

//     protected void developmentMenu(JMenuBar menuBar, WindowInterface wi) {
//         JMenu devMenu = new JMenu("Development");
//         menuBar.add(devMenu);
//         devMenu.add(new jmri.jmrit.symbolicprog.autospeed.AutoSpeedAction("Auto-speed tool"));
//         devMenu.add(new JSeparator());
//         devMenu.add(new jmri.jmrit.automat.SampleAutomatonAction("Sample automaton 1"));
//         devMenu.add(new jmri.jmrit.automat.SampleAutomaton2Action("Sample automaton 2"));
//         devMenu.add(new jmri.jmrit.automat.SampleAutomaton3Action("Sample automaton 3"));
//         //devMenu.add(new JSeparator());
//         //devMenu.add(new jmri.jmrix.serialsensor.SerialSensorAction("Serial port sensors"));
//     }

    static private void helpMenu(JMenuBar menuBar, WindowInterface wi, JPanel containedPane, String windowHelpID) {
        // create menu and standard items
        JMenu helpMenu = HelpUtil.makeHelpMenu(windowHelpID, true);

        // tell help to use default browser for external types
        HelpUtil.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");  // NOI18N

        // use as main help menu
        menuBar.add(helpMenu);
    }

    /**
     * The application decided to quit, handle that.
     *
     * @return true if successfully ran all shutdown tasks and can quit; false
     *         otherwise
     */
    static private boolean handleQuit() {
        return AppsBase.handleQuit();
    }

     static private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppsMainMenu.class);}
