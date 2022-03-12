package jmri.jmrit.display.layoutEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import javax.annotation.*;
import javax.swing.*;
import jmri.util.*;
import jmri.util.swing.JmriColorChooser;

/**
 * A LayoutShape is a set of LayoutShapePoint used to draw a shape. Each point
 * can ether be a point on the shape or a control point that defines a curve
 * that's part of the shape. The shape can be open (end points not connected) or
 * closed (end points connected)
 *
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutShape {

    public static final int MAX_LINEWIDTH = 200;

    // operational instance variables (not saved between sessions)
    private LayoutEditor layoutEditor = null;
    private String name;
    private LayoutShapeType layoutShapeType;
    private int level = 3;
    private int lineWidth = 3;
    private Color lineColor = Color.BLACK;
    private Color fillColor = Color.DARK_GRAY;

    // these are saved
    // list of LayoutShapePoints
    private final ArrayList<LayoutShapePoint> shapePoints;

    /**
     * constructor method (used by XML loading code)
     *
     * @param name         the name of the shape
     * @param layoutEditor reference to the LayoutEditor this shape is in
     */
    public LayoutShape(String name, LayoutEditor layoutEditor) {
        this.name = name;
        this.layoutEditor = layoutEditor;
        this.layoutShapeType = LayoutShapeType.Open;
        this.shapePoints = new ArrayList<>();
    }

    /**
     * constructor method (used by XML loading code)
     *
     * @param name         the name of the shape
     * @param t            the layout shape type.
     * @param layoutEditor reference to the LayoutEditor this shape is in
     */
    public LayoutShape(String name, LayoutShapeType t, LayoutEditor layoutEditor) {
        this(name, layoutEditor);
        this.layoutShapeType = t;
    }

    /**
     * constructor method (used by LayoutEditor)
     *
     * @param name         the name of the shape
     * @param c            the Point2D for the initial point
     * @param layoutEditor reference to the LayoutEditor this shape is in
     */
    public LayoutShape(String name, Point2D c, LayoutEditor layoutEditor) {
        this(name, layoutEditor);
        this.shapePoints.add(new LayoutShapePoint(c));
    }

    /**
     * constructor method (used by duplicate)
     *
     * @param layoutShape to duplicate (deep copy)
     */
    public LayoutShape(LayoutShape layoutShape) {
        this(layoutShape.getName(), layoutShape.getLayoutEditor());
        this.setType(layoutShape.getType());
        this.setLevel(layoutShape.getLevel());
        this.setLineColor(layoutShape.getLineColor());
        this.setFillColor(layoutShape.getFillColor());

        for (LayoutShapePoint lsp : layoutShape.getPoints()) {
            this.shapePoints.add(new LayoutShapePoint(lsp.getType(), lsp.getPoint()));
        }
    }

    // this should only be used for debugging...
    @Override
    public String toString() {
        return String.format("LayoutShape %s", name);
    }

    public String getDisplayName() {
        return String.format("%s %s", Bundle.getMessage("LayoutShape"), name);
    }

    /**
     * accessor methods
     *
     * @return the name of this shape
     */
    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public LayoutShapeType getType() {
        return layoutShapeType;
    }

    public void setType(LayoutShapeType t) {
        if (layoutShapeType != t) {
            switch (t) {
                case Open:
                case Closed:
                case Filled:
                    layoutShapeType = t;
                    break;
                default:    // You shouldn't ever have any invalid LayoutShapeTypes
                    log.error("Invalid Shape Type {}", t); // I18IN
            }
        }
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int w) {
        lineWidth = Math.max(0, w);
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color color) {
        lineColor = color;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color color) {
        fillColor = color;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int l) {
        if (level != l) {
            level = l;
            layoutEditor.sortLayoutShapesByLevel();
        }
    }

    public LayoutEditor getLayoutEditor() {
        return layoutEditor;
    }

    /**
     * add point
     *
     * @param p the point to add
     */
    public void addPoint(Point2D p) {
        if (shapePoints.size() < getMaxNumberPoints()) {
            shapePoints.add(new LayoutShapePoint(p));
        }
    }

    /**
     * add point
     *
     * @param p         the point to add
     * @param nearIndex the index of the existing point to add it near note:
     *                  "near" is defined as before or after depending on
     *                  closest neighbor
     */
    public void addPoint(Point2D p, int nearIndex) {
        int cnt = shapePoints.size();
        if (cnt < getMaxNumberPoints()) {
            // this point
            LayoutShapePoint lsp = shapePoints.get(nearIndex);
            Point2D sp = lsp.getPoint();

            // left point
            int idxL = (nearIndex + cnt - 1) % cnt;
            LayoutShapePoint lspL = shapePoints.get(idxL);
            Point2D pL = lspL.getPoint();
            double distL = MathUtil.distance(p, pL);

            // right point
            int idxR = (nearIndex + 1) % cnt;
            LayoutShapePoint lspR = shapePoints.get(idxR);
            Point2D pR = lspR.getPoint();
            double distR = MathUtil.distance(p, pR);

            // if nearIndex is the 1st point in open shape...
            if ((getType() == LayoutShapeType.Open) && (nearIndex == 0)) {
                distR = MathUtil.distance(pR, p);
                distL = MathUtil.distance(pR, sp);
            }
            int beforeIndex = (distR < distL) ? idxR : nearIndex;

            // if nearIndex is the last point in open shape...
            if ((getType() == LayoutShapeType.Open) && (idxR == 0)) {
                distR = MathUtil.distance(pL, p);
                distL = MathUtil.distance(pL, sp);
                beforeIndex = (distR < distL) ? nearIndex : nearIndex + 1;
            }

            if (beforeIndex >= cnt) {
                shapePoints.add(new LayoutShapePoint(p));
            } else {
                shapePoints.add(beforeIndex, new LayoutShapePoint(p));
            }
        }
    }

    /**
     * add point
     *
     * @param t the type of point to add
     * @param p the point to add
     */
    public void addPoint(LayoutShapePointType t, Point2D p) {
        if (shapePoints.size() < getMaxNumberPoints()) {
            shapePoints.add(new LayoutShapePoint(t, p));
        }
    }

    /**
     * set point
     *
     * @param idx the index of the point to add
     * @param p   the point to add
     */
    public void setPoint(int idx, Point2D p) {
        if (idx < shapePoints.size()) {
            shapePoints.get(idx).setPoint(p);
        }
    }

    /**
     * Get point.
     *
     * @param idx the index of the point to add.
     * @return the 2D point of the ID, MathUtil.zeroPoint2D if no result.
     */
    public Point2D getPoint(int idx) {
        Point2D result = MathUtil.zeroPoint2D;
        if (idx < shapePoints.size()) {
            result = shapePoints.get(idx).getPoint();
        }
        return result;
    }

    // should only be used by xml save code
    public ArrayList<LayoutShapePoint> getPoints() {
        return shapePoints;
    }

    /**
     * get the number of points
     *
     * @return the number of points
     */
    public int getNumberPoints() {
        return shapePoints.size();
    }

    /**
     * get the maximum number of points
     *
     * @return the maximum number of points
     */
    public int getMaxNumberPoints() {
        return HitPointType.NUM_SHAPE_POINTS;
    }

    /**
     * getBounds() - return the bounds of this shape
     *
     * @return Rectangle2D as bound of this shape
     */
    public Rectangle2D getBounds() {
        Rectangle2D result = MathUtil.rectangleAtPoint(shapePoints.get(0).getPoint(), 1.0, 1.0);

        shapePoints.forEach((lsp) -> result.add(lsp.getPoint()));
        return result;
    }

    /**
     * find the hit (location) type for a point
     *
     * @param hitPoint      the point
     * @param useRectangles whether to use (larger) rectangles or (smaller)
     *                      circles for hit testing
     * @return the hit point type for the point (or NONE)
     */
    protected HitPointType findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles) {
        HitPointType result = HitPointType.NONE;  // assume point not on shape

        if (useRectangles) {
            // rather than create rectangles for all the points below and
            // see if the passed in point is in one of those rectangles
            // we can create a rectangle for the passed in point and then
            // test if any of the points below are in that rectangle instead.
            Rectangle2D r = layoutEditor.layoutEditorControlRectAt(hitPoint);

            if (r.contains(getCoordsCenter())) {
                result = HitPointType.SHAPE_CENTER;
            }
            for (int idx = 0; idx < shapePoints.size(); idx++) {
                if (r.contains(shapePoints.get(idx).getPoint())) {
                    result = HitPointType.shapePointIndexedValue(idx);
                    break;
                }
            }
        } else {
            double distance, minDistance = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
            for (int idx = 0; idx < shapePoints.size(); idx++) {
                distance = MathUtil.distance(shapePoints.get(idx).getPoint(), hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    result = HitPointType.shapePointIndexedValue(idx);
                }
            }
        }
        return result;
    }   // findHitPointType

    public static boolean isShapeHitPointType(HitPointType t) {
        return ((t == HitPointType.SHAPE_CENTER)
                || HitPointType.isShapePointOffsetHitPointType(t));
    }

    /**
     * get coordinates of center point of shape
     *
     * @return Point2D coordinates of center point of shape
     */
    public Point2D getCoordsCenter() {
        Point2D sumPoint = MathUtil.zeroPoint2D();
        for (LayoutShapePoint lsp : shapePoints) {
            sumPoint = MathUtil.add(sumPoint, lsp.getPoint());
        }
        return MathUtil.divide(sumPoint, shapePoints.size());
    }

    /*
    * Modify coordinates methods
     */
    /**
     * set center coordinates
     *
     * @param p the coordinates to set
     */
//    @Override
    public void setCoordsCenter(@Nonnull Point2D p) {
        Point2D factor = MathUtil.subtract(p, getCoordsCenter());
        if (!MathUtil.isEqualToZeroPoint2D(factor)) {
            shapePoints.forEach((lsp) -> lsp.setPoint(MathUtil.add(factor, lsp.getPoint())));
        }
    }

    /**
     * scale this shapes coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    public void scaleCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        shapePoints.forEach((lsp) -> lsp.setPoint(MathUtil.multiply(lsp.getPoint(), factor)));
    }

    /**
     * translate this shapes coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    public void translateCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        shapePoints.forEach((lsp) -> lsp.setPoint(MathUtil.add(factor, lsp.getPoint())));
    }

    /**
     * rotate this LayoutTrack's coordinates by angleDEG's
     *
     * @param angleDEG the amount to rotate in degrees
     */
    public void rotateCoords(double angleDEG) {
        Point2D center = getCoordsCenter();
        shapePoints.forEach((lsp) -> lsp.setPoint(MathUtil.rotateDEG(lsp.getPoint(), center, angleDEG)));
    }

    private JPopupMenu popup = null;

    @Nonnull
    protected JPopupMenu showShapePopUp(@CheckForNull MouseEvent mouseEvent, HitPointType hitPointType) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (layoutEditor.isEditable()) {
            int pointIndex = hitPointType.shapePointIndex();

            // JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutShape")) + getName());
            JMenuItem jmi = popup.add(Bundle.getMessage("ShapeNameMenuItemTitle", getName()));

            jmi.setToolTipText(Bundle.getMessage("ShapeNameMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                // prompt for new name
                String newValue = QuickPromptUtil.promptForString(layoutEditor,
                        Bundle.getMessage("LayoutShapeName"),
                        Bundle.getMessage("LayoutShapeName"),
                        name);
                setName(newValue);
                layoutEditor.repaint();
            });

            popup.add(new JSeparator(JSeparator.HORIZONTAL));

//            if (true) { // only enable for debugging; TODO: delete or disable this for production
//                jmi = popup.add("hitPointType: " + hitPointType);
//                jmi.setEnabled(false);
//            }
// add "Change Shape Type to..." menu
            JMenu shapeTypeMenu = new JMenu(Bundle.getMessage("ChangeShapeTypeFromTo", getType().toString()));
            if (getType() != LayoutShapeType.Open) {
                jmi = shapeTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapeTypeOpen")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setType(LayoutShapeType.Open);
                        layoutEditor.repaint();
                    }
                }));
            }

            if (getType() != LayoutShapeType.Closed) {
                jmi = shapeTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapeTypeClosed")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setType(LayoutShapeType.Closed);
                        layoutEditor.repaint();
                    }
                }));
            }

            if (getType() != LayoutShapeType.Filled) {
                jmi = shapeTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapeTypeFilled")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setType(LayoutShapeType.Filled);
                        layoutEditor.repaint();
                    }
                }));
            }

            popup.add(shapeTypeMenu);

// Add "Change Shape Type from {0} to..." menu
            if (hitPointType == HitPointType.SHAPE_CENTER) {
                JMenu shapePointTypeMenu = new JMenu(Bundle.getMessage("ChangeAllShapePointTypesTo"));
                jmi = shapePointTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapePointTypeStraight")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        for (LayoutShapePoint ls : shapePoints) {
                            ls.setType(LayoutShapePointType.Straight);
                        }
                        layoutEditor.repaint();
                    }
                }));

                jmi = shapePointTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapePointTypeCurve")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        for (LayoutShapePoint ls : shapePoints) {
                            ls.setType(LayoutShapePointType.Curve);
                        }
                        layoutEditor.repaint();
                    }
                }));

                popup.add(shapePointTypeMenu);
            } else {
                LayoutShapePoint lsp = shapePoints.get(pointIndex);

                if (lsp != null) { // this should never happen... but just in case...
                    String otherPointTypeName = (lsp.getType() == LayoutShapePointType.Straight)
                            ? LayoutShapePointType.Curve.toString() : LayoutShapePointType.Straight.toString();
                    jmi = popup.add(Bundle.getMessage("ChangeShapePointTypeFromTo", lsp.getType().toString(), otherPointTypeName));
                    jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                        switch (lsp.getType()) {
                            case Straight: {
                                lsp.setType(LayoutShapePointType.Curve);
                                break;
                            }
                            case Curve: {
                                lsp.setType(LayoutShapePointType.Straight);
                                break;
                            }
                            default:
                                log.error("unexpected enum member!");
                        }
                        layoutEditor.repaint();
                    });
                }
            }

// Add "Set Level: x" menu
            jmi = popup.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ShapeLevelMenuItemTitle")) + level));
            jmi.setToolTipText(Bundle.getMessage("ShapeLevelMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                // prompt for level
                int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                        Bundle.getMessage("ShapeLevelMenuItemTitle"),
                        Bundle.getMessage("ShapeLevelMenuItemTitle"),
                        level, QuickPromptUtil.checkIntRange(1, 10, null));
                setLevel(newValue);
                layoutEditor.repaint();
            });

            jmi = popup.add(new JMenuItem(Bundle.getMessage("ShapeLineColorMenuItemTitle")));
            jmi.setToolTipText(Bundle.getMessage("ShapeLineColorMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                Color newColor = JmriColorChooser.showDialog(null, "Choose a color", lineColor);
                if ((newColor != null) && !newColor.equals(lineColor)) {
                    setLineColor(newColor);
                    layoutEditor.repaint();
                }
            });
            jmi.setForeground(lineColor);
            jmi.setBackground(ColorUtil.contrast(lineColor));

            if (getType() == LayoutShapeType.Filled) {
                jmi = popup.add(new JMenuItem(Bundle.getMessage("ShapeFillColorMenuItemTitle")));
                jmi.setToolTipText(Bundle.getMessage("ShapeFillColorMenuItemToolTip"));
                jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    Color newColor = JmriColorChooser.showDialog(null, "Choose a color", fillColor);
                    if ((newColor != null) && !newColor.equals(fillColor)) {
                        setFillColor(newColor);
                        layoutEditor.repaint();
                    }
                });
                jmi.setForeground(fillColor);
                jmi.setBackground(ColorUtil.contrast(fillColor));
            }

// add "Set Line Width: x" menu
            jmi = popup.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("ShapeLineWidthMenuItemTitle")) + lineWidth));
            jmi.setToolTipText(Bundle.getMessage("ShapeLineWidthMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                // prompt for lineWidth
                int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                        Bundle.getMessage("ShapeLineWidthMenuItemTitle"),
                        Bundle.getMessage("ShapeLineWidthMenuItemTitle"),
                        lineWidth, QuickPromptUtil.checkIntRange(1, MAX_LINEWIDTH, null));
                setLineWidth(newValue);
                layoutEditor.repaint();
            });

            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            if (hitPointType == HitPointType.SHAPE_CENTER) {
                jmi = popup.add(new AbstractAction(Bundle.getMessage("ShapeDuplicateMenuItemTitle")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutShape ls = new LayoutShape(LayoutShape.this);
                        ls.setName(layoutEditor.getFinder().uniqueName("S"));

                        double gridSize = layoutEditor.gContext.getGridSize();
                        Point2D delta = new Point2D.Double(gridSize, gridSize);
                        for (LayoutShapePoint lsp : ls.getPoints()) {
                            lsp.setPoint(MathUtil.add(lsp.getPoint(), delta));
                        }
                        layoutEditor.getLayoutShapes().add(ls);
                        layoutEditor.clearSelectionGroups();
                        layoutEditor.amendSelectionGroup(ls);
                    }
                });
                jmi.setToolTipText(Bundle.getMessage("ShapeDuplicateMenuItemToolTip"));

                popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (layoutEditor.removeLayoutShape(LayoutShape.this)) {
                            // Returned true if user did not cancel
                            remove();
                            dispose();
                        }
                    }
                });
            } else {
                popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        shapePoints.remove(pointIndex);
                        layoutEditor.repaint();
                    }
                });
            }
            if (mouseEvent != null) {
                popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
            }
        }
        return popup;
    }   // showPopup

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    //@Override
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Removes this object from display and persistence
     */
    //@Override
    void remove() {
    }

    //@Override
    protected void draw(Graphics2D g2) {
        GeneralPath path = new GeneralPath();

        int idx, cnt = shapePoints.size();
        for (idx = 0; idx < cnt; idx++) {
            // this point
            LayoutShapePoint lsp = shapePoints.get(idx);
            Point2D p = lsp.getPoint();

            // left point
            int idxL = (idx + cnt - 1) % cnt;
            LayoutShapePoint lspL = shapePoints.get(idxL);
            Point2D pL = lspL.getPoint();
            Point2D midL = MathUtil.midPoint(pL, p);

            // right point
            int idxR = (idx + 1) % cnt;
            LayoutShapePoint lspR = shapePoints.get(idxR);
            Point2D pR = lspR.getPoint();
            Point2D midR = MathUtil.midPoint(p, pR);

            // if this is an open shape...
            LayoutShapePointType lspt = lsp.getType();
            if (getType() == LayoutShapeType.Open) {
                // and this is first or last point...
                if ((idx == 0) || (idxR == 0)) {
                    // then force straight shape point type
                    lspt = LayoutShapePointType.Straight;
                }
            }
            switch (lspt) {
                case Straight: {
                    if (idx == 0) { // if this is the first point...
                        // ...and our shape is open...
                        if (getType() == LayoutShapeType.Open) {
                            path.moveTo(p.getX(), p.getY());    // then start here
                        } else {    // otherwise
                            path.moveTo(midL.getX(), midL.getY());  // start here
                            path.lineTo(p.getX(), p.getY());        // draw to here
                        }
                    } else {
                        path.lineTo(midL.getX(), midL.getY());  // start here
                        path.lineTo(p.getX(), p.getY());        // draw to here
                    }
                    // if this is not the last point...
                    // ...or our shape isn't open
                    if ((idxR != 0) || (getType() != LayoutShapeType.Open)) {
                        path.lineTo(midR.getX(), midR.getY());      // draw to here
                    }
                    break;
                }

                case Curve: {
                    if (idx == 0) { // if this is the first point
                        path.moveTo(midL.getX(), midL.getY());  // then start here
                    }
                    path.quadTo(p.getX(), p.getY(), midR.getX(), midR.getY());
                    break;
                }

                default:
                    log.error("unexpected enum member!");
            }
        }   // for (idx = 0; idx < cnt; idx++)

        if (getType() == LayoutShapeType.Filled) {
            g2.setColor(fillColor);
            g2.fill(path);
        }
        g2.setStroke(new BasicStroke(lineWidth,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(lineColor);
        g2.draw(path);
    }   // draw

    protected void drawEditControls(Graphics2D g2) {
        Color backgroundColor = layoutEditor.getBackground();
        Color controlsColor = ColorUtil.contrast(backgroundColor);
        controlsColor = ColorUtil.setAlpha(controlsColor, 0.5);
        g2.setColor(controlsColor);

        shapePoints.forEach((slp) -> g2.draw(layoutEditor.layoutEditorControlRectAt(slp.getPoint())));
        if (shapePoints.size() > 0) {
            Point2D end0 = shapePoints.get(0).getPoint();
            Point2D end1 = end0;
            for (LayoutShapePoint lsp : shapePoints) {
                Point2D end2 = lsp.getPoint();
                g2.draw(new Line2D.Double(end1, end2));
                end1 = end2;
            }

            if (getType() != LayoutShapeType.Open) {
                g2.draw(new Line2D.Double(end1, end0));
            }
        }

        g2.draw(trackEditControlCircleAt(getCoordsCenter()));
    }   // drawEditControls

    // these are convenience methods to return circles used to draw onscreen
    //
    // compute the control point rect at inPoint; use the turnout circle size
    public Ellipse2D trackEditControlCircleAt(@Nonnull Point2D inPoint) {
        return trackControlCircleAt(inPoint);
    }

    // compute the turnout circle at inPoint (used for drawing)
    public Ellipse2D trackControlCircleAt(@Nonnull Point2D inPoint) {
        return new Ellipse2D.Double(inPoint.getX() - layoutEditor.circleRadius,
                inPoint.getY() - layoutEditor.circleRadius,
                layoutEditor.circleDiameter, layoutEditor.circleDiameter);
    }

    /**
     * These are the points that make up the outline of the shape. Each point
     * can be ether a straight or a control point for a curve
     */
    public static class LayoutShapePoint {

        private LayoutShapePointType type;
        private Point2D point;

        /**
         * constructor method
         *
         * @param c Point2D for initial point
         */
        public LayoutShapePoint(Point2D c) {
            this.type = LayoutShapePointType.Straight;
            this.point = c;
        }

        /**
         * Constructor method.
         *
         * @param t the layout shape point type.
         * @param c Point2D for initial point
         */
        public LayoutShapePoint(LayoutShapePointType t, Point2D c) {
            this(c);
            this.type = t;
        }

        /**
         * accessor methods
         *
         * @return the LayoutShapePointType
         */
        public LayoutShapePointType getType() {
            return type;
        }

        public void setType(LayoutShapePointType type) {
            this.type = type;
        }

        public Point2D getPoint() {
            return point;
        }

        public void setPoint(Point2D point) {
            this.point = point;
        }
    }   // class LayoutShapePoint

    /**
     * enum LayoutShapeType
     */
    public enum LayoutShapeType {
        Open, 
        Closed,
        Filled;
    }

    /**
     * enum LayoutShapePointType Straight, Curve
     */
    public enum LayoutShapePointType {
        Straight,
        Curve;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutShape.class);
}
