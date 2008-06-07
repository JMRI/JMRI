// RpsTrackingPanel.java

package jmri.jmrix.rps.trackingpanel;

import jmri.jmrix.rps.*;

import java.awt.*;
import java.awt.geom.*;
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
 * @version   $Revision: 1.5 $
 */
public class RpsTrackingPanel extends javax.swing.JPanel 
    implements MeasurementListener {
    
    public RpsTrackingPanel() {
        super();
        Distributor.instance().addMeasurementListener(this);
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
    static final Color regionColor = new Color(255, 128, 128);
    // static final Color measurementColor = new Color(0,0,0);
    int measurementColor = 0;
    
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
        AffineTransform newAT = new AffineTransform();        
        newAT.translate(0, this.getHeight());
        newAT.scale(xscale, -yscale);
        newAT.translate(-xorigin,-yorigin);  // put origin in bottom corner
        g2.setTransform(newAT);

        // Draw the regions
        g2.setPaint(regionColor);
        List l = Model.instance().getRegions();
        for (int i = 0; i<l.size(); i++) {
            // g2.draw(((Region)l.get(i)).getPath()); // border (same color)
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
        Color color;
        Shape rep1;
        Shape rep2;
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RpsTrackingPanel.class.getName());
}
