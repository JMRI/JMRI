package jmri.jmrix.rps.trackingpanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Model;
import jmri.jmrix.rps.Receiver;
import jmri.jmrix.rps.Region;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Pane to show a 2D representation of the RPS Model and Measurements.
 * <p>
 * @see jmri.jmrix.rps.Model
 * @see jmri.jmrix.rps.Measurement
 *
 * @author	Bob Jacobsen Copyright (C) 2006, 2008
 */
public class RpsTrackingPanel extends javax.swing.JPanel
        implements MeasurementListener {

    RpsSystemConnectionMemo memo = null;

    public RpsTrackingPanel(RpsSystemConnectionMemo _memo) {
        super();
        memo = _memo;
        Distributor.instance().addMeasurementListener(this);
        setToolTipText("<no item>");  // activates ToolTip, sets default
    }

    public void dispose() {
        Distributor.instance().removeMeasurementListener(this);
    }

    /**
     * Provide tool tip text that depends on what's under the cursor.
     * <p>
     * Names either a measurement point or a region.
     *
     * @return null if no object under mouse; this suppresses ToolTip
     */
    @Override
    public String getToolTipText(MouseEvent e) {
        // get mouse coordinates
        try {
            Point mouse = e.getPoint();
            Point2D userPt = currentAT.inverseTransform(new Point2D.Double(mouse.x, mouse.y), null);
            // find the path object containing it, if any
            for (int i = measurementRepList.size() - 1; i >= 0; i--) {
                MeasurementRep r = measurementRepList.get(i);
                if (r.contains(userPt)) {
                    Measurement m = r.measurement;
                    return "ID " + m.getId() + " at " + m.getX() + "," + m.getY();
                }
            }

            // find the region containing it, if any
            // Go through backwards to find the top if overlaps
            List<Region> l = Model.instance().getRegions();
            for (int i = l.size() - 1; i >= 0; i--) {
                Shape s = l.get(i).getPath();
                if (s.contains(userPt)) {
                    return "Region: " + l.get(i).toString() + ", at " + userPt.getX() + "," + userPt.getY();
                }
            }
            // found nothing, just display location
            return "" + userPt.getX() + "," + userPt.getY();
        } catch (Exception ex) {
        } // just skip to default
        // or return default
        return null;
    }

    /**
     * Sets the coordinates of the lower left corner of the screen/paper. Note
     * this is different from the usual Swing coordinate system!
     */
    public void setOrigin(double x, double y) {
        xorigin = x;
        yorigin = y;
    }

    void setShowErrors(boolean show) {
        this.showErrors = show;
    }

    void setShowReceivers(boolean show) {
        this.showReceivers = show;
    }

    void setShowRegions(boolean show) {
        this.showRegions = show;
    }

    boolean showErrors = false;
    boolean showReceivers = false;
    boolean showRegions = false;

    /**
     * Sets the coordinates of the upper-right corner of the screen/paper. Note
     * this is different from the usual Swing coordinate system!
     */
    public void setCoordMax(double x, double y) {
        xmax = x;
        ymax = y;
    }

    double xorigin, yorigin;
    double xmax, ymax;

    static final double MEASUREMENT_ACCURACY = 0.2; // in user units
    static final double RECEIVER_SIZE = 0.75; // in user units
    static final Color regionFillColor = Color.GRAY.brighter();
    static final Color regionOutlineColor = Color.GRAY.darker();
    // static final Color measurementColor = new Color(0,0,0);
    int measurementColor = 0;

    // current transform to graphics coordinates
    AffineTransform currentAT;

    @Override
    public void paint(Graphics g) {
        // draw everything else
        super.paint(g);
        log.debug("paint invoked");

        // Now show regions
        // First, Graphics2D setup
        Graphics2D g2 = (Graphics2D) g;
        double xscale = this.getWidth() / (xmax - xorigin);
        double yscale = this.getHeight() / (ymax - yorigin);
        Stroke stroke = new BasicStroke((float) (2. / xscale),
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);
        // Save the current transform
        AffineTransform saveAT = g2.getTransform();
        // Install the new one
        currentAT = new AffineTransform();
        currentAT.translate(0, this.getHeight());
        currentAT.scale(xscale, -yscale);
        currentAT.translate(-xorigin, -yorigin);  // put origin in bottom corner
        g2.setTransform(currentAT);

        if (showRegions) {
            // Draw the regions
            List<Region> l = Model.instance().getRegions();
            for (int i = 0; i < l.size(); i++) {
                g2.setPaint(regionOutlineColor);
                g2.draw(l.get(i).getPath()); // border (same color)
                g2.setPaint(regionFillColor);
                g2.fill(l.get(i).getPath());
            }
        }

        // Draw the measurements; changes graphics
        for (int i = 0; i < measurementRepList.size(); i++) {
            measurementRepList.get(i).draw(g2);
        }
        if (showReceivers) { // draw receivers
            for (int i = 1; i < Engine.instance().getMaxReceiverNumber() + 1; i++) {  // indexed from 1
                Receiver r = Engine.instance().getReceiver(i);
                Point3d p = Engine.instance().getReceiverPosition(i);
                if (p != null && r != null) {
                    if (r.isActive()) {
                        g2.setPaint(Color.BLACK);
                    } else {
                        g2.setPaint(Color.GRAY);
                    }

                    Shape s = new Ellipse2D.Double(p.x - RECEIVER_SIZE / 2,
                            p.y - RECEIVER_SIZE / 2,
                            RECEIVER_SIZE, RECEIVER_SIZE);
                    g2.draw(s);
                    g2.fill(s);
                }
            }
        }
        // restore original transform
        g2.setTransform(saveAT);
    }

    ArrayList<MeasurementRep> measurementRepList = new ArrayList<MeasurementRep>();
    java.util.HashMap<String, TransmitterStatus> transmitters = new java.util.HashMap<String, TransmitterStatus>(); // TransmitterStatus, keyed by Integer(measurement id)

    /**
     * Pick a color for the next set of measurement lines to draw
     */
    Color nextColor() {
        int red = Math.min(255, ((measurementColor >> 2) & 0x1) * 255 / 1);
        int green = Math.min(255, ((measurementColor >> 1) & 0x1) * 255 / 1);
        int blue = Math.min(255, ((measurementColor >> 0) & 0x1) * 255 / 1);
        measurementColor++;
        return new Color(red, green, blue);
    }

    @Override
    public void notify(Measurement m) {
        String id = m.getId();
        TransmitterStatus transmitter = transmitters.get(id);
        double xend = m.getX();
        double yend = m.getY();
        if (log.isDebugEnabled()) {
            log.debug("notify " + xend + "," + yend);
        }
        if (transmitter == null) {
            // create Transmitter status with current measurement
            // so we can draw line next time
            log.debug("create new TransmitterStatus for " + m.getId());
            transmitter = new TransmitterStatus();
            transmitter.measurement = m;
            transmitter.color = nextColor();

            transmitters.put(id, transmitter);

            // display just the point
            MeasurementRep r = new MeasurementRep();
            r.color = transmitter.color;
            r.rep1 = new Ellipse2D.Double(xend - MEASUREMENT_ACCURACY / 2,
                    yend - MEASUREMENT_ACCURACY / 2,
                    MEASUREMENT_ACCURACY, MEASUREMENT_ACCURACY);
            r.measurement = m;
            measurementRepList.add(r);
            pruneMeasurementRepList();

            return;
        }
        Measurement lastMessage = transmitter.measurement;

        MeasurementRep r = new MeasurementRep();
        r.color = transmitter.color;
        r.rep2 = new Ellipse2D.Double(xend - MEASUREMENT_ACCURACY / 2,
                yend - MEASUREMENT_ACCURACY / 2,
                MEASUREMENT_ACCURACY, MEASUREMENT_ACCURACY);

        if (showErrors || (lastMessage.isOkPoint() && m.isOkPoint())) {
            // also draw line
            double xinit = lastMessage.getX();
            double yinit = lastMessage.getY();
            r.rep1 = new Line2D.Double(xinit, yinit, xend, yend);
            r.measurement = m;
            measurementRepList.add(r);
            pruneMeasurementRepList();
            // cause repaint of whole thing for now
            repaint(getBounds());
        }
        // remember where now
        transmitter.measurement = m;
    }

    static final int MAXREPLISTSIZE = 1000;

    void pruneMeasurementRepList() {
        while (measurementRepList.size() > MAXREPLISTSIZE) {
            measurementRepList.remove(0);
        }
    }

    /**
     * Clear the measurement history
     */
    void clear() {
        measurementRepList = new ArrayList<MeasurementRep>();
        repaint(getBounds());
    }

    /**
     * Simple tuple class for storing information about a single transmitter
     * being tracked
     */
    static class TransmitterStatus {

        Color color;
        Measurement measurement;  // last seen location
    }

    /**
     * Store draw representation of a measurement (set)
     */
    static class MeasurementRep {

        void draw(Graphics2D g2) {
            g2.setPaint(color);
            g2.draw(rep1);
            if (rep2 != null) {
                g2.draw(rep2);
            }
        }

        boolean contains(Point2D pt) {
            if (rep1.contains(pt)) {
                return true;
            }
            if (rep2 != null && rep2.contains(pt)) {
                return true;
            }
            return false;
        }
        Color color;
        Shape rep1;
        Shape rep2;
        Measurement measurement;
    }
    private final static Logger log = LoggerFactory.getLogger(RpsTrackingPanel.class);
}
