// RpsTrackingPanel.java

package jmri.jmrix.rps.trackingpanel;

import jmri.jmrix.rps.*;

/**
 *
 * Fram to show a 2D representation of the RPS Measurements
 * 
 * @author	   Bob Jacobsen   Copyright (C) 2006, 2008
 * @version   $Revision: 1.3 $
 */
public class RpsTrackingPanel extends javax.swing.JPanel 
        implements MeasurementListener {

    public RpsTrackingPanel() {
        super();
        Distributor.instance().addMeasurementListener(this);
    }

    Measurement lastMessage = null;
    
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
    
    public void notify(Measurement m) {
        if (lastMessage == null) {
            // draw line next time
            lastMessage = m;
            return;
        }
        if (showErrors || (lastMessage.getCode() <= 0 && m.getCode() <= 0)) {
            // find coordinates in RPS space
            double xinit = lastMessage.getX();
            double yinit = lastMessage.getY();
        
            double xend = m.getX();
            double yend = m.getY();
        
            log.debug("panel w: "+getWidth()+" h: "+getHeight());
            // rescale to screen coordinates
            int xs_init = (int) Math.round((xinit-xorigin)/(xmax-xorigin)*getWidth());
            int ys_init = (int) Math.round((ymax-yinit)/(ymax-yorigin)*getHeight());

            int xs_end = (int) Math.round((xend-xorigin)/(xmax-xorigin)*getWidth());
            int ys_end = (int) Math.round((ymax-yend)/(ymax-yorigin)*getHeight());
        
            getGraphics().drawLine(xs_init, ys_init, xs_end, ys_end);
        }
                
        lastMessage = m;
        
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RpsTrackingPanel.class.getName());
}
