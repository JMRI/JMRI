package jmri.jmrit.display.layoutEditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.util.MathUtil;
import jmri.util.swing.JmriMouseEvent;

/**
 * The View class for a LayoutTurntable object.
 * This class handles the drawing and user interaction for the turntable.
 */
public class LayoutTurntableView extends LayoutTrackView {

    private final LayoutTurntable turntable;

    public LayoutTurntableView(@Nonnull LayoutTurntable track, @Nonnull Point2D c, @Nonnull LayoutEditor layoutEditor) {
        super(track, c, layoutEditor);
        this.turntable = track;
    }

    /**
     * Provide access to the underlying model, required for saving.
     * @return The LayoutTurntable model object.
     */
    public LayoutTurntable getTurntable() {
        return turntable;
    }

    // --- Overridden abstract methods from LayoutTrackView ---

    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        if (isHidden()) return;
        Point2D center = getCoordsCenter();
        double radius = turntable.getRadius();

        // 1. Draw the outer circle of the turntable pit
        g2.setColor(Color.GRAY);
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(new Ellipse2D.Double(center.getX() - radius, center.getY() - radius, 2 * radius, 2 * radius));

        // 2. Draw the ray tracks (lines only)
        for (int i = 0; i < turntable.getNumberRays(); i++) {
            int rayIndex = turntable.getRayIndex(i);
            double angle = turntable.getRayAngle(i);
            double angleRad = Math.toRadians(angle);

            Point2D anchorPoint = getCoordsForConnectionType(HitPointType.turntableTrackIndexedValue(rayIndex));
            double edgeX = center.getX() + radius * Math.sin(angleRad);
            double edgeY = center.getY() - radius * Math.cos(angleRad);
            Point2D pitEdgePoint = new Point2D.Double(edgeX, edgeY);

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawLine((int)pitEdgePoint.getX(), (int)pitEdgePoint.getY(), (int)anchorPoint.getX(), (int)anchorPoint.getY());
        }

        // 3. Draw the line representing the turntable bridge at its current position
        if (turntable.isTurnoutControlled()) {
            int currentPositionIndex = turntable.getPosition();
            if (currentPositionIndex >= 0) {
                for (int i = 0; i < turntable.getNumberRays(); i++) {
                    if (turntable.getRayIndex(i) == currentPositionIndex) {
                        double angle = turntable.getRayAngle(i);
                        double angleRad = Math.toRadians(angle);
                        double shortRadius = radius * 0.75; // Make one end shorter

                        double x1 = center.getX() + radius * Math.sin(angleRad);
                        double y1 = center.getY() - radius * Math.cos(angleRad);
                        double x2 = center.getX() - shortRadius * Math.sin(angleRad);
                        double y2 = center.getY() + shortRadius * Math.cos(angleRad);

                        g2.setColor(Color.BLACK);
                        g2.setStroke(new BasicStroke(3.0f)); // A thicker line for the bridge
                        g2.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
    }

    @Override
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        // This is handled by drawEditControls, so this method is empty.
    }

    @Override
    protected void drawEditControls(Graphics2D g2) {
        // Draw the center handle for moving the turntable
        g2.setColor(Color.darkGray);
        g2.setStroke(new BasicStroke(1.0f)); // Standard stroke for outer circle
        g2.draw(trackControlCircleAt(getCoordsCenter()));
        
        g2.setStroke(new BasicStroke(2.0f)); // Bold stroke for inner circle
        Point2D center = getCoordsCenter();
        double r = layoutEditor.circleRadius / 2.0;
        g2.draw(new Ellipse2D.Double(center.getX() - r, center.getY() - r, 2 * r, 2 * r));
        g2.setStroke(new BasicStroke(1.0f)); // Reset for other elements

        // Draw the anchor points for each ray
        for (int i = 0; i < turntable.getNumberRays(); i++) {
            Point2D anchorPoint = getCoordsForConnectionType(HitPointType.turntableTrackIndexedValue(turntable.getRayIndex(i)));
            if (turntable.getRayConnectOrdered(i) == null) {
                g2.setColor(Color.RED); // Red for unconnected
            } else {
                g2.setColor(Color.GREEN); // Green for connected
            }
            g2.draw(layoutEditor.layoutEditorControlRectAt(anchorPoint));
        }
    }

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (!turntable.isTurnoutControlled()) return;

        for (int i = 0; i < turntable.getNumberRays(); i++) {
            int rayIndex = turntable.getRayIndex(i);
            if (rayIndex == turntable.getPosition()) {
                continue; // Skip drawing the circle for the active ray
            }

            Turnout rayTurnout = turntable.getRayTurnout(i);
            if (rayTurnout != null) {
                Point2D anchorPoint = getCoordsForConnectionType(HitPointType.turntableTrackIndexedValue(rayIndex));
                Color originalColor = g2.getColor();
                if (rayTurnout.getKnownState() == Turnout.THROWN) {
                    g2.setColor(layoutEditor.turnoutCircleThrownColor);
                } else {
                    g2.setColor(layoutEditor.turnoutCircleColor);
                }
                g2.draw(trackControlCircleAt(anchorPoint));
                g2.setColor(originalColor);
            }
        }
    }

    @Override
    protected void drawDecorations(Graphics2D g2) {
    }

    @Override
    public boolean canRemove() {
        return turntable.canRemove();
    }

    @Override
    public void scaleCoords(double xFactor, double yFactor) {
        Point2D center = getCoordsCenter();
        setCoordsCenter(new Point2D.Double(center.getX() * xFactor, center.getY() * yFactor));
        turntable.setRadius(turntable.getRadius() * Math.min(xFactor, yFactor));
    }

    @Override
    public void translateCoords(double xFactor, double yFactor) {
        Point2D center = getCoordsCenter();
        setCoordsCenter(new Point2D.Double(center.getX() + xFactor, center.getY() + yFactor));
    }

    @Override
    public void rotateCoords(double angleDEG) {
    }

    @Override
    public HitPointType findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        // First, check for hits on the rays' anchor points.
        for (int i = 0; i < turntable.getNumberRays(); i++) {
            Point2D rayCoords = getCoordsForConnectionType(HitPointType.turntableTrackIndexedValue(turntable.getRayIndex(i)));
            // Use the standard framework method for hit detection
            if (trackControlCircleRectAt(rayCoords).contains(hitPoint)) {
                if (requireUnconnected && turntable.getRayConnectOrdered(i) != null) {
                    continue; // Skip this connected ray if we require an unconnected one
                }
                return HitPointType.turntableTrackIndexedValue(turntable.getRayIndex(i));
            }
        }

        // Next, check for a hit on the center point, which is used for moving the whole object.
        if (trackEditControlCircleAt(getCoordsCenter()).contains(hitPoint)) {
            return HitPointType.TURNTABLE_CENTER;
        }

        // If nothing else was hit, return NONE.
        return HitPointType.NONE;
    }

    @Override
    @Nonnull
    public Point2D getCoordsForConnectionType(HitPointType connectionType) {
        if (HitPointType.isTurntableRayHitType(connectionType)) {
            int rayIndex = connectionType.turntableTrackIndex();
            for (int i = 0; i < turntable.getNumberRays(); i++) {
                if (turntable.getRayIndex(i) == rayIndex) {
                    double angle = turntable.getRayAngle(i);
                    Point2D center = getCoordsCenter();
                    // Use a radius slightly larger than the turntable pit for the connection points
                    double radius = turntable.getRadius() + layoutEditor.circleRadius;
                    double angleRad = Math.toRadians(angle);
                    double x = center.getX() + radius * Math.sin(angleRad);
                    double y = center.getY() - radius * Math.cos(angleRad);
                    return new Point2D.Double(x, y);
                }
            }
        }
        return getCoordsCenter();
    }

    @Override
    public Rectangle2D getBounds() {
        Point2D center = getCoordsCenter();
        double radius = turntable.getRadius();
        return new Rectangle2D.Double(center.getX() - radius, center.getY() - radius, 2 * radius, 2 * radius);
    }

    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nonnull JmriMouseEvent mouseEvent) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem("Turntable: " + turntable.getName()));
        
        LayoutBlock block = turntable.getLayoutBlock();
        if (block != null) {
            popup.add(new JMenuItem("Block: " + block.getDisplayName()));
        } else {
            popup.add(new JMenuItem("Block: None"));
        }

        JMenu connectionsMenu = new JMenu("Connections");
        for (int i = 0; i < turntable.getNumberRays(); i++) {
            LayoutTrack connected = turntable.getRayConnectOrdered(i);
            if (connected != null) {
                connectionsMenu.add(new JMenuItem("Ray " + turntable.getRayIndex(i) + " -> " + connected.getName()));
            }
        }
        if (connectionsMenu.getItemCount() > 0) {
            popup.add(connectionsMenu);
        }

        popup.addSeparator();
        popup.add(new JMenuItem("Edit..."));
        popup.add(new JMenuItem("Delete"));
        
        addCommonPopupItems(mouseEvent, popup);
        
        popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        return popup;
    }

    /**
     * Build and show the popup menu for a single ray connection point.
     * This is called directly by LayoutEditor.
     */
    public void showRayPopUp(JmriMouseEvent event, int rayIndex) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem("Turntable Ray " + rayIndex));

        LayoutTrack connectedTrack = null;
        Turnout rayTurnout = null;
        int rayOrderIndex = -1;
        for (int i = 0; i < turntable.getNumberRays(); i++) {
            if (turntable.getRayIndex(i) == rayIndex) {
                rayOrderIndex = i;
                connectedTrack = turntable.getRayConnectOrdered(i);
                rayTurnout = turntable.getRayTurnout(i);
                break;
            }
        }

        if (connectedTrack != null) {
            popup.add(new JMenuItem("Connected to: " + connectedTrack.getName()));
        } else {
            popup.add(new JMenuItem("Unconnected"));
        }

        if (rayTurnout != null) {
            String state = (rayTurnout.getKnownState() == Turnout.THROWN) ? " (Thrown)" : " (Closed)";
            popup.add(new JMenuItem("Turnout: " + rayTurnout.getDisplayName() + state));
        }
        
        popup.addSeparator();

        if (rayTurnout != null) {
            JCheckBoxMenuItem disabledCheck = new JCheckBoxMenuItem("Disabled");
            disabledCheck.setSelected(turntable.isRayDisabled(rayIndex));
            disabledCheck.addActionListener(e -> {
                turntable.setRayDisabled(rayIndex, ((JCheckBoxMenuItem)e.getSource()).isSelected());
            });
            popup.add(disabledCheck);

            JCheckBoxMenuItem disableWhenOccupiedCheck = new JCheckBoxMenuItem("Disabled when Occupied");
            if (turntable.getLayoutBlock() == null) {
                disableWhenOccupiedCheck.setEnabled(false);
            }
            disableWhenOccupiedCheck.setSelected(turntable.isRayDisabledWhenOccupied(rayIndex));
            disableWhenOccupiedCheck.addActionListener(e -> {
                 turntable.setRayDisableWhenOccupied(rayIndex, ((JCheckBoxMenuItem)e.getSource()).isSelected());
            });
            popup.add(disableWhenOccupiedCheck);
        }

        addCommonPopupItems(event, popup);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }


    // --- Delegate remaining abstract methods to the model ---

    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        return turntable.getConnection(connectionType);
    }

    @Override
    public void setConnection(HitPointType connectionType, LayoutTrack o, HitPointType type) throws jmri.JmriException {
        turntable.setConnection(connectionType, o, type);
    }

    @Override
    protected void reCheckBlockBoundary() {
        turntable.reCheckBlockBoundary();
    }

    @Override
    @CheckForNull
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        return turntable.getLayoutConnectivity();
    }

    @Override
    @Nonnull
    public List<HitPointType> checkForFreeConnections() {
        return turntable.checkForFreeConnections();
    }

    @Override
    public boolean checkForUnAssignedBlocks() {
        return turntable.checkForUnAssignedBlocks();
    }

    @Override
    public void checkForNonContiguousBlocks(@Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetMaps) {
        turntable.checkForNonContiguousBlocks(blockNamesToTrackNameSetMaps);
    }

    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName, @Nonnull Set<String> TrackNameSet) {
        turntable.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
    }

    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        turntable.setAllLayoutBlocks(layoutBlock);
    }
}
