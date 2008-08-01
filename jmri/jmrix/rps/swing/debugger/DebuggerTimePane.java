// DebuggerTimePane.java
 
 package jmri.jmrix.rps.swing.debugger;

import jmri.jmrix.rps.*;

import javax.swing.*;
import java.awt.*;
import javax.vecmath.Point3d;
import java.io.*;

/**
 * Pane for manual operation and debugging of the RPS system
 *
 * @author	   Bob Jacobsen   Copyright (C) 2008
 * @version   $Revision: 1.1 $
 */


public class DebuggerTimePane extends JPanel 
            implements ReadingListener, MeasurementListener {

    public DebuggerTimePane() {
        super();
        
        NUMSENSORS = Engine.instance().getReceiverCount();
        
        times = new JTextField[NUMSENSORS];
        residuals = new JLabel[NUMSENSORS];
        
        for (int i = 0; i < NUMSENSORS; i++) {
            times[i] = new JTextField(10);
            times[i].setText("");
            residuals[i] = new JLabel("          ");
        }
    }

    public void dispose() {
        // separate from data source
        Distributor.instance().removeReadingListener(this);
        Distributor.instance().removeMeasurementListener(this);
    }

    java.text.NumberFormat nf;

    int NUMSENSORS = 6;
    
    JTextField[] times;
    JLabel[] residuals;
    
    public void initComponents() {
        nf = java.text.NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel p1;
        JPanel p3 = new JPanel();
        p3.setLayout(new java.awt.GridLayout(NUMSENSORS, 2));
        
        for (int i = 0; i< NUMSENSORS; i++) {
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("r"+(i+1)+":"));
            p1.add(times[i]);   
            p3.add(p1);
            p1 = new JPanel();
            p1.add(new JLabel("r-t: "));
            p1.add(residuals[i]);
            p3.add(p1);         
        }
        add(p3);
    }
        
    void setResidual(int i, Measurement m) {
        Point3d p = Engine.instance().getReceiverPosition(i+1);
        Point3d x = new Point3d((float)m.getX(), (float)m.getY(), (float)m.getZ());
        
        double rt = p.distance(x)/Engine.instance().getVSound();
        int res = (int) (rt-m.getReading().getValue(i));
        residuals[i].setText(""+res);
    }
    
    Measurement lastPoint = null;
    
    public void notify(Reading r) {
        // This implementation creates a new Calculator
        // each time to ensure that the most recent
        // receiver positions are used; this should be
        // replaced with some notification system
        // to reduce the work used.

        // Display this set of time values
        for (int i = 0; i<Math.min(r.getNSample(), times.length); i++) {
            times[i].setText(nf.format(r.getValue(i)));
        }
        
    }

    public void notify(Measurement m) {
        try {
            for (int i=0; i<NUMSENSORS; i++) 
                setResidual(i, m);
        } catch (Exception e) {
            log.error("Error setting residual: "+e);
        }
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DebuggerTimePane.class.getName());
}
