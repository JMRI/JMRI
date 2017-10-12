package jmri.jmrit.display.layoutEditor;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import jmri.util.swing.JMenuUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of tools to check various things on the layout editor panel.
 */
public class LayoutEditorChecks {

    private final LayoutEditor layoutEditor;
    private JMenu checkMenu = new JMenu(Bundle.getMessage("CheckMenuTitle"));

    private JMenu checkUnConnectedTracksMenu = new JMenu(Bundle.getMessage("CheckUnConnectedTracksMenuTitle"));
    private JMenuItem checkUnConnectedTracksMenuItem = new JMenuItem(Bundle.getMessage("CheckUnConnectedTracksMenuTitle"));

    private JMenu checkUnBlockedTracksMenu = new JMenu(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));
    private JMenuItem checkUnBlockedTracksMenuItem = new JMenuItem(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));

    public LayoutEditorChecks(LayoutEditor editor) {
        layoutEditor = editor;
    }

    /**
     * set the layout editor checks menu (in the tools menu)
     *
     * @param toolsMenu where to add our "Check" menu and sub-menus
     */
    //Note: initially the sub menu items of the Check menu are JMenuItems with
    // an action attached. The first time this action is preformed the
    // JMenuItem is replaced with a JMenu (with the same name) and the
    // original JMenuItem is reused as the top sub-menu of the replacement
    // JMenu (After being renamed to "Re-Check").
    protected void setupMenu(@Nonnull JMenu toolsMenu) {
        toolsMenu.add(checkMenu);
        checkMenu.setToolTipText(Bundle.getMessage("CheckMenuToolTip"));

        checkUnConnectedTracksMenuItem.setToolTipText(Bundle.getMessage("CheckUnConnectedTracksMenuToolTip"));
        checkMenu.add(checkUnConnectedTracksMenuItem);
        checkUnConnectedTracksMenuItem.addActionListener((ActionEvent event) -> {
            if (checkUnConnectedTracksMenu.getParent() == null) {
                JMenuUtil.replaceMenuItem(checkUnConnectedTracksMenuItem, checkUnConnectedTracksMenu);
                checkUnConnectedTracksMenu.add(checkUnConnectedTracksMenuItem);
                checkUnConnectedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnConnectedTracksMenuToolTip"));
                checkUnConnectedTracksMenuItem.setText(Bundle.getMessage("ReCheckMenuTitle"));
            }
            setupCheckUnConnectedTracksMenu();

            // If we found any results...
            if (checkUnConnectedTracksMenu.getMenuComponentCount() >= 2) {
                // go ahead and perform their action...
                JMenuItem jmi = (JMenuItem) checkUnConnectedTracksMenu.getMenuComponent(2);
                if (jmi != null) {
                    jmi.doClick();
                }
            }
        });

        checkUnBlockedTracksMenuItem.setToolTipText(Bundle.getMessage("CheckUnBlockedTracksMenuToolTip"));
        checkMenu.add(checkUnBlockedTracksMenuItem);
        checkUnBlockedTracksMenuItem.addActionListener((ActionEvent event) -> {
            if (checkUnBlockedTracksMenu.getParent() == null) {
                JMenuUtil.replaceMenuItem(checkUnBlockedTracksMenuItem, checkUnBlockedTracksMenu);
                checkUnBlockedTracksMenu.add(checkUnBlockedTracksMenuItem);
                checkUnBlockedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnBlockedTracksMenuToolTip"));
                checkUnBlockedTracksMenuItem.setText(Bundle.getMessage("ReCheckMenuTitle"));
            }
            setupCheckUnBlockedTracksMenu();

            // If we found any results...
            if (checkUnBlockedTracksMenu.getMenuComponentCount() >= 2) {
                // go ahead and perform their action...
                JMenuItem jmi = (JMenuItem) checkUnBlockedTracksMenu.getMenuComponent(2);
                if (jmi != null) {
                    jmi.doClick();
                }
            }
        });
    }

    // run the un-connected tracks check and populate the checkUnConnectedTracksMenu
    private void setupCheckUnConnectedTracksMenu() {
        log.debug("setupcheckUnConnectedTracksMenu");

        checkUnConnectedTracksMenu.removeAll();
        checkUnConnectedTracksMenu.add(checkUnConnectedTracksMenuItem);

        boolean flag = false;
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            List<Integer> connections = layoutTrack.getAvailableConnections();
            if (!connections.isEmpty()) {
                if (!flag) {
                    checkUnConnectedTracksMenu.addSeparator();
                    flag = true;
                }
                // add this layout to the check un-connected menu
                JMenuItem jmi = new JMenuItem(layoutTrack.getName());
                checkUnConnectedTracksMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> {
                    JMenuItem menuItem = (JMenuItem) event.getSource();
                    String menuItemName = menuItem.getText();
                    doCheckUnConnectedTracksMenuItem(menuItemName);
                });
            }
        }
    }

    // run the un-blocked tracks check and populate the checkUnBlockedTracksMenu
    private void setupCheckUnBlockedTracksMenu() {
        log.debug("setupCheckUnBlockedTracksMenu");

        checkUnBlockedTracksMenu.removeAll();
        checkUnBlockedTracksMenu.add(checkUnBlockedTracksMenuItem);

        boolean flag = false;
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            if (!layoutTrack.areAllBlocksAssigned()) {
                if (!flag) {
                    checkUnBlockedTracksMenu.addSeparator();
                    flag = true;
                }
                // add this layout to the check un-connected menu
                JMenuItem jmi = new JMenuItem(layoutTrack.getName());
                checkUnBlockedTracksMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> {
                    JMenuItem menuItem = (JMenuItem) event.getSource();
                    String menuItemName = menuItem.getText();
                    doCheckUnBlockedTracksMenuItem(menuItemName);
                });
            }
        }
    }

    // action to be performed when checkUnConnectedTracksMenuItem is clicked
    private void doCheckUnConnectedTracksMenuItem(String menuItemName) {
        log.debug("docheckUnConnectedTracksMenuItem({})", menuItemName);
        layoutEditor.clearSelectionGroups();
        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(menuItemName);
        if (layoutTrack != null) {
            layoutEditor.amendSelectionGroup(layoutTrack);
        }
    }

    // action to be performed when checkUnBlockedTracksMenuItem is clicked
    private void doCheckUnBlockedTracksMenuItem(String menuItemName) {
        log.debug("doCheckUnBlockedTracksMenuItem({})", menuItemName);

        layoutEditor.clearSelectionGroups();
        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(menuItemName);
        if (layoutTrack != null) {
            layoutEditor.setSelectionRect(layoutTrack.getBounds());
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);

            layoutEditor.getLayoutTrackEditors().editLayoutTrack(layoutTrack);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorChecks.class);
}   // class LayoutEditorChecks
