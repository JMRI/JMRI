package jmri.jmrit.display.layoutEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.swing.JCheckBoxMenuItem;
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
    private JMenuItem checkInProgressMenuItem = new JMenuItem(Bundle.getMessage("CheckInProgressMenuItemTitle"));
    private JMenuItem checkNoResultsMenuItem = new JMenuItem(Bundle.getMessage("CheckNoResultsMenuItemTitle"));
//    private ImageIcon checkmarkImageIcon = new ImageIcon(FileUtil.findURL("resources/icons/misc/Checkmark-green.gif", FileUtil.Location.INSTALLED));

    // Check Un-Connected Tracks
    private JMenu checkUnConnectedTracksMenu = new JMenu(Bundle.getMessage("CheckUnConnectedTracksMenuTitle"));
    private JMenuItem checkUnConnectedTracksMenuItem = new JMenuItem(Bundle.getMessage("CheckMenuTitle"));

    // Check Un-Blocked Tracks
    private JMenu checkUnBlockedTracksMenu = new JMenu(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));
    private JMenuItem checkUnBlockedTracksMenuItem = new JMenuItem(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));

    // Check Non-Contiguous Blocks
    private JMenu checkNonContiguousBlocksMenu = new JMenu(Bundle.getMessage("CheckNonContiguousBlocksMenuTitle"));
//    private JMenuItem checkNonContiguousBlocksMenuItem = new JMenuItem(Bundle.getMessage("CheckNonContiguousBlocksMenuTitle"));

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
    protected void setupChecksMenu(@Nonnull JMenu toolsMenu) {
        toolsMenu.add(checkMenu);
        checkMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                log.debug("menuSelected");
                boolean enabled = layoutEditor.isEditable();
                checkUnConnectedTracksMenu.setEnabled(enabled);
                checkUnConnectedTracksMenuItem.setEnabled(enabled);
                checkUnBlockedTracksMenu.setEnabled(enabled);
                checkUnBlockedTracksMenuItem.setEnabled(enabled);
                checkNonContiguousBlocksMenu.setEnabled(enabled);
//                checkNonContiguousBlocksMenuItem.setEnabled(enabled);
            }

            @Override

            public void menuDeselected(MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        }
        );
        checkMenu.setEnabled(layoutEditor.isEditable());
        checkMenu.setToolTipText(Bundle.getMessage("CheckMenuToolTip"));
        checkNoResultsMenuItem.setToolTipText(Bundle.getMessage("CheckNoResultsMenuItemToolTip"));
        checkNoResultsMenuItem.setEnabled(false);
        checkInProgressMenuItem.setToolTipText(Bundle.getMessage("CheckInProgressMenuItemToolTip"));
        checkInProgressMenuItem.setEnabled(false);
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
        checkNonContiguousBlocksMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkNonContiguousBlocksMenu);

        checkNonContiguousBlocksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckNonContiguousBlocksMenu();
            }

            @Override
            public void menuDeselected(MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        });

//        checkNonContiguousBlocksMenuItem.setText(Bundle.getMessage("CheckMenuTitle"));
//        checkNonContiguousBlocksMenuItem.setToolTipText(Bundle.getMessage("CheckNonContiguousBlocksMenuToolTip"));
//        checkNonContiguousBlocksMenu.add(checkNonContiguousBlocksMenuItem);
    }

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
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(layoutTrack.getName());
                checkUnConnectedTracksMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> {
                    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
                    // toggle checkmark state
                    menuItem.setSelected(!menuItem.isSelected());
                    String menuItemName = menuItem.getText();
                    doCheckUnConnectedTracksMenuItem(menuItemName);
                });
            }
        }
        if (checkUnConnectedTracksMenu.getItemCount() > 2) {
            Component menuItem2 = checkUnConnectedTracksMenu.getItem(2);
            JMenuItem jmi = (JMenuItem) menuItem2;
            String menuItemName = jmi.getText();
            doCheckUnConnectedTracksMenuItem(menuItemName);
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
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(layoutTrack.getName());
                checkUnBlockedTracksMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> {
                    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
                    // toggle checkmark state
                    menuItem.setSelected(!menuItem.isSelected());
                    String menuItemName = menuItem.getText();
                    doCheckUnBlockedTracksMenuItem(menuItemName);
                });
            }
        }
        if (checkUnBlockedTracksMenu.getItemCount() > 2) {
            Component menuItem2 = checkUnBlockedTracksMenu.getItem(2);
            JMenuItem jmi = (JMenuItem) menuItem2;
            String menuItemName = jmi.getText();
            doCheckUnBlockedTracksMenuItem(menuItemName);
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

        // collect the names of all checkbox menu items with checkmarks
        Set<String> checkMarkedSet = new HashSet<>();
        for (int idx = 0; idx < checkNonContiguousBlocksMenu.getMenuComponentCount(); idx++) {
            Component menuComponent = checkNonContiguousBlocksMenu.getMenuComponent(idx);
            if (menuComponent instanceof JCheckBoxMenuItem) {
                JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) menuComponent;
                //if (checkBoxMenuItem.getIcon() != null) 
                if (checkBoxMenuItem.isSelected()) {
                    checkMarkedSet.add(checkBoxMenuItem.getText());
                }
            }
        }

        checkNonContiguousBlocksMenu.removeAll();
        checkNonContiguousBlocksMenu.add(checkInProgressMenuItem);

        HashMap<String, Set<String>> blocksMap = new HashMap<>();
        Set<String> badBlocks = new TreeSet<>();
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {

            if (layoutTrack.getName().equals("EC5")
                    || layoutTrack.getName().equals("T20")
                    || layoutTrack.getName().equals("TO9")) {
                log.debug("Stop here!");
            }
            layoutTrack.checkForNonContiguousBlocks(blocksMap, badBlocks);
        }
        checkNonContiguousBlocksMenu.removeAll();
        if (badBlocks.size() > 0) {
            for (String blockName : badBlocks) {
                Set<String> trackSet = blocksMap.get(blockName);

                // add this block to the check non-contiguous blocks menu
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(blockName + " (" + trackSet.size() + ")");
                checkNonContiguousBlocksMenu.add(jmi);
                // we currently don't have anything to do for non-contiguous 
                // blocks other than to just add them to the menu.
                //jmi.setEnabled(false); // don't disable (so checkmarks still work)
                jmi.addActionListener((ActionEvent event) -> {
                    log.info("event: {}", event.paramString());
                    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
                    // toggle checkmark state
                    //menuItem.setSelected(!menuItem.isSelected());
                    //menuItem.setSelected(true);
                    // setState/Selected() doesn't work… try this:
                    //if (menuItem.getIcon() == null) {
                    //    menuItem.setIcon(checkmarkImageIcon);
                    //} else {
                    //    menuItem.setIcon(null);
                    //}

                    String menuItemName = menuItem.getText();
                    doCheckNonContiguousBlocksMenuItem(menuItemName);
                });
                // if it's in the check marked set then checkmark it
                for (String item : checkMarkedSet) {
                    if (item.startsWith(blockName + " (")) {
                        jmi.setSelected(true);
                        //jmi.setIcon(checkmarkImageIcon);
                        break;
                    }
                }
            }
            //// do the 1st one...
            //doCheckNonContiguousBlocksMenuItem(badBlocks.iterator().next());
        } else {
            checkNonContiguousBlocksMenu.add(checkNoResultsMenuItem);
        }
    }
    // action to be performed when checkNonContiguousBlocksMenu item is clicked

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
