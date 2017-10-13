package jmri.jmrit.display.layoutEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import jmri.util.swing.JMenuUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of tools to check various things on the layout editor panel.
 */
public class LayoutEditorChecks {

    private final LayoutEditor layoutEditor;
    private JMenu checkMenu = new JMenu(Bundle.getMessage("CheckMenuTitle"));
    private JMenuItem checkNoResultsMenuItem = new JMenuItem(Bundle.getMessage("CheckNoResultsMenuItemTitle"));

    // Check Un-Connected Tracks
    private JMenu checkUnConnectedTracksMenu = new JMenu(Bundle.getMessage("CheckUnConnectedTracksMenuTitle"));
    private JMenuItem checkUnConnectedTracksMenuItem = new JMenuItem(Bundle.getMessage("CheckMenuTitle"));

    // Check Un-Blocked Tracks
    private JMenu checkUnBlockedTracksMenu = new JMenu(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));
    private JMenuItem checkUnBlockedTracksMenuItem = new JMenuItem(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));

    // Check Non-Contiguous Blocks
    private JMenu checkNonContiguousBlocksMenu = new JMenu(Bundle.getMessage("CheckNonContiguousBlocksMenuTitle"));
    private JMenuItem checkNonContiguousBlocksMenuItem = new JMenuItem(Bundle.getMessage("CheckNonContiguousBlocksMenuTitle"));

    /**
     * The constructor for this class
     *
     * @param layoutEditor the layout editor that uses this class
     */
    public LayoutEditorChecks(LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;
    }

    /**
     * set the layout editor checks menu (in the tools menu)
     *
     * @param toolsMenu where to add our "Check" menu and sub-menus
     */
    //Note: I'm implemented these menus/sub-menu items three different ways...
    // (see the inline notes)
    protected void setupMenu(@Nonnull JMenu toolsMenu) {
        toolsMenu.add(checkMenu);
        checkMenu.setToolTipText(Bundle.getMessage("CheckMenuToolTip"));
        checkNoResultsMenuItem.setToolTipText(Bundle.getMessage("CheckNoResultsMenuItemToolTip"));

        //
        //  check for tracks with free connections
        //
        // Note: This menu has a sub menu item with "Check" in it. Selecting that
        // sub-menu will populate it with a "Re-Check" menu followed by
        // the tracks that fail the check.
        checkUnConnectedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnConnectedTracksMenuToolTip"));
        checkMenu.add(checkUnConnectedTracksMenu);

        checkUnConnectedTracksMenuItem.setToolTipText(Bundle.getMessage("CheckUnConnectedTracksMenuToolTip"));
        checkUnConnectedTracksMenu.add(checkUnConnectedTracksMenuItem);
        checkUnConnectedTracksMenuItem.addActionListener((ActionEvent event) -> {
            setupCheckUnConnectedTracksMenu();
        });

        //
        //  check for tracks without assigned blocks
        //
        // Note: This menu starts out with just a menu item. When it's selected
        // it's replaced with a menu with sub-menus items. The 1st sub-menu item
        // being the initial menu item that was replaced but now renamed to
        // "Re-Check". Any tracks that fail this check are added after it
        // (seperated by a menu separator).
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
        });

        //
        // check for non-contiguous blocks
        //
        // Note: This menu runs its checks as soon as the menu is moused over.
        // The sub-menus are populated at that time.
        checkNonContiguousBlocksMenu.setToolTipText(Bundle.getMessage("CheckNonContiguousBlocksMenuToolTip"));
        checkMenu.add(checkNonContiguousBlocksMenu);

        checkNonContiguousBlocksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                log.info("menuSelected");
                setupCheckNonContiguousBlocksMenu();
            }

            @Override
            public void menuDeselected(MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to do here... move along...
            }

            @Override
            public void menuCanceled(MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to do here... move along...
            }
        });

        checkNonContiguousBlocksMenuItem.setText(Bundle.getMessage("CheckMenuTitle"));
        checkNonContiguousBlocksMenuItem.setToolTipText(Bundle.getMessage("CheckNonContiguousBlocksMenuToolTip"));
        checkNonContiguousBlocksMenu.add(checkNonContiguousBlocksMenuItem);
    }

    //TODO: not used… dead-code strip
    //private void installOnAllSubComponents(JComponent component, AncestorListener ancestorListener) {
    //    component.addAncestorListener(ancestorListener);
    //    for (int idx = 0; idx < component.getComponentCount(); idx++) {
    //        Component subComponent = component.getComponent(idx);
    //        if (subComponent instanceof JComponent) {
    //            JComponent subJComponent = (JComponent) subComponent;
    //            installOnAllSubComponents(subJComponent, ancestorListener);
    //        }
    //    }
    //}
    //
    // run the un-connected tracks check and populate the checkUnConnectedTracksMenu
    //
    private void setupCheckUnConnectedTracksMenu() {
        log.debug("setupcheckUnConnectedTracksMenu");

        checkUnConnectedTracksMenu.removeAll();
        checkUnConnectedTracksMenuItem.setText(Bundle.getMessage("ReCheckMenuTitle"));
        checkUnConnectedTracksMenu.add(checkUnConnectedTracksMenuItem);

        boolean flag = false;
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            List<Integer> connections = layoutTrack.checkForFreeConnections();
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
        if (checkUnConnectedTracksMenu.getItemCount() > 2) {
            Component menuItem2 = checkUnConnectedTracksMenu.getItem(2);
            if (menuItem2 instanceof JMenuItem) {
                JMenuItem jmi = (JMenuItem) menuItem2;
                String menuItemName = jmi.getText();
                doCheckUnConnectedTracksMenuItem(menuItemName);
            }
        }
    }

    //
    // action to be performed when checkUnConnectedTracksMenuItem is clicked
    //
    private void doCheckUnConnectedTracksMenuItem(String menuItemName) {
        log.debug("docheckUnConnectedTracksMenuItem({})", menuItemName);
        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(menuItemName);
        if (layoutTrack != null) {
            layoutEditor.setSelectionRect(layoutTrack.getBounds());
            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);
        } else {
            layoutEditor.clearSelectionGroups();
        }
    }

    //
    // run the un-blocked tracks check and populate the checkUnBlockedTracksMenu
    //
    private void setupCheckUnBlockedTracksMenu() {
        log.debug("setupCheckUnBlockedTracksMenu");

        checkUnBlockedTracksMenu.removeAll();
        checkUnBlockedTracksMenu.add(checkUnBlockedTracksMenuItem);

        boolean flag = false;
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            if (!layoutTrack.checkForUnAssignedBlocks()) {
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
        if (checkUnBlockedTracksMenu.getItemCount() > 2) {
            Component menuItem2 = checkUnBlockedTracksMenu.getItem(2);
            if (menuItem2 instanceof JMenuItem) {
                JMenuItem jmi = (JMenuItem) menuItem2;
                String menuItemName = jmi.getText();
                doCheckUnBlockedTracksMenuItem(menuItemName);
            }
        }
    }

    //
    // action to be performed when checkUnBlockedTracksMenuItem is clicked
    //
    private void doCheckUnBlockedTracksMenuItem(String menuItemName) {
        log.debug("doCheckUnBlockedTracksMenuItem({})", menuItemName);

        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(menuItemName);
        if (layoutTrack != null) {
            layoutEditor.setSelectionRect(layoutTrack.getBounds());
            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);

            layoutEditor.getLayoutTrackEditors().editLayoutTrack(layoutTrack);
        } else {
            layoutEditor.clearSelectionGroups();
        }
    }

    //
    // run the non-contiguous blocks check and populate the checkNonContiguousBlocksMenu
    //
    private void setupCheckNonContiguousBlocksMenu() {
        log.debug("setupCheckNonContiguousBlocksMenu");

        checkNonContiguousBlocksMenu.removeAll();

        HashMap<String, Set<String>> blocksMap = new HashMap<>();
        Set<String> badBlocks = new LinkedHashSet<>();
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            layoutTrack.checkForNonContiguousBlocks(blocksMap, badBlocks);
        }
        if (badBlocks.size() > 0) {
            for (String blockName : badBlocks) {
                Set<String> trackSet = blocksMap.get(blockName);

                // add this block to the check non-contiguous blocks menu
                JMenuItem jmi = new JMenuItem(blockName + " (" + trackSet.size() + ")");
                checkNonContiguousBlocksMenu.add(jmi);
                if (true) {
                    // we don't have anything to do for non-contiguous blocks
                    // other than to report them (by adding them to the menu).
                    jmi.setEnabled(false);
                } else {
                    // but when I think of what to do this code will do it!
                    jmi.addActionListener((ActionEvent event) -> {
                        JMenuItem menuItem = (JMenuItem) event.getSource();
                        String menuItemName = menuItem.getText();
                        doCheckNonContiguousBlocksMenuItem(menuItemName);
                    });

                }
            }
            //// do the 1st one...
            //doCheckNonContiguousBlocksMenuItem(badBlocks.iterator().next());
        } else {
            checkNonContiguousBlocksMenu.add(checkNoResultsMenuItem);
        }
    }

    // action to be performed when checkNonContiguousBlocksMenuItem is clicked
    private void doCheckNonContiguousBlocksMenuItem(String menuItemName) {
        log.debug("doCheckNonContiguousBlocksMenuItem({})", menuItemName);

        //LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(menuItemName);
        //if (layoutTrack != null) {
        //    layoutEditor.setSelectionRect(layoutTrack.getBounds());
        //    //setSelectionRect calls createSelectionGroups...
        //    //so we have to clear it before amending to it
        //    layoutEditor.clearSelectionGroups();
        //    layoutEditor.amendSelectionGroup(layoutTrack);
        //
        //    layoutEditor.getLayoutTrackEditors().editLayoutTrack(layoutTrack);
        //} else {
        //    layoutEditor.clearSelectionGroups();
        //}
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorChecks.class
    );
}   // class LayoutEditorChecks
