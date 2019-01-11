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

    public enum LayoutShapeType {
        eOpen("Open"), eClosed("Closed"), eFilled("Filled");

        private transient String name;
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

    public enum LayoutShapePointType {
        eVertex("Vertex"), eCurve("Curve");

        private transient String name;
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
     * These are the points that make up the outline of the shape
     * Each point can be ether a vertex or a control point for a curve
     */
    public static class LayoutShapePoint {

        private transient LayoutShapePointType type = LayoutShapePointType.eVertex;
        private transient Point2D point;

        /**
         * constructor method
         */
        public LayoutShapePoint(Point2D c) {
            this.point = c;
            this.type = LayoutShapePointType.eVertex;
        }

        /**
         * accessor methods
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
    private LayoutShapeType layoutShapeType = LayoutShapeType.eOpen;
    private int level = 3;
    private int lineWidth = 3;
    private Color lineColor = Color.BLACK;
    private Color fillColor = Color.DARK_GRAY;

    // these are saved
    private ArrayList<LayoutShapePoint> shapePoints = new ArrayList<>(); // list of LayoutShapePoints

    /**
     * constructor method
     */
    public LayoutShape(String name, Point2D c, LayoutEditor layoutEditor) {
        this.name = name;
        this.shapePoints.add(new LayoutShapePoint(c));
        this.layoutEditor = layoutEditor;
    }

    // this should only be used for debugging...
    public String toString() {
        return String.format("LayoutShape %s", name);
    }

    public String getDisplayName() {
        return String.format("%s %s", Bundle.getMessage("LayoutShape"), name);
    }

    /**
     * accessor methods
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
                default:
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

        for (LayoutShapePoint lsp : shapePoints) {
            result.add(lsp.getPoint());
        }
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
        return MathUtil.midPoint(getBounds());
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
            for (LayoutShapePoint lsp : shapePoints) {
                lsp.setPoint(MathUtil.add(factor, lsp.getPoint()));
            }
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
        for (LayoutShapePoint lsp : shapePoints) {
            lsp.setPoint(MathUtil.multiply(lsp.getPoint(), factor));
        }
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
        for (LayoutShapePoint lsp : shapePoints) {
            lsp.setPoint(MathUtil.add(factor, lsp.getPoint()));
        }
    }

    private JPopupMenu popup = null;

    /**
     * {@inheritDoc}
     */
    //@Override
    @Nonnull
    protected JPopupMenu showPopup(@Nullable MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (layoutEditor.isEditable()) {

            JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutShape")) + getName());
            jmi.setEnabled(false);

            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            //TODO: add menu items to display/change layoutShapeType, level, 
            // lineWidth, lineColor and fillColor.
            //TODO: add menu items to display/change shapePoint LayoutShapePointType.
//
//            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
//            hiddenCheckBoxMenuItem.setSelected(hidden);
//            popup.add(hiddenCheckBoxMenuItem);
//            hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e1) -> {
//                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e1.getSource();
//                setHidden(o.isSelected());
//            });
//            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    layoutEditor.getLayoutTrackEditors().editLayoutShape(LayoutShape.this);
//                }
//            });
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
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
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
        //TODO: I think this would work better as a Bezier curve for eCurve points
        GeneralPath path = new GeneralPath();
        int idx, cnt = shapePoints.size();
        for (idx = 0; idx < cnt; idx++) {
            LayoutShapePoint lsp = shapePoints.get(idx);
            Point2D p = lsp.getPoint();
            if (idx == 0) {
                path.moveTo(p.getX(), p.getY());
            } else {
                if (lsp.getType() == LayoutShapePointType.eVertex) {
                    path.lineTo(p.getX(), p.getY());
                } else if (lsp.getType() == LayoutShapePointType.eCurve) {
                    LayoutShapePoint lsp1 = shapePoints.get((idx + 1) % cnt);
                    Point2D p1 = lsp1.getPoint();
                    path.quadTo(p.getX(), p.getY(), p1.getX(), p1.getY());
                    idx++;
                }
            }
        }
        if (getType() != LayoutShapeType.eOpen) {
            LayoutShapePoint lsp = shapePoints.get(0);
            Point2D p = lsp.getPoint();
            path.lineTo(p.getX(), p.getY());
        }
        if (getType() == LayoutShapeType.eFilled) {
            g2.setColor(fillColor);
            g2.fill(path);
        }
        g2.setColor(lineColor);
        g2.draw(path);
    }   // draw

    protected void drawEditControls(Graphics2D g2) {
        g2.setColor(Color.black);

        for (LayoutShapePoint slp : shapePoints) {
            g2.draw(layoutEditor.trackEditControlRectAt(slp.getPoint()));
        }
        Point2D end1 = shapePoints.get(0).getPoint();
        for (LayoutShapePoint slp : shapePoints) {
            Point2D end2 = slp.getPoint();
            if (slp.getType() == LayoutShapePointType.eCurve) {
                g2.draw(new Line2D.Double(end1, end2));
            }
            end1 = end2;
        }
        g2.draw(layoutEditor.trackEditControlCircleAt(getCoordsCenter()));
    }   // drawEditControls

    private final static Logger log = LoggerFactory.getLogger(LayoutShape.class
    );

}   // class LayoutShape
