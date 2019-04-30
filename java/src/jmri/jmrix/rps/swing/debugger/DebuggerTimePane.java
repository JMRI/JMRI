package jmri.jmrix.rps.swing.debugger;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.vecmath.Point3d;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Engine;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.MeasurementListener;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.ReadingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for manual operation and debugging of the RPS system.
 * <p>
 * All index numbers here are 1-based, so they are the same as the RPS hardware
 * number.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class DebuggerTimePane extends JPanel
        implements ReadingListener, MeasurementListener {

    public DebuggerTimePane() {
        super();

        NUMSENSORS = Engine.instance().getMaxReceiverNumber();

        times = new JTextField[NUMSENSORS + 1];
        residuals = new JLabel[NUMSENSORS + 1];
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

        for (int i = 1; i <= NUMSENSORS; i++) {
            p1 = new JPanel();
            p1.setLayout(new FlowLayout());
            p1.add(new JLabel("r" + i + ":"));
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
            Point3d x = new Point3d((float) m.getX(), (float) m.getY(), (float) m.getZ());

            double rt = p.distance(x) / Engine.instance().getVSound();
            int res = (int) (rt - m.getReading().getValue(i)) - Engine.instance().getOffset();
            residuals[i].setText("" + res);
            log.debug(" residual {} from {} vs {}", res, p, x);
        } catch (Exception e) {
            residuals[i].setText(""); // just blank out
        }
    }

    Measurement lastPoint = null;

    @Override
    public void notify(Reading r) {
        // Display this set of time values
        for (int i = 1; i <= Math.min(r.getNValues(), times.length - 1); i++) {
            times[i].setText(nf.format(r.getValue(i)));
        }
    }

    @Override
    public void notify(Measurement m) {
        try {
            for (int i = 1; i <= NUMSENSORS; i++) {
                setResidual(i, m);
            }
        } catch (Exception e) {
            log.error("Error setting residual: ", e);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DebuggerTimePane.class);

}
