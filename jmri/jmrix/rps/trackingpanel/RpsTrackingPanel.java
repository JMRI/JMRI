// RpsTrackingPanel.java

package jmri.jmrix.rps.trackingpanel;

import jmri.jmrix.rps.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

/**
 *
 * Pane to show a 2D representation of the RPS Model and Measurements.
 *<p>
 * @see jmri.jmrix.rps.Model
 * @see jmri.jmrix.rps.Measurement
 *
 * @author	   Bob Jacobsen   Copyright (C) 2006, 2008
 * @version   $Revision: 1.7 $
 */
public class RpsTrackingPanel extends javax.swing.JPanel 
    implements MeasurementListener {
    
    public RpsTrackingPanel() {
        super();
        Distributor.instance().addMeasurementListener(this);
        setToolTipText("<no item>");  // activates ToolTip, sets default
    }
    
    /**
     * Provide tool tip text that depends on 
     * what's under the cursor.
     * <p>
     * Names either a measurement point
     * or a region.
     * @return null if no object under mouse; this suppresses ToolTip
     */
    public String getToolTipText(MouseEvent e) {
        // get mouse coordinates
        try {
            Point mouse = e.getPoint();
            Point2D userPt = currentAT.inverseTransform(new Point2D.Double((double)mouse.x, (double)mouse.y), null);
            // find the path object containing it, if any
            for (int i = measurementRepList.size()-1; i>=0 ; i--) {
                MeasurementRep r = (MeasurementRep)measurementRepList.get(i);
                if (r.contains(userPt)) {
                    Measurement m = r.measurement;
                    return "ID "+m.getID()+" at "+m.getX()+","+m.getY();
                }
            }

            // find the region containing it, if any
            // Go through backwards to find the top if overlaps
            List l = Model.instance().getRegions();
            for (int i = l.size()-1; i>=0; i--) {
                Shape s = ((Region)l.get(i)).getPath();
                if (s.contains(userPt)) {
                    return "Region: "+((Region)l.get(i)).toString();
                }
            }
        } catch (Exception ex) {} // just skip to default
        // or return default
        return null;
    }
    
    /**
     * Sets the coordinates of the lower left corner of
     * the screen/paper.  Note this is different from
     * the usual Swing coordinate system!
     */
    public void setOrigin(double x, double y) {
        xorigin = x;
        yorigin = y;
    }
    
    void setShowErrors(boolean show) {
        this.showErrors = show;
    }
    
    boolean showErrors = false;
    
    /**
     * Sets the coordinates of the upper-right corner of
     * the screen/paper.  Note this is different from
     * the usual Swing coordinate system!
     */
    public void setCoordMax(double x, double y) {
        xmax = x;
        ymax = y;
    }
    
    double xorigin, yorigin;
    double xmax, ymax;
    
    static final double MEASUREMENT_ACCURACY = 0.2; // in user units
    static final Color regionFillColor = Color.BLUE.brighter();
    static final Color regionOutlineColor = Color.GRAY.darker();
    // static final Color measurementColor = new Color(0,0,0);
    int measurementColor = 0;
    
    // current transform to graphics coordinates
    AffineTransform currentAT;
    
    public void paint(Graphics g) {
        // draw everything else
        super.paint(g);

        // Now show regions
        // First, Graphics2D setup
        Graphics2D g2 = (Graphics2D) g;
        double xscale = this.getWidth()/(xmax-xorigin);
        double yscale = this.getHeight()/(ymax-yorigin);
        Stroke stroke = new BasicStroke((float)(2./xscale), 
                                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);
        // Save the current transform
        AffineTransform saveAT = g2.getTransform();
        // Install the new one
        currentAT = new AffineTransform();        
        currentAT.translate(0, this.getHeight());
        currentAT.scale(xscale, -yscale);
        currentAT.translate(-xorigin,-yorigin);  // put origin in bottom corner
        g2.setTransform(currentAT);

        // Draw the regions
        List l = Model.instance().getRegions();
        for (int i = 0; i<l.size(); i++) {
            g2.setPaint(regionOutlineColor);
            g2.draw(((Region)l.get(i)).getPath()); // border (same color)
            g2.setPaint(regionFillColor);
            g2.fill(((Region)l.get(i)).getPath());
        }

        // Draw the measurements; changes graphics
        for (int i = 0; i<measurementRepList.size(); i++) {
            ((MeasurementRep)measurementRepList.get(i)).draw(g2);
        }
        // restore original transform
        g2.setTransform(saveAT);
    }
    
    ArrayList measurementRepList = new ArrayList();
    java.util.HashMap transmitters = new java.util.HashMap(); // TransmitterStatus, keyed by Integer(measurement id)
    
    /**
     * Pick a color for the next set of measurement lines to draw
     */
    Color nextColor() {
        int red = Math.min(255, ((measurementColor>>2)&0x1)*255/1);
        int green = Math.min(255, ((measurementColor>>1)&0x1)*255/1);
        int blue = Math.min(255, ((measurementColor>>0)&0x1)*255/1);
        measurementColor++;
        return new Color(red, green, blue);
    }
    
    public void notify(Measurement m) {
        Integer id = new Integer(m.getID());
        TransmitterStatus transmitter = (TransmitterStatus)transmitters.get(id);
        double xend = m.getX();
        double yend = m.getY();
        if (transmitter == null) {
            // create Transmitter status with current measurement
            // so we can draw line next time
            transmitter = new TransmitterStatus();
            transmitter.measurement = m;
            transmitter.color = nextColor();
            
            transmitters.put(id, transmitter);

            // display just the point
            MeasurementRep r = new MeasurementRep();
            r.color = transmitter.color;
            r.rep1 = new Ellipse2D.Double(xend-MEASUREMENT_ACCURACY/2, 
                                   yend-MEASUREMENT_ACCURACY/2, 
                                   MEASUREMENT_ACCURACY, MEASUREMENT_ACCURACY);
            r.measurement = m;
            measurementRepList.add(r);

            return;
        }
        Measurement lastMessage = transmitter.measurement;
                
        MeasurementRep r = new MeasurementRep();
        r.color = transmitter.color;
        r.rep2 = new Ellipse2D.Double(xend-MEASUREMENT_ACCURACY/2, 
                               yend-MEASUREMENT_ACCURACY/2, 
                               MEASUREMENT_ACCURACY, MEASUREMENT_ACCURACY);
        if (showErrors || (lastMessage.getCode() <= 0 && m.getCode() <= 0)) {
            // also draw line
            double xinit = lastMessage.getX();
            double yinit = lastMessage.getY();
            r.rep1 = new Line2D.Double(xinit, yinit, xend, yend);
            r.measurement = m;
            measurementRepList.add(r);
            // remember where now
            transmitter.measurement = m;        
        }
    }
    
    /**
     * Simple tuple class for storing information
     * about a single transmitter being tracked
     */
    class TransmitterStatus {
        Color color;
        Measurement measurement;  // last seen location
    }
    
    /**
     * Store draw representation of a measurement (set)
     */
    class MeasurementRep {
        void draw(Graphics2D g2) {
            g2.setPaint(color);
            g2.draw(rep1);
            if (rep2!=null) g2.draw(rep2);
        }
        boolean contains(Point2D pt) {
            if (rep1.contains(pt)) return true;
            if (rep2 != null && rep2.contains(pt)) return true;
            return false;
        }
        Color color;
        Shape rep1;
        Shape rep2;
        Measurement measurement;
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RpsTrackingPanel.class.getName());
}
