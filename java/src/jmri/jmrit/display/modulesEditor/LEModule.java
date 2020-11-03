/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.modulesEditor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.display.layoutEditor.PositionablePoint.PointType;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.PointType.EDGE_CONNECTOR;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.MathUtil;

import static org.apache.log4j.NDC.remove;

/**
 *
 * @author geowar
 */
public class LEModule {

    private final ModulesEditor modulesEditor;
    private final LayoutEditor layoutEditor;

    public LEModule(ModulesEditor modulesEditor, LayoutEditor layoutEditor) {
        this.modulesEditor = modulesEditor;
        this.layoutEditor = layoutEditor;
    }

    public ModulesEditor getModulesEditor() {
        return modulesEditor;
    }

    public LayoutEditor getLayoutEditor() {
        return layoutEditor;
    }

    public String getName() {
        return getLayoutEditor().getName();
    }
    /*
     * accessors for location
     */
    private Point2D location = MathUtil.zeroPoint2D();

    public Point2D getLocation() {
        return location;
    }

    public void setLocation(Point2D location) {
        this.location = location;
    }

    /*
     * accessors for rotation
     */
    private double rotationRAD = Math.toRadians(15.0);

    public double getRotationDEG() {
        return Math.toDegrees(rotationRAD);
    }

    public void setRotationDEG(double rotationDEG) {
        rotationRAD = Math.toRadians(rotationDEG);
    }

    public double getRotationRAD() {
        return rotationRAD;
    }

    public void setRotationRAD(double rotationRAD) {
        this.rotationRAD = rotationRAD;
    }

    /**
     * @return this modules outline note:also sets outlineOffset
     */
    public List<Point2D> getOutline() {
        List<Point2D> results = new ArrayList<>();

        // add tracks
        for (LayoutTrackView layoutTrackView : getLayoutEditor().getLayoutTrackViews()) {
            Rectangle2D r = layoutTrackView.getBounds();
            results.add(new Point2D.Double(r.getMinX(), r.getMinY()));
            results.add(new Point2D.Double(r.getMinX(), r.getMaxY()));
            results.add(new Point2D.Double(r.getMaxX(), r.getMinY()));
            results.add(new Point2D.Double(r.getMaxX(), r.getMaxY()));
        }
        // add shapes
        for (LayoutShape layoutShape : getLayoutEditor().getLayoutShapes()) {
            Rectangle2D r = layoutShape.getBounds();
            results.add(new Point2D.Double(r.getMinX(), r.getMinY()));
            results.add(new Point2D.Double(r.getMinX(), r.getMaxY()));
            results.add(new Point2D.Double(r.getMaxX(), r.getMinY()));
            results.add(new Point2D.Double(r.getMaxX(), r.getMaxY()));
        }
        // convert to convex polygon outline
        results = MathUtil.convexHull(results);

        // re-add the orginal point to close the path
        if (!results.isEmpty()) {
            results.add(results.get(0));
        }

        // move to origin, rotate and move to location
        outlineOffset = MathUtil.midPoint(results);
        for (int i = 0; i < results.size(); i++) {
            Point2D p = results.get(i);
            p = MathUtil.subtract(p, outlineOffset);
            p = MathUtil.rotateRAD(p, rotationRAD);
            p = MathUtil.add(p, location);
            results.set(i, p);
        }

        return results;
    }   // getOutline

    private Point2D outlineOffset = MathUtil.zeroPoint2D();

    public Point2D getOutlineOffset() {
        if (MathUtil.equals(outlineOffset, MathUtil.zeroPoint2D)) {
            getOutline();
        }
        return outlineOffset;
    }

    public Rectangle2D getBounds() {
        List<Point2D> points = getOutline();
        Point2D point0 = points.get(0);
        Rectangle2D result = new Rectangle2D.Double(point0.getX(), point0.getY(), 0, 0);
        for (Point2D point : points) {
            result.add(point);
        }
        return result;
    }

    /**
     * draw this module
     *
     * @param g2 the graphics context
     */
    public void draw(Graphics2D g2) {
        BasicStroke thin = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke wide = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        // save original transform
        AffineTransform saveAT = g2.getTransform();

        g2.setStroke(thin);
        g2.setColor(Color.lightGray);
        g2.draw(getBounds());

        List<Point2D> points = getOutline();

        g2.setStroke(wide);
        GeneralPath path = new GeneralPath();
        for (Point2D point : points) {
            if (path.getCurrentPoint() == null) {
                path.moveTo(point.getX(), point.getY());
            } else {
                path.lineTo(point.getX(), point.getY());
            }
        }
        g2.setColor(Color.blue);
        g2.draw(path);

        // Perform transformation to draw layout tracks
        g2.translate(location.getX(), location.getY());
        g2.rotate(rotationRAD);
        g2.translate(-outlineOffset.getX(), -outlineOffset.getY());

        getLayoutEditor().getLayoutTrackViews().forEach((tsv) -> {
            g2.setColor(Color.darkGray);
            LayoutTrack lt = tsv.getLayoutTrack();
            if (lt instanceof PositionablePoint) {
                PositionablePoint pp = (PositionablePoint) lt;
                if (pp.getType() == EDGE_CONNECTOR) {
//                    Point2D c = tsv.getCoordsCenter();
//                    g2.drawLine((int) c.getX() - 3, (int) c.getY() - 3, (int) c.getX() + 3, (int) c.getY() + 3);
//                    g2.drawLine((int) c.getX() - 3, (int) c.getY() + 3, (int) c.getX() + 3, (int) c.getY() - 3);
                    tsv.drawEditControlsPublic(g2);
                }
            } else if (lt.isMainline()) {
                g2.setStroke(wide);
                tsv.drawOne(g2, true, false);
            } else {
                g2.setStroke(thin);
                tsv.drawOne(g2, false, false);
            }
        });

//        g2.setColor(Color.red);
//        g2.fillOval((int) center.getX() - 3, (int) center.getY() - 3, 7, 7);
//
//        g2.setColor(Color.magenta);
//        g2.fillOval((int) outlineOffset.getX() - 3, (int) outlineOffset.getY() - 3, 7, 7);
        // Restore original transform
        g2.setTransform(saveAT);

        g2.setColor(Color.green);
        g2.drawOval((int) location.getX() - 5, (int) location.getY() - 5, 11, 11);
    }

    private JPopupMenu popupMenu = null;

    @Nonnull
    protected JPopupMenu showPopup(@Nonnull MouseEvent mouseEvent) {
        if (popupMenu != null) {
            popupMenu.removeAll();
        } else {
            popupMenu = new JPopupMenu();
        }

        JMenuItem jmi = popupMenu.add(
                Bundle.getMessage("MakeLabel", Bundle.getMessage("Module"))
                + getName());
        jmi.setEnabled(false);

        // if there are any edge connections…
        boolean youveGotToKeepThemSeparated = false;
        for (LayoutTrackView layoutTrackView : getLayoutEditor().getLayoutTrackViews()) {
            if (layoutTrackView instanceof PositionablePointView) {
                PositionablePointView ppv = (PositionablePointView) layoutTrackView;
                if ((ppv.getType() == PointType.EDGE_CONNECTOR) && (ppv.getLinkedPoint() != null)) {
                    if (!youveGotToKeepThemSeparated) {
                        popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));
                        youveGotToKeepThemSeparated = true;
                    }
                    String linkName = ppv.getLinkedPointId();
                    if (!getLayoutEditor().equals(ppv.getLinkedEditor())) {
                        linkName = Bundle.getMessage("MakeLabel", ppv.getLinkedEditorName()) + linkName;
                    }

                    jmi = popupMenu.add(ppv.getName() + " " + Bundle.getMessage("LinkedToX_", linkName));
                    jmi.setEnabled(false);
                }
            }
        }
        if (youveGotToKeepThemSeparated) {
            popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        }
        JMenuItem rotateItem = new JMenuItem(Bundle.getMessage("MakeLabel", Bundle.getMessage("Rotation"))
                + Bundle.getMessage("Rotation_", (int) Math.round(getRotationDEG())));
        rotateItem.addActionListener((ActionEvent event) -> {
            // prompt for rotation angle
            JOptionPane optionPane = new JOptionPane();
            DefaultBoundedRangeModel model = new DefaultBoundedRangeModel((int) getRotationDEG(), 0, 0, 360);
            JSlider slider = new JSlider(model);

            // make it BIGGER!
            Dimension d = slider.getPreferredSize();
            d.setSize(360 + 32, d.getHeight() * 2);
            slider.setPreferredSize(d);

            slider.setMajorTickSpacing(15);
            slider.setPaintTicks(true);

            // Set the labels to be painted on the slider
            slider.setPaintLabels(true);
            // Set the labels to be drawn
            slider.setLabelTable(slider.createStandardLabels(30));

            slider.setValueIsAdjusting(true);
            JLabel rotationLabel = new JLabel(Bundle.getMessage("SetRotation_", (int) Math.round(getRotationDEG())));
            optionPane.setMessage(new Object[]{rotationLabel, slider});
            optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
            optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);

            slider.addChangeListener(changeEvent -> {
                JSlider theSlider = (JSlider) changeEvent.getSource();
                //if (!theSlider.getValueIsAdjusting()) {
                int value = theSlider.getValue();
                optionPane.setInputValue(value);
                setRotationDEG(value);
                getModulesEditor().repaint();
                rotationLabel.setText(Bundle.getMessage("SetRotation_", value));
                //}
            });

            JDialog dialog = optionPane.createDialog(getModulesEditor(), Bundle.getMessage("SetRotation"));
            dialog.setVisible(true);

//                if (rot != 0.0) {
//                    rotateCoords(rot);
//                    getModulesEditor().redrawPanel();
//                }
        });
        popupMenu.add(rotateItem);

        popupMenu.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                //break links before removing
                for (LayoutTrackView layoutTrackView : getLayoutEditor().getLayoutTrackViews()) {
                    if (layoutTrackView instanceof PositionablePointView) {
                        PositionablePointView ppv = (PositionablePointView) layoutTrackView;
                        PointType pt = ppv.getType();
                        if (pt == PointType.EDGE_CONNECTOR && (ppv.getLinkedPoint() != null)) {
                            ppv.removeLinkedPoint();
                        }
                    }
                }

                remove();
                if (popupMenu != null) {
                    popupMenu.removeAll();
                }
                popupMenu = null;
            }
        });

        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());

        return popupMenu;
    }   // showPopup

    // initialize logging
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LEModule.class);
}
