// DebuggerTimePane.java
 
 package jmri.jmrix.rps.swing.debugger;

import org.apache.log4j.Logger;
import jmri.jmrix.rps.*;

import javax.swing.*;
import java.awt.*;
import javax.vecmath.Point3d;

/**
 * Pane for manual operation and debugging of the RPS system.
 *<p>
 * All index numbers here are 1-based, so they are the same
 * as the RPS hardware number.
 *
 * @author	   Bob Jacobsen   Copyright (C) 2008
 * @version   $Revision$
 */


public class DebuggerTimePane extends JPanel 
            implements ReadingListener, MeasurementListener {

    public DebuggerTimePane() {
        super();
        
        NUMSENSORS = Engine.instance().getMaxReceiverNumber();
        
        times = new JTextField[NUMSENSORS+1];
        residuals = new JLabel[NUMSENSORS+1];
        times[0] = null;
        residuals[0] = null;
        for (int i = 1; i <= NUMSENSORS; i++) {
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

    int NUMSENSORS;
    
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
        
        for (int i = 1; i<= NUMSENSORS; i++) {
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("r"+i+":"));
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
        if (times[i].getText().equals("")) {
            residuals[i].setText(""); // just blank out
            return;
        }
        try {
            if (Engine.instance().getReceiver(i) == null) {
                residuals[i].setText(""); // just blank out
                return;
            }
            Point3d p = Engine.instance().getReceiverPosition(i);
            Point3d x = new Point3d((float)m.getX(), (float)m.getY(), (float)m.getZ());
            
            double rt = p.distance(x)/Engine.instance().getVSound();
            int res = (int) (rt-m.getReading().getValue(i))-Engine.instance().getOffset();
            residuals[i].setText(""+res);
            if (log.isDebugEnabled())
                log.debug(" residual "+res+" from "+p+" vs "+x);
        } catch (Exception e) {
            residuals[i].setText(""); // just blank out
        }
    }
    
    Measurement lastPoint = null;
    
    public void notify(Reading r) {
        // Display this set of time values
        for (int i = 1; i<=Math.min(r.getNValues(), times.length-1); i++) {
            times[i].setText(nf.format(r.getValue(i)));
        }
        
    }

    public void notify(Measurement m) {
        try {
            for (int i=1; i<=NUMSENSORS; i++) 
                setResidual(i, m);
        } catch (Exception e) {
            log.error("Error setting residual: "+e);
        }
    }
    
    static Logger log = Logger.getLogger(DebuggerTimePane.class.getName());
}
