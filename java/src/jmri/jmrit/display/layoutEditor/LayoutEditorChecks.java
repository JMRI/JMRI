package jmri.jmrit.display.layoutEditor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import javax.annotation.*;
import javax.swing.*;
import javax.swing.event.*;
import jmri.util.MathUtil;
import org.slf4j.*;

/**
 * A collection of tools to check various things on the layout editor panel.
 *
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutEditorChecks {

    private final LayoutEditor layoutEditor;
    private final JMenu checkMenu = new JMenu(Bundle.getMessage("CheckMenuTitle"));
    private final JMenuItem checkInProgressMenuItem = new JMenuItem(Bundle.getMessage("CheckInProgressMenuItemTitle"));
    private final JMenuItem checkNoResultsMenuItem = new JMenuItem(Bundle.getMessage("CheckNoResultsMenuItemTitle"));

    // Check for Un-Connected Tracks
    private final JMenu checkUnConnectedTracksMenu = new JMenu(Bundle.getMessage("CheckUnConnectedTracksMenuTitle"));

    // Check for Un-Blocked Tracks
    private final JMenu checkUnBlockedTracksMenu = new JMenu(Bundle.getMessage("CheckUnBlockedTracksMenuTitle"));

    // Check for Non-Contiguous Blocks
    private final JMenu checkNonContiguousBlocksMenu = new JMenu(Bundle.getMessage("CheckNonContiguousBlocksMenuTitle"));

    // Check for Unnecessary Anchors
    private final JMenu checkUnnecessaryAnchorsMenu = new JMenu(Bundle.getMessage("CheckUnnecessaryAnchorsMenuTitle"));

    // Check for Linear Bezier Track Segments
    private final JMenu checkLinearBezierTrackSegmentsMenu = new JMenu(Bundle.getMessage("CheckLinearBezierTrackSegmentsMenuTitle"));

    // Check for Fixed Radius Bezier Track Segments
    private final JMenu checkFixedRadiusBezierTrackSegmentsMenu = new JMenu(Bundle.getMessage("CheckFixedRadiusBezierTrackSegmentsMenuTitle"));

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
                checkUnnecessaryAnchorsMenu.setEnabled(enabled);
                checkLinearBezierTrackSegmentsMenu.setEnabled(enabled);
                checkFixedRadiusBezierTrackSegmentsMenu.setEnabled(enabled);
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

        //
        // Check for Unnecessary Anchors
        //
        checkUnnecessaryAnchorsMenu.setToolTipText(Bundle.getMessage("CheckUnnecessaryAnchorsMenuToolTip"));
        checkUnnecessaryAnchorsMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkUnnecessaryAnchorsMenu);

        checkUnnecessaryAnchorsMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckUnnecessaryAnchorsMenu();
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
        // Check for linear bezier track segments
        //
        checkLinearBezierTrackSegmentsMenu.setToolTipText(Bundle.getMessage("CheckLinearBezierTrackSegmentsMenuToolTip"));
        checkLinearBezierTrackSegmentsMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkLinearBezierTrackSegmentsMenu);

        checkLinearBezierTrackSegmentsMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckLinearBezierTrackSegmentsMenu();
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
        // Check for fixed radius bezier track segments (circle arcs)
        //
        checkFixedRadiusBezierTrackSegmentsMenu.setToolTipText(Bundle.getMessage("CheckFixedRadiusBezierTrackSegmentsMenuToolTip"));
        checkFixedRadiusBezierTrackSegmentsMenu.add(checkInProgressMenuItem);
        checkMenu.add(checkFixedRadiusBezierTrackSegmentsMenu);

        checkFixedRadiusBezierTrackSegmentsMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(@Nonnull MenuEvent menuEvent) {
                log.debug("menuSelected");
                setupCheckFixedRadiusBezierTrackSegmentsMenu();
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
            List<LayoutEditor.HitPointType> connections = layoutTrack.checkForFreeConnections();
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
                jmi.addActionListener((ActionEvent event) -> doCheckUnConnectedTracksMenuItem(trackName));

                // if it's in the check marked set then (re-)checkmark it
                if (checkMarkedMenuItemNamesSet.contains(trackName)) {
                    jmi.setSelected(true);
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
                jmi.addActionListener((ActionEvent event) -> doCheckUnBlockedTracksMenuItem(trackName));

                // if it's in the check marked set then (re-)checkmark it
                if (checkMarkedMenuItemNamesSet.contains(trackName)) {
                    jmi.setSelected(true);
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
                    subMenuItem.addActionListener((ActionEvent event) -> doCheckNonContiguousBlocksMenuItem(blockName, trackNameSet));
                }
            }
        }
        // if we didn't find any...
        if (checkNonContiguousBlocksMenu.getMenuComponentCount() == 0) {
            checkNonContiguousBlocksMenu.add(checkNoResultsMenuItem);
        }
    }   // setupCheckNonContiguousBlocksMenu

    //
// action to be performed when checkNonContiguousBlocksMenu item is clicked
    //
    private void doCheckNonContiguousBlocksMenuItem(
            @Nonnull String blockName,
            @CheckForNull Set<String> trackNameSet) {
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

    //
    // run the Unnecessary Anchors check and
    // populate the CheckUnnecessaryAnchorsMenu
    //
    private void setupCheckUnnecessaryAnchorsMenu() {
        log.debug("setupCheckUnnecessaryAnchorsMenu");

        // collect the names of all menu items with checkmarks
        Set<String> checkMarkedMenuItemNamesSet = getCheckMarkedMenuItemNames(checkUnBlockedTracksMenu);
        // mark our menu as "in progress..."
        checkUnnecessaryAnchorsMenu.removeAll();
        checkUnnecessaryAnchorsMenu.add(checkInProgressMenuItem);

        // check all PositionablePoints
        List<PositionablePoint> aatzlts = new ArrayList<>();
        for (PositionablePoint pp : layoutEditor.getPositionablePoints()) {
            // has to be an anchor...
            if (pp.getType() == PositionablePoint.ANCHOR) {
                // adjacent track segments must be defined...
                TrackSegment ts1 = pp.getConnect1();
                TrackSegment ts2 = pp.getConnect2();
                if ((ts1 != null) && (ts2 != null)) {
                    // can't be an arc, circle or bezier...
                    if (!ts1.isArc() && !ts1.isCircle() && !ts1.isBezier()
                            && !ts2.isArc() && !ts2.isCircle() && !ts2.isBezier()) {
                        // must be in same block...
                        String blockName1 = ts1.getBlockName();
                        String blockName2 = ts2.getBlockName();
                        if (blockName1.equals(blockName2)) {
                            // if length of ts1 is zero...
                            Rectangle2D bounds1 = ts1.getBounds();
                            double length1 = Math.hypot(bounds1.getWidth(), bounds1.getHeight());
                            if (length1 < 1.0) {
                                aatzlts.add(pp);
                                continue;   // so we don't get added again
                            }
                            // if length of ts2 is zero...
                            Rectangle2D bounds = ts2.getBounds();
                            double length = Math.hypot(bounds.getWidth(), bounds.getHeight());
                            if (length < 1.0) {
                                aatzlts.add(pp);
                                continue;   // so we don't get added again
                            }
                            // if track segments isMainline's don't match
                            if (ts1.isMainline() != ts2.isMainline()) {
                                continue;   // skip it
                            }
                            // if track segments isHidden's don't match
                            if (ts1.isHidden() != ts2.isHidden()) {
                                continue;   // skip it
                            }
                            // if either track segment has decorations
                            if (ts1.hasDecorations() || ts2.hasDecorations()) {
                                continue;   // skip it
                            }
                            // if adjacent tracks are collinear...
                            double dir1 = ts1.getDirectionRAD();
                            double dir2 = ts2.getDirectionRAD();
                            double diffRAD = MathUtil.absDiffAngleRAD(dir1, dir2);
                            if (MathUtil.equals(diffRAD, 0.0)
                                    || MathUtil.equals(diffRAD, Math.PI)) {
                                aatzlts.add(pp);
                                // so we don't get added again
                            }
                        }   // if blocknames are equal
                    }   // isn't arc, circle or bezier
                }   // isn't null
            }   // is anchor
        }   // for pp

        // clear the "in progress..." menu item
        checkUnnecessaryAnchorsMenu.removeAll();

        // for each anchor we found
        for (PositionablePoint pp : aatzlts) {
            String anchorName = pp.getName();
            JMenuItem jmi = new JMenuItem(anchorName);
            checkUnnecessaryAnchorsMenu.add(jmi);
            jmi.addActionListener((ActionEvent event) -> doCheckUnnecessaryAnchorsMenuItem(anchorName));

            // if it's in the check marked set then (re-)checkmark it
            if (checkMarkedMenuItemNamesSet.contains(anchorName)) {
                jmi.setSelected(true);
            }
        }
        // if we didn't find any...
        if (checkUnnecessaryAnchorsMenu.getMenuComponentCount() == 0) {
            checkUnnecessaryAnchorsMenu.add(checkNoResultsMenuItem);
        }
    }   // setupCheckUnnecessaryAnchorsMenu

    //
    // action to be performed when CheckUnnecessaryAnchorsMenu item is clicked
    //
    private void doCheckUnnecessaryAnchorsMenuItem(
            @Nonnull String anchorName) {
        log.debug("doCheckUnnecessaryAnchorsMenuItem({})", anchorName);

        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(anchorName);
        if (layoutTrack != null) {
            layoutEditor.setSelectionRect(layoutTrack.getBounds());
            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);
            // show its popup menu
            layoutTrack.showPopup();
        } else {
            layoutEditor.clearSelectionGroups();
        }
    }   // doCheckUnnecessaryAnchorsMenuItem

    //
    // run the linear bezier track segments check and
    // populate the checkLinearBezierTrackSegmentsMenu
    //
    private void setupCheckLinearBezierTrackSegmentsMenu() {
        log.debug("setupCheckLinearBezierTrackSegmentsMenu");

        // collect the names of all menu items with checkmarks
        Set<String> checkMarkedMenuItemNamesSet = getCheckMarkedMenuItemNames(checkLinearBezierTrackSegmentsMenu);
        // mark our menu as "in progress..."
        checkLinearBezierTrackSegmentsMenu.removeAll();
        checkLinearBezierTrackSegmentsMenu.add(checkInProgressMenuItem);

        // check all TrackSegments
        List<TrackSegment> linearBezierTrackSegments = new ArrayList<>();
        for (TrackSegment ts : layoutEditor.getTrackSegments()) {
            // has to be a bezier
            if (ts.isBezier()) {
//                if (ts.getName().equals("T104")) {
//                    log.debug("T104");
//                }
// adjacent connections must be defined...
                LayoutTrack c1 = ts.getConnect1();
                LayoutTrack c2 = ts.getConnect2();
                if ((c1 != null) && (c2 != null)) {
                    // if length is zero...
                    Point2D end1 = LayoutEditor.getCoords(ts.getConnect1(), ts.getType1());
                    Point2D end2 = LayoutEditor.getCoords(ts.getConnect2(), ts.getType2());
                    if (MathUtil.distance(end1, end2) <= 4.0) {
                        linearBezierTrackSegments.add(ts);
                        continue;   // so we don't get added again
                    }
                    // if control points are collinear...
                    boolean good = true; //assume success (optimist!)
                    for (Point2D cp : ts.getBezierControlPoints()) {
                        if (Math.abs(MathUtil.distance(end1, end2, cp)) > 1.0) {
                            good = false;
                            break;
                        }
                    }
                    if (good) {
                        linearBezierTrackSegments.add(ts);
//                        ts.setBezier(false);
                    }
                }   // c1 & c2 aren't null
            }   // is bezier
        }   // for ts

        // clear the "in progress..." menu item
        checkLinearBezierTrackSegmentsMenu.removeAll();
        // if we didn't find any...
        if (linearBezierTrackSegments.size() == 0) {
            checkLinearBezierTrackSegmentsMenu.add(checkNoResultsMenuItem);
        } else {
            // for each linear bezier track segment we found
            for (TrackSegment ts : linearBezierTrackSegments) {
                String name = ts.getName();
                JMenuItem jmi = new JMenuItem(name);
                checkLinearBezierTrackSegmentsMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> doCheckLinearBezierTrackSegmentsMenuItem(name));

                // if it's in the check marked set then (re-)checkmark it
                if (checkMarkedMenuItemNamesSet.contains(name)) {
                    jmi.setSelected(true);
                }
                //ts.setBezier(false);
            }
        }   //count == 0
    }   // setupCheckLinearBezierTrackSegmentsMenu

    //
    // action to be performed when checkLinearBezierTrackSegmentsMenu item is clicked
    //
    private void doCheckLinearBezierTrackSegmentsMenuItem(
            @Nonnull String trackSegmentName) {
        log.debug("doCheckLinearBezierTrackSegmentsMenuItem({})", trackSegmentName);

        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(trackSegmentName);
        if (layoutTrack != null) {
            layoutEditor.setSelectionRect(layoutTrack.getBounds());
            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);
            // show its popup menu
            layoutTrack.showPopup();
        } else {
            layoutEditor.clearSelectionGroups();
        }
    }   // doCheckLinearBezierTrackSegmentsMenuItem

    //
    // run the linear bezier track segments check and
    // populate the checkFixedRadiusBezierTrackSegmentsMenu
    //
    private void setupCheckFixedRadiusBezierTrackSegmentsMenu() {
        log.debug("setupCheckFixedRadiusBezierTrackSegmentsMenu");

        // collect the names of all menu items with checkmarks
        Set<String> checkMarkedMenuItemNamesSet = getCheckMarkedMenuItemNames(checkFixedRadiusBezierTrackSegmentsMenu);
        // mark our menu as "in progress..."
        checkFixedRadiusBezierTrackSegmentsMenu.removeAll();
        checkFixedRadiusBezierTrackSegmentsMenu.add(checkInProgressMenuItem);

        // check all TrackSegments
        List<TrackSegment> linearBezierTrackSegments = new ArrayList<>();
        for (TrackSegment ts : layoutEditor.getTrackSegments()) {
            // has to be a bezier
            if (ts.isBezier()) {
                // adjacent connections must be defined...
                LayoutTrack c1 = ts.getConnect1();
                LayoutTrack c2 = ts.getConnect2();
                if ((c1 != null) && (c2 != null)) {
                    Point2D end1 = LayoutEditor.getCoords(c1, ts.getType1());
                    Point2D end2 = LayoutEditor.getCoords(c2, ts.getType2());
                    double chordLength = MathUtil.distance(end1, end2);
                    if (chordLength <= 4.0) {
                        continue;   //skip short segments
                    }

                    //get first and last control points
                    int cnt = ts.getNumberOfBezierControlPoints();
                    if (cnt > 0) {
                        Point2D cp0 = ts.getBezierControlPoint(0);
                        Point2D cpN = ts.getBezierControlPoint(cnt - 1);
                        //calculate orthoginal points
                        Point2D op1 = MathUtil.add(end1, MathUtil.orthogonal(MathUtil.subtract(cp0, end1)));
                        Point2D op2 = MathUtil.subtract(end2, MathUtil.orthogonal(MathUtil.subtract(cpN, end2)));
                        //use them to find center point
                        Point2D ip = MathUtil.intersect(end1, op1, end2, op2);
                        if (ip != null) {   //single intersection point found
                            double r1 = MathUtil.distance(ip, end1);
                            double r2 = MathUtil.distance(ip, end2);
                            if (Math.abs(r1 - r2) <= 1.0) {
                                // the sign of the distance tells what side of the line the center point is on
                                double ipSide = Math.signum(MathUtil.distance(end1, end2, ip));

                                // if all control midpoints are equal distance from intersection point
                                boolean good = true; //assume success (optimist!)

                                for (int idx = 0; idx < cnt - 1; idx++) {
                                    Point2D cp1 = ts.getBezierControlPoint(idx);
                                    Point2D cp2 = ts.getBezierControlPoint(idx + 1);
                                    Point2D mp = MathUtil.midPoint(cp1, cp2);
                                    double rM = MathUtil.distance(ip, mp);
                                    if (Math.abs(r1 - rM) > 1.0) {
                                        good = false;
                                        break;
                                    }
                                    // the sign of the distance tells what side of line the midpoint is on
                                    double cpSide = Math.signum(MathUtil.distance(end1, end2, mp));
                                    if (MathUtil.equals(ipSide, cpSide)) {
                                        //can't be on same side as center point (if so then not circular)
                                        good = false;
                                        break;
                                    }
                                }
                                if (good) {
                                    linearBezierTrackSegments.add(ts);
                                    ts.setCircle(true);
                                }
                            } else {
                                log.error("checkFixedRadiusBezierTrackSegments(): unequal radius");
                            }
                        }
                    }
                }   // c1 & c2 aren't null
            }   // is bezier
        }   // for ts

        // clear the "in progress..." menu item
        checkFixedRadiusBezierTrackSegmentsMenu.removeAll();
        // if we didn't find any...
        if (checkFixedRadiusBezierTrackSegmentsMenu.getMenuComponentCount() == 0) {
            checkFixedRadiusBezierTrackSegmentsMenu.add(checkNoResultsMenuItem);
        } else {
            // for each linear bezier track segment we found
            for (TrackSegment ts : linearBezierTrackSegments) {
                String name = ts.getName();
                JMenuItem jmi = new JMenuItem(name);
                checkFixedRadiusBezierTrackSegmentsMenu.add(jmi);
                jmi.addActionListener((ActionEvent event) -> doCheckFixedRadiusBezierTrackSegmentsMenuItem(name));

                // if it's in the check marked set then (re-)checkmark it
                if (checkMarkedMenuItemNamesSet.contains(name)) {
                    jmi.setSelected(true);
                }
                ///ts.setBezier(false);
            }
        }   //count == 0
    }   // setupCheckFixedRadiusBezierTrackSegmentsMenu

    //
    // action to be performed when checkFixedRadiusBezierTrackSegmentsMenu item is clicked
    //
    private void doCheckFixedRadiusBezierTrackSegmentsMenuItem(
            @Nonnull String trackSegmentName) {
        log.debug("doCheckFixedRadiusBezierTrackSegmentsMenuItem({})", trackSegmentName);

        LayoutTrack layoutTrack = layoutEditor.getFinder().findObjectByName(trackSegmentName);
        if (layoutTrack != null) {
            layoutEditor.setSelectionRect(layoutTrack.getBounds());
            // setSelectionRect calls createSelectionGroups...
            // so we have to clear it before amending to it
            layoutEditor.clearSelectionGroups();
            layoutEditor.amendSelectionGroup(layoutTrack);
            // show its popup menu
            layoutTrack.showPopup();
        } else {
            layoutEditor.clearSelectionGroups();
        }
    }   // doCheckFixedRadiusBezierTrackSegmentsMenuItem

    //
    // collect the names of all checkbox menu items with checkmarks
    //
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

    private final static Logger log
            = LoggerFactory.getLogger(LayoutEditorChecks.class);
}
