package jmri.jmrit.display.layoutEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import jmri.util.MathUtil;
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

    // Check Un-Connected Tracks
    private JMenu checkUnConnectedTracksMenu = new JMenu(Bundle.getMessage("CheckUnConnectedTracksMenuTitle"));

    // Check Un-Blocked Tracks
    private JMenu checkUnBlockedTracksMenu = new JMenu(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));

    // Check Non-Contiguous Blocks
    private JMenu checkNonContiguousBlocksMenu = new JMenu(Bundle.getMessage("CheckNonContiguousBlocksMenuTitle"));

    /**
     * The constructor for this class
     *
     * @param layoutEditor the layout editor that uses this class
     */
    public LayoutEditorChecks(@Nonnull LayoutEditor layoutEditor) {
        this.layoutEditor = layoutEditor;
    }

    /**
     * set the layout editor checks menu (in the tools menu)
     *
     * @param toolsMenu where to add our "Check" menu and sub-menus
     */
    protected void setupChecksMenu(@Nonnull JMenu toolsMenu) {
        toolsMenu.add(checkMenu);
        checkMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                boolean enabled = layoutEditor.isEditable();
                checkUnConnectedTracksMenu.setEnabled(enabled);
                checkUnBlockedTracksMenu.setEnabled(enabled);
                checkNonContiguousBlocksMenu.setEnabled(enabled);
            }

            @Override

            public void menuDeselected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(@Nonnull MenuEvent menuEvent) {
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
        checkUnConnectedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnConnectedTracksMenuToolTip"));
        checkUnConnectedTracksMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkUnConnectedTracksMenu);

        checkUnConnectedTracksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckUnConnectedTracksMenu();
            }

            @Override
            public void menuDeselected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(@Nonnull MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        });

        //
        //  check for tracks without assigned blocks
        //
        checkUnBlockedTracksMenu.setToolTipText(Bundle.getMessage("CheckUnBlockedTracksMenuToolTip"));
        checkUnBlockedTracksMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkUnBlockedTracksMenu);

        checkUnBlockedTracksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckUnBlockedTracksMenu();
            }

            @Override
            public void menuDeselected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(@Nonnull MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        });

        //
        // check for non-contiguous blocks
        //
        checkNonContiguousBlocksMenu.setToolTipText(Bundle.getMessage("CheckNonContiguousBlocksMenuToolTip"));
        checkNonContiguousBlocksMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkNonContiguousBlocksMenu);

        checkNonContiguousBlocksMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckNonContiguousBlocksMenu();
            }

            @Override
            public void menuDeselected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuDeselected");
                //nothing to see here... move along...
            }

            @Override
            public void menuCanceled(@Nonnull MenuEvent menuEvent) {
                log.debug("menuCanceled");
                //nothing to see here... move along...
            }
        });
    }

    //
    // run the un-connected tracks check and populate the checkUnConnectedTracksMenu
    //
    private void setupCheckUnConnectedTracksMenu() {
        log.debug("setupcheckUnConnectedTracksMenu");

        // collect the names of all menu items with checkmarks
        Set<String> checkMarkedMenuItemNamesSet = getCheckMarkedMenuItemNames(checkUnConnectedTracksMenu);

        // mark our menu as "in progress..."
        checkUnConnectedTracksMenu.removeAll();
        checkUnConnectedTracksMenu.add(checkInProgressMenuItem);

        // check all tracks for free connections
        List<String> trackNames = new ArrayList<>();
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            //if (layoutTrack.getName().equals("EC5")) {
            //    log.debug("Stop here!");
            //}
            List<Integer> connections = layoutTrack.checkForFreeConnections();
            if (!connections.isEmpty()) {
                // add this track's name to the list of track names
                trackNames.add(layoutTrack.getName());
            }
        }

        // clear the "in progress..." menu item
        checkUnConnectedTracksMenu.removeAll();

        // for each un-connected track we found...
        if (trackNames.size() > 0) {
            for (String trackName : trackNames) {
                // create a menu item for it
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(trackName);
                checkUnConnectedTracksMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> {
//                    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
//                    String menuItemName = menuItem.getText();
                    doCheckUnConnectedTracksMenuItem(trackName);
                });

                // if it's in the check marked set then (re-)checkmark it
                for (String item : checkMarkedMenuItemNamesSet) {
                    if (item.equals(trackName)) {
                        jmi.setSelected(true);
                        break;
                    }
                }
            }
        } else {
            checkUnConnectedTracksMenu.add(checkNoResultsMenuItem);
        }
    }   // setupCheckUnConnectedTracksMenu

    //
    // action to be performed when checkUnConnectedTracksMenu item is clicked
    //
    private void doCheckUnConnectedTracksMenuItem(@Nonnull String menuItemName) {
        log.debug("docheckUnConnectedTracksMenuItem({})", menuItemName);
        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(menuItemName);
        if (layoutTrack != null) {
            Rectangle2D trackBounds = layoutTrack.getBounds();
            layoutEditor.setSelectionRect(trackBounds);

            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);
        } else {
            layoutEditor.clearSelectionGroups();
        }
    }   // doCheckUnConnectedTracksMenuItem

    //
    // run the un-blocked tracks check and populate the checkUnBlockedTracksMenu
    //
    private void setupCheckUnBlockedTracksMenu() {
        log.debug("setupCheckUnBlockedTracksMenu");

        // collect the names of all menu items with checkmarks
        Set<String> checkMarkedMenuItemNamesSet = getCheckMarkedMenuItemNames(checkUnBlockedTracksMenu);

        // mark our menu as "in progress..."
        checkUnBlockedTracksMenu.removeAll();
        checkUnBlockedTracksMenu.add(checkInProgressMenuItem);

        // check all tracks for un-assigned blocks
        List<String> trackNames = new ArrayList<>();
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
            if (!layoutTrack.checkForUnAssignedBlocks()) {
                // add this track to the list of un-assigned track names
                trackNames.add(layoutTrack.getName());
            }
        }

        // clear the "in progress..." menu item
        checkUnBlockedTracksMenu.removeAll();

        // for each tracks with un-assigned blocks that we found...
        if (trackNames.size() > 0) {
            for (String trackName : trackNames) {
                // create a menu item for it
                JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(trackName);
                checkUnBlockedTracksMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> {
//                    JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem) event.getSource();
//                    String menuItemName = menuItem.getText();
                    doCheckUnBlockedTracksMenuItem(trackName);
                });

                // if it's in the check marked set then (re-)checkmark it
                for (String item : checkMarkedMenuItemNamesSet) {
                    if (item.equals(trackName)) {
                        jmi.setSelected(true);
                        break;
                    }
                }
            }
        } else {
            checkUnBlockedTracksMenu.add(checkNoResultsMenuItem);
        }
    }   // setupCheckUnBlockedTracksMenu

    //
    // action to be performed when checkUnBlockedTracksMenuItem is clicked
    //
    private void doCheckUnBlockedTracksMenuItem(@Nonnull String menuItemName) {
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
    }   // doCheckUnBlockedTracksMenuItem

    //
    // run the non-contiguous blocks check and populate the checkNonContiguousBlocksMenu
    //
    private void setupCheckNonContiguousBlocksMenu() {
        log.debug("setupCheckNonContiguousBlocksMenu");

        // mark our menu as "in progress..."
        checkNonContiguousBlocksMenu.removeAll();
        checkNonContiguousBlocksMenu.add(checkInProgressMenuItem);

        // collect all contiguous blocks
        HashMap<String, List<Set<String>>> blockNamesToTrackNameSetMaps = new HashMap<>();
        for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
        //if (layoutTrack.getName().equals("TO1")
        //        || layoutTrack.getName().equals("TO2")
        //        || layoutTrack.getName().equals("TO3")) {
        //    log.info("•Stop here!");
        //}
            layoutTrack.checkForNonContiguousBlocks(blockNamesToTrackNameSetMaps);
        }

        // clear the "in progress..." menu item
        checkNonContiguousBlocksMenu.removeAll();

        // for each bad block we found...
        for (Map.Entry<String, List<Set<String>>> entry : blockNamesToTrackNameSetMaps.entrySet()) {
            String blockName = entry.getKey();
            List<Set<String>> trackNameSets = entry.getValue();
            if (trackNameSets.size() > 1) {
                JMenu jmi = new JMenu(blockName);
                checkNonContiguousBlocksMenu.add(jmi);

                int idx = 1;
                for (Set<String> trackNameSet : trackNameSets) {
                    JMenuItem subMenuItem = new JMenuItem(
                            Bundle.getMessage("MakeLabel", blockName) + "#" + (idx++));
                    jmi.add(subMenuItem);
                    subMenuItem.addActionListener((ActionEvent event) -> {
                        doCheckNonContiguousBlocksMenuItem(blockName, trackNameSet);
                    });
                }
            }
        }
        // if we didn't find any...
        if (checkNonContiguousBlocksMenu.getMenuComponentCount() == 0) {
            checkNonContiguousBlocksMenu.add(checkNoResultsMenuItem);
        }
    }   // setupCheckNonContiguousBlocksMenu

// action to be performed when checkNonContiguousBlocksMenu item is clicked
    private void doCheckNonContiguousBlocksMenuItem(
            @Nonnull String blockName,
            @Nullable Set<String> trackNameSet) {
        log.debug("doCheckNonContiguousBlocksMenuItem({})", blockName);

        if (trackNameSet != null) {
            // collect all the bounds...
            Rectangle2D bounds = null;
            for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
                if (trackNameSet.contains(layoutTrack.getName())) {
                    Rectangle2D trackBounds = layoutTrack.getBounds();
                    if (bounds == null) {
                        bounds = trackBounds.getBounds2D();
                    } else {
                        bounds.add(trackBounds);
                    }
                }
            }
            layoutEditor.setSelectionRect(bounds);

            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();

            // amend all tracks in this block to the layout editor selection group
            for (LayoutTrack layoutTrack : layoutEditor.getLayoutTracks()) {
                if (trackNameSet.contains(layoutTrack.getName())) {
                    layoutEditor.amendSelectionGroup(layoutTrack);
                }
            }
        } else {
            layoutEditor.setSelectionRect(MathUtil.zeroRectangle2D);
        }
    }   // doCheckNonContiguousBlocksMenuItem

    // collect the names of all checkbox menu items with checkmarks
    private Set<String> getCheckMarkedMenuItemNames(@Nonnull JMenu menu) {
        Set<String> results = new HashSet<>();
        for (int idx = 0; idx < menu.getMenuComponentCount(); idx++) {
            Component menuComponent = menu.getMenuComponent(idx);
            if (menuComponent instanceof JCheckBoxMenuItem) {
                JCheckBoxMenuItem checkBoxMenuItem = (JCheckBoxMenuItem) menuComponent;
                if (checkBoxMenuItem.isSelected()) {
                    results.add(checkBoxMenuItem.getText());
                }
            }
        }
        return results;
    }   // getCheckMarkedMenuItemNames

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorChecks.class
    );
}   // class LayoutEditorChecks
