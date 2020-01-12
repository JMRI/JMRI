package jmri.jmrix.rps.reversealign;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.vecmath.Point3d;
import jmri.jmrix.rps.Algorithms;
import jmri.jmrix.rps.Calculator;
import jmri.jmrix.rps.Constants;
import jmri.jmrix.rps.Distributor;
import jmri.jmrix.rps.Measurement;
import jmri.jmrix.rps.PositionFile;
import jmri.jmrix.rps.Reading;
import jmri.jmrix.rps.ReadingListener;
import jmri.jmrix.rps.trackingpanel.RpsTrackingPanel;
import jmri.jmrix.rps.RpsSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gather RPS Readings and use them to align the detector.
 * <p>
 * Note that algorithms have a bias to find transmitters with positive Z
 * coordinates. Since we're inverting the computation between receivers and
 * transmitters, we also flip the sign of Z coordinates to keep this bias
 * working for us.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public class AlignmentPanel extends javax.swing.JPanel
        implements ReadingListener, Constants {

    RpsSystemConnectionMemo memo = null;

    public AlignmentPanel(RpsSystemConnectionMemo _memo) {
        super();
        memo = _memo;
        Distributor.instance().addReadingListener(this);
        nf = java.text.NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);
        nf.setGroupingUsed(false);
    }

    void initComponents() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        lines = new Line[NREADINGS];

        // add alignment lines
        for (int i = 0; i < NREADINGS; i++) {
            lines[i] = new Line();
            add(lines[i]);
        }

        // add bottom info
        JPanel p;
        p = new JPanel();
        algorithm = Algorithms.algorithmBox();
        p.add(algorithm);
        p.add(new JLabel("Vs: "));
        p.add(vs);
        vs.setText("0.01345");
        p.add(calc);
        calc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                calculate();
            }
        });
        add(p);

        p = new JPanel();
        p.add(new JLabel("X:"));
        p.add(x1l);
        p.add(x2l);
        p.add(x3l);
        p.add(x4l);
        add(p);

        p = new JPanel();
        p.add(new JLabel("Y:"));
        p.add(y1l);
        p.add(y2l);
        p.add(y3l);
        p.add(y4l);
        add(p);

        p = new JPanel();
        p.add(new JLabel("Z:"));
        p.add(z1l);
        p.add(z2l);
        p.add(z3l);
        p.add(z4l);
        add(p);

        p = new JPanel();
        p.add(new JLabel("S:"));
        p.add(stat1);
        p.add(stat2);
        p.add(stat3);
        p.add(stat4);
        add(p);

        // file load, store
        p = new JPanel();
        JButton b1;
        b1 = new JButton("Store...");
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                store();
            }
        });
        p.add(b1);

        b1 = new JButton("Load...");
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                load();
            }
        });
        p.add(b1);

        add(new JSeparator());
        add(p);

        // load for debug
        dummy3();
    }

    JFileChooser fci = jmri.jmrit.XmlFile.userFileChooser();

    void load() {
        try {
            // request the filename from an open dialog
            fci.rescanCurrentDirectory();
            int retVal = fci.showOpenDialog(this);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fci.getSelectedFile();
                if (log.isInfoEnabled()) {
                    log.info("located file " + file + " for load");
                }
                // handle the file
                PositionFile pf = new PositionFile();
                pf.loadFile(file);
                Point3d p;
                Reading r;
                for (int i = 0; i < NREADINGS; i++) {
                    p = pf.getCalibrationPosition(i);
                    if (p != null) {
                        lines[i].setPoint(p);
                    } else {
                        lines[i].setPoint(new Point3d(0.f, 0.f, 0.f));
                    }
                }
                for (int i = 0; i < NREADINGS; i++) {
                    r = pf.getCalibrationReading(i);
                    lines[i].reset();
                    if (r != null) {
                        lines[i].setReading(r);
                    }
                }
            } else {
                log.info("load cancelled in open dialog");
            }
        } catch (Exception e) {
            log.error("exception during load: " + e);
        }
    }

    void store() {
        try {
            // request the filename from an open dialog
            fci.rescanCurrentDirectory();
            int retVal = fci.showSaveDialog(this);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                File file = fci.getSelectedFile();
                if (log.isInfoEnabled()) {
                    log.info("located file " + file + " for load");
                }
                // handle the file
                PositionFile pf = new PositionFile();
                pf.prepare();
                pf.setReceiver(1, getPoint(x1l, y1l, z1l), true);
                pf.setReceiver(2, getPoint(x2l, y1l, z2l), true);
                pf.setReceiver(3, getPoint(x3l, y1l, z3l), true);
                pf.setReceiver(4, getPoint(x4l, y1l, z4l), true);

                // save the measurements too
                for (int i = 0; i < NREADINGS; i++) {
                    Point3d p = lines[i].getPoint();
                    p.z = -p.z;
                    pf.setCalibrationPoint(p, lines[i].getReading());
                }
                pf.store(file);
            } else {
                log.info("load cancelled in open dialog");
            }
        } catch (Exception e) {
            log.error("exception during load: " + e);
        }
    }

    /**
     * Service routine for finding a Point3d from input fields
     */
    Point3d getPoint(JTextField x, JTextField y, JTextField z) {
        float xval = Float.valueOf(x.getText()).floatValue();
        float yval = Float.valueOf(y.getText()).floatValue();
        float zval = Float.valueOf(z.getText()).floatValue();
        return new Point3d(xval, yval, zval);
    }

    void dummy1() {
        lines[0].xl.setText("19");
        lines[0].yl.setText("83.5");
        lines[0].zl.setText("12.1");
        lines[0].s1 = 1100.0;

        lines[1].xl.setText("1.3");
        lines[1].yl.setText("25");
        lines[1].zl.setText("14.2");
        lines[1].s1 = 4304.0;

        lines[2].xl.setText("35.9");
        lines[2].yl.setText("1.5");
        lines[2].zl.setText("13.4");
        lines[2].s1 = 5634.0;

        lines[3].xl.setText("57.1");
        lines[3].yl.setText("21.5");
        lines[3].zl.setText("13.8");
        lines[3].s1 = 4782.0;

    }

    void dummy2() {
        lines[0].xl.setText("14.2");
        lines[0].yl.setText("21.4");
        lines[0].zl.setText("2");
        lines[0].s1l.setText("" + 1274.1);
        lines[0].s2l.setText("" + 3699.3);
        lines[0].s3l.setText("" + 4764.2);
        lines[0].s4l.setText("" + 4363.3);

        lines[1].xl.setText("58.5");
        lines[1].yl.setText("14");
        lines[1].zl.setText("2");
        lines[1].s1l.setText("" + 4389.4);
        lines[1].s2l.setText("" + 1328.3);
        lines[1].s3l.setText("" + 2326.8);
        lines[1].s4l.setText("" + 3340.8);

        lines[2].xl.setText("50.1");
        lines[2].yl.setText("3.8");
        lines[2].zl.setText("2");
        lines[2].s1l.setText("" + 4005.7);
        lines[2].s2l.setText("" + 1104);
        lines[2].s3l.setText("" + 3166);
        lines[2].s4l.setText("" + 4148.3);

        lines[3].xl.setText("70.3");
        lines[3].yl.setText("47.4");
        lines[3].zl.setText("2");
        lines[3].s1l.setText("" + 5741);
        lines[3].s2l.setText("" + 3666.8);
        lines[3].s3l.setText("" + 1652);
        lines[3].s4l.setText("" + 1294.2);
    }

    void dummy3() {
        int i;

        i = 0;
        lines[i].xl.setText("70.1");
        lines[i].yl.setText("21.2");
        lines[i].zl.setText("2");
        lines[i].s1l.setText("" + 1282);
        lines[i].s2l.setText("" + 3818);
        lines[i].s3l.setText("" + 5209);
        lines[i].s4l.setText("" + 4677);

        i = 1;
        lines[i].xl.setText("25.6");
        lines[i].yl.setText("14.1");
        lines[i].zl.setText("2");
        lines[i].s1l.setText("" + 4412);
        lines[i].s2l.setText("" + 1334);
        lines[i].s3l.setText("" + 1956);
        lines[i].s4l.setText("" + 3362);

        i = 2;
        lines[i].xl.setText("32.2");
        lines[i].yl.setText("4.2");
        lines[i].zl.setText("2");
        lines[i].s1l.setText("" + 4010);
        lines[i].s2l.setText("" + 1119);
        lines[i].s3l.setText("" + 2876);
        lines[i].s4l.setText("" + 4177);

        i = 3;
        lines[i].xl.setText("14.2");
        lines[i].yl.setText("47.4");
        lines[i].zl.setText("2");
        lines[i].s1l.setText("" + 5762);
        lines[i].s2l.setText("" + 3634);
        lines[i].s3l.setText("" + 1607);
        lines[i].s4l.setText("" + 1340);

        i = 4;
        lines[i].xl.setText("70.1");
        lines[i].yl.setText("21.2");
        lines[i].zl.setText("7.5");
        lines[i].s1l.setText("" + 1083);
        lines[i].s2l.setText("" + 3765);
        lines[i].s3l.setText("" + 5247);
        lines[i].s4l.setText("" + 4216);

        i = 5;
        lines[i].xl.setText("25.6");
        lines[i].yl.setText("14.1");
        lines[i].zl.setText("7.5");
        lines[i].s1l.setText("" + 4328);
        lines[i].s2l.setText("" + 1091);
        lines[i].s3l.setText("" + 2312);
        lines[i].s4l.setText("" + 3333);

        i = 6;
        lines[i].xl.setText("32.2");
        lines[i].yl.setText("4.2");
        lines[i].zl.setText("7.5");
        lines[i].s1l.setText("" + 3959);
        lines[i].s2l.setText("" + 831);
        lines[i].s3l.setText("" + 3165);
        lines[i].s4l.setText("" + 4148);

        i = 7;
        lines[i].xl.setText("14.2");
        lines[i].yl.setText("47.4");
        lines[i].zl.setText("7.5");
        lines[i].s1l.setText("" + 5741);
        lines[i].s2l.setText("" + 3599);
        lines[i].s3l.setText("" + 1509);
        lines[i].s4l.setText("" + 1119);
    }

    @Override
    public void notify(Reading r) {
        // update lines
        for (int i = 0; i < lines.length; i++) {
            lines[i].update(r);
        }
    }

    double getVSound() {
        try {
            return Double.valueOf(vs.getText()).doubleValue();
        } catch (Exception e) {
            vs.setText("0.0344");
            return 0.0344;
        }
    }

    /**
     * FInd x, y, z of sensors from inputs
     */
    void calculate() {
        // for now, fixed offset

        int offset = 0;

        // read positions as sensor positions
        // Assume 4 right now
        // create a set of device locations
        Point3d[] points = new Point3d[NREADINGS];

        for (int i = 0; i < NREADINGS; i++) {
            points[i] = lines[i].getPoint();
        }

        // Now, for each column of times, locate that sensor
        {
            // create a Reading
            Reading r = getReading(NREADINGS, 0);

            // calculate
            Calculator c = Algorithms.newCalculator(points, getVSound(), offset, (String) algorithm.getSelectedItem());
            Measurement m = c.convert(r);

            // store
            x1l.setText(nf.format(m.getX()));
            y1l.setText(nf.format(m.getY()));
            z1l.setText(nf.format(-m.getZ()));
            stat1.setText(m.textCode());
        }
        {
            // create a Reading
            Reading r = getReading(NREADINGS, 1);

            // calculate
            Calculator c = Algorithms.newCalculator(points, getVSound(), offset, (String) algorithm.getSelectedItem());
            Measurement m = c.convert(r);

            // store
            x2l.setText(nf.format(m.getX()));
            y2l.setText(nf.format(m.getY()));
            z2l.setText(nf.format(-m.getZ()));
            stat2.setText(m.textCode());
        }
        {
            // create a Reading
            Reading r = getReading(NREADINGS, 2);

            // calculate
            Calculator c = Algorithms.newCalculator(points, getVSound(), offset, (String) algorithm.getSelectedItem());
            Measurement m = c.convert(r);

            // store
            x3l.setText(nf.format(m.getX()));
            y3l.setText(nf.format(m.getY()));
            z3l.setText(nf.format(-m.getZ()));
            stat3.setText(m.textCode());
        }
        {
            // create a Reading
            Reading r = getReading(NREADINGS, 3);

            // calculate
            Calculator c = Algorithms.newCalculator(points, getVSound(), offset, (String) algorithm.getSelectedItem());
            Measurement m = c.convert(r);

            // store
            x4l.setText(nf.format(m.getX()));
            y4l.setText(nf.format(m.getY()));
            z4l.setText(nf.format(-m.getZ()));
            stat4.setText(m.textCode());
        }
    }

    Reading getReading(int n, int index) {
        double[] vals = new double[NREADINGS];

        for (int i = 0; i < NREADINGS; i++) {
            vals[i] = lines[i].getTime(index);
        }

        return new Reading("(from alignment)", vals);
    }

    JTextField x1l = new JTextField(5);
    JTextField y1l = new JTextField(5);
    JTextField z1l = new JTextField(5);
    JTextField stat1 = new JTextField(5);

    JTextField x2l = new JTextField(5);
    JTextField y2l = new JTextField(5);
    JTextField z2l = new JTextField(5);
    JTextField stat2 = new JTextField(5);

    JTextField x3l = new JTextField(5);
    JTextField y3l = new JTextField(5);
    JTextField z3l = new JTextField(5);
    JTextField stat3 = new JTextField(5);

    JTextField x4l = new JTextField(5);
    JTextField y4l = new JTextField(5);
    JTextField z4l = new JTextField(5);
    JTextField stat4 = new JTextField(5);

    JTextField vs = new JTextField(5);
    java.text.NumberFormat nf;

    JComboBox<String> algorithm;

    Line[] lines;

    JButton calc = new JButton("Calculate");

    /**
     * Represent one line (DAQ element) of the operation
     */
    class Line extends JPanel {

        Line() {
            setLayout(new java.awt.FlowLayout());
            add(new JLabel("Position:"));
            add(xl);
            add(yl);
            add(zl);

            add(acquire);
            JButton reset = new JButton("Reset");
            reset.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    reset();
                }
            });

            add(reset);
            add(new JLabel("n:"));
            add(nl);
            add(new JLabel("Times:"));
            add(s1l);
            add(s2l);
            add(s3l);
            add(s4l);

            n = 0;
            s1 = s2 = s3 = s4 = 0.0;
        }

        double getTime(int i) {
            switch (i) {
                case 0:
                    return Float.valueOf(s1l.getText()).intValue();
                case 1:
                    return Float.valueOf(s2l.getText()).intValue();
                case 2:
                    return Float.valueOf(s3l.getText()).intValue();
                case 3:
                    return Float.valueOf(s4l.getText()).intValue();
                default:
                    return -1;
            }
        }

        Reading getReading() {
            return new Reading("(from alignment)", new double[]{getTime(0), getTime(1), getTime(2), getTime(3)});
        }

        void reset() {
            nl.setText("0");
            n = 0;
            s1l.setText("0");
            s1 = 0;
            s2l.setText("0");
            s2 = 0;
            s3l.setText("0");
            s3 = 0;
            s4l.setText("0");
            s4 = 0;
        }

        void setReading(Reading r) {
            s1l.setText("" + r.getValue(0));
            s2l.setText("" + r.getValue(1));
            s3l.setText("" + r.getValue(2));
            s4l.setText("" + r.getValue(3));
        }

        void update(Reading r) {
            if (Math.abs(r.getValue(0)) > MAXTIME
                    || Math.abs(r.getValue(1)) > MAXTIME
                    || Math.abs(r.getValue(2)) > MAXTIME
                    || Math.abs(r.getValue(3)) > MAXTIME) {
                return;
            }

            if (acquire.isSelected()) {
                s1 = (n * s1 + r.getValue(0)) / (n + 1);
                s1l.setText(nf.format(s1));

                s2 = (n * s2 + r.getValue(1)) / (n + 1);
                s2l.setText(nf.format(s2));

                s3 = (n * s3 + r.getValue(2)) / (n + 1);
                s3l.setText(nf.format(s3));

                s4 = (n * s4 + r.getValue(3)) / (n + 1);
                s4l.setText(nf.format(s4));

                n++;
                nl.setText("" + n);
            }
        }

        /**
         * Service routine for finding a Point3d from input fields
         */
        Point3d getPoint() {
            float xval = Float.valueOf(xl.getText()).floatValue();
            float yval = Float.valueOf(yl.getText()).floatValue();
            float zval = -Float.valueOf(zl.getText()).floatValue();
            return new Point3d(xval, yval, zval);
        }

        /**
         * Service routine for setting the receiver input fields from a Point3d
         */
        void setPoint(Point3d p) {
            xl.setText("" + p.x);
            yl.setText("" + p.y);
            zl.setText("" + p.z);
        }

        // data values
        JCheckBox acquire = new JCheckBox("Acquire");

        JTextField xl = new JTextField(5);
        JTextField yl = new JTextField(5);
        JTextField zl = new JTextField(5);

        JTextField nl = new JTextField(5);
        long n;

        JTextField s1l = new JTextField(5);
        JTextField s2l = new JTextField(5);
        JTextField s3l = new JTextField(5);
        JTextField s4l = new JTextField(5);
        JTextField s5l = new JTextField(5);
        JTextField s6l = new JTextField(5);
        double s1, s2, s3, s4, s5, s6;
    }

    private final static Logger log = LoggerFactory.getLogger(RpsTrackingPanel.class);

}
