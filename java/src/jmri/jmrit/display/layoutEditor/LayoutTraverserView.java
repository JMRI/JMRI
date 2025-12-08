package jmri.jmrit.display.layoutEditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import static java.lang.Float.POSITIVE_INFINITY;
import java.text.MessageFormat;
import java.util.List;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.display.layoutEditor.LayoutTraverser.SlotTrack;
import jmri.util.MathUtil;
import jmri.util.swing.JmriMouseEvent;

/**
 * MVC View component for the LayoutTraverser class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 * @author Dave Sand Copyright (c) 2024
 */
public class LayoutTraverserView extends LayoutTrackView {

    // defined constants
    // operational instance variables (not saved between sessions)
    private final jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutTraverserEditor editor;

    /**
     * Constructor method.
     * @param traverser the layout traverser to create view for.
     * @param c            where to put it
     * @param layoutEditor what layout editor panel to put it in
     */
    public LayoutTraverserView(@Nonnull LayoutTraverser traverser,
                @Nonnull Point2D c,
                @Nonnull LayoutEditor layoutEditor) {
        super(traverser, c, layoutEditor);
        this.traverser = traverser;

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutTraverserEditor(layoutEditor);
    }

    final private LayoutTraverser traverser;

    final public LayoutTraverser getTraverser() { return traverser; }

    /**
     * Get a string that represents this object. This should only be used for
     * debugging.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "LayoutTraverser " + getName();
    }

    //
    // Accessor methods
    //
    public double getDeckLength() { return traverser.getDeckLength(); }
    public void setDeckLength(double l) { traverser.setDeckLength(l); }
    public double getDeckWidth() { return traverser.getDeckWidth(); }
    public void setDeckWidth(double w) { traverser.setDeckWidth(w); }
    public int getOrientation() { return traverser.getOrientation(); }
    public void setOrientation(int o) { traverser.setOrientation(o); }

    /**
     * @return the layout block name
     */
    @Nonnull
    public String getBlockName() {
        return traverser.getBlockName();
    }

    /**
     * @return the layout block
     */
    public LayoutBlock getLayoutBlock() {
        return traverser.getLayoutBlock();
    }

    /**
     * Set up a LayoutBlock for this LayoutTraverser.
     *
     * @param newLayoutBlock the LayoutBlock to set
     */
    public void setLayoutBlock(@CheckForNull LayoutBlock newLayoutBlock) {
        traverser.setLayoutBlock(newLayoutBlock);
    }

    /**
     * Set up a LayoutBlock for this LayoutTraverser.
     *
     * @param name the name of the new LayoutBlock
     */
    public void setLayoutBlockByName(@CheckForNull String name) {
        traverser.setLayoutBlockByName(name);
    }

    /*
     * non-accessor methods
     */
    /**
     * @return the bounds of this traverser.
     */
    @Override
    public Rectangle2D getBounds() {
        Point2D center = getCoordsCenter();
        double length = getDeckLength();
        double width = getDeckWidth();
        if (getOrientation() == LayoutTraverser.HORIZONTAL) {
            return new Rectangle2D.Double(center.getX() - length / 2, center.getY() - width / 2, length, width);
        } else {
            return new Rectangle2D.Double(center.getX() - width / 2, center.getY() - length / 2, width, length);
        }
    }

    public SlotTrack addSlot(double offset) {
        return traverser.addSlot(offset);
    }

    public TrackSegment getSlotConnectIndexed(int index) {
        return traverser.getSlotConnectIndexed(index);
    }

    public TrackSegment getSlotConnectOrdered(int i) {
        return traverser.getSlotConnectOrdered(i);
    }

    public void setSlotConnect(TrackSegment ts, int index) {
        traverser.setSlotConnect(ts, index);
    }

    public List<SlotTrack> getSlotList() {
        return traverser.getSlotList();
    }

    public int getNumberSlots() {
        return traverser.getNumberSlots();
    }

    public int getSlotIndex(int i) {
        return traverser.getSlotIndex(i);
    }

    public double getSlotOffset(int i) {
        return traverser.getSlotOffset(i);
    }

    public void setSlotTurnout(int index, String turnoutName, int state) {
        traverser.setSlotTurnout(index, turnoutName, state);
    }

    public String getSlotTurnoutName(int i) {
        return traverser.getSlotTurnoutName(i);
    }

    public Turnout getSlotTurnout(int i) {
        return traverser.getSlotTurnout(i);
    }

    public int getSlotTurnoutState(int i) {
        return traverser.getSlotTurnoutState(i);
    }

    public boolean isSlotDisabled(int i) {
        return traverser.isSlotDisabled(i);
    }

    public void setSlotDisabled(int i, boolean boo) {
        traverser.setSlotDisabled(i, boo);
    }

    public boolean isSlotDisabledWhenOccupied(int i) {
        return traverser.isSlotDisabledWhenOccupied(i);
    }

    public void setSlotDisabledWhenOccupied(int i, boolean boo) {
        traverser.setSlotDisabledWhenOccupied(i, boo);
    }

    public Point2D getSlotCoordsIndexed(int index) {
        Point2D center = getCoordsCenter();
        for (SlotTrack st : traverser.slotList) {
            if (st.getConnectionIndex() == index) {
                if (getOrientation() == LayoutTraverser.HORIZONTAL) {
                    return new Point2D.Double(center.getX() + st.getOffset(), center.getY());
                } else {
                    return new Point2D.Double(center.getX(), center.getY() + st.getOffset());
                }
            }
        }
        return MathUtil.zeroPoint2D;
    }

    public Point2D getSlotCoordsOrdered(int i) {
        if (i < traverser.slotList.size()) {
            SlotTrack st = traverser.slotList.get(i);
            if (st != null) {
                Point2D center = getCoordsCenter();
                if (getOrientation() == LayoutTraverser.HORIZONTAL) {
                    return new Point2D.Double(center.getX() + st.getOffset(), center.getY());
                } else {
                    return new Point2D.Double(center.getX(), center.getY() + st.getOffset());
                }
            }
        }
        return MathUtil.zeroPoint2D;
    }

    public void setSlotCoordsIndexed(double x, double y, int index) {
        for (SlotTrack st : traverser.slotList) {
            if (st.getConnectionIndex() == index) {
                if (getOrientation() == LayoutTraverser.HORIZONTAL) {
                    st.setOffset(x - getCoordsCenter().getX());
                } else {
                    st.setOffset(y - getCoordsCenter().getY());
                }
                break;
            }
        }
    }

    public void setSlotCoordsIndexed(Point2D point, int index) {
        setSlotCoordsIndexed(point.getX(), point.getY(), index);
    }

    @Override
    public Point2D getCoordsForConnectionType(HitPointType connectionType) {
        Point2D result = getCoordsCenter();
        if (HitPointType.TRAVERSER_CENTER == connectionType) {
            // This is correct
        } else if (HitPointType.isTraverserRayHitType(connectionType)) {
            result = getSlotCoordsIndexed(connectionType.traverserTrackIndex());
        } else {
            log.error("{}.getCoordsForConnectionType({}); Invalid connection type",
                    getName(), connectionType); // NOI18N
        }
        return result;
    }

    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        if (HitPointType.isTraverserRayHitType(connectionType)) {
            return getSlotConnectIndexed(connectionType.traverserTrackIndex());
        } else {
            String errString = MessageFormat.format("{0}.getCoordsForConnectionType({1}); Invalid connection type",
                    getName(), connectionType); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
        }
    }

    @Override
    public void setConnection(HitPointType connectionType, LayoutTrack o, HitPointType type) throws jmri.JmriException {
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
        }
        if (HitPointType.isTraverserRayHitType(connectionType)) {
            if ((o == null) || (o instanceof TrackSegment)) {
                setSlotConnect((TrackSegment) o, connectionType.traverserTrackIndex());
            } else {
                String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid object: {4}",
                        getName(), connectionType, o.getName(),
                        type, o.getClass().getName()); // NOI18N
                log.error("will throw {}", errString); // NOI18N
                throw new jmri.JmriException(errString);
            }
        } else {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); Invalid connection type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type); // NOI18N
            log.error("will throw {}", errString); // NOI18N
            throw new jmri.JmriException(errString);
        }
    }

    public boolean isMainlineIndexed(int index) {
        return traverser.isMainlineIndexed(index);
    }

    public boolean isMainlineOrdered(int i) {
        return traverser.isMainlineOrdered(i);
    }

    @Override
    public void scaleCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.granulize(MathUtil.multiply(getCoordsCenter(), factor), 1.0));
        if (getOrientation() == LayoutTraverser.HORIZONTAL) {
            setDeckLength(getDeckLength() * xFactor);
            setDeckWidth(getDeckWidth() * yFactor);
        } else {
            setDeckLength(getDeckLength() * yFactor);
            setDeckWidth(getDeckWidth() * xFactor);
        }
    }

    @Override
    public void translateCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.add(getCoordsCenter(), factor));
    }

    @Override
    public void rotateCoords(double angleDEG) {
        // For now, we only support 90-degree rotations
        if (angleDEG == 90 || angleDEG == -270) {
            setOrientation(1 - getOrientation()); // Toggle between HORIZONTAL and VERTICAL
        } else if (angleDEG == -90 || angleDEG == 270) {
            setOrientation(1 - getOrientation()); // Toggle between HORIZONTAL and VERTICAL
        } else if (angleDEG == 180 || angleDEG == -180) {
            // No change in orientation, but might need to flip offsets if that becomes a feature
        }
    }

    @Override
    protected HitPointType findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        HitPointType result = HitPointType.NONE;  // assume point not on connection
        Point2D p, minPoint = MathUtil.zeroPoint2D;

        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double distance, minDistance = POSITIVE_INFINITY;

        // check the center point
        p = getCoordsCenter();
        distance = MathUtil.distance(p, hitPoint);
        if (distance < minDistance) {
            minDistance = distance;
            minPoint = p;
            result = HitPointType.TRAVERSER_CENTER;
        }

        for (int k = 0; k < getNumberSlots(); k++) {
            if (!requireUnconnected || (getSlotConnectOrdered(k) == null)) {
                p = getSlotCoordsOrdered(k);
                distance = MathUtil.distance(p, hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    minPoint = p;
                    result = HitPointType.traverserTrackIndexedValue(k);
                }
            }
        }
        if (minDistance > circleRadius) {
            result = HitPointType.NONE;
        }
        return result;
    }

    public String tLayoutBlockName = "";

    public boolean isTurnoutControlled() {
        return traverser.isTurnoutControlled();
    }

    public void setTurnoutControlled(boolean boo) {
        traverser.setTurnoutControlled(boo);
    }

    private JPopupMenu popupMenu = null;

    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nonnull JmriMouseEvent mouseEvent) {
        if (popupMenu != null) {
            popupMenu.removeAll();
        } else {
            popupMenu = new JPopupMenu();
        }

        JMenuItem jmi = popupMenu.add(Bundle.getMessage("MakeLabel", traverser.getTypeName()) + getName());
        jmi.setEnabled(false);

        LayoutBlock lb = getLayoutBlock();
        if (lb == null) {
            jmi = popupMenu.add(Bundle.getMessage("NoBlock"));
        } else {
            String displayName = lb.getDisplayName();
            jmi = popupMenu.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + displayName);
        }
        jmi.setEnabled(false);

        if (!traverser.slotList.isEmpty()) {
            JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections"));
            traverser.slotList.forEach((rt) -> {
                TrackSegment ts = rt.getConnect();
                if (ts != null) {
                    TrackSegmentView tsv = layoutEditor.getTrackSegmentView(ts);
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "" + rt.getConnectionIndex()) + ts.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            layoutEditor.setSelectionRect(tsv.getBounds());
                            tsv.showPopup();
                        }
                    });
                }
            });
            popupMenu.add(connectionsMenu);
        }

        popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));

        popupMenu.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.editLayoutTrack(LayoutTraverserView.this);
            }
        });
        popupMenu.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (removeInlineLogixNG() && layoutEditor.removeTraverser(traverser)) {
                    remove();
                    dispose();
                }
            }
        });
        layoutEditor.setShowAlignmentMenu(popupMenu);
        addCommonPopupItems(mouseEvent, popupMenu);
        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        return popupMenu;
    }

    private JPopupMenu slotPopup = null;

    protected void showSlotPopUp(JmriMouseEvent e, int index) {
        if (slotPopup != null) {
            slotPopup.removeAll();
        } else {
            slotPopup = new JPopupMenu();
        }

        for (SlotTrack rt : traverser.slotList) {
            if (rt.getConnectionIndex() == index) {
                JMenuItem jmi = slotPopup.add("Traverser Slot " + index);
                jmi.setEnabled(false);

                slotPopup.add(new AbstractAction(
                        Bundle.getMessage("MakeLabel",
                                Bundle.getMessage("Connected"))
                        + rt.getConnect().getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = lf.findObjectByName(rt.getConnect().getName());

                        if (lt != null) {
                            LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                            layoutEditor.setSelectionRect(ltv.getBounds());
                            ltv.showPopup();
                        }
                    }
                });

                if (rt.getTurnout() != null) {
                    String info = rt.getTurnout().getDisplayName();
                    String stateString = getTurnoutStateString(rt.getTurnoutState());
                    if (!stateString.isEmpty()) {
                        info += " (" + stateString + ")";
                    }
                    jmi = slotPopup.add(info);
                    jmi.setEnabled(false);

                    slotPopup.add(new JSeparator(JSeparator.HORIZONTAL));

                    JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(Bundle.getMessage("Disabled"));
                    cbmi.setSelected(rt.isDisabled());
                    slotPopup.add(cbmi);
                    cbmi.addActionListener((java.awt.event.ActionEvent e2) -> {
                        JCheckBoxMenuItem o = (JCheckBoxMenuItem) e2.getSource();
                        rt.setDisabled(o.isSelected());
                    });

                    cbmi = new JCheckBoxMenuItem(Bundle.getMessage("DisabledWhenOccupied"));
                    cbmi.setSelected(rt.isDisabledWhenOccupied());
                    slotPopup.add(cbmi);
                    cbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                        JCheckBoxMenuItem o = (JCheckBoxMenuItem) e3.getSource();
                        rt.setDisabledWhenOccupied(o.isSelected());
                    });
                }
                slotPopup.show(e.getComponent(), e.getX(), e.getY());
                break;
            }
        }
    }

    public void setPosition(int index) {
        traverser.setPosition(index);
    }

    public int getPosition() {
        return traverser.getPosition();
    }

    public void deleteSlot(SlotTrack slotTrack) {
        traverser.deleteSlot(slotTrack);
    }

    public void dispose() {
        if (popupMenu != null) {
            popupMenu.removeAll();
        }
        popupMenu = null;
        traverser.slotList.forEach((rt) -> {
            rt.dispose();
        });
    }

    public void remove() {
        active = false;
    }

    private boolean active = true;

    public boolean isActive() {
        return active;
    }

    @Override
    protected void drawDecorations(Graphics2D g2) {}

    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        if (!isMain) return; // Traversers are always mainline

        float trackWidth = 2.F;

        if (isBlock) {
            // Save original stroke and color
            Stroke originalStroke = g2.getStroke();
            Color originalColor = g2.getColor();

            // Set stroke and color for pit outline
            g2.setStroke(new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(layoutEditor.getDefaultTrackColorColor());

            // Draw the outer pit rectangle
            g2.draw(getBounds());

            // Draw the concentric circles at the center
            double circleRadius = Math.max(getDeckWidth() / 4.f, trackWidth * 2);
            double circleDiameter = circleRadius * 2.f;

            Point2D center = getCoordsCenter();
            g2.draw(new Ellipse2D.Double(center.getX() - circleRadius, center.getY() - circleRadius, circleDiameter, circleDiameter));
            g2.draw(trackControlCircleAt(center));

            // Restore original stroke and color
            g2.setStroke(originalStroke);
            g2.setColor(originalColor);
        }

        // Draw the sliding bridge
        int currentPositionIndex = getPosition();
        if (currentPositionIndex != -1) {
            // Set color for bridge block
            if (isBlock) {
                LayoutBlock lb = getLayoutBlock();
                if (lb != null) {
                    setColorForTrackBlock(g2, lb);
                } else {
                    g2.setColor(layoutEditor.getDefaultTrackColorColor());
                }
            }

            double deckWid = getDeckWidth();
            Point2D center = getCoordsCenter();
            Rectangle2D bridge;
            double offset = getSlotOffset(currentPositionIndex);
            if (getOrientation() == LayoutTraverser.HORIZONTAL) {
                bridge = new Rectangle2D.Double(center.getX() + offset - deckWid / 2, center.getY() - deckWid / 2, deckWid, deckWid);
            } else { // VERTICAL
                bridge = new Rectangle2D.Double(center.getX() - deckWid / 2, center.getY() + offset - deckWid / 2, deckWid, deckWid);
            }
            g2.draw(bridge);
        }
    }

    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        // For now, just draw a simple line for the rails
        if (!isMain) return;

        Rectangle2D pit = getBounds();
        if (getOrientation() == LayoutTraverser.HORIZONTAL) {
            g2.draw(new Line2D.Double(pit.getMinX(), pit.getMinY() + railDisplacement, pit.getMaxX(), pit.getMinY() + railDisplacement));
            g2.draw(new Line2D.Double(pit.getMinX(), pit.getMaxY() - railDisplacement, pit.getMaxX(), pit.getMaxY() - railDisplacement));
        } else { // VERTICAL
            g2.draw(new Line2D.Double(pit.getMinX() + railDisplacement, pit.getMinY(), pit.getMinX() + railDisplacement, pit.getMaxY()));
            g2.draw(new Line2D.Double(pit.getMaxX() - railDisplacement, pit.getMinY(), pit.getMaxX() - railDisplacement, pit.getMaxY()));
        }
    }

    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        for (int j = 0; j < getNumberSlots(); j++) {
            if ((specificType == HitPointType.NONE) || (specificType == (HitPointType.traverserTrackIndexedValue(j)))) {
                if (getSlotConnectOrdered(j) == null) {
                    Point2D pt = getSlotCoordsOrdered(j);
                    g2.fill(trackControlCircleAt(pt));
                }
            }
        }
    }

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (isTurnoutControlled()) {
            for (int j = 0; j < getNumberSlots(); j++) {
                if (getPosition() != j) {
                    SlotTrack rt = traverser.slotList.get(j);
                    if (!rt.isDisabled() && !(rt.isDisabledWhenOccupied() && rt.isOccupied())) {
                        Point2D pt = getSlotCoordsOrdered(j);
                        g2.draw(trackControlCircleAt(pt));
                    }
                }
            }
        }
    }

    @Override
    protected void drawEditControls(Graphics2D g2) {
        for (int j = 0; j < getNumberSlots(); j++) {
            Point2D pt = getSlotCoordsOrdered(j);

            if (getSlotConnectOrdered(j) == null) {
                g2.setColor(Color.red);
            } else {
                g2.setColor(Color.green);
            }
            g2.draw(layoutEditor.layoutEditorControlRectAt(pt));
        }
    }

    @Override
    protected void reCheckBlockBoundary() {}

    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        return null;
    }

    @Override
    public List<HitPointType> checkForFreeConnections() {
        List<HitPointType> result = new ArrayList<>();
        for (int k = 0; k < getNumberSlots(); k++) {
            if (getSlotConnectOrdered(k) == null) {
                result.add(HitPointType.traverserTrackIndexedValue(k));
            }
        }
        return result;
    }

    @Override
    public boolean checkForUnAssignedBlocks() {
        return true;
    }

    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
    }

    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
    }

    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
    }

    @Override
    public boolean canRemove() {
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTraverserView.class);
}
