package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutShape is a set of LayoutShapePoint used to draw a shape. Each point
 * can ether be a point on the shape or a control point that defines a curve
 * that's part of the shape. The shape can be open (end points not connected) or
 * closed (end points connected)
 *
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutShape {

    /**
     * enum LayoutShapeType eOpen, eClosed, eFilled
     */
    public enum LayoutShapeType {
        eOpen("Open"), eClosed("Closed"), eFilled("Filled");

        private final transient String name;
        private transient static final Map<String, LayoutShapeType> ENUM_MAP;

        LayoutShapeType(String name) {
            this.name = name;
        }

        //Build an immutable map of String name to enum pairs.
        static {
            Map<String, LayoutShapeType> map = new ConcurrentHashMap<>();

            for (LayoutShapeType instance : LayoutShapeType.values()) {
                map.put(instance.getName(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static LayoutShapeType getName(@Nullable String name) {
            return ENUM_MAP.get(name);
        }

        public String getName() {
            return name;
        }
    }

    /**
     * enum LayoutShapePointType eVertex, eCurve
     */
    public enum LayoutShapePointType {
        eVertex("Vertex"), eCurve("Curve");

        private final transient String name;
        private static final transient Map<String, LayoutShapePointType> ENUM_MAP;

        LayoutShapePointType(String name) {
            this.name = name;
        }

        //Build an immutable map of String name to enum pairs.
        static {
            Map<String, LayoutShapePointType> map = new ConcurrentHashMap<>();
            for (LayoutShapePointType instance : LayoutShapePointType.values()) {
                map.put(instance.getName(), instance);
            }
            ENUM_MAP = Collections.unmodifiableMap(map);
        }

        public static LayoutShapePointType getName(@Nullable String name) {
            return ENUM_MAP.get(name);
        }

        public String getName() {
            return name;
        }
    } // enum LayoutShapePointType

    /**
     * These are the points that make up the outline of the shape. Each point
     * can be ether a vertex or a control point for a curve
     */
    public static class LayoutShapePoint {

        private transient LayoutShapePointType type;
        private transient Point2D point;

        /**
         * constructor method
         *
         * @param c Point2D for initial point
         */
        public LayoutShapePoint(Point2D c) {
            this.point = c;
            this.type = LayoutShapePointType.eVertex;
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

    // operational instance variables (not saved between sessions)
    private LayoutEditor layoutEditor = null;
    private String name = "";
    private LayoutShapeType layoutShapeType;
    private int level = 3;
    private int lineWidth = 3;
    private Color lineColor = Color.BLACK;
    private Color fillColor = Color.DARK_GRAY;

    // these are saved
    // list of LayoutShapePoints
    private final ArrayList<LayoutShapePoint> shapePoints;

    /**
     * constructor method
     *
     * @param name         the name of the shape
     * @param c            the Point2D for the initial point
     * @param layoutEditor reference to the LayoutEditor this shape is in
     */
    public LayoutShape(String name, Point2D c, LayoutEditor layoutEditor) {
        this.layoutShapeType = LayoutShapeType.eOpen;
        this.shapePoints = new ArrayList<>();
        this.name = name;
        this.shapePoints.add(new LayoutShapePoint(c));
        this.layoutEditor = layoutEditor;
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

    public LayoutShapeType getType() {
        return layoutShapeType;
    }

    public void setType(LayoutShapeType t) {
        if (layoutShapeType != t) {
            switch (t) {
                case eOpen:
                case eClosed:
                case eFilled:
                    layoutShapeType = t;
                    break;
                default:    // You shouldn't ever have any invalid LayoutShapeTypes
                    log.error("Invalid Shape Type " + t); //I18IN
            }
        }
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int w) {
        lineWidth = w;
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
        level = l;
    }

    public void addPoint(Point2D p) {
        if (shapePoints.size() < getMaxNumberPoints()) {
            shapePoints.add(new LayoutShapePoint(p));
        }
    }

    public void setPoint(Point2D p, int idx) {
        if (idx < shapePoints.size()) {
            shapePoints.get(idx).setPoint(p);
        }
    }

    // should only be used by xml save code
    protected ArrayList<LayoutShapePoint> getPointList() {
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
        return LayoutTrack.SHAPE_POINT_OFFSET_MAX - LayoutTrack.SHAPE_POINT_OFFSET_MIN + 1;
    }

    /**
     * getBounds() - return the bounds of this shape
     *
     * @return Rectangle2D as bound of this shape
     */
    public Rectangle2D getBounds() {
        Rectangle2D result = MathUtil.rectangleAtPoint(shapePoints.get(0).getPoint(), 1.0, 1.0);

        shapePoints.forEach((lsp) -> {
            result.add(lsp.getPoint());
        });
        return result;
    }

    /**
     * return the hit point type for the shape at this location
     *
     * @param hitPoint
     * @param useRectangles
     * @return int - hit point type
     */
    protected int findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles) {
        int result = LayoutTrack.NONE;  // assume point not on shape

        if (useRectangles) {
            // rather than create rectangles for all the points below and
            // see if the passed in point is in one of those rectangles
            // we can create a rectangle for the passed in point and then
            // test if any of the points below are in that rectangle instead.
            Rectangle2D r = layoutEditor.trackEditControlRectAt(hitPoint);

            if (r.contains(getCoordsCenter())) {
                result = LayoutTrack.SHAPE_CENTER;
            }
            for (int idx = 0; idx < shapePoints.size(); idx++) {
                if (r.contains(shapePoints.get(idx).getPoint())) {
                    result = LayoutTrack.SHAPE_POINT_OFFSET_MIN + idx;
                    break;
                }
            }
        } else {
            double distance, minDistance = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
            for (int idx = 0; idx < shapePoints.size(); idx++) {
                distance = MathUtil.distance(shapePoints.get(idx).getPoint(), hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    result = LayoutTrack.SHAPE_POINT_OFFSET_MIN + idx;
                }
            }
        }
        return result;
    }   // findHitPointType

    public static boolean isShapeHitPointType(int t) {
        return ((t == LayoutTrack.SHAPE_CENTER)
                || isShapePointOffsetHitPointType(t));
    }

    public static boolean isShapePointOffsetHitPointType(int t) {
        return ((t >= LayoutTrack.SHAPE_POINT_OFFSET_MIN)
                && (t <= LayoutTrack.SHAPE_POINT_OFFSET_MAX));
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
            shapePoints.forEach((lsp) -> {
                lsp.setPoint(MathUtil.add(factor, lsp.getPoint()));
            });
        }
    }

    /**
     * scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
//    @Override
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        shapePoints.forEach((lsp) -> {
            lsp.setPoint(MathUtil.multiply(lsp.getPoint(), factor));
        });
    }

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
//    @Override
    public void translateCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        shapePoints.forEach((lsp) -> {
            lsp.setPoint(MathUtil.add(factor, lsp.getPoint()));
        });
    }

    private JPopupMenu popup = null;

    /**
     * {@inheritDoc}
     */
    //@Override
    @Nonnull
    protected JPopupMenu showShapePopUp(@Nullable MouseEvent mouseEvent, int hitPointType) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (layoutEditor.isEditable()) {

            JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutShape")) + getName());
            jmi.setEnabled(false);

            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            if (true) { // only enable for debugging; TODO: delete or disable this for production
                jmi = popup.add("hitPointType: " + hitPointType);
                jmi.setEnabled(false);
            }

            // add "Change Shape Type to..." menu
            JMenu shapeTypeMenu = new JMenu(Bundle.getMessage("ChangeShapeTypeTo"));
            jmi = shapeTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapeTypeOpen")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setType(LayoutShapeType.eOpen);
                    layoutEditor.repaint();
                }
            }));
            jmi.setSelected(getType() == LayoutShapeType.eOpen);

            jmi = shapeTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapeTypeClosed")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setType(LayoutShapeType.eClosed);
                    layoutEditor.repaint();
                }
            }));
            jmi.setSelected(getType() == LayoutShapeType.eClosed);

            jmi = shapeTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapeTypeFilled")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setType(LayoutShapeType.eFilled);
                    layoutEditor.repaint();
                }
            }));
            jmi.setSelected(getType() == LayoutShapeType.eFilled);

            popup.add(shapeTypeMenu);

            // Add "Change Shape Point Type to..." menu
            if (hitPointType != LayoutTrack.SHAPE_CENTER) {
                int pointIndex = hitPointType - LayoutTrack.SHAPE_POINT_OFFSET_MIN;
                LayoutShapePoint lsp = shapePoints.get(pointIndex);

                if (lsp != null) { // this should never happen... but just in case...
                    JMenu shapePointTypeMenu = new JMenu(Bundle.getMessage("ChangeShapePointTypeTo"));
                    jmi = shapePointTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapePointTypeVertex")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            lsp.setType(LayoutShapePointType.eVertex);
                            layoutEditor.repaint();
                        }
                    }));
                    jmi.setSelected(lsp.getType() == LayoutShapePointType.eVertex);

                    jmi = shapePointTypeMenu.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("ShapePointTypeCurve")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            lsp.setType(LayoutShapePointType.eCurve);
                            layoutEditor.repaint();
                        }
                    }));
                    jmi.setSelected(lsp.getType() == LayoutShapePointType.eCurve);

                    popup.add(shapePointTypeMenu);
                }
            }
            //TODO: add menu items to display/change level, lineWidth, lineColor and fillColor.

            popup.add(new JSeparator(JSeparator.HORIZONTAL));
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
            if (getType() == LayoutShapeType.eOpen) {
                // and this is first or last point...
                if ((idx == 0) || (idxR == 0)) {
                    // then force vertext shape point type
                    lspt = LayoutShapePointType.eVertex;
                }
            }
            switch (lspt) {
                case eVertex: {
                    if (idx == 0) { // if this is the first point...
                        // ...and our shape is open...
                        if (getType() == LayoutShapeType.eOpen) {
                            path.moveTo(p.getX(), p.getY());    // then start here
                        } else {    // otherwise
                            path.moveTo(midL.getX(), midL.getY());  //start here
                            path.lineTo(p.getX(), p.getY());        //draw to here
                        }
                    } else {
                        path.lineTo(midL.getX(), midL.getY());  //start here
                        path.lineTo(p.getX(), p.getY());        //draw to here
                    }
                    // if this is not the last point...
                    // ...or our shape isn't open
                    if ((idxR != 0) || (getType() != LayoutShapeType.eOpen)) {
                        path.lineTo(midR.getX(), midR.getY());      // draw to here
                    }
                    break;
                }

                case eCurve: {
                    if (idx == 0) { // if this is the first point
                        path.moveTo(midL.getX(), midL.getY());  // then start here
                    }
                    path.quadTo(p.getX(), p.getY(), midR.getX(), midR.getY());
                    break;
                }
            }
        }   // for (idx = 0; idx < cnt; idx++)

        if (getType() == LayoutShapeType.eFilled) {
            g2.setColor(fillColor);
            g2.fill(path);
        }

        g2.setColor(lineColor);
        g2.draw(path);
    }   // draw

    protected void drawEditControls(Graphics2D g2) {
        g2.setColor(Color.black);

        shapePoints.forEach((slp) -> {
            g2.draw(layoutEditor.trackEditControlRectAt(slp.getPoint()));
        });
        Point2D end0 = shapePoints.get(0).getPoint();
        Point2D end1 = end0;
        for (LayoutShapePoint lsp : shapePoints) {
            Point2D end2 = lsp.getPoint();
            g2.draw(new Line2D.Double(end1, end2));
            end1 = end2;
        }

        if (getType() != LayoutShapeType.eOpen) {
            g2.draw(new Line2D.Double(end1, end0));
        }

        g2.draw(layoutEditor.trackEditControlCircleAt(getCoordsCenter()));
    }   // drawEditControls

    private final static Logger log = LoggerFactory.getLogger(LayoutShape.class
    );

}   // class LayoutShape
