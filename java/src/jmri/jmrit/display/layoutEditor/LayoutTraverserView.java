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
        double width = traverser.getDeckWidth();
        if (getOrientation() == LayoutTraverser.HORIZONTAL) {
            return new Rectangle2D.Double(center.getX() - width / 2, center.getY() - length / 2, width, length);
        } else {
            return new Rectangle2D.Double(center.getX() - length / 2, center.getY() - width / 2, length, width);
        }
    }

    /**
     * Get the center of the control point, which is on an edge of the traverser.
     * @return The center point for the control circles.
     */
    private Point2D getControlPointCenter() {
        Point2D center = getCoordsCenter();
        Rectangle2D bounds = getBounds();
        if (getOrientation() == LayoutTraverser.HORIZONTAL) {
            // Top edge for a horizontal (tall) traverser
            return new Point2D.Double(center.getX(), bounds.getMinY());
        } else {
            // Left edge for a vertical (wide) traverser
            return new Point2D.Double(bounds.getMinX(), center.getY());
        }
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

    public double getSlotOffsetValue(int i) {
        return traverser.getSlotOffsetValue(i);
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
        double deckWidth = traverser.getDeckWidth();
        double anchorOffset = traverser.getSlotOffset() * 0.5;
        for (int i=0; i<traverser.slotList.size(); i++) {
            SlotTrack st = traverser.slotList.get(i);
            if (st.getConnectionIndex() == index) {
                double offset = st.getOffset();
                boolean sideA = (i % 2 == 0);
                if (getOrientation() == LayoutTraverser.HORIZONTAL) { // Tall
                    double y = center.getY() + offset;
                    double x = center.getX() + (sideA ? (-deckWidth / 2.0) - anchorOffset : (deckWidth / 2.0) + anchorOffset);
                    return new Point2D.Double(x, y);
                } else { // Wide
                    double x = center.getX() + offset;
                    double y = center.getY() + (sideA ? (-deckWidth / 2.0) - anchorOffset : (deckWidth / 2.0) + anchorOffset);
                    return new Point2D.Double(x, y);
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
                double deckWidth = traverser.getDeckWidth();
                double anchorOffset = traverser.getSlotOffset() * 0.5;
                double offset = st.getOffset();
                boolean sideA = (i % 2 == 0);

                if (getOrientation() == LayoutTraverser.HORIZONTAL) { // Tall
                    double y = center.getY() + offset;
                    double x = center.getX() + (sideA ? (-deckWidth / 2.0) - anchorOffset : (deckWidth / 2.0) + anchorOffset);
                    return new Point2D.Double(x, y);
                } else { // Wide
                    double x = center.getX() + offset;
                    double y = center.getY() + (sideA ? (-deckWidth / 2.0) - anchorOffset : (deckWidth / 2.0) + anchorOffset);
                    return new Point2D.Double(x, y);
                }
            }
        }
        return MathUtil.zeroPoint2D;
    }

    private Point2D getSlotEdgePointOrdered(int i) {
        if (i < traverser.slotList.size()) {
            SlotTrack st = traverser.slotList.get(i);
            if (st != null) {
                Point2D center = getCoordsCenter();
                double deckWidth = traverser.getDeckWidth();
                double offset = st.getOffset();
                boolean sideA = (i % 2 == 0);

                if (getOrientation() == LayoutTraverser.HORIZONTAL) { // Tall
                    double y = center.getY() + offset;
                    double x = center.getX() + (sideA ? -deckWidth / 2.0 : deckWidth / 2.0);
                    return new Point2D.Double(x, y);
                } else { // Wide
                    double x = center.getX() + offset;
                    double y = center.getY() + (sideA ? -deckWidth / 2.0 : deckWidth / 2.0);
                    return new Point2D.Double(x, y);
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
        } else if (HitPointType.isTraverserSlotHitType(connectionType)) {
            result = getSlotCoordsIndexed(connectionType.traverserTrackIndex());
        } else {
            log.error("{}.getCoordsForConnectionType({}); Invalid connection type",
                    getName(), connectionType); // NOI18N
        }
        return result;
    }

    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        if (HitPointType.isTraverserSlotHitType(connectionType)) {
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
        if (HitPointType.isTraverserSlotHitType(connectionType)) {
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
        traverser.setSlotOffset(traverser.getSlotOffset() * Math.min(xFactor, yFactor));
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
        double slotControlRadius = traverser.getSlotOffset() * 0.25;
        double distance, minDistance = POSITIVE_INFINITY;

        // check the center point
        if (!requireUnconnected) {
            p = getControlPointCenter();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.TRAVERSER_CENTER;
            }
        }

        for (int k = 0; k < getNumberSlots(); k++) {
            if (!isSlotDisabled(k) && (!requireUnconnected || (getSlotConnectOrdered(k) == null))) {
                p = getSlotCoordsOrdered(k);
                distance = MathUtil.distance(p, hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    minPoint = p;
                    result = HitPointType.traverserTrackIndexedValue(getSlotIndex(k));
                }
            }
        }

        if (result == HitPointType.TRAVERSER_CENTER) {
            if (minDistance > circleRadius) {
                result = HitPointType.NONE;
            }
        } else if (HitPointType.isTraverserSlotHitType(result)) {
            if (minDistance > slotControlRadius) {
                result = HitPointType.NONE;
            }
        } else {
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
        float trackWidth = 2.F;
		
		
		// Only draw in the appropriate pass (mainline or sideline)
        if (isMain != traverser.isMainline()) {
            return;
        }

        // Save original stroke and color
        Stroke originalStroke = g2.getStroke();
        Color originalColor = g2.getColor();

        // Draw pit outline and control circles - these are static and belong to the traverser itself
        if (!isBlock && (isMain == traverser.isMainline())) {
            // Draw the outer pit rectangle with a thin line
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(layoutEditor.getDefaultTrackColorColor());
            g2.draw(getBounds());

            // Draw the control circles with the standard track width
            
            double circleRadius = Math.max(traverser.getDeckWidth() / 8.f, trackWidth * 2);
            double circleDiameter = circleRadius * 2.f;
            Point2D center = getControlPointCenter();
			g2.setStroke(new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.draw(new Ellipse2D.Double(center.getX() - circleRadius, center.getY() - circleRadius, circleDiameter, circleDiameter));
			g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.draw(trackControlCircleAt(center));
			
			// Restore original stroke and color for subsequent drawing
            g2.setStroke(originalStroke);
            g2.setColor(originalColor);
        }

        // Draw slot tracks
        for (int i = 0; i < getNumberSlots(); i++) { // Loop through all slots
            if (!traverser.isSlotDisabled(i)) { // Only draw if the slot is NOT disabled
                Color slotColor = null;
                TrackSegment ts = getSlotConnectOrdered(i);
                // A slot is mainline if the bridge is, or if the connected track is.
                boolean slotIsMain = false;
				if (ts != null) {
					slotIsMain = ts.isMainline();
				}
                // Set color for block, if any
				if (isBlock) {
					if (ts == null) {
						g2.setColor(layoutEditor.getDefaultTrackColorColor());
					} else {
						LayoutBlock lb = ts.getLayoutBlock();
						if (lb != null) {
							slotColor = g2.getColor();
							setColorForTrackBlock(g2, lb);
						}
					}
				}

                if (isMain == slotIsMain) { // Draw only if mainline/sideline status matches the current pass
                    LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();
                    float width = isMain ? ltdo.getMainBlockLineWidth() : ltdo.getSideBlockLineWidth();
                    //g2.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                    Point2D edgePoint = getSlotEdgePointOrdered(i);
                    Point2D anchorPoint = getSlotCoordsOrdered(i);
                    g2.draw(new Line2D.Double(edgePoint, anchorPoint));
                }

                // Restore color if changed
                if (slotColor != null) {
                    g2.setColor(slotColor);
                }
            }
        }

        // Draw the sliding bridge
        int currentPositionConnectionIndex = getPosition();
        int orderedIndex = -1;
        if (currentPositionConnectionIndex != -1) {
            for (int i = 0; i < getNumberSlots(); i++) {
                if (getSlotIndex(i) == currentPositionConnectionIndex) {
                    orderedIndex = i;
                    break;
                }
            }
        }
        // Only draw the bridge if a slot is selected, it's not disabled, and its mainline status matches the current pass
        if (orderedIndex != -1 && !traverser.isSlotDisabled(orderedIndex) && (isMain == traverser.isMainline())) {
            // Set color for bridge
            if (isBlock) {
                LayoutBlock lb = getLayoutBlock();
                if (lb != null) {
                    setColorForTrackBlock(g2, lb);
                } else {
                    g2.setColor(layoutEditor.getDefaultTrackColorColor());
                }
            } else {
                g2.setColor(traverser.isMainline() ? layoutEditor.getLayoutTrackDrawingOptions().getMainRailColor()
                        : layoutEditor.getLayoutTrackDrawingOptions().getSideRailColor());
            }
            // Set the stroke for the bridge
            LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();
            float width = traverser.isMainline() ? ltdo.getMainBlockLineWidth() : ltdo.getSideBlockLineWidth();
            //g2.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

            Point2D center = getCoordsCenter();
            Rectangle2D pit = getBounds();
            double offset = getSlotOffsetValue(orderedIndex);
            double gap = traverser.getDeckWidth() * 0.2;
            boolean sideA = (orderedIndex % 2 == 0);

            if (getOrientation() == LayoutTraverser.HORIZONTAL) {
                double y = center.getY() + offset;
                if (sideA) {
                    g2.draw(new Line2D.Double(pit.getMinX(), y, pit.getMaxX() - gap, y));
                } else {
                    g2.draw(new Line2D.Double(pit.getMinX() + gap, y, pit.getMaxX(), y));
                }
            } else { // VERTICAL
                double x = center.getX() + offset;
                if (sideA) {
                    g2.draw(new Line2D.Double(x, pit.getMinY(), x, pit.getMaxY() - gap));
                } else {
                    g2.draw(new Line2D.Double(x, pit.getMinY() + gap, x, pit.getMaxY()));
                }
            }
        }
        if (!layoutEditor.isEditable()) {
            drawTurnoutControls(g2);
        }

        // Restore original stroke and color
        g2.setStroke(originalStroke);
        g2.setColor(originalColor);
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
            if (!traverser.isSlotDisabled(j) && ((specificType == HitPointType.NONE) || (specificType == (HitPointType.traverserTrackIndexedValue(j))))) {
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
                if (getPosition() != getSlotIndex(j)) {
                    SlotTrack rt = traverser.slotList.get(j); // Get the SlotTrack object
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
            if (!isSlotDisabled(j)) {
                Point2D pt = getSlotCoordsOrdered(j);

                if (getSlotConnectOrdered(j) == null) {
                    g2.setColor(Color.red);
                } else {
                    g2.setColor(Color.green);
                }
                g2.draw(layoutEditor.layoutEditorControlRectAt(pt));
            }
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
